package Entite;

import java.sql.Date;

public class Avis {

    private int id;
    private int note;
    private String commentaire;
    private Date dateAvis;
    private int idUtilisateur;

    public Avis() {
    }

    public Avis(int note, String commentaire, Date dateAvis, int idUtilisateur) {
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
        this.idUtilisateur = idUtilisateur;
    }

    public Avis(int id, int note, String commentaire, Date dateAvis, int idUtilisateur) {
        this.id = id;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
        this.idUtilisateur = idUtilisateur;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Date getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(Date dateAvis) {
        this.dateAvis = dateAvis;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    @Override
    public String toString() {
        return "Avis{" +
                "id=" + id +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateAvis=" + dateAvis +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }
}
