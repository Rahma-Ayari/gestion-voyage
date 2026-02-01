package Service;

import Entite.VoyageActivite;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceVoyageActivite implements IService<VoyageActivite> {

    private Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceVoyageActivite() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println("Erreur initialisation statement : " + e.getMessage());
        }
    }

    @Override
    public boolean ajouter(VoyageActivite va) throws SQLException {
        boolean test = false;
        String req = "INSERT INTO voyage_activite (id_voyage, id_activite) VALUES ("
                + va.getIdVoyage() + ", "
                + va.getIdActivite() + ")";
        int res = st.executeUpdate(req);
        if (res > 0) test = true;
        return test;
    }

    @Override
    public boolean supprimer(VoyageActivite va) throws SQLException {
        boolean test = false;
        String req = "DELETE FROM voyage_activite WHERE id_voyage = " + va.getIdVoyage()
                + " AND id_activite = " + va.getIdActivite();
        int res = st.executeUpdate(req);
        if (res > 0) test = true;
        return test;
    }

    @Override
    public boolean modifier(VoyageActivite va) throws SQLException {
        // Table associative : pas de modification directe possible
        throw new UnsupportedOperationException("Modification non supportée pour VoyageActivite.");
    }

    @Override
    public VoyageActivite findbyId(int id) throws SQLException {
        // Ici, on cherche la première occurence où id_voyage = id
        VoyageActivite va = null;
        String req = "SELECT * FROM voyage_activite WHERE id_voyage = " + id;
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            va = new VoyageActivite(rs.getInt("id_voyage"), rs.getInt("id_activite"));
        }
        return va;
    }

    @Override
    public List<VoyageActivite> readAll() throws SQLException {
        List<VoyageActivite> list = new ArrayList<>();
        String req = "SELECT * FROM voyage_activite";
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            VoyageActivite va = new VoyageActivite();
            va.setIdVoyage(rs.getInt("id_voyage"));
            va.setIdActivite(rs.getInt("id_activite"));
            list.add(va);
        }
        return list;
    }
}
