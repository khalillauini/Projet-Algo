package morphology;

import java.util.*;

/**
 * Moteur de derivation morphologique arabe.
 *
 * Principe cle : toutes les operations travaillent sur les CONSONNES uniquement.
 * Les diacritiques (harakat) sont ignores lors de la comparaison et de l'extraction.
 *
 * Squelette d'un scheme : suite de tokens Integer(1/2/3) ou Character.
 *   1 → C1 (1ere consonne de la racine)
 *   2 → C2
 *   3 → C3
 *   'x' → lettre fixe du scheme
 */
public class MorphologyEngine {

    private final AVLTree         rootTree;
    private final SchemeHashTable schemeTable;

    public MorphologyEngine() {
        this.rootTree   = new AVLTree();
        this.schemeTable = new SchemeHashTable();
        loadDefaultSchemes();
    }

    // ── Schemes par defaut ────────────────────────────────────────────────────

    private void loadDefaultSchemes() {
        // Helper: 'ا'=\u0627, 'م'=\u0645, 'و'=\u0648, 'ت'=\u062A, 'ي'=\u064A
        // 'ة'=\u0629, 'س'=\u0633, 'ن'=\u0646
        char alef  = '\u0627'; // ا
        char meem  = '\u0645'; // م
        char waw   = '\u0648'; // و
        char ta    = '\u062A'; // ت
        char ya    = '\u064A'; // ي
        char ta_m  = '\u0629'; // ة
        char seen  = '\u0633'; // س
        char noon  = '\u0646'; // ن

        // فاعل  [C1, ا, C2, C3]  → كاتب
        addScheme("فاعل",   new Object[]{1, alef, 2, 3},
                "Nom d'agent",    "Celui qui fait l'action (katib=كاتب)");

        // مفعول [م, C1, C2, و, C3] → مكتوب
        addScheme("مفعول",  new Object[]{meem, 1, 2, waw, 3},
                "Nom d'objet",    "Ce qui subit l'action (maktub=مكتوب)");

        // فعال  [C1, C2, ا, C3]  → كتاب
        addScheme("فعال",   new Object[]{1, 2, alef, 3},
                "Masdar intensif","Nom verbal intensif (kitab=كتاب)");

        // مفعل  [م, C1, C2, C3]  → مكتب
        addScheme("مفعل",   new Object[]{meem, 1, 2, 3},
                "Nom de lieu",    "Lieu de l'action (maktab=مكتب)");

        // افتعل [ا, C1, ت, C2, C3] → اكتتب
        addScheme("افتعل",  new Object[]{alef, 1, ta, 2, 3},
                "Forme VIII",     "Verbe reflexif forme VIII (iktatab=اكتتب)");

        // تفاعل [ت, C1, ا, C2, C3] → تكاتب
        addScheme("تفاعل",  new Object[]{ta, 1, alef, 2, 3},
                "Forme VI",       "Action reciproque (takatab=تكاتب)");

        // فعيل  [C1, C2, ي, C3]  → كتيب
        addScheme("فعيل",   new Object[]{1, 2, ya, 3},
                "Adjectif",       "Adjectif de qualite (kabir=كبير)");

        // تفعيل [ت, C1, C2, ي, C3] → تكتيب
        addScheme("تفعيل",  new Object[]{ta, 1, 2, ya, 3},
                "Masdar f.II",    "Masdar de la forme II (taktib=تكتيب)");

        // فاعلة [C1, ا, C2, C3, ة] → كاتبة
        addScheme("فاعلة",  new Object[]{1, alef, 2, 3, ta_m},
                "Fem. nom agent",  "Nom d'agent feminin (katiba=كاتبة)");

        // مفاعل [م, C1, ا, C2, C3] → مكاتب
        addScheme("مفاعل",  new Object[]{meem, 1, alef, 2, 3},
                "Pluriel brise",  "Pluriel brise forme III (makatib=مكاتب)");

        // استفعل [ا,س,ت, C1, C2, C3] → استكتب
        addScheme("استفعل", new Object[]{alef, seen, ta, 1, 2, 3},
                "Forme X",        "Demander a faire (istaktab=استكتب)");

        // فعلة  [C1, C2, C3, ة] → كتبة
        addScheme("فعلة",   new Object[]{1, 2, 3, ta_m},
                "Nom de groupe",  "Collectif feminin (kataba=كتبة)");

        // فعل   [C1, C2, C3]    → كتب (la racine elle-meme)
        addScheme("فعل",    new Object[]{1, 2, 3},
                "Forme de base",  "La racine (verbe accompli) (kataba=كتب)");

        // فعلان [C1, C2, C3, ا, ن] → كتبان
        addScheme("فعلان",  new Object[]{1, 2, 3, alef, noon},
                "Duel",           "Duel masculin (kataban=كتبان)");
    }

