/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.appservice.serviceplan;

import com.intellij.ui.components.JBLabel;
import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.intellij.common.SwingUtils;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.ide.appservice.webapp.model.DraftServicePlan;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo.AzureValidationInfoBuilder;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.intellij.util.ValidationUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Collections;
import java.util.List;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

public class ServicePlanCreationDialog extends AzureDialog<DraftServicePlan>
        implements AzureForm<DraftServicePlan> {
    private Subscription subscription;
    private OperatingSystem os;
    private Region region;
    private JPanel contentPanel;
    private JBLabel labelDescription;
    private AzureTextInput textName;
    private PricingTierComboBox comboBoxPricingTier;

    public ServicePlanCreationDialog(final Subscription subscription,
                                     OperatingSystem os,
                                     Region region,
                                     final List<PricingTier> pricingTierList, final PricingTier defaultPricingTier) {
        super();
        this.subscription = subscription;
        this.os = os;
        this.region = region;
        this.init();
        this.textName.setValidator(this::validateName);
        this.comboBoxPricingTier.setPricingTierList(pricingTierList);
        this.comboBoxPricingTier.setDefaultPricingTier(defaultPricingTier);
        SwingUtils.setTextAndEnableAutoWrap(this.labelDescription, message("appService.servicePlan.description"));
        this.pack();
    }

    private AzureValidationInfo validateName() {
        try {
            ValidationUtils.validateAppServicePlanName(this.textName.getValue());
        } catch (final IllegalArgumentException e) {
            final AzureValidationInfoBuilder builder = AzureValidationInfo.builder();
            return builder.input(this.textName).type(AzureValidationInfo.Type.ERROR).message(e.getMessage()).build();
        }
        return AzureValidationInfo.success(this);
    }

    @Override
    public AzureForm<DraftServicePlan> getForm() {
        return this;
    }

    @Override
    protected String getDialogTitle() {
        return message("appService.servicePlan.create.title");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return this.contentPanel;
    }

    @Override
    public DraftServicePlan getValue() {
        return new DraftServicePlan(this.subscription,
               this.textName.getValue(),
               this.region,
               this.os,
               this.comboBoxPricingTier.getValue());
    }

    @Override
    public void setValue(final DraftServicePlan data) {
        this.subscription = data.getSubscription();
        this.os = data.getOperatingSystem();
        this.region = Region.fromName(data.getRegion());
        this.textName.setValue(data.getName());
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Collections.singletonList(this.textName);
    }

}
