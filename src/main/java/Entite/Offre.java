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
    private String imagePath;         // Nouveau: chemin de l'image

    public Offre() {}

    // Constructeur complet avec tous les attributs
    public Offre(int id_offre, String type, double prix, String description,
                 boolean disponibilite, Voyage idVoyage, Vol vol, Hotel hotel,
                 Destination destination, Activite activite, String imagePath) {
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
        this.imagePath = imagePath;
    }

    // Constructeur sans image
    public Offre(int id_offre, String type, double prix, String description,
                 boolean disponibilite, Voyage idVoyage, Vol vol, Hotel hotel,
                 Destination destination, Activite activite) {
        this(id_offre, type, prix, description, disponibilite, idVoyage, vol, hotel, destination, activite, null);
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

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Activite getActivite() {
        return activite;
    }

    public void setActivite(Activite activite) {
        this.activite = activite;
    }

    // Getter/Setter pour l'image
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}