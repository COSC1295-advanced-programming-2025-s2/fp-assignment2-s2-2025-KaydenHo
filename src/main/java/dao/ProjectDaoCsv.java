package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import model.Project;

/**
 * Flexible CSV/TSV loader for Assignment CSV.
 * - Auto-detects delimiter: comma, semicolon, or tab
 * - Accepts headers in any order and with assignment wording:
 *   "PROJECT TITLE", "LOCATION", "DAY", "HOURLY VALUE (AUD)",
 *   "#REGISTERED SLOTS", "#TOTAL SLOTS"
 */
public class ProjectDaoCsv implements ProjectDao {
    private final String resourcePath;

    public ProjectDaoCsv(String resourcePath) { // e.g. "/data/projects.csv"
        this.resourcePath = resourcePath;
    }

    @Override
    public List<Project> loadAll() throws IOException {
        InputStream is = ProjectDaoCsv.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IOException("CSV not found on classpath: " + resourcePath);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) return List.of();

            // Handle UTF-8 BOM and normalize
            header = header.replace("\uFEFF", "").trim();

            // Detect delimiter from header
            char delim = detectDelimiter(header);

            String[] headerCells = splitSmart(header, delim);
            Map<String, Integer> idx = indexByName(headerCells);

            // Map required logical fields -> actual column index
            Integer iTitle = require(idx, "title");
            Integer iLoc   = require(idx, "location");
            Integer iDay   = require(idx, "day");
            Integer iHourly= require(idx, "hourly_value");
            Integer iTotal = require(idx, "total_slots");
            Integer iReg   = require(idx, "registered_slots");

            List<Project> out = new ArrayList<>();
            String line;
            int row = 1;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;

                String[] t = splitSmart(line, delim);

                String title    = cell(t, iTitle);
                String location = cell(t, iLoc);
                String day      = cell(t, iDay);
                double hourly   = parseDouble(cell(t, iHourly), 0);
                int total       = parseInt(cell(t, iTotal), 0);
                int registered  = parseInt(cell(t, iReg), 0);

                // Safety: if CSV had total/registered swapped, correct it
                if (registered > total && total > 0) {
                    int tmp = total; total = registered; registered = tmp;
                }

                out.add(new Project(title, location, day, hourly, total, registered));
            }

            System.out.printf("Loaded %d projects from %s (delim='%s')%n",
                    out.size(), resourcePath, delimInfo(delim));
            return out;
        }
    }

    // --- helpers ---

    private static char detectDelimiter(String header) {
        // Count likely delimiters
        int cCom = count(header, ',');
        int cTab = count(header, '\t');
        int cSem = count(header, ';');
        if (cTab >= cCom && cTab >= cSem) return '\t';
        if (cCom >= cSem) return ',';
        return ';';
    }

    private static int count(String s, char ch) {
        int n = 0; for (int i = 0; i < s.length(); i++) if (s.charAt(i) == ch) n++; return n;
    }

    private static String[] splitSmart(String line, char delim) {
        // Simple split that also trims surrounding quotes/spaces from each cell
        String[] raw = line.split("\\Q" + delim + "\\E", -1);
        String[] out = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            out[i] = unquote(raw[i].trim());
        }
        return out;
    }

    private static String unquote(String s) {
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) ||
                                (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    private static Map<String, Integer> indexByName(String[] headersRaw) {
        Map<String, Integer> out = new HashMap<>();
        for (int i = 0; i < headersRaw.length; i++) {
            String h = normalizeHeader(headersRaw[i]);
            // Map multiple header aliases to canonical keys
            switch (h) {
                case "title", "project title" -> out.put("title", i);
                case "location" -> out.put("location", i);
                case "day" -> out.put("day", i);
                case "hourly_value", "hourly value (aud)", "hourly value" -> out.put("hourly_value", i);
                case "total_slots", "#total slots", "total slots" -> out.put("total_slots", i);
                case "registered_slots", "#registered slots", "registered slots" -> out.put("registered_slots", i);
                default -> { /* ignore others */ }
            }
        }
        return out;
    }

    private static String normalizeHeader(String h) {
        h = h == null ? "" : h;
        h = h.replace("\u00A0", " "); // NBSP -> space
        h = h.trim().toLowerCase(Locale.ROOT);
        h = h.replaceAll("\\s+", " ");
        return h;
    }

    private static Integer require(Map<String, Integer> idx, String key) throws IOException {
        Integer i = idx.get(key);
        if (i == null || i < 0) throw new IOException("CSV missing required column: " + key);
        return i;
    }

    private static String cell(String[] arr, int i) {
        return (i >= 0 && i < arr.length) ? arr[i].trim() : "";
    }

    private static int parseInt(String s, int d) {
        if (s == null || s.isBlank()) return d;
        s = s.replace("$", "").replace(",", "").trim();
        try { return Integer.parseInt(s); } catch (Exception e) { return d; }
    }

    private static double parseDouble(String s, double d) {
        if (s == null || s.isBlank()) return d;
        s = s.replace("$", "").replace(",", "").trim();
        try { return Double.parseDouble(s); } catch (Exception e) { return d; }
    }

    private static String delimInfo(char d) { return d == '\t' ? "\\t" : Character.toString(d); }
}
