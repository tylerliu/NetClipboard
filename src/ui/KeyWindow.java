package ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import key.KeyUtil;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

class KeyWindow {
    private static byte[] keyResult;

    static synchronized byte[] changeKey(boolean isChange) {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle(isChange ? "Change Key" : "Set Key");
            dialog.setHeaderText("Enter key seed with at least " + KeyUtil.KEY_LEN + " letters");
            dialog.setContentText("Seed:");
            Node loginButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            loginButton.setDisable(true);
            dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
                    loginButton.setDisable(newValue.length() < KeyUtil.KEY_LEN));

            Optional<String> result = dialog.showAndWait();
            result.ifPresentOrElse(key -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Restart");
                alert.setHeaderText(null);
                alert.setContentText("Restart the program for the new key to take effect.");
                alert.getDialogPane().getScene().getWindow().requestFocus();
                alert.showAndWait();
                keyResult = key.getBytes();
            }, () -> keyResult = null);
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            UserInterfacing.printError(e);
        }
        return keyResult;
    }
}
