package Service;

import Entite.Personne;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ══════════════════════════════════════════════════════════════════
 *  ServicePersonne — adapté à votre schéma réel TripEase
 *
 *  Tables existantes :
 *    utilisateur (id_utilisateur PK AUTO, email, motDePasse, dateInscription)
 *    personne    (id_personne FK → utilisateur, nom, prenom)
 *
 *  ⚠ UNE SEULE COMMANDE SQL à exécuter une fois dans MySQL :
 *
 *    ALTER TABLE utilisateur
 *        ADD COLUMN statut ENUM('EN_ATTENTE','APPROUVE','REFUSE')
 *        NOT NULL DEFAULT 'EN_ATTENTE';
 *
 * ══════════════════════════════════════════════════════════════════
 */
public class ServicePersonne implements IService<Personne> {

    private Connection con = DataSource.getInstance().getCon();

    // ─────────────────────────────────────────────────────────────────
    //  AJOUTER — Inscription utilisateur
    //  Insère avec statut 'EN_ATTENTE' → l'admin devra valider ensuite
    // ─────────────────────────────────────────────────────────────────
    @Override
    public boolean ajouter(Personne p) throws SQLException {
        // On ajoute maintenant la colonne statut = 'EN_ATTENTE' par défaut
        String sqlUser = "INSERT INTO utilisateur (email, motDePasse, dateInscription, statut) "
                + "VALUES (?, ?, ?, 'EN_ATTENTE')";
        String sqlPers = "INSERT INTO personne (id_personne, nom, prenom) VALUES (?, ?, ?)";

        try {
            con.setAutoCommit(false);

            // 1. Insertion dans la table UTILISATEUR
            PreparedStatement stUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stUser.setString(1, p.getEmail());
            stUser.setString(2, p.getMotDePasse());
            stUser.setDate(3, new java.sql.Date(p.getDateInscription().getTime()));
            stUser.executeUpdate();

            // 2. Récupération de l'ID généré
            ResultSet rs = stUser.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                p.setIdUtilisateur(generatedId);

                // 3. Insertion dans la table PERSONNE avec le même ID
                PreparedStatement stPers = con.prepareStatement(sqlPers);
                stPers.setInt(1, generatedId);
                stPers.setString(2, p.getNom());
                stPers.setString(3, p.getPrenom());
                stPers.executeUpdate();

                con.commit();
                return true;
            }
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────
    //  READ ALL — Toutes les personnes (toutes tables jointes)
    // ─────────────────────────────────────────────────────────────────
    @Override
    public List<Personne> readAll() throws SQLException {
        List<Personne> liste = new ArrayList<>();
        String req = "SELECT u.id_utilisateur, u.email, u.motDePasse, u.dateInscription, "
                + "       p.nom, p.prenom "
                + "FROM utilisateur u "
                + "JOIN personne p ON u.id_utilisateur = p.id_personne "
                + "ORDER BY u.dateInscription DESC";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                liste.add(mapPersonne(rs));
            }
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────────────────
    //  MODIFIER
    // ─────────────────────────────────────────────────────────────────
    @Override
    public boolean modifier(Personne p) throws SQLException {
        String req1 = "UPDATE utilisateur SET email=?, motDePasse=? WHERE id_utilisateur=?";
        String req2 = "UPDATE personne SET nom=?, prenom=? WHERE id_personne=?";
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(req1);
                 PreparedStatement ps2 = con.prepareStatement(req2)) {
                ps1.setString(1, p.getEmail());
                ps1.setString(2, p.getMotDePasse());
                ps1.setInt(3, p.getIdUtilisateur());
                ps2.setString(1, p.getNom());
                ps2.setString(2, p.getPrenom());
                ps2.setInt(3, p.getIdUtilisateur());
                ps1.executeUpdate();
                ps2.executeUpdate();
                con.commit();
                return true;
            }
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  SUPPRIMER — ON DELETE CASCADE gère personne automatiquement
    // ─────────────────────────────────────────────────────────────────
    @Override
    public boolean supprimer(Personne p) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, p.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  FIND BY ID
    // ─────────────────────────────────────────────────────────────────
    @Override
    public Personne findbyId(int id) throws SQLException {
        String req = "SELECT u.id_utilisateur, u.email, u.motDePasse, u.dateInscription, "
                + "p.nom, p.prenom "
                + "FROM utilisateur u "
                + "JOIN personne p ON u.id_utilisateur = p.id_personne "
                + "WHERE u.id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPersonne(rs);
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────
    //  AUTHENTIFICATION — utilisée par ControllerLogin
    //  ✔ Autorise uniquement statut = 'APPROUVE'
    //  ✘ Lance une SQLException codée pour EN_ATTENTE et REFUSE
    // ─────────────────────────────────────────────────────────────────
    public Personne authentifier(String email, String mdp) throws SQLException {
        String sql = "SELECT u.id_utilisateur, u.email, u.motDePasse, u.dateInscription, "
                + "       p.nom, p.prenom, u.statut "
                + "FROM utilisateur u "
                + "JOIN personne p ON u.id_utilisateur = p.id_personne "
                + "WHERE u.email = ? AND u.motDePasse = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, mdp);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String statut = rs.getString("statut");
                    if ("APPROUVE".equals(statut)) {
                        return mapPersonne(rs);
                    } else if ("EN_ATTENTE".equals(statut)) {
                        // Message codé récupéré dans ControllerLogin
                        throw new SQLException("STATUT:EN_ATTENTE");
                    } else {
                        throw new SQLException("STATUT:REFUSE");
                    }
                }
            }
        }
        return null; // Email ou mot de passe incorrect
    }

    // ─────────────────────────────────────────────────────────────────
    //  MÉTHODES DASHBOARD ADMIN
    //  Lecture par statut pour les 3 sections du tableau de bord
    // ─────────────────────────────────────────────────────────────────

    /** Section "Utilisateurs en attente" */
    public List<Personne> getPersonnesEnAttente() throws SQLException {
        return getByStatut("EN_ATTENTE");
    }

    /** Section "Utilisateurs approuvés" */
    public List<Personne> getPersonnesApprouvees() throws SQLException {
        return getByStatut("APPROUVE");
    }

    /** Section "Utilisateurs refusés" */
    public List<Personne> getPersonnesRefusees() throws SQLException {
        return getByStatut("REFUSE");
    }

    private List<Personne> getByStatut(String statut) throws SQLException {
        List<Personne> liste = new ArrayList<>();
        String sql = "SELECT u.id_utilisateur, u.email, u.motDePasse, u.dateInscription, "
                + "       p.nom, p.prenom "
                + "FROM utilisateur u "
                + "JOIN personne p ON u.id_utilisateur = p.id_personne "
                + "WHERE u.statut = ? "
                + "ORDER BY u.dateInscription DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapPersonne(rs));
            }
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────────────────
    //  ACTIONS ADMIN — Approuver / Refuser
    // ─────────────────────────────────────────────────────────────────

    /**
     * Admin APPROUVE → statut = 'APPROUVE'
     * L'utilisateur pourra désormais se connecter.
     */
    public void approuverInscription(int idUtilisateur) throws SQLException {
        changerStatut(idUtilisateur, "APPROUVE");
    }

    /**
     * Admin REFUSE → statut = 'REFUSE'
     * Les données restent en base pour traçabilité
     * mais la connexion sera bloquée définitivement.
     */
    public void refuserInscription(int idUtilisateur) throws SQLException {
        changerStatut(idUtilisateur, "REFUSE");
    }

    private void changerStatut(int id, String statut) throws SQLException {
        String sql = "UPDATE utilisateur SET statut = ? WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  MÉTHODES UTILITAIRES (conservées de votre code d'origine)
    // ─────────────────────────────────────────────────────────────────

    public boolean verifierEmailExiste(String email) throws SQLException {
        String sql = "SELECT id_utilisateur FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updatePassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE utilisateur SET motDePasse = ? WHERE email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  MAPPER ResultSet → Personne (constructeur existant conservé)
    // ─────────────────────────────────────────────────────────────────
    private Personne mapPersonne(ResultSet rs) throws SQLException {
        return new Personne(
                rs.getInt("id_utilisateur"),
                rs.getString("email"),
                rs.getString("motDePasse"),
                rs.getDate("dateInscription"),
                rs.getString("nom"),
                rs.getString("prenom")
        );
    }
}