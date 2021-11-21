package com.microsoft.azure.toolkit.intellij.common.runtarget;

import com.intellij.execution.target.TargetEnvironmentConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.components.PersistentStateComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AzureCloudTargetEnvironmentConfiguration extends TargetEnvironmentConfiguration
    implements PersistentStateComponent<AzureCloudTargetEnvironmentConfiguration.MyState> {
    public AzureCloudTargetEnvironmentConfiguration() {
        super(AzureCloudTargetType.TYPE_ID);
    }

    @Nonnull
    @Override
    public String getProjectRootOnTarget() {
        return null;
    }

    @Override
    public void setProjectRootOnTarget(@Nonnull String s) {

    }

    @Nullable
    @Override
    public AzureCloudTargetEnvironmentConfiguration.MyState getState() {
        return null;
    }

    @Override
    public void loadState(@Nonnull AzureCloudTargetEnvironmentConfiguration.MyState o) {
    }

    class MyState extends BaseState {

    }
}
