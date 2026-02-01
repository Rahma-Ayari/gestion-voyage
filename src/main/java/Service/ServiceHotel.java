package Service;

import Entite.Hotel;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHotel implements IService<Hotel> {
    private Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceHotel() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean ajouter(Hotel h) throws SQLException {
        boolean test = false;
        int res = -1;
        String req = "INSERT INTO `hotel` (`nom`, `ville`, `adresse`) VALUES ('" + h.getNom() + "', '" + h.getVille() + "', '" + h.getAdresse() + "');";
        res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }

    @Override
    public boolean supprimer(Hotel h) throws SQLException {
        boolean test = false;
        String req = "DELETE FROM hotel WHERE id_hotel = " + h.getIdHotel();
        int res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }


    @Override
    public boolean modifier(Hotel h) throws SQLException {
        boolean test = false;
        String req = "UPDATE hotel SET "
                + "nom = '" + h.getNom() + "', "
                + "ville = '" + h.getVille() + "', "
                + "adresse = '" + h.getAdresse() + "' "
                + "WHERE id_hotel = " + h.getIdHotel();

        int res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }


    @Override
    public Hotel findbyId(int id) throws SQLException {
        Hotel hotel = null;
        String req = "SELECT * FROM hotel WHERE id_hotel = " + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            hotel = new Hotel(
                    rs.getInt("id_hotel"),
                    rs.getString("nom"),
                    rs.getString("ville"),
                    rs.getString("adresse")
            );
        }
        return hotel;
    }


    @Override
    public List<Hotel> readAll() throws SQLException {
        List<Hotel> list = new ArrayList<>();
        String query = "SELECT * FROM `hotel`";
        ResultSet rest = st.executeQuery(query);
        while (rest.next()) {
            int id = rest.getInt(1);
            String nom = rest.getString("nom");
            String ville = rest.getString(3);
            String adresse = rest.getString("adresse");
            Hotel hotel = new Hotel(id, nom, ville, adresse);
            list.add(hotel);
        }
        return list;
    }
}
