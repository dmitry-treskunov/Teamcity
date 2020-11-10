package com.cyberark;

import jetbrains.buildServer.agent.*;

import java.util.Map;

public class ConjurBuildFeature extends AgentLifeCycleAdapter {

    @Override
    public void buildStarted(AgentRunningBuild runningBuild) {
        BuildParametersMap parameters = runningBuild.getSharedBuildParameters();
        BuildProgressLogger logger  = runningBuild.getBuildLogger();

        logger.message("This is a logged message");

        Map<String, String> tmp = parameters.getAllParameters();

        for(Map.Entry<String, String> kv : tmp.entrySet()) {
            String message = String.format("Connection to Cyberark Conjur server at '%s' with login '%s'",
                    kv.getKey(), kv.getValue());
            logger.message(message);
        }
    }

    @Override
    public void afterAgentConfigurationLoaded(BuildAgent agent) {
        agent.getConfiguration().addConfigurationParameter("teamcity.conjur.supported", "true");
    }

    @Override
    public void beforeBuildFinish(AgentRunningBuild build, BuildFinishedStatus buildStatus) {
        return;
    }

    @Override
    public void buildFinished(AgentRunningBuild build, BuildFinishedStatus buildStatus) {
        return;
    }
}
