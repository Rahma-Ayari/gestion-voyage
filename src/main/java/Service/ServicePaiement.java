package Service;

import Entite.Paiement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePaiement implements IService<Paiement> {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_voyage", "root", "");
    }

    @Override
    public boolean ajouter(Paiement p) throws SQLException {
        String sql = "INSERT INTO paiement(montant, date_paiement, statut, id_reservation, id_type_paiement) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, p.getMontant());
            ps.setTimestamp(2, Timestamp.valueOf(p.getDatePaiement()));
            ps.setString(3, p.getStatut());
            ps.setInt(4, p.getIdReservation());
            ps.setInt(5, p.getTypePaiement().getIdTypePaiement());
            int rows = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) p.setIdPaiement(rs.getInt(1));
            return rows > 0;
        }
    }

    @Override
    public boolean supprimer(Paiement p) throws SQLException {
        String sql = "DELETE FROM paiement WHERE id_paiement=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, p.getIdPaiement());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Paiement p) throws SQLException {
        String sql = "UPDATE paiement SET montant=?, date_paiement=?, statut=?, id_reservation=?, id_type_paiement=? WHERE id_paiement=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, p.getMontant());
            ps.setTimestamp(2, Timestamp.valueOf(p.getDatePaiement()));
            ps.setString(3, p.getStatut());
            ps.setInt(4, p.getIdReservation());
            ps.setInt(5, p.getTypePaiement().getIdTypePaiement());
            ps.setInt(6, p.getIdPaiement());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Paiement> readAll() throws SQLException {
        List<Paiement> list = new ArrayList<>();
        String sql = "SELECT * FROM paiement";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                list.add(findbyId(rs.getInt("id_paiement")));
            }
        }
        return list;
    }

    @Override
    public Paiement findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM paiement WHERE id_paiement=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Paiement p = new Paiement();
                p.setIdPaiement(rs.getInt("id_paiement"));
                p.setMontant(rs.getDouble("montant"));
                p.setDatePaiement(rs.getTimestamp("date_paiement").toLocalDateTime());
                p.setStatut(rs.getString("statut"));
                p.setIdReservation(rs.getInt("id_reservation"));

                ServiceTypePaiement stp = new ServiceTypePaiement();
                p.setTypePaiement(stp.findbyId(rs.getInt("id_type_paiement")));

                return p;
            }
        }
        return null;
    }
}

