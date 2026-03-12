package Controller;

import Entite.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardUserController implements Initializable {

    // ══ FXML ══════════════════════════════════════════════════════════════════
    @FXML private Label  lblNomUser;
    @FXML private Label  lblAvatar;
    @FXML private HBox   hboxBreadcrumb;
    @FXML private VBox   contenuPrincipal;

    @FXML private HBox   menuAccueil;
    @FXML private HBox   menuConfigurerVoyage;
    @FXML private HBox   menuOffres;
    @FXML private HBox   menuPaiement;
    @FXML private HBox   menuNotifications;
    @FXML private HBox   menuAvis;
    @FXML private HBox   menuReservation;

    // ══ ÉTAT ══════════════════════════════════════════════════════════════════
    private Utilisateur utilisateurConnecte;
    private HBox        menuActif;

    // ══ STYLES SIDEBAR ════════════════════════════════════════════════════════
    /** Menu actif : fond orange clair + barre orange à gauche */
    private static final String MENU_ACTIF =
            "-fx-padding: 11 14; " +
                    "-fx-background-color: #FFF3E0; " +
                    "-fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-border-color: #FF6B00; " +
                    "-fx-border-width: 0 0 0 3; " +
                    "-fx-border-radius: 0 8 8 0;";

    /** Menu normal : transparent */
    private static final String MENU_NORMAL =
            "-fx-padding: 11 14; -fx-background-radius: 8; -fx-cursor: hand;";

    /** Menu survolé : fond gris très léger */
    private static final String MENU_HOVER =
            "-fx-padding: 11 14; " +
                    "-fx-background-color: #F5F0EB; " +
                    "-fx-background-radius: 8; -fx-cursor: hand;";

    // ══ INIT ══════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuActif = menuAccueil;
        mettreAJourBreadcrumb("Accueil", null);
        afficherPageAccueil();
    }

    // ══ INJECTION UTILISATEUR ═════════════════════════════════════════════════
    public void setUtilisateur(Utilisateur user) {
        this.utilisateurConnecte = user;
        if (user != null) {
            String nom = extraireNom(user.getEmail());
            lblNomUser.setText(nom);
            lblAvatar.setText(
                    String.valueOf(user.getEmail().charAt(0)).toUpperCase());
        }
    }

    // ══ BREADCRUMB ════════════════════════════════════════════════════════════
    /**
     * Construit le fil d'ariane.
     * mettreAJourBreadcrumb("Accueil", null)          → 🏠 Accueil
     * mettreAJourBreadcrumb("Accueil", "Réservation") → 🏠 Accueil › Réservation
     */
    private void mettreAJourBreadcrumb(String s1, String s2) {
        hboxBreadcrumb.getChildren().clear();

        // Icône maison
        Label icone = new Label("🏠");
        icone.setStyle("-fx-font-size: 12px;");

        // Lien "Accueil"
        Label lbl1 = new Label(s1);
        lbl1.setStyle(s2 == null
                ? "-fx-font-size: 12.5px; -fx-text-fill: #FF6B00;" +
                " -fx-font-weight: bold; -fx-font-family: 'Segoe UI';"
                : "-fx-font-size: 12.5px; -fx-text-fill: #999;" +
                " -fx-cursor: hand; -fx-font-family: 'Segoe UI';");
        if (s2 != null) lbl1.setOnMouseClicked(e -> ouvrirAccueil());

        hboxBreadcrumb.getChildren().addAll(icone, lbl1);

        // Deuxième niveau
        if (s2 != null) {
            Label sep = new Label("›");
            sep.setStyle("-fx-font-size: 12px; -fx-text-fill: #CCC; " +
                    "-fx-padding: 0 2;");

            Label lbl2 = new Label(s2);
            lbl2.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #FF6B00;" +
                    " -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            hboxBreadcrumb.getChildren().addAll(sep, lbl2);
        }
    }

    // ══ GESTION SIDEBAR ═══════════════════════════════════════════════════════
    /** Active visuellement le menu cliqué */
    private void activerMenu(HBox cible) {
        if (menuActif != null) {
            menuActif.setStyle(MENU_NORMAL);
            colorierLabels(menuActif, false);
        }
        cible.setStyle(MENU_ACTIF);
        colorierLabels(cible, true);
        menuActif = cible;
    }

    /**
     * Colorie les labels d'un menu.
     * actif=true → orange | actif=false → couleurs normales
     */
    private void colorierLabels(HBox menu, boolean actif) {
        for (var noeud : menu.getChildren()) {
            if (noeud instanceof VBox vbox) {
                var enfants = vbox.getChildren();
                for (int i = 0; i < enfants.size(); i++) {
                    if (enfants.get(i) instanceof Label lbl) {
                        if (i == 0) { // grand label (titre)
                            lbl.setStyle(actif
                                    ? "-fx-font-size: 12.5px; -fx-font-weight: bold;" +
                                    " -fx-text-fill: #FF6B00; -fx-font-family: 'Segoe UI';"
                                    : "-fx-font-size: 12.5px; -fx-text-fill: #4A4035;" +
                                    " -fx-font-family: 'Segoe UI';");
                        } else { // petit label (sous-titre)
                            lbl.setStyle(actif
                                    ? "-fx-font-size: 10px; -fx-text-fill: #FFB066;"
                                    : "-fx-font-size: 10px; -fx-text-fill: #B0A898;");
                        }
                    }
                }
            }
        }
    }

    @FXML public void onMenuHover(MouseEvent e) {
        HBox h = (HBox) e.getSource();
        if (h != menuActif) h.setStyle(MENU_HOVER);
    }

    @FXML public void onMenuExit(MouseEvent e) {
        HBox h = (HBox) e.getSource();
        if (h != menuActif) h.setStyle(MENU_NORMAL);
    }

    // ══ PAGE ACCUEIL ══════════════════════════════════════════════════════════
    @FXML
    public void ouvrirAccueil() {
        activerMenu(menuAccueil);
        mettreAJourBreadcrumb("Accueil", null);
        afficherPageAccueil();
    }

    private void afficherPageAccueil() {
        contenuPrincipal.getChildren().clear();

        VBox page = new VBox(26);
        page.setPadding(new Insets(30, 36, 40, 36));
        page.setStyle("-fx-background-color: #F5F0EB;");

        // ── En-tête de page ─────────────────────────────────────────────────
        HBox enTete = new HBox();
        enTete.setAlignment(Pos.CENTER_LEFT);

        VBox titresBox = new VBox(3);
        Label lblTitre = new Label("Tableau de bord");
        lblTitre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;" +
                " -fx-text-fill: #2A1F14; -fx-font-family: 'Segoe UI';");

        String nom = utilisateurConnecte != null
                ? extraireNom(utilisateurConnecte.getEmail()) : "vous";
        Label lblSous = new Label("Bon retour, " + nom + " ! Que planifions-nous ?");
        lblSous.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #8A7A6A;" +
                " -fx-font-family: 'Segoe UI';");
        titresBox.getChildren().addAll(lblTitre, lblSous);
        enTete.getChildren().add(titresBox);

        // ── Bannière ────────────────────────────────────────────────────────
        Pane banniere = construireBanniere(nom);

        // ── Section cartes ──────────────────────────────────────────────────
        HBox titreCartes = new HBox(10);
        titreCartes.setAlignment(Pos.CENTER_LEFT);
        Label pointDeco = new Label("▌");
        pointDeco.setStyle("-fx-text-fill: #FF6B00; -fx-font-size: 18px;");
        Label lblSectionTitre = new Label("Que voulez-vous faire ?");
        lblSectionTitre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;" +
                " -fx-text-fill: #2A1F14; -fx-font-family: 'Segoe UI';");
        titreCartes.getChildren().addAll(pointDeco, lblSectionTitre);

        // ── Grille : 3 colonnes × 2 lignes ─────────────────────────────────
        GridPane grille = new GridPane();
        grille.setHgap(18);
        grille.setVgap(18);

        // Chaque colonne prend 1/3 de la largeur disponible
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            grille.getColumnConstraints().add(col);
        }

        // Données des 6 cartes : {emoji, titre, description, lien, couleurFond}
        String[][] cartes = {
                {"🗺", "Configurer mon voyage",
                        "Définissez votre destination, vos dates et le rythme de votre séjour.",
                        "Commencer →", "#FFF3E0", "#FFEAD0"},
                {"🎁", "Nos offres spéciales",
                        "Découvrez nos promotions exclusives et bons plans du moment.",
                        "Explorer →", "#FFF8EC", "#FFF0D6"},
                {"💳", "Effectuer un paiement",
                        "Réglez vos réservations de façon simple, rapide et sécurisée.",
                        "Payer →", "#F0FFF7", "#DCFCEE"},
                {"🔔", "Mes notifications",
                        "Consultez vos alertes, rappels et messages importants.",
                        "Voir tout →", "#FFF5F5", "#FFEBEB"},
                {"⭐", "Évaluer mon voyage",
                        "Partagez votre avis et aidez la communauté TripEase.",
                        "Donner mon avis →", "#FFFBEC", "#FFF5D6"},
                {"📅", "Mes réservations",
                        "Retrouvez l'ensemble de vos réservations hôtels, vols et activités.",
                        "Gérer →", "#F0F4FF", "#E0E8FF"}
        };

        // Actions correspondantes aux 6 cartes
        Runnable[] actions = {
                this::ouvrirConfigurerVoyage,
                this::ouvrirOffres,
                this::ouvrirPaiement,
                this::ouvrirNotifications,
                this::ouvrirAvis,
                this::ouvrirReservation
        };

        for (int i = 0; i < 6; i++) {
            String[] d = cartes[i];
            final Runnable action = actions[i];
            VBox carte = construireCarte(
                    d[0], d[1], d[2], d[3], d[4], d[5],
                    e -> action.run());
            grille.add(carte, i % 3, i / 3); // col, row
        }

        page.getChildren().addAll(enTete, banniere, titreCartes, grille);
        contenuPrincipal.getChildren().setAll(page);
    }

    // ══ BANNIÈRE ══════════════════════════════════════════════════════════════
    private Pane construireBanniere(String nom) {
        Pane p = new Pane();
        p.setPrefHeight(108);
        p.setStyle("-fx-background-color: linear-gradient(to right, #FF5500, #FF8C00, #FFA830);" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian,rgba(255,100,0,0.30),18,0,0,5);");

        Label ico = new Label("✈");
        ico.setStyle("-fx-font-size: 20px; -fx-text-fill: rgba(255,255,255,0.9);");
        ico.setLayoutX(28); ico.setLayoutY(25);

        Label titre = new Label("Bienvenue sur TripEase !");
        titre.setStyle("-fx-font-size: 21px; -fx-font-weight: bold;" +
                " -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
        titre.setLayoutX(58); titre.setLayoutY(22);

        Label msg = new Label(
                "Bonjour " + nom + " ! Prêt pour votre prochain voyage ? " +
                        "Explorez, planifiez et partez avec TripEase. 🌍");
        msg.setStyle("-fx-font-size: 12.5px;" +
                " -fx-text-fill: rgba(255,255,255,0.88);" +
                " -fx-font-family: 'Segoe UI';");
        msg.setLayoutX(28); msg.setLayoutY(62);

        // Décoration droite
        Label globe = new Label("🌐");
        globe.setStyle("-fx-font-size: 70px; -fx-opacity: 0.12;");
        globe.setLayoutX(870); globe.setLayoutY(8);

        p.getChildren().addAll(ico, titre, msg, globe);
        return p;
    }

    // ══ CARTE D'ACTION ════════════════════════════════════════════════════════
    /**
     * Construit une carte cliquable avec icône + titre + description + lien.
     *
     * @param emoji        l'icône
     * @param titre        titre principal
     * @param description  texte explicatif
     * @param lien         texte du lien en bas
     * @param fondIcone    couleur de fond du badge icône (clair)
     * @param fondIconeAlt couleur au hover du badge icône (légèrement plus foncé)
     * @param action       ce qui se passe au clic
     */
    private VBox construireCarte(
            String emoji, String titre, String description,
            String lien, String fondIcone, String fondIconeAlt,
            javafx.event.EventHandler<javafx.scene.input.MouseEvent> action) {

        VBox carte = new VBox(10);
        carte.setAlignment(Pos.TOP_LEFT);
        carte.setPadding(new Insets(20, 18, 18, 18));
        appliquerStyleCarteNormal(carte);

        // Badge icône
        StackPane badge = new StackPane();
        badge.setStyle("-fx-background-color: " + fondIcone + ";" +
                "-fx-background-radius: 11;" +
                "-fx-pref-width: 48; -fx-pref-height: 48;" +
                "-fx-max-width: 48; -fx-max-height: 48;");
        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 22px;");
        badge.getChildren().add(lblEmoji);

        // Titre
        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold;" +
                " -fx-text-fill: #2A1F14; -fx-font-family: 'Segoe UI';");
        lblTitre.setWrapText(true);

        // Description
        Label lblDesc = new Label(description);
        lblDesc.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8A7A6A;" +
                " -fx-font-family: 'Segoe UI';");
        lblDesc.setWrapText(true);

        // Espaceur
        Region esp = new Region();
        VBox.setVgrow(esp, Priority.ALWAYS);

        // Ligne de séparation
        Pane separateur = new Pane();
        separateur.setPrefHeight(1);
        separateur.setStyle("-fx-background-color: #F0EBE3;");

        // Lien bas de carte
        HBox lienBox = new HBox();
        lienBox.setAlignment(Pos.CENTER_LEFT);
        lienBox.setPadding(new Insets(6, 0, 0, 0));
        Label lblLien = new Label(lien);
        lblLien.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;" +
                " -fx-text-fill: #FF6B00; -fx-font-family: 'Segoe UI';");
        lienBox.getChildren().add(lblLien);

        carte.getChildren().addAll(badge, lblTitre, lblDesc, esp, separateur, lienBox);

        // ── Hover ────────────────────────────────────────────────────────────
        carte.setOnMouseEntered(e -> {
            appliquerStyleCarteHover(carte);
            badge.setStyle("-fx-background-color: " + fondIconeAlt + ";" +
                    "-fx-background-radius: 11;" +
                    "-fx-pref-width: 48; -fx-pref-height: 48;" +
                    "-fx-max-width: 48; -fx-max-height: 48;");
        });
        carte.setOnMouseExited(e -> {
            appliquerStyleCarteNormal(carte);
            badge.setStyle("-fx-background-color: " + fondIcone + ";" +
                    "-fx-background-radius: 11;" +
                    "-fx-pref-width: 48; -fx-pref-height: 48;" +
                    "-fx-max-width: 48; -fx-max-height: 48;");
        });
        carte.setOnMouseClicked(action);

        return carte;
    }

    private void appliquerStyleCarteNormal(VBox carte) {
        carte.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #EDE8E2;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 14;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06)," +
                "10,0,0,3);");
    }

    private void appliquerStyleCarteHover(VBox carte) {
        carte.setStyle("-fx-background-color: #FFFAF6;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #FF6B00;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 14;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian,rgba(255,107,0,0.18)," +
                "16,0,0,5);");
    }

    // ══ NAVIGATION VERS PAGES ═════════════════════════════════════════════════

    @FXML public void ouvrirConfigurerVoyage() {
        activerMenu(menuConfigurerVoyage);
        mettreAJourBreadcrumb("Accueil", "Configurer mon voyage");
        allerVers("/ConfigurerVoyage/ConfigVoyageUser.fxml",    // ← NOUVEAU NOM CORRECT
                "TripEase — Configurer mon voyage");
    }

    @FXML public void ouvrirAvis() {
        activerMenu(menuAvis);
        mettreAJourBreadcrumb("Accueil", "Évaluer mon voyage");
        try {
            URL url = localiserFXML("/AvisUser.fxml");
            if (url == null) {
                afficherSection("⭐", "Évaluer mon voyage",
                        "AvisUser.fxml introuvable.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof AvisUserController c)
                c.setUtilisateur(utilisateurConnecte);
            Stage st = obtenirStage();
            if (st != null) {
                st.setScene(new Scene(root));
                st.setTitle("TripEase — Évaluer mon voyage");
                st.show();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            afficherErreur("Impossible d'ouvrir la page d'avis : " + ex.getMessage());
        }
    }

    @FXML
    public void ouvrirOffres() {
        try {
            // Charger le FXML de la page reservationUser
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationUser/ReservationUser.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle via un composant existant
            Stage stage = (Stage) menuOffres.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réservation Utilisateur");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void ouvrirPaiement() {
        activerMenu(menuPaiement);
        mettreAJourBreadcrumb("Accueil", "Effectuer un paiement");
        afficherSection("💳", "Effectuer un paiement",
                "Le module de paiement sécurisé sera disponible très prochainement.");
    }

    @FXML public void ouvrirNotifications() {
        activerMenu(menuNotifications);
        mettreAJourBreadcrumb("Accueil", "Mes notifications");
        afficherSection("🔔", "Mes notifications",
                "Vous n'avez aucune nouvelle notification pour le moment.");
    }

    @FXML public void ouvrirReservation() {
        activerMenu(menuReservation);
        mettreAJourBreadcrumb("Accueil", "Mes réservations");
        afficherSection("📅", "Mes réservations",
                "Le module de réservation sera disponible très prochainement.");
    }

    // ══ DÉCONNEXION ═══════════════════════════════════════════════════════════
    @FXML public void seDeconnecter() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Déconnexion");
        a.setHeaderText(null);
        a.setContentText("Voulez-vous vraiment quitter votre espace TripEase ?");
        a.showAndWait().ifPresent(r -> {
            if (r == javafx.scene.control.ButtonType.OK)
                allerVers("/Login.fxml", "TripEase — Connexion");
        });
    }

    // ══ PAGE "BIENTÔT DISPONIBLE" ═════════════════════════════════════════════
    private void afficherSection(String emoji, String titre, String msg) {
        contenuPrincipal.getChildren().clear();
        VBox page = new VBox(24);
        page.setPadding(new Insets(30, 36, 40, 36));
        page.setStyle("-fx-background-color: #F5F0EB;");

        // En-tête de section
        VBox entete = new VBox(3);
        Label t = new Label(titre);
        t.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;" +
                " -fx-text-fill: #2A1F14; -fx-font-family: 'Segoe UI';");
        Label s = new Label("TripEase — votre espace personnel");
        s.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #B0A898;");
        entete.getChildren().addAll(t, s);

        // Carte centrale
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(60, 50, 60, 50));
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #EDE8E2;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 18;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),14,0,0,4);");

        Label ic = new Label(emoji);
        ic.setStyle("-fx-font-size: 50px;");

        Label lt = new Label(titre);
        lt.setStyle("-fx-font-size: 19px; -fx-font-weight: bold;" +
                " -fx-text-fill: #2A1F14; -fx-font-family: 'Segoe UI';");

        Label lm = new Label(msg);
        lm.setStyle("-fx-font-size: 13px; -fx-text-fill: #8A7A6A;");
        lm.setWrapText(true);
        lm.setMaxWidth(380);
        lm.setAlignment(Pos.CENTER);

        Label badge = new Label("  ⏳  Bientôt disponible  ");
        badge.setStyle("-fx-background-color: #FFF3E0;" +
                "-fx-text-fill: #FF6B00;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 7 20;");

        card.getChildren().addAll(ic, lt, lm, badge);

        HBox centrage = new HBox(card);
        centrage.setAlignment(Pos.CENTER);

        page.getChildren().addAll(entete, centrage);
        contenuPrincipal.getChildren().setAll(page);
    }

    // ══ UTILITAIRES ═══════════════════════════════════════════════════════════

    /** Navigue vers un FXML dans la même fenêtre */
    private void allerVers(String chemin, String titreFenetre) {
        try {
            URL url = localiserFXML(chemin);
            if (url == null) {
                afficherErreur("Fichier introuvable : " + chemin); return;
            }
            Parent root = FXMLLoader.load(url);
            Stage st = obtenirStage();
            if (st != null) {
                st.setScene(new Scene(root));
                st.setTitle(titreFenetre);
                st.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur : " + e.getMessage());
        }
    }

    /** Cherche un FXML avec deux méthodes (robuste) */
    private URL localiserFXML(String chemin) {
        URL url = getClass().getResource(chemin);
        if (url == null)
            url = getClass().getClassLoader().getResource(
                    chemin.startsWith("/") ? chemin.substring(1) : chemin);
        return url;
    }

    /** Extrait "rahmaayari272" depuis "rahmaayari272@gmail.com" */
    private String extraireNom(String email) {
        if (email == null || email.isBlank()) return "vous";
        return email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
    }

    /** Récupère le Stage (fenêtre principale) */
    private Stage obtenirStage() {
        if (lblNomUser != null && lblNomUser.getScene() != null)
            return (Stage) lblNomUser.getScene().getWindow();
        return null;
    }

    private void afficherErreur(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}