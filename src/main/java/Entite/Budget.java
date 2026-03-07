package Entite;

public class Budget {

    private int    idBudget;
    private int    idVoyage;
    private double totalVol;
    private double totalHotel;
    private double totalActivite;
    private double totalService;
    private double totalGlobal;

    public Budget() {}

    public Budget(int idVoyage,
                  double totalVol,
                  double totalHotel,
                  double totalActivite,
                  double totalService,
                  double totalGlobal) {
        this.idVoyage      = idVoyage;
        this.totalVol      = totalVol;
        this.totalHotel    = totalHotel;
        this.totalActivite = totalActivite;
        this.totalService  = totalService;
        this.totalGlobal   = totalGlobal;
    }

    public int getIdBudget() {
        return idBudget;
    }

    public void setIdBudget(int idBudget) {
        this.idBudget = idBudget;
    }

    public int getIdVoyage() {
        return idVoyage;
    }

    public void setIdVoyage(int idVoyage) {
        this.idVoyage = idVoyage;
    }

    public double getTotalVol() {
        return totalVol;
    }

    public void setTotalVol(double totalVol) {
        this.totalVol = totalVol;
    }

    public double getTotalHotel() {
        return totalHotel;
    }

    public void setTotalHotel(double totalHotel) {
        this.totalHotel = totalHotel;
    }

    public double getTotalActivite() {
        return totalActivite;
    }

    public void setTotalActivite(double totalActivite) {
        this.totalActivite = totalActivite;
    }

    public double getTotalService() {
        return totalService;
    }

    public void setTotalService(double totalService) {
        this.totalService = totalService;
    }

    public double getTotalGlobal() {
        return totalGlobal;
    }

    public void setTotalGlobal(double totalGlobal) {
        this.totalGlobal = totalGlobal;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "idBudget=" + idBudget +
                ", idVoyage=" + idVoyage +
                ", totalVol=" + totalVol +
                ", totalHotel=" + totalHotel +
                ", totalActivite=" + totalActivite +
                ", totalService=" + totalService +
                ", totalGlobal=" + totalGlobal +
                '}';
    }
}
