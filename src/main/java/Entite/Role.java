package Entite;

public class Role {
    private int id;
    private String nomRole;

    public Role() {}
    public Role(String nomRole) { this.nomRole = nomRole; }
    public Role(int id, String nomRole) {
        this.id = id;
        this.nomRole = nomRole;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomRole() { return nomRole; }
    public void setNomRole(String nomRole) { this.nomRole = nomRole; }

    @Override
    public String toString() {
        return "Role{id=" + id + ", nomRole='" + nomRole + "'}";
    }
}