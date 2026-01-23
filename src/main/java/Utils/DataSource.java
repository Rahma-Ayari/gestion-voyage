package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {

    private static DataSource ds;
    private Connection con;
    private String url = "jdbc:mysql://localhost:3306/gestion_voyage";
    private String user = "root";
    private String password = "";

    private DataSource() {
        try {
            // Essayer de charger le driver explicitement (optionnel depuis Java 6)
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("Driver MySQL chargé avec succès");
            } catch (ClassNotFoundException e) {
                // Le driver sera chargé automatiquement si présent dans le classpath
                System.out.println("Chargement automatique du driver...");
            }

            con = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion à la base de données établie");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données: " + e.getMessage());
            if (e.getMessage().contains("No suitable driver")) {
                System.out.println("\nERREUR: Le driver MySQL n'est pas dans le classpath!");
                System.out.println("Solution: Dans IntelliJ IDEA, cliquez sur 'Run' -> 'Edit Configurations'");
                System.out.println("et assurez-vous que 'Use classpath of module' est sélectionné.");
            }
            e.printStackTrace();
        }
    }

    public Connection getCon() {
        try {
            // Vérifier si la connexion est toujours valide
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la connexion");
            e.printStackTrace();
        }
        return con;
    }

    public static DataSource getInstance() {
        if (ds == null) {
            ds = new DataSource();
        }
        return ds;
    }
}