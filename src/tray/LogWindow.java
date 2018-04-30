package tray;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

class LogWindow {
    //TODO change to jFX window
    private static Alert warnings = null;

    private static TextArea textArea;

    public static void init() {
        if (warnings != null) return;

        new JFXPanel();
        Platform.runLater(() -> {
            warnings = new Alert(Alert.AlertType.INFORMATION);
            warnings.setTitle("Warning Logs");
            warnings.setHeaderText("Logs for Debug and Warning");

            textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);

            warnings.getDialogPane().setContent(expContent);
            warnings.setOnCloseRequest((e) -> warnings.hide());
        });
    }

    public static void toggle() {
        init();
        Platform.runLater(() -> {if (!warnings.isShowing()) warnings.show();});
    }

    /**
     * This method appends the data to the text area.
     *
     * @param data
     *            the Logging information data
     */
    public static void showInfo(String data) {
        init();
        Platform.runLater(() -> textArea.appendText(data));
        //this.getContentPane().validate();
    }

    public static void showError(Exception e) {
        init();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        showInfo(sw.toString());
    }
}
