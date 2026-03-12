package Service;

import Entite.Utilisateur;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtilisateur implements IService<Utilisateur> {

    protected Connection con = DataSource.getInstance().getCon();

    // ── Mapper central ────────────────────────────────────────────────────────
    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(rs.getInt("id_utilisateur"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("motDePasse"));
        u.setDateInscription(rs.getDate("dateInscription"));

        // id_voyage — peut être NULL si pas encore configuré
        int idVoyage = rs.getInt("id_voyage");
        if (!rs.wasNull()) u.setIdVoyage(idVoyage);

        // etat — "done" ou "notDone", jamais NULL grâce au DEFAULT
        String etat = rs.getString("etat");
        u.setEtat(etat != null ? etat : "notDone");

        return u;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public boolean ajouter(Utilisateur user) throws SQLException {
        String req = "INSERT INTO utilisateur "
                + "(email, motDePasse, dateInscription) VALUES (?, ?, ?)";
        try (PreparedStatement ps =
                     con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getMotDePasse());
            ps.setDate  (3, new java.sql.Date(user.getDateInscription().getTime()));
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) user.setIdUtilisateur(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Utilisateur> readAll() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";
        try (Statement  st = con.createStatement();
             ResultSet  rs = st.executeQuery(req)) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public boolean modifier(Utilisateur u) throws SQLException {
        String req = "UPDATE utilisateur SET email=?, motDePasse=? "
                + "WHERE id_utilisateur=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getMotDePasse());
            ps.setInt   (3, u.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Utilisateur u) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id_utilisateur=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, u.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Utilisateur findbyId(int id) throws SQLException {
        String req = "SELECT * FROM utilisateur WHERE id_utilisateur=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── Méthodes métier ───────────────────────────────────────────────────────

    /**
     * Appelée à la fin de la configuration du voyage.
     * Enregistre l'id_voyage et initialise l'état à "notDone".
     */
    public boolean mettreAJourVoyage(int idUtilisateur, int idVoyage)
            throws SQLException {
        String req = "UPDATE utilisateur "
                + "SET id_voyage=?, etat='notDone' "
                + "WHERE id_utilisateur=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, idVoyage);
            ps.setInt(2, idUtilisateur);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Appelée par l'admin pour changer l'état : "done" ou "notDone".
     */
    public boolean mettreAJourEtat(int idUtilisateur, String etat)
            throws SQLException {
        String req = "UPDATE utilisateur SET etat=? WHERE id_utilisateur=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, etat);
            ps.setInt   (2, idUtilisateur);
            return ps.executeUpdate() > 0;
        }
    }
}