package Service;

import Entite.Destination;
import Entite.Vol;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceVol implements IService<Vol> {

    private final Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceVol() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /* ══════════════════════════ AJOUTER ══════════════════════════ */
    @Override
    public boolean ajouter(Vol v) throws SQLException {
        String req = "INSERT INTO vol (numero_vol, compagnie, date_depart, date_arrivee, prix, id_destination, type_vol) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ps.setString(1, v.getNumeroVol());
            ps.setString(2, v.getCompagnie());
            ps.setTimestamp(3, Timestamp.valueOf(v.getDateDepart()));
            ps.setTimestamp(4, Timestamp.valueOf(v.getDateArrivee()));
            ps.setDouble(5, v.getPrix());
            ps.setInt(6, v.getDestination().getIdDestination());
            ps.setString(7, v.getTypeVol());
            return ps.executeUpdate() > 0;
        }
    }

    /* ══════════════════════════ SUPPRIMER ══════════════════════════ */
    @Override
    public boolean supprimer(Vol v) throws SQLException {
        String req = "DELETE FROM vol WHERE id_vol = ?";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ps.setInt(1, v.getIdVol());
            return ps.executeUpdate() > 0;
        }
    }

    /* ══════════════════════════ MODIFIER ══════════════════════════ */
    @Override
    public boolean modifier(Vol v) throws SQLException {
        String req = "UPDATE vol SET numero_vol=?, compagnie=?, date_depart=?, "
                + "date_arrivee=?, prix=?, id_destination=?, type_vol=? "
                + "WHERE id_vol=?";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ps.setString(1, v.getNumeroVol());
            ps.setString(2, v.getCompagnie());
            ps.setTimestamp(3, Timestamp.valueOf(v.getDateDepart()));
            ps.setTimestamp(4, Timestamp.valueOf(v.getDateArrivee()));
            ps.setDouble(5, v.getPrix());
            ps.setInt(6, v.getDestination().getIdDestination());
            ps.setString(7, v.getTypeVol());
            ps.setInt(8, v.getIdVol());
            return ps.executeUpdate() > 0;
        }
    }

    /* ══════════════════════════ FIND BY ID ══════════════════════════ */
    @Override
    public Vol findbyId(int id) throws SQLException {
        String req = "SELECT v.*, d.pays, d.ville, d.description "
                + "FROM vol v "
                + "JOIN destination d ON v.id_destination = d.id_destination "
                + "WHERE v.id_vol = ?";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapVol(rs);
        }
        return null;
    }

    /* ══════════════════════════ READ ALL ══════════════════════════ */
    @Override
    public List<Vol> readAll() throws SQLException {
        List<Vol> list = new ArrayList<>();
        String req = "SELECT v.*, d.pays, d.ville, d.description "
                + "FROM vol v "
                + "JOIN destination d ON v.id_destination = d.id_destination";
        try (PreparedStatement ps = connect.prepareStatement(req)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapVol(rs));
        }
        return list;
    }

    /* ══════════════════════════════════════════════════════════════
       FILTRER PAR TYPE + DESTINATION + DATE
       - ALLER_SIMPLE  → vols dont DATE(date_depart)  = dateAller
       - RETOUR_SIMPLE → vols dont DATE(date_arrivee) = dateRetour
       - ALLER_RETOUR  → vols dont date_depart >= dateAller
                                 ET date_arrivee <= dateRetour
    ══════════════════════════════════════════════════════════════ */
    public List<Vol> findByTypeAndDates(int idDestination,
                                        String typeVol,
                                        LocalDate dateAller,
                                        LocalDate dateRetour)
            throws SQLException {

        List<Vol> list = new ArrayList<>();
        String req;

        if (typeVol.equals("ALLER_SIMPLE")) {
            // Chercher les vols qui PARTENT à la date choisie
            req = "SELECT v.*, d.pays, d.ville, d.description "
                    + "FROM vol v "
                    + "JOIN destination d ON v.id_destination = d.id_destination "
                    + "WHERE v.id_destination = ? "
                    + "AND DATE(v.date_depart) = ?";

            try (PreparedStatement ps = connect.prepareStatement(req)) {
                ps.setInt(1, idDestination);
                ps.setDate(2, Date.valueOf(dateAller));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(mapVol(rs));
            }

        } else if (typeVol.equals("RETOUR_SIMPLE")) {
            // Chercher les vols qui ARRIVENT à la date choisie
            req = "SELECT v.*, d.pays, d.ville, d.description "
                    + "FROM vol v "
                    + "JOIN destination d ON v.id_destination = d.id_destination "
                    + "WHERE v.id_destination = ? "
                    + "AND DATE(v.date_arrivee) = ?";

            try (PreparedStatement ps = connect.prepareStatement(req)) {
                ps.setInt(1, idDestination);
                ps.setDate(2, Date.valueOf(dateRetour));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(mapVol(rs));
            }

        } else {
            // ALLER_RETOUR : vols dont le départ >= dateAller ET arrivée <= dateRetour
            req = "SELECT v.*, d.pays, d.ville, d.description "
                    + "FROM vol v "
                    + "JOIN destination d ON v.id_destination = d.id_destination "
                    + "WHERE v.id_destination = ? "
                    + "AND DATE(v.date_depart) >= ? "
                    + "AND DATE(v.date_arrivee) <= ?";

            try (PreparedStatement ps = connect.prepareStatement(req)) {
                ps.setInt(1, idDestination);
                ps.setDate(2, Date.valueOf(dateAller));
                ps.setDate(3, Date.valueOf(dateRetour));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(mapVol(rs));
            }
        }

        return list;
    }
    /* ══════════════════════════ MAPPER ResultSet → Vol ══════════════════════════ */
    private Vol mapVol(ResultSet rs) throws SQLException {
        Timestamp dateDepartTs  = rs.getTimestamp("date_depart");
        Timestamp dateArriveeTs = rs.getTimestamp("date_arrivee");

        LocalDateTime dateDepart  = dateDepartTs  != null ? dateDepartTs.toLocalDateTime()  : null;
        LocalDateTime dateArrivee = dateArriveeTs != null ? dateArriveeTs.toLocalDateTime() : null;

        Destination d = new Destination();
        d.setIdDestination(rs.getInt("id_destination"));
        d.setPays(rs.getString("pays"));
        d.setVille(rs.getString("ville"));
        d.setDescription(rs.getString("description"));

        Vol vol = new Vol(
                rs.getInt("id_vol"),
                rs.getString("numero_vol"),
                rs.getString("compagnie"),
                dateDepart,
                dateArrivee,
                rs.getDouble("prix"),
                d
        );
        vol.setTypeVol(rs.getString("type_vol")); // ← lire le type depuis la BD
        return vol;
    }
}