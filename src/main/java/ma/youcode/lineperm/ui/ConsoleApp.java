package ma.youcode.lineperm.ui;

import ma.youcode.lineperm.models.AccessLog;
import ma.youcode.lineperm.models.User;
import ma.youcode.lineperm.services.FichierService;
import ma.youcode.lineperm.services.LogAnalyzerService;
import ma.youcode.lineperm.services.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {
    private final UserService userService;
    private final Scanner scanner;
    private User utilisateurConnecte = null;
    private boolean actif = true;
    private final FichierService fichierService = new FichierService();
    private final LogAnalyzerService logAnalyzerService = new LogAnalyzerService();

    public ConsoleApp() {
        this.userService = new UserService();
        this.scanner = new Scanner(System.in);
    }

    public void demarrer() {
        logAnalyzerService.chargerLogs();
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
                break;
            case "showperm":
                if (mots.length < 2) {
                    System.out.println("Usage: showperm <filename>");
                } else {
                    fichierService.afficherPerm(mots[1]);
                }
                break;

            case "cat":
                if (mots.length < 2)
                    System.out.println("Usage: cat <filename>");
                else
                    fichierService.lireFichier(mots[1], utilisateurConnecte != null ? utilisateurConnecte.getLogin() : null);
                break;

            case "nano":
                if (mots.length < 3)
                    System.out.println("Usage: nano <filename> <texte>");
                else {
                    StringBuilder texte = new StringBuilder();
                    for (int i = 2; i < mots.length; i++)
                        texte.append(mots[i]).append(" ");
                    fichierService.ecrireFichier(mots[1], utilisateurConnecte != null ? utilisateurConnecte.getLogin() : null, texte.toString().trim());
                }
                break;

            case "rm":
                if (utilisateurConnecte == null) {
                    System.out.println("Erreur: Vous devez etre connecte pour supprimer un fichier.");
                } else if (mots.length < 2) {
                    System.out.println("Usage: rm <filename>");
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
                    System.out.println("Usage: chmod <filename> <read> <write> <delete>");
                } else {
                    boolean r = Boolean.parseBoolean(mots[2]);
                    boolean w = Boolean.parseBoolean(mots[3]);
                    boolean d = Boolean.parseBoolean(mots[4]);
                    fichierService.modifierPerm(mots[1], utilisateurConnecte.getLogin(), r, w, d);
                }
                break;

            case "stats":
                afficherMenuStats();
                break;

            default:
                System.out.println("Commande inconnue.");
                break;
        }
    }

    private void afficherMenuStats() {
        logAnalyzerService.chargerLogs();
        boolean retour = false;

        System.out.println("Bienvenue dans LogAnalyzer. Choisissez une statistique par son numero.");

        while (!retour) {
            System.out.println("\n=== LogAnalyzer ===");
            System.out.println("1) Nombre total d'actions");
            System.out.println("2) Nombre d'acces mefuses");
            System.out.println("3) Utilisateurs distincts");
            System.out.println("4) Actions par utilisateur");
            System.out.println("5) Top 3 des fichiers consultes");
            System.out.println("6) Acces mefuses d'un utilisateur");
            System.out.println("7) Utilisateur le plus actif");
            System.out.println("8) Repartition des actions par type");
            System.out.println("0) Quitter");
            System.out.print("Choix: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    System.out.println("Nombre total d'actions: " + logAnalyzerService.getTotalActions());
                    break;
                case "2":
                    System.out.println("Acces mefuses: " + logAnalyzerService.getNombreAccesRefuses());
                    break;
                case "3":
                    System.out.println("Utilisateurs distincts: " + logAnalyzerService.getUtilisateursDistincts());
                    break;
                case "4":
                    System.out.println("Actions par utilisateur: " + logAnalyzerService.getActionsParUtilisateur());
                    break;
                case "5":
                    System.out.println("Top 3 des fichiers consultes:");
                    logAnalyzerService.getTop3FichiersConsultes().forEach(e ->
                            System.out.println("- " + e.getKey() + " : " + e.getValue() + " acces")
                    );
                    break;
                case "6":
                    System.out.print("Entrez le nom d'utilisateur: ");
                    String user = scanner.nextLine().trim();
                    List<AccessLog> refus = logAnalyzerService.getAccesRefusesParUtilisateur(user);
                    if (refus.isEmpty()) {
                        System.out.println("Aucun acces mefuse trouve pour " + user);
                    } else {
                        refus.forEach(log -> System.out.println(log));
                    }
                    break;
                case "7":
                    Optional<Map.Entry<String, Long>> plusActif = logAnalyzerService.getUtilisateurLePlusActif();
                    if (plusActif.isPresent()) {
                        System.out.println("Utilisateur le plus actif: " + plusActif.get().getKey() + " (" + plusActif.get().getValue() + " actions)");
                    } else {
                        System.out.println("Aucune donnee disponible.");
                    }
                    break;
                case "8":
                    System.out.println("Repartition des actions par type: " + logAnalyzerService.getRepartitionActionsParType());
                    break;
                case "0":
                    retour = true;
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
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
            System.out.println("Vous devez etre connecte pour creer un fichier.");
            return;
        }
        if (mots.length < 2) {
            System.out.println("Vous devez nommer le fichier.");
            return;
        }

        String nomeFichier = mots[1];
        if (fichierService.creerFichier(nomeFichier, utilisateurConnecte.getLogin())) {
            System.out.println("Le fichier " + nomeFichier + " est bien cree.");
        } else {
            System.out.println("Ce nom existe deja.");
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