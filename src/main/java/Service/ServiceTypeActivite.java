package Service;

import Entite.TypeActivite;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTypeActivite {

    private Connection connect;
    private Statement st;

    public ServiceTypeActivite() {
        connect = DataSource.getInstance().getCon();
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // Lire tous les types d'activité
    public List<TypeActivite> readAll() throws SQLException {
        List<TypeActivite> list = new ArrayList<>();
        String req = "SELECT * FROM type_activite";
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            TypeActivite t = new TypeActivite();
            t.setIdType(rs.getInt("id_type_activite"));
            t.setLibelle(rs.getString("libelle"));
            list.add(t);
        }

        return list;
    }

    // Ajouter un type d'activité
    public boolean ajouter(TypeActivite t) throws SQLException {
        String req = "INSERT INTO type_activite(libelle) VALUES(?)";
        PreparedStatement pst = connect.prepareStatement(req);
        pst.setString(1, t.getLibelle());
        return pst.executeUpdate() > 0;
    }

    // Supprimer un type d'activité
    public boolean supprimer(TypeActivite t) throws SQLException {
        String req = "DELETE FROM type_activite WHERE id_type_activite = ?";
        PreparedStatement pst = connect.prepareStatement(req);
        pst.setInt(1, t.getIdType());
        return pst.executeUpdate() > 0;
    }

    // Modifier un type d'activité
    public boolean modifier(TypeActivite t) throws SQLException {
        String req = "UPDATE type_activite SET libelle = ? WHERE id_type_activite = ?";
        PreparedStatement pst = connect.prepareStatement(req);
        pst.setString(1, t.getLibelle());
        pst.setInt(2, t.getIdType());
        return pst.executeUpdate() > 0;
    }

    // Chercher un type par id
    public TypeActivite findById(int id) throws SQLException {
        String req = "SELECT * FROM type_activite WHERE id_type_activite = ?";
        PreparedStatement pst = connect.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return new TypeActivite(rs.getInt("id_type_activite"), rs.getString("libelle"));
        }
        return null;
    }
}
