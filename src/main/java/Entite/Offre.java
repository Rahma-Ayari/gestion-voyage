package Entite;

public class Offre {
    private int id_offre;
    private String type;
    private double prix;
    private String description;
    private boolean disponibilite;
    private Voyage idVoyage;
    private Vol vol;
    private Hotel hotel;
    private Destination destination;  // Ajouté
    private Activite activite;        // Ajouté

    public Offre() {}

    // Constructeur complet avec tous les attributs
    public Offre(int id_offre, String type, double prix, String description,
                 boolean disponibilite, Voyage idVoyage, Vol vol, Hotel hotel,
                 Destination destination, Activite activite) {
        this.id_offre = id_offre;
        this.type = type;
        this.prix = prix;
        this.description = description;
        this.disponibilite = disponibilite;
        this.idVoyage = idVoyage;
        this.vol = vol;
        this.hotel = hotel;
        this.destination = destination;
        this.activite = activite;
    }

    // Constructeur avec vol et hotel (pour compatibilité)
    public Offre(int id_offre, String type, double prix, String description,
                 boolean disponibilite, Voyage idVoyage, Vol vol, Hotel hotel) {
        this.id_offre = id_offre;
        this.type = type;
        this.prix = prix;
        this.description = description;
        this.disponibilite = disponibilite;
        this.idVoyage = idVoyage;
        this.vol = vol;
        this.hotel = hotel;
        this.destination = null;
        this.activite = null;
    }

    // Constructeur sans vol et hotel (pour compatibilité)
    public Offre(int id_offre, String type, double prix, String description,
                 boolean disponibilite, Voyage idVoyage) {
        this.id_offre = id_offre;
        this.type = type;
        this.prix = prix;
        this.description = description;
        this.disponibilite = disponibilite;
        this.idVoyage = idVoyage;
        this.vol = null;
        this.hotel = null;
        this.destination = null;
        this.activite = null;
    }

    // Getters et Setters existants
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

    public Vol getVol() {
        return vol;
    }

    public void setVol(Vol vol) {
        this.vol = vol;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    // Getters et Setters pour Destination
    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    // Getters et Setters pour Activite
    public Activite getActivite() {
        return activite;
    }

    public void setActivite(Activite activite) {
        this.activite = activite;
    }

    @Override
    public String toString() {
        return "Offre{" +
                "id_offre=" + id_offre +
                ", type='" + type + '\'' +
                ", prix=" + prix +
                ", description='" + description + '\'' +
                ", disponibilite=" + disponibilite +
                ", idVoyage=" + idVoyage +
                ", vol=" + vol +
                ", hotel=" + hotel +
                ", destination=" + destination +
                ", activite=" + activite +
                '}';
    }
}