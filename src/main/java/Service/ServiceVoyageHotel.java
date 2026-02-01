package Service;

import Entite.VoyageHotel;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceVoyageHotel implements IService<VoyageHotel> {

    private Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceVoyageHotel() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println("Erreur initialisation statement : " + e.getMessage());
        }
    }

    @Override
    public boolean ajouter(VoyageHotel vh) throws SQLException {
        boolean test = false;
        String req = "INSERT INTO voyage_hotel (id_voyage, id_hotel) VALUES ("
                + vh.getIdVoyage() + ", "
                + vh.getIdHotel() + ")";
        int res = st.executeUpdate(req);
        if (res > 0) test = true;
        return test;
    }

    @Override
    public boolean supprimer(VoyageHotel vh) throws SQLException {
        boolean test = false;
        String req = "DELETE FROM voyage_hotel WHERE id_voyage = " + vh.getIdVoyage()
                + " AND id_hotel = " + vh.getIdHotel();
        int res = st.executeUpdate(req);
        if (res > 0) test = true;
        return test;
    }

    @Override
    public boolean modifier(VoyageHotel vh) throws SQLException {
        // Table associative : modification non supportée
        throw new UnsupportedOperationException("Modification non supportée pour VoyageHotel.");
    }

    @Override
    public VoyageHotel findbyId(int id) throws SQLException {
        // Cherche la première occurence par id_voyage
        VoyageHotel vh = null;
        String req = "SELECT * FROM voyage_hotel WHERE id_voyage = " + id;
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            vh = new VoyageHotel(rs.getInt("id_voyage"), rs.getInt("id_hotel"));
        }
        return vh;
    }

    @Override
    public List<VoyageHotel> readAll() throws SQLException {
        List<VoyageHotel> list = new ArrayList<>();
        String req = "SELECT * FROM voyage_hotel";
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            VoyageHotel vh = new VoyageHotel();
            vh.setIdVoyage(rs.getInt("id_voyage"));
            vh.setIdHotel(rs.getInt("id_hotel"));
            list.add(vh);
        }
        return list;
    }
}
