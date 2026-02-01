package Entite;

public class Administrateur extends Personne {
    private int idRole;

    public Administrateur(int idAdmin, String nom, String prenom, int idRole) {
        super(idAdmin, nom, prenom);
        this.idRole = idRole;
    }

    public Administrateur(String nom, String prenom, int idRole) {
        super(nom, prenom);
        this.idRole = idRole;
    }

    public int getIdAdmin() {
        return this.idPersonne;
    }

    public int getIdRole() { return idRole; }
    public void setIdRole(int idRole) { this.idRole = idRole; }

    @Override
    public String toString() {
        return "Administrateur{" +
                "id_admin=" + idPersonne +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", id_role=" + idRole +
                '}';
    }
}