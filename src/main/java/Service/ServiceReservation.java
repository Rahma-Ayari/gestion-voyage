package Service;

import Entite.Reservation;
import Entite.Personne;
import Entite.Voyage;
import Entite.StatutReservation;
import Entite.Offre;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements IService<Reservation> {

    private Connection cnx = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceReservation() {
        try {
            st = cnx.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean ajouter(Reservation r) throws SQLException {
        // Tous les champs FK sont optionnels (nullable)
        String personneVal = (r.getId_personne() != null)
                ? String.valueOf(r.getId_personne().getIdUtilisateur()) : "NULL";
        String voyageVal   = (r.getId_voyage()   != null)
                ? String.valueOf(r.getId_voyage().getIdVoyage())        : "NULL";
        String offreVal    = (r.getId_offre()    != null)
                ? String.valueOf(r.getId_offre().getId_offre())         : "NULL";

        String req = "INSERT INTO reservation("
                + "date_reservation, prix_reservation, etat, id_personne, id_statut, id_voyage, id_offre"
                + ") VALUES ('"
                + r.getDate_reservation() + "',"
                + r.getPrix_reservation() + ",'"
                + r.getEtat()             + "',"
                + personneVal             + ","
                + r.getId_statut().getId_statut() + ","
                + voyageVal               + ","
                + offreVal                + ")";

        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean supprimer(Reservation r) throws SQLException {
        String req = "DELETE FROM reservation WHERE id_reservation=" + r.getId_reservation();
        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean modifier(Reservation r) throws SQLException {
        String personneVal = (r.getId_personne() != null)
                ? String.valueOf(r.getId_personne().getIdUtilisateur()) : "NULL";
        String voyageVal   = (r.getId_voyage()   != null)
                ? String.valueOf(r.getId_voyage().getIdVoyage())        : "NULL";
        String offreVal    = (r.getId_offre()    != null)
                ? String.valueOf(r.getId_offre().getId_offre())         : "NULL";

        String req = "UPDATE reservation SET "
                + "date_reservation='" + r.getDate_reservation()       + "', "
                + "prix_reservation="  + r.getPrix_reservation()       + ", "
                + "etat='"             + r.getEtat()                   + "', "
                + "id_personne="       + personneVal                   + ", "
                + "id_statut="         + r.getId_statut().getId_statut()+ ", "
                + "id_voyage="         + voyageVal                     + ", "
                + "id_offre="          + offreVal
                + " WHERE id_reservation=" + r.getId_reservation();

        return st.executeUpdate(req) > 0;
    }

    @Override
    public Reservation findbyId(int id) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM reservation WHERE id_reservation=" + id);
        if (rs.next()) return mapRow(rs);
        return null;
    }

    @Override
    public List<Reservation> readAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT * FROM reservation");
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    // ── Helper mapping ResultSet → Reservation ────────
    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId_reservation(rs.getInt("id_reservation"));
        r.setDate_reservation(rs.getDate("date_reservation"));
        r.setPrix_reservation(rs.getDouble("prix_reservation"));
        r.setEtat(rs.getString("etat"));

        // id_personne nullable
        int idPersonne = rs.getInt("id_personne");
        if (!rs.wasNull()) {
            Personne p = new Personne();
            p.setIdUtilisateur(idPersonne);
            r.setId_personne(p);
        }

        // id_voyage nullable
        int idVoyage = rs.getInt("id_voyage");
        if (!rs.wasNull()) {
            Voyage v = new Voyage();
            v.setIdVoyage(idVoyage);
            r.setId_voyage(v);
        }

        // id_statut
        StatutReservation s = new StatutReservation();
        s.setId_statut(rs.getInt("id_statut"));
        r.setId_statut(s);

        // id_offre nullable
        int idOffre = rs.getInt("id_offre");
        if (!rs.wasNull()) {
            Offre o = new Offre();
            o.setId_offre(idOffre);
            r.setId_offre(o);
        }

        return r;
    }
}