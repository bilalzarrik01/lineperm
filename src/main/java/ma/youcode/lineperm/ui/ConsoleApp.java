package ma.youcode.lineperm.ui;

import ma.youcode.lineperm.models.User;
import ma.youcode.lineperm.services.UserService;

import java.util.Scanner;

public class ConsoleApp {
    private final UserService userService;
    private final Scanner scanner;
    private User utilisateurConnecte = null;
    private boolean actif = true;

    public ConsoleApp() {
        this.userService = new UserService();
        this.scanner = new Scanner(System.in);
    }

    public void demarrer() {
        afficherBanniere();

        while (actif) {
            String ligne = lireLigne(prompt());
            traiter(ligne);
        }
    }

    private String prompt() {
        if (utilisateurConnecte != null) {
            return utilisateurConnecte.getLogin() + "@linperm> ";
        }
        return "linperm> ";
    }

    private String lireLigne(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    public void traiter(String ligne) {
        String nettoyee = ligne.trim();
        if (nettoyee.isEmpty()) {
            return;
        }

        String[] mots = nettoyee.split("\\s+");
        String commande = mots[0].toLowerCase();

        
        if (utilisateurConnecte == null && commande.equals("logout")) {
            System.out.println("Vous devez etre connecte pour executer cette commande.");
            return;
        }

        if (utilisateurConnecte != null && (commande.equals("signup") || commande.equals("login"))) {
            System.out.println("Vous etes deja connecte.");
            return;
        }

        switch (commande) {
            case "signup":
                signup();
                break;
            case "login":
                login();
                break;
            case "logout":
                logout();
                break;
            case "exit":
                actif = false;
                break;
            default:
                System.out.println("Commande inconnue.");
                break;
        }
    }

    private void signup() {
        String login = lireLigne("Login : ").trim();
        if (login.isEmpty()) {
            System.out.println("Login invalide.");
            return;
        }

        if (userService.existe(login)) {
            System.out.println("Ce login existe deja.");
            return;
        }

        String pass = lireLigne("Mot de passe : ");
        if (pass.isEmpty()) {
            System.out.println("Le mot de passe ne peut pas etre vide.");
            return;
        }

        if (userService.creerCompte(login, pass)) {
            System.out.println("Compte cree avec succes.");
        } else {
            System.out.println("Erreur lors de la creation du compte.");
        }
    }

    private void login() {
        String login = lireLigne("Login : ");
        String pass = lireLigne("Mot de passe : ");

        User user = userService.connecter(login, pass);
        if (user != null) {
            utilisateurConnecte = user;
            System.out.println("Bienvenue " + user.getLogin() + " !");
        } else {
            System.out.println("Identifiants incorrects.");
        }
    }

    private void logout() {
        utilisateurConnecte = null;
        System.out.println("Deconnecte.");
    }

    private void afficherBanniere() {
        System.out.println("=================================");
        System.out.println("     Bienvenue sur LinePerm      ");
        System.out.println("=================================");
    }
}