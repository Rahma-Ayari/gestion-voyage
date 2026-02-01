package Service;

import Entite.Personne;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePersonne implements IService<Personne> {

    private Connection con = DataSource.getInstance().getCon();

    @Override
    public boolean ajouter(Personne p) throws SQLException {
        String req = "INSERT INTO personne (nom, prenom) VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, p.getNom());
        ps.setString(2, p.getPrenom());

        int affectedRows = ps.executeUpdate();

        if (affectedRows > 0) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                p.setIdPersonne(rs.getInt(1));
            }
            return true;
        }
        return false;
    }

    @Override
    public List<Personne> readAll() throws SQLException {
        List<Personne> liste = new ArrayList<>();
        String req = "SELECT * FROM personne";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            liste.add(new Personne(
                    rs.getInt("id_personne"),
                    rs.getString("nom"),
                    rs.getString("prenom")
            ));
        }
        return liste;
    }

    @Override
    public boolean supprimer(Personne p) throws SQLException {
        String req = "DELETE FROM personne WHERE id_personne = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, p.getIdPersonne());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Personne p) throws SQLException {
        String req = "UPDATE personne SET nom = ?, prenom = ? WHERE id_personne = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setString(1, p.getNom());
        ps.setString(2, p.getPrenom());
        ps.setInt(3, p.getIdPersonne());
        return ps.executeUpdate() > 0;
    }

    @Override
    public Personne findbyId(int id) throws SQLException {
        String req = "SELECT * FROM personne WHERE id_personne = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Personne(
                    rs.getInt("id_personne"),
                    rs.getString("nom"),
                    rs.getString("prenom")
            );
        }
        return null;
    }
}