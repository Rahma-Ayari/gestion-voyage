package Entite;

public class Budget {
    private int idBudget;
    private double montantMax;
    private double montantUtilise;
    private int idUtilisateur;


    public int getIdBudget() { return idBudget; }
    public void setIdBudget(int idBudget) { this.idBudget = idBudget; }

    public double getMontantMax() { return montantMax; }
    public void setMontantMax(double montantMax) { this.montantMax = montantMax; }

    public double getMontantUtilise() { return montantUtilise; }
    public void setMontantUtilise(double montantUtilise) { this.montantUtilise = montantUtilise; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }


    public boolean depasseBudget() {
        return montantUtilise > montantMax;
    }

    public void ajouterDepense(double montant) {
        this.montantUtilise += montant;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "idBudget=" + idBudget +
                ", montantMax=" + montantMax +
                ", montantUtilise=" + montantUtilise +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }
}
