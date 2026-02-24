package morphology;

import java.util.ArrayList;
import java.util.List;

/**
 * Arbre AVL pour l'indexation efficace des racines arabes triliteres.
 * La comparaison des racines se fait caractere par caractere (Unicode arabe).
 * Complexite : O(log n) pour insertion, recherche et suppression.
 */
public class AVLTree {

    private AVLNode root;

    // --- Utilitaires AVL --------------------------------------------------------

    private int height(AVLNode node) {
        return node == null ? 0 : node.height;
    }

    private int balanceFactor(AVLNode node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private void updateHeight(AVLNode node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;
        x.right = y;
        y.left = T2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;
        y.left = x;
        x.right = T2;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    private AVLNode balance(AVLNode node) {
        updateHeight(node);
        int bf = balanceFactor(node);

        // Cas gauche-gauche
        if (bf > 1 && balanceFactor(node.left) >= 0)
            return rotateRight(node);
        // Cas gauche-droite
        if (bf > 1 && balanceFactor(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        // Cas droite-droite
        if (bf < -1 && balanceFactor(node.right) <= 0)
            return rotateLeft(node);
        // Cas droite-gauche
        if (bf < -1 && balanceFactor(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }
        return node;
    }

    // --- Operations publiques ----------------------------------------------------

    /**
     * Insere une racine dans l'arbre AVL.
     * @param rootWord la racine arabe a inserer
     */
    public void insert(String rootWord) {
        root = insert(root, rootWord);
    }

    private AVLNode insert(AVLNode node, String rootWord) {
        if (node == null) return new AVLNode(rootWord);
        int cmp = rootWord.compareTo(node.root);
        if (cmp < 0)       node.left  = insert(node.left,  rootWord);
        else if (cmp > 0)  node.right = insert(node.right, rootWord);
        else return node; // deja presente
        return balance(node);
    }

    /**
     * Recherche une racine dans l'arbre.
     * @param rootWord la racine a chercher
     * @return le nœud AVL ou null si absent
     */
    public AVLNode search(String rootWord) {
        return search(root, rootWord);
    }

    private AVLNode search(AVLNode node, String rootWord) {
        if (node == null) return null;
        int cmp = rootWord.compareTo(node.root);
        if (cmp == 0)  return node;
        if (cmp < 0)   return search(node.left,  rootWord);
        return             search(node.right, rootWord);
    }

    /**
     * Supprime une racine de l'arbre.
     * @param rootWord la racine a supprimer
     */
    public void delete(String rootWord) {
        root = delete(root, rootWord);
    }

    private AVLNode delete(AVLNode node, String rootWord) {
        if (node == null) return null;
        int cmp = rootWord.compareTo(node.root);
        if (cmp < 0) {
            node.left = delete(node.left, rootWord);
        } else if (cmp > 0) {
            node.right = delete(node.right, rootWord);
        } else {
            if (node.left == null || node.right == null) {
                node = (node.left != null) ? node.left : node.right;
            } else {
                AVLNode minRight = minNode(node.right);
                node.root = minRight.root;
                node.derivedWords = minRight.derivedWords;
                node.frequency = minRight.frequency;
                node.right = delete(node.right, minRight.root);
            }
        }
        return node == null ? null : balance(node);
    }

    private AVLNode minNode(AVLNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    /**
     * Ajoute un mot derive valide a une racine existante.
     */
    public boolean addDerivedWord(String rootWord, String derivedWord) {
        AVLNode node = search(rootWord);
        if (node == null) return false;
        node.addDerivedWord(derivedWord);
        return true;
    }

    /**
     * Retourne toutes les racines en ordre in-order (ordre lexicographique arabe).
     */
    public List<String> getAllRoots() {
        List<String> result = new ArrayList<>();
        inOrder(root, result);
        return result;
    }

    private void inOrder(AVLNode node, List<String> result) {
        if (node == null) return;
        inOrder(node.left, result);
        result.add(node.root);
        inOrder(node.right, result);
    }

    /**
     * Affiche l'arbre de maniere structuree.
     */
    public void display() {
        System.out.println("================================================");
        System.out.println("       ARBRE AVL DES RACINES ARABES");
        System.out.println("================================================");
        if (root == null) {
            System.out.println("  (arbre vide)");
        } else {
            displayTree(root, "", true);
        }
    }

    private void displayTree(AVLNode node, String prefix, boolean isRight) {
        if (node == null) return;
        System.out.println(prefix + (isRight ? "+-- " : "+-- ") + node.root
                + " [h=" + node.height + ", derives=" + node.derivedWords.size() + "]");
        if (node.left != null || node.right != null) {
            displayTree(node.right, prefix + (isRight ? "    " : "|   "), true);
            displayTree(node.left,  prefix + (isRight ? "    " : "|   "), false);
        }
    }

    /**
     * Verifie si l'arbre est vide.
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Retourne le nombre de racines dans l'arbre.
     */
    public int size() {
        return size(root);
    }

    private int size(AVLNode node) {
        if (node == null) return 0;
        return 1 + size(node.left) + size(node.right);
    }

    public AVLNode getRoot() { return root; }
}
