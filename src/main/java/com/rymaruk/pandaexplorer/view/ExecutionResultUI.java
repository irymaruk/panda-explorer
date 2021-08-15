package com.rymaruk.pandaexplorer.view;

import com.rymaruk.pandaexplorer.model.DateTimeEnum;
import com.rymaruk.pandaexplorer.model.SqlResultTO;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecutionResultUI {

    private static final Logger log = LoggerFactory.getLogger(ExecutionResultUI.class);
    private static final CodeArea area = new CodeArea();
    private static final String[] KEYWORDS = new String[]{
            "SELECT", "FROM", "AS", "WHERE", "IN", "AND", "COUNT", "DISTINCT", "BETWEEN", "ORDER BY", "GROUP BY"
    };
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String JSON_KEYS_PATTERN = "\"\\w+\" :";
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<JSONKEYS>" + JSON_KEYS_PATTERN + ")",
            Pattern.CASE_INSENSITIVE
    );
    private static SqlResultTO executionResult = new SqlResultTO("");
    private final BorderPane resultPane = new BorderPane();
    private final TextField searchField = new TextField();
    private Subscription cleanupWhenNoLongerNeedIt;

    public ExecutionResultUI() {
        area.setWrapText(true);
        VirtualizedScrollPane<StyleClassedTextArea> vsPane = new VirtualizedScrollPane<>(area);
        area.setPadding(new Insets(5));
        area.setEditable(false);
        resultPane.setCenter(vsPane);
        initSearchField();
        addSyntaxHighlighting();
        ToolBarUI.getFetchAllProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                log.debug("Removing highlighting");
                cleanupWhenNoLongerNeedIt.unsubscribe();
            } else {
                addSyntaxHighlighting();
            }
        });
        ToolBarUI.getDatetimeFormatProperty().addListener((observable, oldValue, newValue) -> {
            area.replaceText(convertTimestampToDatetime(executionResult.getFormattedString(), newValue));
        });
    }

    public static SqlResultTO getExecutionResult() {
        return executionResult;
    }

    public static void setExecutionResult(SqlResultTO result) {
        executionResult = result;
        setExecutionResultText(result.getFormattedString());
    }

    public static void setExecutionResultText(String text) {
        area.replaceText(convertTimestampToDatetime(text, ToolBarUI.getDatetimeFormatProperty().get()));
    }

    private static String convertTimestampToDatetime(String text, String format) {
        return convertTimestampToDatetime(text, DateTimeEnum.valueOf(format));
    }

    private static String convertTimestampToDatetime(String text, DateTimeEnum format) {
        if (text == null) return "";
        if (format.equals(DateTimeEnum.RAW)) return text;
        Pattern pattern = Pattern.compile("(\\b\\d{12,14}\\b)");
        Matcher matcher = pattern.matcher(text);
        ArrayList<List> replaceList = new ArrayList<>();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String substring = text.substring(start, end);
            Instant instant = Instant.ofEpochMilli(Long.parseLong(substring));
            String datetime = formatInstantDateTime(instant, format);
            replaceList.add(Arrays.asList(start, end, String.format("\"%s\"", datetime)));
        }
        Collections.reverse(replaceList);
        StringBuilder stringBuilder = new StringBuilder(text);
        for (List l : replaceList) {
            stringBuilder.delete((int) l.get(0), (int) l.get(1));
            stringBuilder.insert((int) l.get(0), l.get(2).toString());
        }
        return stringBuilder.toString();
    }

    private static String formatInstantDateTime(Instant instant, DateTimeEnum format) {
        switch (format) {
            case LOCAL:
                return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).replace("T", " ");
            case UTC:
                return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).replace("T", " ");
            default:
                throw new IllegalArgumentException("Not supported DateTime format = " + format);
        }
    }

    public Node get() {
        return resultPane;
    }

    private Node initSearchField() {
        searchField.setPadding(new Insets(5));
        searchField.setVisible(false);
        searchField.textProperty().addListener(observable -> {
            searchField.setStyle("-fx-text-fill: black;");
        });
        searchField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                String searchStr = searchField.getText();
                int from = StringUtils.indexOfIgnoreCase(area.getText(), searchStr, area.getCaretPosition());
                if (from == -1) {
                    from = StringUtils.indexOfIgnoreCase(area.getText(), searchStr, 0);
                }
                if (from != -1) {
                    area.selectRange(from, from + searchStr.length());
                    area.showParagraphInViewport(area.getCurrentParagraph());
                } else {
                    area.selectRange(area.getCaretPosition(), area.getCaretPosition());
                    searchField.setStyle("-fx-text-fill: red;");
                }
            }
        });
        addSearchBtn();
        area.textProperty().addListener(e -> hideSearchField());
        return searchField;
    }

    private void addSearchBtn() {
        Button searchBtn = new Button();
        searchBtn.setVisible(false);
        area.addEventHandler(KeyEvent.KEY_PRESSED, getCtrlFEventHandler());
        area.addEventHandler(KeyEvent.KEY_PRESSED, getEscEventHandler());
        searchField.addEventHandler(KeyEvent.KEY_PRESSED, getCtrlFEventHandler());
        searchField.addEventHandler(KeyEvent.KEY_PRESSED, getEscEventHandler());
    }

    //    Syntax Highlighting

    private EventHandler<KeyEvent> getCtrlFEventHandler() {
        return event -> {
            if (event.getCode().equals(KeyCode.F) && event.isControlDown()) {
                event.consume();
                if (searchField.isVisible()) {
                    hideSearchField();
                } else {
                    showSearchField();
                }
            }
        };
    }

    private EventHandler<KeyEvent> getEscEventHandler() {
        return event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                event.consume();
                hideSearchField();
            }
        };
    }

    private void hideSearchField() {
        if (searchField.isVisible()) {
            searchField.clear();
            searchField.setVisible(false);
            resultPane.setTop(null);
            area.requestFocus();
        }
    }

    private void showSearchField() {
        resultPane.setTop(searchField);
        searchField.setVisible(true);
        searchField.requestFocus();
    }

    private void addSyntaxHighlighting() {
        log.debug("Adding highlighting");
        cleanupWhenNoLongerNeedIt = area
                .multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(ignore -> area.setStyleSpans(0, computeHighlighting(area.getText())));
    }

    private StyleSpans<? extends Collection<String>> computeHighlighting(String text) {
        long startTime = System.nanoTime();
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("JSONKEYS") != null ? "json-keys" :
                                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        log.debug("computeHighlighting time mls: {}", (System.nanoTime() - startTime) / 1_000_000);
        return spansBuilder.create();
    }
}
