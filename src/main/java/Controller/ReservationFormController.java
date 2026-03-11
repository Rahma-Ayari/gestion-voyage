package Controller;

import Entite.*;
import Service.ServiceReservation;
import Service.ServiceStatutReservation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ReservationFormController implements Initializable {

    // ── FXML ──────────────────────────────────────────
    @FXML private StackPane        imagePane;
    @FXML private ImageView        offreImageView;
    @FXML private Label            imagePlaceholder;
    @FXML private Label            typeBadge;
    @FXML private Label            offreTitreLabel;
    @FXML private Label            offreDescLabel;
    @FXML private Label            destinationLabel;
    @FXML private Label            datesLabel;
    @FXML private Label            hotelLabel;
    @FXML private Label            volLabel;
    @FXML private Label            activiteLabel;
    @FXML private Label            prixUnitaireLabel;

    @FXML private Spinner<Integer> nbPersonnesSpinner;
    @FXML private DatePicker       datePicker;
    @FXML private TextField        emailField;
    @FXML private TextField        numTelField;

    /** Conteneur principal de la section passeports (cadre gris). */
    @FXML private VBox             passeportsContainer;
    /** Sous-VBox où sont injectés les champs TextField passeport. */
    @FXML private VBox             passeportFieldsBox;
    /** Label "— N personne(s)" à côté du titre de la section. */
    @FXML private Label            passeportCountLabel;

    @FXML private TextArea         commentaireArea;
    @FXML private Label            errorLabel;
    @FXML private Label            prixDetailLabel;
    @FXML private Label            totalLabel;
    @FXML private Button           confirmerBtn;

    // ── Services ──────────────────────────────────────
    private final ServiceReservation       serviceRes    = new ServiceReservation();
    private final ServiceStatutReservation serviceStatut = new ServiceStatutReservation();

    // ── Données ───────────────────────────────────────
    private Offre                   offreSelectionnee;
    private List<StatutReservation> statuts;

    /**
     * Liste des TextField passeport générés dynamiquement.
     * passeportFields.get(0) = Personne 1, get(1) = Personne 2, etc.
     */
    private final List<TextField> passeportFields = new ArrayList<>();

    // ── Patterns de validation ────────────────────────
    private static final DateTimeFormatter FMT              = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern           EMAIL_PATTERN    =
            Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern           TEL_PATTERN      =
            Pattern.compile("^[+]?[0-9 \\-().]{6,20}$");
    private static final Pattern           PASSPORT_PATTERN =
            Pattern.compile("^[A-Z0-9]{6,12}$");

    // ⚠️ Remplacez par votre système de session
    private static final int ID_UTILISATEUR_CONNECTE = 1;

    // ══════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        datePicker.setValue(LocalDate.now());
        chargerStatuts();
        styleFocusBorder(emailField);
        styleFocusBorder(numTelField);

        // Générer les champs passeport dès le départ (1 personne)
        genererChampsPasseport(1);
    }

    /** Appelé depuis ReservationUserController avant d'afficher cette page. */
    public void initOffre(Offre offre) {
        this.offreSelectionnee = offre;
        remplirResumeOffre(offre);
        mettreAJourTotal();

        // Écouter le spinner → recalculer le total ET régénérer les passeports
        nbPersonnesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            mettreAJourTotal();
            genererChampsPasseport(newVal);
        });
    }

    // ══════════════════════════════════════════════════
    //  GÉNÉRATION DYNAMIQUE DES CHAMPS PASSEPORT
    // ══════════════════════════════════════════════════

    /**
     * Vide passeportFieldsBox et recrée N champs TextField,
     * un par personne. Chaque rangée affiche :
     *   [👤 Personne X]  [TextField ____________]
     */
    private void genererChampsPasseport(int nb) {
        passeportFields.clear();
        passeportFieldsBox.getChildren().clear();

        // Mettre à jour le label de comptage
        passeportCountLabel.setText("— " + nb + " personne" + (nb > 1 ? "s" : ""));

        for (int i = 1; i <= nb; i++) {
            final int index = i;

            // Label personne
            Label lblPersonne = new Label("Personne " + i);
            lblPersonne.setMinWidth(80);
            lblPersonne.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #FF6B35;");

            // Champ passeport
            TextField tf = new TextField();
            tf.setPromptText("ex : AB" + (1000000 + i));
            tf.setStyle(
                    "-fx-font-size: 13px; -fx-background-radius: 8;" +
                            "-fx-border-color: #DDD; -fx-border-radius: 8; -fx-padding: 8 12;");
            HBox.setHgrow(tf, Priority.ALWAYS);

            // Convertir automatiquement en majuscules à la saisie
            tf.textProperty().addListener((obs, oldTxt, newTxt) -> {
                if (newTxt != null && !newTxt.equals(newTxt.toUpperCase())) {
                    tf.setText(newTxt.toUpperCase());
                }
            });

            styleFocusBorder(tf);

            // Badge numéro
            Label badge = new Label("🛂 " + index);
            badge.setStyle(
                    "-fx-font-size: 11px; -fx-text-fill: #888;" +
                            "-fx-background-color: #F0F0F0; -fx-background-radius: 6; -fx-padding: 4 8;");

            HBox row = new HBox(10, badge, lblPersonne, tf);
            row.setAlignment(Pos.CENTER_LEFT);

            passeportFieldsBox.getChildren().add(row);
            passeportFields.add(tf);
        }
    }

    // ══════════════════════════════════════════════════
    //  RÉSUMÉ OFFRE
    // ══════════════════════════════════════════════════

    private void remplirResumeOffre(Offre offre) {
        typeBadge.setText(offre.getType() != null ? offre.getType().toUpperCase() : "OFFRE");
        offreTitreLabel.setText(offre.getType() != null ? offre.getType() : "Offre de voyage");
        offreDescLabel.setText(offre.getDescription() != null ? offre.getDescription() : "");

        // Image
        Image img = chargerImage(offre.getImagePath(), 360, 180);
        if (img != null) {
            offreImageView.setImage(img);
            Rectangle clip = new Rectangle(360, 180);
            clip.setArcWidth(28);
            clip.setArcHeight(28);
            offreImageView.setClip(clip);
            offreImageView.setVisible(true);
            imagePlaceholder.setVisible(false);
        } else {
            offreImageView.setVisible(false);
            imagePlaceholder.setVisible(true);
        }

        // Destination
        if (offre.getDestination() != null) {
            Destination d = offre.getDestination();
            String dest = (d.getVille() != null ? d.getVille() : "")
                    + (d.getPays() != null ? (d.getVille() != null ? ", " : "") + d.getPays() : "");
            destinationLabel.setText(dest.isEmpty() ? "—" : dest);
        } else {
            destinationLabel.setText("—");
        }

        // Dates
        if (offre.getDateDebut() != null && offre.getDateFin() != null) {
            long jours = java.time.temporal.ChronoUnit.DAYS
                    .between(offre.getDateDebut(), offre.getDateFin());
            datesLabel.setText(offre.getDateDebut().format(FMT)
                    + " → " + offre.getDateFin().format(FMT)
                    + "  (" + jours + " jours)");
        } else if (offre.getDateDebut() != null) {
            datesLabel.setText("Dès le " + offre.getDateDebut().format(FMT));
        } else {
            datesLabel.setText("—");
        }

        // Hôtel
        if (offre.getHotel() != null && offre.getHotel().getNom() != null) {
            Hotel h = offre.getHotel();
            String etoiles = "★".repeat(Math.max(0, h.getStars()));
            hotelLabel.setText(h.getNom() + "  " + etoiles
                    + (h.getVille() != null ? "  — " + h.getVille() : ""));
        } else {
            hotelLabel.setText("—");
        }

        // Vol
        if (offre.getVol() != null) {
            Vol v = offre.getVol();
            String volTxt = (v.getCompagnie() != null ? v.getCompagnie() : "")
                    + (v.getNumeroVol() != null ? "  " + v.getNumeroVol() : "");
            volLabel.setText(volTxt.isBlank() ? "—" : volTxt.trim());
        } else {
            volLabel.setText("—");
        }

        // Activité
        if (offre.getActivite() != null && offre.getActivite().getNom() != null) {
            Activite a = offre.getActivite();
            activiteLabel.setText(a.getNom()
                    + (a.getCategorie() != null ? "  — " + a.getCategorie() : ""));
        } else {
            activiteLabel.setText("—");
        }

        prixUnitaireLabel.setText(String.format("%.0f €", offre.getPrix()));
    }

    // ══════════════════════════════════════════════════
    //  STATUTS
    // ══════════════════════════════════════════════════

    private void chargerStatuts() {
        try {
            statuts = serviceStatut.readAll();
        } catch (SQLException e) {
            statuts = null;
        }
    }

    private StatutReservation getStatutEnAttente() {
        if (statuts == null || statuts.isEmpty()) return null;
        return statuts.stream()
                .filter(s -> s.getLibelle() != null
                        && s.getLibelle().toLowerCase().contains("attente"))
                .findFirst()
                .orElse(statuts.get(0));
    }

    // ══════════════════════════════════════════════════
    //  TOTAL DYNAMIQUE
    // ══════════════════════════════════════════════════

    private void mettreAJourTotal() {
        if (offreSelectionnee == null) return;
        int    nb  = nbPersonnesSpinner.getValue();
        double pu  = offreSelectionnee.getPrix();
        double tot = pu * nb;
        prixDetailLabel.setText(String.format("%.0f € × %d personne(s)", pu, nb));
        totalLabel.setText(String.format("%.0f €", tot));
    }

    // ══════════════════════════════════════════════════
    //  CONFIRMER RÉSERVATION
    // ══════════════════════════════════════════════════

    @FXML
    private void confirmerReservation() {
        cacherErreur();

        if (offreSelectionnee == null) {
            afficherErreur("⚠ Aucune offre sélectionnée.");
            return;
        }
        if (datePicker.getValue() == null) {
            afficherErreur("⚠ Veuillez sélectionner une date de réservation.");
            return;
        }

        String email  = emailField.getText() != null ? emailField.getText().trim() : "";
        String numTel = numTelField.getText() != null ? numTelField.getText().trim() : "";

        if (email.isEmpty()) {
            afficherErreur("⚠ Veuillez saisir votre adresse e-mail.");
            emailField.requestFocus();
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            afficherErreur("⚠ Adresse e-mail invalide (ex : nom@domaine.com).");
            emailField.requestFocus();
            return;
        }
        if (numTel.isEmpty()) {
            afficherErreur("⚠ Veuillez saisir votre numéro de téléphone.");
            numTelField.requestFocus();
            return;
        }
        if (!TEL_PATTERN.matcher(numTel).matches()) {
            afficherErreur("⚠ Numéro de téléphone invalide (ex : +216 12 345 678).");
            numTelField.requestFocus();
            return;
        }

        // ── Validation de chaque passeport ────────────
        List<String> passeports = new ArrayList<>();
        for (int i = 0; i < passeportFields.size(); i++) {
            String val = passeportFields.get(i).getText() != null
                    ? passeportFields.get(i).getText().trim().toUpperCase() : "";

            if (val.isEmpty()) {
                afficherErreur("⚠ Veuillez saisir le numéro de passeport de la Personne " + (i + 1) + ".");
                passeportFields.get(i).requestFocus();
                return;
            }
            if (!PASSPORT_PATTERN.matcher(val).matches()) {
                afficherErreur("⚠ Passeport invalide pour la Personne " + (i + 1)
                        + " — 6 à 12 caractères alphanumériques (ex : AB1234567).");
                passeportFields.get(i).requestFocus();
                return;
            }
            passeports.add(val);
        }

        // ── Statut ────────────────────────────────────
        StatutReservation statutEnAttente = getStatutEnAttente();
        if (statutEnAttente == null) {
            afficherErreur("⚠ Impossible de trouver le statut 'En attente' en base de données.");
            return;
        }

        try {
            Reservation reservation = new Reservation();

            reservation.setDate_reservation(Date.valueOf(datePicker.getValue()));

            double prixTotal = offreSelectionnee.getPrix() * nbPersonnesSpinner.getValue();
            reservation.setPrix_reservation(prixTotal);
            reservation.setEtat("En attente");
            reservation.setEmail(email);
            reservation.setNum_tel(numTel);
            reservation.setNombre_personnes(nbPersonnesSpinner.getValue());
            reservation.setCommentaire(
                    commentaireArea.getText() != null ? commentaireArea.getText().trim() : "");

            // Passeports joints : "AB1234567 | CD9876543 | EF1122334"
            reservation.setNum_passeport(String.join(" | ", passeports));

            reservation.setId_voyage(null);
            reservation.setId_statut(statutEnAttente);
            reservation.setId_offre(offreSelectionnee);

            boolean ok = serviceRes.ajouter(reservation);
            if (ok) {
                afficherSucces(prixTotal, passeports);
            } else {
                afficherErreur("❌ Erreur lors de l'enregistrement. Veuillez réessayer.");
            }

        } catch (SQLException ex) {
            afficherErreur("❌ Erreur SQL : " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    //  POPUP SUCCÈS
    // ══════════════════════════════════════════════════

    private void afficherSucces(double prixTotal, List<String> passeports) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Réservation confirmée !");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: white;");

        Label check = new Label("✅");
        check.setStyle("-fx-font-size: 64px;");

        Label titre = new Label("Réservation confirmée !");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #27AE60;");

        Label sous = new Label("Votre réservation est en attente de validation.");
        sous.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");

        VBox details = new VBox(0);
        details.setStyle(
                "-fx-background-color: #F9F9F9; -fx-background-radius: 12;" +
                        "-fx-border-color: #ECECEC; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 4 16;");

        details.getChildren().addAll(
                infoLigne("🎫 Offre",      offreSelectionnee.getType() != null ? offreSelectionnee.getType() : "—"),
                new Separator(),
                infoLigne("📅 Date",       datePicker.getValue().format(FMT)),
                new Separator(),
                infoLigne("👥 Personnes",  nbPersonnesSpinner.getValue() + " personne(s)"),
                new Separator(),
                infoLigne("✉ E-mail",      emailField.getText().trim()),
                new Separator(),
                infoLigne("📞 Téléphone",  numTelField.getText().trim()),
                new Separator()
        );

        // Afficher chaque passeport individuellement dans le récap
        for (int i = 0; i < passeports.size(); i++) {
            details.getChildren().add(
                    infoLigne("🛂 Passeport P" + (i + 1), passeports.get(i)));
            details.getChildren().add(new Separator());
        }

        details.getChildren().addAll(
                infoLigne("💰 Total payé", String.format("%.0f €", prixTotal)),
                new Separator(),
                infoLigne("📌 Statut",     "⏳ En attente")
        );

        if (offreSelectionnee.getDestination() != null) {
            Destination d = offreSelectionnee.getDestination();
            String dest = (d.getVille() != null ? d.getVille() : "")
                    + (d.getPays() != null ? ", " + d.getPays() : "");
            details.getChildren().addAll(new Separator(), infoLigne("📍 Destination", dest));
        }

        String comm = commentaireArea.getText() != null ? commentaireArea.getText().trim() : "";
        if (!comm.isEmpty()) {
            details.getChildren().addAll(new Separator(), infoLigne("💬 Commentaire", comm));
        }

        Button btnOk = new Button("🏠  Retour aux offres");
        btnOk.setStyle(
                "-fx-background-color: linear-gradient(to right, #FF6B35, #F7931E);" +
                        "-fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;" +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 30;");
        btnOk.setOnAction(e -> { dialog.close(); retourOffres(); });

        ScrollPane scroll = new ScrollPane(details);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setPrefHeight(320);

        root.getChildren().addAll(check, titre, sous, scroll, btnOk);
        dialog.setScene(new Scene(root, 480, 580));
        dialog.showAndWait();
    }

    private HBox infoLigne(String label, String valeur) {
        HBox hb = new HBox(12);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setPadding(new Insets(10, 0, 10, 0));
        Label lbl = new Label(label);
        lbl.setMinWidth(140);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-font-weight: bold;");
        Label val = new Label(valeur);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C2C2C; -fx-font-weight: bold;");
        hb.getChildren().addAll(lbl, val);
        return hb;
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION RETOUR
    // ══════════════════════════════════════════════════

    @FXML
    public void retourOffres() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ReservationUser/ReservationUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) confirmerBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    private void afficherErreur(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void cacherErreur() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /** Bordure orange au focus, grise sinon. */
    private void styleFocusBorder(TextField tf) {
        String base = "-fx-font-size: 13px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 12;";
        tf.focusedProperty().addListener((obs, wasFocused, isFocused) ->
                tf.setStyle(base + (isFocused
                        ? "-fx-border-color: #FF6B35; -fx-border-width: 1.5;"
                        : "-fx-border-color: #DDD; -fx-border-width: 1;")));
    }

    private Image chargerImage(String imagePath, double w, double h) {
        if (imagePath == null || imagePath.trim().isEmpty()) return null;
        try {
            File f = new File(imagePath.replace("\\", "/"));
            if (f.exists()) return new Image(f.toURI().toString(), w, h, false, true);
            URL res = getClass().getResource("/images/" + f.getName());
            if (res != null) return new Image(res.toExternalForm(), w, h, false, true);
        } catch (Exception ignored) {}
        return null;
    }
}