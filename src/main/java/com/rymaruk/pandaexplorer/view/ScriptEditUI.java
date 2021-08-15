package com.rymaruk.pandaexplorer.view;

import com.rymaruk.pandaexplorer.misc.FileUtils;
import com.rymaruk.pandaexplorer.service.AutoSaveScriptService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.commons.lang.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptEditUI {

    private final BorderPane sqlPane = new BorderPane();
    private static final CodeArea area = new CodeArea();

    public Node get() {
        return sqlPane;
    }

    public static CodeArea getArea() {
        return area;
    }

    public ScriptEditUI() {
        area.setWrapText(true);
        area.requestFocus();
        addLineNumbers();
        addSyntaxHighlighting();
        area.setPadding(new Insets(5, 5, 5, 0));
        VirtualizedScrollPane<StyleClassedTextArea> vsPane = new VirtualizedScrollPane<>(area);
        sqlPane.setCenter(vsPane);
        loadScripts();
        addCopyHandler();
        AutoSaveScriptService.start(area);
        addToggleCommentBtn();
        addDuplicateLineBtn();
    }

    // fix for null values in windows clipboard
    private void addCopyHandler() {
        area.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                String clipboardString = clipboard.getString();
                if (clipboardString != null) {
                    String newStr = clipboardString.replace("\0", "");
                    ClipboardContent content = new ClipboardContent();
                    content.putString(newStr);
                    clipboard.setContent(content);
                }
            }
        });
    }

    public void addLineNumbers() {
        IntFunction<Node> numberFactory = LineNumberFactory.get(area);
        IntFunction<Node> arrowFactory = new ArrowFactory(area.currentParagraphProperty());
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox(
                    numberFactory.apply(line),
                    arrowFactory.apply(line));
            hbox.setAlignment(Pos.CENTER_LEFT);
            return hbox;
        };
        area.setParagraphGraphicFactory(graphicFactory);
    }

    private void addToggleCommentBtn() {
        Button toggleCommentBtn = new Button();
        toggleCommentBtn.setVisible(false);
        toggleCommentBtn.setOnAction(event -> {
            int currentParagraph = area.getCurrentParagraph();
            int caretColumn = area.getCaretColumn();
            String text = area.getParagraph(currentParagraph).getText();
            int moveCaretColumnTo = text.startsWith("--") ? caretColumn - 2 : caretColumn + 2;
            String newText = text.startsWith("--") ? StringUtils.removeStart(text, "--") : "--" + text;
            area.replaceText(currentParagraph, 0, currentParagraph, text.length(), newText);
            area.moveTo(currentParagraph, moveCaretColumnTo);
        });
        area.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.SLASH) && event.isControlDown()) {
                toggleCommentBtn.fire();
                event.consume();
            }
        });
    }

    private void addDuplicateLineBtn() {
        Button duplicateBtn = new Button();
        duplicateBtn.setVisible(false);
        duplicateBtn.setOnAction(event -> {
            int currentParagraph = area.getCurrentParagraph();
            int caretColumn = area.getCaretColumn();
            String text = area.getParagraph(currentParagraph).getText();
            String newText = String.join("\n", text, text);
            area.replaceText(currentParagraph, 0, currentParagraph, text.length(), newText);
            area.moveTo(currentParagraph + 1, caretColumn);
        });
        area.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.D) && event.isControlDown()) {
                duplicateBtn.fire();
                event.consume();
            }
        });
    }

    private void loadScripts() {
        String text;
        try {
            text = FileUtils.readScriptContent();
        } catch (Exception e) {
            text = e.toString();
        }
        area.replaceText(text);
    }


//    Syntax Highlighting

    private void addSyntaxHighlighting() {
        Subscription cleanupWhenNoLongerNeedIt = area
                .multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(ignore -> area.setStyleSpans(0, computeHighlighting(area.getText())));
    }

    private static final String[] KEYWORDS = new String[]{
            "SELECT", "FROM", "AS", "WHERE", "IN", "AND", "COUNT", "DISTINCT", "BETWEEN", "ORDER BY", "GROUP BY"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "--.*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")",
            Pattern.CASE_INSENSITIVE
    );

    private StyleSpans<? extends Collection<String>> computeHighlighting(String text) {

        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("COMMENT") != null ? "comment" :
                                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
