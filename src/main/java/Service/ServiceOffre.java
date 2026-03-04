//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package Service;

import Entite.Offre;
import Utils.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceOffre {
    private Connection cnx = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceOffre() {
        try {
            this.st = this.cnx.createStatement();
        } catch (SQLException e) {
            System.out.println("Erreur initialisation statement : " + e.getMessage());
        }

    }

    public boolean ajouter(Offre o) throws SQLException {
        StringBuilder req = new StringBuilder("INSERT INTO offre(type, prix, description, disponibilite, id_voyage");
        if (o.getVol() != null) {
            req.append(", id_vol");
        }

        if (o.getHotel() != null) {
            req.append(", id_hotel");
        }

        if (o.getDestination() != null) {
            req.append(", id_destination");
        }

        if (o.getActivite() != null) {
            req.append(", id_activite");
        }

        if (o.getImagePath() != null) {
            req.append(", image_path");
        }

        if (o.getDateDebut() != null) {
            req.append(", date_debut");
        }

        if (o.getDateFin() != null) {
            req.append(", date_fin");
        }

        req.append(") VALUES ('").append(o.getType()).append("',").append(o.getPrix()).append(",'").append(o.getDescription()).append("',").append(o.isDisponibilite()).append(",").append(o.getVoyage().getIdVoyage());
        if (o.getVol() != null) {
            req.append(",").append(o.getVol().getIdVol());
        }

        if (o.getHotel() != null) {
            req.append(",").append(o.getHotel().getIdHotel());
        }

        if (o.getDestination() != null) {
            req.append(",").append(o.getDestination().getIdDestination());
        }

        if (o.getActivite() != null) {
            req.append(",").append(o.getActivite().getIdActivite());
        }

        if (o.getImagePath() != null) {
            req.append(",'").append(o.getImagePath()).append("'");
        }

        if (o.getDateDebut() != null) {
            req.append(",'").append(Date.valueOf(o.getDateDebut())).append("'");
        }

        if (o.getDateFin() != null) {
            req.append(",'").append(Date.valueOf(o.getDateFin())).append("'");
        }

        req.append(")");
        return this.st.executeUpdate(req.toString()) > 0;
    }

    public boolean supprimer(int id) throws SQLException {
        String req = "DELETE FROM offre WHERE id_offre=" + id;
        return this.st.executeUpdate(req) > 0;
    }

    public boolean modifier(Offre o) throws SQLException {
        StringBuilder req = (new StringBuilder("UPDATE offre SET ")).append("type='").append(o.getType()).append("', ").append("prix=").append(o.getPrix()).append(", ").append("description='").append(o.getDescription()).append("', ").append("disponibilite=").append(o.isDisponibilite()).append(", ").append("id_voyage=").append(o.getVoyage().getIdVoyage());
        req.append(o.getVol() != null ? ", id_vol=" + o.getVol().getIdVol() : ", id_vol=NULL");
        req.append(o.getHotel() != null ? ", id_hotel=" + o.getHotel().getIdHotel() : ", id_hotel=NULL");
        req.append(o.getDestination() != null ? ", id_destination=" + o.getDestination().getIdDestination() : ", id_destination=NULL");
        req.append(o.getActivite() != null ? ", id_activite=" + o.getActivite().getIdActivite() : ", id_activite=NULL");
        req.append(o.getImagePath() != null ? ", image_path='" + o.getImagePath() + "'" : ", image_path=NULL");
        req.append(o.getDateDebut() != null ? ", date_debut='" + String.valueOf(Date.valueOf(o.getDateDebut())) + "'" : ", date_debut=NULL");
        req.append(o.getDateFin() != null ? ", date_fin='" + String.valueOf(Date.valueOf(o.getDateFin())) + "'" : ", date_fin=NULL");
        req.append(" WHERE id_offre=").append(o.getId_offre());
        return this.st.executeUpdate(req.toString()) > 0;
    }

    public Offre findById(int id) throws SQLException {
        String req = "SELECT * FROM offre WHERE id_offre=" + id;
        ResultSet rs = this.st.executeQuery(req);
        return rs.next() ? this.createOffreFromResultSet(rs) : null;
    }

    public List<Offre> readAll() throws SQLException {
        List<Offre> list = new ArrayList();
        ResultSet rs = this.st.executeQuery("SELECT * FROM offre");

        while(rs.next()) {
            list.add(this.createOffreFromResultSet(rs));
        }

        return list;
    }

    private Offre createOffreFromResultSet(ResultSet rs) throws SQLException {
        Offre o = new Offre();
        o.setId_offre(rs.getInt("id_offre"));
        o.setType(rs.getString("type"));
        o.setPrix(rs.getDouble("prix"));
        o.setDescription(rs.getString("description"));
        o.setDisponibilite(rs.getBoolean("disponibilite"));
        ServiceVoyage sv = new ServiceVoyage();
        o.setVoyage(sv.findbyId(rs.getInt("id_voyage")));
        Object idVolObj = rs.getObject("id_vol");
        if (idVolObj != null) {
            o.setVol((new ServiceVol()).findbyId((Integer)idVolObj));
        }

        Object idHotelObj = rs.getObject("id_hotel");
        if (idHotelObj != null) {
            o.setHotel((new ServiceHotel()).findbyId((Integer)idHotelObj));
        }

        Object idDestinationObj = rs.getObject("id_destination");
        if (idDestinationObj != null) {
            o.setDestination((new ServiceDestination()).findbyId((Integer)idDestinationObj));
        }

        Object idActiviteObj = rs.getObject("id_activite");
        if (idActiviteObj != null) {
            o.setActivite((new ServiceActivite()).findbyId((Integer)idActiviteObj));
        }

        o.setImagePath(rs.getString("image_path"));
        Date dDebut = rs.getDate("date_debut");
        if (dDebut != null) {
            o.setDateDebut(dDebut.toLocalDate());
        }

        Date dFin = rs.getDate("date_fin");
        if (dFin != null) {
            o.setDateFin(dFin.toLocalDate());
        }

        return o;
    }
}
