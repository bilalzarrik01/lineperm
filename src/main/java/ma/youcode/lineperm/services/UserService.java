package ma.youcode.lineperm.services;

import ma.youcode.lineperm.dao.UserDao;
import ma.youcode.lineperm.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;


public class UserService {
    private final UserDao userDao;

    public UserService() {
        try {
            this.userDao = new UserDao();
        } catch (SQLException e) {
            throw new IllegalStateException("Connexion a la base impossible.", e);
        }
    }

    /** Conserved pour compatibilite: les utilisateurs sont lus a la demande via le DAO. */
    public void charger() {
        // Aucun chargement en memoire n'est necessaire avec SQLite.
    }

    public boolean existe(String login) {
        try {
            return userDao.trouverParLogin(login).isPresent();
        } catch (SQLException e) {
            System.err.println("Erreur de recherche utilisateur : " + e.getMessage());
            return true;
        }
    }

    public boolean creerCompte(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null || motDePasse.isEmpty() || existe(login)) {
            return false;
        }

        String hash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
        try {
            userDao.ajouter(new User(login.trim(), hash));
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur de creation du compte : " + e.getMessage());
            return false;
        }
    }

    public User connecter(String login, String motDePasse) {
        if (login == null || motDePasse == null) {
            return null;
        }

        try {
            Optional<User> resultat = userDao.trouverParLogin(login.trim());
            if (resultat.isEmpty()) {
                return null;
            }

            User user = resultat.get();
            String passwordEnregistre = user.getPasswordHash();
            if (estHashBCrypt(passwordEnregistre)) {
                return BCrypt.checkpw(motDePasse, passwordEnregistre) ? user : null;
            }

            if (passwordEnregistre.equals(motDePasse)) {
                String nouveauHash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
                if (userDao.modifierMotDePasse(user.getId(), nouveauHash)) {
                    user.setPasswordHash(nouveauHash);
                    return user;
                }
            }
            return null;
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Erreur de connexion utilisateur : " + e.getMessage());
            return null;
        }
    }

    private boolean estHashBCrypt(String valeur) {
        return valeur != null && valeur.matches("\\$2[aby]\\$\\d{2}\\$.*");
    }
}
