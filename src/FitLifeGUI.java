import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Graphical version of FitLife. Run THIS file instead of Main.java.
 * It uses the group's classes (User, Member, Trainer, Admin, Workout, FitnessGoal,
 * BodyMeasurement, FitnessReport, FileManager) and Main's file helpers, so the
 * console version and the GUI share the same data/*.txt files.
 */
public class FitLifeGUI extends JFrame {
    private static final Color GREEN = new Color(30, 130, 90);
    private static final Color LIGHT = new Color(240, 247, 243);

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private JPanel dashboard;

    public FitLifeGUI() {
        super("FitLife: Fitness and Health Monitoring System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 680);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);

        root.add(buildLoginPanel(), "login");
        add(root);
        cards.show(root, "login");
    }

    // =====================================================================
    // Screen switching
    // =====================================================================
    private void showLogin() {
        if (dashboard != null) {
            root.remove(dashboard);
            dashboard = null;
        }
        cards.show(root, "login");
    }

    private void showDashboard(User u) {
        if (dashboard != null) {
            root.remove(dashboard);
        }
        dashboard = buildDashboard(u);
        root.add(dashboard, "dashboard");
        cards.show(root, "dashboard");
        root.revalidate();
        root.repaint();
    }

    // =====================================================================
    // Login and register
    // =====================================================================
    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(LIGHT);

        JLabel title = new JLabel("FitLife", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 36f));
        title.setForeground(GREEN);
        JLabel sub = new JLabel("Fitness and Health Monitoring System", SwingConstants.CENTER);
        sub.setForeground(Color.DARK_GRAY);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);

        JTextField email = new JTextField(22);
        JPasswordField password = new JPasswordField(22);
        JPanel form = formOf("Email", email, "Password", password);
        form.setOpaque(false);

        JButton login = new JButton("Login");
        login.setFont(login.getFont().deriveFont(Font.BOLD));
        JButton register = new JButton("Create an account");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        buttons.setOpaque(false);
        buttons.add(login);
        buttons.add(register);

        JLabel hint = new JLabel("Demo admin: admin@fitlife.com / admin123", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        hint.setFont(hint.getFont().deriveFont(12f));
        JPanel south = new JPanel(new GridLayout(2, 1, 0, 6));
        south.setOpaque(false);
        south.add(buttons);
        south.add(hint);

        JPanel box = new JPanel(new BorderLayout(0, 14));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 215)),
                BorderFactory.createEmptyBorder(28, 36, 24, 36)));
        box.add(titles, BorderLayout.NORTH);
        box.add(form, BorderLayout.CENTER);
        box.add(south, BorderLayout.SOUTH);
        outer.add(box);

        Runnable doLogin = () -> {
            String typedEmail = FileManager.clean(email.getText().trim());
            String typedPassword = FileManager.clean(new String(password.getPassword()).trim());
            User u = Main.findByEmail(typedEmail);
            if (u != null && u.login(typedEmail, typedPassword)) {
                password.setText("");
                showDashboard(u);
            } else {
                error("Invalid email or password.");
            }
        };
        login.addActionListener(e -> doLogin.run());
        password.addActionListener(e -> doLogin.run());
        email.addActionListener(e -> password.requestFocusInWindow());
        register.addActionListener(e -> showRegisterDialog());
        return outer;
    }

    private void showRegisterDialog() {
        JComboBox<String> role = new JComboBox<>(new String[]{"Member", "Trainer"});
        JTextField name = new JTextField(18);
        JTextField email = new JTextField(18);
        JPasswordField password = new JPasswordField(18);
        JTextField age = new JTextField(18);
        JTextField weight = new JTextField(18);
        JTextField height = new JTextField(18);
        JComboBox<String> trainerBox = new JComboBox<>();
        JTextField specialization = new JTextField(18);
        specialization.setEnabled(false);

        List<Integer> trainerIds = new ArrayList<>();
        trainerIds.add(0);
        trainerBox.addItem("None");
        for (String line : Main.users.readAll()) {
            User x = User.fromFileString(line);
            if (x instanceof Trainer t) {
                trainerIds.add(t.getId());
                trainerBox.addItem(t.getId() + " - " + t.getName() + " (" + t.getSpecialization() + ")");
            }
        }

        role.addActionListener(e -> {
            boolean member = role.getSelectedIndex() == 0;
            age.setEnabled(member);
            weight.setEnabled(member);
            height.setEnabled(member);
            trainerBox.setEnabled(member);
            specialization.setEnabled(!member);
        });

        JPanel form = formOf("Register as", role, "Name", name, "Email", email, "Password", password,
                "Age (Member)", age, "Weight in kg (Member)", weight, "Height in cm (Member)", height,
                "Trainer (Member)", trainerBox, "Specialization (Trainer)", specialization);

        int[] newId = {0};
        boolean ok = showForm("Create an account", form, () -> {
            String em = text(email);
            if (Main.findByEmail(em) != null) {
                throw new IllegalArgumentException("Email already registered.");
            }
            int id = Main.users.nextId();
            String pw = FileManager.clean(new String(password.getPassword()).trim());
            User u;
            if (role.getSelectedIndex() == 0) {
                u = new Member(id, text(name), em, pw,
                        Integer.parseInt(age.getText().trim()),
                        Double.parseDouble(weight.getText().trim()),
                        Double.parseDouble(height.getText().trim()),
                        trainerIds.get(trainerBox.getSelectedIndex()));
            } else {
                u = new Trainer(id, text(name), em, pw, text(specialization));
            }
            Main.users.append(u.toFileString());
            newId[0] = id;
        });
        if (ok) {
            info("Registered! Your user ID is " + newId[0] + ". You can log in now.");
        }
    }

    // =====================================================================
    // Dashboards
    // =====================================================================
    private JPanel buildDashboard(User u) {
        JLabel welcome = new JLabel();
        welcome.setForeground(Color.WHITE);
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 18f));
        Runnable updateWelcome = () ->
                welcome.setText("Welcome, " + u.getName() + "   (" + u.getRole() + ")");
        updateWelcome.run();

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> showLogin());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        header.add(welcome, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        if (u instanceof Member m) {
            tabs.addTab("Profile", profilePanel(m, updateWelcome));
            tabs.addTab("My Trainer", trainerChoicePanel(m));
            tabs.addTab("Workouts", workoutsPanel(m));
            tabs.addTab("Fitness Goals", goalsPanel(m));
            tabs.addTab("Measurements", measurementsPanel(m));
            tabs.addTab("Reports", reportsPanel(m));
        } else if (u instanceof Trainer t) {
            tabs.addTab("My Clients", clientsPanel(t));
            tabs.addTab("Client Reports", clientReportsPanel(t));
            tabs.addTab("Workout Plans", planPanel(t));
            tabs.addTab("Profile", profilePanel(t, updateWelcome));
        } else if (u instanceof Admin a) {
            tabs.addTab("Users", usersPanel(a));
            tabs.addTab("Profile", profilePanel(a, updateWelcome));
        }

        JPanel page = new JPanel(new BorderLayout());
        page.add(header, BorderLayout.NORTH);
        page.add(tabs, BorderLayout.CENTER);
        return page;
    }

    // =====================================================================
    // Profile (every role, and the admin's "edit user" dialog)
    // =====================================================================
    private JPanel profilePanel(User u, Runnable onSaved) {
        JTextField name = new JTextField(u.getName(), 20);
        JTextField email = new JTextField(u.getEmail(), 20);
        JPasswordField password = new JPasswordField(u.getPassword(), 20);
        JTextField age = new JTextField(10);
        JTextField weight = new JTextField(10);
        JTextField height = new JTextField(10);
        JTextField specialization = new JTextField(20);

        List<Object> rows = new ArrayList<>(List.of(
                "User ID", new JLabel(String.valueOf(u.getId())),
                "Role", new JLabel(u.getRole()),
                "Name", name, "Email", email, "Password", password));
        if (u instanceof Member m) {
            age.setText(String.valueOf(m.getAge()));
            weight.setText(String.valueOf(m.getWeight()));
            height.setText(String.valueOf(m.getHeight()));
            rows.addAll(List.of("Age", age, "Weight (kg)", weight, "Height (cm)", height));
        } else if (u instanceof Trainer t) {
            specialization.setText(t.getSpecialization());
            rows.addAll(List.of("Specialization", specialization));
        }
        JPanel form = formOf(rows.toArray());

        JButton save = new JButton("Save changes");
        save.setFont(save.getFont().deriveFont(Font.BOLD));
        save.addActionListener(e -> {
            try {
                String em = text(email);
                User other = Main.findByEmail(em);
                if (other != null && other.getId() != u.getId()) {
                    error("That email is already used.");
                    return;
                }
                u.setName(text(name));
                u.setEmail(em);
                u.setPassword(FileManager.clean(new String(password.getPassword()).trim()));
                if (u instanceof Member m) {
                    m.setAge(Integer.parseInt(age.getText().trim()));
                    m.setWeight(Double.parseDouble(weight.getText().trim()));
                    m.setHeight(Double.parseDouble(height.getText().trim()));
                } else if (u instanceof Trainer t) {
                    t.setSpecialization(text(specialization));
                }
                Main.users.update(u.getId(), u.toFileString());
                info("Profile updated.");
                onSaved.run();
            } catch (NumberFormatException ex) {
                restoreFromFile(u);
                error("Age, weight and height must be numbers.");
            } catch (IllegalArgumentException ex) {
                restoreFromFile(u);
                error(ex.getMessage());
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(save);
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        content.add(form, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.add(content);
        return wrapper;
    }

    // If an edit fails halfway, put the saved values back so nothing stays half-changed
    private void restoreFromFile(User u) {
        User fresh = Main.findById(u.getId());
        if (fresh == null) {
            return;
        }
        u.setName(fresh.getName());
        u.setEmail(fresh.getEmail());
        u.setPassword(fresh.getPassword());
        if (u instanceof Member m && fresh instanceof Member f) {
            m.setAge(f.getAge());
            m.setWeight(f.getWeight());
            m.setHeight(f.getHeight());
        } else if (u instanceof Trainer t && fresh instanceof Trainer f) {
            t.setSpecialization(f.getSpecialization());
        }
    }

    // =====================================================================
    // Member tabs
    // =====================================================================
    private JPanel trainerChoicePanel(Member m) {
        JLabel current = new JLabel();
        current.setFont(current.getFont().deriveFont(Font.BOLD, 15f));
        JComboBox<String> combo = new JComboBox<>();
        List<Trainer> trainers = new ArrayList<>();

        Runnable refresh = () -> {
            trainers.clear();
            combo.removeAllItems();
            for (String line : Main.users.readAll()) {
                User x = User.fromFileString(line);
                if (x instanceof Trainer t) {
                    trainers.add(t);
                    combo.addItem(t.getId() + " - " + t.getName() + " (" + t.getSpecialization() + ")");
                }
            }
            User assigned = m.getTrainerID() == 0 ? null : Main.findById(m.getTrainerID());
            current.setText(assigned instanceof Trainer tr
                    ? "Current trainer: " + tr.getName() + " (" + tr.getSpecialization() + ")"
                    : "You have no trainer yet.");
        };
        refresh.run();

        JButton assign = new JButton("Choose this trainer");
        assign.addActionListener(e -> {
            int i = combo.getSelectedIndex();
            if (i < 0) {
                error("There are no trainers registered yet.");
                return;
            }
            m.setTrainerID(trainers.get(i).getId());
            Main.users.update(m.getId(), m.toFileString());
            refresh.run();
            info("Trainer assigned.");
        });
        JButton remove = new JButton("Remove my trainer");
        remove.addActionListener(e -> {
            m.setTrainerID(0);
            Main.users.update(m.getId(), m.toFileString());
            refresh.run();
        });
        JButton reload = new JButton("Refresh list");
        reload.addActionListener(e -> refresh.run());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(assign);
        buttons.add(remove);
        buttons.add(reload);

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        content.add(current);
        content.add(new JLabel("Available trainers:"));
        content.add(combo);
        content.add(buttons);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.NORTH);
        return wrapper;
    }

    // ---------- Workouts ----------
    private JPanel workoutsPanel(Member m) {
        return this.<Workout>crudPanel("Add workout",
                new String[]{"ID", "Exercise", "Duration (min)", "Calories burned", "Date"},
                () -> Main.loadWorkouts(m.getId()),
                w -> new Object[]{w.getId(), w.getExerciseName(), w.getDuration(),
                        w.getCaloriesBurned(), w.getDate()},
                () -> workoutForm(m, null),
                w -> workoutForm(m, w),
                w -> Main.workouts.delete(w.getId()),
                null, null);
    }

    private void workoutForm(Member m, Workout w) {
        JTextField exercise = new JTextField(w == null ? "" : w.getExerciseName(), 18);
        JTextField duration = new JTextField(w == null ? "" : String.valueOf(w.getDuration()), 18);
        JTextField calories = new JTextField(w == null ? "" : String.valueOf(w.getCaloriesBurned()), 18);
        JPanel form = formOf("Exercise", exercise, "Duration (min)", duration, "Calories burned", calories);

        showForm(w == null ? "Add workout" : "Edit workout", form, () -> {
            int d = Integer.parseInt(duration.getText().trim());
            double c = Double.parseDouble(calories.getText().trim());
            if (w == null) {
                Workout n = new Workout(Main.workouts.nextId(), m.getId(), text(exercise), d, c, Main.today());
                Main.workouts.append(n.toFileString());
            } else {
                w.setExerciseName(text(exercise));
                w.setDuration(d);
                w.setCaloriesBurned(c);
                Main.workouts.update(w.getId(), w.toFileString());
            }
        });
    }

    // ---------- Fitness goals ----------
    private JPanel goalsPanel(Member m) {
        return this.<FitnessGoal>crudPanel("Add goal",
                new String[]{"ID", "Goal type", "Target value", "Status"},
                () -> Main.loadGoals(m.getId()),
                g -> new Object[]{g.getId(), g.getGoalType(), g.getTargetValue(), g.getStatus()},
                () -> goalForm(m, null),
                g -> goalForm(m, g),
                g -> Main.goals.delete(g.getId()),
                null, null);
    }

    private void goalForm(Member m, FitnessGoal g) {
        JTextField type = new JTextField(g == null ? "" : g.getGoalType(), 18);
        JTextField target = new JTextField(g == null ? "" : String.valueOf(g.getTargetValue()), 18);
        JComboBox<String> status = new JComboBox<>(new String[]{"In Progress", "Achieved"});
        if (g != null) {
            status.setSelectedItem(g.getStatus());
        }
        JPanel form = formOf("Goal type (e.g. Target Weight)", type, "Target value", target, "Status", status);

        showForm(g == null ? "Add goal" : "Edit goal", form, () -> {
            double t = Double.parseDouble(target.getText().trim());
            String s = (String) status.getSelectedItem();
            if (g == null) {
                FitnessGoal n = new FitnessGoal(Main.goals.nextId(), m.getId(), text(type), t, s);
                Main.goals.append(n.toFileString());
            } else {
                g.setGoalType(text(type));
                g.setTargetValue(t);
                g.setStatus(s);
                Main.goals.update(g.getId(), g.toFileString());
            }
        });
    }

    // ---------- Body measurements ----------
    private JPanel measurementsPanel(Member m) {
        return this.<BodyMeasurement>crudPanel("Add measurement",
                new String[]{"ID", "Date", "Weight (kg)", "Height (cm)", "BMI", "Category"},
                () -> Main.loadMeasures(m.getId()),
                b -> new Object[]{b.getId(), b.getDate(), b.getWeight(), b.getHeight(),
                        b.getBmi(), bmiCategory(b.getBmi())},
                () -> measurementForm(m, null),
                b -> measurementForm(m, b),
                b -> Main.measures.delete(b.getId()),
                null, null);
    }

    private void measurementForm(Member m, BodyMeasurement b) {
        JTextField weight = new JTextField(String.valueOf(b == null ? m.getWeight() : b.getWeight()), 18);
        JTextField height = new JTextField(String.valueOf(b == null ? m.getHeight() : b.getHeight()), 18);
        JPanel form = formOf("Weight (kg)", weight, "Height (cm)", height);

        double[] bmi = {0};
        boolean ok = showForm(b == null ? "Add measurement" : "Edit measurement", form, () -> {
            double w = Double.parseDouble(weight.getText().trim());
            double h = Double.parseDouble(height.getText().trim());
            if (b == null) {
                BodyMeasurement n = new BodyMeasurement(Main.measures.nextId(), m.getId(), Main.today(), w, h);
                Main.measures.append(n.toFileString());
                bmi[0] = n.getBmi();
            } else {
                b.setWeight(w);
                b.setHeight(h);
                Main.measures.update(b.getId(), b.toFileString());
                bmi[0] = b.getBmi();
            }
        });
        if (ok) {
            info("Saved. BMI = " + bmi[0] + " (" + bmiCategory(bmi[0]) + ")");
        }
    }

    private static String bmiCategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25) {
            return "Normal";
        } else if (bmi < 30) {
            return "Overweight";
        }
        return "Obese";
    }

    // ---------- Reports ----------
    private JPanel reportsPanel(Member m) {
        JLabel live = new JLabel();
        live.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));
        return this.<FitnessReport>crudPanel("Generate & save report",
                new String[]{"ID", "Date", "Summary"},
                () -> Main.loadReports(m.getId()),
                r -> new Object[]{r.getId(), r.getDate(), r.getProgressSummary()},
                () -> {
                    FitnessReport r = new FitnessReport(Main.reports.nextId(), m.getId(),
                            Main.today(), Main.buildSummary(m.getId()));
                    Main.reports.append(r.toFileString());
                    info("Report saved:\n" + r.getProgressSummary());
                },
                null,
                r -> Main.reports.delete(r.getId()),
                live,
                () -> live.setText("<html><b>Current progress:</b> " + Main.buildSummary(m.getId()) + "</html>"));
    }

    // =====================================================================
    // Trainer tabs
    // =====================================================================
    private JPanel clientsPanel(Trainer t) {
        DefaultTableModel model = tableModel("ID", "Name", "Email", "Age", "Weight (kg)", "Height (cm)", "Progress");
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(6).setPreferredWidth(360);

        Runnable load = () -> {
            model.setRowCount(0);
            for (Member c : Main.clientsOf(t)) {
                model.addRow(new Object[]{c.getId(), c.getName(), c.getEmail(), c.getAge(),
                        c.getWeight(), c.getHeight(), Main.buildSummary(c.getId())});
            }
        };
        load.run();

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load.run());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Members who chose you as their trainer:"));
        top.add(refresh);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel clientReportsPanel(Trainer t) {
        List<Member> clients = new ArrayList<>();
        JComboBox<String> combo = new JComboBox<>();
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(10, 10, 10, 10));

        Runnable show = () -> {
            int i = combo.getSelectedIndex();
            if (i < 0 || i >= clients.size()) {
                area.setText("You have no clients yet.");
                return;
            }
            Member c = clients.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("Current progress for ").append(c.getName()).append(":\n")
                    .append(Main.buildSummary(c.getId())).append("\n\nSaved reports:\n");
            List<FitnessReport> saved = Main.loadReports(c.getId());
            if (saved.isEmpty()) {
                sb.append("(nothing yet)");
            }
            for (FitnessReport r : saved) {
                sb.append(r).append("\n");
            }
            area.setText(sb.toString());
            area.setCaretPosition(0);
        };
        Runnable load = () -> fillClientCombo(t, clients, combo);
        combo.addActionListener(e -> show.run());
        load.run();
        show.run();

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> {
            load.run();
            show.run();
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Client:"));
        top.add(combo);
        top.add(refresh);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private JPanel planPanel(Trainer t) {
        List<Member> clients = new ArrayList<>();
        JComboBox<String> combo = new JComboBox<>();
        DefaultTableModel model = tableModel("ID", "Exercise", "Duration (min)", "Calories", "Date");
        JTable table = new JTable(model);
        table.setRowHeight(24);

        Runnable loadTable = () -> {
            model.setRowCount(0);
            int i = combo.getSelectedIndex();
            if (i < 0 || i >= clients.size()) {
                return;
            }
            for (Workout w : Main.loadWorkouts(clients.get(i).getId())) {
                model.addRow(new Object[]{w.getId(), w.getExerciseName(), w.getDuration(),
                        w.getCaloriesBurned(), w.getDate()});
            }
        };
        combo.addActionListener(e -> loadTable.run());
        fillClientCombo(t, clients, combo);
        loadTable.run();

        JTextField exercise = new JTextField(14);
        JTextField duration = new JTextField(6);
        JTextField calories = new JTextField(6);
        JButton add = new JButton("Add plan");
        add.addActionListener(e -> {
            int i = combo.getSelectedIndex();
            if (i < 0 || i >= clients.size()) {
                error("You have no clients yet.");
                return;
            }
            try {
                Member c = clients.get(i);
                Workout w = new Workout(Main.workouts.nextId(), c.getId(), "[Plan] " + text(exercise),
                        Integer.parseInt(duration.getText().trim()),
                        Double.parseDouble(calories.getText().trim()), Main.today());
                Main.workouts.append(w.toFileString());
                exercise.setText("");
                duration.setText("");
                calories.setText("");
                loadTable.run();
                info("Plan added to " + c.getName() + "'s workouts.");
            } catch (NumberFormatException ex) {
                error("Duration and calories must be numbers.");
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        });
        JButton refresh = new JButton("Refresh clients");
        refresh.addActionListener(e -> {
            fillClientCombo(t, clients, combo);
            loadTable.run();
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Client:"));
        top.add(combo);
        top.add(refresh);
        JPanel formRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formRow.add(new JLabel("Exercise:"));
        formRow.add(exercise);
        formRow.add(new JLabel("Minutes:"));
        formRow.add(duration);
        formRow.add(new JLabel("Target calories:"));
        formRow.add(calories);
        formRow.add(add);
        JPanel north = new JPanel(new GridLayout(2, 1));
        north.add(top);
        north.add(formRow);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void fillClientCombo(Trainer t, List<Member> clients, JComboBox<String> combo) {
        clients.clear();
        combo.removeAllItems();
        for (Member c : Main.clientsOf(t)) {
            clients.add(c); // add to the list first so the combo's listener always finds it
            combo.addItem(c.getId() + " - " + c.getName());
        }
    }

    // =====================================================================
    // Admin tab
    // =====================================================================
    private JPanel usersPanel(Admin admin) {
        DefaultTableModel model = tableModel("ID", "Role", "Name", "Email");
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextField search = new JTextField(18);
        Runnable load = () -> {
            model.setRowCount(0);
            String key = search.getText().trim().toLowerCase();
            for (String line : Main.users.readAll()) {
                User x = User.fromFileString(line);
                String all = (x.getId() + " " + x.getRole() + " " + x.getName() + " " + x.getEmail()).toLowerCase();
                if (key.isEmpty() || all.contains(key)) {
                    model.addRow(new Object[]{x.getId(), x.getRole(), x.getName(), x.getEmail()});
                }
            }
        };
        load.run();

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> load.run());
        search.addActionListener(e -> load.run());
        JButton showAll = new JButton("Show all");
        showAll.addActionListener(e -> {
            search.setText("");
            load.run();
        });

        JButton edit = new JButton("Edit selected");
        edit.addActionListener(e -> {
            int id = selectedId(table, model);
            if (id < 0) {
                error("Select a user in the table first.");
                return;
            }
            if (id == admin.getId()) {
                info("To edit your own account, use the Profile tab.");
                return;
            }
            User target = Main.findById(id);
            if (target == null) {
                load.run();
                return;
            }
            JDialog dialog = new JDialog(this, "Edit user " + id, true);
            dialog.add(profilePanel(target, () -> {
                dialog.dispose();
                load.run();
            }));
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        JButton delete = new JButton("Delete selected");
        delete.addActionListener(e -> {
            int id = selectedId(table, model);
            if (id < 0) {
                error("Select a user in the table first.");
                return;
            }
            User target = Main.findById(id);
            if (target == null) {
                load.run();
                return;
            }
            if (target instanceof Admin) {
                error("Admin accounts cannot be deleted.");
                return;
            }
            int answer = JOptionPane.showConfirmDialog(this,
                    "Delete " + target.getName() + " (ID " + id + ")? This cannot be undone.",
                    "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                deleteUser(target);
                load.run();
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(search);
        top.add(searchBtn);
        top.add(showAll);
        top.add(Box.createHorizontalStrut(20));
        top.add(edit);
        top.add(delete);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // Same rules as the console version: a member's records go with the member (composition);
    // a deleted trainer's clients stay but lose their trainer (aggregation).
    private void deleteUser(User u) {
        int id = u.getId();
        Main.users.delete(id);
        if (u instanceof Member) {
            Main.workouts.deleteWhere(1, "" + id);
            Main.goals.deleteWhere(1, "" + id);
            Main.measures.deleteWhere(1, "" + id);
            Main.reports.deleteWhere(1, "" + id);
        } else {
            for (String line : Main.users.readAll()) {
                User x = User.fromFileString(line);
                if (x instanceof Member mm && mm.getTrainerID() == id) {
                    mm.setTrainerID(0);
                    Main.users.update(mm.getId(), mm.toFileString());
                }
            }
        }
    }

    // =====================================================================
    // Reusable pieces
    // =====================================================================

    /**
     * A table with Add / Edit / Delete buttons. onEdit may be null (no Edit button).
     * south and afterLoad are optional extras (used by the Reports tab).
     */
    private <T extends Storable> JPanel crudPanel(String addLabel, String[] columns,
                                                  Supplier<List<T>> loader,
                                                  Function<T, Object[]> rowMapper,
                                                  Runnable onAdd, Consumer<T> onEdit, Consumer<T> onDelete,
                                                  JComponent south, Runnable afterLoad) {
        DefaultTableModel model = tableModel(columns);
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < columns.length; i++) {
            // keep ID and Date narrow so long text (like a report summary) gets the space
            if (columns[i].equals("ID") || columns[i].equals("Date")) {
                int w = columns[i].equals("ID") ? 60 : 110;
                table.getColumnModel().getColumn(i).setMaxWidth(w);
                table.getColumnModel().getColumn(i).setPreferredWidth(w);
            }
        }
        List<T> rows = new ArrayList<>();

        Runnable refresh = () -> {
            rows.clear();
            rows.addAll(loader.get());
            model.setRowCount(0);
            for (T item : rows) {
                model.addRow(rowMapper.apply(item));
            }
            if (afterLoad != null) {
                afterLoad.run();
            }
        };
        refresh.run();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton(addLabel);
        add.setFont(add.getFont().deriveFont(Font.BOLD));
        add.addActionListener(e -> {
            onAdd.run();
            refresh.run();
        });
        top.add(add);

        if (onEdit != null) {
            JButton edit = new JButton("Edit selected");
            edit.addActionListener(e -> {
                int i = table.getSelectedRow();
                if (i < 0) {
                    error("Select a row in the table first.");
                    return;
                }
                onEdit.accept(rows.get(i));
                refresh.run();
            });
            top.add(edit);
        }

        JButton delete = new JButton("Delete selected");
        delete.addActionListener(e -> {
            int i = table.getSelectedRow();
            if (i < 0) {
                error("Select a row in the table first.");
                return;
            }
            int answer = JOptionPane.showConfirmDialog(this, "Delete the selected record?",
                    "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                onDelete.accept(rows.get(i));
                refresh.run();
            }
        });
        top.add(delete);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        if (south != null) {
            panel.add(south, BorderLayout.SOUTH);
        }
        return panel;
    }

    /** Shows a form in a dialog; keeps it open with the typed values until it saves or is cancelled. */
    private boolean showForm(String title, JComponent form, Runnable saver) {
        while (true) {
            int result = JOptionPane.showConfirmDialog(this, form, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return false;
            }
            try {
                saver.run();
                return true;
            } catch (NumberFormatException ex) {
                error("Please enter valid numbers in the number fields.");
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        }
    }

    /** formOf("Label", field, "Label", field, ...) builds a neat two-column form. */
    private static JPanel formOf(Object... labelsAndFields) {
        JPanel form = new JPanel(new GridBagLayout());
        for (int i = 0; i + 1 < labelsAndFields.length; i += 2) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = i / 2;
            c.insets = new Insets(5, 5, 5, 8);
            c.anchor = GridBagConstraints.WEST;
            c.gridx = 0;
            form.add(new JLabel((String) labelsAndFields[i]), c);
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            form.add((JComponent) labelsAndFields[i + 1], c);
        }
        return form;
    }

    private static String text(JTextField f) {
        return FileManager.clean(f.getText().trim());
    }

    private static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static int selectedId(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return -1;
        }
        return (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "FitLife", JOptionPane.ERROR_MESSAGE);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "FitLife", JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================================
    // Start the program
    // =====================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // falls back to the default look
        }
        Font base = new Font("Segoe UI", Font.PLAIN, 14);
        for (Object key : Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(base));
            }
        }

        Main.seedAdmin(); // makes sure the demo admin account exists
        SwingUtilities.invokeLater(() -> new FitLifeGUI().setVisible(true));
    }
}