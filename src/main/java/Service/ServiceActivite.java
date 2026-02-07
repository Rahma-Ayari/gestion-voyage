package Service;

import Entite.Activite;
import Entite.TypeActivite;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceActivite implements IService<Activite> {
    private Connection connect = DataSource.getInstance().getCon();
    private Statement st;

    public ServiceActivite() {
        try {
            st = connect.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean ajouter(Activite a) throws SQLException {
        boolean test = false;
        int res = -1;

        // ✅ Requête corrigée avec id_type_activite
        String req = "INSERT INTO `activite` (`nom`, `description`, `prix`, `dureeEnHeure`, `categorie`, `horaire`, `id_type_activite`) VALUES ('"
                + a.getNom() + "', '"
                + a.getDescription() + "', "
                + a.getPrix() + ", "
                + a.getDureeEnHeure() + ", '"
                + a.getCategorie() + "', '"
                + a.getHoraire() + "', "
                + (a.getTypeAct() != null ? a.getTypeAct().getIdType() : "NULL") + ")";

        res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }

    @Override
    public boolean supprimer(Activite a) throws SQLException {
        boolean test = false;
        String req = "DELETE FROM activite WHERE id_activite = " + a.getIdActivite();
        int res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }


    @Override
    public boolean modifier(Activite a) throws SQLException {
        boolean test = false;

        // ❌ ERREUR : virgule avant WHERE !
        // ✅ Requête corrigée
        String req = "UPDATE activite SET "
                + "nom = '" + a.getNom() + "', "
                + "description = '" + a.getDescription() + "', "
                + "prix = " + a.getPrix() + ", "
                + "dureeEnHeure = " + a.getDureeEnHeure() + ", "
                + "categorie = '" + a.getCategorie() + "', "
                + "horaire = '" + a.getHoraire() + "', "
                + "id_type_activite = " + (a.getTypeAct() != null ? a.getTypeAct().getIdType() : "NULL")
                + " WHERE id_activite = " + a.getIdActivite();

        int res = st.executeUpdate(req);
        if (res > 0)
            test = true;
        return test;
    }


    @Override
    public Activite findbyId(int id) throws SQLException {
        Activite activite = null;
        String req = "SELECT * FROM activite WHERE id_activite = " + id;
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            activite = new Activite();
            activite.setIdActivite(rs.getInt("id_activite"));
            activite.setNom(rs.getString("nom"));
            activite.setDescription(rs.getString("description"));
            activite.setPrix(rs.getDouble("prix"));
            activite.setDureeEnHeure(rs.getInt("dureeEnHeure"));
            activite.setCategorie(rs.getString("categorie"));
            activite.setHoraire(rs.getString("horaire"));
        }
        return activite;
    }


    @Override
    public List<Activite> readAll() throws SQLException {
        List<Activite> list = new ArrayList<>();

        // ✅ Jointure avec type_activite
        String query = "SELECT a.*, t.id_type, t.libelle " +
                "FROM `activite` a " +
                "LEFT JOIN `type_activite` t ON a.id_type_activite = t.id_type";

        ResultSet rest = st.executeQuery(query);

        while (rest.next()) {
            Activite activite = new Activite();
            activite.setIdActivite(rest.getInt("id_activite"));
            activite.setNom(rest.getString("nom"));
            activite.setDescription(rest.getString("description"));
            activite.setPrix(rest.getDouble("prix"));
            activite.setDureeEnHeure(rest.getInt("dureeEnHeure"));
            activite.setCategorie(rest.getString("categorie"));
            activite.setHoraire(rest.getString("horaire"));

            // ✅ Charger le type d'activité
            if (rest.getObject("id_type") != null) {
                TypeActivite type = new TypeActivite();
                type.setIdType(rest.getInt("id_type"));
                type.setLibelle(rest.getString("libelle"));
                activite.setTypeAct(type);
            }

            list.add(activite);
        }
        return list;
    }
}
