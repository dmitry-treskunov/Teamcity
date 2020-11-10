package com.cyberark;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agentServer.AgentBuild;
import jetbrains.buildServer.artifacts.ArtifactDependencyInfo;
import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.serverSide.*;

import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import jetbrains.buildServer.serverSide.oauth.OAuthConstants;
import jetbrains.buildServer.util.Option;
import jetbrains.buildServer.util.PasswordReplacer;
import jetbrains.buildServer.vcs.VcsChangeInfo;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.vcs.VcsRootEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.*;

public class ConjurParametersProvider extends AbstractBuildParametersProvider {

    private boolean isFeatureEnabled(SBuild build) {
        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            return false;
        }

        SProject project = buildType.getProject();
        Collection<SProjectFeatureDescriptor> availableFeatures = project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE);

        Iterator<SProjectFeatureDescriptor> it = availableFeatures.iterator();

        while(it.hasNext()) {
            SProjectFeatureDescriptor desc = it.next();
            // TODO: "Connection" probably should not be hardcoded. Also this connection is different in the hashi implemention
            if (desc.getParameters().get(OAuthConstants.OAUTH_TYPE_PARAM) == "Connection") {
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<String> getParametersAvailableOnAgent(SBuild build) {
        // If buildType is null or build has finished or
        // feature is not enabled then return empty collection
        SBuildType buildType = build.getBuildType();
        if (buildType == null || build.isFinished() || !isFeatureEnabled(build)) {
            return Collections.emptyList();
        }

        HashSet<String> exposed = new HashSet<String>();
        Iterator<SProjectFeatureDescriptor> it = buildType.getProject().getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE).iterator();

        SProjectFeatureDescriptor connectionFeatures = null;

        while(it.hasNext()) {
            SProjectFeatureDescriptor desc = it.next();
            // TODO: "Connection" should probably not be hardedcoded. Also this connection is different in the hashi implemention
            if (desc.getParameters().get(OAuthConstants.OAUTH_TYPE_PARAM) == "Connection") {
                connectionFeatures = desc;
                break;
            }
        }

        if (connectionFeatures == null) {
            return Collections.emptyList();
        }

        Map<String, String> conjurFeatures = connectionFeatures.getParameters();

        Map<String, String> parameters = build.getBuildOwnParameters();






        return Collections.emptyList();
    }
}
