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
        String req = "INSERT INTO hotel (nom, ville, adresse, stars, capacite, type_chambre, prix_par_nuit, disponibilite) VALUES ("
                + "'" + h.getNom() + "', "
                + "'" + h.getVille() + "', "
                + "'" + h.getAdresse() + "', "
                + h.getStars() + ", "
                + h.getCapacite() + ", "
                + "'" + h.getTypeChambre() + "', "
                + h.getPrixParNuit() + ", "
                + (h.isDisponibilite() ? 1 : 0) + ");";

        int res = st.executeUpdate(req);
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
                + "adresse = '" + h.getAdresse() + "', "
                + "stars = " + h.getStars() + ", "
                + "capacite = " + h.getCapacite() + ", "
                + "type_chambre = '" + h.getTypeChambre() + "', "
                + "prix_par_nuit = " + h.getPrixParNuit() + ", "
                + "disponibilite = " + (h.isDisponibilite() ? 1 : 0) + " "
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
                    rs.getString("adresse"),
                    rs.getInt("stars"),
                    rs.getInt("capacite"),
                    rs.getString("type_chambre"),
                    rs.getDouble("prix_par_nuit"),
                    rs.getBoolean("disponibilite")
            );
        }
        return hotel;
    }

    @Override
    public List<Hotel> readAll() throws SQLException {
        List<Hotel> list = new ArrayList<>();
        String query = "SELECT * FROM hotel";
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Hotel hotel = new Hotel(
                    rs.getInt("id_hotel"),
                    rs.getString("nom"),
                    rs.getString("ville"),
                    rs.getString("adresse"),
                    rs.getInt("stars"),
                    rs.getInt("capacite"),
                    rs.getString("type_chambre"),
                    rs.getDouble("prix_par_nuit"),
                    rs.getBoolean("disponibilite")
            );
            list.add(hotel);
        }
        return list;
    }
}