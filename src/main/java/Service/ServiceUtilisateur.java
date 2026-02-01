package Service;

import Entite.Utilisateur;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtilisateur implements IService<Utilisateur> {
    private Connection con = DataSource.getInstance().getCon();

    @Override
    public boolean ajouter(Utilisateur user) throws SQLException {
        String req1 = "INSERT INTO personne (nom, prenom) VALUES (?, ?)";
        PreparedStatement ps1 = con.prepareStatement(req1, Statement.RETURN_GENERATED_KEYS);
        ps1.setString(1, user.getNom());
        ps1.setString(2, user.getPrenom());
        ps1.executeUpdate();

        ResultSet rs = ps1.getGeneratedKeys();
        if (rs.next()) {
            int idGenere = rs.getInt(1);
            user.setIdPersonne(idGenere);

            String req2 = "INSERT INTO utilisateur (id_utilisateur, email, motDePasse, dateInscription) VALUES (?, ?, ?, ?)";
            PreparedStatement ps2 = con.prepareStatement(req2);
            ps2.setInt(1, idGenere);
            ps2.setString(2, user.getEmail());
            ps2.setString(3, user.getMotDePasse());
            ps2.setDate(4, new java.sql.Date(user.getDateInscription().getTime()));
            return ps2.executeUpdate() > 0;
        }
        return false;
    }

    @Override
    public List<Utilisateur> readAll() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String req = "SELECT p.id_personne, p.nom, p.prenom, u.email, u.motDePasse, u.dateInscription " +
                "FROM personne p INNER JOIN utilisateur u ON p.id_personne = u.id_utilisateur";
        ResultSet rs = con.createStatement().executeQuery(req);
        while (rs.next()) {
            liste.add(new Utilisateur(rs.getInt("id_personne"), rs.getString("nom"), rs.getString("prenom"),
                    rs.getString("email"), rs.getString("motDePasse"), rs.getDate("dateInscription")));
        }
        return liste;
    }

    @Override public boolean supprimer(Utilisateur u) throws SQLException {
        String req = "DELETE FROM personne WHERE id_personne=?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, u.getIdPersonne());
        return ps.executeUpdate() > 0;
    }

    @Override public boolean modifier(Utilisateur u) throws SQLException {
        String req1 = "UPDATE personne SET nom=?, prenom=? WHERE id_personne=?";
        PreparedStatement ps1 = con.prepareStatement(req1);
        ps1.setString(1, u.getNom()); ps1.setString(2, u.getPrenom()); ps1.setInt(3, u.getIdPersonne());
        ps1.executeUpdate();

        String req2 = "UPDATE utilisateur SET email=?, motDePasse=? WHERE id_utilisateur=?";
        PreparedStatement ps2 = con.prepareStatement(req2);
        ps2.setString(1, u.getEmail()); ps2.setString(2, u.getMotDePasse()); ps2.setInt(3, u.getIdPersonne());
        return ps2.executeUpdate() > 0;
    }

    @Override public Utilisateur findbyId(int id) throws SQLException {
        String req = "SELECT p.*, u.* FROM personne p JOIN utilisateur u ON p.id_personne = u.id_utilisateur WHERE p.id_personne=?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return new Utilisateur(rs.getInt("id_personne"), rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("motDePasse"), rs.getDate("dateInscription"));
        return null;
    }
}