    private void addScheme(String name, Object[] skeleton, String cat, String desc) {
        schemeTable.put(name, new Scheme(name, skeleton, cat, desc));
    }

    // ── Gestion des racines ───────────────────────────────────────────────────

    public boolean insertRoot(String root) {
        String cleaned = cleanRoot(root);
        char[] cons = Scheme.extractConsonants(cleaned);
        if (cons == null || cons.length != 3) {
            System.out.println("  [X] Racine invalide (doit contenir exactement 3 consonnes) : " + root);
            return false;
        }
        if (rootTree.search(cleaned) != null) {
            System.out.println("  [X] Racine deja existante : " + cleaned);
            return false;
        }
        rootTree.insert(cleaned);
        System.out.println("  [OK] Racine inseree : " + cleaned);
        return true;
    }

    public AVLNode searchRoot(String root) {
        return rootTree.search(cleanRoot(root));
    }

    public boolean deleteRoot(String root) {
        String c = cleanRoot(root);
        if (rootTree.search(c) == null) return false;
        rootTree.delete(c);
        return true;
    }

    public void displayRootTree()   { rootTree.display(); System.out.println("  Total : " + rootTree.size() + " racines.\n"); }
    public List<String> getAllRoots() { return rootTree.getAllRoots(); }

    // ── Gestion des schemes ───────────────────────────────────────────────────

    public void addOrUpdateScheme(String name, Object[] skeleton, String cat, String desc) {
        schemeTable.put(name, new Scheme(name, skeleton, cat, desc));
        System.out.println("  [OK] Scheme " + name + " ajoute/mis a jour.");
    }

    public boolean deleteScheme(String name) { return schemeTable.delete(name); }
    public void displaySchemeTable()         { schemeTable.display(); }
    public List<Scheme> getAllSchemes()       { return schemeTable.getAllSchemes(); }

    // ── Moteur de derivation ──────────────────────────────────────────────────

    /**
     * Genere un mot derive a partir d'une racine et d'un scheme.
     */
    public String generateWord(String root, String schemeName) {
        char[] cons = Scheme.extractConsonants(cleanRoot(root));
        if (cons == null) { System.out.println("  [X] Racine invalide."); return null; }
        Scheme s = schemeTable.get(schemeName);
        if (s == null) { System.out.println("  [X] Scheme inconnu : " + schemeName); return null; }
        return s.apply(cons);
    }

    /**
     * Genere tous les derives d'une racine avec tous les schemes.
     * Retourne une map : nom_scheme → mot_derive
     */
    public Map<String, String> generateAllDerivatives(String root) {
        char[] cons = Scheme.extractConsonants(cleanRoot(root));
        if (cons == null) return Collections.emptyMap();
        Map<String, String> results = new LinkedHashMap<>();
        for (Scheme s : schemeTable.getAllSchemes()) {
            String word = s.apply(cons);
            if (word != null) results.put(s.getName(), word);
        }
        return results;
    }

    /**
     * Genere les derives avec une liste de schemes selectionnes.
     */
    public Map<String, String> generateSelectedDerivatives(String root, List<String> schemeNames) {
        char[] cons = Scheme.extractConsonants(cleanRoot(root));
        if (cons == null) return Collections.emptyMap();
        Map<String, String> results = new LinkedHashMap<>();
        for (String name : schemeNames) {
            Scheme s = schemeTable.get(name.trim());
            if (s != null) {
                String word = s.apply(cons);
                if (word != null) results.put(name, word);
            }
        }
        return results;
    }

