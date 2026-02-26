package Service;

import Entite.Avis;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAvis implements IService<Avis> {

    private Connection con = DataSource.getInstance().getCon();

    @Override
    public boolean ajouter(Avis a) throws SQLException {
        String req = "INSERT INTO avis (id_utilisateur, id_voyage, note, titre, commentaire, " +
                "note_hebergement, note_transport, note_activites, note_qualite_prix, " +
                "recommande, date_avis) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,     a.getIdUtilisateur());
            ps.setInt(2,     a.getIdVoyage());
            ps.setInt(3,     a.getNote());
            ps.setString(4,  a.getTitre());
            ps.setString(5,  a.getCommentaire());
            ps.setInt(6,     a.getNoteHebergement());
            ps.setInt(7,     a.getNoteTransport());
            ps.setInt(8,     a.getNoteActivites());
            ps.setInt(9,     a.getNoteQualitePrix());
            ps.setBoolean(10, a.isRecommande());
            ps.setDate(11,   a.getDateAvis());

            int res = ps.executeUpdate();
            if (res > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) a.setId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean modifier(Avis a) throws SQLException {
        String req = "UPDATE avis SET id_utilisateur=?, id_voyage=?, note=?, titre=?, commentaire=?, " +
                "note_hebergement=?, note_transport=?, note_activites=?, " +
                "note_qualite_prix=?, recommande=?, date_avis=? WHERE id=?";

        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1,     a.getIdUtilisateur());
            ps.setInt(2,     a.getIdVoyage());
            ps.setInt(3,     a.getNote());
            ps.setString(4,  a.getTitre());
            ps.setString(5,  a.getCommentaire());
            ps.setInt(6,     a.getNoteHebergement());
            ps.setInt(7,     a.getNoteTransport());
            ps.setInt(8,     a.getNoteActivites());
            ps.setInt(9,     a.getNoteQualitePrix());
            ps.setBoolean(10, a.isRecommande());
            ps.setDate(11,   a.getDateAvis());
            ps.setInt(12,    a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Avis a) throws SQLException {
        String req = "DELETE FROM avis WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Avis> readAll() throws SQLException {
        List<Avis> liste = new ArrayList<>();
        String req = "SELECT * FROM avis";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) liste.add(mapResultSetToAvis(rs));
        }
        return liste;
    }

    /** Récupère tous les avis d'un utilisateur donné */
    public List<Avis> findByUtilisateur(int idUtilisateur) throws SQLException {
        List<Avis> liste = new ArrayList<>();
        String req = "SELECT * FROM avis WHERE id_utilisateur=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapResultSetToAvis(rs));
            }
        }
        return liste;
    }

    /** Récupère tous les avis pour un voyage donné */
    public List<Avis> findByVoyage(int idVoyage) throws SQLException {
        List<Avis> liste = new ArrayList<>();
        String req = "SELECT * FROM avis WHERE id_voyage=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, idVoyage);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapResultSetToAvis(rs));
            }
        }
        return liste;
    }

    @Override
    public Avis findbyId(int id) throws SQLException {
        String req = "SELECT * FROM avis WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToAvis(rs);
            }
        }
        return null;
    }

    private Avis mapResultSetToAvis(ResultSet rs) throws SQLException {
        return new Avis(
                rs.getInt("id"),
                rs.getInt("id_utilisateur"),
                rs.getInt("id_voyage"),
                rs.getInt("note"),
                rs.getString("titre"),
                rs.getString("commentaire"),
                rs.getInt("note_hebergement"),
                rs.getInt("note_transport"),
                rs.getInt("note_activites"),
                rs.getInt("note_qualite_prix"),
                rs.getBoolean("recommande"),
                rs.getDate("date_avis")
        );
    }
}