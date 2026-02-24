package morphology;

import java.util.ArrayList;
import java.util.List;

/**
 * Table de hachage implementee manuellement pour le stockage des schemes morphologiques.
 *
 * Strategie de resolution des collisions : chainage (listes chainees dans chaque bucket).
 * Facteur de charge cible : 0.75 → redimensionnement automatique.
 * Fonction de hachage : polynomiale sur les codes Unicode des caracteres arabes.
 *
 * Complexite moyenne : O(1) pour get/put/delete, O(n) dans le pire cas (collisions).
 */
public class SchemeHashTable {

    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR    = 0.75;

    // Maillon de la liste chainee dans chaque bucket
    private static class Entry {
        String  key;   // nom arabe du scheme
        Scheme  value;
        Entry   next;

        Entry(String key, Scheme value) {
            this.key   = key;
            this.value = value;
        }
    }

    private Entry[] buckets;
    private int     size;
    private int     capacity;

    @SuppressWarnings("unchecked")
    public SchemeHashTable() {
        this.capacity = INITIAL_CAPACITY;
        this.buckets  = new Entry[capacity];
        this.size     = 0;
    }

    // --- Fonction de hachage -----------------------------------------------------

    /**
     * Hachage polynomial sur les codes Unicode.
     * h(k) = Σ k[i] * 31^(n-1-i)  mod capacity
     */
    private int hash(String key) {
        int hash = 0;
        for (char c : key.toCharArray()) {
            hash = (hash * 31 + c) & 0x7FFFFFFF; // masque pour rester positif
        }
        return hash % capacity;
    }

    // --- Operations CRUD ---------------------------------------------------------

    /**
     * Insere ou met a jour un scheme dans la table.
     */
    public void put(String key, Scheme scheme) {
        if ((double) size / capacity >= LOAD_FACTOR) {
            resize();
        }
        int idx = hash(key);
        Entry cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) {
                cur.value = scheme; // mise a jour
                return;
            }
            cur = cur.next;
        }
        // Insertion en tete de liste
        Entry entry = new Entry(key, scheme);
        entry.next  = buckets[idx];
        buckets[idx] = entry;
        size++;
    }

    /**
     * Recupere un scheme par sa cle.
     * @return le Scheme ou null si absent
     */
    public Scheme get(String key) {
        int idx = hash(key);
        Entry cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    /**
     * Supprime un scheme de la table.
     * @return true si supprime, false si absent
     */
    public boolean delete(String key) {
        int idx = hash(key);
        Entry cur  = buckets[idx];
        Entry prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) buckets[idx] = cur.next;
                else              prev.next     = cur.next;
                size--;
                return true;
            }
            prev = cur;
            cur  = cur.next;
        }
        return false;
    }

    /**
     * Verifie l'existence d'un scheme.
     */
    public boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * Retourne la liste de tous les schemes stockes.
     */
    public List<Scheme> getAllSchemes() {
        List<Scheme> list = new ArrayList<>();
        for (Entry bucket : buckets) {
            Entry cur = bucket;
            while (cur != null) {
                list.add(cur.value);
                cur = cur.next;
            }
        }
        return list;
    }

    /**
     * Retourne toutes les cles.
     */
    public List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        for (Entry bucket : buckets) {
            Entry cur = bucket;
            while (cur != null) {
                keys.add(cur.key);
                cur = cur.next;
            }
        }
        return keys;
    }

    // --- Redimensionnement -------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void resize() {
        int      oldCapacity = capacity;
        Entry[]  oldBuckets  = buckets;
        capacity = capacity * 2;
        buckets  = new Entry[capacity];
        size     = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Entry cur = oldBuckets[i];
            while (cur != null) {
                put(cur.key, cur.value);
                cur = cur.next;
            }
        }
    }

    // --- Affichage ---------------------------------------------------------------

    /**
     * Affiche le contenu de la table de hachage avec les statistiques des buckets.
     */
    public void display() {
        System.out.println("================================================================");
        System.out.println("         TABLE DE HACHAGE DES SCHEMES MORPHOLOGIQUES");
        System.out.println("================================================================");
        System.out.printf("  %-14s | %-14s | %-30s%n", "Scheme", "Categorie", "Description");
        System.out.println("  ---------------------------------------------------------------");
        for (Entry bucket : buckets) {
            Entry cur = bucket;
            while (cur != null) {
                System.out.printf("  %-14s | %-14s | %-30s%n",
                        cur.value.getName(),
                        cur.value.getCategory(),
                        cur.value.getDescription());
                cur = cur.next;
            }
        }
        System.out.println("================================================================");
        System.out.printf("  Total : %d schemes | Capacite : %d buckets | Charge : %.1f%%%n",
                size, capacity, (double) size / capacity * 100);
    }

    public int getSize()     { return size; }
    public int getCapacity() { return capacity; }

    public int getBucketSize(int idx) {
        if (idx < 0 || idx >= capacity) return 0;
        int count = 0;
        Entry cur = buckets[idx];
        while (cur != null) { count++; cur = cur.next; }
        return count;
    }
    public int bucketIndexOf(String key) { return hash(key); }
}
