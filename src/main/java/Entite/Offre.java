package Entite;

public class Offre {
    private int id_offre;
    private String type;
    private double prix;
    private String description;
    private boolean disponibilite;
    private Voyage idVoyage;
    public Offre() {}

    public Offre(int id_offre, String type, double prix, String description, boolean disponibilite, Voyage idVoyage) {
        this.id_offre = id_offre;
        this.type = type;
        this.prix = prix;
        this.description = description;
        this.disponibilite = disponibilite;
        this.idVoyage = idVoyage;
    }

    public int getId_offre() {
        return id_offre;
    }

    public void setId_offre(int id_offre) {
        this.id_offre = id_offre;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(boolean disponibilite) {
        this.disponibilite = disponibilite;
    }

    public Voyage getvoyage() {
        return idVoyage;
    }

    public void setvoyage(Voyage idVoyage) {
        this.idVoyage = idVoyage;
    }


}
