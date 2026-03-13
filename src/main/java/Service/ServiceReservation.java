package Service;

import Entite.Reservation;
import Entite.Personne;
import Entite.Voyage;
import Entite.StatutReservation;
import Entite.Offre;
import Utils.DataSource;
import Entite.Notification;
import Service.ServiceNotification;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements IService<Reservation> {

    private Connection cnx = DataSource.getInstance().getCon();

    // ══════════════════════════════════════════════════
    //  INSERT
    // ══════════════════════════════════════════════════

    @Override
    public boolean ajouter(Reservation r) throws SQLException {

        String sql = "INSERT INTO reservation("
                + "date_reservation, prix_reservation, etat, email, num_tel, commentaire, "
                + "nombre_personnes, num_passeport, id_personne, id_statut, id_voyage, id_offre"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate  (1,  r.getDate_reservation() != null
                    ? new java.sql.Date(r.getDate_reservation().getTime()) : null);
            ps.setDouble(2,  r.getPrix_reservation());
            ps.setString(3,  r.getEtat());
            ps.setString(4,  r.getEmail());
            ps.setString(5,  r.getNum_tel());
            ps.setString(6,  r.getCommentaire());
            ps.setInt   (7,  r.getNombre_personnes());
            ps.setString(8,  r.getNum_passeport());

            if (r.getId_personne() != null)
                ps.setInt(9, r.getId_personne().getIdUtilisateur());
            else
                ps.setNull(9, Types.INTEGER);

            if (r.getId_statut() != null)
                ps.setInt(10, r.getId_statut().getId_statut());
            else
                ps.setNull(10, Types.INTEGER);

            if (r.getId_voyage() != null)
                ps.setInt(11, r.getId_voyage().getIdVoyage());
            else
                ps.setNull(11, Types.INTEGER);

            if (r.getId_offre() != null)
                ps.setInt(12, r.getId_offre().getId_offre());
            else
                ps.setNull(12, Types.INTEGER);

            boolean success = ps.executeUpdate() > 0;

            if (success) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) r.setId_reservation(keys.getInt(1));
                }
                try {
                    ServiceNotification sn = new ServiceNotification();
                    Notification n = new Notification();
                    n.setMessage("Nouvelle réservation effectuée");
                    n.setDateNotification(LocalDateTime.now());
                    n.setLu(false);
                    n.setTypeNotification("reservation");
                    n.setIdReservation(r.getId_reservation());
                    n.setIdUtilisateur(1);
                    sn.ajouter(n);
                } catch (Exception ex) {
                    System.err.println("Notification non envoyée : " + ex.getMessage());
                }
            }
            return success;
        }
    }

    // ══════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════

    @Override
    public boolean supprimer(Reservation r) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id_reservation = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getId_reservation());
            return ps.executeUpdate() > 0;
        }
    }

    // ══════════════════════════════════════════════════
    //  UPDATE  ← root cause of "no value for parameter 6" fixed here
    // ══════════════════════════════════════════════════

    @Override
    public boolean modifier(Reservation r) throws SQLException {

        String sql = "UPDATE reservation SET "
                + "date_reservation  = ?, "   // 1
                + "prix_reservation  = ?, "   // 2
                + "etat              = ?, "   // 3
                + "email             = ?, "   // 4
                + "num_tel           = ?, "   // 5
                + "commentaire       = ?, "   // 6
                + "nombre_personnes  = ?, "   // 7
                + "num_passeport     = ?, "   // 8
                + "id_personne       = ?, "   // 9
                + "id_statut         = ?, "   // 10
                + "id_voyage         = ?, "   // 11
                + "id_offre          = ? "    // 12
                + "WHERE id_reservation = ?"; // 13

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setDate  (1,  r.getDate_reservation() != null
                    ? new java.sql.Date(r.getDate_reservation().getTime()) : null);
            ps.setDouble(2,  r.getPrix_reservation());
            ps.setString(3,  r.getEtat());
            ps.setString(4,  r.getEmail());           // null-safe — JDBC handles it
            ps.setString(5,  r.getNum_tel());
            ps.setString(6,  r.getCommentaire());     // ← was crashing if contained '
            ps.setInt   (7,  r.getNombre_personnes());
            ps.setString(8,  r.getNum_passeport());

            if (r.getId_personne() != null)
                ps.setInt(9, r.getId_personne().getIdUtilisateur());
            else
                ps.setNull(9, Types.INTEGER);

            if (r.getId_statut() != null)
                ps.setInt(10, r.getId_statut().getId_statut());
            else
                ps.setNull(10, Types.INTEGER);

            if (r.getId_voyage() != null)
                ps.setInt(11, r.getId_voyage().getIdVoyage());
            else
                ps.setNull(11, Types.INTEGER);

            if (r.getId_offre() != null)
                ps.setInt(12, r.getId_offre().getId_offre());
            else
                ps.setNull(12, Types.INTEGER);

            ps.setInt(13, r.getId_reservation());

            boolean success = ps.executeUpdate() > 0;

            // Notification d'acceptation
            if (success
                    && r.getEtat() != null
                    && r.getEtat().equalsIgnoreCase("acceptée")
                    && r.getId_personne() != null) {
                try {
                    ServiceNotification sn = new ServiceNotification();
                    Notification n = new Notification();
                    n.setMessage("Votre réservation a été acceptée");
                    n.setDateNotification(LocalDateTime.now());
                    n.setLu(false);
                    n.setTypeNotification("reservation");
                    n.setIdReservation(r.getId_reservation());
                    n.setIdUtilisateur(r.getId_personne().getIdUtilisateur());
                    sn.ajouter(n);
                } catch (Exception ex) {
                    System.err.println("Notification non envoyée : " + ex.getMessage());
                }
            }

            return success;
        }
    }

    // ══════════════════════════════════════════════════
    //  FIND BY ID
    // ══════════════════════════════════════════════════

    @Override
    public Reservation findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE id_reservation = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ══════════════════════════════════════════════════
    //  READ ALL
    // ══════════════════════════════════════════════════

    @Override
    public List<Reservation> readAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ══════════════════════════════════════════════════
    //  MAPPING ResultSet → Reservation
    // ══════════════════════════════════════════════════

    private Reservation mapRow(ResultSet rs) throws SQLException {

        Reservation r = new Reservation();

        r.setId_reservation  (rs.getInt   ("id_reservation"));
        r.setDate_reservation(rs.getDate  ("date_reservation"));
        r.setPrix_reservation(rs.getDouble("prix_reservation"));
        r.setEtat            (rs.getString("etat"));
        r.setEmail           (rs.getString("email"));
        r.setNum_tel         (rs.getString("num_tel"));
        r.setCommentaire     (rs.getString("commentaire"));
        r.setNombre_personnes(rs.getInt   ("nombre_personnes"));
        r.setNum_passeport   (rs.getString("num_passeport"));

        int idPersonne = rs.getInt("id_personne");
        if (!rs.wasNull()) {
            Personne p = new Personne();
            p.setIdUtilisateur(idPersonne);
            r.setId_personne(p);
        }

        int idVoyage = rs.getInt("id_voyage");
        if (!rs.wasNull()) {
            Voyage v = new Voyage();
            v.setIdVoyage(idVoyage);
            r.setId_voyage(v);
        }

        int idStatut = rs.getInt("id_statut");
        if (!rs.wasNull()) {
            StatutReservation s = new StatutReservation();
            s.setId_statut(idStatut);
            r.setId_statut(s);
        }

        int idOffre = rs.getInt("id_offre");
        if (!rs.wasNull()) {
            Offre o = new Offre();
            o.setId_offre(idOffre);
            r.setId_offre(o);
        }

        return r;
    }
}