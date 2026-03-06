package Service;

import Entite.Destination;
import Entite.Vol;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class ServiceVol {

    private Connection connect;
    private Statement st;

    public ServiceVol() throws SQLException {
        connect = DataSource.getInstance().getCon();
        st = connect.createStatement();
    }
    public void deleteAll() throws SQLException {
        String query = "DELETE FROM vol";
        st.executeUpdate(query);
    }
    // Read all vols with full Destination and VilleDepart objects
    public List<Vol> readAll() throws SQLException {
        List<Vol> vols = new ArrayList<>();

        String query = "SELECT v.id_vol, v.numero_vol, v.compagnie, v.date_depart, v.date_arrivee, v.prix, " +
                "d1.id_destination AS dep_id, d1.ville AS dep_ville, d1.pays AS dep_pays, d1.description AS dep_desc, " +
                "d2.id_destination AS dest_id, d2.ville AS dest_ville, d2.pays AS dest_pays, d2.description AS dest_desc " +
                "FROM vol v " +
                "JOIN destination d1 ON v.ville_depart_id = d1.id_destination " +
                "JOIN destination d2 ON v.id_destination = d2.id_destination";

        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery(query);

        while(rs.next()) {
            Vol vol = new Vol();
            vol.setIdVol(rs.getInt("id_vol"));
            vol.setNumeroVol(rs.getString("numero_vol"));
            vol.setCompagnie(rs.getString("compagnie"));
            vol.setDateDepart(rs.getTimestamp("date_depart").toLocalDateTime());
            vol.setDateArrivee(rs.getTimestamp("date_arrivee").toLocalDateTime());
            vol.setPrix(rs.getDouble("prix"));

            Destination dep = new Destination();
            dep.setIdDestination(rs.getInt("dep_id"));
            dep.setVille(rs.getString("dep_ville"));
            dep.setPays(rs.getString("dep_pays"));
            dep.setDescription(rs.getString("dep_desc"));
            vol.setVilleDepart(dep);

            Destination dest = new Destination();
            dest.setIdDestination(rs.getInt("dest_id"));
            dest.setVille(rs.getString("dest_ville"));
            dest.setPays(rs.getString("dest_pays"));
            dest.setDescription(rs.getString("dest_desc"));
            vol.setDestination(dest);

            vols.add(vol);
        }

        rs.close();
        st.close();
        return vols;
    }
    public boolean ajouter(Vol v) {
        try {
            String query = "INSERT INTO vol (compagnie, date_depart, date_arrivee, prix, id_destination, ville_depart_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = connect.prepareStatement(query);

            ps.setString(1, v.getCompagnie());
            ps.setTimestamp(2, Timestamp.valueOf(v.getDateDepart()));
            ps.setTimestamp(3, Timestamp.valueOf(v.getDateArrivee()));
            ps.setDouble(4, v.getPrix());
            ps.setInt(5, v.getDestination().getIdDestination());
            ps.setInt(6, v.getVilleDepart().getIdDestination());

            ps.executeUpdate();
            ps.close();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Update an existing Vol
    public void modifier(Vol v) throws SQLException {
        String query = "UPDATE vol SET numero_vol = ?, compagnie = ?, date_depart = ?, date_arrivee = ?, prix = ?, " +
                "id_destination = ?, ville_depart_id = ? WHERE id_vol = ?";
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setString(1, v.getNumeroVol());
        ps.setString(2, v.getCompagnie());
        ps.setTimestamp(3, Timestamp.valueOf(v.getDateDepart()));
        ps.setTimestamp(4, Timestamp.valueOf(v.getDateArrivee()));
        ps.setDouble(5, v.getPrix());
        ps.setInt(6, v.getDestination().getIdDestination());
        ps.setInt(7, v.getVilleDepart().getIdDestination());
        ps.setInt(8, v.getIdVol());
        ps.executeUpdate();
        ps.close();
    }

    // Delete a Vol
    public void supprimer(Vol v) throws SQLException {
        String query = "DELETE FROM vol WHERE id_vol = ?";
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setInt(1, v.getIdVol());
        ps.executeUpdate();
        ps.close();
    }

    // Get all distinct airline companies
    public List<String> getAllCompanies() throws SQLException {
        List<String> companies = new ArrayList<>();
        String query = "SELECT DISTINCT compagnie FROM vol";
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery(query);
        while(rs.next()) {
            companies.add(rs.getString("compagnie"));
        }
        rs.close();
        st.close();
        return companies;
    }

    // Get all distinct destinations (arrival cities)
    public List<String> getAllDestinations() throws SQLException {
        List<String> destinations = new ArrayList<>();
        String query = "SELECT DISTINCT ville FROM destination d " +
                "JOIN vol v ON v.id_destination = d.id_destination";
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery(query);
        while(rs.next()) {
            destinations.add(rs.getString("ville"));
        }
        rs.close();
        st.close();
        return destinations;
    }

    // Get all distinct departure cities
    public List<String> getAllDepartures() throws SQLException {
        List<String> departures = new ArrayList<>();
        String query = "SELECT DISTINCT ville FROM destination d " +
                "JOIN vol v ON v.ville_depart_id = d.id_destination";
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery(query);
        while(rs.next()) {
            departures.add(rs.getString("ville"));
        }
        rs.close();
        st.close();
        return departures;
    }
    public int getDestinationIdByVille(String ville) throws SQLException {
        String query = "SELECT id_destination FROM destination WHERE ville = ?";
        PreparedStatement ps = connect.prepareStatement(query);
        ps.setString(1, ville);
        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            return rs.getInt("id_destination");
        }
        return -1;
    }

    public List<Vol> fetchFlightsFromAPI(String departureCity, String arrivalCity, LocalDateTime date) {
        System.out.println("Calling external Flight API...");
        System.out.println("Departure: " + departureCity);
        System.out.println("Arrival: " + arrivalCity);
        System.out.println("Date: " + date);

        List<Vol> apiFlights = new ArrayList<>();

        try {
            // In reality we reuse database data
            List<Vol> vols = readAll();

            for (Vol v : vols) {
                if (v.getVilleDepart().getVille().equalsIgnoreCase(departureCity)
                        && v.getDestination().getVille().equalsIgnoreCase(arrivalCity)) {
                    apiFlights.add(v);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return apiFlights;
    }


    public void syncFlightsFromAPI() {
        System.out.println("Synchronizing flights with external API...");
    }


    public List<String> fetchAirlinesFromAPI() {

        System.out.println("Fetching airlines from API...");

        try {
            return getAllCompanies();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }


    public List<String> fetchDestinationsFromAPI() {

        System.out.println("Fetching destinations from API...");

        try {
            return getAllDestinations();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
