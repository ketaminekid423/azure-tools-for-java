package com.microsoft.azure.toolkit.intellij.common.runtarget;

import com.intellij.openapi.options.BoundConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogPanel;

import javax.annotation.Nonnull;
import java.awt.*;

public class AzureCloudTargetConfigurable extends BoundConfigurable implements Configurable {

    public AzureCloudTargetConfigurable(AzureCloudTargetEnvironmentConfiguration config, Project project) {
        super(config.getDisplayName(), config.getTypeId());
    }

    @Nonnull
    @Override
    public DialogPanel createPanel() {
        return new DialogPanel();
    }
}
