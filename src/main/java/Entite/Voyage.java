package Entite;

import java.time.LocalDate;

public class Voyage {

    private int       idVoyage;
    private int       idUtilisateur;   // ← NOUVEAU : qui a créé ce voyage
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

    // ─── Constructeur vide ────────────────────────────────────────
    public Voyage() {}

    // ─── Constructeur existant (gardé intact) ────────────────────
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

    // ─── Getters & Setters ────────────────────────────────────────

    public int       getIdVoyage()                         { return idVoyage; }
    public void      setIdVoyage(int idVoyage)             { this.idVoyage = idVoyage; }

    // ← NOUVEAU
    public int       getIdUtilisateur()                    { return idUtilisateur; }
    public void      setIdUtilisateur(int idUtilisateur)   { this.idUtilisateur = idUtilisateur; }

    public int       getDuree()                            { return duree; }
    public void      setDuree(int duree)                   { this.duree = duree; }

    public LocalDate getDateDebut()                        { return dateDebut; }
    public void      setDateDebut(LocalDate dateDebut)     { this.dateDebut = dateDebut; }

    public LocalDate getDateFin()                          { return dateFin; }
    public void      setDateFin(LocalDate dateFin)         { this.dateFin = dateFin; }

    public String    getRythme()                           { return rythme; }
    public void      setRythme(String rythme)              { this.rythme = rythme; }

    public int       getIdDestination()                    { return idDestination; }
    public void      setIdDestination(int idDestination)   { this.idDestination = idDestination; }

    public int       getIdVol()                            { return idVol; }
    public void      setIdVol(int idVol)                   { this.idVol = idVol; }

    public int       getIdHotel()                          { return idHotel; }
    public void      setIdHotel(int idHotel)               { this.idHotel = idHotel; }

    public LocalDate getDateCheckin()                      { return dateCheckin; }
    public void      setDateCheckin(LocalDate dateCheckin) { this.dateCheckin = dateCheckin; }

    public LocalDate getDateCheckout()                     { return dateCheckout; }
    public void      setDateCheckout(LocalDate d)          { this.dateCheckout = d; }

    public Destination getDestination()                    { return destination; }
    public void        setDestination(Destination d)       { this.destination = d; }

    @Override
    public String toString() {
        // Ce texte apparaît dans le ComboBox de l'écran d'avis
        return "Voyage #" + idVoyage
                + "  |  " + dateDebut + " → " + dateFin
                + "  |  " + rythme
                + "  (" + duree + " jours)";
    }
}