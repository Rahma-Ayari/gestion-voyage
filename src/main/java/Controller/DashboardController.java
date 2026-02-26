package Controller;

import Entite.Personne;
import Service.ServicePersonne;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * ══════════════════════════════════════════════════════════════════
 *  DashboardAdminController
 *  Gère la modération des inscriptions TripEase :
 *    - 3 sections : EN ATTENTE / APPROUVÉS / REFUSÉS
 *    - Recherche en temps réel
 *    - Pagination (PAGE_SIZE lignes par page)
 *    - Actions Approuver / Refuser avec confirmation
 * ══════════════════════════════════════════════════════════════════
 */
public class DashboardController implements Initializable { // "Initializable" signifie que ce code se lancera dès que la fenêtre s'ouvre.

    // ─── Injections FXML: font le pont avec l'interface graphique ───────────────────────────────────────────────────────
    @FXML private VBox       usersContainer;
    @FXML private Label      paginationLabel;
    @FXML private Label      badgeCount;
    @FXML private Label      pageTitle;
    @FXML private Label      pageSubtitle;
    @FXML private Button     btnPrecedent;
    @FXML private Button     btnSuivant;
    @FXML private TextField  searchField;
    @FXML private HBox       menuAttente;
    @FXML private HBox       menuApprouves;
    @FXML private HBox       menuRefuses;

    // ─── Services & données ────────────────────────────────────────────────────
    private final ServicePersonne   service  = new ServicePersonne();
    private final SimpleDateFormat  sdf      = new SimpleDateFormat("MMM dd, yyyy");

    private List<Personne> allData       = new ArrayList<>();
    private List<Personne> filteredData  = new ArrayList<>();
    private String         currentMode   = "pending"; // "pending" | "approved" | "refused"
    private static final int PAGE_SIZE   = 5;
    private int currentPage = 0;

