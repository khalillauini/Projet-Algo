package morphology;

import java.util.ArrayList;
import java.util.List;

/**
 * Scheme morphologique arabe.
 *
 * Le squelette consonantique (skeleton) est une liste de tokens :
 *   - Integer 1, 2, 3  → position de la consonne de la racine (C1, C2, C3)
 *   - Character         → lettre fixe appartenant au scheme
 *
 * Cette approche travaille UNIQUEMENT sur les consonnes (sans diacritiques),
 * ce qui rend la generation et la validation robustes et correctes.
 *
 * Exemple :
 *   Scheme فاعل  → skeleton = [1, 'ا', 2, 3]
 *   Racine كتب   → consonnes = ['ك','ت','ب']
 *   Resultat     → ك + ا + ت + ب = كاتب
 */
public class Scheme {

    private final String       name;
    private final Object[]     skeleton;  // Integer(1/2/3) ou Character
    private final String       category;
    private final String       description;

    public Scheme(String name, Object[] skeleton, String category, String description) {
        this.name        = name;
        this.skeleton    = skeleton;
        this.category    = category;
        this.description = description;
    }

    // ── Generation ────────────────────────────────────────────────────────────

    /**
     * Applique ce scheme a un tableau de consonnes de la racine.
     * @param consonants les consonnes extraites de la racine (sans diacritiques)
     * @return le mot derive (consonnes uniquement, sans harakat)
     */
    public String apply(char[] consonants) {
        if (consonants == null || consonants.length < 2) return null;
        StringBuilder sb = new StringBuilder();
        for (Object token : skeleton) {
            if (token instanceof Integer) {
                int idx = (Integer) token - 1;
                if (idx < consonants.length) sb.append(consonants[idx]);
            } else {
                sb.append((Character) token);
            }
        }
        return sb.toString();
    }

    // ── Validation inverse ────────────────────────────────────────────────────

    /**
     * Tente d'extraire les consonnes de la racine a partir d'un mot donne.
     * Travaille sur les consonnes uniquement (les diacritiques du mot sont ignores).
     *
     * @param word le mot arabe a decomposer
     * @return les 3 consonnes extraites [C1,C2,C3], ou null si incompatible
     */
    public char[] extractRoot(String word) {
        char[] wordCons = extractConsonants(word);
        if (wordCons == null || wordCons.length != skeleton.length) return null;

        char c1 = 0, c2 = 0, c3 = 0;

        for (int i = 0; i < skeleton.length; i++) {
            Object token = skeleton[i];
            char   wc    = wordCons[i];

            if (token instanceof Integer) {
                int pos = (Integer) token;
                if (pos == 1) {
                    if (c1 == 0) c1 = wc;
                    else if (c1 != wc) return null;
                } else if (pos == 2) {
                    if (c2 == 0) c2 = wc;
                    else if (c2 != wc) return null;
                } else if (pos == 3) {
                    if (c3 == 0) c3 = wc;
                    else if (c3 != wc) return null;
                }
            } else {
                // Lettre fixe : doit correspondre exactement
                char fixed = (Character) token;
                if (fixed != wc) return null;
            }
        }

        if (c1 == 0 || c2 == 0 || c3 == 0) return null;
        return new char[]{c1, c2, c3};
    }

    // ── Utilitaire : extraction des consonnes ─────────────────────────────────

    /**
     * Extrait uniquement les lettres arabes consonantiques d'un mot,
     * en supprimant tous les diacritiques (harakat : U+064B a U+065F).
     */
    public static char[] extractConsonants(String word) {
        if (word == null) return null;
        List<Character> result = new ArrayList<>();
        for (char c : word.toCharArray()) {
            int code = (int) c;
            // Lettres arabes consonantiques (excluant les diacritiques)
            if ((code >= 0x0621 && code <= 0x063A) || (code >= 0x0641 && code <= 0x064A)) {
                result.add(c);
            }
            // On ignore tout le reste (diacritiques, espaces, etc.)
        }
        if (result.isEmpty()) return null;
        char[] arr = new char[result.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = result.get(i);
        return arr;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String   getName()        { return name; }
    public Object[] getSkeleton()    { return skeleton; }
    public String   getCategory()    { return category; }
    public String   getDescription() { return description; }

    /** Representation lisible du squelette consonantique */
    public String getSkeletonStr() {
        StringBuilder sb = new StringBuilder();
        for (Object t : skeleton) {
            if (t instanceof Integer) sb.append(t);
            else sb.append((Character) t);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%-8s | skeleton=%-12s | %-18s | %s",
                name, getSkeletonStr(), category, description);
    }
}
