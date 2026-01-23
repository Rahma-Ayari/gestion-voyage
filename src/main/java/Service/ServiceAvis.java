package Service;

import Entite.Avis;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAvis implements IService <Avis> {

    private Connection con = DataSource.getInstance().getCon();

    @Override
    public boolean ajouter(Avis a) throws SQLException {
        String req = "INSERT INTO avis (note, commentaire, date_avis, id_utilisateur) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, a.getNote());
        ps.setString(2, a.getCommentaire());
        ps.setDate(3, a.getDateAvis());
        ps.setInt(4, a.getIdUtilisateur());

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
    public boolean supprimer(Avis a) throws SQLException {
        // La colonne s'appelle bien 'id' dans votre SQL
        String req = "DELETE FROM avis WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, a.getId());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Avis a) throws SQLException {
        String req = "UPDATE avis SET note = ?, commentaire = ?, date_avis = ?, id_utilisateur = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, a.getNote());
        ps.setString(2, a.getCommentaire());
        ps.setDate(3, a.getDateAvis());
        ps.setInt(4, a.getIdUtilisateur());
        ps.setInt(5, a.getId());
        return ps.executeUpdate() > 0;
    }

    @Override
    public List<Avis> readAll() throws SQLException {
        List<Avis> listeAvis = new ArrayList<>();
        String req = "SELECT * FROM avis";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            listeAvis.add(new Avis(
                    rs.getInt("id"),
                    rs.getInt("note"),
                    rs.getString("commentaire"),
                    rs.getDate("date_avis"),
                    rs.getInt("id_utilisateur")
            ));
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
            return new Avis(
                    rs.getInt("id"),
                    rs.getInt("note"),
                    rs.getString("commentaire"),
                    rs.getDate("date_avis"),
                    rs.getInt("id_utilisateur")
            );
        }
        return null;
    }
}