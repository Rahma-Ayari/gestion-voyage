package Service;

import Entite.Administrateur;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAdministrateur implements IService<Administrateur> {

    private Connection con = DataSource.getInstance().getCon();

    @Override
    public boolean ajouter(Administrateur admin) throws SQLException {
        String req1 = "INSERT INTO personne (nom, prenom) VALUES (?, ?)";
        PreparedStatement ps1 = con.prepareStatement(req1, Statement.RETURN_GENERATED_KEYS);
        ps1.setString(1, admin.getNom());
        ps1.setString(2, admin.getPrenom());
        ps1.executeUpdate();

        ResultSet rs = ps1.getGeneratedKeys();
        if (rs.next()) {
            int idGenere = rs.getInt(1);
            admin.setIdPersonne(idGenere);

            String req2 = "INSERT INTO administrateur (id_admin, id_role) VALUES (?, ?)";
            PreparedStatement ps2 = con.prepareStatement(req2);
            ps2.setInt(1, idGenere);
            ps2.setInt(2, admin.getIdRole());
            return ps2.executeUpdate() > 0;
        }
        return false;
    }

    @Override
    public boolean supprimer(Administrateur admin) throws SQLException {
        String req = "DELETE FROM personne WHERE id_personne = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, admin.getIdPersonne());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Administrateur admin) throws SQLException {
        String req1 = "UPDATE personne SET nom = ?, prenom = ? WHERE id_personne = ?";
        PreparedStatement ps1 = con.prepareStatement(req1);
        ps1.setString(1, admin.getNom());
        ps1.setString(2, admin.getPrenom());
        ps1.setInt(3, admin.getIdPersonne());
        ps1.executeUpdate();

        String req2 = "UPDATE administrateur SET id_role = ? WHERE id_admin = ?";
        PreparedStatement ps2 = con.prepareStatement(req2);
        ps2.setInt(1, admin.getIdRole());
        ps2.setInt(2, admin.getIdPersonne());

        return ps2.executeUpdate() > 0;
    }

    @Override
    public List<Administrateur> readAll() throws SQLException {
        List<Administrateur> liste = new ArrayList<>();
        String req = "SELECT p.id_personne, p.nom, p.prenom, a.id_role " +
                "FROM personne p " +
                "INNER JOIN administrateur a ON p.id_personne = a.id_admin";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Administrateur admin = new Administrateur(
                    rs.getInt("id_personne"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getInt("id_role")
            );
            liste.add(admin);
        }
        return liste;
    }

    @Override
    public Administrateur findbyId(int id) throws SQLException {
        String req = "SELECT p.id_personne, p.nom, p.prenom, a.id_role " +
                "FROM personne p " +
                "JOIN administrateur a ON p.id_personne = a.id_admin " +
                "WHERE p.id_personne = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Administrateur(
                    rs.getInt("id_personne"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getInt("id_role")
            );
        }
        return null;
    }
}