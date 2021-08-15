package com.rymaruk.pandaexplorer.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainUI {
    BorderPane borderPane;
    public static Label progressText = new Label();
    public static ProgressBar progressBar = new ProgressBar();

    public BorderPane get() {
        return borderPane;
    }

    public MainUI() {
        borderPane = new BorderPane();
        borderPane.setTop(new ToolBarUI().get());
        borderPane.setCenter(getSectionTabPane());
        borderPane.setBottom(getStatusBar());
    }

    private Node getStatusBar() {
        progressBar.setVisible(false);
        progressBar.setPrefWidth(400);
        progressBar.setPadding(new Insets(0, 100, 0, 0));
        progressText.setWrapText(true);
        HBox hBox = new HBox(progressText, progressBar);
        hBox.setSpacing(20);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        return hBox;
    }

    private Node getSectionTabPane() {
        SplitPane splitPane = new SplitPane(
                new ConnectionUI().get(),
                new ScriptEditUI().get(),
                new ExecutionResultUI().get());
        splitPane.setDividerPositions(0.20, 0.60);
        return splitPane;
    }
}
