package Service;

import Entite.Budget;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceBudget implements IService<Budget> {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_voyage", "root", "");
    }

    @Override
    public boolean ajouter(Budget b) throws SQLException {
        String sql = "INSERT INTO budget(montant_max, montant_utilise, id_utilisateur) VALUES(?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, b.getMontantMax());
            ps.setDouble(2, b.getMontantUtilise());
            ps.setInt(5, b.getIdUtilisateur());
            int rows = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) b.setIdBudget(rs.getInt(1));
            return rows > 0;
        }
    }

    @Override
    public boolean supprimer(Budget b) throws SQLException {
        String sql = "DELETE FROM budget WHERE id_budget=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, b.getIdBudget());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Budget b) throws SQLException {
        String sql = "UPDATE budget SET montant_max=?, montant_utilise=?, id_utilisateur=? WHERE id_budget=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, b.getMontantMax());
            ps.setDouble(2, b.getMontantUtilise());
            ps.setInt(5, b.getIdUtilisateur());
            ps.setInt(6, b.getIdBudget());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Budget> readAll() throws SQLException {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budget";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                list.add(findbyId(rs.getInt("id_budget")));
            }
        }
        return list;
    }

    @Override
    public Budget findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM budget WHERE id_budget=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Budget b = new Budget();
                b.setIdBudget(rs.getInt("id_budget"));
                b.setMontantMax(rs.getDouble("montant_max"));
                b.setMontantUtilise(rs.getDouble("montant_utilise"));
                b.setIdUtilisateur(rs.getInt("id_utilisateur"));
                return b;
            }
        }
        return null;
    }
}
