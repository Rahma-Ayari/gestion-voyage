package Service;

import Entite.StatutReservation;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceStatutReservation implements IService<StatutReservation> {

    private Connection cnx = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceStatutReservation() {
        try {
            st = cnx.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean ajouter(StatutReservation s) throws SQLException {
        String req = "INSERT INTO statut_reservation(libelle) VALUES ('" + s.getLibelle() + "')";
        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean supprimer(StatutReservation s) throws SQLException {
        String req = "DELETE FROM statut_reservation WHERE id_statut=" + s.getId_statut();
        return st.executeUpdate(req) > 0;
    }

    @Override
    public boolean modifier(StatutReservation s) throws SQLException {
        String req = "UPDATE statut_reservation SET libelle='" + s.getLibelle() + "' WHERE id_statut=" + s.getId_statut();
        return st.executeUpdate(req) > 0;
    }

    @Override
    public StatutReservation findbyId(int id) throws SQLException {
        String req = "SELECT * FROM statut_reservation WHERE id_statut=" + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            StatutReservation s = new StatutReservation();
            s.setId_statut(rs.getInt("id_statut"));
            s.setLibelle(rs.getString("libelle"));
            return s;
        }
        return null;
    }

    @Override
    public List<StatutReservation> readAll() throws SQLException {
        List<StatutReservation> list = new ArrayList<>();
        String req = "SELECT * FROM statut_reservation";
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            StatutReservation s = new StatutReservation();
            s.setId_statut(rs.getInt("id_statut"));
            s.setLibelle(rs.getString("libelle"));
            list.add(s);
        }
        return list;
    }
}
