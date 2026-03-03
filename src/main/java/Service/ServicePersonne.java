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
        con.setAutoCommit(false);
        try {
            String req1 = "INSERT INTO utilisateur (email, motDePasse, dateInscription) VALUES (?, ?, ?)";
            PreparedStatement ps1 = con.prepareStatement(req1, Statement.RETURN_GENERATED_KEYS);
            ps1.setString(1, p.getEmail());
            ps1.setString(2, p.getMotDePasse());
            ps1.setDate(3, new java.sql.Date(p.getDateInscription().getTime()));
            ps1.executeUpdate();

            ResultSet rs = ps1.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                p.setIdUtilisateur(id);

                String req2 = "INSERT INTO personne (id_personne, nom, prenom) VALUES (?, ?, ?)";
                PreparedStatement ps2 = con.prepareStatement(req2);
                ps2.setInt(1, id);
                ps2.setString(2, p.getNom());
                ps2.setString(3, p.getPrenom());
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
        return false;
    }

    @Override
    public List<Personne> readAll() throws SQLException {
        List<Personne> liste = new ArrayList<>();
        String req = "SELECT u.*, p.nom, p.prenom FROM utilisateur u JOIN personne p ON u.id_utilisateur = p.id_personne";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                liste.add(new Personne(
                        rs.getInt("id_utilisateur"), rs.getString("email"), rs.getString("motDePasse"),
                        rs.getDate("dateInscription"), rs.getString("nom"), rs.getString("prenom")
                ));
            }
        }
        return liste;
    }

    @Override
    public boolean modifier(Personne p) throws SQLException {
        String req1 = "UPDATE utilisateur SET email=?, motDePasse=? WHERE id_utilisateur=?";
        String req2 = "UPDATE personne SET nom=?, prenom=? WHERE id_personne=?";

        try (PreparedStatement ps1 = con.prepareStatement(req1);
             PreparedStatement ps2 = con.prepareStatement(req2)) {
            ps1.setString(1, p.getEmail()); ps1.setString(2, p.getMotDePasse()); ps1.setInt(3, p.getIdUtilisateur());
            ps2.setString(1, p.getNom()); ps2.setString(2, p.getPrenom()); ps2.setInt(3, p.getIdUtilisateur());

            return ps1.executeUpdate() > 0 && ps2.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Personne p) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, p.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Personne findbyId(int id) throws SQLException {
        String req = "SELECT u.*, p.nom, p.prenom FROM utilisateur u JOIN personne p ON u.id_utilisateur = p.id_personne WHERE u.id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Personne(
                        rs.getInt("id_utilisateur"), rs.getString("email"), rs.getString("motDePasse"),
                        rs.getDate("dateInscription"), rs.getString("nom"), rs.getString("prenom")
                );
            }
        }
        return null;
    }
}