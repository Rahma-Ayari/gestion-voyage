package Entite;


import java.time.LocalDateTime;

public class Vol {
    private int idVol;
    private String numeroVol;
    private String compagnie;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArrivee;
    private double prix;
    private Destination destination;

    public Vol() {}

    public Vol(int idVol, String numeroVol, String compagnie,
               LocalDateTime dateDepart, LocalDateTime dateArrivee,
               double prix, Destination destination) {
        this.idVol = idVol;
        this.numeroVol = numeroVol;
        this.compagnie = compagnie;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.prix = prix;
        this.destination = destination;
    }

    public int getIdVol() {
        return idVol;
    }

    public void setIdVol(int idVol) {
        this.idVol = idVol;
    }

    public String getNumeroVol() {
        return numeroVol;
    }

    public void setNumeroVol(String numeroVol) {
        this.numeroVol = numeroVol;
    }

    public String getCompagnie() {
        return compagnie;
    }

    public void setCompagnie(String compagnie) {
        this.compagnie = compagnie;
    }

    public LocalDateTime getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDateTime dateDepart) {
        this.dateDepart = dateDepart;
    }

    public LocalDateTime getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(LocalDateTime dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "Vol{id=" + idVol + ", numeroVol='" + numeroVol + "', compagnie='" + compagnie + "', prix=" + prix + "}";
    }

}

