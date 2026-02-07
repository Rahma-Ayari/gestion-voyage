package Service;

import Entite.Reservation;
import Entite.Personne;
import Entite.Voyage;
import Entite.StatutReservation;
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
        String req = "INSERT INTO reservation(date_reservation, prix_reservation, etat, id_personne, id_voyage, id_statut) VALUES ('"
                + r.getDate_reservation() + "',"
                + r.getPrix_reservation() + ",'"
                + r.getEtat() + "',"
                + r.getId_personne().getIdUtilisateur() + ","
                + r.getId_voyage().getIdVoyage() + ","
                + r.getId_statut().getId_statut() + ")";
        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean supprimer(Reservation r) throws SQLException {
        String req = "DELETE FROM reservation WHERE id_reservation=" + r.getId_reservation();
        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean modifier(Reservation r) throws SQLException {
        String req = "UPDATE reservation SET "
                + "date_reservation='" + r.getDate_reservation() + "', "
                + "prix_reservation=" + r.getPrix_reservation() + ", "
                + "etat='" + r.getEtat() + "', "
                + "id_personne=" + r.getId_personne().getIdUtilisateur() + ", "
                + "id_voyage=" + r.getId_voyage().getIdVoyage() + ", "
                + "id_statut=" + r.getId_statut().getId_statut() + " "
                + "WHERE id_reservation=" + r.getId_reservation();
        return st.executeUpdate(req) > 0;
    }

    @Override
    public Reservation findbyId(int id) throws SQLException {
        String req = "SELECT * FROM reservation WHERE id_reservation=" + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Reservation r = new Reservation();
            r.setId_reservation(rs.getInt("id_reservation"));
            r.setDate_reservation(rs.getDate("date_reservation"));
            r.setPrix_reservation(rs.getDouble("prix_reservation"));
            r.setEtat(rs.getString("etat"));

            // Crée les objets liés
            Personne p = new Personne();
            p.setIdUtilisateur(rs.getInt("id_personne"));
            r.setId_personne(p);

            Voyage v = new Voyage();
            v.setIdVoyage(rs.getInt("id_voyage"));
            r.setId_voyage(v);

            StatutReservation s = new StatutReservation();
            s.setId_statut(rs.getInt("id_statut"));
            r.setId_statut(s);

            return r;
        }
        return null;
    }

    @Override
    public List<Reservation> readAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String req = "SELECT * FROM reservation";
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Reservation r = new Reservation();
            r.setId_reservation(rs.getInt("id_reservation"));
            r.setDate_reservation(rs.getDate("date_reservation"));
            r.setPrix_reservation(rs.getDouble("prix_reservation"));
            r.setEtat(rs.getString("etat"));

            Personne p = new Personne();
            p.setIdUtilisateur(rs.getInt("id_personne"));
            r.setId_personne(p);

            Voyage v = new Voyage();
            v.setIdVoyage(rs.getInt("id_voyage"));
            r.setId_voyage(v);

            StatutReservation s = new StatutReservation();
            s.setId_statut(rs.getInt("id_statut"));
            r.setId_statut(s);

            list.add(r);
        }
        return list;
    }
}
