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
        // Requête incluant les 10 colonnes (id est auto-incrémenté)
        String req = "INSERT INTO avis (id_utilisateur, note, titre, commentaire, note_hebergement, " +
                "note_transport, note_activites, note_qualite_prix, recommande, date_avis) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, a.getIdUtilisateur());
        ps.setInt(2, a.getNote());
        ps.setString(3, a.getTitre());
        ps.setString(4, a.getCommentaire());
        ps.setInt(5, a.getNoteHebergement());
        ps.setInt(6, a.getNoteTransport());
        ps.setInt(7, a.getNoteActivites());
        ps.setInt(8, a.getNoteQualitePrix());
        ps.setBoolean(9, a.isRecommande());
        ps.setDate(10, a.getDateAvis());

        int res = ps.executeUpdate();

        if (res > 0) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                a.setId(rs.getInt(1));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean modifier(Avis a) throws SQLException {
        String req = "UPDATE avis SET id_utilisateur = ?, note = ?, titre = ?, commentaire = ?, " +
                "note_hebergement = ?, note_transport = ?, note_activites = ?, " +
                "note_qualite_prix = ?, recommande = ?, date_avis = ? WHERE id = ?";

        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, a.getIdUtilisateur());
        ps.setInt(2, a.getNote());
        ps.setString(3, a.getTitre());
        ps.setString(4, a.getCommentaire());
        ps.setInt(5, a.getNoteHebergement());
        ps.setInt(6, a.getNoteTransport());
        ps.setInt(7, a.getNoteActivites());
        ps.setInt(8, a.getNoteQualitePrix());
        ps.setBoolean(9, a.isRecommande());
        ps.setDate(10, a.getDateAvis());
        ps.setInt(11, a.getId());

        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean supprimer(Avis a) throws SQLException {
        String req = "DELETE FROM avis WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, a.getId());
        return ps.executeUpdate() > 0;
    }

    @Override
    public List<Avis> readAll() throws SQLException {
        List<Avis> listeAvis = new ArrayList<>();
        String req = "SELECT * FROM avis";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            listeAvis.add(mapResultSetToAvis(rs));
        }
        return listeAvis;
    }

    @Override
    public Avis findbyId(int id) throws SQLException {
        String req = "SELECT * FROM avis WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToAvis(rs);
        }
        return null;
    }

    /**
     * Méthode utilitaire pour transformer une ligne de ResultSet en objet Avis
     * Cela évite de répéter le code dans readAll et findById
     */
    private Avis mapResultSetToAvis(ResultSet rs) throws SQLException {
        return new Avis(
                rs.getInt("id"),
                rs.getInt("id_utilisateur"),
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