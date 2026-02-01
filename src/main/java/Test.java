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

    public static void main(String[] args) throws SQLException {
        try {
            System.out.println("=== TEST CONNEXION ===");
            con = DataSource.getInstance().getCon();
            if (con != null) {
                System.out.println("Connexion OK !");
                stmt = con.createStatement();
            }


            System.out.println("\n=== TEST ROLE ===");
            ServiceRole sr = new ServiceRole();
            Role r = new Role("Administrateur");
            sr.ajouter(r);
            System.out.println("Rôle créé : " + r);

            System.out.println("\n=== TEST UTILISATEUR ===");
            ServiceUtilisateur su = new ServiceUtilisateur();
            String email = "test" + System.currentTimeMillis() + "@mail.com";
            Utilisateur u = new Utilisateur("Nom", "Prenom", email, "azerty", new Date(System.currentTimeMillis()));
            if (su.ajouter(u)) System.out.println("Utilisateur ajouté : " + u.getIdUtilisateur());

            System.out.println("\n=== TEST AVIS ===");
            ServiceAvis sa = new ServiceAvis();
            Avis av = new Avis(5, "Top !", new Date(System.currentTimeMillis()), u.getIdUtilisateur());
            if (sa.ajouter(av)) System.out.println("Avis ajouté avec succès !");

        } catch (SQLException e) {
            System.err.println("ERREUR SQL : " + e.getMessage());
            e.printStackTrace();
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
        Vol volForVoyage = null;
        try {
            vols = sv.readAll();
            if (!vols.isEmpty()) volForVoyage = vols.get(0);
        } catch (SQLException e) {
            System.out.println(e);
        }

        ServiceVoyage svy = new ServiceVoyage();
        Voyage voyage = new Voyage();
        voyage.setDuree(5);
        voyage.setDateDebut(LocalDate.now());
        voyage.setDateFin(LocalDate.now().plusDays(5));
        voyage.setRythme("Calme");
        if (destForVol != null) voyage.setDestination(destForVol);
        if (volForVoyage != null) voyage.setVol(volForVoyage);

        try {
            boolean res = svy.ajouter(voyage);
            System.out.println("Voyage ajouté ? " + res);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM `voyage`");
            while (rs.next()) {
                int id = rs.getInt(1);
                int duree = rs.getInt("duree");
                String rythme = rs.getString("rythme");
                System.out.println("id: " + id + " duree: " + duree + " rythme: " + rythme);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

        System.out.println("\n--- Test Activite ---");

// Récupérer un voyage existant pour lier l'activité
        Voyage voyageForActivite = null;
        try {
            List<Voyage> voyages = svy.readAll();
            if (!voyages.isEmpty()) voyageForActivite = voyages.get(0);
        } catch (SQLException e) {
            System.out.println(e);
        }

// Création de l'activité
        ServiceActivite sact = new ServiceActivite();
        Activite act = new Activite();
        act.setNom("Plongée");
        act.setDescription("Activité sous-marine");
        act.setPrix(50);
        act.setDureeEnHeure(2);
        act.setCategorie("Sport");
        act.setHoraire("10:00-12:00");
        //if (voyageForActivite != null) act.setVoyage(voyageForActivite);

        try {
            boolean res = sact.ajouter(act);
            System.out.println("Activité ajoutée ? " + res);
        } catch (SQLException e) {
            System.out.println(e);
        }

// Affichage via service (PAS DE SQL)
        try {
            List<Activite> activites = sact.readAll();
            for (Activite a : activites) {
                System.out.println(a);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }


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


        System.out.println("\n--- Test Voyage ---");

// Récupérer un vol existant pour lier au voyage
        volForVoyage = null;
        try {
            List<Vol> vols = sv.readAll();
            if (!vols.isEmpty()) volForVoyage = vols.get(0);
        } catch (SQLException e) {
            System.out.println(e);
        }

        voyage = new Voyage();
        voyage.setDuree(5);
        voyage.setDateDebut(LocalDate.now());
        voyage.setDateFin(LocalDate.now().plusDays(5));
        voyage.setRythme("Calme");
        if (destForVol != null) voyage.setDestination(destForVol);
        if (volForVoyage != null) voyage.setVol(volForVoyage);

        try {
            boolean resVoyage = svy.ajouter(voyage);
            System.out.println("Voyage ajouté ? " + resVoyage);

            // Affichage via service
            List<Voyage> voyages = svy.readAll();
            for (Voyage v : voyages) {
                System.out.println(v);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }


        System.out.println("\n--- Test Offre pour voyage id 1 ---");
        ServiceOffre so = new ServiceOffre();
        Offre o = new Offre();
        o.setType("Promo spéciale");
        o.setPrix(200);
        o.setDescription("Réduction sur le voyage");
        o.setDisponibilite(true);

        svy = new ServiceVoyage();
        voyage = null;
        try {
            voyage = svy.findbyId(1);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du voyage : " + e.getMessage());
        }
        if (voyage != null) {
            o.setvoyage(voyage);

            try {
                boolean resultat = so.ajouter(o);
                if (resultat) {
                    System.out.println("Offre ajoutée avec succès pour le voyage id 1 !");
                } else {
                    System.out.println("Échec lors de l'ajout de l'offre.");
                }
            } catch (SQLException e) {
                System.out.println("Erreur SQL: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Impossible d'ajouter l'offre : le voyage avec id 1 n'existe pas.");
        }

        System.out.println("\n--- Test StatutReservation ---");
        ServiceStatutReservation ssr = new ServiceStatutReservation();
        StatutReservation s = new StatutReservation();
        s.setLibelle("En attente");
        try {
            boolean res = ssr.ajouter(s);
            System.out.println("Statut ajouté : " + res);
        } catch (SQLException e) {
            System.out.println(e);
        }

        System.out.println("\n--- Test Reservation ---");

        ServiceReservation sres = new ServiceReservation();
        ServicePersonne sp = new ServicePersonne();

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


    }

}