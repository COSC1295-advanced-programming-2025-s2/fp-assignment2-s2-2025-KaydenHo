package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import model.Project;

/**
 * Expects a headered CSV at /data/projects.csv with columns:
 * title,location,day,hourly_value,total_slots,registered_slots
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
        	// handle UTF-8 BOM if present
        	header = header.replace("\uFEFF", "").trim();

            List<Project> out = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] t = line.split(",", -1);
                String title       = col(t, 0);
                String location    = col(t, 1);
                String day         = col(t, 2);
                double hourly      = parseDouble(col(t, 3), 0);
                int totalSlots     = parseInt(col(t, 4), 0);
                int registered     = parseInt(col(t, 5), 0);
                out.add(new Project(title, location, day, hourly, totalSlots, registered));
            }
            return out;
        }
    }

    private static String col(String[] arr, int idx) { 
    	return idx < arr.length ? arr[idx].trim() : ""; }
    
    private static int parseInt(String s, int d) { try 
    { 
    	return Integer.parseInt(s); } 
    catch (Exception e) { 
    	return d; } }
    
    private static double parseDouble(String s, double d) { try 
    { return Double.parseDouble(s); } 
    catch (Exception e) { 
    	return d; } }
}
