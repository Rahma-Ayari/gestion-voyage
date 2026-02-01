package Entite;

public class Activite {
    private int idActivite;
    private String nom;
    private String description;
    private double prix;
    private int dureeEnHeure;
    private String categorie;
    private String horaire;
    private TypeActivite TypeAct;

    public Activite() {}

    public Activite(int idActivite, String nom, String description,
                    double prix, int dureeEnHeure,
                    String categorie, String horaire) {
        this.idActivite = idActivite;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.dureeEnHeure = dureeEnHeure;
        this.categorie = categorie;
        this.horaire = horaire;
    }

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getDureeEnHeure() {
        return dureeEnHeure;
    }

    public void setDureeEnHeure(int dureeEnHeure) {
        this.dureeEnHeure = dureeEnHeure;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getHoraire() {
        return horaire;
    }

    public void setHoraire(String horaire) {
        this.horaire = horaire;
    }


}

