package com.rymaruk.pandaexplorer.view;

import com.rymaruk.pandaexplorer.misc.CommonUtils;
import com.rymaruk.pandaexplorer.service.CosmosCollectionService;
import com.rymaruk.pandaexplorer.task.GetDbCollectionTask;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import org.eclipse.fx.ui.controls.tree.SortableTreeItem;
import org.eclipse.fx.ui.controls.tree.TreeItemComparator;
import org.eclipse.fx.ui.controls.tree.TreeItemPredicate;
import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.Map;

public class ConnectionUI {

    private final BorderPane connectionPane = new BorderPane();
    private final static TextField filterField = new TextField();
    private final static CodeArea sqlArea = ScriptEditUI.getArea();


    public Node get() {
        return connectionPane;
    }

    public ConnectionUI() {
        connectionPane.setTop(getCollectionSearchField());
        ToolBarUI.getEnvComboBox().valueProperty().addListener((observable, oldValue, newValue) -> {
            ToolBarUI.getEnvComboBox().setDisable(true);
            ToolBarUI.getExecuteBtn().setDisable(true);
            filterField.clear();
            connectionPane.setCenter(getCollectionsTree(newValue));
        });
        connectionPane.setPadding(new Insets(5));
    }

    private Node getCollectionSearchField() {
        filterField.setPromptText("collection name...");
        return filterField;
    }


    private Node getCollectionsTree(String env) {
        TreeView<String> treeView = new TreeView<>();
        Task<Map<String, List<String>>> task = new GetDbCollectionTask(env, treeView);
        CommonUtils.bindProgress(task);
        task.setOnSucceeded(ignored -> initTree(treeView, task.getValue()));
        CommonUtils.getExecutorService().submit(task);
        return treeView;
    }

    public static void initTree(TreeView<String> treeView, Map<String, List<String>> taskValue) {
        CosmosCollectionService.init(taskValue);
        printCountsToStatusBar(taskValue);
        SortableTreeItem<String> root = new SortableTreeItem<>("");
        for (String dbName : taskValue.keySet()) {
            SortableTreeItem<String> dbItem = new SortableTreeItem<>(dbName);
            root.getInternalChildren().add(dbItem);
            taskValue.get(dbName)
                    .forEach(collectionName -> dbItem.getInternalChildren().add(new SortableTreeItem<>(collectionName)));
        }
        root.setExpanded(true);
        treeView.setShowRoot(false);
        treeView.setRoot(root);

        // add collections name filter
        root.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            if (filterField.getText() == null || filterField.getText().isEmpty()) return null;
            return TreeItemPredicate.create(s -> s.toLowerCase().contains(filterField.getText().toLowerCase()));
        }, filterField.textProperty()));

        // add DB and Collection sorting
        root.setComparator(TreeItemComparator.create(String::compareTo));

        // expand when search
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                root.getInternalChildren().forEach(item -> item.setExpanded(true));
            } else {
                root.getInternalChildren().forEach(item -> item.setExpanded(false));
            }
        });

        // double click add select statement to editor
        treeView.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 2) {
                    TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
                    if (item.isLeaf()) {
                        String collName = item.getValue();
                        String text;
                        if (CosmosCollectionService.isCollectionUnique(collName)) {
                            text = String.format("\n\nSELECT * FROM %s as c", collName);
                        } else {
                            String dbName = item.getParent().getValue();
                            text = String.format("\n\nSELECT * FROM %s.%s as c", dbName, collName);
                        }
                        int textLength = sqlArea.getText().replaceFirst("\\s++$", "").length();
                        sqlArea.insertText(textLength, text);
                        sqlArea.requestFocus();
                    }
                }
            }
        });

        // copy DB/Collection name to clipboard
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        treeView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.C) && event.isControlDown()) {
                String value = treeView.getSelectionModel().getSelectedItem().getValue();
                content.putString(value);
                clipboard.setContent(content);
                event.consume();
            }
        });
    }

    private static void printCountsToStatusBar(Map<String, List<String>> taskValue) {
        int collectionCount = taskValue.values().stream().mapToInt(List::size).sum();
        String str = String.format("DB count: %s, Collection count: %s", taskValue.keySet().size(), collectionCount);
        CommonUtils.setStatusText(str);
    }
}
