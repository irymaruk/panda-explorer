package com.rymaruk.pandaexplorer.task;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.rymaruk.pandaexplorer.misc.CommonUtils;
import com.rymaruk.pandaexplorer.misc.CosmosDbUtils;
import com.rymaruk.pandaexplorer.view.ConnectionUI;
import com.rymaruk.pandaexplorer.view.ToolBarUI;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetDbCollectionTask extends Task<Map<String, List<String>>> {

    private final String env;
    private TreeView<String> treeView;

    public GetDbCollectionTask(String env, TreeView<String> treeView) {
        this.env = env;
        this.treeView = treeView;
    }

    @Override
    protected Map<String, List<String>> call() {
        updateMessage("Establishing connection");
        CosmosClient client = CosmosDbUtils.getInstance().getClient(env);
        updateMessage("Read databases");
        List<String> dbNames = client.readAllDatabases().stream().map(CosmosDatabaseProperties::getId).collect(Collectors.toList());
        Map<String, List<String>> data = new HashMap<>();
        for (int i = 0; i < dbNames.size(); i++) {
            String dbName = dbNames.get(i);
            updateProgress(i, dbNames.size());
            updateMessage("Read Collections for " + dbName);
            List<String> collectionNames = client.getDatabase(dbName)
                    .readAllContainers().stream().map(CosmosContainerProperties::getId)
                    .collect(Collectors.toList());
            data.put(dbName, collectionNames);
        }
        return data;
    }

    @Override
    protected void failed() {
        Platform.runLater(() -> ToolBarUI.getEnvComboBox().setDisable(false));
        CommonUtils.setStatusText("Exception happened, see logs for details: \n" + this.getException().toString());
    }

    @Override
    protected void succeeded() {
        ConnectionUI.initTree(treeView, this.getValue());
        Platform.runLater(() -> ToolBarUI.getExecuteBtn().setDisable(false));
        Platform.runLater(() -> ToolBarUI.getEnvComboBox().setDisable(false));
    }
}
