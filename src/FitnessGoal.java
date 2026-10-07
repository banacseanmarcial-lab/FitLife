/** goals.txt: id|memberID|goalType|targetValue|status */
public class FitnessGoal implements Storable {
    private int goalID, memberID;
    private String goalType, status;
    private double targetValue;

    public FitnessGoal(int goalID, int memberID, String goalType, double targetValue, String status) {
        this.goalID = goalID; this.memberID = memberID;
        setGoalType(goalType); setTargetValue(targetValue); setStatus(status);
    }

    @Override public int getId() { return goalID; }
    public int getMemberID() { return memberID; }
    public String getGoalType() { return goalType; }
    public double getTargetValue() { return targetValue; }
    public String getStatus() { return status; }

    public void setGoalType(String g) {
        if (g == null || g.trim().isEmpty()) throw new IllegalArgumentException("Goal type required.");
        this.goalType = g;
    }
    public void setTargetValue(double t) {
        if (t <= 0) throw new IllegalArgumentException("Target must be positive.");
        this.targetValue = t;
    }
    public void setStatus(String s) {
        if (!s.equals("In Progress") && !s.equals("Achieved"))
            throw new IllegalArgumentException("Status must be 'In Progress' or 'Achieved'.");
        this.status = s;
    }

    @Override public String toFileString() {
        return goalID + "|" + memberID + "|" + goalType + "|" + targetValue + "|" + status;
    }
    public static FitnessGoal fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        return new FitnessGoal(Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2], Double.parseDouble(p[3]), p[4]);
    }
    @Override public String toString() {
        return String.format("#%d  %s -> %.1f | %s", goalID, goalType, targetValue, status);
    }
}
