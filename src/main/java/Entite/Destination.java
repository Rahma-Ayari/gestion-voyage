package Entite;

import java.time.LocalDate;

public class Destination {

    private int       idDestination;
    private String    pays;
    private String    ville;
    private String    description;
    private LocalDate dateDebut;   // date de disponibilité début
    private LocalDate dateFin;     // date de disponibilité fin

    /* ── Constructeurs ── */
    public Destination() {}

    /** Constructeur complet (avec dates) */
    public Destination(int idDestination, String pays, String ville,
                       String description, LocalDate dateDebut, LocalDate dateFin) {
        this.idDestination = idDestination;
        this.pays          = pays;
        this.ville         = ville;
        this.description   = description;
        this.dateDebut     = dateDebut;
        this.dateFin       = dateFin;
    }

    /** Constructeur sans dates (rétro-compatibilité) */
    public Destination(int idDestination, String pays, String ville, String description) {
        this(idDestination, pays, ville, description, null, null);
    }

    /* ── Getters / Setters ── */
    public int       getIdDestination()               { return idDestination; }
    public void      setIdDestination(int id)         { this.idDestination = id; }

    public String    getPays()                         { return pays; }
    public void      setPays(String pays)              { this.pays = pays; }

    public String    getVille()                        { return ville; }
    public void      setVille(String ville)            { this.ville = ville; }

    public String    getDescription()                  { return description; }
    public void      setDescription(String description){ this.description = description; }

    public LocalDate getDateDebut()                    { return dateDebut; }
    public void      setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin()                      { return dateFin; }
    public void      setDateFin(LocalDate dateFin)     { this.dateFin = dateFin; }

    @Override
    public String toString() {
        return pays + " — " + ville;
    }
}