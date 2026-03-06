package Service;

import Entite.Offre;
import Entite.Voyage;
import Entite.Vol;
import Entite.Hotel;
import Entite.Destination;
import Entite.Activite;
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
        StringBuilder req = new StringBuilder("INSERT INTO offre(type, prix, description, disponibilite, id_voyage");

        // Ajouter les colonnes si vol, hotel, destination ou activite sont présents
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

        req.append(") VALUES ('")
                .append(o.getType()).append("',")
                .append(o.getPrix()).append(",'")
                .append(o.getDescription()).append("',")
                .append(o.isDisponibilite()).append(",")
                .append(o.getvoyage().getIdVoyage());

        // Ajouter les valeurs si vol, hotel, destination ou activite sont présents
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

        req.append(")");

        return st.executeUpdate(req.toString()) > 0;
    }

    // Supprimer une offre
    public boolean supprimer(int id) throws SQLException {
        String req = "DELETE FROM offre WHERE id_offre=" + id;
        return st.executeUpdate(req) > 0;
    }

    // Modifier une offre
    public boolean modifier(Offre o) throws SQLException {
        StringBuilder req = new StringBuilder("UPDATE offre SET ")
                .append("type='").append(o.getType()).append("', ")
                .append("prix=").append(o.getPrix()).append(", ")
                .append("description='").append(o.getDescription()).append("', ")
                .append("disponibilite=").append(o.isDisponibilite()).append(", ")
                .append("id_voyage=").append(o.getvoyage().getIdVoyage());

        // Mettre à jour vol
        if (o.getVol() != null) {
            req.append(", id_vol=").append(o.getVol().getIdVol());
        } else {
            req.append(", id_vol=NULL");
        }

        // Mettre à jour hotel
        if (o.getHotel() != null) {
            req.append(", id_hotel=").append(o.getHotel().getIdHotel());
        } else {
            req.append(", id_hotel=NULL");
        }

        // Mettre à jour destination
        if (o.getDestination() != null) {
            req.append(", id_destination=").append(o.getDestination().getIdDestination());
        } else {
            req.append(", id_destination=NULL");
        }

        // Mettre à jour activite
        if (o.getActivite() != null) {
            req.append(", id_activite=").append(o.getActivite().getIdActivite());
        } else {
            req.append(", id_activite=NULL");
        }

        req.append(" WHERE id_offre=").append(o.getId_offre());

        return st.executeUpdate(req.toString()) > 0;
    }

    // Chercher une offre par id
    public Offre findById(int id) throws SQLException {
        String req = "SELECT * FROM offre WHERE id_offre=" + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            return createOffreFromResultSet(rs);
        }
        return null;
    }

    // Lire toutes les offres
    public List<Offre> readAll() throws SQLException {
        List<Offre> list = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT * FROM offre");

        while (rs.next()) {
            list.add(createOffreFromResultSet(rs));
        }
        return list;
    }

    // Méthode helper pour créer une offre à partir du ResultSet
    private Offre createOffreFromResultSet(ResultSet rs) throws SQLException {
        Offre o = new Offre();
        o.setId_offre(rs.getInt("id_offre"));
        o.setType(rs.getString("type"));
        o.setPrix(rs.getDouble("prix"));
        o.setDescription(rs.getString("description"));
        o.setDisponibilite(rs.getBoolean("disponibilite"));

        // Récupérer le voyage associé
        int idVoyage = rs.getInt("id_voyage");
        ServiceVoyage sv = new ServiceVoyage();
        Voyage v = sv.findbyId(idVoyage);
        o.setvoyage(v);

        // Récupérer le vol si présent
        Object idVolObj = rs.getObject("id_vol");
        if (idVolObj != null) {
            int idVol = (Integer) idVolObj;
            ServiceVol sVol = new ServiceVol();
            Vol vol = sVol.findbyId(idVol);
            o.setVol(vol);
        }

        // Récupérer l'hotel si présent
        Object idHotelObj = rs.getObject("id_hotel");
        if (idHotelObj != null) {
            int idHotel = (Integer) idHotelObj;
            ServiceHotel sHotel = new ServiceHotel();
            Hotel hotel = sHotel.findbyId(idHotel);
            o.setHotel(hotel);
        }

        // Récupérer la destination si présente
        Object idDestinationObj = rs.getObject("id_destination");
        if (idDestinationObj != null) {
            int idDestination = (Integer) idDestinationObj;
            ServiceDestination sDestination = new ServiceDestination();
            Destination destination = sDestination.findbyId(idDestination);
            o.setDestination(destination);
        }

        // Récupérer l'activité si présente
        Object idActiviteObj = rs.getObject("id_activite");
        if (idActiviteObj != null) {
            int idActivite = (Integer) idActiviteObj;
            ServiceActivite sActivite = new ServiceActivite();
            Activite activite = sActivite.findbyId(idActivite);
            o.setActivite(activite);
        }

        return o;
    }
}