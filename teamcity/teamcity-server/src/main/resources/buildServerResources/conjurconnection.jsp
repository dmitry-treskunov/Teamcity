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
    <td><label for="${keys.namespace}">Conjur Appliance URL:</label></td>
    <td>
        <props:textProperty name="${keys.namespace}"
                            className="longField textProperty_max-width js_max-width"/>
        <span class="error" id="error_${keys.namespace}"/>
    </td>
</tr>