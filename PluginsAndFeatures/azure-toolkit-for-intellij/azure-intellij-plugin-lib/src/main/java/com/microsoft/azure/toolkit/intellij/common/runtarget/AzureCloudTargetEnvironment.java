package com.microsoft.azure.toolkit.intellij.common.runtarget;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Platform;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.target.TargetEnvironment;
import com.intellij.execution.target.TargetEnvironmentRequest;
import com.intellij.execution.target.TargetPlatform;
import com.intellij.execution.target.TargetedCommandLine;
import com.intellij.openapi.progress.ProgressIndicator;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class AzureCloudTargetEnvironment extends TargetEnvironment {
    public AzureCloudTargetEnvironment(@Nonnull TargetEnvironmentRequest request) {
        super(request);
    }

    @Nonnull
    @Override
    public TargetPlatform getTargetPlatform() {
        return new TargetPlatform(Platform.UNIX);
    }

    @Nonnull
    @Override
    public Process createProcess(@Nonnull TargetedCommandLine commandLine, @Nonnull ProgressIndicator progressIndicator) throws ExecutionException {
        final var line = new GeneralCommandLine(Arrays.asList(
            "C:\\Users\\wangmi\\.jdks\\adopt-openjdk-11.0.11\\bin\\java.exe",
            "-Dfile.encoding=UTF-8",
            "-classpath",
            "/mnt/c/Users/wangmi/workspace/demo-apps/azure-examples/example-connector-env-variables/target/classes:/mnt/c/Users/wangmi/.m2/repository/edu/wpi/first/wpilibj/wpilibj-java/2019.3.1/wpilibj-java-2019.3.1.jar",
            "com.microsoft.azure.toolkit.example.connector.java.env.ConnectorEnvApplication"));
        line.getEnvironment().putAll(commandLine.getEnvironmentVariables());
        return line.createProcess();
    }

    @Override
    public void shutdown() {

    }
}
