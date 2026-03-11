package Service;

import Entite.Destination;
import Entite.Vol;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceVol implements IService<Vol> {

    private final Connection connect = DataSource.getInstance().getCon();
    private Statement st;


    public ServiceVol() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean ajouter(Vol v) throws SQLException {
        if(flightExists(v.getNumeroVol(), v.getDateDepart()))
            return false;
        String req = "INSERT INTO vol (numero_vol, compagnie, date_depart, date_arrivee, prix, id_destination, type_vol) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ps.setString(1, v.getNumeroVol());
            ps.setString(2, v.getCompagnie());
            ps.setTimestamp(3, Timestamp.valueOf(v.getDateDepart()));
            ps.setTimestamp(4, Timestamp.valueOf(v.getDateArrivee()));
            ps.setDouble(5, v.getPrix());
            ps.setInt(6, v.getDestination().getIdDestination());
            ps.setString(7, v.getTypeVol());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(Vol v) throws SQLException {
        String req = "DELETE FROM vol WHERE id_vol = ?";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ps.setInt(1, v.getIdVol());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Vol v) throws SQLException {
        String req = "UPDATE vol SET numero_vol=?, compagnie=?, date_depart=?, "
                + "date_arrivee=?, prix=?, id_destination=?, type_vol=? "
                + "WHERE id_vol=?";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ps.setString(1, v.getNumeroVol());
            ps.setString(2, v.getCompagnie());
            ps.setTimestamp(3, Timestamp.valueOf(v.getDateDepart()));
            ps.setTimestamp(4, Timestamp.valueOf(v.getDateArrivee()));
            ps.setDouble(5, v.getPrix());
            ps.setInt(6, v.getDestination().getIdDestination());
            ps.setString(7, v.getTypeVol());
            ps.setInt(8, v.getIdVol());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Vol findbyId(int id) throws SQLException {
        String req = "SELECT v.*, d.pays, d.ville, d.description "
                + "FROM vol v "
                + "JOIN destination d ON v.id_destination = d.id_destination "
                + "WHERE v.id_vol = ?";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapVol(rs);
        }
        return null;
    }

    @Override
    public List<Vol> readAll() throws SQLException {
        List<Vol> list = new ArrayList<>();
        String req = "SELECT v.*, d.pays, d.ville, d.description "
                + "FROM vol v "
                + "JOIN destination d ON v.id_destination = d.id_destination";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapVol(rs));
        }
        return list;
    }


    public List<Vol> findByTypeAndDates(int idDestination,
                                        String typeVol,
                                        LocalDate dateAller,
                                        LocalDate dateRetour)
            throws SQLException {

        List<Vol> list = new ArrayList<>();
        String req;

        if (typeVol.equals("ALLER_SIMPLE")) {
            req = "SELECT v.*, d.pays, d.ville, d.description "
                    + "FROM vol v "
                    + "JOIN destination d ON v.id_destination = d.id_destination "
                    + "WHERE v.id_destination = ? "
                    + "AND DATE(v.date_depart) = ?";

            try (PreparedStatement ps = connect.prepareStatement(req)) {
                ps.setInt(1, idDestination);
                ps.setDate(2, Date.valueOf(dateAller));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(mapVol(rs));
            }

        } else if (typeVol.equals("RETOUR_SIMPLE")) {
            req = "SELECT v.*, d.pays, d.ville, d.description "
                    + "FROM vol v "
                    + "JOIN destination d ON v.id_destination = d.id_destination "
                    + "WHERE v.id_destination = ? "
                    + "AND DATE(v.date_arrivee) = ?";

            try (PreparedStatement ps = connect.prepareStatement(req)) {
                ps.setInt(1, idDestination);
                ps.setDate(2, Date.valueOf(dateRetour));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(mapVol(rs));
            }

        } else {
            req = "SELECT v.*, d.pays, d.ville, d.description "
                    + "FROM vol v "
                    + "JOIN destination d ON v.id_destination = d.id_destination "
                    + "WHERE v.id_destination = ? "
                    + "AND DATE(v.date_depart) >= ? "
                    + "AND DATE(v.date_arrivee) <= ?";

            try (PreparedStatement ps = connect.prepareStatement(req)) {
                ps.setInt(1, idDestination);
                ps.setDate(2, Date.valueOf(dateAller));
                ps.setDate(3, Date.valueOf(dateRetour));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(mapVol(rs));
            }
        }

        return list;
    }
    private Vol mapVol(ResultSet rs) throws SQLException {

        Timestamp dateDepartTs  = rs.getTimestamp("date_depart");
        Timestamp dateArriveeTs = rs.getTimestamp("date_arrivee");

        LocalDateTime dateDepart  = dateDepartTs  != null ? dateDepartTs.toLocalDateTime()  : null;
        LocalDateTime dateArrivee = dateArriveeTs != null ? dateArriveeTs.toLocalDateTime() : null;

        Destination d = new Destination();
        d.setIdDestination(rs.getInt("id_destination"));
        d.setPays(rs.getString("pays"));
        d.setVille(rs.getString("ville"));
        d.setDescription(rs.getString("description"));

        Vol vol = new Vol();
        vol.setIdVol(rs.getInt("id_vol"));
        vol.setNumeroVol(rs.getString("numero_vol"));
        vol.setCompagnie(rs.getString("compagnie"));
        vol.setDateDepart(dateDepart);
        vol.setDateArrivee(dateArrivee);
        vol.setPrix(rs.getDouble("prix"));
        vol.setDestination(d);
        vol.setId_destination(rs.getInt("id_destination"));
        vol.setTypeVol(rs.getString("type_vol"));

        return vol;
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
    public int createDestinationIfNotExists(String ville) throws SQLException {

        int id = getDestinationIdByVille(ville);

        if(id != -1) {
            return id;
        }

        String query = "INSERT INTO destination (ville, pays, description) VALUES (?, 'Unknown', 'API imported')";
        PreparedStatement ps = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, ville);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()) {
            return rs.getInt(1);
        }

        return -1;
    }

    public List<Vol> fetchFlightsFromAPI(String departureCity, String arrivalCity, LocalDate date) {

        AmadeusFlightService api = new AmadeusFlightService();
        List<Vol> apiFlights = new ArrayList<>();

        int insertedCount = 0; // counter for inserted flights

        try {

            // Fetch flights from API
            apiFlights = api.searchFlights(departureCity, arrivalCity, date.toString());
            System.out.println("API returned flights: " + apiFlights.size());

            for (Vol v : apiFlights) {

                // Automatically create destination if it does not exist
                int destId = createDestinationIfNotExists(v.getDestination().getVille());
                int depId  = createDestinationIfNotExists(v.getVilleDepart().getVille());

                // Set IDs in the flight object
                v.getDestination().setIdDestination(destId);
                v.getVilleDepart().setIdDestination(depId);

                v.setTypeVol("ALLER_SIMPLE");

                // Duplicate protection
                if (!flightExists(v.getNumeroVol(), v.getDateDepart())) {

                    ajouter(v);
                    insertedCount++; // increment counter
                    System.out.println("Inserted: " + v.getNumeroVol());

                } else {
                    System.out.println("Skipped duplicate: " + v.getNumeroVol());
                }
            }

            // Final console message
            System.out.println("Total flights inserted into database from API: " + insertedCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return apiFlights;
    }
    public void syncFlightsFromAPI() {

        String[][] routes = {
                {"CDG","DXB"},
                {"JFK","LHR"},
                {"IST","FCO"},
                {"DXB","HND"},
                {"LHR","CDG"},
                {"FCO","IST"}
        };

        LocalDate today = LocalDate.now();

        for(int d = 0; d < 7; d++) {

            LocalDate date = today.plusDays(d);

            for(String[] route : routes) {

                System.out.println("Route: " + route[0] + " -> " + route[1] + " | Date: " + date);

                List<Vol> flights = fetchFlightsFromAPI(route[0], route[1], date);

                System.out.println("Flights returned: " + flights.size());
            }
        }

        System.out.println("Flights synced successfully from API.");
    }

    public boolean flightExists(String numeroVol, LocalDateTime dateDepart) throws SQLException {

        String q = "SELECT id_vol FROM vol WHERE numero_vol=? AND date_depart=?";
        PreparedStatement ps = connect.prepareStatement(q);
        ps.setString(1, numeroVol);
        ps.setTimestamp(2, Timestamp.valueOf(dateDepart));

        ResultSet rs = ps.executeQuery();

        return rs.next();
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
