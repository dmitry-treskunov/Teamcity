package com.cyberark.server;

import com.cyberark.common.exceptions.ConjurApiAuthenticateException;
import jetbrains.buildServer.BuildProblemData;
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

    // TODO: Currently when retrieving the connection type, we find the first connection that meets the `providerType`
    //   and then return that object. It is possible to define multiple connections. I think if multiples are defined
    //   an error should be returned. And only accept 1 connection per project for the time being.
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

    private BuildProblemData createBuildProblem(SBuild build, String message) {
        return BuildProblemData.createBuildProblem(build.getBuildNumber(), ConjurSettings.getFeatureType(), message);
    }

    @Override
    public void updateParameters(BuildStartContext context) {
        // TODO: For now we are going to implement all the logic on the Teamcity server rather than the agent
        //   This means that this method will retrieve the secrets and then set them for the actual agent
        //   However when we implement secret retrieval on the agent we will need to get the `Connection` info
        //   And pass that to the agent
        //   the agent will then use that `Connection` info to establish a connection to the Conjur REST API and
        //   retrieve the secrets on the agent
        //   This will allow the ability to put CIDR restrictions on an API key so it can only run on specific
        //   Teamcity agents.

        SRunningBuild build = context.getBuild();

        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            // It is possible of build type to be null, if this is the case lets return and not retrieve conjur secrets
            return;
        }
        SProject project = buildType.getProject();

        SProjectFeatureDescriptor connectionFeatures = getConnectionType(project, ConjurSettings.getFeatureType());
        if (connectionFeatures == null) {
            // If connection feature cannot be found (no connection has been configured on this project)
            // then return and do not perform conjur secret retrieval actions
            return;
        }

        ConjurConnectionParameters conjurConfig = new ConjurConnectionParameters(connectionFeatures.getParameters());

        ConjurConfig config = new ConjurConfig(
                conjurConfig.getApplianceUrl(),
                conjurConfig.getAccount(),
                conjurConfig.getAuthnLogin(),
                conjurConfig.getApiKey(),
                null,
                conjurConfig.getCertFile());

        Map<String, String> buildParams = build.getBuildOwnParameters();
        Map<String, String> conjurVariables = getVariableIdsFromBuildParameters(buildParams);

        if (conjurVariables.size() == 0) {
            // No conjur variables are present in the build parameters, if this is the case lets not attempt to
            // authenticate and just return
            return;
        }

        ConjurApi client = new ConjurApi(config);
        try {
            client.authenticate();

            // TODO: Implement failOnError around here
            for(Map.Entry<String, String> kv : conjurVariables.entrySet()) {
                HttpResponse response = client.getSecret(kv.getValue());
                if (response.statusCode != 200 && conjurConfig.getFailOnError()) {
                    BuildProblemData buildProblem = createBuildProblem(build,
                            String.format("ERROR: Retrieving secret '%s' from conjur. Received status code '%d'",
                                    kv.getValue(), response.statusCode));
                    build.addBuildProblem(buildProblem);
                    return;
                }

                kv.setValue(response.body);
            }

        } catch (ConjurApiAuthenticateException e) {
            BuildProblemData buildProblem = createBuildProblem(build,
                    String.format("ERROR: Authenticating to conjur at '%s' with account '%s' and with login '%s'. %s",
                            conjurConfig.getApplianceUrl(),
                            conjurConfig.getAccount(),
                            conjurConfig.getAuthnLogin(),
                            e.getMessage()));
            build.addBuildProblem(buildProblem);
            return;
        }
        catch (Exception e) {
            BuildProblemData buildProblem = createBuildProblem(build,
                    String.format("ERROR: Generic error returned when establishing connection to conjur. %s",
                            e.getMessage()));
            build.addBuildProblem(buildProblem);
            return;
        }

        // If we make it here `conjurVariables` Map<String, String> should contain the parameters names and the actual values.
        for(Map.Entry<String, String> kv : conjurVariables.entrySet()) {
            context.addSharedParameter(kv.getKey(), kv.getValue());
        }
    }
}
