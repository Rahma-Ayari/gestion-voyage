package Service;

import Entite.Role;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRole implements IService<Role> {
    private Connection con = DataSource.getInstance().getCon();

    @Override
    public boolean ajouter(Role r) throws SQLException {
        String req = "INSERT INTO role(nom_role) VALUES (?)";
        PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, r.getNomRole());
        int res = ps.executeUpdate();
        if (res > 0) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) r.setId(rs.getInt(1));
            return true;
        }
        return false;
    }

    @Override
    public boolean supprimer(Role r) throws SQLException {
        String req = "DELETE FROM role WHERE id=?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, r.getId());
        return ps.executeUpdate() > 0;
    }

    @Override
    public boolean modifier(Role r) throws SQLException {
        String req = "UPDATE role SET nom_role=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setString(1, r.getNomRole());
        ps.setInt(2, r.getId());
        return ps.executeUpdate() > 0;
    }

    @Override
    public List<Role> readAll() throws SQLException {
        List<Role> roles = new ArrayList<>();
        String req = "SELECT * FROM role";
        ResultSet rs = con.createStatement().executeQuery(req);
        while (rs.next()) {
            roles.add(new Role(rs.getInt("id"), rs.getString("nom_role")));
        }
        return roles;
    }

    @Override
    public Role findbyId(int id) throws SQLException {
        String req = "SELECT * FROM role WHERE id=?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return new Role(rs.getInt("id"), rs.getString("nom_role"));
        return null;
    }
}