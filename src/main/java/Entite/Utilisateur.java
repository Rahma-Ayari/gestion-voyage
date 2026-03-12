package Entite;

import java.util.Date;

public class Utilisateur {
    protected int    idUtilisateur;
    protected String email;
    protected String motDePasse;
    protected Date   dateInscription;
    protected int    idVoyage;   // ← lien vers voyage (remplace id_voyage_configure)
    protected String etat;       // ← "done" ou "notDone"

    public Utilisateur() {}

    public Utilisateur(int idUtilisateur, String email,
                       String motDePasse, Date dateInscription) {
        this.idUtilisateur   = idUtilisateur;
        this.email           = email;
        this.motDePasse      = motDePasse;
        this.dateInscription = dateInscription;
    }

    public Utilisateur(int idUtilisateur, String email, String motDePasse,
                       Date dateInscription, int idVoyage, String etat) {
        this.idUtilisateur   = idUtilisateur;
        this.email           = email;
        this.motDePasse      = motDePasse;
        this.dateInscription = dateInscription;
        this.idVoyage        = idVoyage;
        this.etat            = etat;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public int    getIdUtilisateur()           { return idUtilisateur; }
    public void   setIdUtilisateur(int id)     { this.idUtilisateur = id; }

    public String getEmail()                   { return email; }
    public void   setEmail(String email)       { this.email = email; }

    public String getMotDePasse()              { return motDePasse; }
    public void   setMotDePasse(String mp)     { this.motDePasse = mp; }

    public Date   getDateInscription()         { return dateInscription; }
    public void   setDateInscription(Date d)   { this.dateInscription = d; }

    public int    getIdVoyage()                { return idVoyage; }
    public void   setIdVoyage(int idVoyage)    { this.idVoyage = idVoyage; }

    public String getEtat()                    { return etat; }
    public void   setEtat(String etat)         { this.etat = etat; }

    @Override
    public String toString() { return email; }
}