package Controller.ConfigurerVoyage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import Controller.ReservationVoyageController;
import Entite.Activite;
import Entite.Budget;
import Entite.Destination;
import Entite.Hotel;
import Entite.Vol;
import Entite.Voyage;
import Service.ServiceActivite;
import Service.ServiceBudget;
import Service.ServiceHotel;
import Service.ServiceVol;
import Service.ServiceVoyage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class BudgetController {

    /* Header / résumé */
    @FXML private Label headerDestLabel;
    @FXML private Label headerDatesLabel;
    @FXML private Label destinationLabel;
    @FXML private Label datesLabel;
    @FXML private Label dureeLabel;

    /* Détail cartes */
    @FXML private Label volTotalLabel;
    @FXML private Label volDetailLabel;
    @FXML private Label hotelTotalLabel;
    @FXML private Label hotelDetailLabel;
    @FXML private Label activitesTotalLabel;
    @FXML private Label activitesDetailLabel;
    @FXML private Label servicesTotalLabel;
    @FXML private Label servicesDetailLabel;

    /* Résumé global */
    @FXML private Label totalResumeLabel;
    @FXML private Label totalVolSmallLabel;
    @FXML private Label totalHotelSmallLabel;
    @FXML private Label totalActivitesSmallLabel;
    @FXML private Label totalServicesSmallLabel;
    @FXML private Label grandTotalLabel;
    @FXML private Label parJourLabel;
    @FXML private Button terminerButton;

    private final ServiceVoyage    serviceVoyage    = new ServiceVoyage();
    private final ServiceVol       serviceVol       = new ServiceVol();
    private final ServiceHotel     serviceHotel     = new ServiceHotel();
    private final ServiceActivite  serviceActivite  = new ServiceActivite();
    private final ServiceBudget    serviceBudget    = new ServiceBudget();
    private final DateTimeFormatter fmt             = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Destination destination;
    private LocalDate   dateDebut;
    private LocalDate   dateFin;
    private int         idVoyage = -1;

    // Totaux calculés (conservés pour enregistrement en BD)
    private double totalVol;
    private double totalHotel;
    private double totalActivites;
    private double totalServices;
    private double totalGlobal;

    // Services supplémentaires sélectionnés (booleans simples depuis l'écran précédent)
    private boolean assurance;
    private boolean transfert;
    private boolean bagage;
    private boolean voiture;
    private boolean wifi;

    // Montants unitaires (doivent être cohérents avec ServicesSuppUserController)
    public static final double PRIX_ASSURANCE = 120;
    public static final double PRIX_TRANSFERT = 80;
    public static final double PRIX_BAGAGE    = 60;
    public static final double PRIX_VOITURE   = 200;
    public static final double PRIX_WIFI      = 50;

    /**
     * Appelée depuis ServicesSuppUserController une fois toute la config terminée.
     */
    public void initDonnees(Destination destination, LocalDate dateDebut, LocalDate dateFin,
                            int idVoyage,
                            boolean assurance, boolean transfert, boolean bagage,
                            boolean voiture, boolean wifi) {
        this.destination = destination;
        this.dateDebut   = dateDebut;
        this.dateFin     = dateFin;
        this.idVoyage    = idVoyage;
        this.assurance   = assurance;
        this.transfert   = transfert;
        this.bagage      = bagage;
        this.voiture     = voiture;
        this.wifi        = wifi;

        mettreAJourHeader();
        calculerBudget();
    }

    private void mettreAJourHeader() {
        if (destination == null || dateDebut == null || dateFin == null) return;
        String destTxt  = destination.getPays() + " — " + destination.getVille();
        String datesTxt = dateDebut.format(fmt) + "  →  " + dateFin.format(fmt);
        long   jours    = ChronoUnit.DAYS.between(dateDebut, dateFin);

        if (destinationLabel != null) destinationLabel.setText("🌍 " + destTxt);
        if (datesLabel       != null) datesLabel.setText(datesTxt);
        if (dureeLabel       != null) dureeLabel.setText(jours + " jour" + (jours > 1 ? "s" : ""));
        if (headerDestLabel  != null) headerDestLabel.setText(destTxt);
        if (headerDatesLabel != null) headerDatesLabel.setText(datesTxt);
    }

    private void calculerBudget() {
        totalVol       = 0;
        totalHotel     = 0;
        totalActivites = 0;
        totalServices  = 0;

        // Chargement du voyage pour récupérer vol/hôtel/dates
        Voyage v;
        try {
            v = serviceVoyage.findbyId(idVoyage);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de récupérer les informations du voyage : " + e.getMessage());
            return;
        }
        if (v == null) {
            showAlert("Erreur", "Voyage introuvable (id=" + idVoyage + ").");
            return;
        }

        // Vol
        try {
            if (v.getIdVol() > 0) {
                Vol vol = serviceVol.findbyId(v.getIdVol());
                if (vol != null) {
                    totalVol = vol.getPrix();
                    volTotalLabel.setText(String.format("%.0f TND", totalVol));
                    volDetailLabel.setText("✈ " + vol.getCompagnie() + " — Vol " + vol.getNumeroVol()
                            + " (" + String.format("%.0f TND", vol.getPrix()) + ")");
                } else {
                    volTotalLabel.setText("—");
                    volDetailLabel.setText("Vol introuvable pour ce voyage.");
                }
            } else {
                volTotalLabel.setText("—");
                volDetailLabel.setText("Aucun vol enregistré pour ce voyage.");
            }
        } catch (SQLException e) {
            volTotalLabel.setText("—");
            volDetailLabel.setText("Erreur lors du chargement du vol : " + e.getMessage());
        }

        // Hôtel
        try {
            if (v.getIdHotel() > 0) {
                Hotel h = serviceHotel.findbyId(v.getIdHotel());
                if (h != null && v.getDateCheckin() != null && v.getDateCheckout() != null
                        && v.getDateCheckout().isAfter(v.getDateCheckin())) {
                    long nuits = ChronoUnit.DAYS.between(v.getDateCheckin(), v.getDateCheckout());
                    totalHotel = h.getPrixParNuit() * nuits;
                    hotelTotalLabel.setText(String.format("%.0f TND", totalHotel));
                    hotelDetailLabel.setText(String.format("🏨 %s — %.0f TND / nuit × %d nuit%s",
                            h.getNom(), h.getPrixParNuit(), nuits, nuits > 1 ? "s" : ""));
                } else if (h != null) {
                    hotelTotalLabel.setText(String.format("%.0f TND", h.getPrixParNuit()));
                    hotelDetailLabel.setText("🏨 " + h.getNom() + " — " +
                            String.format("%.0f TND / nuit (dates incomplètes)", h.getPrixParNuit()));
                } else {
                    hotelTotalLabel.setText("—");
                    hotelDetailLabel.setText("Hôtel introuvable pour ce voyage.");
                }
            } else {
                hotelTotalLabel.setText("—");
                hotelDetailLabel.setText("Aucun hôtel enregistré pour ce voyage.");
            }
        } catch (SQLException e) {
            hotelTotalLabel.setText("—");
            hotelDetailLabel.setText("Erreur lors du chargement de l'hôtel : " + e.getMessage());
        }

        // Activités
        try {
            List<Activite> acts = serviceActivite.findByVoyage(idVoyage);
            if (acts.isEmpty()) {
                activitesTotalLabel.setText("0 TND");
                activitesDetailLabel.setText("Aucune activité sélectionnée pour ce voyage.");
            } else {
                totalActivites = acts.stream().mapToDouble(Activite::getPrix).sum();
                activitesTotalLabel.setText(String.format("%.0f TND", totalActivites));
                activitesDetailLabel.setText(acts.size() + " activité"
                        + (acts.size() > 1 ? "s" : "") + " sélectionnée"
                        + (acts.size() > 1 ? "s" : "")
                        + " — total " + String.format("%.0f TND", totalActivites));
            }
        } catch (SQLException e) {
            activitesTotalLabel.setText("—");
            activitesDetailLabel.setText("Erreur lors du chargement des activités : " + e.getMessage());
        }

        // Services supplémentaires (calculés à partir des booleans)
        StringBuilder sb = new StringBuilder();
        if (assurance) {
            totalServices += PRIX_ASSURANCE;
            sb.append("• Assurance voyage (").append((int) PRIX_ASSURANCE).append(" TND)\n");
        }
        if (transfert) {
            totalServices += PRIX_TRANSFERT;
            sb.append("• Transfert aéroport (").append((int) PRIX_TRANSFERT).append(" TND)\n");
        }
        if (bagage) {
            totalServices += PRIX_BAGAGE;
            sb.append("• Bagage supplémentaire (").append((int) PRIX_BAGAGE).append(" TND)\n");
        }
        if (voiture) {
            totalServices += PRIX_VOITURE;
            sb.append("• Location de voiture (").append((int) PRIX_VOITURE).append(" TND)\n");
        }
        if (wifi) {
            totalServices += PRIX_WIFI;
            sb.append("• Wi-Fi portable (").append((int) PRIX_WIFI).append(" TND)\n");
        }

        if (totalServices == 0) {
            servicesTotalLabel.setText("0 TND");
            servicesDetailLabel.setText("Aucun service supplémentaire sélectionné.");
        } else {
            servicesTotalLabel.setText(String.format("%.0f TND", totalServices));
            servicesDetailLabel.setText(sb.toString().trim());
        }

        // Mise à jour du résumé global
        totalGlobal = totalVol + totalHotel + totalActivites + totalServices;

        totalVolSmallLabel.setText(String.format("%.0f TND", totalVol));
        totalHotelSmallLabel.setText(String.format("%.0f TND", totalHotel));
        totalActivitesSmallLabel.setText(String.format("%.0f TND", totalActivites));
        totalServicesSmallLabel.setText(String.format("%.0f TND", totalServices));

        grandTotalLabel.setText(String.format("%.0f TND", totalGlobal));
        totalResumeLabel.setText("Total estimé pour l'ensemble de votre configuration");

        if (dateDebut != null && dateFin != null && dateFin.isAfter(dateDebut)) {
            long jours = ChronoUnit.DAYS.between(dateDebut, dateFin);
            if (jours > 0) {
                double parJour = totalGlobal / jours;
                parJourLabel.setText(String.format("≈ %.0f TND par jour sur %d jour%s",
                        parJour, jours, jours > 1 ? "s" : ""));
            }
        } else {
            parJourLabel.setText("");
        }
    }

    @FXML
    private void retourEtapePrecedente() {
        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/ServicesSuppUser.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/ServicesSuppUser.fxml");
        if (url == null) {
            showAlert("Erreur", "ServicesSuppUser.fxml introuvable.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            ServicesSuppUserController ctrl = loader.getController();
            ctrl.initDonnees(destination, dateDebut, dateFin, idVoyage);
            Stage stage = (Stage) grandTotalLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Services supplémentaires");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger ServicesSuppUser.fxml : " + e.getMessage());
        }
    }

    @FXML
    private void terminerConfiguration() {
        if (idVoyage <= 0) {
            showAlert("Information",
                    "L'identifiant du voyage est invalide. Impossible de procéder à la réservation.");
            return;
        }

        // Enregistrement du budget en BD
        try {
            Budget b = new Budget(idVoyage, totalVol, totalHotel, totalActivites, totalServices, totalGlobal);
            int idBudget = serviceBudget.enregistrer(b);
            if (idBudget > 0) {
                serviceVoyage.mettreAJourBudget(idVoyage, idBudget);
            }
        } catch (Exception e) {
            showAlert("Avertissement",
                    "Le budget a été calculé mais n'a pas pu être enregistré : " + e.getMessage()
                            + "\nVous pouvez tout de même continuer la réservation.");
        }

        // ── Ouvrir l'écran de réservation ──
        URL url = getClass().getClassLoader().getResource("ReservationUser/ReservationVoyage.fxml");
        if (url == null) url = getClass().getResource("/ReservationUser/ReservationVoyage.fxml");
        if (url == null) {
            showAlert("Erreur", "ReservationVoyage.fxml introuvable.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            ReservationVoyageController ctrl = loader.getController();
            ctrl.initDonnees(
                    destination,
                    dateDebut,
                    dateFin,
                    idVoyage,
                    totalVol,
                    totalHotel,
                    totalActivites,
                    totalServices,
                    totalGlobal
            );

            Stage stage = (Stage) grandTotalLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Réservation");
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "ReservationVoyage.fxml : " + e.getMessage());
        }
    }


    @FXML
    private void onMouseEnteredButton(javafx.scene.input.MouseEvent e)  {
        ((Button)e.getSource()).setOpacity(0.85);
    }

    @FXML
    private void onMouseExitedButton(javafx.scene.input.MouseEvent e)   {
        ((Button)e.getSource()).setOpacity(1.0);
    }

    @FXML
    private void onMouseEnteredSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#229954,#27AE60);-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(39,174,96,0.6),16,0,0,5);"
        );
    }

    @FXML
    private void onMouseExitedSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button)e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#27AE60,#2ECC71);-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(39,174,96,0.45),12,0,0,4);"
        );
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}