/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud.component;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.springcloud.AzureSpringCloud;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudClusterModule;

import org.eclipse.swt.widgets.Composite;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SpringCloudClusterComboBox extends AzureComboBox<SpringCloudCluster> {

    private Subscription subscription;

    public SpringCloudClusterComboBox(Composite parent) {
        super(parent, false);
    }

    @Override
    protected String getItemText(final Object item) {
        if (Objects.isNull(item)) {
            return AzureComboBox.EMPTY_ITEM;
        }
        return ((SpringCloudCluster) item).name();
    }

    public void setSubscription(Subscription subscription) {
        if (Objects.equals(subscription, this.subscription)) {
            return;
        }
        this.subscription = subscription;
        if (subscription == null) {
            this.clear();
            return;
        }
        this.refreshItems();
    }

    @NotNull
    @Override
    @AzureOperation(
        name = "springcloud|cluster.list.subscription",
        params = {"this.subscription.getId()"},
        type = AzureOperation.Type.SERVICE
    )
    protected List<? extends SpringCloudCluster> loadItems() throws Exception {
        if (Objects.nonNull(this.subscription)) {
            final String sid = this.subscription.getId();
            final SpringCloudClusterModule az = Azure.az(AzureSpringCloud.class).clusters(sid);
            return az.list();
        }
        return Collections.emptyList();
    }
}
