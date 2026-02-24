package morphology;

import java.util.ArrayList;
import java.util.List;

/**
 * Nœud de l'arbre AVL contenant une racine arabe trilitere.
 * Chaque nœud stocke la racine, ses mots derives valides et leur frequence.
 */
public class AVLNode {
    String root;                          // Racine arabe (ex: كتب)
    List<String> derivedWords;            // Liste des mots derives valides
    int frequency;                        // Frequence d'apparition
    AVLNode left, right;
    int height;

    public AVLNode(String root) {
        this.root = root;
        this.derivedWords = new ArrayList<>();
        this.frequency = 0;
        this.height = 1;
    }

    public void addDerivedWord(String word) {
        if (!derivedWords.contains(word)) {
            derivedWords.add(word);
            frequency++;
        }
    }
}
