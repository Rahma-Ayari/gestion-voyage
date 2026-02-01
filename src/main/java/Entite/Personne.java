package Entite;

import java.util.Date;

public class Personne extends Utilisateur {
    private String nom;
    private String prenom;

    public Personne() {
        super();
    }

    public Personne(int id, String email, String motDePasse, Date dateInscription, String nom, String prenom) {
        super(id, email, motDePasse, dateInscription);
        this.nom = nom;
        this.prenom = prenom;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    @Override
    public String toString() {
        return "Personne [ID=" + idUtilisateur + ", Nom=" + nom + ", Prenom=" + prenom + ", Email=" + email + "]";
    }
}