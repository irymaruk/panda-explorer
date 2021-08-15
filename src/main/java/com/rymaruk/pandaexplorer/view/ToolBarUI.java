package com.rymaruk.pandaexplorer.view;

import com.rymaruk.pandaexplorer.MainApp;
import com.rymaruk.pandaexplorer.misc.CommonUtils;
import com.rymaruk.pandaexplorer.misc.ConvertJsonToCsv;
import com.rymaruk.pandaexplorer.misc.config.ConfigReader;
import com.rymaruk.pandaexplorer.model.DateTimeEnum;
import com.rymaruk.pandaexplorer.task.SqlExecutionTask;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ToolBarUI {

    private static final Logger log = LoggerFactory.getLogger(ToolBarUI.class);
    private static ComboBox<String> comboBox;
    private static Button executeBtn;
    private static BooleanProperty fetchAll;
    private static ObjectProperty<String> dateTimeFormatProperty;
    private final CodeArea scriptArea = ScriptEditUI.getArea();

    public static ComboBox<String> getEnvComboBox() {
        return comboBox;
    }

    public static String getEnv() {
        return getEnvComboBox().valueProperty().get();
    }

    public static Button getExecuteBtn() {
        return executeBtn;
    }

    public static BooleanProperty getFetchAllProperty() {
        return fetchAll;
    }

    public static ObjectProperty<String> getDatetimeFormatProperty() {
        return dateTimeFormatProperty;
    }

    public HBox get() {
        return init();
    }

    private HBox init() {
        HBox hBox = new HBox(
                getEnvironmentComboBox(),
                getExecutionBtn(),
                getClipboardCopyBtn(),
                getHelpBtn(),
                getFetchAllChkBox(),
                getDatetimeComboBox()
        );
        hBox.setSpacing(50);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    private Node getFetchAllChkBox() {
        CheckBox checkBox = new CheckBox("Fetch all");
        fetchAll = checkBox.selectedProperty();
        checkBox.setSelected(false);
        checkBox.setTooltip(new Tooltip("Retrieve all documents from DB or only 100"));
        return checkBox;
    }

    private Node getDatetimeComboBox() {
        Label label = new Label("Datetime:");
        ComboBox<String> comboBox = new ComboBox<>();
        dateTimeFormatProperty = comboBox.valueProperty();
        comboBox.getItems().addAll(DateTimeEnum.getAllStringValues());
        comboBox.setValue(DateTimeEnum.RAW.name());
        comboBox.setTooltip(new Tooltip("Convert Milliseconds to human-readable Datetime"));
        HBox hBox = new HBox(label, comboBox);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));
        return hBox;
    }

    private Button getClipboardCopyBtn() {
        Button copyBtn = new Button("Copy as csv", CommonUtils.getImageView("copy.png"));
        copyBtn.setPrefWidth(150);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        copyBtn.setOnAction(event -> {
            content.putString(ConvertJsonToCsv.convert(ExecutionResultUI.getExecutionResult().getResponse()));
            clipboard.setContent(content);
        });
        return copyBtn;
    }

    private Button getHelpBtn() {
        Button helpBtn = new Button("Help", CommonUtils.getImageView("help.png"));
        helpBtn.setPrefWidth(150);
        Alert alert = getHelpAlert();
        helpBtn.setOnAction(event -> {
            alert.showAndWait();
        });
        return helpBtn;
    }

    private Alert getHelpAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Panda Explorer for Microsoft Azure Cosmos DB");
        WebView webView = new WebView();
        webView.getEngine().load(CommonUtils.getResource("/help.html").toString());
        webView.setPrefSize(700, 770);
        alert.getDialogPane().setContent(webView);
        WebEngine webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Document document = webEngine.getDocument();
                NodeList nodeList = document.getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    org.w3c.dom.Node node = nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener("click", evt -> {
                        EventTarget target = evt.getCurrentTarget();
                        HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                        String href = anchorElement.getHref();
                        log.info("href = {}", href);
                        MainApp.getAppInstance().getHostServices().showDocument(href);
                        evt.preventDefault();
                    }, false);
                }
            }
        });
        return alert;
    }

    private Node getEnvironmentComboBox() {
        Label label = new Label("Environment:");
        comboBox = new ComboBox<>();
        comboBox.setPrefWidth(180);
        comboBox.getItems().addAll(ConfigReader.getAllEnvNames());
        HBox hBox = new HBox(label, comboBox);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));
        return hBox;
    }

    private Node getExecutionBtn() {
        executeBtn = new Button("Execute", CommonUtils.getImageView("execute.png"));
        executeBtn.setOnAction(event -> {
            ExecutionResultUI.setExecutionResultText("Executing query...");
            String rawSqlStr = getSql(scriptArea);
            SqlExecutionTask task = new SqlExecutionTask(getEnv(), rawSqlStr, getFetchAllProperty());
            CommonUtils.bindProgress(task);
            task.setOnSucceeded(e -> updateResultAreaSuccess(task));
            task.setOnFailed(e -> updateResultAreaFailed(task.getException()));
            CommonUtils.getExecutorService().submit(task);
        });
        executeBtn.setTooltip(new Tooltip("F5 or Ctrl+ENTER to execute query"));
        scriptArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.ENTER) && event.isControlDown() || event.getCode().equals(KeyCode.F5)) {
                executeBtn.fire();
                event.consume();
            }
        });
        executeBtn.setDisable(true);
        executeBtn.setPrefWidth(150);
        return executeBtn;
    }

    private void updateResultAreaSuccess(SqlExecutionTask task) {
        ExecutionResultUI.setExecutionResult(task.getValue());
        CommonUtils.setStatusText(task.getMessage());
    }

    private void updateResultAreaFailed(Throwable result) {
        ExecutionResultUI.setExecutionResultText(result.toString());
    }


    /**
     * Returns SQL for multiline queries even if cursor was placed in the middle of the query
     *
     * @param area
     * @return slq as string
     */
    private String getSql(StyleClassedTextArea area) {
        String selectedText = area.getSelectedText();
        if (!selectedText.isEmpty()) {
            return Arrays.stream(selectedText.split("\n"))
                    .filter(s -> !s.trim().startsWith("--"))
                    .collect(Collectors.joining(" \n"));

        } else {
            int currentParagraph = area.getCurrentParagraph();
            int startParagraph = findStartParagraph(currentParagraph, area);
            int endParagraph = findEndParagraph(startParagraph, area);
            return area.getParagraphs()
                    .subList(startParagraph, endParagraph + 1)
                    .stream().map(Paragraph::getText)
                    .filter(s -> !s.trim().startsWith("--"))
                    .collect(Collectors.joining(" \n"));
        }
    }

    private int findStartParagraph(int paragraph, StyleClassedTextArea area) {
        if (paragraph == 0) {
            return 0;
        } else {
            boolean isEmpty = area.getText(paragraph).trim().isEmpty();
            return isEmpty ? paragraph : findStartParagraph(paragraph - 1, area);
        }
    }

    private int findEndParagraph(int paragraph, StyleClassedTextArea area) {
        int max = area.getParagraphs().size() - 1;
        int next = paragraph + 1;
        if (paragraph >= max) {
            return max;
        } else {
            boolean isEmpty = area.getText(next).trim().isEmpty();
            return isEmpty ? paragraph : findEndParagraph(next, area);
        }
    }
}
