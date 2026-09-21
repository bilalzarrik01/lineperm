package ma.youcode.lineperm.ui;

import ma.youcode.lineperm.models.User;
import ma.youcode.lineperm.services.UserService;
import ma.youcode.lineperm.services.FichierService;
import java.util.Scanner;

public class ConsoleApp {
    private final UserService userService;
    private final Scanner scanner;
    private User utilisateurConnecte = null;
    private boolean actif = true;
    private final FichierService fichierService = new FichierService();

    public ConsoleApp() {
        this.userService = new UserService();
        this.scanner = new Scanner(System.in);
    }

    public void demarrer() {
        fichierService.afficherPerm(null);
        fichierService.charger();
        userService.charger();
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
            case "touch":
                Createfile(mots);
                break;
            case "ls":
                fichierService.listerFichiers();
            case "showperm":
                if (mots.length < 2) {
                    System.out.println("Usage: showperm ");
                } else {
                    fichierService.afficherPerm(mots[1]);
                }
                break;

            case "cat":
                if (mots.length < 2)
                    System.out.println("Usage: cat <filename>");
                else
                    fichierService.lireFichier(mots[1], utilisateurConnecte.getLogin());
                break;

            case "nano":
                if (mots.length < 3)
                    System.out.println("Usage: nano <filename> <texte>");
                else {
                    StringBuilder texte = new StringBuilder();
                    for (int i = 2; i < mots.length; i++)
                        texte.append(mots[i]).append(" ");
                    fichierService.ecrireFichier(mots[1], utilisateurConnecte.getLogin(), texte.toString().trim());
                }
                break;

            case "rm":
                if (utilisateurConnecte == null) {
                    System.out.println("Erreur: Vous devez etre connecte pour supprimer un fichier.");
                } else if (mots.length < 2) {
                    System.out.println("Usage: rm ");
                } else {
                    if (fichierService.supprimerFichier(mots[1], utilisateurConnecte.getLogin())) {
                        System.out.println("Fichier supprime avec succes.");
                    }
                }
                break;
            case "chmod":
                if (utilisateurConnecte == null) {
                    System.out.println("Erreur: Vous devez etre connecte.");
                } else if (mots.length < 5) {
                    System.out.println("Usage: chmod    ");
                } else {
                    boolean r = Boolean.parseBoolean(mots[2]);
                    boolean w = Boolean.parseBoolean(mots[3]);
                    boolean d = Boolean.parseBoolean(mots[4]);
                    fichierService.modifierPerm(mots[1], utilisateurConnecte.getLogin(), r, w, d);
                }
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

    public void Createfile(String[] mots) {
        if (utilisateurConnecte == null) {
            System.out.println("vous deuvez etre connecter pour creer un fichier ");
            return;
        }
        if (mots.length < 2) {
            System.out.println("vous deuvez nomee le fichier");
            return;
        }

        String nomeFichier = mots[1];
        if (fichierService.creerFichier(nomeFichier, utilisateurConnecte.getLogin())) {
            System.out.println("le fichier " + nomeFichier + "est bien creer");
        } else {
            System.out.println("cette nome deja existe");
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