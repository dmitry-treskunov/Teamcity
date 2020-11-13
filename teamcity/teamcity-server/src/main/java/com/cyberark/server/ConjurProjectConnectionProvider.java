package com.cyberark.server;

import com.cyberark.common.ConjurConnectionParameters;
import com.cyberark.common.ConjurJspKey;
import com.cyberark.common.ConjurSettings;
import jetbrains.buildServer.serverSide.oauth.OAuthProvider;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor;
import java.util.Map;

import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;


public class ConjurProjectConnectionProvider extends OAuthProvider {
	final private PluginDescriptor descriptor;

    public ConjurProjectConnectionProvider(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
	}

	@Override
	@NotNull
	public String getDisplayName() {
		return ConjurSettings.getConnectionName();
	}

	@Override
	@NotNull
	public String getType() { return ConjurSettings.getFeatureType(); }

	@Override
	public String getEditParametersUrl() {
		return this.descriptor.getPluginResourcesPath("conjurconnection.jsp");
	}

	@Override
	@NotNull
	public String describeConnection(OAuthConnectionDescriptor connection) {
		ConjurConnectionParameters parameters = new ConjurConnectionParameters(connection.getParameters());

		return String.format("Connection to Cyberark Conjur server at '%s' with login '%s'",
				parameters.getApplianceUrl(), parameters.getAuthnLogin());
	}
}