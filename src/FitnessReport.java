import java.util.List;

/** reports.txt: id|memberID|date|progressSummary */
public class FitnessReport implements Storable {
    private int reportID, memberID;
    private String date, progressSummary;

    public FitnessReport(int reportID, int memberID, String date, String progressSummary) {
        this.reportID = reportID; this.memberID = memberID; this.date = date;
        this.progressSummary = progressSummary;
    }

    @Override public int getId() { return reportID; }
    public int getMemberID() { return memberID; }
    public String getDate() { return date; }
    public String getProgressSummary() { return progressSummary; }

    /** Builds a summary from the member's records (abstraction: callers just get a String). */
    public static String generateReport(List<Workout> ws, List<FitnessGoal> gs, List<BodyMeasurement> ms) {
        int minutes = 0; double kcal = 0;
        for (Workout w : ws) { minutes += w.getDuration(); kcal += w.getCaloriesBurned(); }
        int achieved = 0;
        for (FitnessGoal g : gs) if (g.getStatus().equals("Achieved")) achieved++;
        String bmiText = "no measurements yet";
        if (!ms.isEmpty()) {
            double first = ms.get(0).getBmi(), last = ms.get(ms.size() - 1).getBmi();
            bmiText = String.format("BMI %.1f -> %.1f (change %+.1f)", first, last, last - first);
        }
        return String.format("Workouts: %d (%d min, %.0f kcal). Goals achieved: %d of %d. %s.",
                ws.size(), minutes, kcal, achieved, gs.size(), bmiText);
    }

    @Override public String toFileString() {
        return reportID + "|" + memberID + "|" + date + "|" + progressSummary;
    }
    public static FitnessReport fromFileString(String line) {
        String[] p = line.split("\\|", 4);
        return new FitnessReport(Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2], p[3]);
    }
    @Override public String toString() { return "#" + reportID + "  " + date + " | " + progressSummary; }
}
