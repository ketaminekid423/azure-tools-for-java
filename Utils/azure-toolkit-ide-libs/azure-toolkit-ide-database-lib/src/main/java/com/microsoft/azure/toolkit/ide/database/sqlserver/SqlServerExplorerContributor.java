/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.database.sqlserver;

import com.microsoft.azure.toolkit.ide.common.IExplorerContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.lib.sqlserver.AzureSqlServer;
import com.microsoft.azure.toolkit.lib.sqlserver.MicrosoftSqlServer;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.microsoft.azure.toolkit.lib.Azure.az;

public class SqlServerExplorerContributor implements IExplorerContributor {
    private static final String NAME = "SQL Server";
    private static final String ICON = "/icons/Microsoft.SQL/default.svg";

    @Override
    public Node<?> getModuleNode() {
        final AzureSqlServer service = az(AzureSqlServer.class);
        final Function<AzureSqlServer, List<MicrosoftSqlServer>> servers = s -> s.list().stream()
            .flatMap(m -> m.servers().list().stream()).collect(Collectors.toList());
        return new Node<>(service).view(new AzureServiceLabelView<>(service, NAME, ICON))
            .actions(SqlServerActionsContributor.SERVICE_ACTIONS)
            .addChildren(servers, (server, serviceNode) -> new Node<>(server)
                .view(new AzureResourceLabelView<>(server))
                .actions(SqlServerActionsContributor.SERVER_ACTIONS));
    }
}
