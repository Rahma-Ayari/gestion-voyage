package Entite;

import java.sql.Date;

public class Reservation {

    private int id_reservation;
    private Date date_reservation;
    private double prix_reservation;
    private String etat;

    private String email;
    private String num_tel;
    private String commentaire;
    private int nombre_personnes;
    private String num_passeport;   // ← NOUVEAU : ex "AB1234567 | CD9876543"

    private Personne id_personne;
    private Voyage id_voyage;
    private StatutReservation id_statut;
    private Offre id_offre;

    public Reservation() {}

    public Reservation(int id_reservation, Date date_reservation, double prix_reservation, String etat,
                       String email, String num_tel, String commentaire, int nombre_personnes,
                       String num_passeport,
                       Personne id_personne, Voyage id_voyage, StatutReservation id_statut, Offre id_offre) {

        this.id_reservation    = id_reservation;
        this.date_reservation  = date_reservation;
        this.prix_reservation  = prix_reservation;
        this.etat              = etat;
        this.email             = email;
        this.num_tel           = num_tel;
        this.commentaire       = commentaire;
        this.nombre_personnes  = nombre_personnes;
        this.num_passeport     = num_passeport;
        this.id_personne       = id_personne;
        this.id_voyage         = id_voyage;
        this.id_statut         = id_statut;
        this.id_offre          = id_offre;
    }

    // ── Getters / Setters ─────────────────────────────

    public int getId_reservation() { return id_reservation; }
    public void setId_reservation(int id_reservation) { this.id_reservation = id_reservation; }

    public Date getDate_reservation() { return date_reservation; }
    public void setDate_reservation(Date date_reservation) { this.date_reservation = date_reservation; }

    public double getPrix_reservation() { return prix_reservation; }
    public void setPrix_reservation(double prix_reservation) { this.prix_reservation = prix_reservation; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNum_tel() { return num_tel; }
    public void setNum_tel(String num_tel) { this.num_tel = num_tel; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public int getNombre_personnes() { return nombre_personnes; }
    public void setNombre_personnes(int nombre_personnes) { this.nombre_personnes = nombre_personnes; }

    /** Numéros de passeport séparés par " | " (un par personne). */
    public String getNum_passeport() { return num_passeport; }
    public void setNum_passeport(String num_passeport) { this.num_passeport = num_passeport; }

    public Personne getId_personne() { return id_personne; }
    public void setId_personne(Personne id_personne) { this.id_personne = id_personne; }

    public Voyage getId_voyage() { return id_voyage; }
    public void setId_voyage(Voyage id_voyage) { this.id_voyage = id_voyage; }

    public StatutReservation getId_statut() { return id_statut; }
    public void setId_statut(StatutReservation id_statut) { this.id_statut = id_statut; }

    public Offre getId_offre() { return id_offre; }
    public void setId_offre(Offre id_offre) { this.id_offre = id_offre; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id_reservation=" + id_reservation +
                ", date_reservation=" + date_reservation +
                ", prix_reservation=" + prix_reservation +
                ", etat='" + etat + '\'' +
                ", email='" + email + '\'' +
                ", num_tel='" + num_tel + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", nombre_personnes=" + nombre_personnes +
                ", num_passeport='" + num_passeport + '\'' +
                ", id_personne=" + id_personne +
                ", id_voyage=" + id_voyage +
                ", id_statut=" + id_statut +
                ", id_offre=" + id_offre +
                '}';
    }
}