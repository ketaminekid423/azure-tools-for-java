/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.ui;

import com.azure.core.management.AzureEnvironment;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ui.UIUtil;
import com.microsoft.azure.toolkit.ide.common.store.AzureConfigInitializer;
import com.microsoft.azure.toolkit.intellij.connector.Password;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.AzureConfiguration;
import com.microsoft.azure.toolkit.lib.auth.AzureCloud;
import com.microsoft.azure.toolkit.lib.auth.util.AzureEnvironmentUtils;
import com.microsoft.azure.toolkit.lib.legacy.function.FunctionCoreToolsCombobox;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.authmanage.CommonSettings;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.intellij.AzurePlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Objects;
import java.util.Optional;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.ACCOUNT;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.SIGNOUT;

@Slf4j
public class AzurePanel implements AzureAbstractConfigurablePanel {
    private static final String DISPLAY_NAME = "Azure";
    private JPanel contentPane;
    private JCheckBox allowTelemetryCheckBox;
    private JTextPane allowTelemetryComment;
    private JComboBox<AzureEnvironment> azureEnvironmentComboBox;
    private JComboBox<Password.SaveType> savePasswordComboBox;
    private FunctionCoreToolsCombobox funcCoreToolsPath;
    private JLabel azureEnvDesc;

    private AzureConfiguration originalConfig;
    private final Project project;

    public AzurePanel(Project project) {
        this.project = project;
    }

