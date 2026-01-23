package Entite;

import java.time.LocalDate;

public class Facture {
    private int idFacture;
    private String numeroFacture;
    private LocalDate dateFacture;
    private double montantTotal;
    private Paiement paiement;


    public int getIdFacture() { return idFacture; }
    public void setIdFacture(int idFacture) { this.idFacture = idFacture; }

    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }

    public LocalDate getDateFacture() { return dateFacture; }
    public void setDateFacture(LocalDate dateFacture) { this.dateFacture = dateFacture; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public Paiement getPaiement() { return paiement; }
    public void setPaiement(Paiement paiement) { this.paiement = paiement; }


    public void genererNumero() {
        this.numeroFacture = "FAC-" + System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Facture{" +
                "idFacture=" + idFacture +
                ", numeroFacture='" + numeroFacture + '\'' +
                ", dateFacture=" + dateFacture +
                ", montantTotal=" + montantTotal +
                ", paiement=" + paiement +
                '}';
    }
}
