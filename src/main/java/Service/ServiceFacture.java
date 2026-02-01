package Service;

import Entite.Facture;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFacture implements IService<Facture> {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_voyage", "root", "");
    }

    @Override
    public boolean ajouter(Facture f) throws SQLException {
        String sql = "INSERT INTO facture(numero_facture, date_facture, montant_total, id_paiement) VALUES(?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getNumeroFacture());
            ps.setDate(2, Date.valueOf(f.getDateFacture()));
            ps.setDouble(3, f.getMontantTotal());
            ps.setInt(4, f.getPaiement().getIdPaiement());
            int rows = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) f.setIdFacture(rs.getInt(1));
            return rows > 0;
        }
    }

    @Override
    public boolean supprimer(Facture f) throws SQLException {
        String sql = "DELETE FROM facture WHERE id_facture=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, f.getIdFacture());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Facture f) throws SQLException {
        String sql = "UPDATE facture SET numero_facture=?, date_facture=?, montant_total=?, id_paiement=? WHERE id_facture=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, f.getNumeroFacture());
            ps.setDate(2, Date.valueOf(f.getDateFacture()));
            ps.setDouble(3, f.getMontantTotal());
            ps.setInt(4, f.getPaiement().getIdPaiement());
            ps.setInt(5, f.getIdFacture());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Facture> readAll() throws SQLException {
        List<Facture> list = new ArrayList<>();
        String sql = "SELECT * FROM facture";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                list.add(findbyId(rs.getInt("id_facture")));
            }
        }
        return list;
    }

    @Override
    public Facture findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM facture WHERE id_facture=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Facture f = new Facture();
                f.setIdFacture(rs.getInt("id_facture"));
                f.setNumeroFacture(rs.getString("numero_facture"));
                f.setDateFacture(rs.getDate("date_facture").toLocalDate());
                f.setMontantTotal(rs.getDouble("montant_total"));

                ServicePaiement sp = new ServicePaiement();
                f.setPaiement(sp.findbyId(rs.getInt("id_paiement")));

                return f;
            }
        }
        return null;
    }
}
