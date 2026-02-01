package Entite;

import java.util.Date;

public class Utilisateur {
    protected int idUtilisateur;
    protected String email;
    protected String motDePasse;
    protected Date dateInscription;

    public Utilisateur() {}

    public Utilisateur(int idUtilisateur, String email, String motDePasse, Date dateInscription) {
        this.idUtilisateur = idUtilisateur;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateInscription = dateInscription;
    }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Date getDateInscription() { return dateInscription; }
    public void setDateInscription(Date dateInscription) { this.dateInscription = dateInscription; }
}