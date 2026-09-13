package ma.youcode.lineperm.services;

import ma.youcode.lineperm.models.Fichier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FichierService {
   private Map<String, Fichier> fichiers = new HashMap<>();
    private final String dossier = "resources/text";

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
    public void charger(){
        fichiers.clear();
        File dir  = new File(dossier);
        if (!dir.exists() || !dir.isDirectory()) {
           return ; 
        }
        File[] list = dir.listFiles();
        if (list != null) {
            for (File file : list) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    fichiers.put(fileName, new Fichier(fileName , null));
                    
                }
                
            }
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
            System.out.println("- " + f.getName() );
        }
    }
}