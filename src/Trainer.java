public class Trainer extends User {
    private String specialization;

    public Trainer(int id, String name, String email, String password, String specialization) {
        super(id, name, email, password);
        setSpecialization(specialization);
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String s) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException("Specialization cannot be empty.");
        this.specialization = s;
    }

    @Override public String getRole() { return "TRAINER"; }

    @Override public void displayDashboard() {
        System.out.println("\n=== TRAINER DASHBOARD: " + getName() + " (" + specialization + ") ===");
        System.out.println("1. View Clients   2. View Client Report");
        System.out.println("3. Create Workout Plan for Client   0. Logout");
    }

    @Override public String toFileString() { return baseString() + "|" + specialization; }
}
