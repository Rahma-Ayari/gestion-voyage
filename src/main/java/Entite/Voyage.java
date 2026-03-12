package Entite;

import java.time.LocalDate;

public class Voyage {

    // ══════════════════════════════════════════════════════════════════
    // ATTRIBUTS
    // ══════════════════════════════════════════════════════════════════
    private int       idVoyage;
    private int       duree;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String    rythme;
    private int       idDestination;
    private int       idVol;
    private int       idHotel;
    private LocalDate dateCheckin;
    private LocalDate dateCheckout;
    private int       idUser;
    private int       idBudget;
    private String    statut;       // "ACTIF" | "ARCHIVE"

    // ══════════════════════════════════════════════════════════════════
    // CONSTRUCTEURS
    // ══════════════════════════════════════════════════════════════════

    /** Constructeur complet (utilisé par mapVoyage dans ServiceVoyage) */
    public Voyage(int idVoyage, int duree,
                  LocalDate dateDebut, LocalDate dateFin,
                  String rythme, int idDestination,
                  int idVol, int idHotel,
                  LocalDate dateCheckin, LocalDate dateCheckout) {
        this.idVoyage      = idVoyage;
        this.duree         = duree;
        this.dateDebut     = dateDebut;
        this.dateFin       = dateFin;
        this.rythme        = rythme;
        this.idDestination = idDestination;
        this.idVol         = idVol;
        this.idHotel       = idHotel;
        this.dateCheckin   = dateCheckin;
        this.dateCheckout  = dateCheckout;
        this.statut        = "ACTIF";
    }

    /** Constructeur pour créer un nouveau voyage (sans ID ni vol/hôtel encore) */
    public Voyage(int duree, LocalDate dateDebut, LocalDate dateFin,
                  String rythme, int idDestination, int idUser) {
        this.duree         = duree;
        this.dateDebut     = dateDebut;
        this.dateFin       = dateFin;
        this.rythme        = rythme;
        this.idDestination = idDestination;
        this.idUser        = idUser;
        this.statut        = "ACTIF";
    }

    /** Constructeur vide */
    public Voyage() {
        this.statut = "ACTIF";
    }

    // ══════════════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ══════════════════════════════════════════════════════════════════

    public int getIdVoyage()                  { return idVoyage; }
    public void setIdVoyage(int idVoyage)     { this.idVoyage = idVoyage; }

    public int getDuree()                     { return duree; }
    public void setDuree(int duree)           { this.duree = duree; }

    public LocalDate getDateDebut()           { return dateDebut; }
    public void setDateDebut(LocalDate d)     { this.dateDebut = d; }

    public LocalDate getDateFin()             { return dateFin; }
    public void setDateFin(LocalDate d)       { this.dateFin = d; }

    public String getRythme()                 { return rythme; }
    public void setRythme(String rythme)      { this.rythme = rythme; }

    public int getIdDestination()             { return idDestination; }
    public void setIdDestination(int id)      { this.idDestination = id; }

    public int getIdVol()                     { return idVol; }
    public void setIdVol(int idVol)           { this.idVol = idVol; }

    public int getIdHotel()                   { return idHotel; }
    public void setIdHotel(int idHotel)       { this.idHotel = idHotel; }

    public LocalDate getDateCheckin()         { return dateCheckin; }
    public void setDateCheckin(LocalDate d)   { this.dateCheckin = d; }

    public LocalDate getDateCheckout()        { return dateCheckout; }
    public void setDateCheckout(LocalDate d)  { this.dateCheckout = d; }

    public int getIdUser()                    { return idUser; }
    public void setIdUser(int idUser)         { this.idUser = idUser; }

    public int getIdBudget()                  { return idBudget; }
    public void setIdBudget(int idBudget)     { this.idBudget = idBudget; }

    public String getStatut()                 { return statut; }
    public void setStatut(String statut)      { this.statut = statut; }

    // ══════════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ══════════════════════════════════════════════════════════════════

    /** Retourne true si vol ET hôtel sont configurés */
    public boolean isComplete() {
        return idVol > 0 && idHotel > 0;
    }

    /** Retourne true si seulement l'un des deux est configuré */
    public boolean isPartial() {
        return (idVol > 0 || idHotel > 0) && !isComplete();
    }

    /** Retourne true si le voyage est archivé */
    public boolean isArchive() {
        return "ARCHIVE".equals(statut);
    }

    @Override
    public String toString() {
        return "Voyage{" +
                "id="          + idVoyage      +
                ", rythme='"   + rythme        + '\'' +
                ", debut="     + dateDebut     +
                ", fin="       + dateFin       +
                ", duree="     + duree         +
                ", dest="      + idDestination +
                ", vol="       + idVol         +
                ", hotel="     + idHotel       +
                ", checkin="   + dateCheckin   +
                ", checkout="  + dateCheckout  +
                ", budget="    + idBudget      +
                ", user="      + idUser        +
                ", statut='"   + statut        + '\'' +
                '}';
    }
}