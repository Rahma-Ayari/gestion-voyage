package Service;

import Entite.Personne;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ══════════════════════════════════════════════════════════════════
 *  ServicePersonne — avec support du champ ROLE
 *
 *  Commande SQL à exécuter UNE SEULE FOIS dans MySQL :
 *
 *    ALTER TABLE utilisateur
 *        ADD COLUMN role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER';
 *
 *  (Si statut n'existe pas encore :)
 *    ALTER TABLE utilisateur
 *        ADD COLUMN statut ENUM('EN_ATTENTE','APPROUVE','REFUSE')
 *        NOT NULL DEFAULT 'EN_ATTENTE';
 *
 * ══════════════════════════════════════════════════════════════════
 */
public class ServicePersonne implements IService<Personne> {

    private Connection con = DataSource.getInstance().getCon();

    // ─── SELECT commun avec role ───────────────────────────────────────────────
    private static final String SELECT_BASE =
            "SELECT u.id_utilisateur, u.email, u.motDePasse, u.dateInscription, " +
                    "       u.role, p.nom, p.prenom " +
                    "FROM utilisateur u " +
                    "JOIN personne p ON u.id_utilisateur = p.id_personne ";

    // ─────────────────────────────────────────────────────────────────
    //  AJOUTER
    // ─────────────────────────────────────────────────────────────────
    @Override
    public boolean ajouter(Personne p) throws SQLException {
        String sqlUser = "INSERT INTO utilisateur (email, motDePasse, dateInscription, statut, role) "
                + "VALUES (?, ?, ?, 'EN_ATTENTE', 'USER')";
        String sqlPers = "INSERT INTO personne (id_personne, nom, prenom) VALUES (?, ?, ?)";

        try {
            con.setAutoCommit(false);
            PreparedStatement stUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stUser.setString(1, p.getEmail());
            stUser.setString(2, p.getMotDePasse());
            stUser.setDate(3, new java.sql.Date(p.getDateInscription().getTime()));
            stUser.executeUpdate();

            ResultSet rs = stUser.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                p.setIdUtilisateur(generatedId);
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
    //  READ ALL
    // ─────────────────────────────────────────────────────────────────
    @Override
    public List<Personne> readAll() throws SQLException {
        List<Personne> liste = new ArrayList<>();
        String req = SELECT_BASE + "ORDER BY u.dateInscription DESC";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) liste.add(mapPersonne(rs));
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────────────────
    //  MODIFIER
    // ─────────────────────────────────────────────────────────────────
    @Override
    public boolean modifier(Personne p) throws SQLException {
        String req1 = "UPDATE utilisateur SET email=?, motDePasse=?, role=? WHERE id_utilisateur=?";
        String req2 = "UPDATE personne SET nom=?, prenom=? WHERE id_personne=?";
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(req1);
                 PreparedStatement ps2 = con.prepareStatement(req2)) {
                ps1.setString(1, p.getEmail());
                ps1.setString(2, p.getMotDePasse());
                ps1.setString(3, p.getRole() != null ? p.getRole() : "USER");
                ps1.setInt(4, p.getIdUtilisateur());
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
    //  SUPPRIMER
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
        String req = SELECT_BASE + "WHERE u.id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPersonne(rs);
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────
    //  AUTHENTIFICATION
    // ─────────────────────────────────────────────────────────────────
    public Personne authentifier(String email, String mdp) throws SQLException {
        String sql = SELECT_BASE.replace("FROM utilisateur u", "FROM utilisateur u")
                + "WHERE u.email = ? AND u.motDePasse = ?";
        // On reconstruit proprement :
        String sqlAuth =
                "SELECT u.id_utilisateur, u.email, u.motDePasse, u.dateInscription, " +
                        "       u.role, u.statut, p.nom, p.prenom " +
                        "FROM utilisateur u " +
                        "JOIN personne p ON u.id_utilisateur = p.id_personne " +
                        "WHERE u.email = ? AND u.motDePasse = ?";

        try (PreparedStatement ps = con.prepareStatement(sqlAuth)) {
            ps.setString(1, email);
            ps.setString(2, mdp);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String statut = rs.getString("statut");
                    if ("APPROUVE".equals(statut)) {
                        return mapPersonne(rs);
                    } else if ("EN_ATTENTE".equals(statut)) {
                        throw new SQLException("STATUT:EN_ATTENTE");
                    } else {
                        throw new SQLException("STATUT:REFUSE");
                    }
                }
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────
    //  MÉTHODES DASHBOARD ADMIN
    // ─────────────────────────────────────────────────────────────────

    public List<Personne> getPersonnesEnAttente() throws SQLException {
        return getByStatut("EN_ATTENTE", null);
    }

    public List<Personne> getPersonnesApprouvees() throws SQLException {
        return getByStatut("APPROUVE", null);
    }

    public List<Personne> getPersonnesRefusees() throws SQLException {
        return getByStatut("REFUSE", null);
    }

    /** Filtre optionnel par rôle : null = tous, "ADMIN" ou "USER" */
    public List<Personne> getPersonnesApprouveesByRole(String role) throws SQLException {
        return getByStatut("APPROUVE", role);
    }

    private List<Personne> getByStatut(String statut, String role) throws SQLException {
        List<Personne> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT u.id_utilisateur, u.email, u.motDePasse, u.dateInscription, " +
                        "       u.role, p.nom, p.prenom " +
                        "FROM utilisateur u " +
                        "JOIN personne p ON u.id_utilisateur = p.id_personne " +
                        "WHERE u.statut = ? ");
        if (role != null) sql.append("AND u.role = ? ");
        sql.append("ORDER BY u.dateInscription DESC");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setString(1, statut);
            if (role != null) ps.setString(2, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapPersonne(rs));
            }
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────────────────
    //  CHANGER ROLE
    // ─────────────────────────────────────────────────────────────────
    public void changerRole(int idUtilisateur, String role) throws SQLException {
        String sql = "UPDATE utilisateur SET role = ? WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, idUtilisateur);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  ACTIONS ADMIN
    // ─────────────────────────────────────────────────────────────────
    public void approuverInscription(int id) throws SQLException { changerStatut(id, "APPROUVE"); }
    public void refuserInscription(int id) throws SQLException   { changerStatut(id, "REFUSE"); }

    private void changerStatut(int id, String statut) throws SQLException {
        String sql = "UPDATE utilisateur SET statut = ? WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  UTILITAIRES
    // ─────────────────────────────────────────────────────────────────
    public boolean verifierEmailExiste(String email) throws SQLException {
        String sql = "SELECT id_utilisateur FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
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
    //  MAPPER ResultSet → Personne
    // ─────────────────────────────────────────────────────────────────
    private Personne mapPersonne(ResultSet rs) throws SQLException {
        String role = "USER";
        try { role = rs.getString("role"); } catch (SQLException ignored) {}
        return new Personne(
                rs.getInt("id_utilisateur"),
                rs.getString("email"),
                rs.getString("motDePasse"),
                rs.getDate("dateInscription"),
                rs.getString("nom"),
                rs.getString("prenom"),
                role
        );
    }
}