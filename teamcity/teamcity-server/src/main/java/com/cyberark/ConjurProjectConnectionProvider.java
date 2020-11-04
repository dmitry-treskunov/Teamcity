package com.cyberark;

import jetbrains.buildServer.serverSide.oauth.OAuthProvider;
import jetbrains.buildServer.web.openapi.PluginDescriptor;


public class ConjurProjectConnectionProvider extends OAuthProvider {
	private PluginDescriptor descriptor;

    public ConjurProjectConnectionProvider(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
	}

	@Override
	public String getDisplayName() {
		return "Cyberark Conjur";
	}

	@Override
	public String getType() { return "Connection"; }

	@Override
	public String getEditParametersUrl() {
		return this.descriptor.getPluginResourcesPath("conjurconnection.jsp");
	}
}