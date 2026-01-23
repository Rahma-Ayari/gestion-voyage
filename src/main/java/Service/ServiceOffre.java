package Service;

import Entite.Offre;
import Entite.Voyage;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOffre {

    private Connection cnx = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceOffre() {
        try {
            st = cnx.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Ajouter une offre
    public boolean ajouter(Offre o) throws SQLException {
        String req = "INSERT INTO offre(type, prix, description, disponibilite, id_voyage) VALUES ("
                + "'" + o.getType() + "',"
                + o.getPrix() + ","
                + "'" + o.getDescription() + "',"
                + o.isDisponibilite() + ","
                + o.getvoyage().getIdVoyage() + ")";
        return st.executeUpdate(req) > 0;
    }

    // Supprimer une offre
    public boolean supprimer(int id) throws SQLException {
        String req = "DELETE FROM offre WHERE id_offre=" + id;
        return st.executeUpdate(req) > 0;
    }

    // Modifier une offre
    public boolean modifier(Offre o) throws SQLException {
        String req = "UPDATE offre SET "
                + "type='" + o.getType() + "', "
                + "prix=" + o.getPrix() + ", "
                + "description='" + o.getDescription() + "', "
                + "disponibilite=" + o.isDisponibilite() + ", "
                + "id_voyage=" + o.getvoyage().getIdVoyage()
                + " WHERE id_offre=" + o.getId_offre();
        return st.executeUpdate(req) > 0;
    }

    // Chercher une offre par id
    public Offre findById(int id) throws SQLException {
        String req = "SELECT * FROM offre WHERE id_offre=" + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Offre o = new Offre();
            o.setId_offre(rs.getInt("id_offre"));
            o.setType(rs.getString("type"));
            o.setPrix(rs.getDouble("prix"));
            o.setDescription(rs.getString("description"));
            o.setDisponibilite(rs.getBoolean("disponibilite"));

            // Pour le voyage associé
            int idVoyage = rs.getInt("id_voyage");
            ServiceVoyage sv = new ServiceVoyage();
            Voyage v = sv.findbyId(idVoyage);
            o.setvoyage(v);

            return o;
        }
        return null;
    }

    // Lire toutes les offres
    public List<Offre> readAll() throws SQLException {
        List<Offre> list = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT * FROM offre");

        ServiceVoyage sv = new ServiceVoyage();

        while (rs.next()) {
            Offre o = new Offre();
            o.setId_offre(rs.getInt("id_offre"));
            o.setType(rs.getString("type"));
            o.setPrix(rs.getDouble("prix"));
            o.setDescription(rs.getString("description"));
            o.setDisponibilite(rs.getBoolean("disponibilite"));

            int idVoyage = rs.getInt("id_voyage");
            Voyage v = sv.findbyId(idVoyage);
            o.setvoyage(v);

            list.add(o);
        }
        return list;
    }
}
