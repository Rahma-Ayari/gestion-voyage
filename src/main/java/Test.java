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
    private static List<Vol> vols;
    private static Utilisateur u;

    public static void main(String[] args) throws SQLException {
        try {
            System.out.println("=== TEST CONNEXION ===");
            con = DataSource.getInstance().getCon();
            if (con != null) {
                System.out.println("Connexion OK !");
                stmt = con.createStatement();
            }
            



        System.out.println("\n--- Test Destination ---");
        ServiceDestination sd = new ServiceDestination();

        Destination d = new Destination();
        d.setPays("Tunisie11111");
        d.setVille("Tunis");
        d.setDescription("Destination test11111");

        sd.ajouter(d);

        List<Destination> destinations = sd.readAll();
        for (Destination dest : destinations) {
            System.out.println(dest);
        }



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

// Affichage propre
        for (Vol v : sv.readAll()) {
            System.out.println(v);
        }




        System.out.println("\n--- Test Voyage ---");
        ServiceVoyage svy = new ServiceVoyage();  // <- initialisation correcte

        Vol volForVoyage = null;
        try {
            List<Vol> vols = sv.readAll();
            if (!vols.isEmpty()) volForVoyage = vols.get(0);
        } catch (SQLException e) {
            System.out.println(e);
        }

        Voyage voyage = new Voyage();
        voyage.setDuree(5);
        voyage.setDateDebut(LocalDate.now());
        voyage.setDateFin(LocalDate.now().plusDays(5));
        voyage.setRythme("Calme");
        if (destForVol != null) voyage.setDestination(destForVol);
        if (volForVoyage != null) voyage.setVol(volForVoyage);

        try {
            boolean resVoyage = svy.ajouter(voyage);
            System.out.println("Voyage ajouté ? " + resVoyage);

            List<Voyage> voyages = svy.readAll();
            for (Voyage v : voyages) {
                System.out.println(v);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

// Test réservation
        System.out.println("\n--- Test Reservation ---");
        ServiceStatutReservation ssr = new ServiceStatutReservation(); // <- initialisation
        ServiceReservation sres = new ServiceReservation();
        ServicePersonne sp = new ServicePersonne();


        System.out.println("\n--- Test Hotel ---");

        ServiceHotel sh = new ServiceHotel();

        Hotel h = new Hotel();
        h.setNom("Hotel Test");
        h.setVille("Tunis");
        h.setAdresse("Rue Test");

        try {
            boolean resHotel = sh.ajouter(h);
            System.out.println("Hotel ajouté ? " + resHotel);

            // Affichage via service
            List<Hotel> hotels = sh.readAll();
            for (Hotel hotel : hotels) {
                System.out.println(hotel);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }



        System.out.println("\n--- Test Reservation ---");

        sres = new ServiceReservation();
        sp = new ServicePersonne();

        Reservation resv = new Reservation();
        resv.setDate_reservation(Date.valueOf(LocalDate.now()));
        resv.setPrix_reservation(180.0);
        resv.setEtat("Confirmée");

        try {
            Personne p = sp.findbyId(22);
            Voyage v = svy.findbyId(1);
            StatutReservation st = ssr.findbyId(1);

            if (p == null || v == null || st == null) {
                System.out.println("Erreur : données liées inexistantes");
            } else {
                resv.setId_personne(p);
                resv.setId_voyage(v);
                resv.setId_statut(st);

                boolean ok = sres.ajouter(resv);
                System.out.println("Réservation ajoutée ? " + ok);
            }

        } catch (SQLException e) {
            System.out.println("Erreur réservation : " + e.getMessage());
        }

        try {
            stmt.close();
            con.close();
            System.out.println("\nConnexion fermée.");
        } catch (SQLException e) {
            System.out.println(e);
        }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}