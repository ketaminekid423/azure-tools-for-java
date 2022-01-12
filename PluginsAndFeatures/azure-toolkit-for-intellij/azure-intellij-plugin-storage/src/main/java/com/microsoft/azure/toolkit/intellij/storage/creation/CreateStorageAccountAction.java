/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.storage.creation;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.model.Draft;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.telemetry.AzureTelemetry;
import com.microsoft.azure.toolkit.lib.resource.AzureGroup;
import com.microsoft.azure.toolkit.lib.storage.StorageAccountDraft;
import com.microsoft.azure.toolkit.lib.storage.model.StorageAccountConfig;
import com.microsoft.azure.toolkit.lib.storage.AzureStorageAccount;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;

public class CreateStorageAccountAction {
    public static void createStorageAccount(Project project) {
        Azure.az(AzureAccount.class).account();
        AzureTaskManager.getInstance().runLater(() -> {
            final StorageAccountCreationDialog dialog = new StorageAccountCreationDialog(project);
            dialog.setOkActionListener((config) -> {
                dialog.close();
                create(config);
            });
            dialog.show();
        });
    }

    @AzureOperation(name = "storage.create_account.account", params = {"config.getName()"}, type = AzureOperation.Type.ACTION)
    public static void create(final StorageAccountConfig config) {
        final AzureString title = AzureOperationBundle.title("storage.create_account.account", config.getName());
        AzureTaskManager.getInstance().runInBackground(title, () -> createStorageAccount(config));
    }

    public static StorageAccount createStorageAccount(StorageAccountConfig config) {
        final String subscriptionId = config.getSubscription().getId();
        AzureTelemetry.getContext().getActionParent().setProperty("subscriptionId", subscriptionId);
        if (config.getResourceGroup() instanceof Draft) { // create resource group if necessary.
            final ResourceGroup newResourceGroup = Azure.az(AzureGroup.class)
                    .subscription(subscriptionId).create(config.getResourceGroup().getName(), config.getRegion().getName());
            config.setResourceGroup(newResourceGroup);
        }
        final AzureStorageAccount az = Azure.az(AzureStorageAccount.class);
        final StorageAccountDraft draft = az.accounts(config.getSubscriptionId()).create(config.getName(), config.getResourceGroupName());
        draft.setConfig(config);
        return draft.commit();
    }
}
