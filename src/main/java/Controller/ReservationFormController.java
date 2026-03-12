package Controller;

import Entite.*;
import Service.ServiceReservation;
import Service.ServiceStatutReservation;
import Utils.SessionManager;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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
    @FXML private WebView          hotelMapView;
    @FXML private Label            hotelAdresseLabel;
    @FXML private Button           agrandirCarteBtn;

    @FXML private Spinner<Integer> nbPersonnesSpinner;
    @FXML private DatePicker       datePicker;
    @FXML private TextField        emailField;
    @FXML private TextField        numTelField;
    @FXML private VBox             passeportsContainer;
    @FXML private VBox             passeportFieldsBox;
    @FXML private Label            passeportCountLabel;
    @FXML private TextArea         commentaireArea;
    @FXML private Label            errorLabel;
    @FXML private Label            prixDetailLabel;
    @FXML private Label            totalLabel;
    @FXML private Button           confirmerBtn;
    @FXML private Label            labelEmailConnecte;

    private final ServiceReservation       serviceRes    = new ServiceReservation();
    private final ServiceStatutReservation serviceStatut = new ServiceStatutReservation();

    private Offre                   offreSelectionnee;
    private List<StatutReservation> statuts;
    private final List<TextField>   passeportFields = new ArrayList<>();

    // Coordonnées courantes de l'hôtel affiché
    private double currentLat = 36.8189;
    private double currentLng = 10.1658;
    private String currentNom = "Hôtel";
    private String currentAdr = "";
    private String currentVille = "";

    private static final DateTimeFormatter FMT              = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern           EMAIL_PATTERN    = Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern           TEL_PATTERN      = Pattern.compile("^[+]?[0-9 \\-().]{6,20}$");
    private static final Pattern           PASSPORT_PATTERN = Pattern.compile("^[A-Z0-9]{6,12}$");

    // ══════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        datePicker.setValue(LocalDate.now());
        chargerStatuts();
        styleFocusBorder(emailField);
        styleFocusBorder(numTelField);
        genererChampsPasseport(1);

        String emailSession = SessionManager.getUserEmail();
        if (emailSession != null) {
            emailField.setText(emailSession);
            labelEmailConnecte.setText("👤 " + emailSession);
        }

        // Style bouton Agrandir
        if (agrandirCarteBtn != null) {
            agrandirCarteBtn.setStyle(
                    "-fx-background-color: rgba(255,107,53,0.92);" +
                            "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 12;");
            agrandirCarteBtn.setText("🔍 Agrandir");
            agrandirCarteBtn.setOnAction(e -> ouvrirPopupCarte());
        }
    }

    public void initOffre(Offre offre) {
        this.offreSelectionnee = offre;
        remplirResumeOffre(offre);
        mettreAJourTotal();
        nbPersonnesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            mettreAJourTotal();
            genererChampsPasseport(newVal);
        });
    }

    // ══════════════════════════════════════════════════
    //  RÉSUMÉ OFFRE
    // ══════════════════════════════════════════════════

    private void remplirResumeOffre(Offre offre) {
        typeBadge.setText(offre.getType() != null ? offre.getType().toUpperCase() : "OFFRE");
        offreTitreLabel.setText(offre.getType() != null ? offre.getType() : "Offre de voyage");
        offreDescLabel.setText(offre.getDescription() != null ? offre.getDescription() : "");

        Image img = chargerImage(offre.getImagePath(), 360, 180);
        if (img != null) {
            offreImageView.setImage(img);
            Rectangle clip = new Rectangle(360, 180);
            clip.setArcWidth(28); clip.setArcHeight(28);
            offreImageView.setClip(clip);
            offreImageView.setVisible(true);
            imagePlaceholder.setVisible(false);
        } else {
            offreImageView.setVisible(false);
            imagePlaceholder.setVisible(true);
        }

        if (offre.getDestination() != null) {
            Destination d = offre.getDestination();
            String dest = (d.getVille() != null ? d.getVille() : "")
                    + (d.getPays() != null ? (d.getVille() != null ? ", " : "") + d.getPays() : "");
            destinationLabel.setText(dest.isEmpty() ? "—" : dest);
        } else { destinationLabel.setText("—"); }

        if (offre.getDateDebut() != null && offre.getDateFin() != null) {
            long j = java.time.temporal.ChronoUnit.DAYS.between(offre.getDateDebut(), offre.getDateFin());
            datesLabel.setText(offre.getDateDebut().format(FMT) + " → " + offre.getDateFin().format(FMT) + "  (" + j + " jours)");
        } else if (offre.getDateDebut() != null) {
            datesLabel.setText("Dès le " + offre.getDateDebut().format(FMT));
        } else { datesLabel.setText("—"); }

        if (offre.getHotel() != null && offre.getHotel().getNom() != null) {
            Hotel h = offre.getHotel();
            hotelLabel.setText(h.getNom() + "  " + "★".repeat(Math.max(0, h.getStars()))
                    + (h.getVille() != null ? "  — " + h.getVille() : ""));
        } else { hotelLabel.setText("—"); }

        if (offre.getVol() != null) {
            Vol v = offre.getVol();
            String t = (v.getCompagnie() != null ? v.getCompagnie() : "")
                    + (v.getNumeroVol() != null ? "  " + v.getNumeroVol() : "");
            volLabel.setText(t.isBlank() ? "—" : t.trim());
        } else { volLabel.setText("—"); }

        if (offre.getActivite() != null && offre.getActivite().getNom() != null) {
            Activite a = offre.getActivite();
            activiteLabel.setText(a.getNom() + (a.getCategorie() != null ? "  — " + a.getCategorie() : ""));
        } else { activiteLabel.setText("—"); }

        prixUnitaireLabel.setText(String.format("%.0f €", offre.getPrix()));

        chargerCarteHotel(offre.getHotel());
    }

    // ══════════════════════════════════════════════════
    //  CARTE INLINE 200px (Leaflet.js via CDN)
    // ══════════════════════════════════════════════════

    private void chargerCarteHotel(Hotel hotel) {
        if (hotelMapView == null) return;

        WebEngine engine = hotelMapView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Si aucun hôtel, afficher carte vide
        if (hotel == null) {
            engine.loadContent(buildCarteVide());
            if (hotelAdresseLabel != null) hotelAdresseLabel.setText("—");
            return;
        }

        currentNom   = hotel.getNom()     != null ? hotel.getNom()     : "Hôtel";
        currentAdr   = hotel.getAdresse() != null ? hotel.getAdresse() : "";
        currentVille = hotel.getVille()   != null ? hotel.getVille()   : "";
        currentLat   = (hotel.getLatitude()  == 0.0) ? 36.8189 : hotel.getLatitude();
        currentLng   = (hotel.getLongitude() == 0.0) ? 10.1658 : hotel.getLongitude();

        if (hotelAdresseLabel != null) {
            String full = currentAdr.isEmpty() ? currentVille
                    : currentAdr + (currentVille.isEmpty() ? "" : ", " + currentVille);
            hotelAdresseLabel.setText(full.isEmpty() ? currentNom : full);
        }

        // Utilisation de Platform.runLater pour garantir le rendu WebView
        javafx.application.Platform.runLater(() -> {
            engine.loadContent(buildLeafletHtml(currentLat, currentLng, currentNom, currentAdr, currentVille, 15));
        });
    }

    // ══════════════════════════════════════════════════
    //  POPUP 800×600 avec grande carte Leaflet
    // ══════════════════════════════════════════════════

    @FXML
    private void ouvrirPopupCarte() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("📍 " + currentNom);
        popup.setResizable(true);

        // WebView grande carte
        WebView bigMap = new WebView();
        bigMap.setMinSize(800, 520);
        bigMap.setPrefSize(800, 520);
        WebEngine engine = bigMap.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.loadContent(buildLeafletHtml(currentLat, currentLng, currentNom, currentAdr, currentVille, 16));

        // Barre infos en bas
        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle("-fx-background-color: #FF6B35;");

        Label nomLbl = new Label("📍 " + currentNom);
        nomLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        String adrTxt = currentAdr.isEmpty() ? currentVille
                : currentAdr + (currentVille.isEmpty() ? "" : ", " + currentVille);
        Label adrLbl = new Label(adrTxt);
        adrLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button fermerBtn = new Button("✕  Fermer");
        fermerBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.25);" +
                        "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 16;");
        fermerBtn.setOnAction(e -> popup.close());

        footer.getChildren().addAll(nomLbl, adrLbl, spacer, fermerBtn);

        VBox root = new VBox(bigMap, footer);
        VBox.setVgrow(bigMap, Priority.ALWAYS);

        popup.setScene(new Scene(root, 800, 600));
        popup.show();
    }

    // ══════════════════════════════════════════════════
    //  HTML LEAFLET (réutilisé inline + popup)
    // ══════════════════════════════════════════════════

    private String buildLeafletHtml(double lat, double lng,
                                    String nom, String adresse, String ville, int zoom) {
        String nomJ  = nom.replace("'", "\\'").replace("\"", "&quot;");
        String adrJ  = adresse.replace("'", "\\'");
        String vilJ  = ville.replace("'", "\\'");
        String popup = nomJ + (adrJ.isEmpty() ? "" : "<br><small>" + adrJ
                + (vilJ.isEmpty() ? "" : ", " + vilJ) + "</small>");

        return "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'/>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "* { margin:0; padding:0; box-sizing:border-box; }" +
                "html,body,#map { width:100%; height:100%; }" +
                "</style></head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', {" +
                "  zoomControl: true," +
                "  scrollWheelZoom: false" +
                "}).setView([" + lat + "," + lng + "], " + zoom + ");" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "  attribution: '© OpenStreetMap contributors'," +
                "  maxZoom: 19" +
                "}).addTo(map);" +
                // Marqueur orange custom
                "var icon = L.divIcon({" +
                "  className: ''," +
                "  html: '<div style=\"width:32px;height:32px;background:#FF6B35;border:3px solid white;" +
                "border-radius:50% 50% 50% 0;transform:rotate(-45deg);box-shadow:0 2px 6px rgba(0,0,0,0.3)\">" +
                "<div style=\\\"transform:rotate(45deg);text-align:center;line-height:26px;font-size:14px;\\\">🏨</div>" +
                "</div>'," +
                "  iconSize: [32,32]," +
                "  iconAnchor: [16,32]," +
                "  popupAnchor: [0,-32]" +
                "});" +
                "L.marker([" + lat + "," + lng + "], {icon: icon})" +
                ".addTo(map)" +
                ".bindPopup('" + popup + "', {maxWidth: 220})" +
                ".openPopup();" +
                "</script>" +
                "</body></html>";
    }

    private String buildCarteVide() {
        return "<!DOCTYPE html><html><head><style>" +
                "* {margin:0;padding:0;}" +
                "body {width:100%;height:100%;display:flex;align-items:center;" +
                "justify-content:center;background:#FFF8F5;" +
                "font-family:Arial,sans-serif;flex-direction:column;gap:8px;}" +
                "</style></head><body>" +
                "<div style='font-size:32px'>🏨</div>" +
                "<div style='font-size:12px;color:#FF6B35;font-weight:bold;'>Aucun hôtel associé</div>" +
                "<div style='font-size:10px;color:#BBB;'>à cette offre</div>" +
                "</body></html>";
    }

    // ══════════════════════════════════════════════════
    //  PASSEPORTS
    // ══════════════════════════════════════════════════

    private void genererChampsPasseport(int nb) {
        passeportFields.clear();
        passeportFieldsBox.getChildren().clear();
        passeportCountLabel.setText("— " + nb + " personne" + (nb > 1 ? "s" : ""));
        for (int i = 1; i <= nb; i++) {
            final int idx = i;
            Label lbl = new Label("Personne " + i);
            lbl.setMinWidth(80);
            lbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#FF6B35;");
            TextField tf = new TextField();
            tf.setPromptText("ex : AB" + (1000000 + i));
            tf.setStyle("-fx-font-size:13px;-fx-background-radius:8;" +
                    "-fx-border-color:#DDD;-fx-border-radius:8;-fx-padding:8 12;");
            HBox.setHgrow(tf, Priority.ALWAYS);
            tf.textProperty().addListener((o, ov, nv) -> {
                if (nv != null && !nv.equals(nv.toUpperCase())) tf.setText(nv.toUpperCase());
            });
            styleFocusBorder(tf);
            Label badge = new Label("🛂 " + idx);
            badge.setStyle("-fx-font-size:11px;-fx-text-fill:#888;" +
                    "-fx-background-color:#F0F0F0;-fx-background-radius:6;-fx-padding:4 8;");
            HBox row = new HBox(10, badge, lbl, tf);
            row.setAlignment(Pos.CENTER_LEFT);
            passeportFieldsBox.getChildren().add(row);
            passeportFields.add(tf);
        }
    }

    // ══════════════════════════════════════════════════
    //  STATUTS
    // ══════════════════════════════════════════════════

    private void chargerStatuts() {
        try { statuts = serviceStatut.readAll(); } catch (SQLException e) { statuts = null; }
    }

    private StatutReservation getStatutEnAttente() {
        if (statuts == null || statuts.isEmpty()) return null;
        return statuts.stream()
                .filter(s -> s.getLibelle() != null && s.getLibelle().toLowerCase().contains("attente"))
                .findFirst().orElse(statuts.get(0));
    }

    // ══════════════════════════════════════════════════
    //  TOTAL
    // ══════════════════════════════════════════════════

    private void mettreAJourTotal() {
        if (offreSelectionnee == null) return;
        int nb = nbPersonnesSpinner.getValue();
        double pu = offreSelectionnee.getPrix();
        prixDetailLabel.setText(String.format("%.0f € × %d personne(s)", pu, nb));
        totalLabel.setText(String.format("%.0f €", pu * nb));
    }

    // ══════════════════════════════════════════════════
    //  CONFIRMER RÉSERVATION
    // ══════════════════════════════════════════════════

    @FXML
    private void confirmerReservation() {
        cacherErreur();
        if (offreSelectionnee == null) { afficherErreur("⚠ Aucune offre sélectionnée."); return; }
        if (datePicker.getValue() == null) { afficherErreur("⚠ Veuillez sélectionner une date."); return; }

        String email  = emailField.getText()  != null ? emailField.getText().trim()  : "";
        String numTel = numTelField.getText() != null ? numTelField.getText().trim() : "";

        if (email.isEmpty())                         { afficherErreur("⚠ Veuillez saisir votre e-mail."); emailField.requestFocus(); return; }
        if (!EMAIL_PATTERN.matcher(email).matches()) { afficherErreur("⚠ E-mail invalide."); emailField.requestFocus(); return; }
        if (numTel.isEmpty())                        { afficherErreur("⚠ Veuillez saisir votre téléphone."); numTelField.requestFocus(); return; }
        if (!TEL_PATTERN.matcher(numTel).matches())  { afficherErreur("⚠ Téléphone invalide."); numTelField.requestFocus(); return; }

        List<String> passeports = new ArrayList<>();
        for (int i = 0; i < passeportFields.size(); i++) {
            String val = passeportFields.get(i).getText() != null
                    ? passeportFields.get(i).getText().trim().toUpperCase() : "";
            if (val.isEmpty())                            { afficherErreur("⚠ Passeport manquant — Personne " + (i+1)); passeportFields.get(i).requestFocus(); return; }
            if (!PASSPORT_PATTERN.matcher(val).matches()) { afficherErreur("⚠ Passeport invalide — Personne " + (i+1)); passeportFields.get(i).requestFocus(); return; }
            passeports.add(val);
        }

        StatutReservation statut = getStatutEnAttente();
        if (statut == null) { afficherErreur("⚠ Statut 'En attente' introuvable."); return; }

        try {
            Reservation r = new Reservation();
            r.setDate_reservation(Date.valueOf(datePicker.getValue()));
            double total = offreSelectionnee.getPrix() * nbPersonnesSpinner.getValue();
            r.setPrix_reservation(total);
            r.setEtat("En attente");
            r.setEmail(email);
            r.setNum_tel(numTel);
            r.setNombre_personnes(nbPersonnesSpinner.getValue());
            r.setCommentaire(commentaireArea.getText() != null ? commentaireArea.getText().trim() : "");
            r.setNum_passeport(String.join(" | ", passeports));

            Utilisateur u = SessionManager.getCurrentUser();
            if (u instanceof Personne) r.setId_personne((Personne) u);

            r.setId_voyage(null);
            r.setId_statut(statut);
            r.setId_offre(offreSelectionnee);

            if (serviceRes.ajouter(r)) afficherSucces(total, passeports);
            else afficherErreur("❌ Erreur lors de l'enregistrement.");
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

        Label check = new Label("✅"); check.setStyle("-fx-font-size:64px;");
        Label titre = new Label("Réservation confirmée !"); titre.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#27AE60;");
        Label sous  = new Label("Votre réservation est en attente de validation."); sous.setStyle("-fx-font-size:13px;-fx-text-fill:#888;");

        VBox details = new VBox(0);
        details.setStyle("-fx-background-color:#F9F9F9;-fx-background-radius:12;" +
                "-fx-border-color:#ECECEC;-fx-border-radius:12;-fx-border-width:1;-fx-padding:4 16;");
        details.getChildren().addAll(
                infoLigne("🎫 Offre",     offreSelectionnee.getType() != null ? offreSelectionnee.getType() : "—"),
                new Separator(), infoLigne("📅 Date",      datePicker.getValue().format(FMT)),
                new Separator(), infoLigne("👥 Personnes", nbPersonnesSpinner.getValue() + " personne(s)"),
                new Separator(), infoLigne("✉ E-mail",     emailField.getText().trim()),
                new Separator(), infoLigne("📞 Téléphone", numTelField.getText().trim()),
                new Separator());
        for (int i = 0; i < passeports.size(); i++) {
            details.getChildren().add(infoLigne("🛂 Passeport P" + (i+1), passeports.get(i)));
            details.getChildren().add(new Separator());
        }
        details.getChildren().addAll(
                infoLigne("💰 Total payé", String.format("%.0f €", prixTotal)),
                new Separator(), infoLigne("📌 Statut", "⏳ En attente"));

        Utilisateur u = SessionManager.getCurrentUser();
        if (u != null) details.getChildren().addAll(new Separator(), infoLigne("👤 Compte", u.getEmail()));

        if (offreSelectionnee.getDestination() != null) {
            Destination d = offreSelectionnee.getDestination();
            details.getChildren().addAll(new Separator(), infoLigne("📍 Destination",
                    (d.getVille() != null ? d.getVille() : "") + (d.getPays() != null ? ", " + d.getPays() : "")));
        }
        String comm = commentaireArea.getText() != null ? commentaireArea.getText().trim() : "";
        if (!comm.isEmpty()) details.getChildren().addAll(new Separator(), infoLigne("💬 Commentaire", comm));

        Button btnOk = new Button("🏠  Retour aux offres");
        btnOk.setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);" +
                "-fx-text-fill:white;-fx-background-radius:10;-fx-cursor:hand;" +
                "-fx-font-size:14px;-fx-font-weight:bold;-fx-padding:12 30;");
        btnOk.setOnAction(e -> { dialog.close(); retourOffres(); });

        ScrollPane scroll = new ScrollPane(details);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setPrefHeight(320);

        root.getChildren().addAll(check, titre, sous, scroll, btnOk);
        dialog.setScene(new Scene(root, 480, 580));
        dialog.showAndWait();
    }

    private HBox infoLigne(String label, String valeur) {
        HBox hb = new HBox(12);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setPadding(new Insets(10, 0, 10, 0));
        Label l = new Label(label); l.setMinWidth(140); l.setStyle("-fx-font-size:12px;-fx-text-fill:#999;-fx-font-weight:bold;");
        Label v = new Label(valeur); v.setStyle("-fx-font-size:13px;-fx-text-fill:#2C2C2C;-fx-font-weight:bold;");
        hb.getChildren().addAll(l, v);
        return hb;
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @FXML
    public void retourOffres() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationUser/ReservationUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) confirmerBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    private void afficherErreur(String msg) { errorLabel.setText(msg); errorLabel.setVisible(true); errorLabel.setManaged(true); }
    private void cacherErreur()             { errorLabel.setVisible(false); errorLabel.setManaged(false); }

    private void styleFocusBorder(TextField tf) {
        String base = "-fx-font-size:13px;-fx-background-radius:8;-fx-border-radius:8;-fx-padding:8 12;";
        tf.focusedProperty().addListener((obs, wf, f) ->
                tf.setStyle(base + (f ? "-fx-border-color:#FF6B35;-fx-border-width:1.5;"
                        : "-fx-border-color:#DDD;-fx-border-width:1;")));
    }

    private Image chargerImage(String path, double w, double h) {
        if (path == null || path.trim().isEmpty()) return null;
        try {
            File f = new File(path.replace("\\", "/"));
            if (f.exists()) return new Image(f.toURI().toString(), w, h, false, true);
            URL res = getClass().getResource("/images/" + f.getName());
            if (res != null) return new Image(res.toExternalForm(), w, h, false, true);
        } catch (Exception ignored) {}
        return null;
    }
}