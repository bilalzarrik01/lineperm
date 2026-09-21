package ma.youcode.lineperm.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class AccessLog {
    private LocalDate date;
    private LocalTime heure;
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

        String[] parts = line.split(";");
        if (parts.length < 6) return null;

        try {
            LocalDate date = LocalDate.parse(parts[0].trim());
            LocalTime heure = LocalTime.parse(parts[1].trim());
            String utilisateur = parts[2].trim();
            String action = parts[3].trim();
            String fichier = parts[4].trim();
            String resultat = parts[5].trim();

            return new AccessLog(date, heure, utilisateur, action, fichier, resultat);
        } catch (Exception e) {
            return null;
        }
    }

    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public String getUtilisateur() { return utilisateur; }
    public String getAction() { return action; }
    public String getFichier() { return fichier; }
    public String getResultat() { return resultat; }

    @Override
    public String toString() {
        return date + " " + heure + " | " + utilisateur + " | " + action + " | " + fichier + " | " + resultat;
    }
}