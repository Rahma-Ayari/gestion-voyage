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
    private Destination villeDepart;
    private int id_destination;
    private int	ville_depart_id;
    private String typeVol;



    public Vol() {}

    public Vol(int idVol, String numeroVol, String compagnie, LocalDateTime dateDepart, LocalDateTime dateArrivee, double prix, Destination destination, Destination villeDepart, int id_destination, int ville_depart_id, String typeVol) {
        this.idVol = idVol;
        this.numeroVol = numeroVol;
        this.compagnie = compagnie;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.prix = prix;
        this.destination = destination;
        this.villeDepart = villeDepart;
        this.id_destination = id_destination;
        this.ville_depart_id = ville_depart_id;
        this.typeVol = typeVol;
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

    public Destination getVilleDepart() {
        return villeDepart;
    }

    public void setVilleDepart(Destination villeDepart) {
        this.villeDepart = villeDepart;
    }

    public int getId_destination() {
        return id_destination;
    }

    public void setId_destination(int id_destination) {
        this.id_destination = id_destination;
    }

    public int getVille_depart_id() {
        return ville_depart_id;
    }

    public void setVille_depart_id(int ville_depart_id) {
        this.ville_depart_id = ville_depart_id;
    }

    public String getTypeVol() {
        return typeVol;
    }

    public void setTypeVol(String typeVol) {
        this.typeVol = typeVol;
    }

    @Override
    public String toString() {
        return "Vol{" +
                "idVol=" + idVol +
                ", numeroVol='" + numeroVol + '\'' +
                ", compagnie='" + compagnie + '\'' +
                ", dateDepart=" + dateDepart +
                ", dateArrivee=" + dateArrivee +
                ", prix=" + prix +
                ", destination=" + destination +
                ", villeDepart=" + villeDepart +
                ", id_destination=" + id_destination +
                ", ville_depart_id=" + ville_depart_id +
                ", typeVol='" + typeVol + '\'' +
                '}';
    }
}