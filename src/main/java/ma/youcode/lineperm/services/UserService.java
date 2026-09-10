package ma.youcode.lineperm.services;

import ma.youcode.lineperm.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private final Map<String, User> comptes = new HashMap<>();

    public boolean existe(String login) {
        return comptes.containsKey(login);
    }

    public boolean creerCompte(String login, String motDePasse) {
        if (existe(login)) {
            return false;
        }

        String hash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
        
        comptes.put(login, new User(login, hash));
        return true;
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