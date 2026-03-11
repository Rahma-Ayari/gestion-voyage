package Controller;
import Entite.Notification;
import Service.ServiceNotification;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
public class notificationsController implements Initializable {
    @FXML
    private VBox notificationsContainer;

    private ServiceNotification serviceNotification = new ServiceNotification();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadNotifications();
    }

    private void loadNotifications() {
        try {

            List<Notification> notifications = serviceNotification.readAll();

            notificationsContainer.getChildren().clear();

            for (Notification n : notifications) {

                HBox notificationBox = new HBox();
                notificationBox.setSpacing(15);
                notificationBox.setStyle("-fx-background-color:white; -fx-padding:12; -fx-background-radius:8;");

                Label icon = new Label("🔔");
                icon.setStyle("-fx-font-size:22px;");

                VBox textBox = new VBox();

                Label message = new Label(n.getMessage());
                message.setStyle("-fx-font-size:14px;");

                Label date = new Label(
                        n.getDateNotification().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
                date.setStyle("-fx-text-fill:gray;");

                textBox.getChildren().addAll(message, date);

                notificationBox.getChildren().addAll(icon, textBox);

                notificationsContainer.getChildren().add(notificationBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
