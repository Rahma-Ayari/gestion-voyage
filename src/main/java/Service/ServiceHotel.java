package Service;

import Entite.Hotel;
import Utils.DataSource;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHotel implements IService<Hotel> {

    private static final String API_KEY = "VOTRE_CLE_API_GOOGLE_MAPS";

    private Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceHotel() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // ─────────────────────────────────────────────
    // AJOUTER
    // ─────────────────────────────────────────────
    @Override
    public boolean ajouter(Hotel h) throws SQLException {
        String req = "INSERT INTO hotel " +
                "(nom, ville, adresse, stars, capacite, type_chambre, prix_par_nuit, disponibilite, latitude, longitude) VALUES (" +
                "'" + h.getNom() + "', " +
                "'" + h.getVille() + "', " +
                "'" + h.getAdresse() + "', " +
                h.getStars() + ", " +
                h.getCapacite() + ", " +
                "'" + h.getTypeChambre() + "', " +
                h.getPrixParNuit() + ", " +
                (h.isDisponibilite() ? 1 : 0) + ", " +
                h.getLatitude() + ", " +
                h.getLongitude() + ");";
        int res = st.executeUpdate(req);
        return res > 0;
    }

    // ─────────────────────────────────────────────
    // SUPPRIMER
    // ─────────────────────────────────────────────
    @Override
    public boolean supprimer(Hotel h) throws SQLException {
        String req = "DELETE FROM hotel WHERE id_hotel = " + h.getIdHotel();
        int res = st.executeUpdate(req);
        return res > 0;
    }

    // ─────────────────────────────────────────────
    // MODIFIER
    // ─────────────────────────────────────────────
    @Override
    public boolean modifier(Hotel h) throws SQLException {
        String req = "UPDATE hotel SET " +
                "nom = '" + h.getNom() + "', " +
                "ville = '" + h.getVille() + "', " +
                "adresse = '" + h.getAdresse() + "', " +
                "stars = " + h.getStars() + ", " +
                "capacite = " + h.getCapacite() + ", " +
                "type_chambre = '" + h.getTypeChambre() + "', " +
                "prix_par_nuit = " + h.getPrixParNuit() + ", " +
                "disponibilite = " + (h.isDisponibilite() ? 1 : 0) + ", " +
                "latitude = " + h.getLatitude() + ", " +
                "longitude = " + h.getLongitude() + " " +
                "WHERE id_hotel = " + h.getIdHotel();
        int res = st.executeUpdate(req);
        return res > 0;
    }

    // ─────────────────────────────────────────────
    // FIND BY ID
    // ─────────────────────────────────────────────
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
                    rs.getBoolean("disponibilite"),
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude")
            );
        }
        return hotel;
    }

    // ─────────────────────────────────────────────
