<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="keys" class="com.cyberark.common.ConjurJspKey"/>

<style type="text/css">
    .auth-container {
        display: none;
    }
</style>

<tr>
    <td><label for="displayName">Display name:</label><l:star/></td>
    <td>
        <props:textProperty name="displayName" className="longField"/>
        <span class="smallNote">Provide some name to distinguish this connection from others.</span>
        <span class="error" id="error_displayName"></span>
    </td>
</tr>

<tr>
    <td><label for="${keys.applianceUrl}">Conjur Appliance URL:</label><l:star/></td>
    <td>
        <props:textProperty name="${keys.applianceUrl}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.applianceUrl}"/>
        <span class="smallNote">e.g. https://conjur-follower.company.local</span>
    </td>
</tr>

<tr>
    <td><label for="${keys.account}">Conjur Account:</label><l:star/></td>
    <td>
        <props:textProperty name="${keys.account}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.account}"/>
        <span class="smallNote">e.g. companyName</span>
    </td>
</tr>

<tr>
    <td><label for="${keys.authnLogin}">Conjur Authn Login:</label><l:star/></td>
    <td>
        <props:textProperty name="${keys.authnLogin}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.authnLogin}"/>
        <span class="smallNote">e.g. host/teamcity/projectName</span>
    </td>
</tr>

<tr>
    <td><label for="${keys.apiKey}">Conjur API Key:</label><l:star/></td>
    <td>
        <props:passwordProperty name="${keys.apiKey}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.apiKey}"/>
    </td>
</tr>

<tr>
    <td><label for="${keys.certFile}">Conjur Certificate:</label></td>
    <td>
        <props:multilineProperty expanded="true" name="${keys.certFile}" className="longField textProperty_max-width js_max-width"
                             rows="4" cols="45" linkTitle="Conjur Certificate"/>
        <span class="error" id="error_${keys.certFile}"/>
        <span class="smallNote">The public certificate chain used to establish TLS connection to the Conjur API</span>
    </td>
</tr>

<tr>
    <td><label for="${keys.failOnError}">Fail in case of error</label></td>
    <td>
        <props:checkboxProperty name="${keys.failOnError}"/>
        <span class="error" id="error_${keys.failOnError}"/>
        <span class="smallNote">Whether to fail builds in case of parameter resolving error</span>
    </td>
</tr>

<tr>
    <td><label for="${keys.verboseLogging}">Enable verbose logging</label></td>
    <td>
        <props:checkboxProperty name="${keys.verboseLogging}"/>
        <span class="error" id="error_${keys.verboseLogging}"/>
        <span class="smallNote">Whether to enable verbose logging</span>
    </td>
</tr>