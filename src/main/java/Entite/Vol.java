package Entite;

import java.time.LocalDateTime;

public class Vol {

    private int           idVol;
    private String        numeroVol;
    private String        compagnie;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArrivee;
    private double        prix;
    private Destination   destination;
    private String        typeVol; // "ALLER_SIMPLE" | "RETOUR_SIMPLE" | "ALLER_RETOUR"

    public Vol() {}

    public Vol(int idVol, String numeroVol, String compagnie,
               LocalDateTime dateDepart, LocalDateTime dateArrivee,
               double prix, Destination destination) {
        this.idVol       = idVol;
        this.numeroVol   = numeroVol;
        this.compagnie   = compagnie;
        this.dateDepart  = dateDepart;
        this.dateArrivee = dateArrivee;
        this.prix        = prix;
        this.destination = destination;
    }

    public int           getIdVol()               { return idVol; }
    public void          setIdVol(int v)           { this.idVol = v; }

    public String        getNumeroVol()            { return numeroVol; }
    public void          setNumeroVol(String v)    { this.numeroVol = v; }

    public String        getCompagnie()            { return compagnie; }
    public void          setCompagnie(String v)    { this.compagnie = v; }

    public LocalDateTime getDateDepart()                { return dateDepart; }
    public void          setDateDepart(LocalDateTime v) { this.dateDepart = v; }

    public LocalDateTime getDateArrivee()                { return dateArrivee; }
    public void          setDateArrivee(LocalDateTime v) { this.dateArrivee = v; }

    public double        getPrix()                 { return prix; }
    public void          setPrix(double v)         { this.prix = v; }

    public Destination   getDestination()                { return destination; }
    public void          setDestination(Destination v)   { this.destination = v; }

    public String        getTypeVol()              { return typeVol; }
    public void          setTypeVol(String v)      { this.typeVol = v; }

    @Override
    public String toString() {
        return "Vol{id=" + idVol + ", numero='" + numeroVol
                + "', compagnie='" + compagnie + "', type='" + typeVol + "'}";
    }
}