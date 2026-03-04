package Controller;

import Entite.Offre;
import Service.ServiceOffre;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationUserController implements Initializable {

    @FXML
    private FlowPane offreContainer;

    private ServiceOffre serviceOffre;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceOffre = new ServiceOffre();
        afficherOffres();
    }

    private void afficherOffres() {
        try {
            List<Offre> offres = serviceOffre.readAll();
            offreContainer.getChildren().clear();

            for (Offre o : offres) {
                VBox card = createCard(o);
                offreContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            System.out.println("Erreur affichage offres: " + e.getMessage());
        }
    }

    private VBox createCard(Offre o) {

        VBox card = new VBox(12);
        card.setPrefWidth(320);
        card.setPadding(new Insets(18));
        card.setStyle("""
                -fx-background-color:white;
                -fx-background-radius:20;
                -fx-border-radius:20;
                -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.12),15,0,0,5);
                """);

        Label typeLabel = new Label(o.getType());
        typeLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        Label descLabel = new Label(o.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill:#777;");

        Label prixLabel = new Label(o.getPrix() + " DT");
        prixLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#FF6B35;");

        Label dispoLabel = new Label(
                o.isDisponibilite() ? "Disponible ✅" : "Indisponible ❌"
        );

        dispoLabel.setStyle(o.isDisponibilite()
                ? "-fx-text-fill:green;"
                : "-fx-text-fill:red;"
        );

        Button reserverBtn = new Button("Réserver");
        reserverBtn.setMaxWidth(Double.MAX_VALUE);
        reserverBtn.setDisable(!o.isDisponibilite());

        reserverBtn.setStyle("""
                -fx-background-color: linear-gradient(to right,#FF6B35,#F7931E);
                -fx-text-fill:white;
                -fx-font-weight:bold;
                -fx-background-radius:12;
                """);

        reserverBtn.setOnAction(e -> {
            System.out.println("Réserver offre ID: " + o.getId_offre());
            // 👉 هنا باش نعملو création reservation
        });

        card.getChildren().addAll(typeLabel, descLabel, prixLabel, dispoLabel, reserverBtn);

        return card;
    }
}