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
    private Destination destination;
    private int idUser;
    private String statut;   // "ACTIF" ou "ARCHIVE"
    private int    idBudget;

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Voyage() {}

    public Voyage(int idVoyage, int duree, LocalDate dateDebut, LocalDate dateFin, String rythme, int idDestination, int idVol, int idHotel, LocalDate dateCheckin, LocalDate dateCheckout, Destination destination, int idUser) {
        this.idVoyage = idVoyage;
        this.duree = duree;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.rythme = rythme;
        this.idDestination = idDestination;
        this.idVol = idVol;
        this.idHotel = idHotel;
        this.dateCheckin = dateCheckin;
        this.dateCheckout = dateCheckout;
        this.destination = destination;
        this.idUser = idUser;
    }

    public Voyage(int idVoyage, int duree, LocalDate dateDebut, LocalDate dateFin,
                  String rythme, int idDestination, int idVol,
                  int idHotel, LocalDate dateCheckin, LocalDate dateCheckout) {
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

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int       getIdVoyage()                        { return idVoyage; }
    public void      setIdVoyage(int idVoyage)            { this.idVoyage = idVoyage; }

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

    public String getStatut()          { return statut; }
    public void   setStatut(String s)  { this.statut = s; }

    public int  getIdBudget()          { return idBudget; }
    public void setIdBudget(int id)    { this.idBudget = id; }

    @Override
    public String toString() {
        return "Voyage{" +
                "idVoyage=" + idVoyage +
                ", duree=" + duree +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", rythme='" + rythme + '\'' +
                ", idDestination=" + idDestination +
                ", idVol=" + idVol +
                ", idHotel=" + idHotel +
                ", dateCheckin=" + dateCheckin +
                ", dateCheckout=" + dateCheckout +
                ", destination=" + destination +
                ", idUser=" + idUser +
                '}';
    }
}