    @Override
    public void init() {
        if (AzurePlugin.IS_ANDROID_STUDIO) {
            return;
        }
        Messages.configureMessagePaneUi(allowTelemetryComment, message("settings.root.telemetry.notice"));
        allowTelemetryComment.setForeground(UIUtil.getContextHelpForeground());
        final ComboBoxModel<AzureEnvironment> envModel = new DefaultComboBoxModel<>(Azure.az(AzureCloud.class).list().toArray(new AzureEnvironment[0]));
        azureEnvironmentComboBox.setModel(envModel);
        azureEnvironmentComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nonnull JList list, AzureEnvironment value, int index, boolean selected, boolean hasFocus) {
                setText(azureEnvironmentDisplayString(value));
            }
        });
        final ComboBoxModel<Password.SaveType> saveTypeModel = new DefaultComboBoxModel<>(Password.SaveType.values());
        savePasswordComboBox.setModel(saveTypeModel);
        savePasswordComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nonnull JList<? extends Password.SaveType> list, Password.SaveType value, int index, boolean selected, boolean hasFocus) {
                setText(value.title());
            }
        });
        azureEnvDesc.setForeground(UIUtil.getContextHelpForeground());
        azureEnvDesc.setMaximumSize(new Dimension());
        azureEnvironmentComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                displayDescriptionForAzureEnv();
            }
        });

        displayDescriptionForAzureEnv();

        final AzureConfiguration config = Azure.az().config();
        if (Objects.isNull(config.getDatabasePasswordSaveType())) {
            config.setDatabasePasswordSaveType(Password.SaveType.UNTIL_RESTART.name());
        }
        setData(config);
    }

    public void setData(AzureConfiguration config) {
        this.originalConfig = config;
        final AzureEnvironment oldEnv = ObjectUtils.firstNonNull(AzureEnvironmentUtils.stringToAzureEnvironment(config.getCloud()), AzureEnvironment.AZURE);
        final String oldPasswordSaveType = config.getDatabasePasswordSaveType();
        final Boolean oldTelemetryEnabled = config.getTelemetryEnabled();
        final String oldFuncCoreToolsPath = config.getFunctionCoreToolsPath();
        azureEnvironmentComboBox.setSelectedItem(oldEnv);
        savePasswordComboBox.setSelectedItem(Optional.ofNullable(oldPasswordSaveType).map(Password.SaveType::valueOf).orElse(Password.SaveType.UNTIL_RESTART));
        if (StringUtils.isNotBlank(oldFuncCoreToolsPath)) {
            funcCoreToolsPath.setValue(oldFuncCoreToolsPath);
        }
        allowTelemetryCheckBox.setSelected(oldTelemetryEnabled);
    }

    public AzureConfiguration getData() {
        final AzureConfiguration data = new AzureConfiguration();
        data.setCloud(AzureEnvironmentUtils.azureEnvironmentToString((AzureEnvironment) azureEnvironmentComboBox.getSelectedItem()));
        data.setDatabasePasswordSaveType(Optional.ofNullable(savePasswordComboBox.getSelectedItem())
            .map(i -> ((Password.SaveType) i).name())
            .orElse(Password.SaveType.UNTIL_RESTART.name()));
        data.setTelemetryEnabled(allowTelemetryCheckBox.isSelected());
        data.setFunctionCoreToolsPath(funcCoreToolsPath.getItem());
        return data;
    }

    private void displayDescriptionForAzureEnv() {
        if (AuthMethodManager.getInstance().isSignedIn()) {
            final String azureEnv = AuthMethodManager.getInstance().getAuthMethodDetails().getAzureEnv();
            final AzureEnvironment currentEnv =
                AzureEnvironmentUtils.stringToAzureEnvironment(azureEnv);
            final String currentEnvStr = azureEnvironmentToString(currentEnv);
            if (Objects.equals(currentEnv, azureEnvironmentComboBox.getSelectedItem())) {
                setTextToLabel(azureEnvDesc, "You are currently signed in with environment: " + currentEnvStr);
                azureEnvDesc.setIcon(AllIcons.General.Information);
            } else {
                setTextToLabel(azureEnvDesc,
                    String.format("You are currently signed in to environment: %s, your change will sign out your account.", currentEnvStr));
                azureEnvDesc.setIcon(AllIcons.General.Warning);
            }
        } else {
            setTextToLabel(azureEnvDesc, "You are currently not signed in, the environment will be applied when you sign in next time.");
            azureEnvDesc.setIcon(AllIcons.General.Warning);
        }
    }

    private static void setTextToLabel(@Nonnull JLabel label, @Nonnull String text) {
        label.setText("<html>" + text + "</html>");
    }

    private static String azureEnvironmentDisplayString(@Nonnull AzureEnvironment env) {
        return String.format("%s - %s", azureEnvironmentToString(env), env.getActiveDirectoryEndpoint());
    }

    private static String azureEnvironmentToString(@Nonnull AzureEnvironment env) {
        final String name = AzureEnvironmentUtils.getCloudNameForAzureCli(env);
        return StringUtils.removeEnd(name, "Cloud");
    }

    @Override
    public JComponent getPanel() {
        return contentPane;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean doOKAction() {
        final AzureConfiguration newConfig = getData();
        // set partial config to global config
        this.originalConfig.setCloud(newConfig.getCloud());
        this.originalConfig.setTelemetryEnabled(newConfig.getTelemetryEnabled());
        this.originalConfig.setDatabasePasswordSaveType(newConfig.getDatabasePasswordSaveType());
        this.originalConfig.setFunctionCoreToolsPath(newConfig.getFunctionCoreToolsPath());
        final String userAgent = String.format(AzurePlugin.USER_AGENT, AzurePlugin.PLUGIN_VERSION,
            this.originalConfig.getTelemetryEnabled() ? this.originalConfig.getMachineId() : StringUtils.EMPTY);
        this.originalConfig.setUserAgent(userAgent);
        CommonSettings.setUserAgent(newConfig.getUserAgent());
        // apply changes
        // we need to get rid of AuthMethodManager, using az.azure_account
        if (AuthMethodManager.getInstance().isSignedIn()) {
            final AuthMethodManager authMethodManager = AuthMethodManager.getInstance();
            final String azureEnv = authMethodManager.getAuthMethodDetails().getAzureEnv();
            final AzureEnvironment currentEnv = AzureEnvironmentUtils.stringToAzureEnvironment(azureEnv);
            if (!Objects.equals(currentEnv, azureEnvironmentComboBox.getSelectedItem())) {
                EventUtil.executeWithLog(ACCOUNT, SIGNOUT, (operation) -> {
                    authMethodManager.signOut();
                });
            }
        }

        if (StringUtils.isNotBlank(newConfig.getCloud())) {
            Azure.az(AzureCloud.class).setByName(newConfig.getCloud());
        }
        AzureConfigInitializer.saveAzConfig();
        return true;
    }

    @Override
    public String getSelectedValue() {
        return null;
    }

    @Override
    public ValidationInfo doValidate() {
        return null;
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public boolean isModified() {
        if (originalConfig == null) {
            return false;
        }
        final AzureConfiguration newConfig = getData();
        final AzureEnvironment newEnv = AzureEnvironmentUtils.stringToAzureEnvironment(newConfig.getCloud());
        final AzureEnvironment oldEnv = AzureEnvironmentUtils.stringToAzureEnvironment(originalConfig.getCloud());
        return !Objects.equals(newEnv, oldEnv) ||
            !StringUtils.equalsIgnoreCase(newConfig.getDatabasePasswordSaveType(), originalConfig.getDatabasePasswordSaveType()) ||
            !StringUtils.equalsIgnoreCase(newConfig.getFunctionCoreToolsPath(), originalConfig.getFunctionCoreToolsPath()) ||
            !Objects.equals(newConfig.getTelemetryEnabled(), newConfig.getTelemetryEnabled());
    }

    @Override
    public void reset() {
        setData(originalConfig);
    }

    private void createUIComponents() {
        this.funcCoreToolsPath = new FunctionCoreToolsCombobox(project, false);
    }
}
