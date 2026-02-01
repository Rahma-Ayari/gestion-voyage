package Service;

import Entite.Activite;
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
        String req = "INSERT INTO `activite` (`nom`, `description`, `prix`, `dureeEnHeure`, `categorie`, `horaire`, `id_voyage`) VALUES ('"
                + a.getNom() + "', '"
                + a.getDescription() + "', "
                + a.getPrix() + ", "
                + a.getDureeEnHeure() + ", '"
                + a.getCategorie() + "', '"
                + a.getHoraire() + "', "
                ;
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
        String req = "UPDATE activite SET "
                + "nom = '" + a.getNom() + "', "
                + "description = '" + a.getDescription() + "', "
                + "prix = " + a.getPrix() + ", "
                + "dureeEnHeure = " + a.getDureeEnHeure() + ", "
                + "categorie = '" + a.getCategorie() + "', "
                + "horaire = '" + a.getHoraire() + "', "
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
        String query = "SELECT * FROM `activite`";
        ResultSet rest = st.executeQuery(query);
        while (rest.next()) {
            int id = rest.getInt(1);
            String nom = rest.getString("nom");
            String description = rest.getString(3);
            double prix = rest.getDouble("prix");
            int dureeEnHeure = rest.getInt("dureeEnHeure");
            String categorie = rest.getString("categorie");
            String horaire = rest.getString("horaire");
            Activite activite = new Activite();
            activite.setIdActivite(id);
            activite.setNom(nom);
            activite.setDescription(description);
            activite.setPrix(prix);
            activite.setDureeEnHeure(dureeEnHeure);
            activite.setCategorie(categorie);
            activite.setHoraire(horaire);
            list.add(activite);
        }
        return list;
    }
}
