package Service;

import Entite.Destination;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceDestination implements IService<Destination> {

    private final Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceDestination() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /* ══════════════════════════════════════════
       AJOUTER
    ══════════════════════════════════════════ */
    @Override
    public boolean ajouter(Destination d) throws SQLException {
        String req = "INSERT INTO destination (pays, ville, description, date_debut, date_fin) VALUES ('"
                + d.getPays() + "', '"
                + d.getVille() + "', '"
                + d.getDescription() + "', '"
                + d.getDateDebut() + "', '"
                + d.getDateFin() + "')";

        int res = st.executeUpdate(req);
        return res > 0;
    }

    /* ══════════════════════════════════════════
       SUPPRIMER
    ══════════════════════════════════════════ */
    @Override
    public boolean supprimer(Destination d) throws SQLException {
        String req = "DELETE FROM destination WHERE id_destination = "
                + d.getIdDestination();

        int res = st.executeUpdate(req);
        return res > 0;
    }

    /* ══════════════════════════════════════════
       MODIFIER
    ══════════════════════════════════════════ */
    @Override
    public boolean modifier(Destination d) throws SQLException {
        String req = "UPDATE destination SET "
                + "pays = '" + d.getPays() + "', "
                + "ville = '" + d.getVille() + "', "
                + "description = '" + d.getDescription() + "', "
                + "date_debut = '" + d.getDateDebut() + "', "
                + "date_fin = '" + d.getDateFin() + "' "
                + "WHERE id_destination = " + d.getIdDestination();

        int res = st.executeUpdate(req);
        return res > 0;
    }

    /* ══════════════════════════════════════════
       FIND BY ID
    ══════════════════════════════════════════ */
    @Override
    public Destination findbyId(int id) throws SQLException {

        String req = "SELECT * FROM destination WHERE id_destination = " + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Destination d = new Destination();
            d.setIdDestination(rs.getInt("id_destination"));
            d.setPays(rs.getString("pays"));
            d.setVille(rs.getString("ville"));
            d.setDescription(rs.getString("description"));
            d.setDateDebut(rs.getDate("date_debut").toLocalDate());
            d.setDateFin(rs.getDate("date_fin").toLocalDate());
            return d;
        }

        return null;
    }

    /* ══════════════════════════════════════════
       READ ALL
    ══════════════════════════════════════════ */
    @Override
    public List<Destination> readAll() throws SQLException {

        List<Destination> list = new ArrayList<>();
        String req = "SELECT * FROM destination";
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Destination d = new Destination();
            d.setIdDestination(rs.getInt("id_destination"));
            d.setPays(rs.getString("pays"));
            d.setVille(rs.getString("ville"));
            d.setDescription(rs.getString("description"));
            d.setDateDebut(rs.getDate("date_debut").toLocalDate());
            d.setDateFin(rs.getDate("date_fin").toLocalDate());
            list.add(d);
        }

        return list;
    }

    /* ══════════════════════════════════════════
       FILTRER par plage de dates utilisateur
       Retourne les destinations dont la période [date_debut, date_fin]
       CONTIENT la plage demandée par l'utilisateur
       (date_debut <= userDebut  ET  date_fin >= userFin)
    ══════════════════════════════════════════ */
    public List<Destination> findByDateRange(LocalDate debut, LocalDate fin) throws SQLException {

        List<Destination> list = new ArrayList<>();

        String req = "SELECT * FROM destination WHERE date_debut <= '"
                + debut + "' AND date_fin >= '" + fin + "'";

        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Destination d = new Destination();
            d.setIdDestination(rs.getInt("id_destination"));
            d.setPays(rs.getString("pays"));
            d.setVille(rs.getString("ville"));
            d.setDescription(rs.getString("description"));
            d.setDateDebut(rs.getDate("date_debut").toLocalDate());
            d.setDateFin(rs.getDate("date_fin").toLocalDate());
            list.add(d);
        }

        return list;
    }
}