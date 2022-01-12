/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.function.action;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.messager.IntellijAzureMessager;
import com.microsoft.azure.toolkit.intellij.legacy.function.FunctionAppCreationDialog;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.FunctionApp;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessage;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.telemetry.AzureTelemetry;
import com.microsoft.azure.toolkit.ide.appservice.function.FunctionAppConfig;
import com.microsoft.azure.toolkit.lib.legacy.function.FunctionAppService;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import com.microsoft.intellij.util.AzureLoginHelper;
import rx.Single;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;
import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class CreateFunctionAppAction {

    private static final String NOTIFICATION_GROUP_ID = "Azure Plugin";
    private final FunctionAppService functionAppService;
    private final Project project;

    public CreateFunctionAppAction(final Project project) {
        super();
        this.project = project;
        this.functionAppService = FunctionAppService.getInstance();
    }

    @AzureOperation(name = "function.create_app", type = AzureOperation.Type.ACTION)
    public void execute() {
        AzureLoginHelper.requireSignedIn(project, () -> this.openDialog(project, null));
    }

    @AzureOperation(name = "function.open_creation_dialog", type = AzureOperation.Type.ACTION)
    private void openDialog(final Project project, @Nullable final FunctionAppConfig data) {
        final FunctionAppCreationDialog dialog = new FunctionAppCreationDialog(project);
        if (Objects.nonNull(data)) {
            dialog.setData(data);
        }
        dialog.setOkActionListener((config) -> {
            dialog.close();
            this.createFunctionApp(config)
                .subscribe(functionApp -> {
                }, (error) -> {
                    final String title = String.format("Reopen dialog \"%s\"", dialog.getTitle());
                    final Consumer<Object> act = t -> AzureTaskManager.getInstance().runLater("open dialog", () -> this.openDialog(project, config));
                    final Action<?> action = new Action<>(act, new ActionView.Builder(title));
                    AzureMessager.getMessager().error(error, null, action);
                });
        });
        dialog.show();
    }

    @AzureOperation(name = "function.create_app.app", params = {"config.getName()"}, type = AzureOperation.Type.ACTION)
    private Single<FunctionApp> createFunctionApp(final FunctionAppConfig config) {
        final AzureString title = title("function.create_app.app", config.getName());
        final IntellijAzureMessager actionMessenger = new IntellijAzureMessager() {
            @Override
            public boolean show(IAzureMessage raw) {
                if (raw.getType() != IAzureMessage.Type.INFO) {
                    return super.show(raw);
                }
                return false;
            }
        };
        final AzureTask<FunctionApp> task = new AzureTask<>(null, title, false, () -> {
            final Operation operation = TelemetryManager.createOperation(TelemetryConstants.FUNCTION, TelemetryConstants.CREATE_FUNCTION_APP);
            operation.trackProperties(config.getTelemetryProperties());
            try {
                AzureMessager.getContext().setMessager(actionMessenger);
                final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                indicator.setIndeterminate(true);
                return functionAppService.createFunctionApp(config);
            } finally {
                operation.trackProperties(AzureTelemetry.getContext().getActionParent().getProperties());
                operation.complete();
            }
        });
        return AzureTaskManager.getInstance().runInModalAsObservable(task).toSingle().doOnSuccess(app -> {
            AzureMessager.getMessager().success(message("function.create.success.message", app.name()), message("function.create.success.title"));
        });
    }
}
