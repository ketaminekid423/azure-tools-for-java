/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.springcloud.component;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.springcloud.creation.SpringCloudAppCreationDialog;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudAppDraft;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpringCloudAppComboBox extends AzureComboBox<SpringCloudApp> {
    private SpringCloudCluster cluster;
    private final Map<String, SpringCloudApp> localItems = new HashMap<>();

    @Override
    protected String getItemText(final Object item) {
        if (Objects.isNull(item)) {
            return EMPTY_ITEM;
        }
        final SpringCloudApp app = (SpringCloudApp) item;
        if (!app.exists()) {
            return "(New) " + app.name();
        }
        return app.name();
    }

    public void setCluster(SpringCloudCluster cluster) {
        if (Objects.equals(cluster, this.cluster)) {
            return;
        }
        this.cluster = cluster;
        if (cluster == null) {
            this.clear();
            return;
        }
        this.refreshItems();
    }

    @NotNull
    @Override
    @AzureOperation(
            name = "springcloud.list_apps.cluster",
            params = {"this.cluster.name()"},
            type = AzureOperation.Type.SERVICE
    )
    protected List<? extends SpringCloudApp> loadItems() throws Exception {
        final List<SpringCloudApp> apps = new ArrayList<>();
        if (Objects.nonNull(this.cluster)) {
            if (!this.localItems.isEmpty()) {
                apps.add(this.localItems.get(this.cluster.name()));
            }
            apps.addAll(cluster.apps().list());
        }
        return apps;
    }

    @Nullable
    @Override
    protected ExtendableTextComponent.Extension getExtension() {
        return ExtendableTextComponent.Extension.create(
            AllIcons.General.Add, "Create Azure Spring Cloud app", this::showAppCreationPopup);
    }

    private void showAppCreationPopup() {
        final SpringCloudAppCreationDialog dialog = new SpringCloudAppCreationDialog(this.cluster);
        dialog.setOkActionListener((config) -> {
            final SpringCloudAppDraft app = cluster.apps().create(config.getAppName(), cluster.getResourceGroupName());
            app.setConfig(config);
            this.addLocalItem(app);
            dialog.close();
            this.setValue(app);
        });
        dialog.show();
    }

    public void addLocalItem(SpringCloudApp app) {
        final SpringCloudApp cached = this.localItems.get(app.getParent().name());
        if (Objects.isNull(cached) || !Objects.equals(app.name(), cached.name())) {
            this.localItems.put(app.getParent().name(), app);
            final List<SpringCloudApp> items = this.getItems();
            items.add(0, app);
            this.setItems(items);
        }
    }
}
