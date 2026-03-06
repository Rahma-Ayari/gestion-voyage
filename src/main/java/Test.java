import Entite.*;
import Service.*;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Test {

    private static Connection con;
    private static Statement stmt;

    public static void main(String[] args) {
        try {
            System.out.println("=== TEST CONNEXION ===");
            con = DataSource.getInstance().getCon();
            if (con != null) {
                System.out.println("Connexion OK !");
                stmt = con.createStatement();
            }

            // ========================
            // Test Destination
            // ========================
            System.out.println("\n--- Test Destination ---");
            ServiceDestination sd = new ServiceDestination();

            Destination d = new Destination();
            d.setPays("Tunisie");
            d.setVille("Tunis");
            d.setDescription("Destination test");

            sd.ajouter(d);

            List<Destination> destinations = sd.readAll();
            for (Destination dest : destinations) {
                System.out.println(dest);
            }

            // ========================
            // Test Vol
            // ========================
            System.out.println("\n--- Test Vol ---");
            Destination destForVol = sd.readAll().stream().findFirst().orElse(null);

            ServiceVol sv = new ServiceVol();
            Vol vol = new Vol();
            vol.setNumeroVol("TN123");
            vol.setCompagnie("TunisAir");
            vol.setDateDepart(LocalDateTime.now());
            vol.setDateArrivee(LocalDateTime.now().plusHours(2));
            vol.setPrix(250.5);
            if (destForVol != null) vol.setDestination(destForVol);

            boolean resVol = sv.ajouter(vol);
            System.out.println("Vol ajouté ? " + resVol);

            for (Vol v : sv.readAll()) {
                System.out.println(v);
            }

            // ========================
            // Test Voyage
            // ========================
            System.out.println("\n--- Test Voyage ---");
            ServiceVoyage svy = new ServiceVoyage();

            Vol volForVoyage = sv.readAll().stream().findFirst().orElse(null);

            Voyage voyage = new Voyage();
            voyage.setDuree(5);
            voyage.setDateDebut(LocalDate.now());
            voyage.setDateFin(LocalDate.now().plusDays(5));
            voyage.setRythme("Calme");
            if (destForVol != null)
                voyage.setIdDestination(destForVol.getIdDestination());
            if (volForVoyage != null)
                voyage.setIdVol(volForVoyage.getIdVol());

            int resVoyage = svy.ajouter(voyage);
            System.out.println("Voyage ajouté ? " + (resVoyage > 0));

            for (Voyage v : svy.readAll()) {
                System.out.println(v);
            }

            // ========================
            // Test Hotel
            // ========================
            System.out.println("\n--- Test Hotel ---");
            ServiceHotel sh = new ServiceHotel();

            Hotel h = new Hotel();
            h.setNom("Hotel Test");
            h.setVille("Tunis");
            h.setAdresse("Rue Test");
            h.setStars(4);
            h.setCapacite(50);
            h.setTypeChambre("Standard");
            h.setPrixParNuit(120.0);
            h.setDisponibilite(true);

            boolean resHotel = sh.ajouter(h);
            System.out.println("Hotel ajouté ? " + resHotel);

            for (Hotel hotel : sh.readAll()) {
                System.out.println(hotel);
            }

            // ========================
            // Test Personne et Reservation
            // ========================
            System.out.println("\n--- Test Reservation ---");
            ServicePersonne sp = new ServicePersonne();
            ServiceReservation sres = new ServiceReservation();
            ServiceStatutReservation ssr = new ServiceStatutReservation();

            Reservation resv = new Reservation();
            resv.setDate_reservation(Date.valueOf(LocalDate.now()));
            resv.setPrix_reservation(180.0);
            resv.setEtat("Confirmée");

            Personne p = sp.findbyId(22);        // adapte l'ID selon ta BDD
            Voyage v = svy.findbyId(1);          // adapte l'ID selon ta BDD
            StatutReservation st = ssr.findbyId(1);// adapte l'ID selon ta BDD

            if (p != null && v != null && st != null) {
                resv.setId_personne(p);
                resv.setId_voyage(v);
                resv.setId_statut(st);

                boolean ok = sres.ajouter(resv);
                System.out.println("Réservation ajoutée ? " + ok);
            } else {
                System.out.println("Erreur : données liées inexistantes pour la réservation.");
            }

            // ========================
            // Fermeture connexion
            // ========================
            stmt.close();
            con.close();
            System.out.println("\nConnexion fermée.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}