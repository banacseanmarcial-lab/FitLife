import java.time.LocalDate;
import java.util.*;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static FileManager users = new FileManager("data/users.txt");
    static FileManager workouts = new FileManager("data/workouts.txt");
    static FileManager goals = new FileManager("data/goals.txt");
    static FileManager measures = new FileManager("data/measurements.txt");
    static FileManager reports = new FileManager("data/reports.txt");

    public static void main(String[] args) {
        seedAdmin();
        while (true) {
            System.out.println("\n=== FITLIFE ===\n1. Register   2. Login   0. Exit");
            switch (askInt("Choice")) {
                case 1: register(); break;
                case 2: login(); break;
                case 0: System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ---------- input helpers ----------
    static String ask(String p) { System.out.print(p + ": "); return FileManager.clean(sc.nextLine().trim()); }
    static int askInt(String p) {
        while (true) {
            try { return Integer.parseInt(ask(p)); }
            catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
        }
    }
    static double askDouble(String p) {
        while (true) {
            try { return Double.parseDouble(ask(p)); }
            catch (NumberFormatException e) { System.out.println("Enter a number."); }
        }
    }
    static String today() { return LocalDate.now().toString(); }

    // ---------- users ----------
    static void seedAdmin() {
        if (users.readAll().isEmpty())
            users.append(new Admin(1, "Admin", "admin@fitlife.com", "admin123", 1).toFileString());
    }

    static User findByEmail(String email) {
        for (String l : users.readAll()) {
            User u = User.fromFileString(l);
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }
    static User findById(int id) {
        for (String l : users.readAll()) {
            User u = User.fromFileString(l);
            if (u.getId() == id) return u;
        }
        return null;
    }

    static void register() {
        System.out.println("Register as: 1. Member  2. Trainer");
        int type = askInt("Choice");
        try {
            String name = ask("Name"), email = ask("Email"), pw = ask("Password");
            if (findByEmail(email) != null) { System.out.println("Email already registered."); return; }
            int id = users.nextId();
            User u;
            if (type == 1) {
                int age = askInt("Age");
                double w = askDouble("Weight (kg)"), h = askDouble("Height (cm)");
                int tid = askInt("Trainer ID (0 for none)");
                if (tid != 0 && !(findById(tid) instanceof Trainer)) { System.out.println("No such trainer; set to none."); tid = 0; }
                u = new Member(id, name, email, pw, age, w, h, tid);
            } else if (type == 2) {
                u = new Trainer(id, name, email, pw, ask("Specialization"));
            } else { System.out.println("Invalid choice."); return; }
            users.append(u.toFileString());
            System.out.println("Registered! Your user ID is " + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    static void login() {
        String email = ask("Email"), pw = ask("Password");
        User u = findByEmail(email);
        if (u == null || !u.login(email, pw)) { System.out.println("Invalid email or password."); return; }
        session(u);
    }

    /** Polymorphism: current is a User; displayDashboard() runs the role's own version. */
    static void session(User current) {
        while (true) {
            current.displayDashboard();
            int c = askInt("Choice");
            if (c == 0) { current.logout(); return; }
            if (current instanceof Member) memberAction((Member) current, c);
            else if (current instanceof Trainer) trainerAction((Trainer) current, c);
            else adminAction((Admin) current, c);
        }
    }

    // ---------- loaders ----------
    static List<Workout> loadWorkouts(int mid) {
        List<Workout> r = new ArrayList<>();
        for (String l : workouts.readAll()) { Workout w = Workout.fromFileString(l); if (w.getMemberID() == mid) r.add(w); }
        return r;
    }
    static List<FitnessGoal> loadGoals(int mid) {
        List<FitnessGoal> r = new ArrayList<>();
        for (String l : goals.readAll()) { FitnessGoal g = FitnessGoal.fromFileString(l); if (g.getMemberID() == mid) r.add(g); }
        return r;
    }
    static List<BodyMeasurement> loadMeasures(int mid) {
        List<BodyMeasurement> r = new ArrayList<>();
        for (String l : measures.readAll()) { BodyMeasurement m = BodyMeasurement.fromFileString(l); if (m.getMemberID() == mid) r.add(m); }
        return r;
    }
    static List<FitnessReport> loadReports(int mid) {
        List<FitnessReport> r = new ArrayList<>();
        for (String l : reports.readAll()) { FitnessReport f = FitnessReport.fromFileString(l); if (f.getMemberID() == mid) r.add(f); }
        return r;
    }
    static <T> void printAll(List<T> list) {
        if (list.isEmpty()) System.out.println("(nothing yet)");
        for (T t : list) System.out.println(t);
    }

    // ---------- member ----------
    static void memberAction(Member m, int c) {
        try {
            switch (c) {
                case 1: workoutMenu(m); break;
                case 2: goalMenu(m); break;
                case 3: measureMenu(m); break;
                case 4: reportMenu(m); break;
                case 5: editProfile(m); break;
                default: System.out.println("Invalid choice.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Not saved: " + e.getMessage());
        }
    }

    static void editProfile(Member m) {
        m.setName(ask("New name"));
        m.setPassword(ask("New password"));
        m.setWeight(askDouble("Weight (kg)"));
        m.setHeight(askDouble("Height (cm)"));
        users.update(m.getId(), m.toFileString());
        System.out.println("Profile updated.");
    }

    static void workoutMenu(Member m) {
        System.out.println("1. Add  2. View  3. Edit  4. Delete");
        int c = askInt("Choice");
        if (c == 1) {
            Workout w = new Workout(workouts.nextId(), m.getId(), ask("Exercise"), askInt("Duration (min)"),
                    askDouble("Calories burned"), today());
            workouts.append(w.toFileString());
            System.out.println("Workout added.");
        } else if (c == 2) printAll(loadWorkouts(m.getId()));
        else if (c == 3 || c == 4) {
            printAll(loadWorkouts(m.getId()));
            int id = askInt("Workout ID");
            Workout w = null;
            for (Workout x : loadWorkouts(m.getId())) if (x.getId() == id) w = x;
            if (w == null) { System.out.println("Not found."); return; }
            if (c == 4) { workouts.delete(id); System.out.println("Deleted."); return; }
            w.setExerciseName(ask("Exercise")); w.setDuration(askInt("Duration (min)"));
            w.setCaloriesBurned(askDouble("Calories burned"));
            workouts.update(id, w.toFileString());
            System.out.println("Updated.");
        }
    }

    static void goalMenu(Member m) {
        System.out.println("1. Add  2. View  3. Edit  4. Delete");
        int c = askInt("Choice");
        if (c == 1) {
            FitnessGoal g = new FitnessGoal(goals.nextId(), m.getId(), ask("Goal type (e.g. Target Weight)"),
                    askDouble("Target value"), "In Progress");
            goals.append(g.toFileString());
            System.out.println("Goal added.");
        } else if (c == 2) printAll(loadGoals(m.getId()));
        else if (c == 3 || c == 4) {
            printAll(loadGoals(m.getId()));
            int id = askInt("Goal ID");
            FitnessGoal g = null;
            for (FitnessGoal x : loadGoals(m.getId())) if (x.getId() == id) g = x;
            if (g == null) { System.out.println("Not found."); return; }
            if (c == 4) { goals.delete(id); System.out.println("Deleted."); return; }
            g.setTargetValue(askDouble("New target"));
            g.setStatus(askInt("Status (1 = In Progress, 2 = Achieved)") == 2 ? "Achieved" : "In Progress");
            goals.update(id, g.toFileString());
            System.out.println("Updated.");
        }
    }

    static void measureMenu(Member m) {
        System.out.println("1. Add  2. View  3. Edit  4. Delete");
        int c = askInt("Choice");
        if (c == 1) {
            BodyMeasurement b = new BodyMeasurement(measures.nextId(), m.getId(), today(),
                    askDouble("Weight (kg)"), askDouble("Height (cm)"));
            measures.append(b.toFileString());
            System.out.println("Saved. BMI = " + b.getBmi());
        } else if (c == 2) printAll(loadMeasures(m.getId()));
        else if (c == 3 || c == 4) {
            printAll(loadMeasures(m.getId()));
            int id = askInt("Measurement ID");
            BodyMeasurement b = null;
            for (BodyMeasurement x : loadMeasures(m.getId())) if (x.getId() == id) b = x;
            if (b == null) { System.out.println("Not found."); return; }
            if (c == 4) { measures.delete(id); System.out.println("Deleted."); return; }
            b.setWeight(askDouble("Weight (kg)")); b.setHeight(askDouble("Height (cm)"));
            measures.update(id, b.toFileString());
            System.out.println("Updated. BMI = " + b.getBmi());
        }
    }

    static String buildSummary(int mid) {
        return FitnessReport.generateReport(loadWorkouts(mid), loadGoals(mid), loadMeasures(mid));
    }

    static void reportMenu(Member m) {
        System.out.println("1. Generate & save  2. View saved  3. Delete");
        int c = askInt("Choice");
        if (c == 1) {
            FitnessReport r = new FitnessReport(reports.nextId(), m.getId(), today(), buildSummary(m.getId()));
            reports.append(r.toFileString());
            System.out.println(r);
        } else if (c == 2) printAll(loadReports(m.getId()));
        else if (c == 3) {
            printAll(loadReports(m.getId()));
            int id = askInt("Report ID");
            for (FitnessReport r : loadReports(m.getId()))
                if (r.getId() == id) { reports.delete(id); System.out.println("Deleted."); return; }
            System.out.println("Not found.");
        }
    }

    // ---------- trainer ----------
    static List<Member> clientsOf(Trainer t) {
        List<Member> r = new ArrayList<>();
        for (String l : users.readAll()) {
            User u = User.fromFileString(l);
            if (u instanceof Member && ((Member) u).getTrainerID() == t.getId()) r.add((Member) u);
        }
        return r;
    }

    static Member pickClient(Trainer t) {
        List<Member> clients = clientsOf(t);
        if (clients.isEmpty()) { System.out.println("You have no clients yet."); return null; }
        for (Member m : clients) System.out.println("ID " + m.getId() + " - " + m.getName());
        int id = askInt("Client ID");
        for (Member m : clients) if (m.getId() == id) return m;
        System.out.println("Not your client.");
        return null;
    }

    static void trainerAction(Trainer t, int c) {
        try {
            if (c == 1) {
                List<Member> cl = clientsOf(t);
                if (cl.isEmpty()) System.out.println("No clients yet.");
                for (Member m : cl) System.out.println("ID " + m.getId() + " - " + m.getName() + " | " + buildSummary(m.getId()));
            } else if (c == 2) {
                Member m = pickClient(t);
                if (m != null) { System.out.println("Latest saved reports:"); printAll(loadReports(m.getId())); }
            } else if (c == 3) {
                Member m = pickClient(t);
                if (m == null) return;
                Workout w = new Workout(workouts.nextId(), m.getId(), "[Plan] " + ask("Exercise"),
                        askInt("Duration (min)"), askDouble("Target calories"), today());
                workouts.append(w.toFileString());
                System.out.println("Plan added to " + m.getName() + "'s workouts.");
            } else System.out.println("Invalid choice.");
        } catch (IllegalArgumentException e) {
            System.out.println("Not saved: " + e.getMessage());
        }
    }

    // ---------- admin ----------
    static void adminAction(Admin a, int c) {
        if (c == 1) {
            for (String l : users.readAll()) {
                User u = User.fromFileString(l);
                System.out.println("ID " + u.getId() + " | " + u.getRole() + " | " + u.getName() + " | " + u.getEmail());
            }
        } else if (c == 2) {
            String key = ask("Enter user ID or email");
            User u = key.contains("@") ? findByEmail(key) : null;
            if (u == null) try { u = findById(Integer.parseInt(key)); } catch (NumberFormatException e) { }
            System.out.println(u == null ? "Not found." : "ID " + u.getId() + " | " + u.getRole() + " | " + u.getName() + " | " + u.getEmail());
        } else if (c == 3) {
            int id = askInt("User ID to delete");
            User u = findById(id);
            if (u == null) { System.out.println("Not found."); return; }
            if (u instanceof Admin) { System.out.println("Admin accounts cannot be deleted."); return; }
            users.delete(id);
            if (u instanceof Member) {               // composition: records go with the member
                workouts.deleteWhere(1, "" + id);
                goals.deleteWhere(1, "" + id);
                measures.deleteWhere(1, "" + id);
                reports.deleteWhere(1, "" + id);
            } else {                                  // aggregation: clients survive, lose trainer
                for (String l : users.readAll()) {
                    User x = User.fromFileString(l);
                    if (x instanceof Member && ((Member) x).getTrainerID() == id) {
                        ((Member) x).setTrainerID(0);
                        users.update(x.getId(), x.toFileString());
                    }
                }
            }
            System.out.println("User deleted.");
        } else System.out.println("Invalid choice.");
    }
}