    // ─── Palette couleurs ──────────────────────────────────────────────────────
    private static final String ORANGE         = "#FF6B00";
    private static final String ORANGE_LIGHT   = "#FFF3E0";
    private static final String BORDER_COLOR   = "#F0F0F0";
    private static final String ROW_HOVER      = "#FFF8F2";
    private static final String SIDEBAR_ACTIVE = "-fx-padding: 11 14 11 14; -fx-background-color: #FFF3E0; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String SIDEBAR_NORMAL = "-fx-padding: 11 14 11 14; -fx-background-radius: 8; -fx-cursor: hand;";

    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showPendingUsers(); // Au démarrage, affiche direct les utilisateurs en attente
    }

    // ─── Navigation sidebar ────────────────────────────────────────────────────
    @FXML
    public void showPendingUsers() {
        currentMode = "pending";
        pageTitle.setText("Inscriptions en attente");
        pageSubtitle.setText("Examinez et gérez les demandes de création de compte utilisateur.");
        updateSidebarActive(menuAttente);
        loadAndDisplay();
    }

    @FXML
    public void showApprovedUsers() {
        currentMode = "approved";
        pageTitle.setText("Utilisateurs approuvés");
        pageSubtitle.setText("Liste des comptes validés et actifs dans l'application TripEase.");
        updateSidebarActive(menuApprouves);
        loadAndDisplay();
    }

    @FXML
    public void showRefusedUsers() {
        currentMode = "refused";
        pageTitle.setText("Utilisateurs refusés");
        pageSubtitle.setText("Demandes d'inscription rejetées par l'administrateur.");
        updateSidebarActive(menuRefuses);
        loadAndDisplay();
    }

    /** Met en évidence l'élément actif dans la sidebar */
    private void updateSidebarActive(HBox active) {
        menuAttente.setStyle(SIDEBAR_NORMAL);
        menuApprouves.setStyle(SIDEBAR_NORMAL);
        menuRefuses.setStyle(SIDEBAR_NORMAL);
        active.setStyle(SIDEBAR_ACTIVE);

        // Ajuste les couleurs des labels enfants
        menuAttente.getChildren().forEach(n -> {
            if (n instanceof VBox vb) {
                vb.getChildren().forEach(c -> {
                    if (c instanceof Label l)
                        l.setStyle(l.getStyle().replace("-fx-text-fill: #FF6B00;", "-fx-text-fill: #666;")
                                .replace("-fx-font-weight: bold;", ""));
                });
            }
        });
        // Réactive le style sur l'élément sélectionné
        if (active == menuAttente) {
            active.getChildren().stream()
                    .filter(n -> n instanceof VBox)
                    .map(n -> (VBox) n)
                    .forEach(vb -> vb.getChildren().stream()
                            .filter(c -> c instanceof Label)
                            .map(c -> (Label) c)
                            .forEach(l -> l.setStyle(l.getStyle()
                                    + "-fx-text-fill: #FF6B00; -fx-font-weight: bold;")));
        }
    }

    // ─── Chargement des données ────────────────────────────────────────────────
    private void loadAndDisplay() {
        currentPage = 0;
        try {
            allData = switch (currentMode) {
                case "pending"  -> service.getPersonnesEnAttente();
                case "approved" -> service.getPersonnesApprouvees();
                case "refused"  -> service.getPersonnesRefusees();
                default         -> new ArrayList<>();
            };
        } catch (SQLException e) {
            showErrorAlert("Erreur base de données", e.getMessage());
            allData = new ArrayList<>();
        }

        // Mise à jour du badge toujours avec le nombre en attente
        try {
            badgeCount.setText(String.valueOf(service.getPersonnesEnAttente().size()));
        } catch (SQLException ignored) {}

        applySearch();
    }

    // ─── Recherche en temps réel ───────────────────────────────────────────────
    @FXML
    public void onSearch() {
        applySearch();
    }

    private void applySearch() {
        String q = searchField.getText().trim().toLowerCase();
        filteredData = allData.stream()
                .filter(p -> q.isEmpty()
                        || p.getNom().toLowerCase().contains(q)
                        || p.getPrenom().toLowerCase().contains(q)
                        || p.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());
        currentPage = 0;
        renderPage();
    }

    @FXML
    public void refreshData() {
        searchField.clear();
        loadAndDisplay();
    }

    // ─── Rendu de la page courante ─────────────────────────────────────────────
    private void renderPage() {
        usersContainer.getChildren().clear();

        int total = filteredData.size();
        int from  = currentPage * PAGE_SIZE;
        int to    = Math.min(from + PAGE_SIZE, total);

        if (total == 0) {
            Label empty = new Label("Aucun utilisateur trouvé pour cette section.");
            empty.setStyle("-fx-padding: 40; -fx-text-fill: #BBB; -fx-font-size: 14px;");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            usersContainer.getChildren().add(empty);
        } else {
            List<Personne> page = filteredData.subList(from, to);
            for (int i = 0; i < page.size(); i++) {
                usersContainer.getChildren().add(buildRow(page.get(i), i));
            }
        }

        // Label pagination
        paginationLabel.setText(
                total == 0
                        ? "Aucune demande"
                        : String.format("Affichage de %d demandes sur %d", (to - from), total));

        // Boutons pagination
        btnPrecedent.setDisable(currentPage == 0);
        int maxPage = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE) - 1;
        btnSuivant.setDisable(currentPage >= maxPage || total == 0);
    }

    // ─── Construction d'une ligne du tableau ───────────────────────────────────
    private HBox buildRow(Personne p, int rowIndex) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 20, 13, 20));

        String bgDefault = rowIndex % 2 == 0 ? "white" : "#FDFDFD";
        row.setStyle("-fx-background-color: " + bgDefault + ";"
                + "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;"
                + "-fx-border-width: 0 0 1 0;");
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: " + ROW_HOVER + ";"
                        + "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;"
                        + "-fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color: " + bgDefault + ";"
                        + "-fx-border-color: transparent transparent " + BORDER_COLOR + " transparent;"
                        + "-fx-border-width: 0 0 1 0;"));

        // ── Colonne 1 : Avatar + Nom prénom ──────────────────────────────────
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setPrefWidth(195);

        Label avatar = new Label(getInitiales(p));
        avatar.setStyle(
                "-fx-background-color: " + ORANGE_LIGHT + ";"
                        + "-fx-background-radius: 50%;"
                        + "-fx-min-width: 36; -fx-min-height: 36;"
                        + "-fx-max-width: 36; -fx-max-height: 36;"
                        + "-fx-alignment: center;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + ORANGE + ";"
                        + "-fx-font-size: 12px;");

        Label nameLabel = new Label(p.getNom() + " " + p.getPrenom());
        nameLabel.setStyle(
                "-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #222;"
                        + "-fx-font-family: 'Segoe UI';");
        nameBox.getChildren().addAll(avatar, nameLabel);

        // ── Colonne 2 : Email ─────────────────────────────────────────────────
        Label emailLbl = new Label(p.getEmail());
        emailLbl.setPrefWidth(235);
        emailLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        // ── Colonne 3 : Date ──────────────────────────────────────────────────
        String dateStr = p.getDateInscription() != null
                ? sdf.format(p.getDateInscription()) : "—";
        Label dateLbl = new Label(dateStr);
        dateLbl.setPrefWidth(175);
        dateLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");

        // ── Colonne 4 : Actions ───────────────────────────────────────────────
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(actions, Priority.ALWAYS);

        switch (currentMode) {
            case "pending" -> {
                Button btnApprove = makeButton("✔  Approuver", ORANGE, "white", true);
                Button btnRefuse  = makeButton("✖  Refuser",  "white", "#EF4444", false);
                btnApprove.setOnAction(e -> onApprove(p));
                btnRefuse.setOnAction(e  -> onRefuse(p));
                actions.getChildren().addAll(btnApprove, btnRefuse);
            }
            case "approved" -> {
                Label badge = new Label("✔  Approuvé");
                badge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46;"
                        + "-fx-background-radius: 20; -fx-padding: 5 14 5 14;"
                        + "-fx-font-size: 12px; -fx-font-weight: bold;");
                actions.getChildren().add(badge);
            }
            case "refused" -> {
                Label badge = new Label("✖  Refusé");
                badge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C;"
                        + "-fx-background-radius: 20; -fx-padding: 5 14 5 14;"
                        + "-fx-font-size: 12px; -fx-font-weight: bold;");
                actions.getChildren().add(badge);
            }
        }

        row.getChildren().addAll(nameBox, emailLbl, dateLbl, actions);
        return row;
    }

    private Button makeButton(String text, String bg, String fg, boolean filled) {
        Button btn = new Button(text);
        if (filled) {
            btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                    + "-fx-background-radius: 20; -fx-border-radius: 20;"
                    + "-fx-padding: 7 16 7 16; -fx-font-size: 13px;"
                    + "-fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: " + bg + ";"
                    + "-fx-border-color: " + fg + "; -fx-border-width: 1.5;"
                    + "-fx-text-fill: " + fg + ";"
                    + "-fx-background-radius: 20; -fx-border-radius: 20;"
                    + "-fx-padding: 7 16 7 16; -fx-font-size: 13px;"
                    + "-fx-font-weight: bold; -fx-cursor: hand;");
        }
        return btn;
    }

    // ─── Actions admin ─────────────────────────────────────────────────────────

    private void onApprove(Personne p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'approbation");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Voulez-vous approuver le compte de " + p.getNom() + " " + p.getPrenom() + " ?\n\n"
                        + "✔  L'utilisateur sera enregistré et pourra se connecter.");
        styleAlert(confirm);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.approuverInscription(p.getIdUtilisateur());
                    showInfoAlert("Compte approuvé ✔",
                            p.getNom() + " " + p.getPrenom() + " a été approuvé avec succès.\n"
                                    + "Son compte est maintenant actif sur TripEase.");
                    loadAndDisplay();
                } catch (SQLException e) {
                    showErrorAlert("Erreur", "Impossible d'approuver : " + e.getMessage());
                }
            }
        });
    }

    private void onRefuse(Personne p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer le refus");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Voulez-vous refuser la demande de " + p.getNom() + " " + p.getPrenom() + " ?\n\n"
                        + "✖  L'utilisateur ne pourra pas se connecter à TripEase.");
        styleAlert(confirm);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.refuserInscription(p.getIdUtilisateur());
                    showInfoAlert("Demande refusée",
                            "La demande de " + p.getNom() + " " + p.getPrenom() + " a été refusée.");
                    loadAndDisplay();
                } catch (SQLException e) {
                    showErrorAlert("Erreur", "Impossible de refuser : " + e.getMessage());
                }
            }
        });
    }

    // ─── Pagination ────────────────────────────────────────────────────────────
    @FXML
    public void goToPreviousPage() {
        if (currentPage > 0) { currentPage--; renderPage(); }
    }

    @FXML
    public void goToNextPage() {
        int max = (int) Math.ceil((double) filteredData.size() / PAGE_SIZE) - 1;
        if (currentPage < max) { currentPage++; renderPage(); }
    }

    // ─── Hover sidebar ─────────────────────────────────────────────────────────
    @FXML
    public void onMenuHover(MouseEvent e) {
        HBox hbox = (HBox) e.getSource();
        hbox.setStyle("-fx-padding: 11 14 11 14; -fx-background-color: #F5F5F5;"
                + "-fx-background-radius: 8; -fx-cursor: hand;");
    }

    @FXML
    public void onMenuExit(MouseEvent e) {
        HBox hbox = (HBox) e.getSource();
        // Ne pas écraser l'élément actif
        if (hbox != menuAttente  || !"pending".equals(currentMode))
            if (hbox != menuApprouves || !"approved".equals(currentMode))
                if (hbox != menuRefuses   || !"refused".equals(currentMode))
                    hbox.setStyle(SIDEBAR_NORMAL);
    }

    // ─── Alertes ───────────────────────────────────────────────────────────────
    private void showInfoAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        styleAlert(a); a.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        styleAlert(a); a.showAndWait();
    }

    private void styleAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle("-fx-background-color: white; -fx-border-color: " + ORANGE + ";"
                + "-fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
        // Bouton OK en orange
        dp.getButtonTypes().forEach(bt -> {
            Button b = (Button) dp.lookupButton(bt);
            if (b != null && (bt == ButtonType.OK || bt == ButtonType.CANCEL)) {
                b.setStyle("-fx-background-color: " + (bt == ButtonType.OK ? ORANGE : "white") + ";"
                        + "-fx-text-fill: " + (bt == ButtonType.OK ? "white" : ORANGE) + ";"
                        + "-fx-border-color: " + ORANGE + "; -fx-border-width: 1;"
                        + "-fx-background-radius: 6; -fx-border-radius: 6;"
                        + "-fx-padding: 6 20 6 20;");
            }
        });
    }

    // ─── Utilitaires ───────────────────────────────────────────────────────────
    private String getInitiales(Personne p) {
        String n  = (p.getNom()    != null && !p.getNom().isEmpty())    ? String.valueOf(p.getNom().charAt(0)).toUpperCase()    : "";
        String pr = (p.getPrenom() != null && !p.getPrenom().isEmpty()) ? String.valueOf(p.getPrenom().charAt(0)).toUpperCase() : "";
        return n + pr;
    }
}