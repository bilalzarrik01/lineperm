package ma.youcode.lineperm.services;

import ma.youcode.lineperm.models.AccessLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class LogAnalyzerService {
    private List<AccessLog> logs = new ArrayList<>();
    private final Path logFilePath = Paths.get("resources/access.log");

    public void chargerLogs() {
        if (!Files.exists(logFilePath)) {
            return;
        }

        try {
            logs = Files.lines(logFilePath)
                    .filter(line -> !line.isBlank())
                    .map(AccessLog::fromCsvLine)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            System.out.println("Erreur chargement logs : " + e.getMessage());
        }
    }

    public long getTotalActions() {
        return logs.stream().count();
    }

    public long getNombreAccesRefuses() {
        return logs.stream()
                .filter(log -> "REFUSE".equalsIgnoreCase(log.getResultat()))
                .count();
    }

    public List<String> getUtilisateursDistincts() {
        return logs.stream()
                .map(AccessLog::getUtilisateur)
                .distinct()
                .toList();
    }

    public Map<String, Long> getActionsParUtilisateur() {
        return logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getUtilisateur, Collectors.counting()));
    }

    public List<Map.Entry<String, Long>> getTop3FichiersConsultes() {
        return logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getFichier, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .toList();
    }

    public List<AccessLog> getAccesRefusesParUtilisateur(String username) {
        return logs.stream()
                .filter(log -> log.getUtilisateur().equalsIgnoreCase(username))
                .filter(log -> "REFUSE".equalsIgnoreCase(log.getResultat()))
                .toList();
    }

    public Optional<Map.Entry<String, Long>> getUtilisateurLePlusActif() {
        return logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getUtilisateur, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    public Map<String, Long> getRepartitionActionsParType() {
        return logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getAction, Collectors.counting()));
    }
}