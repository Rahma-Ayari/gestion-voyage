package Service;
import Entite.TypePaiement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTypePaiement implements IService<TypePaiement> {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_voyage", "root", "");
    }

    @Override
    public boolean ajouter(TypePaiement t) throws SQLException {
        String sql = "INSERT INTO type_paiement(libelle, description) VALUES(?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getLibelle());
            ps.setString(2, t.getDescription());
            int rows = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) t.setIdTypePaiement(rs.getInt(1));
            return rows > 0;
        }
    }

    @Override
    public boolean supprimer(TypePaiement t) throws SQLException {
        String sql = "DELETE FROM type_paiement WHERE id_type_paiement=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, t.getIdTypePaiement());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(TypePaiement t) throws SQLException {
        String sql = "UPDATE type_paiement SET libelle=?, description=? WHERE id_type_paiement=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getLibelle());
            ps.setString(2, t.getDescription());
            ps.setInt(3, t.getIdTypePaiement());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<TypePaiement> readAll() throws SQLException {
        List<TypePaiement> list = new ArrayList<>();
        String sql = "SELECT * FROM type_paiement";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                TypePaiement t = new TypePaiement();
                t.setIdTypePaiement(rs.getInt("id_type_paiement"));
                t.setLibelle(rs.getString("libelle"));
                t.setDescription(rs.getString("description"));
                list.add(t);
            }
        }
        return list;
    }

    @Override
    public TypePaiement findbyId(int id) throws SQLException {
        String sql = "SELECT * FROM type_paiement WHERE id_type_paiement=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                TypePaiement t = new TypePaiement();
                t.setIdTypePaiement(rs.getInt("id_type_paiement"));
                t.setLibelle(rs.getString("libelle"));
                t.setDescription(rs.getString("description"));
                return t;
            }
        }
        return null;
    }
}

