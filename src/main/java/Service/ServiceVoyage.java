package Service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Entite.Voyage;
import Utils.DataSource;

public class ServiceVoyage {

    private final Connection connect = DataSource.getInstance().getCon();

    // ══════════════════════════════════════════════════════════════
    //  AJOUTER un nouveau voyage
    //  On enregistre aussi id_utilisateur dans la base de données
    // ══════════════════════════════════════════════════════════════
    public int ajouter(Voyage v) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                // ← On ajoute id_utilisateur dans le INSERT
                "INSERT INTO voyage (id_utilisateur, duree, dateDebut, dateFin, rythme, id_destination) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setInt   (1, v.getIdUtilisateur());  // ← l'utilisateur connecté
        ps.setInt   (2, v.getDuree());
        ps.setDate  (3, Date.valueOf(v.getDateDebut()));
        ps.setDate  (4, Date.valueOf(v.getDateFin()));
        ps.setString(5, v.getRythme());
        ps.setInt   (6, v.getIdDestination());
        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) return keys.getInt(1);
        return -1;
    }

    // ══════════════════════════════════════════════════════════════
    //  NOUVEAU : récupérer uniquement les voyages d'un utilisateur
    //  Utilisé dans AvisUserController pour remplir le ComboBox
    // ══════════════════════════════════════════════════════════════
    public List<Voyage> findByUtilisateur(int idUtilisateur) throws SQLException {
        List<Voyage> list = new ArrayList<>();
        PreparedStatement ps = connect.prepareStatement(
                "SELECT * FROM voyage WHERE id_utilisateur = ? ORDER BY id_voyage DESC");
        ps.setInt(1, idUtilisateur);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapVoyage(rs));
        return list;
    }

    // ══════════════════════════════════════════════════════════════
    //  METTRE À JOUR le vol d'un voyage
    // ══════════════════════════════════════════════════════════════
    public boolean mettreAJourVol(int idVoyage, int idVol) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "UPDATE voyage SET id_vol = ? WHERE id_voyage = ?");
        ps.setInt(1, idVol);
        ps.setInt(2, idVoyage);
        return ps.executeUpdate() > 0;
    }

    // ══════════════════════════════════════════════════════════════
    //  METTRE À JOUR l'hôtel d'un voyage
    // ══════════════════════════════════════════════════════════════
    public boolean mettreAJourHotel(int idVoyage, int idHotel,
                                    LocalDate checkin, LocalDate checkout) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "UPDATE voyage SET id_hotel = ?, date_checkin = ?, date_checkout = ? " +
                        "WHERE id_voyage = ?");
        ps.setInt   (1, idHotel);
        ps.setDate  (2, Date.valueOf(checkin));
        ps.setDate  (3, Date.valueOf(checkout));
        ps.setInt   (4, idVoyage);
        return ps.executeUpdate() > 0;
    }

    // ══════════════════════════════════════════════════════════════
    //  METTRE À JOUR le budget d'un voyage
    // ══════════════════════════════════════════════════════════════
    public boolean mettreAJourBudget(int idVoyage, int idBudget) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "UPDATE voyage SET id_budget = ? WHERE id_voyage = ?");
        ps.setInt(1, idBudget);
        ps.setInt(2, idVoyage);
        return ps.executeUpdate() > 0;
    }

    // ══════════════════════════════════════════════════════════════
    //  TROUVER un voyage par son id
    // ══════════════════════════════════════════════════════════════
    public Voyage findbyId(int id) throws SQLException {
        PreparedStatement ps = connect.prepareStatement(
                "SELECT * FROM voyage WHERE id_voyage = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapVoyage(rs);
        return null;
    }

    // ══════════════════════════════════════════════════════════════
    //  LIRE tous les voyages (pour l'admin)
    // ══════════════════════════════════════════════════════════════
    public List<Voyage> readAll() throws SQLException {
        List<Voyage> list = new ArrayList<>();
        Statement st = connect.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM voyage ORDER BY id_voyage DESC");
        while (rs.next()) list.add(mapVoyage(rs));
        return list;
    }

    // ══════════════════════════════════════════════════════════════
    //  METTRE À JOUR les activités d'un voyage
    // ══════════════════════════════════════════════════════════════
    public boolean mettreAJourActivites(int idVoyage, List<Integer> idActivites) throws SQLException {
        new ServiceActivite().enregistrerActivitesVoyage(idVoyage, idActivites);
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  Méthode privée : convertit une ligne SQL → objet Voyage
    // ══════════════════════════════════════════════════════════════
    private Voyage mapVoyage(ResultSet rs) throws SQLException {
        LocalDate checkin  = rs.getDate("date_checkin")  != null
                ? rs.getDate("date_checkin").toLocalDate()  : null;
        LocalDate checkout = rs.getDate("date_checkout") != null
                ? rs.getDate("date_checkout").toLocalDate() : null;

        Voyage v = new Voyage(
                rs.getInt   ("id_voyage"),
                rs.getInt   ("duree"),
                rs.getDate  ("dateDebut") != null ? rs.getDate("dateDebut").toLocalDate() : null,
                rs.getDate  ("dateFin")   != null ? rs.getDate("dateFin").toLocalDate()   : null,
                rs.getString("rythme"),
                rs.getInt   ("id_destination"),
                rs.getInt   ("id_vol"),
                rs.getInt   ("id_hotel"),
                checkin,
                checkout
        );
        // ← NOUVEAU : on récupère aussi id_utilisateur depuis la BDD
        v.setIdUtilisateur(rs.getInt("id_utilisateur"));
        return v;
    }
}