public class Admin extends User {
    private int accessLevel;

    public Admin(int id, String name, String email, String password, int accessLevel) {
        super(id, name, email, password);
        this.accessLevel = accessLevel;
    }

    public int getAccessLevel() { return accessLevel; }
    public void setAccessLevel(int a) { this.accessLevel = a; }

    @Override public String getRole() { return "ADMIN"; }

    @Override public void displayDashboard() {
        System.out.println("\n=== ADMIN DASHBOARD: " + getName() + " ===");
        System.out.println("1. View All Users   2. Search User   3. Delete User   0. Logout");
    }

    @Override public String toFileString() { return baseString() + "|" + accessLevel; }
}
