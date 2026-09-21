package ma.youcode.lineperm.services;

import ma.youcode.lineperm.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;



public class UserService {
    private final String filePath = "resources/users.txt";
    private final Map<String, User> comptes = new HashMap<>();

    public boolean existe(String login) {
        return comptes.containsKey(login);
    }
public void charger(){
    comptes.clear();
    File file = new File(filePath);
    if (!file.exists()) {
        return ;     
    }
   try {
    List<String> lines =  Files.readAllLines(Paths.get(filePath));
    for (String  line : lines) {
        if (line.trim().isEmpty()) {
           continue;
        }
        String[] parts = line.split(":" ,2);
        if (parts.length == 2) {
            comptes.put(parts[0] , new User(parts[0], parts[1]));
            
        }
        
    }
   }catch (IOException e){
    System.err.println("Erreur de chargement : "+ e.getMessage());
   }

}
    private boolean sauvegarder(){
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (User u : comptes.values()) {
                writer.println(u.getLogin()  +":" +u.getPasswordHash());
            }
             return true ;
            
         } catch (IOException e) {
            System.err.println("ERR de sauvegarde est : "+e.getMessage());
            return false ;
        }
    }


    public boolean creerCompte(String login, String motDePasse) {
        if (existe(login)) {
            return false;
        }

        String hash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
        
        comptes.put(login, new User(login, hash));
        return sauvegarder();

    }

    public User connecter(String login, String motDePasse) {
        User user = comptes.get(login);
        if (user == null) {
            return null;
        }

        if (BCrypt.checkpw(motDePasse, user.getPasswordHash())) {
            return user;
        }

        return null;
    }
}