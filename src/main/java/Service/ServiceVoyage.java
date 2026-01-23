package Service;

import Entite.Voyage;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceVoyage implements IService<Voyage>{

    private Connection cnx = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceVoyage() {
        try {
            st = cnx.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean ajouter(Voyage v) throws SQLException {
        String req = "INSERT INTO voyage(duree, dateDebut, dateFin, rythme, id_destination, id_vol) VALUES ("
                + v.getDuree() + ",'"
                + Date.valueOf(v.getDateDebut()) + "','"
                + Date.valueOf(v.getDateFin()) + "','"
                + v.getRythme() + "',"
                + v.getDestination().getIdDestination() + ","
                + v.getVol().getIdVol() + ")";
        return st.executeUpdate(req) > 0;
    }


    @Override
    public boolean supprimer(Voyage v) throws SQLException {
        String req = "DELETE FROM voyage WHERE id_voyage=" + v.getIdVoyage();
        return st.executeUpdate(req) > 0;
    }


    public boolean modifier(Voyage v) throws SQLException {
        String req = "UPDATE voyage SET duree="
                + v.getDuree() + ", dateDebut='"
                + Date.valueOf(v.getDateDebut()) + "', dateFin='"
                + Date.valueOf(v.getDateFin()) + "', rythme='"
                + v.getRythme() + "', id_destination="
                + v.getDestination().getIdDestination() + ", id_vol="
                + v.getVol().getIdVol()
                + " WHERE id_voyage=" + v.getIdVoyage();
        return st.executeUpdate(req) > 0;
    }



    @Override
    public Voyage findbyId(int id) throws SQLException {
        String req = "SELECT * FROM voyage WHERE id_voyage=" + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Voyage v = new Voyage();
            v.setIdVoyage(rs.getInt("id_voyage"));
            v.setDuree(rs.getInt("duree"));
            v.setDateDebut(rs.getDate("dateDebut").toLocalDate());
            v.setDateFin(rs.getDate("dateFin").toLocalDate());
            v.setRythme(rs.getString("rythme"));
            return v;
        }
        return null;
    }

    @Override
    public List<Voyage> readAll() throws SQLException {
        List<Voyage> list = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT * FROM voyage");

        while (rs.next()) {
            Voyage v = new Voyage();
            v.setIdVoyage(rs.getInt("id_voyage"));
            v.setDuree(rs.getInt("duree"));
            v.setDateDebut(rs.getDate("dateDebut").toLocalDate());
            v.setDateFin(rs.getDate("dateFin").toLocalDate());
            v.setRythme(rs.getString("rythme"));
            list.add(v);
        }
        return list;
    }
}