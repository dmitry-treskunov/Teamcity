package com.cyberark;

public class ConjurJspKey {
    public ConjurJspKey() { }

    private String applianceUrl = "url";
    private String account = "account";
    private String authnLogin = "login";
    private String apiKey = "apiKey";

    public String getApplianceUrl(){
        return this.applianceUrl;
    }

    public String getAccount(){
        return this.account;
    }

    public String getAuthnLogin(){
        return this.authnLogin;
    }

    public String getApiKey(){
        return this.apiKey;
    }
}