// FIND BY DESTINATION
// ─────────────────────────────────────────────
    public List<Hotel> findByDestination(int idDestination) throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        String req = "SELECT * FROM hotel WHERE id_destination = " + idDestination;
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            hotels.add(new Hotel(
                    rs.getInt("id_hotel"),
                    rs.getString("nom"),
                    rs.getString("ville"),
                    rs.getString("adresse"),
                    rs.getInt("stars"),
                    rs.getInt("capacite"),
                    rs.getString("type_chambre"),
                    rs.getDouble("prix_par_nuit"),
                    rs.getBoolean("disponibilite"),
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude")
            ));
        }
        return hotels;
    }

    // ─────────────────────────────────────────────
    // READ ALL
    // ─────────────────────────────────────────────
    @Override
    public List<Hotel> readAll() throws SQLException {
        List<Hotel> list = new ArrayList<>();
        String query = "SELECT * FROM hotel";
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            list.add(new Hotel(
                    rs.getInt("id_hotel"),
                    rs.getString("nom"),
                    rs.getString("ville"),
                    rs.getString("adresse"),
                    rs.getInt("stars"),
                    rs.getInt("capacite"),
                    rs.getString("type_chambre"),
                    rs.getDouble("prix_par_nuit"),
                    rs.getBoolean("disponibilite"),
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude")
            ));
        }
        return list;
    }

    // ─────────────────────────────────────────────
    // CARTE - Géocodage adresse → lat/lon
    // ─────────────────────────────────────────────
    public double[] geocoderAdresse(String adresse, String ville) {
        try {
            String adresseComplete = adresse + ", " + ville;
            String urlStr = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(adresseComplete, "UTF-8")
                    + "&key=" + API_KEY;

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            String json = response.toString();
            if (!json.contains("\"OK\"")) {
                System.out.println("Geocoding échoué : " + json);
                return null;
            }

            String latStr = json.substring(json.indexOf("\"lat\" :") + 7).trim();
            latStr = latStr.substring(0, latStr.indexOf(",")).trim();
            double lat = Double.parseDouble(latStr);

            String lngStr = json.substring(json.indexOf("\"lng\" :") + 7).trim();
            lngStr = lngStr.substring(0, lngStr.indexOf("\n")).trim();
            double lng = Double.parseDouble(lngStr);

            return new double[]{lat, lng};

        } catch (Exception e) {
            System.out.println("Erreur geocodage : " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────
    // CARTE - Géocoder et sauvegarder en BDD
    // ─────────────────────────────────────────────
    public boolean geocoderEtSauvegarder(Hotel h) throws SQLException {
        double[] coords = geocoderAdresse(h.getAdresse(), h.getVille());
        if (coords != null) {
            h.setLatitude(coords[0]);
            h.setLongitude(coords[1]);
            return modifier(h);
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // CARTE - HTML pour 1 hôtel avec carte directe
    // ─────────────────────────────────────────────
    public String genererHtmlCarte(Hotel h) {
        String etoiles = "⭐".repeat(h.getStars());
        String dispo = h.isDisponibilite() ? "✅ Disponible" : "❌ Indisponible";
        String dispoBg = h.isDisponibilite()
                ? "background:#e8f5e9; color:#2e7d32;"
                : "background:#ffebee; color:#c62828;";
        String gmapsEmbed = "https://maps.google.com/maps?q="
                + h.getLatitude() + "," + h.getLongitude()
                + "&z=15&output=embed";

        return "<!DOCTYPE html><html><head><meta charset='utf-8'/>" +
                "<style>" +
                "* { box-sizing: border-box; margin: 0; padding: 0; }" +
                "body { font-family: Arial, sans-serif; background: #f0f4f8; display: flex; flex-direction: column; height: 100vh; }" +
                ".card { background: white; padding: 12px 16px; border-bottom: 3px solid #FF9800;" +
                "        box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                ".titre { font-size: 18px; font-weight: bold; color: #FF9800; margin-bottom: 8px; }" +
                ".infos { display: flex; flex-wrap: wrap; gap: 8px; font-size: 13px; color: #444; }" +
                ".info-item { background: #f9f9f9; border-radius: 8px; padding: 5px 10px; border: 1px solid #eee; }" +
                ".badge { display:inline-block; padding: 4px 12px; border-radius: 20px;" +
                "         font-size: 12px; font-weight: bold; " + dispoBg + " }" +
                "iframe { flex: 1; border: none; width: 100%; }" +
                "</style></head><body>" +
                "<div class='card'>" +
                "  <div class='titre'>🏨 " + h.getNom() + "</div>" +
                "  <div class='infos'>" +
                "    <div class='info-item'>📍 " + h.getAdresse() + ", " + h.getVille() + "</div>" +
                "    <div class='info-item'>" + etoiles + "</div>" +
                "    <div class='info-item'>🛏 " + h.getTypeChambre() + "</div>" +
                "    <div class='info-item'>👥 " + h.getCapacite() + " chambres</div>" +
                "    <div class='info-item'>💶 " + h.getPrixParNuit() + " €/nuit</div>" +
                "    <div class='badge'>" + dispo + "</div>" +
                "  </div>" +
                "</div>" +
                "<iframe src='" + gmapsEmbed + "' allowfullscreen></iframe>" +
                "</body></html>";
    }

    // ─────────────────────────────────────────────
    // CARTE - HTML pour tous les hôtels
    // ─────────────────────────────────────────────
    public String genererHtmlCarteMultiple(List<Hotel> hotels) {
        StringBuilder lignes = new StringBuilder();
        for (Hotel h : hotels) {
            String dispo = h.isDisponibilite() ? "✅" : "❌";
            String gmaps = "https://www.google.com/maps?q=" + h.getLatitude() + "," + h.getLongitude();
            lignes.append(
                    "<tr>" +
                            "<td>" + h.getNom() + "</td>" +
                            "<td>" + h.getVille() + "</td>" +
                            "<td>" + h.getAdresse() + "</td>" +
                            "<td>" + "⭐".repeat(h.getStars()) + "</td>" +
                            "<td>" + h.getTypeChambre() + "</td>" +
                            "<td><b>" + h.getPrixParNuit() + " €</b></td>" +
                            "<td>" + dispo + "</td>" +
                            "<td><a href='" + gmaps + "' style='color:#FF9800;font-weight:bold;'>📍 Voir</a></td>" +
                            "</tr>"
            );
        }

        return "<!DOCTYPE html><html><head><meta charset='utf-8'/>" +
                "<style>" +
                "body { font-family: Arial; background: #f0f4f8; padding: 16px; margin: 0; }" +
                "h2 { color: #FF9800; margin-bottom: 14px; font-size: 20px; }" +
                "table { width: 100%; border-collapse: collapse; background: white;" +
                "        border-radius: 12px; overflow: hidden;" +
                "        box-shadow: 0 4px 16px rgba(0,0,0,0.1); }" +
                "th { background: #FF9800; color: white; padding: 11px 8px; text-align: left; font-size: 13px; }" +
                "td { padding: 9px 8px; border-bottom: 1px solid #eee; font-size: 13px; color: #444; }" +
                "tr:last-child td { border-bottom: none; }" +
                "tr:hover td { background: #fff8f0; }" +
                "a { text-decoration: none; }" +
                "</style></head><body>" +
                "<h2>🏨 Liste des Hôtels (" + hotels.size() + ")</h2>" +
                "<table>" +
                "<tr><th>Nom</th><th>Ville</th><th>Adresse</th><th>⭐</th>" +
                "<th>Type</th><th>Prix/Nuit</th><th>Dispo</th><th>Carte</th></tr>" +
                lignes +
                "</table>" +
                "</body></html>";
    }
}