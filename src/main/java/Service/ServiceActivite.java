package Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Entite.Activite;
import Utils.DataSource;

public class ServiceActivite implements IService<Activite> {

    private final Connection connect = DataSource.getInstance().getCon();

    private static final String SELECT_BASE =
            "SELECT a.*, t.libelle AS libelle_type " +
                    "FROM activite a " +
                    "LEFT JOIN type_activite t ON a.id_type_activite = t.id_type_activite ";

    @Override
    public boolean ajouter(Activite a) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "INSERT INTO activite (nom, description, prix, dureeEnHeure, horaire, " +
                        "id_type_activite, id_destination) VALUES (?,?,?,?,?,?,?)");
        ps.setString(1, a.getNom());
        ps.setString(2, a.getDescription());
        ps.setDouble(3, a.getPrix());
        ps.setInt   (4, a.getDureeEnHeure());
        ps.setString(5, a.getHoraire());
        ps.setInt   (6, a.getIdTypeActivite());
        ps.setInt   (7, a.getIdDestination());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean supprimer(Activite a) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "DELETE FROM activite WHERE id_activite = ?");
        ps.setInt(1, a.getIdActivite());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Activite a) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "UPDATE activite SET nom=?, description=?, prix=?, dureeEnHeure=?, " +
                        "horaire=?, id_type_activite=?, id_destination=? WHERE id_activite=?");
        ps.setString(1, a.getNom());
        ps.setString(2, a.getDescription());
        ps.setDouble(3, a.getPrix());
        ps.setInt   (4, a.getDureeEnHeure());
        ps.setString(5, a.getHoraire());
        ps.setInt   (6, a.getIdTypeActivite());
        ps.setInt   (7, a.getIdDestination());
        ps.setInt   (8, a.getIdActivite());
        return ps.executeUpdate() > 0;
    }

    @Override
    public Activite findbyId(int id) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(SELECT_BASE + "WHERE a.id_activite = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapActivite(rs);
        return null;
    }

    @Override
    public List<Activite> readAll() throws SQLException {
        List<Activite> list = new ArrayList<>();
        ResultSet rs = connect.createStatement().executeQuery(SELECT_BASE + "ORDER BY a.nom ASC");
        while (rs.next()) list.add(mapActivite(rs));
        return list;
    }



    public List<Activite> findByDestination(int idDestination) throws SQLException {
        List<Activite> list = new ArrayList<>();
        PreparedStatement ps = connect.prepareStatement(
                SELECT_BASE + "WHERE a.id_destination = ? ORDER BY t.libelle ASC, a.nom ASC");
        ps.setInt(1, idDestination);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapActivite(rs));
        return list;
    }

    public List<Activite> findByDestinationEtRythme(int idDestination, String rythme) throws SQLException {

        String conditionType = switch (rythme.toLowerCase()) {
            case "détendu", "detendu", "relaxed" ->
                    "t.id_type_activite IN (2,5,8,12,13,16,18,20)";
            case "intense", "sportif" ->
                    "t.id_type_activite IN (1,3,4,6,7,9,10,15,19)";
            default ->
                    "t.id_type_activite IN (1,2,8,9,11,12,17,18,20)";
        };

        List<Activite> list = new ArrayList<>();
        PreparedStatement ps = connect.prepareStatement(
                SELECT_BASE + "WHERE a.id_destination = ? AND " + conditionType +
                        " ORDER BY t.libelle ASC, a.nom ASC");
        ps.setInt(1, idDestination);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapActivite(rs));
        return list;
    }

    public void enregistrerActivitesVoyage(int idVoyage, List<Integer> idActivites) throws SQLException {
        PreparedStatement del = connect.prepareStatement(
                "DELETE FROM voyage_activite WHERE id_voyage = ?");
        del.setInt(1, idVoyage);
        del.executeUpdate();

        PreparedStatement ins = connect.prepareStatement(
                "INSERT INTO voyage_activite (id_voyage, id_activite) VALUES (?, ?)");
        for (int idAct : idActivites) {
            ins.setInt(1, idVoyage);
            ins.setInt(2, idAct);
            ins.addBatch();
        }
        ins.executeBatch();
    }

    /**
     * Retourne la liste des activités associées à un voyage (table voyage_activite).
     */
    public List<Activite> findByVoyage(int idVoyage) throws SQLException {
        List<Activite> list = new ArrayList<>();
        String sql =
                SELECT_BASE +
                "JOIN voyage_activite va ON va.id_activite = a.id_activite " +
                "WHERE va.id_voyage = ? " +
                "ORDER BY a.nom ASC";
        PreparedStatement ps = connect.prepareStatement(sql);
        ps.setInt(1, idVoyage);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapActivite(rs));
        return list;
    }

    private Activite mapActivite(ResultSet rs) throws SQLException {
        return new Activite(
                rs.getInt   ("id_activite"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getDouble("prix"),
                rs.getInt   ("dureeEnHeure"),
                rs.getString("horaire"),
                rs.getInt   ("id_type_activite"),
                rs.getString("libelle_type"),
                rs.getInt   ("id_destination")
        );
    }
}