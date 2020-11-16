package com.cyberark.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class ConjurConfig {
    public String url;
    public String account;
    public String sessionToken;
    public String username;
    public String apiKey;
    public String authnType;
    public String serviceId;
    public Boolean ignoreSsl;
    public String certificateContent;

    public ConjurConfig(String url, String account) {
        this(url, account, null, null, null, null);
    }

    public ConjurConfig(String url, String account, String username) {
        this(url, account, username, null, null, null);
    }

    public ConjurConfig(String url, String account, String username, String apiKey) {
        this(url, account, username, apiKey, null, null);
    }

    public ConjurConfig(String url, String account, String username, String apiKey, String sessionToken, String certificateContent) {
        this.url = url.trim();
        this.account = account;
        this.username = username;
        this.apiKey = apiKey;
        this.sessionToken = sessionToken;
        this.ignoreSsl = false;
        this.serviceId = null;
        this.authnType = null;
        this.certificateContent = certificateContent;
    }

    public String authenticateUrl() {
        try {
            return this.url + "/authn/" + this.account + "/" + URLEncoder.encode(this.username, "UTF-8") + "/authenticate";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String authenticateServiceUrl() {
        try {
            return this.url + "/authn-" + this.authnType + "/" + this.serviceId + "/" + this.account + "/" + URLEncoder.encode(this.username, "UTF-8") + "/authenticate";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String listUrl() {
        return this.url + "/resources/" + this.account;
    }

    public String loadPolicyUrl(String branch) {
        try {
            return this.url + "/policies/" + this.account + "/policy/" + URLEncoder.encode(branch, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String secretUrl(String secretId) {
        try {
            return this.url + "/secrets/" + this.account + "/variable/" + URLEncoder.encode(secretId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}