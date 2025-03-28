/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.container.handlers;

import java.nio.file.Paths;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;

import com.microsoft.azuretools.container.ConsoleLogger;
import com.microsoft.azuretools.container.Constant;
import com.microsoft.azuretools.container.ui.PublishWebAppOnLinuxDialog;
import com.microsoft.azuretools.container.utils.WarUtil;
import com.microsoft.azuretools.core.actions.MavenExecuteAction;
import com.microsoft.azuretools.core.handlers.SignInCommandHandler;
import com.microsoft.azuretools.core.utils.AzureAbstractHandler;
import com.microsoft.azuretools.core.utils.MavenUtils;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.tooling.msservices.components.DefaultLoader;

public class PublishHandler extends AzureAbstractHandler {

    private static final String MAVEN_GOALS = "package";
    private IProject project;
    private String destinationPath;
    private String basePath;

    @Override
    public Object onExecute(ExecutionEvent event) throws ExecutionException {
        project = PluginUtil.getSelectedProject();
        if (project == null) {
            return null;
        }

        SignInCommandHandler.requireSignedIn(PluginUtil.getParentShell(), () -> {
            basePath = project.getLocation().toString();
            try {
                if (MavenUtils.isMavenProject(project)) {
                    destinationPath = MavenUtils.getTargetPath(project);
                    ConsoleLogger.info(String.format(Constant.MESSAGE_PACKAGING_PROJECT, destinationPath));
                    MavenExecuteAction action = new MavenExecuteAction(MAVEN_GOALS);
                    IContainer container;
                    container = MavenUtils.getPomFile(project).getParent();
                    action.launch(container, () -> {
                        buildAndRun(event);
                    });
                } else {
                    destinationPath = Paths.get(basePath, Constant.DOCKERFILE_FOLDER, project.getName() + ".war").normalize().toString();
                    ConsoleLogger.info(String.format(Constant.MESSAGE_PACKAGING_PROJECT, destinationPath));
                    WarUtil.export(project, destinationPath);
                    buildAndRun(event);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendTelemetryOnException(event, e);
            }
        });
        return null;
    }

    private void buildAndRun(ExecutionEvent event) {
        DefaultLoader.getIdeHelper().invokeAndWait(() -> {
            PublishWebAppOnLinuxDialog dialog = new PublishWebAppOnLinuxDialog(PluginUtil.getParentShell(), basePath, destinationPath);
            dialog.open();
        });
    }
}
