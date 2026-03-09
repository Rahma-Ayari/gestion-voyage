package Controller;

import Entite.*;
import Service.ServiceReservation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class ReservationAdminController implements Initializable {

    // ===== TABLE ET COLONNES =====
    @FXML private TableView<Reservation>            tableView;
    @FXML private TableColumn<Reservation, Integer> idCol;
    @FXML private TableColumn<Reservation, String>  clientCol;
    @FXML private TableColumn<Reservation, String>  offreCol;
    @FXML private TableColumn<Reservation, String>  dateReservationCol;
    @FXML private TableColumn<Reservation, String>  dateVoyageCol;
    @FXML private TableColumn<Reservation, String>  nbPersonnesCol;
    @FXML private TableColumn<Reservation, Double>  montantCol;
    @FXML private TableColumn<Reservation, String>  statutCol;
    @FXML private TableColumn<Reservation, String>  commentaireCol;

    // ===== STATS =====
    @FXML private Label totalLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label accepteesLabel;
    @FXML private Label refuseesLabel;
    @FXML private Label annuleesLabel;

    // ===== LEFT PANEL =====
    @FXML private VBox             detailsBox;
    @FXML private ComboBox<String> statutFilter;
    @FXML private TextField        searchField;

    // ===== BOUTONS =====
    @FXML private Button accepterBtn;
    @FXML private Button refuserBtn;
    @FXML private Button annulerBtn;

    private final ServiceReservation    service        = new ServiceReservation();
    private ObservableList<Reservation> observableList = FXCollections.observableArrayList();

    // ===== CONFIG EMAIL (à adapter) =====
    private static final String MAIL_HOST     = "smtp.gmail.com";
    private static final String MAIL_PORT     = "587";
    private static final String MAIL_USERNAME = "eya.mardessi@gmail.com";   // ← modifier
    private static final String MAIL_PASSWORD = "keggvmctfehvvdns";  // ← modifier (mot de passe app Gmail)
    private static final String MAIL_FROM     = "TripEase <eya.mardessi@gmail.com>";

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        configurerFiltres();
        configurerRecherche();
        configurerSelection();
        loadData();
    }

    // ══════════════════════════════════════════════════════
    //  COLONNES
    // ══════════════════════════════════════════════════════

    private void configurerColonnes() {

        idCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getId_reservation()).asObject());

        clientCol.setCellValueFactory(data -> {
            Personne p = data.getValue().getId_personne();
            if (p == null) return new SimpleStringProperty("—");
            String nom = ((p.getNom() != null ? p.getNom() : "") + " "
                    + (p.getPrenom() != null ? p.getPrenom() : "")).trim();
            return new SimpleStringProperty(nom.isBlank() ? "Client #" + p.getIdUtilisateur() : nom);
        });

        offreCol.setCellValueFactory(data -> {
            Offre o = data.getValue().getId_offre();
            if (o == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(
                    o.getType() != null ? o.getType() : "Offre #" + o.getId_offre());
        });

        dateReservationCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDate_reservation() != null
                                ? data.getValue().getDate_reservation().toString() : "—"));

        dateVoyageCol.setCellValueFactory(data -> {
            Voyage v = data.getValue().getId_voyage();
            if (v == null || v.getDateDebut() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(v.getDateDebut().toString());
        });

        // ── CORRECTION : affiche le vrai nombre de personnes ──
        nbPersonnesCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.valueOf(data.getValue().getNombre_personnes())));

        montantCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getPrix_reservation()).asObject());

        // Statut coloré
        statutCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEtat() != null ? data.getValue().getEtat() : "—"));
        statutCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) { setText(null); setStyle(""); return; }
                setText(etat);
                switch (etat.toLowerCase()) {
                    case "acceptée"   -> setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                    case "refusée"    -> setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                    case "annulée"    -> setStyle("-fx-text-fill: #9E9E9E; -fx-font-weight: bold;");
                    case "en attente" -> setStyle("-fx-text-fill: #F7931E; -fx-font-weight: bold;");
                    default           -> setStyle("-fx-text-fill: #333;");
                }
            }
        });

        // ── CORRECTION : affiche le vrai commentaire ──
        commentaireCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCommentaire() != null
                                ? data.getValue().getCommentaire() : "—"));
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT
    // ══════════════════════════════════════════════════════

    private void loadData() {
        try {
            List<Reservation> list = service.readAll();
            observableList.setAll(list);
            tableView.setItems(observableList);
            updateStats();
        } catch (SQLException e) {
            showAlert("Erreur chargement : " + e.getMessage());
        }
    }

    private void updateStats() {
        totalLabel.setText(String.valueOf(observableList.size()));

        long enAttente = observableList.stream()
                .filter(r -> "En attente".equalsIgnoreCase(r.getEtat())).count();
        long acceptees = observableList.stream()
                .filter(r -> "Acceptée".equalsIgnoreCase(r.getEtat())).count();
        long refusees  = observableList.stream()
                .filter(r -> "Refusée".equalsIgnoreCase(r.getEtat())).count();
        long annulees  = observableList.stream()
                .filter(r -> "Annulée".equalsIgnoreCase(r.getEtat())).count();

        enAttenteLabel.setText(String.valueOf(enAttente));
        accepteesLabel.setText(String.valueOf(acceptees));
        refuseesLabel.setText(String.valueOf(refusees));
        annuleesLabel.setText(String.valueOf(annulees));
    }

    // ══════════════════════════════════════════════════════
    //  FILTRES, RECHERCHE & SÉLECTION
    // ══════════════════════════════════════════════════════

    private void configurerFiltres() {
        statutFilter.getItems().addAll("Tous", "En attente", "Acceptée", "Refusée", "Annulée");
        statutFilter.setValue("Tous");
        statutFilter.setOnAction(e -> appliquerFiltres());
    }

    private void configurerRecherche() {
        searchField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        String statut = statutFilter.getValue();
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();

        ObservableList<Reservation> filtered = observableList.filtered(r -> {
            boolean matchStatut = "Tous".equals(statut)
                    || (r.getEtat() != null && r.getEtat().equalsIgnoreCase(statut));

            boolean matchSearch = search.isEmpty();
            if (!matchSearch) {
                if (r.getId_personne() != null) {
                    String nom = ((r.getId_personne().getNom() != null ? r.getId_personne().getNom() : "") + " "
                            + (r.getId_personne().getPrenom() != null ? r.getId_personne().getPrenom() : "")).toLowerCase();
                    if (nom.contains(search)) matchSearch = true;
                }
                if (!matchSearch && r.getId_offre() != null && r.getId_offre().getType() != null
                        && r.getId_offre().getType().toLowerCase().contains(search)) {
                    matchSearch = true;
                }
                if (!matchSearch && r.getEtat() != null && r.getEtat().toLowerCase().contains(search)) {
                    matchSearch = true;
                }
                // ── AJOUT : recherche sur email et commentaire ──
                if (!matchSearch && r.getEmail() != null && r.getEmail().toLowerCase().contains(search)) {
                    matchSearch = true;
                }
                if (!matchSearch && r.getCommentaire() != null && r.getCommentaire().toLowerCase().contains(search)) {
                    matchSearch = true;
                }
            }
            return matchStatut && matchSearch;
        });

        tableView.setItems(filtered);
    }

    private void configurerSelection() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { if (newVal != null) showDetails(newVal); });
    }

    // ══════════════════════════════════════════════════════
    //  PANNEAU DÉTAILS
    // ══════════════════════════════════════════════════════

    private void showDetails(Reservation r) {
        detailsBox.getChildren().clear();

        Label titre = new Label("📋 Réservation #" + r.getId_reservation());
        titre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF6B35;");
        detailsBox.getChildren().addAll(titre, new Separator());

        // Client
        if (r.getId_personne() != null) {
            Personne p = r.getId_personne();
            detailsBox.getChildren().add(sectionTitre("👤 Client"));
            String nomComplet = ((p.getNom() != null ? p.getNom() : "") + " "
                    + (p.getPrenom() != null ? p.getPrenom() : "")).trim();
            detailsBox.getChildren().add(ligneDetail("Nom",
                    nomComplet.isBlank() ? "Client #" + p.getIdUtilisateur() : nomComplet));
            if (p.getEmail() != null)
                detailsBox.getChildren().add(ligneDetail("Email", p.getEmail()));
            detailsBox.getChildren().add(new Separator());
        }

        // Contact de la réservation
        if (r.getEmail() != null || r.getNum_tel() != null) {
            detailsBox.getChildren().add(sectionTitre("📞 Contact"));
            if (r.getEmail() != null)
                detailsBox.getChildren().add(ligneDetail("Email", r.getEmail()));
            if (r.getNum_tel() != null)
                detailsBox.getChildren().add(ligneDetail("Tél", r.getNum_tel()));
            detailsBox.getChildren().add(new Separator());
        }

        // Offre
        if (r.getId_offre() != null) {
            Offre o = r.getId_offre();
            detailsBox.getChildren().add(sectionTitre("🎫 Offre"));
            detailsBox.getChildren().add(ligneDetail("Type",
                    o.getType() != null ? o.getType() : "—"));
            detailsBox.getChildren().add(ligneDetail("Prix",
                    String.format("%.0f €", o.getPrix())));
            if (o.getDestination() != null) {
                Destination d = o.getDestination();
                detailsBox.getChildren().add(ligneDetail("Destination",
                        (d.getVille() != null ? d.getVille() : "") + ", "
                                + (d.getPays() != null ? d.getPays() : "")));
            }
            if (o.getHotel() != null && o.getHotel().getNom() != null)
                detailsBox.getChildren().add(ligneDetail("Hôtel", o.getHotel().getNom()));
            if (o.getVol() != null && o.getVol().getNumeroVol() != null)
                detailsBox.getChildren().add(ligneDetail("Vol", o.getVol().getNumeroVol()));
            if (o.getActivite() != null && o.getActivite().getNom() != null)
                detailsBox.getChildren().add(ligneDetail("Activité", o.getActivite().getNom()));
            detailsBox.getChildren().add(new Separator());
        }

        // Réservation
        detailsBox.getChildren().add(sectionTitre("📅 Réservation"));
        detailsBox.getChildren().add(ligneDetail("Date",
                r.getDate_reservation() != null ? r.getDate_reservation().toString() : "—"));
        detailsBox.getChildren().add(ligneDetail("Montant",
                String.format("%.0f €", r.getPrix_reservation())));
        detailsBox.getChildren().add(ligneDetail("Personnes",
                String.valueOf(r.getNombre_personnes())));
        if (r.getNum_passeport() != null)
            detailsBox.getChildren().add(ligneDetail("Passeport(s)", r.getNum_passeport()));
        if (r.getCommentaire() != null && !r.getCommentaire().isBlank())
            detailsBox.getChildren().add(ligneDetail("Commentaire", r.getCommentaire()));

        // Badge statut
        Label statutBadge = new Label(r.getEtat() != null ? r.getEtat() : "—");
        String style = switch (r.getEtat() != null ? r.getEtat().toLowerCase() : "") {
            case "acceptée"   -> "-fx-background-color:#E8F5E9; -fx-text-fill:#27AE60;";
            case "refusée"    -> "-fx-background-color:#FFEBEE; -fx-text-fill:#E74C3C;";
            case "annulée"    -> "-fx-background-color:#F5F5F5; -fx-text-fill:#9E9E9E;";
            case "en attente" -> "-fx-background-color:#FFF8E1; -fx-text-fill:#F7931E;";
            default           -> "-fx-background-color:#F0F0F0; -fx-text-fill:#333;";
        };
        statutBadge.setStyle(style +
                "-fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:3 10;");
        HBox statutRow = new HBox(8);
        statutRow.setAlignment(Pos.CENTER_LEFT);
        Label statutLbl = new Label("Statut :");
        statutLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#888;");
        statutRow.getChildren().addAll(statutLbl, statutBadge);
        detailsBox.getChildren().add(statutRow);

        // Voyage
        if (r.getId_voyage() != null) {
            Voyage v = r.getId_voyage();
            detailsBox.getChildren().addAll(new Separator(), sectionTitre("✈ Voyage"));
            detailsBox.getChildren().add(ligneDetail("ID", "#" + v.getIdVoyage()));
            if (v.getDateDebut() != null)
                detailsBox.getChildren().add(ligneDetail("Départ", v.getDateDebut().toString()));
            if (v.getDateFin() != null)
                detailsBox.getChildren().add(ligneDetail("Retour", v.getDateFin().toString()));
            if (v.getDuree() > 0)
                detailsBox.getChildren().add(ligneDetail("Durée", v.getDuree() + " jours"));
        }
    }

    // ══════════════════════════════════════════════════════
    //  ACTIONS ADMIN
    // ══════════════════════════════════════════════════════

    @FXML private void accepterReservation() { changeEtat("Acceptée"); }
    @FXML private void refuserReservation()  { changeEtat("Refusée");  }
    @FXML private void annulerReservation()  { changeEtat("Annulée");  }

    private void changeEtat(String nouvelEtat) {
        Reservation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠ Veuillez sélectionner une réservation.");
            return;
        }

        // Sauvegarde de l'ancien état pour décider si on envoie un email
        String ancienEtat = selected.getEtat();
        selected.setEtat(nouvelEtat);

        try {
            service.modifier(selected);

            // ── Envoi d'email uniquement pour Acceptée et Refusée ──
            if ("Acceptée".equals(nouvelEtat) || "Refusée".equals(nouvelEtat)) {
                String destinataire = resolveEmail(selected);
                if (destinataire != null && !destinataire.isBlank()) {
                    envoyerEmailStatut(selected, nouvelEtat, destinataire);
                }
            }

            loadData();
            showDetails(selected);
        } catch (SQLException e) {
            showAlert("Erreur : " + e.getMessage());
        }
    }

    /**
     * Résout l'adresse email à utiliser :
     *  1. email de la réservation (saisi lors de la commande)
     *  2. sinon email du compte Personne
     */
    private String resolveEmail(Reservation r) {
        if (r.getEmail() != null && !r.getEmail().isBlank())
            return r.getEmail();
        if (r.getId_personne() != null && r.getId_personne().getEmail() != null)
            return r.getId_personne().getEmail();
        return null;
    }

    // ══════════════════════════════════════════════════════
    //  ENVOI D'EMAIL
    // ══════════════════════════════════════════════════════

    /**
     * Envoie un email HTML à l'utilisateur pour lui notifier
     * que sa réservation a été acceptée ou refusée.
     */
    private void envoyerEmailStatut(Reservation r, String statut, String destinataire) {

        String prenomClient = "";
        if (r.getId_personne() != null && r.getId_personne().getPrenom() != null)
            prenomClient = r.getId_personne().getPrenom();

        String offreLibelle = "—";
        if (r.getId_offre() != null && r.getId_offre().getType() != null)
            offreLibelle = r.getId_offre().getType();

        boolean acceptee = "Acceptée".equals(statut);

        String sujet = acceptee
                ? "✅ Votre réservation TripEase a été acceptée !"
                : "❌ Votre réservation TripEase a été refusée";

        String couleurStatut = acceptee ? "#27AE60" : "#E74C3C";
        String emoji         = acceptee ? "✅" : "❌";
        String messageCorps  = acceptee
                ? "Nous avons le plaisir de vous confirmer que votre réservation a été <strong>acceptée</strong>. "
                + "Vous pouvez dès à présent préparer votre voyage !"
                : "Nous sommes au regret de vous informer que votre réservation a été <strong>refusée</strong>. "
                + "N'hésitez pas à nous contacter pour plus d'informations ou pour effectuer une nouvelle réservation.";

        String html = """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif;background:#f5f7fa;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f7fa;padding:40px 0;">
                    <tr><td align="center">
                      <table width="580" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:16px;overflow:hidden;
                                    box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                        <!-- HEADER -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#FF6B35,#F7931E);
                                     padding:36px 40px;text-align:center;">
                            <h1 style="margin:0;color:#fff;font-size:28px;letter-spacing:1px;">TripEase</h1>
                            <p style="margin:6px 0 0;color:rgba(255,255,255,.85);font-size:14px;">
                              Votre agence de voyage
                            </p>
                          </td>
                        </tr>

                        <!-- BODY -->
                        <tr>
                          <td style="padding:40px;">

                            <!-- Statut badge -->
                            <div style="text-align:center;margin-bottom:28px;">
                              <span style="display:inline-block;background:%s;color:#fff;
                                           font-size:16px;font-weight:bold;border-radius:30px;
                                           padding:10px 30px;">
                                %s %s
                              </span>
                            </div>

                            <p style="font-size:16px;color:#333;margin:0 0 16px;">
                              Bonjour <strong>%s</strong>,
                            </p>
                            <p style="font-size:15px;color:#555;line-height:1.7;margin:0 0 24px;">
                              %s
                            </p>

                            <!-- Récapitulatif -->
                            <table width="100%%" cellpadding="12" cellspacing="0"
                                   style="background:#FFF8F4;border-radius:10px;
                                          border:1px solid #FFD8C0;margin-bottom:28px;">
                              <tr>
                                <td style="color:#888;font-size:13px;width:140px;">N° Réservation</td>
                                <td style="color:#333;font-weight:bold;font-size:13px;">#%d</td>
                              </tr>
                              <tr style="border-top:1px solid #FFD8C0;">
                                <td style="color:#888;font-size:13px;">Offre</td>
                                <td style="color:#333;font-weight:bold;font-size:13px;">%s</td>
                              </tr>
                              <tr style="border-top:1px solid #FFD8C0;">
                                <td style="color:#888;font-size:13px;">Date de réservation</td>
                                <td style="color:#333;font-weight:bold;font-size:13px;">%s</td>
                              </tr>
                              <tr style="border-top:1px solid #FFD8C0;">
                                <td style="color:#888;font-size:13px;">Montant</td>
                                <td style="color:#333;font-weight:bold;font-size:13px;">%.0f €</td>
                              </tr>
                              <tr style="border-top:1px solid #FFD8C0;">
                                <td style="color:#888;font-size:13px;">Statut</td>
                                <td style="color:%s;font-weight:bold;font-size:13px;">%s %s</td>
                              </tr>
                            </table>

                            <p style="font-size:14px;color:#888;margin:0;">
                              Pour toute question, contactez-nous à
                              <a href="mailto:support@tripease.com" style="color:#FF6B35;">
                                support@tripease.com
                              </a>
                            </p>
                          </td>
                        </tr>

                        <!-- FOOTER -->
                        <tr>
                          <td style="background:#f9f9f9;padding:20px 40px;text-align:center;
                                     border-top:1px solid #eee;">
                            <p style="margin:0;font-size:12px;color:#bbb;">
                              © 2025 TripEase — Tous droits réservés
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                couleurStatut, emoji, statut,
                prenomClient.isBlank() ? "cher(e) client(e)" : prenomClient,
                messageCorps,
                r.getId_reservation(),
                offreLibelle,
                r.getDate_reservation() != null ? r.getDate_reservation().toString() : "—",
                r.getPrix_reservation(),
                couleurStatut, emoji, statut
        );

        // Envoi dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth",            "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host",            MAIL_HOST);
                props.put("mail.smtp.port",            MAIL_PORT);
                props.put("mail.smtp.ssl.trust",       MAIL_HOST);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MAIL_USERNAME, MAIL_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(MAIL_FROM));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(destinataire));
                message.setSubject(sujet);
                message.setContent(html, "text/html; charset=utf-8");

                Transport.send(message);
                System.out.println("[Email] Envoyé à " + destinataire + " — statut : " + statut);

            } catch (MessagingException ex) {
                System.err.println("[Email] Échec d'envoi : " + ex.getMessage());
                // On n'affiche pas d'alerte JavaFX depuis un thread secondaire directement ;
                // si besoin, utiliser Platform.runLater(...)
            }
        }, "email-thread").start();
    }

    @FXML
    private void refreshTable() {
        searchField.clear();
        statutFilter.setValue("Tous");
        loadData();
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS UI
    // ══════════════════════════════════════════════════════

    private Label sectionTitre(String texte) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size:11.5px; -fx-font-weight:bold; -fx-text-fill:#FF6B35; -fx-padding:4 0 2 0;");
        return lbl;
    }

    private HBox ligneDetail(String label, String valeur) {
        HBox hb = new HBox(6);
        hb.setAlignment(Pos.TOP_LEFT);
        hb.setPadding(new Insets(2, 0, 2, 0));
        Label lbl = new Label(label + " :");
        lbl.setMinWidth(72);
        lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#AAA;");
        Label val = new Label(valeur);
        val.setWrapText(true);
        val.setStyle("-fx-font-size:11.5px; -fx-text-fill:#333; -fx-font-weight:bold;");
        HBox.setHgrow(val, Priority.ALWAYS);
        hb.getChildren().addAll(lbl, val);
        return hb;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}