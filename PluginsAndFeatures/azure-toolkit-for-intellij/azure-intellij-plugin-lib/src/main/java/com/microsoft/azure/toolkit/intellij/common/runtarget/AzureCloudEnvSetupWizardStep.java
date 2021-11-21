package com.microsoft.azure.toolkit.intellij.common.runtarget;

import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AzureCloudEnvSetupWizardStep extends AbstractWizardStepEx {
    public AzureCloudEnvSetupWizardStep(@Nullable @NlsContexts.DialogTitle String title) {
        super(title);
    }

    @Override
    public @NotNull Object getStepId() {
        return AzureCloudEnvSetupWizardStep.class;
    }

    @Override
    public @Nullable Object getNextStepId() {
        return null;
    }

    @Override
    public @Nullable Object getPreviousStepId() {
        return null;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {

    }

    @Override
    public JComponent getComponent() {
        return new JButton("Azure Test");
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }
}