    public void displayDerivatives(String root, Map<String, String> derivatives) {
        System.out.println("================================================================");
        System.out.println("  FAMILLE MORPHOLOGIQUE DE : " + root);
        System.out.println("================================================================");
        System.out.printf("  %-12s | %-18s | %-20s%n", "Scheme", "Mot Genere", "Categorie");
        System.out.println("  ---------------------------------------------------------------");
        for (Map.Entry<String, String> e : derivatives.entrySet()) {
            Scheme s = schemeTable.get(e.getKey());
            String cat = (s != null) ? s.getCategory() : "-";
            System.out.printf("  %-12s | %-18s | %-20s%n", e.getKey(), e.getValue(), cat);
        }
        System.out.println("================================================================");
    }

    // ── Validation morphologique ──────────────────────────────────────────────

    /**
     * Verifie si un mot appartient morphologiquement a une racine donnee.
     *
     * Algorithme :
     *  1. Extraire les consonnes de la racine fournie
     *  2. Pour chaque scheme, tenter d'extraire les consonnes du mot
     *  3. Comparer avec les consonnes de la racine
     */
    public ValidationResult validateMorphology(String word, String root) {
        char[] rootCons = Scheme.extractConsonants(cleanRoot(root));
        if (rootCons == null) return new ValidationResult(false, null, "Racine invalide.");

        String rootStr = new String(rootCons);

        for (Scheme s : schemeTable.getAllSchemes()) {
            char[] extracted = s.extractRoot(word);
            if (extracted == null) continue;
            if (new String(extracted).equals(rootStr)) {
                // Enregistrer le derive valide
                AVLNode node = rootTree.search(cleanRoot(root));
                if (node != null) node.addDerivedWord(word);
                return new ValidationResult(true, s, "Correspondance trouvee.");
            }
        }
        return new ValidationResult(false, null, "Aucun scheme ne correspond.");
    }

    /**
     * Analyse un mot : trouve toutes les paires (scheme, racine) possibles.
     */
    public List<ValidationResult> analyzeWord(String word) {
        List<ValidationResult> matches = new ArrayList<>();
        for (Scheme s : schemeTable.getAllSchemes()) {
            char[] extracted = s.extractRoot(word);
            if (extracted != null) {
                String extractedRoot = new String(extracted);
                boolean inTree = rootTree.search(extractedRoot) != null;
                ValidationResult vr = new ValidationResult(true, s,
                        inTree ? "Racine " + extractedRoot + " trouvee dans l'arbre"
                               : "Racine " + extractedRoot + " (non indexee)");
                vr.setExtractedRoot(extractedRoot);
                matches.add(vr);
            }
        }
        return matches;
    }

    // ── Derives valides ───────────────────────────────────────────────────────

    public void displayValidatedDerivatives(String root) {
        String cleaned = cleanRoot(root);
        AVLNode node = rootTree.search(cleaned);
        if (node == null) { System.out.println("  [X] Racine " + cleaned + " introuvable."); return; }
        System.out.println("  Derives valides pour " + cleaned + " :");
        if (node.derivedWords.isEmpty()) System.out.println("    (aucun derive valide pour l'instant)");
        else for (int i=0; i<node.derivedWords.size(); i++)
            System.out.println("    " + (i+1) + ". " + node.derivedWords.get(i));
        System.out.println("  Frequence totale : " + node.frequency);
    }

    // ── Chargement par lot ────────────────────────────────────────────────────

    public void loadRootsFromList(List<String> roots) {
        int count = 0;
        for (String r : roots) {
            String cleaned = cleanRoot(r);
            char[] cons = Scheme.extractConsonants(cleaned);
            if (cons != null && cons.length == 3 && rootTree.search(cleaned) == null) {
                rootTree.insert(cleaned);
                count++;
            }
        }
        System.out.println("  [OK] " + count + " racines chargees.");
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    public String cleanRoot(String root) {
        if (root == null) return "";
        // Retire espaces, tirets, et les diacritiques pour avoir la forme canonique
        StringBuilder sb = new StringBuilder();
        for (char c : root.toCharArray()) {
            int code = (int) c;
            if ((code >= 0x0621 && code <= 0x063A) || (code >= 0x0641 && code <= 0x064A)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int getRootCount()            { return rootTree.size(); }
    public int getSchemeCount()          { return schemeTable.getSize(); }
    public AVLTree getRootTree()         { return rootTree; }
    public SchemeHashTable getSchemeTable() { return schemeTable; }
}
