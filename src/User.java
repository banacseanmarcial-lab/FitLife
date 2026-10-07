public abstract class User implements Storable {
    private int userID;
    private String name, email, password;

    public User(int userID, String name, String email, String password) {
        this.userID = userID;
        setName(name);
        setEmail(email);
        setPassword(password);
    }

    public boolean login(String email, String password) {
        return this.email.equalsIgnoreCase(email) && this.password.equals(password);
    }
    public void logout() { System.out.println(name + " logged out."); }

    public abstract void displayDashboard();
    public abstract String getRole();

    @Override public int getId() { return userID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty.");
        this.name = name;
    }
    public void setEmail(String email) {
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("Invalid email.");
        this.email = email;
    }
    public void setPassword(String password) {
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password cannot be empty.");
        this.password = password;
    }

    protected String baseString() {
        return userID + "|" + getRole() + "|" + name + "|" + email + "|" + password;
    }

    /** users.txt: id|ROLE|name|email|password|extra fields... */
    public static User fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        int id = Integer.parseInt(p[0]);
        switch (p[1]) {
            case "MEMBER":
                return new Member(id, p[2], p[3], p[4], Integer.parseInt(p[5]),
                        Double.parseDouble(p[6]), Double.parseDouble(p[7]), Integer.parseInt(p[8]));
            case "TRAINER":
                return new Trainer(id, p[2], p[3], p[4], p[5]);
            default:
                return new Admin(id, p[2], p[3], p[4], Integer.parseInt(p[5]));
        }
    }
}
