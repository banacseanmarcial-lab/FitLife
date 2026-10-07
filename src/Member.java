public class Member extends User {
    private int age, trainerID;          // trainerID 0 = no trainer
    private double weight, height;       // kg, cm

    public Member(int id, String name, String email, String password,
                  int age, double weight, double height, int trainerID) {
        super(id, name, email, password);
        setAge(age); setWeight(weight); setHeight(height);
        this.trainerID = trainerID;
    }

    public int getAge() { return age; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public int getTrainerID() { return trainerID; }

    public void setAge(int age) {
        if (age <= 0) throw new IllegalArgumentException("Age must be positive.");
        this.age = age;
    }
    public void setWeight(double w) {
        if (w <= 0) throw new IllegalArgumentException("Weight must be positive.");
        this.weight = w;
    }
    public void setHeight(double h) {
        if (h <= 0) throw new IllegalArgumentException("Height must be positive.");
        this.height = h;
    }
    public void setTrainerID(int trainerID) { this.trainerID = trainerID; }

    @Override public String getRole() { return "MEMBER"; }

    @Override public void displayDashboard() {
        System.out.println("\n=== MEMBER DASHBOARD: " + getName() + " ===");
        System.out.println("1. Workouts   2. Fitness Goals   3. Body Measurements");
        System.out.println("4. Reports    5. Edit Profile    0. Logout");
    }

    @Override public String toFileString() {
        return baseString() + "|" + age + "|" + weight + "|" + height + "|" + trainerID;
    }
}
