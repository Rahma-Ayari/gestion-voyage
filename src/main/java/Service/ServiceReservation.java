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

    // ══════════════════════════════════════════════════
    //  INSERT
    // ══════════════════════════════════════════════════

    @Override
    public boolean ajouter(Reservation r) throws SQLException {

        String personneVal = (r.getId_personne() != null)
                ? String.valueOf(r.getId_personne().getIdUtilisateur()) : "NULL";

        String voyageVal = (r.getId_voyage() != null)
                ? String.valueOf(r.getId_voyage().getIdVoyage()) : "NULL";

        String offreVal = (r.getId_offre() != null)
                ? String.valueOf(r.getId_offre().getId_offre()) : "NULL";

        // num_passeport est nullable : si null on insère NULL, sinon la valeur
        String passeportVal = (r.getNum_passeport() != null)
                ? "'" + r.getNum_passeport().replace("'", "''") + "'"
                : "NULL";

        String req = "INSERT INTO reservation("
                + "date_reservation, prix_reservation, etat, email, num_tel, commentaire, "
                + "nombre_personnes, num_passeport, "          // ← num_passeport ajouté
                + "id_personne, id_statut, id_voyage, id_offre"
                + ") VALUES ('"
                + r.getDate_reservation()     + "',"
                + r.getPrix_reservation()     + ",'"
                + r.getEtat()                 + "','"
                + r.getEmail()                + "','"
                + r.getNum_tel()              + "','"
                + r.getCommentaire()          + "',"
                + r.getNombre_personnes()     + ","
                + passeportVal                + ","            // ← valeur insérée
                + personneVal                 + ","
                + r.getId_statut().getId_statut() + ","
                + voyageVal                   + ","
                + offreVal                    + ")";

        return st.executeUpdate(req) > 0;
    }

    // ══════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════

    @Override
    public boolean supprimer(Reservation r) throws SQLException {
        String req = "DELETE FROM reservation WHERE id_reservation=" + r.getId_reservation();
        return st.executeUpdate(req) > 0;
    }

    // ══════════════════════════════════════════════════
    //  UPDATE
    // ══════════════════════════════════════════════════

    @Override
    public boolean modifier(Reservation r) throws SQLException {

        String personneVal = (r.getId_personne() != null)
                ? String.valueOf(r.getId_personne().getIdUtilisateur()) : "NULL";

        String voyageVal = (r.getId_voyage() != null)
                ? String.valueOf(r.getId_voyage().getIdVoyage()) : "NULL";

        String offreVal = (r.getId_offre() != null)
                ? String.valueOf(r.getId_offre().getId_offre()) : "NULL";

        String passeportVal = (r.getNum_passeport() != null)
                ? "'" + r.getNum_passeport().replace("'", "''") + "'"
                : "NULL";

        String req = "UPDATE reservation SET "
                + "date_reservation='"  + r.getDate_reservation()          + "', "
                + "prix_reservation="   + r.getPrix_reservation()           + ", "
                + "etat='"              + r.getEtat()                       + "', "
                + "email='"             + r.getEmail()                      + "', "
                + "num_tel='"           + r.getNum_tel()                    + "', "
                + "commentaire='"       + r.getCommentaire()                + "', "
                + "nombre_personnes="   + r.getNombre_personnes()           + ", "
                + "num_passeport="      + passeportVal                      + ", " // ← ajouté
                + "id_personne="        + personneVal                       + ", "
                + "id_statut="          + r.getId_statut().getId_statut()   + ", "
                + "id_voyage="          + voyageVal                         + ", "
                + "id_offre="           + offreVal
                + " WHERE id_reservation=" + r.getId_reservation();

        return st.executeUpdate(req) > 0;
    }

    // ══════════════════════════════════════════════════
    //  FIND BY ID
    // ══════════════════════════════════════════════════

    @Override
    public Reservation findbyId(int id) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM reservation WHERE id_reservation=" + id);
        if (rs.next()) return mapRow(rs);
        return null;
    }

    // ══════════════════════════════════════════════════
    //  READ ALL
    // ══════════════════════════════════════════════════

    @Override
    public List<Reservation> readAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT * FROM reservation");
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    // ══════════════════════════════════════════════════
    //  MAPPING ResultSet → Reservation
    // ══════════════════════════════════════════════════

    private Reservation mapRow(ResultSet rs) throws SQLException {

        Reservation r = new Reservation();

        r.setId_reservation(rs.getInt("id_reservation"));
        r.setDate_reservation(rs.getDate("date_reservation"));
        r.setPrix_reservation(rs.getDouble("prix_reservation"));
        r.setEtat(rs.getString("etat"));
        r.setEmail(rs.getString("email"));
        r.setNum_tel(rs.getString("num_tel"));
        r.setCommentaire(rs.getString("commentaire"));
        r.setNombre_personnes(rs.getInt("nombre_personnes"));
        r.setNum_passeport(rs.getString("num_passeport"));   // ← lu depuis la BDD

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

        StatutReservation s = new StatutReservation();
        s.setId_statut(rs.getInt("id_statut"));
        r.setId_statut(s);

        int idOffre = rs.getInt("id_offre");
        if (!rs.wasNull()) {
            Offre o = new Offre();
            o.setId_offre(idOffre);
            r.setId_offre(o);
        }

        return r;
    }
}