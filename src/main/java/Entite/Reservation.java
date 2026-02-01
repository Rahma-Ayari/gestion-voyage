package Entite;

import java.sql.Date;

public class Reservation {
    private int id_reservation;
    private Date date_reservation;
    private double prix_reservation;
    private String etat;
    private Personne id_personne;
    private Voyage id_voyage;
    private StatutReservation id_statut;
    public Reservation() {}

    public Reservation(int id_reservation, Date date_reservation, double prix_reservation, String etat, Personne id_personne, Voyage id_voyage, StatutReservation id_statut) {
        this.id_reservation = id_reservation;
        this.date_reservation = date_reservation;
        this.prix_reservation = prix_reservation;
        this.etat = etat;
        this.id_personne = id_personne;
        this.id_voyage = id_voyage;
        this.id_statut = id_statut;
    }

    public int getId_reservation() {
        return id_reservation;
    }

    public void setId_reservation(int id_reservation) {
        this.id_reservation = id_reservation;
    }

    public Date getDate_reservation() {
        return date_reservation;
    }

    public void setDate_reservation(Date date_reservation) {
        this.date_reservation = date_reservation;
    }

    public double getPrix_reservation() {
        return prix_reservation;
    }

    public void setPrix_reservation(double prix_reservation) {
        this.prix_reservation = prix_reservation;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public Personne getId_personne() {
        return id_personne;
    }

    public void setId_personne(Personne id_personne) {
        this.id_personne = id_personne;
    }

    public Voyage getId_voyage() {
        return id_voyage;
    }

    public void setId_voyage(Voyage id_voyage) {
        this.id_voyage = id_voyage;
    }

    public StatutReservation getId_statut() {
        return id_statut;
    }

    public void setId_statut(StatutReservation id_statut) {
        this.id_statut = id_statut;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id_reservation=" + id_reservation +
                ", date_reservation=" + date_reservation +
                ", prix_reservation=" + prix_reservation +
                ", etat='" + etat + '\'' +
                ", id_personne=" + id_personne +
                ", id_voyage=" + id_voyage +
                ", id_statut=" + id_statut +
                '}';
    }
}

