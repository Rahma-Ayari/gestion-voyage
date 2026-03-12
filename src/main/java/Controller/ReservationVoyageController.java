package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import Entite.Destination;
import Entite.Paiement;
import Entite.Personne;
import Entite.Reservation;
import Entite.StatutReservation;
import Entite.TypePaiement;
import Entite.Voyage;
import Service.ServicePaiement;
import Service.ServiceReservation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ReservationVoyageController {

    // ══════════════════════════════════════════════════
    //  IDs — adapter selon votre BDD
    // ══════════════════════════════════════════════════
    private static final int    ID_TYPE_CARTE            = 1;
    private static final int    ID_TYPE_VIREMENT         = 2;
    private static final int    ID_TYPE_ESPECES          = 3;
    private static final int    ID_STATUT_EN_ATTENTE     = 1;
    private static final int    ID_PERSONNE_CONNECTE     = 1;
    private static final String ETAT_RESERVATION         = "En attente";
    private static final String STATUT_PAIEMENT_OK       = "Payé";
    private static final String STATUT_PAIEMENT_ATTENTE  = "En attente";

    // ── Header ──
    @FXML private Label headerDestLabel;
    @FXML private Label headerDatesLabel;

    // ── Bandeau info (comme Budget) ──
    @FXML private Label destinationLabel;
    @FXML private Label datesLabel;
    @FXML private Label dureeLabel;

    // ── Recap voyage (colonne droite) ──
    @FXML private Label recapDestLabel;
    @FXML private Label recapDepartLabel;
    @FXML private Label recapRetourLabel;
    @FXML private Label recapDureeLabel;

    // ── Formulaire voyageur ──
    @FXML private TextField        nomField;
    @FXML private TextField        prenomField;
    @FXML private TextField        emailField;
    @FXML private TextField        telephoneField;
    @FXML private TextField        passportField;
    @FXML private ComboBox<String> nationaliteCombo;
    @FXML private Spinner<Integer> nbPersonnesSpinner;
    @FXML private TextArea         commentaireArea;

    // ── Paiement ──
    @FXML private ToggleButton  btnCarte;
    @FXML private ToggleButton  btnVirement;
    @FXML private ToggleButton  btnEspeces;
    @FXML private VBox          panelCarte;
    @FXML private Label         paiementInfoLabel;
    @FXML private TextField     numCarteField;
    @FXML private TextField     nomCarteField;
    @FXML private TextField     expirationField;
    @FXML private PasswordField cvvField;
    @FXML private Label         carteTypeLabel;
    @FXML private Label         carteStatutLabel;

    // ── Résumé commande ──
    @FXML private Label  cmdVolLabel;
    @FXML private Label  cmdHotelLabel;
    @FXML private Label  cmdActivitesLabel;
    @FXML private Label  cmdServicesLabel;
    @FXML private Label  cmdTotalLabel;
    @FXML private Label  cmdParJourLabel;
    @FXML private Button confirmerButton;

    // ── Données injectées ──
    private Destination destination;
    private LocalDate   dateDebut;
    private LocalDate   dateFin;
    private int         idVoyage;
    private double      totalVol;
    private double      totalHotel;
    private double      totalActivites;
    private double      totalServices;
    private double      totalGlobal;

    private String  modePaiement     = "CARTE";
    private boolean enCoursDeFormat  = false;

    private final DateTimeFormatter  fmt                = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ServiceReservation serviceReservation = new ServiceReservation();
    private final ServicePaiement    servicePaiement    = new ServicePaiement();

    // ══════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════

    @FXML
    public void initialize() {
        // Nationalités
        if (nationaliteCombo != null) {
            nationaliteCombo.getItems().addAll(
                    "Tunisienne", "Française", "Algérienne", "Marocaine",
                    "Allemande", "Espagnole", "Italienne", "Britannique",
                    "Américaine", "Canadienne", "Autre");
            nationaliteCombo.getSelectionModel().selectFirst();
        }

        // Spinner
        if (nbPersonnesSpinner != null)
            nbPersonnesSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));

        // Paiement par défaut : carte
        if (btnCarte != null) { btnCarte.setSelected(true); afficherPanelCarte(); }

        // ── Listener numéro carte : auto-format + Luhn ──
        if (numCarteField != null) {
            numCarteField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (enCoursDeFormat) return;
                String digits = newVal.replaceAll("[^0-9]", "");
                if (digits.length() > 16) digits = digits.substring(0, 16);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) sb.append(" ");
                    sb.append(digits.charAt(i));
                }
                String result = sb.toString();
                if (!result.equals(newVal)) {
                    enCoursDeFormat = true;
                    Platform.runLater(() -> {
                        try {
                            numCarteField.setText(result);
                            numCarteField.positionCaret(Math.max(0, Math.min(result.length(), numCarteField.getLength())));
                        } finally { enCoursDeFormat = false; }
                    });
                }
                detecterTypeCarteEtValider(digits);
            });
        }

        // ── Listener expiration : auto MM/AA ──
        if (expirationField != null) {
            expirationField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (enCoursDeFormat) return;
                String digits = newVal.replaceAll("[^0-9]", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);
                String result = digits.length() > 2
                        ? digits.substring(0, 2) + "/" + digits.substring(2) : digits;
                if (!result.equals(newVal)) {
                    enCoursDeFormat = true;
                    final String r = result;
                    Platform.runLater(() -> {
                        try {
                            expirationField.setText(r);
                            expirationField.positionCaret(Math.max(0, Math.min(r.length(), expirationField.getLength())));
                        } finally { enCoursDeFormat = false; }
                    });
                }
            });
        }

        // ── Listener CVV : chiffres seulement ──
        if (cvvField != null) {
            cvvField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (enCoursDeFormat) return;
                String digits = newVal.replaceAll("[^0-9]", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);
                if (!digits.equals(newVal)) {
                    enCoursDeFormat = true;
                    final String d = digits;
                    Platform.runLater(() -> {
                        try {
                            cvvField.setText(d);
                            cvvField.positionCaret(d.length());
                        } finally { enCoursDeFormat = false; }
                    });
                }
            });
        }
    }

    // ══════════════════════════════════════════════════
    //  INJECTION DONNÉES (depuis BudgetController)
    // ══════════════════════════════════════════════════

    public void initDonnees(Destination destination, LocalDate dateDebut, LocalDate dateFin,
                            int idVoyage,
                            double totalVol, double totalHotel,
                            double totalActivites, double totalServices, double totalGlobal) {
        this.destination    = destination;
        this.dateDebut      = dateDebut;
        this.dateFin        = dateFin;
        this.idVoyage       = idVoyage;
        this.totalVol       = totalVol;
        this.totalHotel     = totalHotel;
        this.totalActivites = totalActivites;
        this.totalServices  = totalServices;
        this.totalGlobal    = totalGlobal;
        mettreAJourInterface();
    }

    // ══════════════════════════════════════════════════
    //  MISE À JOUR INTERFACE (même pattern que BudgetController)
    // ══════════════════════════════════════════════════

    private void mettreAJourInterface() {
        if (destination == null) return;

        String destTxt  = destination.getPays() + " — " + destination.getVille();
        String datesTxt = dateDebut != null && dateFin != null
                ? dateDebut.format(fmt) + "  →  " + dateFin.format(fmt) : "—";
        long   jours    = dateDebut != null && dateFin != null
                ? ChronoUnit.DAYS.between(dateDebut, dateFin) : 0;
        String joursStr = jours + " jour" + (jours > 1 ? "s" : "");

        // Header
        if (headerDestLabel  != null) headerDestLabel.setText(destTxt);
        if (headerDatesLabel != null) headerDatesLabel.setText(datesTxt);

        // Bandeau info (identique à Budget)
        if (destinationLabel != null) destinationLabel.setText("🌍 " + destTxt);
        if (datesLabel       != null) datesLabel.setText(datesTxt);
        if (dureeLabel       != null) dureeLabel.setText(joursStr);

        // Recap colonne droite
        if (recapDestLabel   != null) recapDestLabel.setText(destTxt);
        if (recapDepartLabel != null) recapDepartLabel.setText(dateDebut != null ? dateDebut.format(fmt) : "—");
        if (recapRetourLabel != null) recapRetourLabel.setText(dateFin   != null ? dateFin.format(fmt)   : "—");
        if (recapDureeLabel  != null) recapDureeLabel.setText(joursStr);

        // Résumé commande
        if (cmdVolLabel       != null) cmdVolLabel.setText(String.format("%.0f TND", totalVol));
        if (cmdHotelLabel     != null) cmdHotelLabel.setText(String.format("%.0f TND", totalHotel));
        if (cmdActivitesLabel != null) cmdActivitesLabel.setText(String.format("%.0f TND", totalActivites));
        if (cmdServicesLabel  != null) cmdServicesLabel.setText(String.format("%.0f TND", totalServices));
        if (cmdTotalLabel     != null) cmdTotalLabel.setText(String.format("%.0f TND", totalGlobal));
        if (cmdParJourLabel   != null && jours > 0)
            cmdParJourLabel.setText(String.format("≈ %.0f TND par jour sur %d jour%s",
                    totalGlobal / jours, jours, jours > 1 ? "s" : ""));
    }

    // ══════════════════════════════════════════════════
    //  DÉTECTION CARTE + LUHN
    // ══════════════════════════════════════════════════

    private void detecterTypeCarteEtValider(String digits) {
        if (carteTypeLabel == null || carteStatutLabel == null) return;
        if (digits.isEmpty()) { carteTypeLabel.setText(""); carteStatutLabel.setText(""); return; }

        String type = "CARTE";
        if      (digits.startsWith("4"))           type = "💳 VISA";
        else if (digits.matches("^5[1-5].*"))      type = "💳 MASTERCARD";
        else if (digits.startsWith("34") || digits.startsWith("37")) type = "💳 AMEX";
        else if (digits.startsWith("6011") || digits.startsWith("65")) type = "💳 DISCOVER";

        carteTypeLabel.setText(type);
        carteTypeLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#FF6B35;");

        boolean isAmex = digits.startsWith("34") || digits.startsWith("37");
        int     expLen = isAmex ? 15 : 16;

        if (digits.length() == expLen) {
            if (algorithemLuhn(digits)) {
                carteStatutLabel.setText("✔ Carte valide");
                carteStatutLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#27AE60;");
                numCarteField.setStyle("-fx-background-color:#F0FFF4;-fx-border-color:#27AE60;-fx-border-width:2;"
                        + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-size:14px;");
            } else {
                carteStatutLabel.setText("✘ Numero invalide");
                carteStatutLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#E74C3C;");
                numCarteField.setStyle("-fx-background-color:#FFF5F5;-fx-border-color:#E74C3C;-fx-border-width:2;"
                        + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-size:14px;");
            }
        } else {
            carteStatutLabel.setText("");
            numCarteField.setStyle("-fx-background-color:#F8F9FA;-fx-border-color:#DDD;"
                    + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-size:14px;");
        }
    }

    private boolean algorithemLuhn(String numero) {
        int somme = 0; boolean doubler = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int c = numero.charAt(i) - '0';
            if (doubler) { c *= 2; if (c > 9) c -= 9; }
            somme += c; doubler = !doubler;
        }
        return somme % 10 == 0;
    }

    private boolean verifierExpiration(String exp) {
        try {
            String[] p = exp.split("/");
            int mois = Integer.parseInt(p[0]);
            int annee = 2000 + Integer.parseInt(p[1]);
            if (mois < 1 || mois > 12) return false;
            return !YearMonth.of(annee, mois).isBefore(YearMonth.now());
        } catch (Exception e) { return false; }
    }

    // ══════════════════════════════════════════════════
    //  TOGGLE PAIEMENT
    // ══════════════════════════════════════════════════

    @FXML
    private void onPaiementToggle(javafx.event.ActionEvent event) {
        String off = "-fx-background-color:#F0F0F0;-fx-text-fill:#555;-fx-font-size:12.5px;-fx-background-radius:9;-fx-cursor:hand;";
        String on  = "-fx-background-color:#FF6B35;-fx-text-fill:white;-fx-font-size:12.5px;-fx-background-radius:9;-fx-cursor:hand;";
        btnCarte.setStyle(off);    btnCarte.setSelected(false);
        btnVirement.setStyle(off); btnVirement.setSelected(false);
        btnEspeces.setStyle(off);  btnEspeces.setSelected(false);

        Object src = event.getSource();
        if (src == btnCarte) {
            btnCarte.setStyle(on); btnCarte.setSelected(true);
            modePaiement = "CARTE"; afficherPanelCarte();
        } else if (src == btnVirement) {
            btnVirement.setStyle(on); btnVirement.setSelected(true);
            modePaiement = "VIREMENT"; masquerPanelCarte();
            paiementInfoLabel.setText(
                    "Paiement par virement bancaire\n\n" +
                            "RIB : 12 345 6789012345678 90\n" +
                            "Banque : Banque de Tunisie\n" +
                            "Reference : VOY-" + idVoyage + "\n\n" +
                            "Votre reservation sera confirmee des reception du virement.");
            paiementInfoLabel.setVisible(true); paiementInfoLabel.setManaged(true);
        } else if (src == btnEspeces) {
            btnEspeces.setStyle(on); btnEspeces.setSelected(true);
            modePaiement = "ESPECES"; masquerPanelCarte();
            paiementInfoLabel.setText(
                    "Paiement en especes\n\nMontant : " +
                            String.format("%.0f TND", totalGlobal) +
                            "\n\nAdresse : 12 Avenue Habib Bourguiba, Tunis\nHoraires : Lun-Ven 09h-18h");
            paiementInfoLabel.setVisible(true); paiementInfoLabel.setManaged(true);
        }
    }

    private void afficherPanelCarte() {
        if (panelCarte != null)        { panelCarte.setVisible(true);        panelCarte.setManaged(true);         }
        if (paiementInfoLabel != null) { paiementInfoLabel.setVisible(false); paiementInfoLabel.setManaged(false); }
    }

    private void masquerPanelCarte() {
        if (panelCarte != null) { panelCarte.setVisible(false); panelCarte.setManaged(false); }
    }

    // ══════════════════════════════════════════════════
    //  CONFIRMER
    // ══════════════════════════════════════════════════

    @FXML
    private void confirmerReservation() {
        if (!validerFormulaire()) return;
        if ("CARTE".equals(modePaiement)) {
            simulerPaiementCarte();
        } else {
            int idRes = enregistrerReservation();
            if (idRes > 0) {
                enregistrerPaiement(idRes, STATUT_PAIEMENT_ATTENTE);
                afficherSuccesFinal(idRes);
            }
        }
    }

    // ══════════════════════════════════════════════════
    //  SIMULATION PAIEMENT CARTE
    // ══════════════════════════════════════════════════

    private void simulerPaiementCarte() {
        if (confirmerButton != null) {
            confirmerButton.setDisable(true);
            confirmerButton.setText("Traitement en cours...");
            confirmerButton.setStyle("-fx-background-color:#BDC3C7;-fx-text-fill:white;-fx-font-size:14px;"
                    + "-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:wait;");
        }

        Timeline t1 = new Timeline(new KeyFrame(Duration.millis(800), e -> setBtnText("Connexion securisee...")));
        Timeline t2 = new Timeline(new KeyFrame(Duration.millis(800), e -> setBtnText("Verification de la carte...")));
        Timeline t3 = new Timeline(new KeyFrame(Duration.millis(800), e -> setBtnText("Autorisation bancaire...")));
        Timeline t4 = new Timeline(new KeyFrame(Duration.millis(800), e -> Platform.runLater(() -> {
            if (confirmerButton != null) {
                confirmerButton.setDisable(false);
                confirmerButton.setText("Confirmer la reservation");
                confirmerButton.setStyle("-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);"
                        + "-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;"
                        + "-fx-background-radius:10;-fx-cursor:hand;"
                        + "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.45),12,0,0,4);");
            }
            afficherRecu();
        })));

        t1.setOnFinished(e -> t2.play());
        t2.setOnFinished(e -> t3.play());
        t3.setOnFinished(e -> t4.play());
        t1.play();
    }

    private void setBtnText(String t) { if (confirmerButton != null) confirmerButton.setText(t); }

    private void afficherRecu() {
        String digits    = numCarteField.getText().replaceAll("\\s", "");
        String derniers4 = digits.length() >= 4 ? digits.substring(digits.length() - 4) : "****";
        String type      = digits.startsWith("4") ? "VISA"
                : digits.startsWith("5") ? "MASTERCARD"
                : digits.startsWith("3") ? "AMEX" : "CARTE";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paiement approuve");
        alert.setHeaderText("Transaction approuvee");
        alert.setContentText(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "      RECU DE PAIEMENT\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Reseau     : " + type + "\n" +
                        "Carte      : **** **** **** " + derniers4 + "\n" +
                        "Titulaire  : " + nomCarteField.getText().trim().toUpperCase() + "\n" +
                        "Montant    : " + String.format("%.0f TND", totalGlobal) + "\n" +
                        "Code auth. : " + genererCodeAuth() + "\n" +
                        "Date       : " + LocalDate.now().format(fmt) + "\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "            APPROUVE"
        );
        alert.showAndWait();

        int idRes = enregistrerReservation();
        if (idRes > 0) {
            enregistrerPaiement(idRes, STATUT_PAIEMENT_OK);
            afficherSuccesFinal(idRes);
        }
    }

    // ══════════════════════════════════════════════════
    //  ENREGISTREMENT RÉSERVATION
    // ══════════════════════════════════════════════════

    private int enregistrerReservation() {
        try {
            Reservation r = new Reservation();
            r.setDate_reservation(Date.valueOf(LocalDate.now()));
            r.setPrix_reservation(totalGlobal);
            r.setEtat(ETAT_RESERVATION);
            r.setEmail(emailField.getText().trim());
            r.setNum_tel(telephoneField.getText().trim());
            r.setNum_passeport(passportField.getText().trim().isEmpty()
                    ? null : passportField.getText().trim());

            String commentaire = prenomField.getText().trim() + " " + nomField.getText().trim()
                    + " | Paiement: " + modePaiement;
            if ("CARTE".equals(modePaiement) && numCarteField != null) {
                String d = numCarteField.getText().replaceAll("\\s", "");
                commentaire += " (**** " + (d.length() >= 4 ? d.substring(d.length() - 4) : "****") + ")";
            }
            if (commentaireArea != null && !commentaireArea.getText().isBlank())
                commentaire += " — " + commentaireArea.getText().trim();
            r.setCommentaire(commentaire);

            r.setNombre_personnes(nbPersonnesSpinner != null ? nbPersonnesSpinner.getValue() : 1);

            StatutReservation statut = new StatutReservation();
            statut.setId_statut(ID_STATUT_EN_ATTENTE);
            r.setId_statut(statut);

            Personne personne = new Personne();
            personne.setIdUtilisateur(ID_PERSONNE_CONNECTE);
            r.setId_personne(personne);

            Voyage voyage = new Voyage();
            voyage.setIdVoyage(idVoyage);
            r.setId_voyage(voyage);
            r.setId_offre(null);

            boolean ok = serviceReservation.ajouter(r);
            if (!ok) { showAlert("Erreur", "La reservation n'a pas pu etre enregistree."); return -1; }
            return obtenirDernierIdReservation();

        } catch (SQLException e) {
            showAlert("Erreur BD", "Reservation : " + e.getMessage());
            return -1;
        }
    }

    private int obtenirDernierIdReservation() {
        try {
            java.util.List<Reservation> all = serviceReservation.readAll();
            if (all != null && !all.isEmpty())
                return all.get(all.size() - 1).getId_reservation();
        } catch (SQLException e) {
            System.err.println("Impossible de recuperer l'id reservation : " + e.getMessage());
        }
        return -1;
    }

    // ══════════════════════════════════════════════════
    //  ENREGISTREMENT PAIEMENT
    // ══════════════════════════════════════════════════

    private void enregistrerPaiement(int idReservation, String statut) {
        try {
            Paiement p = new Paiement();
            p.setMontant(totalGlobal);
            p.setDatePaiement(LocalDateTime.now());
            p.setStatut(statut);
            p.setIdReservation(idReservation);

            TypePaiement tp = new TypePaiement();
            switch (modePaiement) {
                case "CARTE"    -> tp.setIdTypePaiement(ID_TYPE_CARTE);
                case "VIREMENT" -> tp.setIdTypePaiement(ID_TYPE_VIREMENT);
                case "ESPECES"  -> tp.setIdTypePaiement(ID_TYPE_ESPECES);
                default         -> tp.setIdTypePaiement(ID_TYPE_CARTE);
            }
            p.setTypePaiement(tp);

            boolean ok = servicePaiement.ajouter(p);
            if (!ok)
                showAlert("Paiement",
                        "Reservation enregistree mais le paiement n'a pas pu etre sauvegarde.");
        } catch (SQLException e) {
            showAlert("Paiement", "Reservation OK, erreur paiement :\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    //  SUCCÈS
    // ══════════════════════════════════════════════════

    private void afficherSuccesFinal(int idReservation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservation confirmee");
        alert.setHeaderText("Votre voyage a ete reserve avec succes !");
        alert.setContentText(
                "Destination   : " + (destination != null
                        ? destination.getPays() + " — " + destination.getVille() : "—") + "\n" +
                        "Depart        : " + (dateDebut != null ? dateDebut.format(fmt) : "—") + "\n" +
                        "Retour        : " + (dateFin   != null ? dateFin.format(fmt)   : "—") + "\n" +
                        "Total paye    : " + String.format("%.0f TND", totalGlobal) + "\n" +
                        "Mode paiement : " + modePaiement + "\n" +
                        "Statut        : " + ETAT_RESERVATION + "\n" +
                        "Reference     : RES-" + idReservation + "\n\n" +
                        "Un email de confirmation sera envoye a " + emailField.getText().trim() + "."
        );
        alert.showAndWait();
    }

    // ══════════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════════

    private boolean validerFormulaire() {
        if (estVide(nomField) || estVide(prenomField)) {
            showAlert("Champs manquants", "Veuillez saisir votre nom et prenom.");
            surlignerErreur(nomField); surlignerErreur(prenomField); return false;
        }
        if (estVide(emailField) || !emailField.getText().contains("@")) {
            showAlert("Email invalide", "Veuillez saisir une adresse email valide.");
            surlignerErreur(emailField); return false;
        }
        if (estVide(telephoneField)) {
            showAlert("Champs manquants", "Veuillez saisir votre numero de telephone.");
            surlignerErreur(telephoneField); return false;
        }
        if ("CARTE".equals(modePaiement)) {
            String digits  = numCarteField != null ? numCarteField.getText().replaceAll("\\s", "") : "";
            boolean isAmex = digits.startsWith("34") || digits.startsWith("37");
            int     expLen = isAmex ? 15 : 16;
            if (digits.length() < expLen) {
                showAlert("Carte invalide",
                        "Numero incomplet (" + expLen + " chiffres requis).\n\n"
                                + "Cartes de test :\n• VISA : 4111 1111 1111 1111\n"
                                + "• MC   : 5500 0055 5555 5559\n• AMEX : 3714 496353 98431");
                surlignerErreur(numCarteField); return false;
            }
            if (!algorithemLuhn(digits)) {
                showAlert("Carte invalide", "Numero de carte invalide.\n\n"
                        + "Cartes de test :\n• VISA : 4111 1111 1111 1111\n• MC : 5500 0055 5555 5559");
                surlignerErreur(numCarteField); return false;
            }
            if (estVide(nomCarteField)) {
                showAlert("Carte invalide", "Veuillez saisir le nom tel qu'il apparait sur la carte.");
                surlignerErreur(nomCarteField); return false;
            }
            if (estVide(expirationField) || !expirationField.getText().matches("\\d{2}/\\d{2}")) {
                showAlert("Carte invalide", "Format d'expiration invalide. Utilisez MM/AA (ex: 12/28).");
                surlignerErreur(expirationField); return false;
            }
            if (!verifierExpiration(expirationField.getText())) {
                showAlert("Carte expiree", "La carte saisie est expiree.");
                surlignerErreur(expirationField); return false;
            }
            if (cvvField == null || cvvField.getText().length() < 3) {
                showAlert("Carte invalide", "Veuillez saisir le code CVV (3 ou 4 chiffres)."); return false;
            }
        }
        reinitialiserStyle(nomField, prenomField, emailField, telephoneField,
                numCarteField, expirationField, nomCarteField);
        return true;
    }

    private boolean estVide(TextField f) {
        return f == null || f.getText() == null || f.getText().trim().isEmpty();
    }

    private void surlignerErreur(TextField f) {
        if (f != null) f.setStyle("-fx-background-color:#FFF5F5;-fx-border-color:#E74C3C;-fx-border-width:2;"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-size:13px;");
    }

    private void reinitialiserStyle(TextField... fields) {
        String base = "-fx-background-color:#F8F9FA;-fx-border-color:#DDD;-fx-border-radius:8;"
                + "-fx-background-radius:8;-fx-padding:9 12;-fx-font-size:13px;";
        for (TextField f : fields) if (f != null) f.setStyle(base);
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @FXML
    private void retourBudget() {
        URL url = getClass().getClassLoader().getResource("ConfigurerVoyage/Budget.fxml");
        if (url == null) url = getClass().getResource("/ConfigurerVoyage/Budget.fxml");
        if (url == null) { showAlert("Erreur", "Budget.fxml introuvable."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) confirmerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TripEase — Budget");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger Budget.fxml : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    //  HOVER BOUTONS (même pattern que BudgetController)
    // ══════════════════════════════════════════════════

    @FXML
    private void onMouseEnteredButton(javafx.scene.input.MouseEvent e) {
        ((Button) e.getSource()).setOpacity(0.85);
    }

    @FXML
    private void onMouseExitedButton(javafx.scene.input.MouseEvent e) {
        ((Button) e.getSource()).setOpacity(1.0);
    }

    @FXML
    private void onMouseEnteredSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button) e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#E55A25,#E8820F);"
                        + "-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;"
                        + "-fx-background-radius:10;-fx-cursor:hand;"
                        + "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.65),16,0,0,5);");
    }

    @FXML
    private void onMouseExitedSuivantButton(javafx.scene.input.MouseEvent e) {
        ((Button) e.getSource()).setStyle(
                "-fx-background-color:linear-gradient(to right,#FF6B35,#F7931E);"
                        + "-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;"
                        + "-fx-background-radius:10;-fx-cursor:hand;"
                        + "-fx-effect:dropshadow(gaussian,rgba(255,107,53,0.45),12,0,0,4);");
    }

    // ══════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════

    private String genererCodeAuth() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 6; i++) code.append(chars.charAt(rand.nextInt(chars.length())));
        return code.toString();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}