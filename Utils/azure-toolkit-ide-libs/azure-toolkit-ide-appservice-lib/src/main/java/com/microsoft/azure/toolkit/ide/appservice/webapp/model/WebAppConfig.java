/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp.model;

import com.microsoft.azure.toolkit.ide.appservice.model.AppServiceConfig;
import com.microsoft.azure.toolkit.ide.common.model.DraftResourceGroup;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.account.IAzureAccount;
import com.microsoft.azure.toolkit.lib.appservice.config.RuntimeConfig;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class WebAppConfig extends AppServiceConfig {
    public static final Runtime DEFAULT_RUNTIME = Runtime.LINUX_JAVA8_TOMCAT9;
    public static final PricingTier DEFAULT_PRICING_TIER = PricingTier.BASIC_B2;
    @Builder.Default
    protected Runtime runtime = DEFAULT_RUNTIME;

    public static WebAppConfig getWebAppDefaultConfig() {
        return getWebAppDefaultConfig(StringUtils.EMPTY);
    }

    public static WebAppConfig getWebAppDefaultConfig(final String name) {
        final String appName = StringUtils.isEmpty(name) ? String.format("app-%s", DATE_FORMAT.format(new Date())) :
                String.format("app-%s-%s", name, DATE_FORMAT.format(new Date()));
        final Subscription subscription = Azure.az(IAzureAccount.class).account().getSelectedSubscriptions().stream().findFirst().orElse(null);
        final DraftResourceGroup group = new DraftResourceGroup(subscription, StringUtils.substring(String.format("rg-%s", appName), 0, RG_NAME_MAX_LENGTH));
        group.setSubscription(subscription);
        final Region region = AppServiceConfig.getDefaultRegion();
        final String planName = StringUtils.substring(String.format("sp-%s", appName), 0, SP_NAME_MAX_LENGTH);
        final DraftServicePlan plan = new DraftServicePlan(subscription, planName, region, WebAppConfig.DEFAULT_RUNTIME.getOperatingSystem(),
                WebAppConfig.DEFAULT_PRICING_TIER);
        return WebAppConfig.builder()
                .subscription(subscription)
                .resourceGroup(group)
                .name(appName)
                .servicePlan(plan)
                .runtime(WebAppConfig.DEFAULT_RUNTIME)
                .pricingTier(WebAppConfig.DEFAULT_PRICING_TIER)
                .region(region).build();
    }

    public static com.microsoft.azure.toolkit.lib.appservice.config.AppServiceConfig convertToTaskConfig(WebAppConfig config) {
        final com.microsoft.azure.toolkit.lib.appservice.config.AppServiceConfig result =
                new com.microsoft.azure.toolkit.lib.appservice.config.AppServiceConfig();
        result.appName(config.getName());
        result.resourceGroup(config.getResourceGroupName());
        result.subscriptionId(config.getSubscriptionId());
        result.pricingTier(Optional.ofNullable(config.getServicePlan()).map(AppServicePlanEntity::getPricingTier).orElseGet(config::getPricingTier));
        result.region(config.getRegion());
        result.servicePlanName(Optional.ofNullable(config.getServicePlan()).map(AppServicePlanEntity::getName).orElse(null));
        result.servicePlanResourceGroup(Optional.ofNullable(config.getServicePlan())
                .map(AppServicePlanEntity::getResourceGroup).orElseGet(config::getResourceGroupName));
        Optional.ofNullable(config.getRuntime()).ifPresent(runtime -> result.runtime(
                new RuntimeConfig().os(runtime.getOperatingSystem()).javaVersion(runtime.getJavaVersion()).webContainer(runtime.getWebContainer())));
        return result;
    }

    public static WebAppConfig fromRemote(WebApp webApp) {
        return WebAppConfig.builder()
                .name(webApp.name())
                .resourceId(webApp.id())
                .servicePlan(AppServicePlanEntity.builder().id(webApp.entity().getAppServicePlanId()).build())
                .subscription(Subscription.builder().id(webApp.subscriptionId()).build())
                .resourceGroup(ResourceGroup.builder().name(webApp.resourceGroup()).build())
                .runtime(webApp.getRuntime())
                .region(webApp.entity().getRegion())
                .build();
    }
}
