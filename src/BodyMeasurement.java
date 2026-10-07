/** measurements.txt: id|memberID|date|weight|height|bmi   (weight kg, height cm) */
public class BodyMeasurement implements Storable {
    private int measurementID, memberID;
    private String date;
    private double weight, height, bmi;

    public BodyMeasurement(int measurementID, int memberID, String date, double weight, double height) {
        this.measurementID = measurementID; this.memberID = memberID; this.date = date;
        setWeight(weight); setHeight(height);
    }

    @Override public int getId() { return measurementID; }
    public int getMemberID() { return memberID; }
    public String getDate() { return date; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public double getBmi() { return bmi; }

    public void setWeight(double w) {
        if (w <= 0) throw new IllegalArgumentException("Weight must be positive.");
        this.weight = w; calculateBMI();
    }
    public void setHeight(double h) {
        if (h <= 0) throw new IllegalArgumentException("Height must be positive.");
        this.height = h; calculateBMI();
    }
    public double calculateBMI() {
        if (weight > 0 && height > 0) {
            double m = height / 100.0;
            bmi = Math.round(weight / (m * m) * 10.0) / 10.0;
        }
        return bmi;
    }

    @Override public String toFileString() {
        return measurementID + "|" + memberID + "|" + date + "|" + weight + "|" + height + "|" + bmi;
    }
    public static BodyMeasurement fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        return new BodyMeasurement(Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2],
                Double.parseDouble(p[3]), Double.parseDouble(p[4]));
    }
    @Override public String toString() {
        return String.format("#%d  %s | %.1f kg | %.1f cm | BMI %.1f", measurementID, date, weight, height, bmi);
    }
}
