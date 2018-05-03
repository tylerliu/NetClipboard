package ui;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import net.handshake.DirectConnect;
import net.handshake.KeyBased;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class DirectConnectWindow {

    public static InetAddress showConnFailWarning(String reason) {
        AtomicReference<InetAddress> address = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Net Clipboard Connection");
            alert.setHeaderText("Fail to connect to the other computer");
            alert.setContentText(reason);

            ButtonType tryAgainButton = new ButtonType("Try again");
            ButtonType directConnButton = new ButtonType("Enter IP...");
            ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(tryAgainButton, directConnButton, exitButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == tryAgainButton) {
                new Thread(() -> {
                    address.set(KeyBased.getTarget());
                    latch.countDown();
                }).start();
            } else if (result.get() == directConnButton) {
                String name = getDirectConnIP();
                if (name != null)
                    address.set(DirectConnect.getTarget(name));
                else address.set(null);
                latch.countDown();
            } else {
                address.set(null);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return address.get();
    }

    public static String getDirectConnIP() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connect");
        dialog.setHeaderText("");
        dialog.setContentText("Enter IP of other Computer: ");
        try {
            Label label = new Label("The IP of this computer is " + InetAddress.getLocalHost().getHostAddress());
            GridPane pane = new GridPane();
            pane.add(dialog.getDialogPane().getContent(), 0, 0);
            pane.add(label, 0, 1);
            dialog.getDialogPane().setContent(pane);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}
