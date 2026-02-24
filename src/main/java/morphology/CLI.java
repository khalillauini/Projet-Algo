package morphology;

import java.io.*;
import java.util.*;

public class CLI {

    private final MorphologyEngine engine;
    private final Scanner          scanner;

    public CLI() {
        this.engine  = new MorphologyEngine();
        this.scanner = new Scanner(new InputStreamReader(System.in, java.nio.charset.StandardCharsets.UTF_8));
    }

    public void run() {
        printBanner();
        loadDefaultRoots();
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = readLine("Votre choix : ");
            switch (choice.trim()) {
                case "1"  -> manageRoots();
                case "2"  -> manageSchemes();
                case "3"  -> generateDerivatives();
                case "4"  -> validateMorphology();
                case "5"  -> analyzeWord();
                case "6"  -> showStatistics();
                case "0"  -> { running = false; System.out.println("\n  Au revoir!\n"); }
                default   -> System.out.println("  [X] Choix invalide.\n");
            }
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println("================================================================");
        System.out.println("  Moteur de Recherche Morphologique Arabe");
        System.out.println("  Generateur de Derivation & Validateur");
        System.out.println("================================================================");
        System.out.println("  Structures : Arbre AVL + Table de Hachage");
        System.out.println("================================================================");
        System.out.println();
    }

    private void printMainMenu() {
        System.out.println("----------------------------------------------------------------");
        System.out.println("  MENU PRINCIPAL");
        System.out.println("----------------------------------------------------------------");
        System.out.println("  1. Gestion des racines arabes      (Arbre AVL)");
        System.out.println("  2. Gestion des schemes             (Table de hachage)");
        System.out.println("  3. Generation morphologique        (Derivation)");
        System.out.println("  4. Validation morphologique        (Verification)");
        System.out.println("  5. Analyse d'un mot arabe          (Decomposition)");
        System.out.println("  6. Statistiques du systeme");
        System.out.println("  0. Quitter");
        System.out.println();
    }

