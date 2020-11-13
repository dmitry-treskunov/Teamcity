package com.cyberark.server;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.oauth.OAuthConstants;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import com.cyberark.common.*;

public class ConjurBuildStartContextProcessor implements BuildStartContextProcessor {

    public ByteArrayInputStream getInputStreamFromString(String input) throws IOException {
        return new ByteArrayInputStream(input.getBytes());
    }

    // TODO: I think this method works.
    private SSLContext getSSLContext(String certContents) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(getInputStreamFromString(certContents));

        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null);
        ks.setCertificateEntry("conjurTlsCaPath", cert);
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext conjurSSLContext = SSLContext.getInstance("TLS");
        conjurSSLContext.init(null, tmf.getTrustManagers(), null);
        return conjurSSLContext;
    }

    // This method will turn a map of SOMETHING = %conjur:some/secret% into
    // SOMETHING = some/secret
    // input == {
    //   "env.SECRET": "%conjur:super/secret%",
    //   "env.DB_PASS": "%conjur:db/mysql/username%",
    //   "TEAMCITY_BUILD": "22"
    // }
    //
    // All non-conjur variables should not be returned
    // Also the %conjur: and % should be removed from the value
    // The key should remain the same
    // output == {
    //   "env.SECRET": "super/secret",
    //   "env.DB_PASS": "db/mysql/username"
    // }
    private Map<String, String> getVariableIdsFromBuildParameters(Map<String, String> parameters) {
        Map<String, String> variableIds = new java.util.HashMap<>(Collections.emptyMap());

        for (Map.Entry<String, String> kv : parameters.entrySet() ) {
            String variableIdPrefix = "%conjur:";
            String variableIdSuffix = "%";

            if (kv.getValue().startsWith(variableIdPrefix) && kv.getValue().endsWith(variableIdSuffix)) {
                // This value represents that this parameter needs to be replaced
                String id = kv.getValue().trim();
                id = id.substring(variableIdPrefix.length());
                id = id.substring(0, id.length()-variableIdSuffix.length());

                variableIds.put(kv.getKey(), id);
            }
        }

        return variableIds;
    }

    private SProjectFeatureDescriptor getConnectionType(SProject project, String providerType) {
        Iterator<SProjectFeatureDescriptor> it = project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE).iterator();
        while(it.hasNext()) {
            SProjectFeatureDescriptor desc = it.next();
            String connectionType = desc.getParameters().get(OAuthConstants.OAUTH_TYPE_PARAM);

            if (connectionType.equals(providerType)) {
                // TODO: Some of these print statements should be logged via the Teamcity logger (If its possible)
                // System.out.printf("Found connection feature for TYPE '%s'\n", providerType);
                return desc;
            }
        }
        return null;
    }

    @Override
    public void updateParameters(BuildStartContext context) {
        // TODO: For now we are going to implement all the logic on the Teamcity server rather than the agent
        // This means that this method will retrieve the secrets and then set them for the actual agent
        // However when we implement secret retrieval on the agent we will need to get the `Connection` info
        // And pass that to the agent
        // the agent will then use that `Connection` info to establish a connection to the Conjur REST API and
        // retrieve the secrets on the agent
        // This will allow the ability to put CIDR restrictions on an API key so it can only run on specific
        // Teamcity agents.

        SRunningBuild build = context.getBuild();
        SProject project = build.getBuildType().getProject();

        Map<String, String> buildParams = build.getBuildOwnParameters();
        Map<String, String> conjurVariables = getVariableIdsFromBuildParameters(buildParams);


        // TODO: Connectiomn should not be hard coded
        SProjectFeatureDescriptor connectionFeatures = getConnectionType(project, "Connection");
        ConjurConnectionParameters conjurConfig = new ConjurConnectionParameters(connectionFeatures.getParameters());
        ConjurConfig config = new ConjurConfig(
                conjurConfig.getApplianceUrl(),
                conjurConfig.getAccount(),
                conjurConfig.getAuthnLogin(),
                conjurConfig.getApiKey());

        // TODO: Add ability to add the certificate to the client so
        //  I do not have to hard code in to ignore SSL Cert verification
        config.ignoreSsl = true;
        ConjurApi client = new ConjurApi(config);


        try {
            // Conjur conjur = new Conjur(authnLogin, apiKey, getSSLContext(certFile));
            client.authenticate();

            // TODO: Implement failOnError around here
            for(Map.Entry<String, String> kv : conjurVariables.entrySet()) {
                HttpResponse response = client.getSecret(kv.getValue());
                if (response.statusCode != 200) {
                    System.out.printf("ERROR: Received status code '%d'. %s", response.statusCode, response.body);
                    return;
                }

                kv.setValue(response.body);
            }

        } catch (Exception e) {

            // TODO: Gotta figure out how to make this look prettier
            //  I think it is okay to catch all exceptions here, as long as we can forward the exception
            //  To some type of log messages and to the build.
            //  Maybe create an exception that wraps all of these exceptions called
            //  ConjurBuildStartUpdateParametersException, just make sure we can include an inner exception
            e.printStackTrace();
            System.out.println("AN ERROR HAS OCCURED: " + e.toString());
            return;
        }

        // If we make it here `conjurVariables` Map<String, String> should contain the parameters names and the actual values.
        for(Map.Entry<String, String> kv : conjurVariables.entrySet()) {
            context.addSharedParameter(kv.getKey(), kv.getValue());
        }
    }
}
