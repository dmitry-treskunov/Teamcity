package com.cyberark.common;

public class ConjurJspKey {
    public ConjurJspKey() { }

    private String applianceUrl = "url";
    private String account = "account";
    private String authnLogin = "login";
    private String apiKey = "apiKey";
    private String certFile = "certFile";
    private String failOnError = "failOnError";
    private String verboseLogging = "verboseLogging";

    public String getApplianceUrl(){
        return this.applianceUrl;
    }

    public String getAccount(){
        return this.account;
    }

    public String getAuthnLogin(){
        return this.authnLogin;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getCertFile() { return this.certFile; }

    public String getFailOnError() { return this.failOnError; }

    public String getVerboseLogging() { return this.verboseLogging; }
}