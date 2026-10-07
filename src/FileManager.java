import java.io.*;
import java.util.*;

/** Hides Java File I/O. Each record is one line: id|field|field|... */
public class FileManager {
    private String filePath;

    public FileManager(String filePath) {
        this.filePath = filePath;
        try {
            File f = new File(filePath);
            if (f.getParentFile() != null) f.getParentFile().mkdirs();
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("Cannot create file: " + filePath);
        }
    }

    public List<String> readAll() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String l;
            while ((l = br.readLine()) != null) if (!l.trim().isEmpty()) lines.add(l);
        } catch (IOException e) {
            System.out.println("Read error: " + e.getMessage());
        }
        return lines;
    }

    public void append(String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Write error: " + e.getMessage());
        }
    }

    private void writeAll(List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        } catch (IOException e) {
            System.out.println("Write error: " + e.getMessage());
        }
    }

    public void update(int id, String newLine) {
        List<String> lines = readAll();
        for (int i = 0; i < lines.size(); i++)
            if (idOf(lines.get(i)) == id) lines.set(i, newLine);
        writeAll(lines);
    }

    public void delete(int id) {
        List<String> lines = readAll();
        lines.removeIf(l -> idOf(l) == id);
        writeAll(lines);
    }

    /** Deletes every line whose field at fieldIndex equals value (used for cascade deletes). */
    public void deleteWhere(int fieldIndex, String value) {
        List<String> lines = readAll();
        lines.removeIf(l -> {
            String[] p = l.split("\\|", -1);
            return p.length > fieldIndex && p[fieldIndex].equals(value);
        });
        writeAll(lines);
    }

    public int nextId() {
        int max = 0;
        for (String l : readAll()) max = Math.max(max, idOf(l));
        return max + 1;
    }

    private static int idOf(String line) {
        return Integer.parseInt(line.split("\\|", 2)[0]);
    }

    /** Removes the delimiter from user input so it cannot corrupt a record. */
    public static String clean(String s) {
        return s.replace("|", "/");
    }
}
