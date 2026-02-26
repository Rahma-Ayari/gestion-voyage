package Entite;

import java.sql.Date;

/**
 * Entité Avis complète
 * Table : id, note, commentaire, date_avis, id_utilisateur, id_voyage, titre,
 * note_hebergement, note_transport, note_activites, note_qualite_prix, recommande
 */
public class Avis {

    private int id;
    private int idUtilisateur;
    private int idVoyage;          // ← NEW : lien vers le voyage évalué
    private int note;
    private String titre;
    private String commentaire;
    private int noteHebergement;
    private int noteTransport;
    private int noteActivites;
    private int noteQualitePrix;
    private boolean recommande;
    private Date dateAvis;

    // --- Constructeur Vide ---
    public Avis() {}

    // --- Constructeur pour l'INSERTION (sans ID) ---
    public Avis(int idUtilisateur, int idVoyage, int note, String titre, String commentaire,
                int noteHebergement, int noteTransport, int noteActivites,
                int noteQualitePrix, boolean recommande, Date dateAvis) {
        this.idUtilisateur  = idUtilisateur;
        this.idVoyage       = idVoyage;
        this.note           = note;
        this.titre          = titre;
        this.commentaire    = commentaire;
        this.noteHebergement = noteHebergement;
        this.noteTransport  = noteTransport;
        this.noteActivites  = noteActivites;
        this.noteQualitePrix = noteQualitePrix;
        this.recommande     = recommande;
        this.dateAvis       = dateAvis;
    }

    // --- Constructeur COMPLET (avec ID) ---
    public Avis(int id, int idUtilisateur, int idVoyage, int note, String titre, String commentaire,
                int noteHebergement, int noteTransport, int noteActivites,
                int noteQualitePrix, boolean recommande, Date dateAvis) {
        this.id             = id;
        this.idUtilisateur  = idUtilisateur;
        this.idVoyage       = idVoyage;
        this.note           = note;
        this.titre          = titre;
        this.commentaire    = commentaire;
        this.noteHebergement = noteHebergement;
        this.noteTransport  = noteTransport;
        this.noteActivites  = noteActivites;
        this.noteQualitePrix = noteQualitePrix;
        this.recommande     = recommande;
        this.dateAvis       = dateAvis;
    }

    // --- Getters & Setters ---

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getIdUtilisateur()               { return idUtilisateur; }
    public void setIdUtilisateur(int v)         { this.idUtilisateur = v; }

    public int getIdVoyage()                    { return idVoyage; }
    public void setIdVoyage(int v)              { this.idVoyage = v; }

    public int getNote()                        { return note; }
    public void setNote(int note)               { this.note = note; }

    public String getTitre()                    { return titre; }
    public void setTitre(String titre)          { this.titre = titre; }

    public String getCommentaire()              { return commentaire; }
    public void setCommentaire(String c)        { this.commentaire = c; }

    public int getNoteHebergement()             { return noteHebergement; }
    public void setNoteHebergement(int v)       { this.noteHebergement = v; }

    public int getNoteTransport()               { return noteTransport; }
    public void setNoteTransport(int v)         { this.noteTransport = v; }

    public int getNoteActivites()               { return noteActivites; }
    public void setNoteActivites(int v)         { this.noteActivites = v; }

    public int getNoteQualitePrix()             { return noteQualitePrix; }
    public void setNoteQualitePrix(int v)       { this.noteQualitePrix = v; }

    public boolean isRecommande()               { return recommande; }
    public void setRecommande(boolean v)        { this.recommande = v; }

    public Date getDateAvis()                   { return dateAvis; }
    public void setDateAvis(Date dateAvis)      { this.dateAvis = dateAvis; }

    @Override
    public String toString() {
        return "Avis{id=" + id + ", idUtilisateur=" + idUtilisateur +
                ", idVoyage=" + idVoyage + ", note=" + note +
                ", titre='" + titre + "', recommande=" + recommande + '}';
    }
}