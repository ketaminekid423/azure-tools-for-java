package com.microsoft.azure.toolkit.intellij.common.runtarget;

import com.intellij.execution.Platform;
import com.intellij.execution.target.BaseTargetEnvironmentRequest;
import com.intellij.execution.target.HostPort;
import com.intellij.execution.target.TargetEnvironment;
import com.intellij.execution.target.TargetEnvironmentConfiguration;
import com.intellij.execution.target.TargetPlatform;
import com.intellij.execution.target.TargetProgressIndicator;
import com.intellij.execution.target.value.TargetValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AzureCloudEnvironmentRequest extends BaseTargetEnvironmentRequest {
    private final AzureCloudTargetEnvironmentConfiguration config;

    public AzureCloudEnvironmentRequest(AzureCloudTargetEnvironmentConfiguration config) {
        this.config = config;
    }

    @Nullable
    @Override
    public TargetEnvironmentConfiguration getConfiguration() {
        return config;
    }

    @Nonnull
    @Override
    public Volume getDefaultVolume() {
        throw new UnsupportedOperationException("createDownloadRoot is not implemented");
    }

    @Nonnull
    @Override
    public TargetPlatform getTargetPlatform() {
        return new TargetPlatform(Platform.UNIX);
    }

    @Nonnull
    @Override
    public TargetValue<HostPort> bindLocalPort(int i) {
        return TargetValue.fixed(new HostPort("localhost", i));
    }

    @Nonnull
    @Override
    public TargetValue<Integer> bindTargetPort(int i) {
        return TargetValue.fixed(i);
    }

    @Nonnull
    @Override
    public DownloadableVolume createDownloadRoot(@Nullable String s) {
        throw new UnsupportedOperationException("createDownloadRoot is not implemented");
    }

    @Nonnull
    @Override
    public Volume createUploadRoot(@Nullable String s, boolean b) {
        throw new UnsupportedOperationException("createDownloadRoot is not implemented");
    }

    @Nonnull
    @Override
    public TargetEnvironment prepareEnvironment(@Nonnull TargetProgressIndicator targetProgressIndicator) {
        return new AzureCloudTargetEnvironment(this);
    }
}
