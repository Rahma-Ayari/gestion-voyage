package Entite;

import java.time.LocalDate;

public class Voyage {

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
    }


    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int       getIdVoyage()                        { return idVoyage; }
    public void      setIdVoyage(int idVoyage)            { this.idVoyage = idVoyage; }

    public int       getDuree()                           { return duree; }
    public void      setDuree(int duree)                  { this.duree = duree; }

    public LocalDate getDateDebut()                       { return dateDebut; }
    public void      setDateDebut(LocalDate dateDebut)    { this.dateDebut = dateDebut; }

    public LocalDate getDateFin()                         { return dateFin; }
    public void      setDateFin(LocalDate dateFin)        { this.dateFin = dateFin; }

    public String    getRythme()                          { return rythme; }
    public void      setRythme(String rythme)             { this.rythme = rythme; }

    public int       getIdDestination()                   { return idDestination; }
    public void      setIdDestination(int idDestination)  { this.idDestination = idDestination; }

    public int       getIdVol()                           { return idVol; }
    public void      setIdVol(int idVol)                  { this.idVol = idVol; }

    public int       getIdHotel()                         { return idHotel; }
    public void      setIdHotel(int idHotel)              { this.idHotel = idHotel; }

    public LocalDate getDateCheckin()                     { return dateCheckin; }
    public void      setDateCheckin(LocalDate dateCheckin){ this.dateCheckin = dateCheckin; }

    public LocalDate getDateCheckout()                     { return dateCheckout; }
    public void      setDateCheckout(LocalDate d)          { this.dateCheckout = d; }

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