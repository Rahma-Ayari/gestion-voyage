package Service;

import Entite.Destination;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDestination implements IService<Destination> {
    private Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceDestination() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean ajouter(Destination d) throws SQLException {
        boolean test = false;
        int res = -1;
        String req = "INSERT INTO `destination` (`pays`, `ville`, `description`) VALUES ('" + d.getPays() + "', '" + d.getVille() + "', '" + d.getDescription() + "');";
        res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }

    @Override
    public boolean supprimer(Destination d) throws SQLException {
        boolean test = false;
        String req = "DELETE FROM destination WHERE id_destination = " + d.getIdDestination();
        int res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }


    @Override
    public boolean modifier(Destination d) throws SQLException {
        boolean test = false;
        String req = "UPDATE destination SET "
                + "pays = '" + d.getPays() + "', "
                + "ville = '" + d.getVille() + "', "
                + "description = '" + d.getDescription() + "' "
                + "WHERE id_destination = " + d.getIdDestination();

        int res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }


    @Override
    public Destination findbyId(int id) throws SQLException {
        Destination destination = null;
        String req = "SELECT * FROM destination WHERE id_destination = " + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            destination = new Destination(
                    rs.getInt("id_destination"),
                    rs.getString("pays"),
                    rs.getString("ville"),
                    rs.getString("description")
            );
        }
        return destination;
    }


    @Override
    public List<Destination> readAll() throws SQLException {
        List<Destination> list = new ArrayList<>();
        String query = "SELECT * FROM `destination`";
        ResultSet rest = st.executeQuery(query);
        while (rest.next()) {
            int id = rest.getInt(1);
            String pays = rest.getString("pays");
            String ville = rest.getString(3);
            String description = rest.getString("description");
            Destination destination = new Destination(id, pays, ville, description);
            list.add(destination);
        }
        return list;
    }
}
