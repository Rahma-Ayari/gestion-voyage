package Controller;

import Entite.Notification;
import Service.ServiceNotification;
import javafx.fxml.FXML;

import java.sql.SQLException;
import java.util.List;

public class NotificationController {

    private ServiceNotification service = new ServiceNotification();

    @FXML
    public void initialize() {

        System.out.println("Notification page loaded successfully.");

        loadNotificationsFromDatabase();
    }

    private void loadNotificationsFromDatabase() {
        try {
            List<Notification> notifications = service.readAll();

            System.out.println("Nombre total de notifications: " + notifications.size());

            // For now we just print them in console
            // Your FXML currently contains static notification cards
            // If you want dynamic cards later, we can generate them inside a VBox

            for (Notification n : notifications) {
                System.out.println("ID: " + n.getIdNotification());
                System.out.println("Message: " + n.getMessage());
                System.out.println("Type: " + n.getTypeNotification());
                System.out.println("Lu: " + n.isLu());
                System.out.println("Reservation ID: " + n.getIdReservation());
                System.out.println("-------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
