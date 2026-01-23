package Entite;

import java.time.LocalDate;
import java.util.List;

public class Voyage {
    private int idVoyage;
    private int duree;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String rythme;
    private Destination destination;
    private Vol vol;
    private List<Activite> activites;
    private List<Hotel> hotels;

    public Voyage() {}

    public Voyage(int idVoyage, int duree, LocalDate dateDebut,
                  LocalDate dateFin, String rythme,
                  Destination destination, Vol vol) {
        this.idVoyage = idVoyage;
        this.duree = duree;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.rythme = rythme;
        this.destination = destination;
        this.vol = vol;
    }

    public int getIdVoyage() {
        return idVoyage;
    }

    public void setIdVoyage(int idVoyage) {
        this.idVoyage = idVoyage;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getRythme() {
        return rythme;
    }

    public void setRythme(String rythme) {
        this.rythme = rythme;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Vol getVol() {
        return vol;
    }

    public void setVol(Vol vol) {
        this.vol = vol;
    }

    public List<Activite> getActivites() {
        return activites;
    }

    public void setActivites(List<Activite> activites) {
        this.activites = activites;
    }

    public List<Hotel> getHotels() {
        return hotels;
    }

    public void setHotels(List<Hotel> hotels) {
        this.hotels = hotels;
    }

}
