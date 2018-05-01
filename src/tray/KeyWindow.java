package tray;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class KeyWindow {

    private static boolean isReady;
    private static byte[] keyResult;

    public static synchronized byte[] changeKey(boolean isChange) {
        isReady = false;
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle(isChange ? "Change Key" : "Set Key");
            dialog.setHeaderText("Enter Key seed with at least 32 letters");
            dialog.setContentText("Seed:");
            Node loginButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            loginButton.setDisable(true);
            dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                loginButton.setDisable(newValue.length() < 32);
            });

            Optional<String> result = dialog.showAndWait();
            result.ifPresentOrElse(key -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Restart");
                alert.setHeaderText(null);
                alert.setContentText("Restart the program for the new key to take effect.");
                alert.showAndWait();
                keyResult = key.getBytes();
                isReady = true;
            }, () -> {
                keyResult = null;
                isReady = true;
            });
        });

        while (!isReady) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return keyResult;
    }
}
