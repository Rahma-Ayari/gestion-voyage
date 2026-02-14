package Entite;

import java.sql.Date;

/**
 * Entité Avis complète
 * Table : id, note, commentaire, date_avis, id_utilisateur, titre,
 * note_hebergement, note_transport, note_activites, note_qualite_prix, recommande
 */
public class Avis {

    private int id;
    private int idUtilisateur;
    private int note; // Note globale (étoiles)
    private String titre;
    private String commentaire;
    private int noteHebergement;
    private int noteTransport;
    private int noteActivites;
    private int noteQualitePrix;
    private boolean recommande;
    private Date dateAvis;

    // --- 1. Constructeur Vide ---
    public Avis() {
    }

    // --- 2. Constructeur pour l'INSERTION (sans ID) ---
    // Utilisé dans AvisUserController lors du clic sur "Publier"
    public Avis(int idUtilisateur, int note, String titre, String commentaire,
                int noteHebergement, int noteTransport, int noteActivites,
                int noteQualitePrix, boolean recommande, Date dateAvis) {
        this.idUtilisateur = idUtilisateur;
        this.note = note;
        this.titre = titre;
        this.commentaire = commentaire;
        this.noteHebergement = noteHebergement;
        this.noteTransport = noteTransport;
        this.noteActivites = noteActivites;
        this.noteQualitePrix = noteQualitePrix;
        this.recommande = recommande;
        this.dateAvis = dateAvis;
    }

    // --- 3. Constructeur COMPLET (avec ID) ---
    // Utilisé pour récupérer les données depuis la base de données
    public Avis(int id, int idUtilisateur, int note, String titre, String commentaire,
                int noteHebergement, int noteTransport, int noteActivites,
                int noteQualitePrix, boolean recommande, Date dateAvis) {
        this.id = id;
        this.idUtilisateur = idUtilisateur;
        this.note = note;
        this.titre = titre;
        this.commentaire = commentaire;
        this.noteHebergement = noteHebergement;
        this.noteTransport = noteTransport;
        this.noteActivites = noteActivites;
        this.noteQualitePrix = noteQualitePrix;
        this.recommande = recommande;
        this.dateAvis = dateAvis;
    }

    // --- 4. Getters et Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public int getNoteHebergement() { return noteHebergement; }
    public void setNoteHebergement(int noteHebergement) { this.noteHebergement = noteHebergement; }

    public int getNoteTransport() { return noteTransport; }
    public void setNoteTransport(int noteTransport) { this.noteTransport = noteTransport; }

    public int getNoteActivites() { return noteActivites; }
    public void setNoteActivites(int noteActivites) { this.noteActivites = noteActivites; }

    public int getNoteQualitePrix() { return noteQualitePrix; }
    public void setNoteQualitePrix(int noteQualitePrix) { this.noteQualitePrix = noteQualitePrix; }

    public boolean isRecommande() { return recommande; }
    public void setRecommande(boolean recommande) { this.recommande = recommande; }

    public Date getDateAvis() { return dateAvis; }
    public void setDateAvis(Date dateAvis) { this.dateAvis = dateAvis; }

    // --- 5. Méthode toString ---
    @Override
    public String toString() {
        return "Avis{" +
                "id=" + id +
                ", idUtilisateur=" + idUtilisateur +
                ", note=" + note +
                ", titre='" + titre + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", noteHebergement=" + noteHebergement +
                ", noteTransport=" + noteTransport +
                ", noteActivites=" + noteActivites +
                ", noteQualitePrix=" + noteQualitePrix +
                ", recommande=" + recommande +
                ", dateAvis=" + dateAvis +
                '}';
    }
}