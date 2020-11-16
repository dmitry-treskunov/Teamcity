package com.cyberark.common;

import com.cyberark.common.exceptions.*;

public class ConjurApi {
    public ConjurConfig config;

    public ConjurApi(ConjurConfig config) {
        this.config = config;
        if(this.config.ignoreSsl) {
            HttpClient.disableSSL();
        }
    }

    public HttpResponse health() {
        String url = this.config.url + "/health";
        return HttpClient.get(url, "", this.config.certificateContent);
    }

    private String getAuthenticatorUrl() throws ConjurApiAuthenticateException {
        // url used to authenticate via api key
        String url = this.config.authenticateUrl();

        // use a non-default authenticator
        if(this.config.authnType != null) {
            // throw exception if invalid authnType, currently only iam and oidc supported
            // TODO: oidc has not been tested
            if(this.config.authnType != null && this.config.authnType != "iam" && this.config.authnType != "oidc") {
                throw new ConjurApiAuthenticateException("Invalid ConjurConfig.authnType. Valid values are null, iam or oidc");
            }

            // validate service ID and apiKey are populated
            if(this.config.serviceId == null) {
                throw new ConjurApiAuthenticateException("ConjurConfig.serviceId is mandatory");
            }
            if(this.config.apiKey == null) {
                throw new ConjurApiAuthenticateException("ConjurConfig.apiKey is mandatory");
            }

            url = this.config.authenticateServiceUrl();
        }

        return url;
    }

    public void authenticate() throws ConjurApiAuthenticateException {
        String url = getAuthenticatorUrl();
        HttpResponse response = HttpClient.post(url, "", this.config.apiKey, this.config.certificateContent);
        // successful authentication returns 200 status code
        if(response.statusCode != 200) {
            throw new ConjurApiAuthenticateException("Failed to authenticate. Received status code: " + response.statusCode);
        }

        // create the authorization token used for subsequent calls
        String token = '"' + HttpClient.base64Encode(response.body) + '"';
        String tokenHeader = "Token token=" + token;
        this.config.sessionToken = tokenHeader;
    }

    public HttpResponse list() {
        String url = this.config.listUrl();
        return HttpClient.get(url, this.config.sessionToken, this.config.certificateContent);
    }

    public HttpResponse appendPolicy(String policyBranch, String policyContent) {
        String url = this.config.loadPolicyUrl(policyBranch);
        return HttpClient.post(url, this.config.sessionToken, policyContent, this.config.certificateContent);
    }

    public HttpResponse replacePolicy(String policyBranch, String policyContent) {
        String url = this.config.loadPolicyUrl(policyBranch);
        return HttpClient.put(url, this.config.sessionToken, policyContent, this.config.certificateContent);
    }

    public HttpResponse patchPolicy(String policyBranch, String policyContent) {
        String url = this.config.loadPolicyUrl(policyBranch);
        return HttpClient.patch(url, this.config.sessionToken, policyContent, this.config.certificateContent);
    }

    public HttpResponse getSecret(String secretId) {
        String url = this.config.secretUrl(secretId);
        return HttpClient.get(url, this.config.sessionToken, this.config.certificateContent);
    }

    public HttpResponse setSecret(String secretId, String secretValue) {
        String url = this.config.secretUrl(secretId);
        return HttpClient.post(url, this.config.sessionToken, secretValue, this.config.certificateContent);
    }
}