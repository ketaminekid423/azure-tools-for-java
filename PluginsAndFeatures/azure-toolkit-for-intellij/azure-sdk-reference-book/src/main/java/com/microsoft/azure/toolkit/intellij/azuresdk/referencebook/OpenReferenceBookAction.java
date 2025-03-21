/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.azuresdk.referencebook;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class OpenReferenceBookAction extends AnAction implements DumbAware {
    public static final String ID = "AzureToolkit.OpenSdkReferenceBook";

    @Override
    public void actionPerformed(@Nonnull final AnActionEvent event) {
        final Module module = event.getData(LangDataKeys.MODULE);
        AzureTaskManager.getInstance().runLater(() -> openSdkReferenceBook(event.getProject()));
    }

    @AzureOperation(name = "sdk.open_reference_book", type = AzureOperation.Type.ACTION)
    private void openSdkReferenceBook(final @Nullable Project project) {
        final AzureSdkReferenceBookDialog dialog = new AzureSdkReferenceBookDialog(project);
        dialog.show();
    }
}
