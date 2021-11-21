package com.microsoft.azure.toolkit.intellij.common.runtarget;

import com.intellij.execution.target.LanguageRuntimeType;
import com.intellij.execution.target.TargetEnvironmentRequest;
import com.intellij.execution.target.TargetEnvironmentType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;

@Getter
public class AzureCloudTargetType extends TargetEnvironmentType<AzureCloudTargetEnvironmentConfiguration> {
    public static final String TYPE_ID = "azure";
    public static final String DISPLAY_NAME = "Azure";
    @Nls
    @Nonnull
    private final String displayName = DISPLAY_NAME;
    @Nonnull
    private final Icon icon = AllIcons.Providers.Azure;

    public AzureCloudTargetType() {
        super(TYPE_ID);
    }

    @Nonnull
    @Override
    public PersistentStateComponent<?> createSerializer(@Nonnull AzureCloudTargetEnvironmentConfiguration config) {
        return config;
    }

    @Nonnull
    @Override
    public AzureCloudTargetEnvironmentConfiguration createDefaultConfig() {
        return new AzureCloudTargetEnvironmentConfiguration();
    }

    @Override
    public boolean providesNewWizard(@Nonnull Project project, @Nullable LanguageRuntimeType<?> runtimeType) {
        return true;
    }

    @Nullable
    @Override
    public List<AbstractWizardStepEx> createStepsForNewWizard(@Nonnull Project project, @Nonnull AzureCloudTargetEnvironmentConfiguration config, @Nullable LanguageRuntimeType<?> runtimeType) {
        config.setDisplayName("Azure Cloud");
        config.addLanguageRuntime(runtimeType.createDefaultConfig());
        return Arrays.asList(new AzureCloudEnvSetupWizardStep("Azure Env"));
    }

    @Nonnull
    @Override
    public AzureCloudTargetEnvironmentConfiguration duplicateConfig(@Nonnull AzureCloudTargetEnvironmentConfiguration config) {
        return duplicateTargetConfiguration(this, config);
    }

    @Nonnull
    @Override
    public Configurable createConfigurable(@Nonnull Project project, @Nonnull AzureCloudTargetEnvironmentConfiguration config, @Nullable LanguageRuntimeType<?> languageRuntimeType, @Nullable Configurable configurable) {
        return new AzureCloudTargetConfigurable(config, project);
    }

    @Nonnull
    @Override
    public TargetEnvironmentRequest createEnvironmentRequest(@Nonnull Project project, @Nonnull AzureCloudTargetEnvironmentConfiguration config) {
        return new AzureCloudEnvironmentRequest(config);
    }
}
