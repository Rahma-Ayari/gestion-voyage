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

            // ------------------ TEST ROLE ------------------
            System.out.println("\n=== TEST ROLE ===");
            ServiceRole sr = new ServiceRole();
            Role r = new Role("Client");
            sr.ajouter(r);
            System.out.println("Rôle créé : " + r);

            // ------------------ TEST UTILISATEUR ------------------
            System.out.println("\n=== TEST UTILISATEUR ===");
            ServiceUtilisateur su = new ServiceUtilisateur();
            String email = "test" + System.currentTimeMillis() + "@mail.com";
            Utilisateur u = new Utilisateur("Nom", "Prenom", email, "azerty", new Date(System.currentTimeMillis()));
            if (su.ajouter(u)) System.out.println("Utilisateur ajouté : " + u.getIdUtilisateur());

            // ------------------ TEST AVIS ------------------
            System.out.println("\n=== TEST AVIS ===");
            ServiceAvis sa = new ServiceAvis();
            Avis av = new Avis(5, "Top !", new Date(System.currentTimeMillis()), u.getIdUtilisateur());
            if (sa.ajouter(av)) System.out.println("Avis ajouté avec succès !");

        } catch (SQLException e) {
            System.err.println("ERREUR SQL : " + e.getMessage());
            e.printStackTrace();
        }

        // ------------------ TEST DESTINATION ------------------
        System.out.println("\n--- Test Destination ---");
        ServiceDestination sd = new ServiceDestination();
        Destination d = new Destination();
        d.setPays("Tunisie");
        d.setVille("Tunis");
        d.setDescription("Destination test");
        try {
            boolean resultat = sd.ajouter(d);
            System.out.println("Destination ajoutée ? " + resultat);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM `destination`");
            while (rs.next()) {
                int id = rs.getInt(1);
                String pays = rs.getString("pays");
                String ville = rs.getString("ville");
                String desc = rs.getString("description");
                System.out.println("id: " + id + " pays: " + pays + " ville: " + ville + " description: " + desc);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

        // ------------------ TEST VOL ------------------
        System.out.println("\n--- Test Vol ---");
        Destination destForVol = null;
        try {
            java.util.List<Destination> destinations = sd.readAll();
            if (!destinations.isEmpty()) {
                destForVol = destinations.get(0);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

        ServiceVol sv = new ServiceVol();
        Vol vol = new Vol();
        vol.setNumeroVol("TN123");
        vol.setCompagnie("TunisAir");
        vol.setDateDepart(LocalDateTime.now());
        vol.setDateArrivee(LocalDateTime.now().plusHours(2));
        vol.setPrix(250.5);
        if (destForVol != null) vol.setDestination(destForVol);

        try {
            boolean res = sv.ajouter(vol);
            System.out.println("Vol ajouté ? " + res);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM `vol`");
            while (rs.next()) {
                int id = rs.getInt(1);
                String numero = rs.getString("numero_vol");
                String compagnie = rs.getString("compagnie");
                System.out.println("id: " + id + " numeroVol: " + numero + " compagnie: " + compagnie);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

        // ------------------ TEST VOYAGE ------------------
        System.out.println("\n--- Test Voyage ---");
        Vol volForVoyage = null;
        try {
            java.util.List<Vol> vols = sv.readAll();
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

        // ------------------ TEST ACTIVITE ------------------
        System.out.println("\n--- Test Activite ---");
        Voyage voyageForActivite = null;
        try {
            java.util.List<Voyage> voyages = svy.readAll();
            if (!voyages.isEmpty()) voyageForActivite = voyages.get(0);
        } catch (SQLException e) {
            System.out.println(e);
        }

        ServiceActivite sact = new ServiceActivite();
        Activite act = new Activite();
        act.setNom("Plongée");
        act.setDescription("Activité sous-marine");
        act.setPrix(50);
        act.setDureeEnHeure(2);
        act.setCategorie("Sport");
        act.setHoraire("10:00-12:00");
        if (voyageForActivite != null) act.setIdVoyage(voyageForActivite.getIdVoyage());

        try {
            boolean res = sact.ajouter(act);
            System.out.println("Activité ajoutée ? " + res);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM `activite`");
            while (rs.next()) {
                int id = rs.getInt(1);
                String nom = rs.getString("nom");
                double prix = rs.getDouble("prix");
                System.out.println("id: " + id + " nom: " + nom + " prix: " + prix);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

        // ------------------ TEST HOTEL ------------------
        System.out.println("\n--- Test Hotel ---");
        ServiceHotel sh = new ServiceHotel();
        Hotel h = new Hotel();
        h.setNom("Hotel Test");
        h.setVille("Tunis");
        h.setAdresse("Rue Test");
        try {
            boolean res = sh.ajouter(h);
            System.out.println("Hotel ajouté ? " + res);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM `hotel`");
            while (rs.next()) {
                int id = rs.getInt(1);
                String nom = rs.getString("nom");
                String ville = rs.getString("ville");
                System.out.println("id: " + id + " nom: " + nom + " ville: " + ville);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }






        ServiceTypePaiement stp = new ServiceTypePaiement();
        TypePaiement t1 = new TypePaiement();
        t1.setLibelle("Carte bancaire");
        t1.setDescription("Paiement par CB");
        try {
            stp.ajouter(t1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("TypePaiement ajouté : " + t1);

        List<TypePaiement> types = null;
        try {
            types = stp.readAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Tous les TypePaiements : " + types);

        t1.setDescription("Paiement par carte bancaire classique");
        try {
            stp.modifier(t1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println("TypePaiement modifié : " + stp.findbyId(t1.getIdTypePaiement()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\n--- Test Offre pour voyage id 1 ---");
        ServiceOffre so = new ServiceOffre();
        Offre o = new Offre();
        o.setType("Promo spéciale");               // Type d'offre
        o.setPrix(200);                             // Prix de l'offre
        o.setDescription("Réduction sur le voyage"); // Description
        o.setDisponibilite(true);

        svy = new ServiceVoyage();
        voyage = null;
        try {
            voyage = svy.findbyId(1); // voyage avec id 1
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du voyage : " + e.getMessage());
        }
        if (voyage != null) {
            o.setvoyage(voyage); // Associer le voyage à l'offre

            try {
                boolean resultat = so.ajouter(o); // Ajouter l'offre dans la base
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




        /* ServicePaiement sp = new ServicePaiement();
        Paiement p1 = new Paiement();
        p1.setMontant(250.0);
        p1.setDatePaiement(LocalDateTime.now());
        p1.setStatut("EN_ATTENTE");
        p1.setIdReservation(1);
        p1.setTypePaiement(t1);
        try {
            sp.ajouter(p1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Paiement ajouté : " + p1);

        List<Paiement> paiements = null;
        try {
            paiements = sp.readAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Tous les paiements : " + paiements);

        p1.setStatut("PAYE");
        try {
            sp.modifier(p1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println("Paiement modifié : " + sp.findbyId(p1.getIdPaiement()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ServiceFacture sf = new ServiceFacture();
        Facture f1 = new Facture();
        f1.setNumeroFacture("FAC-1001");
        f1.setDateFacture(LocalDate.now());
        f1.setMontantTotal(250.0);
        f1.setPaiement(p1);
        try {
            sf.ajouter(f1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Facture ajoutée : " + f1);

        List<Facture> factures = null;
        try {
            factures = sf.readAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Toutes les factures : " + factures);

        f1.setMontantTotal(300.0);
        try {
            sf.modifier(f1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println("Facture modifiée : " + sf.findbyId(f1.getIdFacture()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
  */


        // ------------------ Fermeture connexion ------------------
        try {
            stmt.close();
            con.close();
            System.out.println("\nConnexion fermée.");
        } catch (SQLException e) {
            System.out.println(e);
        }


    }

}