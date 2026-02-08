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
        // Requêtes SQL
        String sqlUser = "INSERT INTO utilisateur (email, motDePasse, dateInscription) VALUES (?, ?, ?)";
        String sqlPers = "INSERT INTO personne (id_personne, nom, prenom) VALUES (?, ?, ?)";

        try {
            // Étape importante : on désactive l'enregistrement automatique
            con.setAutoCommit(false);

            // 1. On insère dans la table UTILISATEUR d'abord
            PreparedStatement stUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stUser.setString(1, p.getEmail());
            stUser.setString(2, p.getMotDePasse());
            stUser.setDate(3, new java.sql.Date(p.getDateInscription().getTime()));
            stUser.executeUpdate();

            // On récupère l'ID que la base de données vient de créer
            ResultSet rs = stUser.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);

                // 2. On insère dans la table PERSONNE en utilisant le MÊME ID
                PreparedStatement stPers = con.prepareStatement(sqlPers);
                stPers.setInt(1, generatedId);
                stPers.setString(2, p.getNom());
                stPers.setString(3, p.getPrenom());
                stPers.executeUpdate();

                // Si on arrive ici, tout est bon : on valide !
                con.commit();
                return true;
            }
        } catch (SQLException e) {
            // Si quelque chose plante, on annule tout (rollback)
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
        // On joint les deux tables pour avoir les infos complètes
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
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(req1);
                 PreparedStatement ps2 = con.prepareStatement(req2)) {
                ps1.setString(1, p.getEmail()); ps1.setString(2, p.getMotDePasse()); ps1.setInt(3, p.getIdUtilisateur());
                ps2.setString(1, p.getNom()); ps2.setString(2, p.getPrenom()); ps2.setInt(3, p.getIdUtilisateur());

                ps1.executeUpdate();
                ps2.executeUpdate();
                con.commit();
                return true;
            }
        } catch (SQLException e) {
            con.rollback();
            throw e;
        }
    }

    @Override
    public boolean supprimer(Personne p) throws SQLException {
        // Comme il y a une contrainte ON DELETE CASCADE, supprimer l'utilisateur supprimera la personne automatiquement
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

    /**
     * Vérifie les identifiants et retourne un objet Personne si valide
     */
    public Personne authentifier(String email, String mdp) throws SQLException {
        // On joint utilisateur et personne pour récupérer l'objet complet après connexion
        String sql = "SELECT u.*, p.nom, p.prenom FROM utilisateur u " +
                "JOIN personne p ON u.id_utilisateur = p.id_personne " +
                "WHERE u.email = ? AND u.motDePasse = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, mdp); // Note: En production, on comparerait des mots de passe hachés

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Si une ligne correspond, on crée et retourne l'objet Personne
                    return new Personne(
                            rs.getInt("id_utilisateur"),
                            rs.getString("email"),
                            rs.getString("motDePasse"),
                            rs.getDate("dateInscription"),
                            rs.getString("nom"),
                            rs.getString("prenom")
                    );
                }
            }
        }
        // Si aucun utilisateur ne correspond
        return null;
    }

    // Vérifie si l'email existe
    public boolean verifierEmailExiste(String email) throws SQLException {
        String sql = "SELECT id_utilisateur FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Met à jour le mot de passe
    public boolean updatePassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE utilisateur SET motDePasse = ? WHERE email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        }
    }
}