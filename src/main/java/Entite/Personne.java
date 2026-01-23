package Entite;

public class Personne {
    protected int idPersonne;
    protected String nom;
    protected String prenom;

    public Personne(int idPersonne, String nom, String prenom) {
        this.idPersonne = idPersonne;
        this.nom = nom;
        this.prenom = prenom;
    }

    public Personne(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getIdPersonne() { return idPersonne; }
    public void setIdPersonne(int idPersonne) { this.idPersonne = idPersonne; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    @Override
    public String toString() {
        return "ID: " + idPersonne + " | Nom: " + nom + " | Prenom: " + prenom;
    }
}