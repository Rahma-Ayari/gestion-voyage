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
        String req1 = "INSERT INTO utilisateur (email, motDePasse, dateInscription) VALUES (?, ?, ?)";
        try (PreparedStatement ps1 = con.prepareStatement(req1, Statement.RETURN_GENERATED_KEYS)) {
            ps1.setString(1, admin.getEmail());
            ps1.setString(2, admin.getMotDePasse());
            ps1.setDate(3, new java.sql.Date(admin.getDateInscription().getTime()));
            ps1.executeUpdate();

            try (ResultSet rs = ps1.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    admin.setIdUtilisateur(id);

                    String req2 = "INSERT INTO administrateur (id_admin) VALUES (?)";
                    try (PreparedStatement ps2 = con.prepareStatement(req2)) {
                        ps2.setInt(1, id);
                        return ps2.executeUpdate() > 0;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<Administrateur> readAll() throws SQLException {
        List<Administrateur> liste = new ArrayList<>();
        String req = "SELECT u.* FROM utilisateur u JOIN administrateur a ON u.id_utilisateur = a.id_admin";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                liste.add(new Administrateur(
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
    public boolean modifier(Administrateur admin) throws SQLException {
        String req = "UPDATE utilisateur SET email=?, motDePasse=? WHERE id_utilisateur=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, admin.getEmail());
            ps.setString(2, admin.getMotDePasse());
            ps.setInt(3, admin.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Administrateur admin) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, admin.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Administrateur findbyId(int id) throws SQLException {
        String req = "SELECT u.* FROM utilisateur u JOIN administrateur a ON u.id_utilisateur = a.id_admin WHERE u.id_utilisateur = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Administrateur(
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