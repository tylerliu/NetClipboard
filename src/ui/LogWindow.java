package ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.io.PrintWriter;
import java.io.StringWriter;

class LogWindow {
    private static Alert warnings = null;

    private static TextArea textArea;
    private static GridPane expContent;

    static void init() {
        if (textArea != null) return;
        new JFXPanel();
        Platform.setImplicitExit(false);
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
    }

    static void toggle() {
        init();
        Platform.runLater(() -> {
            warnings = new Alert(Alert.AlertType.INFORMATION);
            warnings.setTitle("Warning Logs");
            warnings.setHeaderText("Logs for Debug and Warning");
            warnings.getDialogPane().setContent(expContent);
            warnings.show();
            expContent.requestFocus();
        });
    }

    /**
     * This method appends the data to the text area.
     *
     * @param data the Logging information data
     */
    static void showInfo(String data) {
        init();
        Platform.runLater(() -> textArea.appendText(data));
    }

    static void showError(Exception e) {
        init();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        showInfo(sw.toString());
    }
}
