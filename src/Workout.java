/** workouts.txt: id|memberID|exerciseName|duration|caloriesBurned|date */
public class Workout implements Storable {
    private int workoutID, memberID, duration;
    private double caloriesBurned;
    private String exerciseName, date;

    public Workout(int workoutID, int memberID, String exerciseName, int duration, double caloriesBurned, String date) {
        this.workoutID = workoutID; this.memberID = memberID; this.date = date;
        setExerciseName(exerciseName); setDuration(duration); setCaloriesBurned(caloriesBurned);
    }

    @Override public int getId() { return workoutID; }
    public int getMemberID() { return memberID; }
    public String getExerciseName() { return exerciseName; }
    public int getDuration() { return duration; }
    public double getCaloriesBurned() { return caloriesBurned; }
    public String getDate() { return date; }

    public void setExerciseName(String e) {
        if (e == null || e.trim().isEmpty()) throw new IllegalArgumentException("Exercise name required.");
        this.exerciseName = e;
    }
    public void setDuration(int d) {
        if (d <= 0) throw new IllegalArgumentException("Duration must be positive.");
        this.duration = d;
    }
    public void setCaloriesBurned(double c) {
        if (c < 0) throw new IllegalArgumentException("Calories cannot be negative.");
        this.caloriesBurned = c;
    }

    @Override public String toFileString() {
        return workoutID + "|" + memberID + "|" + exerciseName + "|" + duration + "|" + caloriesBurned + "|" + date;
    }
    public static Workout fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        return new Workout(Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2],
                Integer.parseInt(p[3]), Double.parseDouble(p[4]), p[5]);
    }
    @Override public String toString() {
        return String.format("#%d  %s | %d min | %.0f kcal | %s", workoutID, exerciseName, duration, caloriesBurned, date);
    }
}
