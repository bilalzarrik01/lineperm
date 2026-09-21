package ma.youcode.lineperm.services;

import ma.youcode.lineperm.models.AccessLog;
import ma.youcode.lineperm.models.Fichier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FichierService {
    private Map<String, Fichier> fichiers = new HashMap<>();
    private final String dossier = "resources/text";
    private final Path permFilePath = Paths.get("resources/perm.txt");
    private final LogAnalyzerService logAnalyzerService = new LogAnalyzerService();

    public FichierService() {
        try {
            Path path = Paths.get(dossier);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.out.println("Erreur dossier : " + e.getMessage());
        }
    }

    public boolean existe(String fileName) {
        return fichiers.containsKey(fileName);
    }

    public void charger() {
        try {
            if (!Files.exists(permFilePath)) {
                Files.createFile(permFilePath);
            }

            List<String> lines = Files.readAllLines(permFilePath);
            for (String line : lines) {
                if (line.isBlank())
                    continue;

                String[] parts = line.split(";");
                if (parts.length == 5) {
                    String nom = parts[0];
                    String owner = parts[1];
                    boolean read = Boolean.parseBoolean(parts[2]);
                    boolean write = Boolean.parseBoolean(parts[3]);
                    boolean delete = Boolean.parseBoolean(parts[4]);

                    Fichier f = new Fichier(nom, owner);
                    f.setOtherRead(read);
                    f.setOtherWrite(write);
                    f.setOtherDelete(delete);

                    fichiers.put(nom, f);
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement des permissions : " + e.getMessage());
        }
    }

    public boolean creerFichier(String fileName, String owner) {
        if (existe(fileName)) {
            return false;
        }

        try {
            Path filePath = Paths.get(dossier, fileName);
            Files.createFile(filePath);

            Fichier newFichier = new Fichier(fileName, owner);
            fichiers.put(fileName, newFichier);
            
            sauvegarderPermissions();

            // Enregistrer log
            logAnalyzerService.sauvegarderLog(new AccessLog(owner, "WRITE", fileName, "AUTORISE"));

            return true;
        } catch (IOException e) {
            System.out.println("Erreur de creation : " + e.getMessage());
            return false;
        }
    }

    public Fichier getFichier(String fileName) {
        return fichiers.get(fileName);
    }

    public Map<String, Fichier> getAllFichiers() {
        return fichiers;
    }

    public void listerFichiers() {
        if (fichiers.isEmpty()) {
            System.out.println("aucun fichier trouve ");
            return;
        }
        System.out.println("Fichiers disponibles : ");
        for (Fichier f : fichiers.values()) {
            System.out.println("- " + f.getName());
        }
    }

    public void afficherPerm(String fileName) {
        Fichier f = getFichier(fileName);
        if (f == null) {
            System.out.println("Fichier non trouve.");
            return;
        }
        System.out.println("--- Permissions de " + f.getName() + " ---");
        System.out.println("Proprietaire : " + f.getOwner());
        System.out.println("Owner  -> Read: " + f.isOwnerRead() + " | Write: " + f.isOwnerWrite() + " | Delete: "
                + f.isOwnerDelete());
        System.out.println("Others -> Read: " + f.isOtherRead() + " | Write: " + f.isOtherWrite() + " | Delete: "
                + f.isOtherDelete());
    }

    public boolean modifierPerm(String fileName, String userConnecte, boolean read, boolean write, boolean delete) {
        Fichier f = getFichier(fileName);
        if (f == null) {
            System.out.println("Erreur: Fichier non trouve.");
            return false;
        }

        if (!f.getOwner().equals(userConnecte)) {
            System.out.println("Permission refusee : Seul le proprietaire peut modifier les permissions.");
            logAnalyzerService.sauvegarderLog(new AccessLog(userConnecte, "CHMOD", fileName, "REFUSE"));
            return false;
        }

        f.setOtherRead(read);
        f.setOtherWrite(write);
        f.setOtherDelete(delete);
        
        sauvegarderPermissions();
        logAnalyzerService.sauvegarderLog(new AccessLog(userConnecte, "CHMOD", fileName, "AUTORISE"));
        
        System.out.println("Permissions meises a jour pour : " + fileName);
        return true;
    }

    public void sauvegarderPermissions() {
        try {
            List<String> lines = new ArrayList<>();
            for (Fichier f : fichiers.values()) {
                String line = f.getName() + ";" +
                        f.getOwner() + ";" +
                        f.isOtherRead() + ";" +
                        f.isOtherWrite() + ";" +
                        f.isOtherDelete();
                lines.add(line);
            }
            Files.write(permFilePath, lines);
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde des permissions : " + e.getMessage());
        }
    }

    public boolean supprimerFichier(String fileName, String userConnecte) {
        Fichier f = getFichier(fileName);
        if (f == null) {
            System.out.println("Fichier non trouve.");
            return false;
        }

        boolean estOwner = f.getOwner().equals(userConnecte);
        if (!estOwner && !f.isOtherDelete()) {
            System.out.println("Permission refusee : vous n'avez pas le droit de supprimer.");
            logAnalyzerService.sauvegarderLog(new AccessLog(userConnecte, "DELETE", fileName, "REFUSE"));
            return false;
        }

        try {
            Path path = Paths.get(dossier, fileName);
            Files.deleteIfExists(path);
            fichiers.remove(fileName);
            
            sauvegarderPermissions();
            logAnalyzerService.sauvegarderLog(new AccessLog(userConnecte, "DELETE", fileName, "AUTORISE"));
            
            return true;
        } catch (IOException e) {
            System.out.println("Erreur de suppression : " + e.getMessage());
            return false;
        }
    }

    public void lireFichier(String fileName, String userConnecte) {
        Fichier f = getFichier(fileName);
        if (f == null) {
            System.out.println("Erreur: Fichier non trouve.");
            return;
        }

        boolean estOwner = f.getOwner() != null && f.getOwner().equals(userConnecte);

        if (!estOwner && !f.isOtherRead()) {
            System.out.println("Permission refusee : vous n'avez pas le droit de lire ce fichier.");
            logAnalyzerService.sauvegarderLog(new AccessLog(userConnecte, "READ", fileName, "REFUSE"));
            return;
        }

        try {
            Path path = Paths.get(dossier, fileName);
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                System.out.println("--- Contenu de " + fileName + " ---");
                for (String line : lines) {
                    System.out.println(line);
                }
                logAnalyzerService.sauvegarderLog(new AccessLog(userConnecte, "READ", fileName, "AUTORISE"));
            } else {
                System.out.println("Le fichier est vide ou n'existe pas sur le disque.");
            }
        } catch (IOException e) {
            System.out.println("Erreur de lecture : " + e.getMessage());
        }
    }

    public void ecrireFichier(String fileName, String userConnecte, String texte) {
        Fichier f = getFichier(fileName);
        if (f == null) {
            System.out.println("Erreur: Fichier non trouve.");
            return;
        }

        boolean estOwner = f.getOwner() != null && f.getOwner().equals(userConnecte);

        if (!estOwner && !f.isOtherWrite()) {
            System.out.println("Permission refusee : vous n'avez pas le droit de modifier ce fichier.");
            logAnalyzerService.sauvegarderLog(new AccessLog(userConnecte, "WRITE", fileName, "REFUSE"));
            return;
        }

        try {
            Path path = Paths.get(dossier, fileName);
            Files.writeString(path, texte);
            System.out.println("Fichier " + fileName + " modifie avec succes.");
            logAnalyzerService.sauvegarderLog(new AccessLog(userConnecte, "WRITE", fileName, "AUTORISE"));
        } catch (IOException e) {
            System.out.println("Erreur d'ecriture : " + e.getMessage());
        }
    }
}