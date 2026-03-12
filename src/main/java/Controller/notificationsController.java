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
    private int currentUserId;
    public void setUserId(int id){
        this.currentUserId = id;
        loadNotifications();
    }
    @FXML
    private VBox notificationsContainer;
    // Déclarer en tant que membre de classe (en haut, avec tes autres services)
    private final ServiceNotification serviceNotification = new ServiceNotification();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private void loadNotifications() {

        List<Notification> notifications =
                serviceNotification.getNotificationsByUser(currentUserId);

        notificationsContainer.getChildren().clear();

        for (Notification n : notifications) {

            HBox notificationBox = new HBox();
            notificationBox.setSpacing(15);
            if(!n.isLu()){
                notificationBox.setStyle("-fx-background-color:#E8F4FF; -fx-padding:12; -fx-background-radius:8;");
            }else{
                notificationBox.setStyle("-fx-background-color:white; -fx-padding:12; -fx-background-radius:8;");
            }

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

            serviceNotification.markAsRead(n.getIdNotification());
        }

    }
}