    private void manageRoots() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  -- Gestion des Racines Arabes (Arbre AVL) --");
            System.out.println("  a. Afficher l'arbre des racines");
            System.out.println("  b. Lister toutes les racines");
            System.out.println("  c. Inserer une nouvelle racine");
            System.out.println("  d. Rechercher une racine");
            System.out.println("  e. Supprimer une racine");
            System.out.println("  f. Afficher les derives valides d'une racine");
            System.out.println("  0. Retour\n");
            String ch = readLine("  Choix : ");
            switch (ch.trim()) {
                case "a" -> engine.displayRootTree();
                case "b" -> {
                    List<String> roots = engine.getAllRoots();
                    System.out.println("\n  Racines indexees :");
                    for (int i = 0; i < roots.size(); i++)
                        System.out.printf("  %3d. %s%n", i + 1, roots.get(i));
                    System.out.println();
                }
                case "c" -> { String r = readLine("  Racine a inserer : "); engine.insertRoot(r); }
                case "d" -> {
                    String r = readLine("  Racine a rechercher : ");
                    AVLNode node = engine.searchRoot(r);
                    if (node == null) System.out.println("  [X] Introuvable : " + engine.cleanRoot(r));
                    else {
                        System.out.println("  [OK] Racine : " + node.root);
                        System.out.println("    Derives valides : " + node.derivedWords.size());
                        System.out.println("    Frequence       : " + node.frequency);
                        if (!node.derivedWords.isEmpty())
                            System.out.println("    Mots            : " + node.derivedWords);
                    }
                }
                case "e" -> {
                    String r = readLine("  Racine a supprimer : ");
                    System.out.println(engine.deleteRoot(r) ? "  [OK] Supprimee." : "  [X] Introuvable.");
                }
                case "f" -> { String r = readLine("  Racine : "); engine.displayValidatedDerivatives(r); }
                case "0" -> back = true;
                default  -> System.out.println("  [X] Choix invalide.");
            }
        }
    }

    private void manageSchemes() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  -- Gestion des Schemes Morphologiques (Table de Hachage) --");
            System.out.println("  a. Afficher tous les schemes");
            System.out.println("  b. Rechercher un scheme");
            System.out.println("  c. Ajouter / Modifier un scheme");
            System.out.println("  d. Supprimer un scheme");
            System.out.println("  0. Retour\n");
            String ch = readLine("  Choix : ");
            switch (ch.trim()) {
                case "a" -> engine.displaySchemeTable();
                case "b" -> {
                    String name = readLine("  Nom du scheme : ");
                    Scheme s = engine.getSchemeTable().get(name);
                    if (s == null) System.out.println("  [X] Introuvable.");
                    else {
                        System.out.println("  Nom         : " + s.getName());
                        System.out.println("  Squelette   : " + s.getSkeletonStr());
                        System.out.println("  Categorie   : " + s.getCategory());
                        System.out.println("  Description : " + s.getDescription());
                    }
                }
                case "c" -> {
                    String name = readLine("  Nom arabe du scheme : ");
                    String skelStr = readLine("  Squelette (1=C1,2=C2,3=C3 + lettres fixes, ex: 1a23) : ");
                    String cat  = readLine("  Categorie : ");
                    String desc = readLine("  Description : ");
                    // Parse: digits 1/2/3 become Integer, other chars become Character
                    java.util.List<Object> skel = new java.util.ArrayList<>();
                    for (char c : skelStr.toCharArray()) {
                        if (c == '1') skel.add(1);
                        else if (c == '2') skel.add(2);
                        else if (c == '3') skel.add(3);
                        else skel.add(c);
                    }
                    engine.addOrUpdateScheme(name, skel.toArray(), cat, desc);
                }
                case "d" -> {
                    String name = readLine("  Scheme a supprimer : ");
                    System.out.println(engine.deleteScheme(name) ? "  [OK] Supprime." : "  [X] Introuvable.");
                }
                case "0" -> back = true;
                default  -> System.out.println("  [X] Choix invalide.");
            }
        }
    }

    private void generateDerivatives() {
        System.out.println("\n  -- Generation Morphologique --");
        System.out.println("  a. Tous les schemes");
        System.out.println("  b. Un scheme specifique");
        System.out.println("  c. Plusieurs schemes selectionnes\n");
        String ch = readLine("  Choix : ");
        String root = readLine("  Racine arabe : ");
        switch (ch.trim()) {
            case "a" -> {
                Map<String, String> d = engine.generateAllDerivatives(root);
                if (d.isEmpty()) System.out.println("  [X] Generation impossible (racine invalide ?)");
                else {
                    engine.displayDerivatives(root, d);
                    if (readLine("  Enregistrer ces derives ? (o/n) : ").trim().equalsIgnoreCase("o")) {
                        engine.insertRoot(root);
                        for (String w : d.values()) engine.getRootTree().addDerivedWord(engine.cleanRoot(root), w);
                        System.out.println("  [OK] Enregistres.");
                    }
                }
            }
            case "b" -> {
                engine.displaySchemeTable();
                String sn = readLine("  Nom du scheme : ");
                String w  = engine.generateWord(root, sn);
                if (w != null) System.out.println("  Racine: " + root + " | Scheme: " + sn + " | Resultat: " + w);
            }
            case "c" -> {
                engine.displaySchemeTable();
                String input = readLine("  Schemes (separes par virgule) : ");
                Map<String, String> r = engine.generateSelectedDerivatives(root, Arrays.asList(input.split(",")));
                if (r.isEmpty()) System.out.println("  [X] Aucun derive genere.");
                else engine.displayDerivatives(root, r);
            }
            default -> System.out.println("  [X] Choix invalide.");
        }
    }

    private void validateMorphology() {
        System.out.println("\n  -- Validation Morphologique --");
        String word   = readLine("  Mot arabe a verifier : ");
        String root   = readLine("  Racine presumee      : ");
        ValidationResult result = engine.validateMorphology(word, root);
        System.out.println();
        System.out.println("  ------------------------------------------------");
        System.out.println("  Resultat : " + (result.isValid() ? "OUI [OK]" : "NON [X]"));
        if (result.isValid() && result.getScheme() != null) {
            System.out.println("  Scheme   : " + result.getScheme().getName() + " (" + result.getScheme().getCategory() + ")");
        }
        System.out.println("  Message  : " + result.getMessage());
        System.out.println("  ------------------------------------------------\n");
    }

    private void analyzeWord() {
        System.out.println("\n  -- Analyse Morphologique d'un Mot --");
        String word = readLine("  Mot arabe a analyser : ");
        List<ValidationResult> matches = engine.analyzeWord(word);
        System.out.println();
        if (matches.isEmpty()) {
            System.out.println("  [X] Aucune decomposition trouvee.");
        } else {
            System.out.println("  Analyses pour : " + word);
            System.out.println("  ----------------------------------------------------------------");
            System.out.printf("  %-15s | %-15s | %-28s%n", "Scheme", "Racine", "Statut");
            System.out.println("  ----------------------------------------------------------------");
            for (ValidationResult vr : matches) {
                String st = vr.getMessage().length() > 28 ? vr.getMessage().substring(0,25)+"..." : vr.getMessage();
                System.out.printf("  %-15s | %-15s | %-28s%n",
                        vr.getScheme().getName(),
                        vr.getExtractedRoot() != null ? vr.getExtractedRoot() : "?",
                        st);
            }
            System.out.println("  ----------------------------------------------------------------");
        }
        System.out.println();
    }

    private void showStatistics() {
        System.out.println("\n  -- Statistiques du Systeme --\n");
        System.out.println("  Arbre AVL des racines :");
        System.out.println("    Racines indexees : " + engine.getRootCount());
        System.out.println("    Complexite       : O(log n)");
        System.out.println();
        System.out.println("  Table de hachage :");
        System.out.println("    Schemes          : " + engine.getSchemeCount());
        System.out.println("    Capacite buckets : " + engine.getSchemeTable().getCapacity());
        System.out.printf ("    Facteur charge   : %.1f%%%n",
                (double) engine.getSchemeCount() / engine.getSchemeTable().getCapacity() * 100);
        System.out.println("    Complexite       : O(1) amorti");
        System.out.println();
        List<String> roots = engine.getAllRoots();
        System.out.println("  Racines avec derives valides :");
        roots.stream()
             .map(r -> engine.getRootTree().search(r))
             .filter(Objects::nonNull)
             .filter(n -> !n.derivedWords.isEmpty())
             .sorted((a, b) -> b.derivedWords.size() - a.derivedWords.size())
             .limit(5)
             .forEach(n -> System.out.printf("    - %-8s : %d derive(s)%n", n.root, n.derivedWords.size()));
        System.out.println();
    }

    private void loadDefaultRoots() {
        System.out.println("  Chargement des racines par defaut...");
        engine.loadRootsFromList(Arrays.asList(
            "كتب","قرأ","علم","فهم","درس","فتح","نصر","حمل","جلس","خرج",
            "دخل","قول","ضرب","سمع","رأى","أكل","شرب","نظر","حكم","صلح"
        ));
        System.out.println("  Systeme pret !\n");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        try { return scanner.hasNextLine() ? scanner.nextLine() : ""; }
        catch (Exception e) { return ""; }
    }
}
