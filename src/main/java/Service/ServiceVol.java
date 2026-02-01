package Service;

import Entite.Vol;
import Entite.Destination;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceVol implements IService<Vol> {
    private Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceVol() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean ajouter(Vol v) throws SQLException {
        boolean test = false;
        int res = -1;
        String req = "INSERT INTO `vol` (`numero_vol`, `compagnie`, `date_depart`, `date_arrivee`, `prix`, `id_destination`) VALUES ('"
                + v.getNumeroVol() + "', '"
                + v.getCompagnie() + "', '"
                + Timestamp.valueOf(v.getDateDepart()) + "', '"
                + Timestamp.valueOf(v.getDateArrivee()) + "', "
                + v.getPrix() + ", "
                + v.getDestination().getIdDestination() + ");";
        res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }

    @Override
    public boolean supprimer(Vol v) throws SQLException {
        boolean test = false;
        String req = "DELETE FROM vol WHERE id_vol = " + v.getIdVol();
        int res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }


    @Override
    public boolean modifier(Vol v) throws SQLException {
        boolean test = false;
        String req = "UPDATE vol SET "
                + "numero_vol = '" + v.getNumeroVol() + "', "
                + "compagnie = '" + v.getCompagnie() + "', "
                + "date_depart = '" + Timestamp.valueOf(v.getDateDepart()) + "', "
                + "date_arrivee = '" + Timestamp.valueOf(v.getDateArrivee()) + "', "
                + "prix = " + v.getPrix() + ", "
                + "id_destination = " + v.getDestination().getIdDestination()
                + " WHERE id_vol = " + v.getIdVol();

        int res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }


    @Override
    public Vol findbyId(int id) throws SQLException {
        Vol vol = null;
        String req = "SELECT * FROM vol WHERE id_vol = " + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Timestamp dateDepartTs = rs.getTimestamp("date_depart");
            Timestamp dateArriveeTs = rs.getTimestamp("date_arrivee");

            LocalDateTime dateDepart = dateDepartTs != null ? dateDepartTs.toLocalDateTime() : null;
            LocalDateTime dateArrivee = dateArriveeTs != null ? dateArriveeTs.toLocalDateTime() : null;

            Destination d = new Destination();
            d.setIdDestination(rs.getInt("id_destination"));

            vol = new Vol(
                    rs.getInt("id_vol"),
                    rs.getString("numero_vol"),
                    rs.getString("compagnie"),
                    dateDepart,
                    dateArrivee,
                    rs.getDouble("prix"),
                    d
            );
        }
        return vol;
    }


    @Override
    public List<Vol> readAll() throws SQLException {
        List<Vol> list = new ArrayList<>();
        String query = "SELECT * FROM `vol`";
        ResultSet rest = st.executeQuery(query);
        while (rest.next()) {
            int id = rest.getInt(1);
            String numeroVol = rest.getString("numero_vol");
            String compagnie = rest.getString(3);
            Timestamp dateDepartTs = rest.getTimestamp("date_depart");
            Timestamp dateArriveeTs = rest.getTimestamp("date_arrivee");
            LocalDateTime dateDepart = dateDepartTs != null ? dateDepartTs.toLocalDateTime() : null;
            LocalDateTime dateArrivee = dateArriveeTs != null ? dateArriveeTs.toLocalDateTime() : null;
            double prix = rest.getDouble("prix");
            int idDestination = rest.getInt("id_destination");
            Destination d = new Destination();
            d.setIdDestination(idDestination);
            Vol vol = new Vol(id, numeroVol, compagnie, dateDepart, dateArrivee, prix, d);
            list.add(vol);
        }
        return list;
    }
}
