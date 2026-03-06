package Entite;

public class Hotel {
    private int idHotel;
    private String nom;
    private String ville;
    private String adresse;
    private int stars;              // Nombre d'étoiles
    private int capacite;           // Nombre de chambres
    private String typeChambre;     // Simple, Double, Suite...
    private double prixParNuit;
    private boolean disponibilite;
    private double latitude;        // Latitude de l'hôtel
    private double longitude;       // Longitude de l'hôtel

    // Constructeur vide
    public Hotel() {}

    // Constructeur complet
    public Hotel(int idHotel, String nom, String ville, String adresse, int stars,
                 int capacite, String typeChambre, double prixParNuit,
                 boolean disponibilite, double latitude, double longitude) {
        this.idHotel = idHotel;
        this.nom = nom;
        this.ville = ville;
        this.adresse = adresse;
        this.stars = stars;
        this.capacite = capacite;
        this.typeChambre = typeChambre;
        this.prixParNuit = prixParNuit;
        this.disponibilite = disponibilite;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters et Setters
    public int getIdHotel() { return idHotel; }
    public void setIdHotel(int idHotel) { this.idHotel = idHotel; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getTypeChambre() { return typeChambre; }
    public void setTypeChambre(String typeChambre) { this.typeChambre = typeChambre; }

    public double getPrixParNuit() { return prixParNuit; }
    public void setPrixParNuit(double prixParNuit) { this.prixParNuit = prixParNuit; }

    public boolean isDisponibilite() { return disponibilite; }
    public void setDisponibilite(boolean disponibilite) { this.disponibilite = disponibilite; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "Hotel{" +
                "idHotel=" + idHotel +
                ", nom='" + nom + '\'' +
                ", ville='" + ville + '\'' +
                ", adresse='" + adresse + '\'' +
                ", stars=" + stars +
                ", capacite=" + capacite +
                ", typeChambre='" + typeChambre + '\'' +
                ", prixParNuit=" + prixParNuit +
                ", disponibilite=" + disponibilite +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}