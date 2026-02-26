package Service;

import Entite.Hotel;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHotel implements IService<Hotel> {

    private Connection connect = DataSource.getInstance().getCon();
    private Statement  st;

    public ServiceHotel() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════════════════════

    @Override
    public boolean ajouter(Hotel h) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "INSERT INTO hotel " +
                        "(nom, ville, adresse, stars, capacite, type_chambre, prix_par_nuit, disponibilite, id_destination) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString (1, h.getNom());
        ps.setString (2, h.getVille());
        ps.setString (3, h.getAdresse());
        ps.setInt    (4, h.getStars());
        ps.setInt    (5, h.getCapacite());
        ps.setString (6, h.getTypeChambre());
        ps.setDouble (7, h.getPrixParNuit());
        ps.setBoolean(8, h.isDisponibilite());
        ps.setInt    (9, h.getIdDestination());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean supprimer(Hotel h) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "DELETE FROM hotel WHERE id_hotel = ?");
        ps.setInt(1, h.getIdHotel());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Hotel h) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "UPDATE hotel SET " +
                        "nom = ?, ville = ?, adresse = ?, stars = ?, capacite = ?, " +
                        "type_chambre = ?, prix_par_nuit = ?, disponibilite = ?, id_destination = ? " +
                        "WHERE id_hotel = ?");
        ps.setString (1, h.getNom());
        ps.setString (2, h.getVille());
        ps.setString (3, h.getAdresse());
        ps.setInt    (4, h.getStars());
        ps.setInt    (5, h.getCapacite());
        ps.setString (6, h.getTypeChambre());
        ps.setDouble (7, h.getPrixParNuit());
        ps.setBoolean(8, h.isDisponibilite());
        ps.setInt    (9, h.getIdDestination());
        ps.setInt    (10, h.getIdHotel());
        return ps.executeUpdate() > 0;
    }

    @Override
    public Hotel findbyId(int id) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "SELECT * FROM hotel WHERE id_hotel = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapHotel(rs);
        return null;
    }

    @Override
    public List<Hotel> readAll() throws SQLException {
        List<Hotel> list = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT * FROM hotel ORDER BY nom ASC");
        while (rs.next()) list.add(mapHotel(rs));
        return list;
    }

    // ══════════════════════════════════════════════════════════════
    //  REQUÊTES MÉTIER
    // ══════════════════════════════════════════════════════════════

    /**
     * Tous les hôtels d'une ville (disponibles + complets).
     * Le filtrage disponibilité / étoiles / nom se fait côté HotelController.
     */
    public List<Hotel> findByVille(String ville) throws SQLException {
        List<Hotel> list = new ArrayList<>();
        PreparedStatement ps = connect.prepareStatement(
                "SELECT * FROM hotel WHERE ville = ? ORDER BY stars DESC, nom ASC");
        ps.setString(1, ville);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapHotel(rs));
        return list;
    }

    /**
     * Tous les hôtels liés à une destination (disponibles + complets).
     * Utilisé par HotelController — le filtrage se fait en mémoire.
     */
    public List<Hotel> findByDestination(int idDestination) throws SQLException {
        List<Hotel> list = new ArrayList<>();
        PreparedStatement ps = connect.prepareStatement(
                "SELECT * FROM hotel WHERE id_destination = ? ORDER BY stars DESC, nom ASC");
        ps.setInt(1, idDestination);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapHotel(rs));
        return list;
    }

    // ══════════════════════════════════════════════════════════════
    //  MAPPING  ResultSet → Hotel
    // ══════════════════════════════════════════════════════════════

    private Hotel mapHotel(ResultSet rs) throws SQLException {
        return new Hotel(
                rs.getInt    ("id_hotel"),
                rs.getString ("nom"),
                rs.getString ("ville"),
                rs.getString ("adresse"),
                rs.getInt    ("stars"),
                rs.getInt    ("capacite"),
                rs.getString ("type_chambre"),
                rs.getDouble ("prix_par_nuit"),
                rs.getBoolean("disponibilite"),
                rs.getInt    ("id_destination")
        );
    }
}