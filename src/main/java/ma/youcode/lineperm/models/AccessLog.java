package ma.youcode.lineperm.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class AccessLog {
    private int id;
    private LocalDate date;
    private LocalTime heure;
    private int userId;
    private int fileId;
    private String utilisateur;
    private String action;
    private String fichier;
    private String resultat;

    public AccessLog(LocalDate date, LocalTime heure, String utilisateur, String action, String fichier, String resultat) {
        this.date = date;
        this.heure = heure;
        this.utilisateur = utilisateur;
        this.action = action;
        this.fichier = fichier;
        this.resultat = resultat;
    }

    public AccessLog(String utilisateur, String action, String fichier, String resultat) {
        this(LocalDate.now(), LocalTime.now(), utilisateur, action, fichier, resultat);
    }

    public String toCsvLine() {
        return date + ";" + heure + ";" + utilisateur + ";" + action + ";" + fichier + ";" + resultat;
    }

    public static AccessLog fromCsvLine(String line) {
        if (line == null || line.isBlank()) return null;

        String[] parts = line.split(";", -1);
        if (parts.length < 6) return null;

        try {
            if (parts[0].trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
                return new AccessLog(
                        LocalDate.parse(parts[0].trim()),
                        LocalTime.parse(parts[1].trim()),
                        parts[2].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim());
            }

            if (parts[0].trim().matches("\\d{2}:\\d{2}:.*")) {
                return new AccessLog(LocalDate.now(), LocalTime.parse(parts[0].trim()),
                        parts[1].trim(), parts[2].trim(), parts[3].trim(), normaliserResultat(parts[4]));
            }

            LocalDate date = LocalDate.parse(parts[0].trim());
            return new AccessLog(date, LocalTime.parse(parts[1].trim()), parts[2].trim(),
                    parts[3].trim(), parts[4].trim(), normaliserResultat(parts[5]));
        } catch (Exception e) {
            return null;
        }
    }

    public AccessLog(int id, int userId, int fileId, LocalDate date, LocalTime heure,
            String utilisateur, String action, String fichier, String resultat) {
        this(date, heure, utilisateur, action, fichier, resultat);
        this.id = id;
        this.userId = userId;
        this.fileId = fileId;
    }

    private static String normaliserResultat(String resultat) {
        return "AUTORISE".equalsIgnoreCase(resultat.trim()) ? "OK" : resultat.trim();
    }

    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getFileId() { return fileId; }
    public void setFileId(int fileId) { this.fileId = fileId; }
    public String getUtilisateur() { return utilisateur; }
    public String getAction() { return action; }
    public String getFichier() { return fichier; }
    public String getResultat() { return resultat; }

    @Override
    public String toString() {
        return date + " " + heure + " | " + utilisateur + " | " + action + " | " + fichier + " | " + resultat;
    }
}
