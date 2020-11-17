package com.cyberark.common;

import java.util.Map;

public class ConjurConnectionParameters {
    private String apiKey;
    private String authnLogin;
    private String applianceUrl;
    private String account;
    private String certFile;
    private String failOnError;
    private String verboseLogging;

    public ConjurConnectionParameters(Map<String, String> parameters) {
        ConjurJspKey conjurKeys = new ConjurJspKey();
        this.apiKey = parameters.get(conjurKeys.getApiKey());
        this.applianceUrl = parameters.get(conjurKeys.getApplianceUrl());
        this.authnLogin = parameters.get(conjurKeys.getAuthnLogin());
        this.account = parameters.get(conjurKeys.getAccount());
        this.certFile = parameters.get(conjurKeys.getCertFile());
        this.failOnError = parameters.get(conjurKeys.getFailOnError());
    }

    private boolean validateUrl(String url) {
        if (url.startsWith("https://") || url.startsWith("http://")) {
            return true;
        }
        return false;
    }

    public boolean isValidUrl(){ return this.validateUrl(this.applianceUrl); };

    public String getApplianceUrl(){
        return this.applianceUrl.trim();
    }
    public String getAccount(){ return this.account.trim(); }
    public String getAuthnLogin(){ return this.authnLogin.trim();}
    public String getApiKey(){ return this.apiKey.trim();}
    public String getCertFile(){ return this.certFile.trim(); }
    public boolean getFailOnError(){ return this.failOnError.equals("true"); }
    public boolean getVerboseLogging() {return this.verboseLogging.equals("true"); }

}