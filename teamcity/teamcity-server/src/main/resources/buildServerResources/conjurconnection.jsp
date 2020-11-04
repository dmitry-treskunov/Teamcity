<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="keys" class="com.cyberark.ConjurJspKey"/>

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
    <td><label for="${keys.applianceUrl}">Conjur Appliance URL:</label></td>
    <td>
        <props:textProperty name="${keys.applianceUrl}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.applianceUrl}"/>
    </td>
</tr>

<tr>
    <td><label for="${keys.account}">Conjur Account:</label></td>
    <td>
        <props:textProperty name="${keys.account}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.account}"/>
    </td>
</tr>

<tr>
    <td><label for="${keys.authnLogin}">Conjur Authn Login:</label></td>
    <td>
        <props:textProperty name="${keys.authnLogin}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.authnLogin}"/>
    </td>
</tr>

<tr>
    <td><label for="${keys.apiKey}">Conjur API Key:</label></td>
    <td>
        <props:passwordProperty name="${keys.apiKey}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.apiKey}"/>
    </td>
</tr>