//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package Entite;

import java.time.LocalDate;

public class Offre {
    private int id_offre;
    private String type;
    private double prix;
    private String description;
    private boolean disponibilite;
    private Vol vol;
    private Hotel hotel;
    private Destination destination;
    private Activite activite;
    private String imagePath;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    public Offre() {
    }

    public Offre(int id_offre, String type, double prix, String description, boolean disponibilite, Vol vol, Hotel hotel, Destination destination, Activite activite, String imagePath, LocalDate dateDebut, LocalDate dateFin) {
        this.id_offre = id_offre;
        this.type = type;
        this.prix = prix;
        this.description = description;
        this.disponibilite = disponibilite;
        this.vol = vol;
        this.hotel = hotel;
        this.destination = destination;
        this.activite = activite;
        this.imagePath = imagePath;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Offre(int id_offre, String type, double prix, String description, boolean disponibilite, Vol vol, Hotel hotel, Destination destination, Activite activite, LocalDate dateDebut, LocalDate dateFin) {
        this(id_offre, type, prix, description, disponibilite, vol, hotel, destination, activite, (String)null, dateDebut, dateFin);
    }

    // Getters et Setters existants
    public int getId_offre() {
        return this.id_offre;
    }

    public void setId_offre(int id_offre) {
        this.id_offre = id_offre;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrix() {
        return this.prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisponibilite() {
        return this.disponibilite;
    }

    public void setDisponibilite(boolean disponibilite) {
        this.disponibilite = disponibilite;
    }



    public Vol getVol() {
        return this.vol;
    }

    public void setVol(Vol vol) {
        this.vol = vol;
    }

    public Hotel getHotel() {
        return this.hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Destination getDestination() {
        return this.destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Activite getActivite() {
        return this.activite;
    }

    public void setActivite(Activite activite) {
        this.activite = activite;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDate getDateDebut() {
        return this.dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return this.dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String toString() {
        int var10000 = this.id_offre;
        return "Offre{id_offre=" + var10000 + ", type='" + this.type + "', prix=" + this.prix + ", description='" + this.description + "', disponibilite=" + this.disponibilite +  ", vol=" + String.valueOf(this.vol) + ", hotel=" + String.valueOf(this.hotel) + ", destination=" + String.valueOf(this.destination) + ", activite=" + String.valueOf(this.activite) + ", imagePath='" + this.imagePath + "', dateDebut=" + String.valueOf(this.dateDebut) + ", dateFin=" + String.valueOf(this.dateFin) + "}";
    }
}