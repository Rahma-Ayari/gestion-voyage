package Service;

import Entite.Utilisateur;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtilisateur implements IService<Utilisateur> {
    protected Connection con = DataSource.getInstance().getCon();

    @Override
    public boolean ajouter(Utilisateur user) throws SQLException {
        String req = "INSERT INTO utilisateur (email, motDePasse, dateInscription) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getMotDePasse());
            ps.setDate(3, new java.sql.Date(user.getDateInscription().getTime()));

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
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                liste.add(new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateInscription")
                ));
            }
        }
        return liste;
    }

    @Override
    public boolean modifier(Utilisateur u) throws SQLException {
        String req = "UPDATE utilisateur SET email = ?, motDePasse = ? WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getMotDePasse());
            ps.setInt(3, u.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Utilisateur u) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, u.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Utilisateur findbyId(int id) throws SQLException {
        String req = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getDate("dateInscription")
                );
            }
        }
        return null;
    }
}