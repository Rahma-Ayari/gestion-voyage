package Service;

import Entite.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceNotification implements IService<Notification> {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion-voyage", "root", "");
    }

    @Override
    public boolean ajouter(Notification n) throws SQLException {
        String sql = "INSERT INTO notification(message, date_notification, lu, type_notification, id_reservation, id_utilisateur)\n" +
                "VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getMessage());
            ps.setTimestamp(2, Timestamp.valueOf(n.getDateNotification()));
            ps.setBoolean(3, n.isLu());
            ps.setString(4, n.getTypeNotification());
            ps.setInt(5, n.getIdReservation());
            int rows = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) n.setIdNotification(rs.getInt(1));
            return rows > 0;
        }
    }

    @Override
    public boolean supprimer(Notification n) throws SQLException {
        String sql = "DELETE FROM notification WHERE id_notification=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, n.getIdNotification());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Notification n) throws SQLException {
        String sql = "UPDATE notification SET message=?, date_notification=?, lu=?, type_notification=?, id_reservation=? WHERE id_notification=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, n.getMessage());
            ps.setTimestamp(2, Timestamp.valueOf(n.getDateNotification()));
            ps.setBoolean(3, n.isLu());
            ps.setString(4, n.getTypeNotification());
            ps.setInt(5, n.getIdReservation());
            ps.setInt(6, n.getIdNotification());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Notification> readAll() throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                list.add(findbyId(rs.getInt("id_notification")));
            }
        }
        return list;
    }
    public List<Notification> getNotificationsByUser(int userId) {

        List<Notification> notifications = new ArrayList<>();

        String sql = "SELECT * FROM notification WHERE id_utilisateur = ? ORDER BY date_notification DESC";

        try {

            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Notification n = new Notification();

                n.setIdNotification(rs.getInt("id_notification"));
                n.setMessage(rs.getString("message"));
                n.setDateNotification(rs.getTimestamp("date_notification").toLocalDateTime());
                n.setLu(rs.getBoolean("lu"));
                n.setTypeNotification(rs.getString("type_notification"));
                n.setIdReservation(rs.getInt("id_reservation"));
                n.setIdUtilisateur(rs.getInt("id_utilisateur"));

                notifications.add(n);
            }

        } catch (SQLException e) {
            System.out.println("Erreur récupération notifications : " + e.getMessage());
        }

        return notifications;
    }
    public void markAsRead(int notificationId) {

        String sql = "UPDATE notification SET lu = true WHERE id_notification = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur markAsRead : " + e.getMessage());
        }
    }
    @Override
    public Notification findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM notification WHERE id_notification=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Notification n = new Notification();
                n.setIdNotification(rs.getInt("id_notification"));
                n.setMessage(rs.getString("message"));
                n.setDateNotification(rs.getTimestamp("date_notification").toLocalDateTime());
                n.setLu(rs.getBoolean("lu"));
                n.setTypeNotification(rs.getString("type_notification"));
                n.setIdReservation(rs.getInt("id_reservation"));
                return n;
            }
        }
        return null;
    }
}
