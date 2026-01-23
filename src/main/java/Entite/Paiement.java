package Entite;

import java.time.LocalDateTime;

public class Paiement {
    private int idPaiement;
    private double montant;
    private LocalDateTime datePaiement;
    private String statut;
    private int idReservation;
    private TypePaiement typePaiement;

    // Getters / Setters
    public int getIdPaiement() { return idPaiement; }
    public void setIdPaiement(int idPaiement) { this.idPaiement = idPaiement; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public TypePaiement getTypePaiement() { return typePaiement; }
    public void setTypePaiement(TypePaiement typePaiement) { this.typePaiement = typePaiement; }

    // Méthode métier
    public boolean estPaye() {
        return "PAYE".equals(statut);
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "idPaiement=" + idPaiement +
                ", montant=" + montant +
                ", datePaiement=" + datePaiement +
                ", statut='" + statut + '\'' +
                ", idReservation=" + idReservation +
                ", typePaiement=" + typePaiement +
                '}';
    }
}
