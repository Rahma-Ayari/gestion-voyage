package Service;

import Entite.Vol;
import Entite.Destination;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AmadeusFlightService {

    private final String CLIENT_ID = "YOUR_API_KEY";
    private final String CLIENT_SECRET = "YOUR_SECRET";

    private String accessToken;

    public String getAccessToken() {
        try {
            URL url = new URL("https://test.api.amadeus.com/v1/security/oauth2/token");

            String body = "grant_type=client_credentials"
                    + "&client_id=" + CLIENT_ID
                    + "&client_secret=" + CLIENT_SECRET;

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes());

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null)
                response.append(line);

            JSONObject json = new JSONObject(response.toString());

            accessToken = json.getString("access_token");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return accessToken;
    }

    public List<Vol> searchFlights(String depart, String arrivee, String date) {

        List<Vol> vols = new ArrayList<>();

        try {

            if (accessToken == null)
                getAccessToken();

            String urlStr =
                    "https://test.api.amadeus.com/v2/shopping/flight-offers"
                            + "?originLocationCode=" + depart
                            + "&destinationLocationCode=" + arrivee
                            + "&departureDate=" + date
                            + "&adults=1&max=10";

            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null)
                response.append(line);

            JSONObject json = new JSONObject(response.toString());

            JSONArray data = json.getJSONArray("data");

            for (int i = 0; i < data.length(); i++) {

                JSONObject flight = data.getJSONObject(i);

                JSONObject itinerary = flight
                        .getJSONArray("itineraries")
                        .getJSONObject(0);

                JSONObject segment = itinerary
                        .getJSONArray("segments")
                        .getJSONObject(0);

                Vol vol = new Vol();

                vol.setNumeroVol(
                        segment.getString("carrierCode")
                                + segment.getString("number")
                );

                vol.setCompagnie(segment.getString("carrierCode"));

                vol.setDateDepart(
                        LocalDateTime.parse(
                                segment.getJSONObject("departure")
                                        .getString("at")
                        )
                );

                vol.setDateArrivee(
                        LocalDateTime.parse(
                                segment.getJSONObject("arrival")
                                        .getString("at")
                        )
                );

                vol.setPrix(
                        flight.getJSONObject("price")
                                .getDouble("total")
                );

                Destination dest = new Destination();
                dest.setVille(
                        segment.getJSONObject("arrival")
                                .getString("iataCode")
                );

                Destination dep = new Destination();
                dep.setVille(
                        segment.getJSONObject("departure")
                                .getString("iataCode")
                );

                vol.setDestination(dest);
                vol.setVilleDepart(dep);

                vols.add(vol);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return vols;
    }
}
