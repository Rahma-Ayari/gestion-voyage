package Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Entite.Budget;
import Utils.DataSource;

public class ServiceBudget {

    private final Connection connect = DataSource.getInstance().getCon();

    /**
     * Enregistre un budget pour un voyage.
     * Suppose l'existence d'une table:
     *   budget(id_budget PK AUTO_INCREMENT, id_voyage, total_vol, total_hotel,
     *          total_activite, total_service, total_global, date_creation TIMESTAMP)
     */
    public void enregistrer(Budget b) throws SQLException {
        String sql = "INSERT INTO budget (id_voyage, total_vol, total_hotel, " +
                     "total_activite, total_service, total_global) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt   (1, b.getIdVoyage());
            ps.setDouble(2, b.getTotalVol());
            ps.setDouble(3, b.getTotalHotel());
            ps.setDouble(4, b.getTotalActivite());
            ps.setDouble(5, b.getTotalService());
            ps.setDouble(6, b.getTotalGlobal());
            ps.executeUpdate();
        }
    }

    /**
     * Récupère le dernier budget enregistré pour un voyage donné.
     */
    public Budget findByVoyage(int idVoyage) throws SQLException {
        String sql = "SELECT * FROM budget WHERE id_voyage = ? " +
                     "ORDER BY id_budget DESC LIMIT 1";
        try (PreparedStatement ps = connect.prepareStatement(sql)) {
            ps.setInt(1, idVoyage);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Budget b = new Budget();
                b.setIdBudget    (rs.getInt("id_budget"));
                b.setIdVoyage    (rs.getInt("id_voyage"));
                b.setTotalVol    (rs.getDouble("total_vol"));
                b.setTotalHotel  (rs.getDouble("total_hotel"));
                b.setTotalActivite(rs.getDouble("total_activite"));
                b.setTotalService(rs.getDouble("total_service"));
                b.setTotalGlobal (rs.getDouble("total_global"));
                return b;
            }
            return null;
        }
    }
}

