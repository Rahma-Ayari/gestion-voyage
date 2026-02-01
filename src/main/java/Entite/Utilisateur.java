package Entite;

import java.util.Date;

public class Utilisateur extends Personne {
    private String email;
    private String motDePasse;
    private Date dateInscription;

    public Utilisateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse, Date dateInscription) {
        super(idUtilisateur, nom, prenom);
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateInscription = dateInscription;
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, Date dateInscription) {
        super(nom, prenom);
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateInscription = dateInscription;
    }

    public int getIdUtilisateur() {
        return this.idPersonne;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Date getDateInscription() { return dateInscription; }
    public void setDateInscription(Date dateInscription) { this.dateInscription = dateInscription; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id_utilisateur=" + idPersonne +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", dateInscription=" + dateInscription +
                '}';
    }
}