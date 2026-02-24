package morphology;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Interface graphique Swing — Moteur Morphologique Arabe.
 *
 * Utilise directement :
 *   MorphologyEngine  → logique centrale
 *   AVLTree / AVLNode → arbre des racines
 *   SchemeHashTable   → dictionnaire des schemes
 *   Scheme            → application/inversion du squelette
 *   ValidationResult  → resultat de validation
 */
public class GUI extends JFrame {

    // ── Couleurs ──────────────────────────────────────────────────────────────
    private static final Color C_BG      = new Color(18,  20,  26);
    private static final Color C_SURFACE = new Color(28,  31,  40);
    private static final Color C_BORDER  = new Color(55,  60,  75);
    private static final Color C_GOLD    = new Color(212, 175, 82);
    private static final Color C_GOLDD   = new Color(140, 110, 45);
    private static final Color C_TEAL    = new Color(56,  178, 172);
    private static final Color C_TEALD   = new Color(30,  110, 106);
    private static final Color C_TEXT    = new Color(220, 215, 200);
    private static final Color C_MUTED   = new Color(110, 108, 100);
    private static final Color C_OK      = new Color(72,  199, 116);
    private static final Color C_ERR     = new Color(220, 80,  70);
    private static final Color C_SIDE    = new Color(22,  24,  32);

    // ── Polices ───────────────────────────────────────────────────────────────
    private static final Font F_AR_LG = new Font("Arial", Font.BOLD,  22);
    private static final Font F_AR_MD = new Font("Arial", Font.PLAIN, 16);
    private static final Font F_AR_SM = new Font("Arial", Font.PLAIN, 13);
    private static final Font F_BOLD  = new Font("SansSerif", Font.BOLD,  13);
    private static final Font F_UI    = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font F_MONO  = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font F_TTL   = new Font("SansSerif", Font.BOLD,  15);
    private static final Font F_HDR   = new Font("SansSerif", Font.BOLD,  11);

    // ── Moteur ────────────────────────────────────────────────────────────────
    private final MorphologyEngine engine = new MorphologyEngine();

    // ── Navigation ────────────────────────────────────────────────────────────
    private final CardLayout     cards   = new CardLayout();
    private final JPanel         content = new JPanel(cards);
    private final List<JButton>  navBtns = new ArrayList<>();

    // ── Modeles de tables (mis a jour dynamiquement) ───────────────────────────
    private DefaultTableModel rootsModel;
    private DefaultTableModel schemesModel;
    private DefaultTableModel genModel;
    private DefaultTableModel anaModel;

    // ── Zone de sortie des racines ─────────────────────────────────────────────
    private JTextArea rootOut;
    private JTextArea schemeOut;

    // ── Canvas AVL (reference pour repaint) ───────────────────────────────────
    private AVLCanvas avlCanvas;

    // ─────────────────────────────────────────────────────────────────────────
    public GUI() {
        super("Moteur Morphologique Arabe — \u0645\u062d\u0631\u0643 \u0627\u0644\u0635\u0631\u0641 \u0627\u0644\u0639\u0631\u0628\u064a");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1160, 740);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        // Racines par defaut (20 racines arabes courantes)
        engine.loadRootsFromList(Arrays.asList(
            "\u0643\u062a\u0628", "\u0642\u0631\u0623", "\u0639\u0644\u0645",
            "\u0641\u0647\u0645", "\u062f\u0631\u0633", "\u0641\u062a\u062d",
            "\u0646\u0635\u0631", "\u062d\u0645\u0644", "\u062c\u0644\u0633",
            "\u062e\u0631\u062c", "\u062f\u062e\u0644", "\u0642\u0648\u0644",
            "\u0636\u0631\u0628", "\u0633\u0645\u0639", "\u0631\u0623\u0649",
            "\u0623\u0643\u0644", "\u0634\u0631\u0628", "\u0646\u0638\u0631",
            "\u062d\u0643\u0645", "\u0635\u0644\u062d"
        ));

        getContentPane().setBackground(C_BG);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildHeader(),  BorderLayout.NORTH);
        getContentPane().add(buildSidebar(), BorderLayout.WEST);

        content.setBackground(C_BG);
        content.add(buildHomePanel(),    "home");
        content.add(buildRootsPanel(),   "roots");
        content.add(buildSchemesPanel(), "schemes");
        content.add(buildGenPanel(),     "gen");
        content.add(buildValidPanel(),   "valid");
        content.add(buildAnalyzePanel(), "analyze");
        getContentPane().add(content, BorderLayout.CENTER);

        showCard("home", 0);
        setVisible(true);
    }

    // ═══════════════════════════════════ HEADER ═══════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_SIDE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, C_GOLDD));
        p.setPreferredSize(new Dimension(0, 56));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        left.setOpaque(false);
        JLabel ar = new JLabel("\u0645\u062d\u0631\u0643 \u0627\u0644\u0635\u0631\u0641 \u0627\u0644\u0639\u0631\u0628\u064a");
        ar.setFont(F_AR_LG); ar.setForeground(C_GOLD);
        JLabel fr = new JLabel("Moteur Morphologique Arabe");
        fr.setFont(F_MONO);  fr.setForeground(C_MUTED);
        left.add(ar); left.add(fr);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 16));
        right.setOpaque(false);
        JLabel info = new JLabel("AVL + Hash Table  |  2025-2026");
        info.setFont(F_MONO); info.setForeground(C_MUTED);
        right.add(info);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ═══════════════════════════════════ SIDEBAR ══════════════════════════════

    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SIDE);
        p.setPreferredSize(new Dimension(205, 0));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, C_BORDER));
        p.add(Box.createVerticalStrut(18));

        String[][] items = {
            {"home",    "\u2302  Accueil"},
            {"roots",   "\u25B8  Racines (AVL)"},
            {"schemes", "\u25B8  Schemes (Hash)"},
            {"gen",     "\u25B8  Generation"},
            {"valid",   "\u25B8  Validation"},
            {"analyze", "\u25B8  Analyse"},
        };
        for (int i = 0; i < items.length; i++) {
            final String card = items[i][0];
            final int    idx  = i;
            JButton btn = new JButton(items[i][1]);
            btn.setFont(F_UI); btn.setForeground(C_MUTED);
            btn.setBackground(C_SIDE); btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(205, 40));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            btn.addActionListener(e -> showCard(card, idx));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { if (!btn.getBackground().equals(C_TEALD)) btn.setBackground(new Color(35,38,50)); }
                public void mouseExited(MouseEvent e)  { if (!btn.getBackground().equals(C_TEALD)) btn.setBackground(C_SIDE); }
            });
            navBtns.add(btn);
            p.add(btn);
            if (i == 0) { JSeparator s = new JSeparator(); s.setForeground(C_BORDER); s.setMaximumSize(new Dimension(200,1)); p.add(s); }
        }
        p.add(Box.createVerticalGlue());
        JLabel ver = new JLabel("v2.0  consonant-skeleton");
        ver.setFont(new Font("Monospaced", Font.PLAIN, 9));
        ver.setForeground(new Color(55,58,68));
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(ver); p.add(Box.createVerticalStrut(10));
        return p;
    }

    private void showCard(String card, int idx) {
        cards.show(content, card);
        for (int i = 0; i < navBtns.size(); i++) {
            JButton b = navBtns.get(i);
            if (i == idx) { b.setBackground(C_TEALD); b.setForeground(C_TEXT); b.setFont(F_BOLD); }
            else           { b.setBackground(C_SIDE);  b.setForeground(C_MUTED);b.setFont(F_UI);  }
        }
    }

    // ═══════════════════════════════════ HOME ═════════════════════════════════

    private JPanel buildHomePanel() {
        JPanel p = dark(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.insets = new Insets(8, 30, 8, 30);

        g.gridy = 0;
        JLabel big = new JLabel("\u0645\u062d\u0631\u0643 \u0627\u0644\u0628\u062d\u062b \u0627\u0644\u0635\u0631\u0641\u064a \u0627\u0644\u0639\u0631\u0628\u064a");
        big.setFont(new Font("Arial", Font.BOLD, 34)); big.setForeground(C_GOLD);
        p.add(big, g);

        g.gridy = 1;
        JLabel sub = new JLabel("Moteur Morphologique  —  Generateur de Derivation Arabe");
        sub.setFont(F_MONO); sub.setForeground(C_MUTED);
        p.add(sub, g);

        g.gridy = 2; g.insets = new Insets(28, 30, 8, 30);
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.add(hcard("Arbre AVL",        "Indexation des racines\nO(log n) garanti",              C_GOLD));
        grid.add(hcard("Table de Hachage", "Acces aux schemes\nO(1) amorti",                        C_TEAL));
        grid.add(hcard("Generation",       "14 schemes morphologiques\nRacine doit exister dans AVL",new Color(160,100,200)));
        grid.add(hcard("Validation",       "Verification d'appartenance\nMet a jour derives + freq.", new Color(220,120,60)));
        grid.add(hcard("Analyse inverse",  "Decomposition d'un mot\nIdentification du scheme",       new Color(60,180,100)));
        grid.add(hcard("20 racines",       "Chargees au demarrage\nAccepte 2+ consonnes",            new Color(80,140,220)));
        p.add(grid, g);

        g.gridy = 3; g.insets = new Insets(18, 30, 8, 30);
        JLabel hint = new JLabel("Naviguez via le menu lateral.");
        hint.setFont(F_MONO); hint.setForeground(C_MUTED);
        p.add(hint, g);
        return p;
    }

    private JPanel hcard(String title, String body, Color accent) {
        JPanel c = new JPanel(new BorderLayout(5, 5));
        c.setBackground(C_SURFACE);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel t = new JLabel(title); t.setFont(F_BOLD); t.setForeground(accent);
        c.add(t, BorderLayout.NORTH);
        JLabel b = new JLabel("<html><font color='#6e6c64'>" + body.replace("\n","<br>") + "</font></html>");
        b.setFont(F_UI); c.add(b, BorderLayout.CENTER);
        return c;
    }

    // ═══════════════════════════════════ RACINES ══════════════════════════════

    private JPanel buildRootsPanel() {
        JPanel p = dark(new BorderLayout(14, 14));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Colonne gauche : controles ─────────────────────────────────────────
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(265, 0));

        left.add(sec("RACINE ARABE")); left.add(box(5));
        JTextField inp = arField(); left.add(inp); left.add(box(10));

        // Inserer
        left.add(btn("Inserer", C_OK, e -> {
            boolean ok = engine.insertRoot(inp.getText().trim());
            rootOut.setText(ok ? "[OK] Inseree : " + engine.cleanRoot(inp.getText().trim())
                               : "[X] Invalide ou deja existante.");
            refreshRoots();
        }));
        left.add(box(6));
        // Rechercher
        left.add(btn("Rechercher", C_TEAL, e -> {
            AVLNode n = engine.searchRoot(inp.getText().trim());
            if (n == null) { rootOut.setText("[X] Introuvable."); return; }
            rootOut.setText("[OK] " + n.root
                + "\nDerives : " + n.derivedWords.size()
                + "\nFrequence : " + n.frequency
                + (n.derivedWords.isEmpty() ? "" : "\nMots : " + String.join(" | ", n.derivedWords)));
        }));
        left.add(box(6));
        // Supprimer
        left.add(btn("Supprimer", C_ERR, e -> {
            boolean ok = engine.deleteRoot(inp.getText().trim());
            rootOut.setText(ok ? "[OK] Supprimee." : "[X] Introuvable.");
            refreshRoots();
        }));
        left.add(box(6));
        // Derives valides
        left.add(btn("Voir derives valides", C_GOLD, e -> {
            AVLNode n = engine.searchRoot(inp.getText().trim());
            if (n == null) { rootOut.setText("[X] Introuvable."); return; }
            StringBuilder sb = new StringBuilder("Derives pour " + n.root + " :\n");
            if (n.derivedWords.isEmpty()) sb.append("  (aucun)\n");
            else n.derivedWords.forEach(w -> sb.append("  - ").append(w).append("\n"));
            sb.append("Frequence : ").append(n.frequency);
            rootOut.setText(sb.toString());
        }));
        left.add(box(14));
        left.add(sec("RESULTAT")); left.add(box(5));
        rootOut = outArea();
        JScrollPane outScroll = new JScrollPane(rootOut);
        outScroll.setBorder(null);
        outScroll.setPreferredSize(new Dimension(0, 150));
        outScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        left.add(outScroll);

        // ── Colonne droite : table + arbre ─────────────────────────────────────
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);

        // Table des racines
        String[] cols = {"#", "Racine", "Hauteur", "Derives", "Freq."};
        rootsModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl = styledTable(rootsModel);
        tbl.getColumnModel().getColumn(1).setCellRenderer(arRenderer());
        tbl.getColumnModel().getColumn(0).setPreferredWidth(30);
        refreshRoots();

        JPanel tableSection = new JPanel(new BorderLayout(0, 5));
        tableSection.setOpaque(false);
        JPanel tRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tRow.setOpaque(false);
        tRow.add(sec("RACINES INDEXEES")); tRow.add(Box.createHorizontalStrut(10));
        tRow.add(btn("Actualiser", C_TEAL, e -> refreshRoots()));
        tableSection.add(tRow, BorderLayout.NORTH);
        tableSection.add(new JScrollPane(tbl) {{ setBorder(BorderFactory.createLineBorder(C_BORDER)); }}, BorderLayout.CENTER);
        tableSection.setPreferredSize(new Dimension(0, 240));

        // Canvas AVL scrollable — prend tout l'espace restant
        avlCanvas = new AVLCanvas(engine.getRootTree());
        JScrollPane avlScroll = new JScrollPane(avlCanvas,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        avlScroll.setBorder(BorderFactory.createLineBorder(C_BORDER));
        avlScroll.getViewport().setBackground(new Color(14, 16, 22));

        JPanel avlSection = new JPanel(new BorderLayout(0, 5));
        avlSection.setOpaque(false);
        JPanel avlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avlRow.setOpaque(false);
        avlRow.add(sec("VISUALISATION ARBRE AVL (scrollable)")); avlRow.add(Box.createHorizontalStrut(10));
        avlRow.add(btn("Actualiser", C_TEAL, e -> { avlCanvas.rebuild(); avlScroll.revalidate(); }));
        avlSection.add(avlRow,   BorderLayout.NORTH);
        avlSection.add(avlScroll, BorderLayout.CENTER);

        right.add(tableSection, BorderLayout.NORTH);
        right.add(avlSection,   BorderLayout.CENTER);

        p.add(panTitle("Gestion des Racines — Arbre AVL"), BorderLayout.NORTH);
        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);
        return p;
    }

    /** Met a jour la table ET le canvas en meme temps */
    private void refreshRoots() {
        rootsModel.setRowCount(0);
        List<String> roots = engine.getAllRoots();
        for (int i = 0; i < roots.size(); i++) {
            AVLNode n = engine.getRootTree().search(roots.get(i));
            rootsModel.addRow(new Object[]{
                i + 1, roots.get(i),
                n != null ? n.height : "-",
                n != null ? n.derivedWords.size() : 0,   // mis a jour en temps reel
                n != null ? n.frequency : 0               // mis a jour en temps reel
            });
        }
        if (avlCanvas != null) { avlCanvas.rebuild(); avlCanvas.revalidate(); }
    }

    // ═══════════════════════════════════ SCHEMES ══════════════════════════════

    private JPanel buildSchemesPanel() {
        JPanel p = dark(new BorderLayout(14, 14));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Formulaire ────────────────────────────────────────────────────────
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(285, 0));

        left.add(sec("NOM DU SCHEME")); left.add(box(5));
        JTextField sName = arField(); left.add(sName); left.add(box(10));

        left.add(sec("SQUELETTE  (1=C1  2=C2  3=C3 + lettres fixes)")); left.add(box(5));
        JTextField sSkel = arField();
        sSkel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        left.add(sSkel); left.add(box(3));
        JLabel hint = new JLabel("  Ex: pour فاعل → tapez: 1\u0627 23");
        hint.setFont(new Font("Monospaced", Font.PLAIN, 10)); hint.setForeground(C_MUTED);
        left.add(hint); left.add(box(10));

        left.add(sec("CATEGORIE")); left.add(box(5));
        JTextField sCat = new JTextField(); styleInp(sCat); left.add(sCat); left.add(box(10));

        left.add(sec("DESCRIPTION")); left.add(box(5));
        JTextField sDesc = new JTextField(); styleInp(sDesc); left.add(sDesc); left.add(box(14));

        schemeOut = outArea();

        left.add(btn("Ajouter / Modifier", C_OK, e -> {
            String name = sName.getText().trim(), skelStr = sSkel.getText().trim();
            if (name.isEmpty() || skelStr.isEmpty()) { schemeOut.setText("[X] Nom et squelette requis."); return; }
            List<Object> skel = new ArrayList<>();
            for (char c : skelStr.toCharArray()) {
                if (c == '1') skel.add(1);
                else if (c == '2') skel.add(2);
                else if (c == '3') skel.add(3);
                else skel.add(c);
            }
            engine.addOrUpdateScheme(name, skel.toArray(), sCat.getText().trim(), sDesc.getText().trim());
            schemeOut.setText("[OK] Scheme '" + name + "' sauvegarde.");
            refreshSchemes();
        }));
        left.add(box(6));
        left.add(btn("Supprimer", C_ERR, e -> {
            boolean ok = engine.deleteScheme(sName.getText().trim());
            schemeOut.setText(ok ? "[OK] Supprime." : "[X] Introuvable.");
            refreshSchemes();
        }));
        left.add(box(12));
        left.add(sec("RESULTAT")); left.add(box(5));
        JScrollPane os = new JScrollPane(schemeOut); os.setBorder(null);
        os.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        left.add(os);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"Scheme", "Squelette", "Categorie", "Description", "Bucket#"};
        schemesModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl = styledTable(schemesModel);
        tbl.getColumnModel().getColumn(0).setCellRenderer(arRenderer());
        tbl.getColumnModel().getColumn(0).setPreferredWidth(80);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(220);
        refreshSchemes();

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setOpaque(false);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(sec("SCHEMES DANS LA TABLE DE HACHAGE")); row.add(Box.createHorizontalStrut(10));
        row.add(btn("Actualiser", C_TEAL, e -> refreshSchemes()));
        right.add(row, BorderLayout.NORTH);
        right.add(new JScrollPane(tbl) {{ setBorder(BorderFactory.createLineBorder(C_BORDER)); }}, BorderLayout.CENTER);

        p.add(panTitle("Gestion des Schemes — Table de Hachage"), BorderLayout.NORTH);
        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);
        return p;
    }

    private void refreshSchemes() {
        schemesModel.setRowCount(0);
        List<Scheme> list = engine.getAllSchemes();
        list.sort(Comparator.comparing(Scheme::getName));
        for (Scheme s : list)
            schemesModel.addRow(new Object[]{
                s.getName(), s.getSkeletonStr(),
                s.getCategory(), s.getDescription(),
                engine.getSchemeTable().bucketIndexOf(s.getName())
            });
    }

    // ═══════════════════════════════════ GENERATION ════════════════════════════

    private JPanel buildGenPanel() {
        JPanel p = dark(new BorderLayout(14, 14));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Barre superieure ──────────────────────────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);
        topBar.add(sec("RACINE :"));
        JTextField rootF = arField(); rootF.setPreferredSize(new Dimension(150, 34));
        topBar.add(rootF);

        // IMPORTANT : on genere UNIQUEMENT si la racine est dans l'arbre AVL
        topBar.add(btn("Tous les schemes", C_GOLD, e -> {
            String raw = rootF.getText().trim();
            String cleaned = engine.cleanRoot(raw);
            if (engine.searchRoot(cleaned) == null) {
                JOptionPane.showMessageDialog(this,
                    "La racine '" + (cleaned.isEmpty() ? raw : cleaned) + "' n'existe pas dans l'arbre AVL.\n"
                    + "Inserez-la d'abord dans l'onglet Racines.",
                    "Racine introuvable", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Map<String, String> d = engine.generateAllDerivatives(cleaned);
            genModel.setRowCount(0);
            for (Map.Entry<String, String> en : d.entrySet()) {
                Scheme s = engine.getSchemeTable().get(en.getKey());
                genModel.addRow(new Object[]{en.getKey(), en.getValue(),
                    s != null ? s.getSkeletonStr() : "-",
                    s != null ? s.getCategory()    : "-"});
            }
        }));
        topBar.add(btn("Enregistrer derives", C_TEAL, e -> {
            String cleaned = engine.cleanRoot(rootF.getText().trim());
            if (genModel.getRowCount() == 0) return;
            AVLNode node = engine.getRootTree().search(cleaned);
            if (node == null) { JOptionPane.showMessageDialog(this,"Racine introuvable dans l'arbre."); return; }
            for (int i = 0; i < genModel.getRowCount(); i++)
                node.addDerivedWord((String) genModel.getValueAt(i, 1));
            refreshRoots(); // met a jour derives + frequence dans la table
            JOptionPane.showMessageDialog(this, "[OK] " + genModel.getRowCount() + " derives enregistres pour " + cleaned);
        }));

        // ── Checkboxes des schemes ─────────────────────────────────────────────
        JPanel checkWrap = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 4));
        checkWrap.setBackground(C_SURFACE);
        checkWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        List<JCheckBox> boxes = new ArrayList<>();
        for (Scheme s : engine.getAllSchemes()) {
            JCheckBox cb = new JCheckBox(s.getName());
            cb.setFont(F_AR_SM); cb.setForeground(C_TEXT);
            cb.setBackground(C_SURFACE); cb.setSelected(true);
            boxes.add(cb); checkWrap.add(cb);
        }

        JPanel selRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selRow.setOpaque(false);
        selRow.add(btn("Generer selectionnes", new Color(140, 80, 200), e -> {
            String cleaned = engine.cleanRoot(rootF.getText().trim());
            if (engine.searchRoot(cleaned) == null) {
                JOptionPane.showMessageDialog(this,
                    "La racine '" + cleaned + "' n'est pas dans l'arbre AVL.\nInserez-la d'abord.",
                    "Racine introuvable", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<String> sel = new ArrayList<>();
            for (JCheckBox cb : boxes) if (cb.isSelected()) sel.add(cb.getText());
            Map<String, String> d = engine.generateSelectedDerivatives(cleaned, sel);
            genModel.setRowCount(0);
            for (Map.Entry<String, String> en : d.entrySet()) {
                Scheme s = engine.getSchemeTable().get(en.getKey());
                genModel.addRow(new Object[]{en.getKey(), en.getValue(),
                    s != null ? s.getSkeletonStr() : "-",
                    s != null ? s.getCategory()    : "-"});
            }
        }));
        selRow.add(btn("Tout cocher",   C_MUTED, e -> boxes.forEach(cb -> cb.setSelected(true))));
        selRow.add(btn("Tout decocher", C_MUTED, e -> boxes.forEach(cb -> cb.setSelected(false))));

        // ── Table des resultats ────────────────────────────────────────────────
        String[] cols = {"Scheme", "Mot Derive", "Squelette", "Categorie"};
        genModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl = styledTable(genModel);
        tbl.getColumnModel().getColumn(0).setCellRenderer(arRenderer());
        tbl.getColumnModel().getColumn(1).setCellRenderer(arRendererLg());
        tbl.getColumnModel().getColumn(1).setPreferredWidth(200);

        JPanel schSection = new JPanel(new BorderLayout(0, 6));
        schSection.setOpaque(false);
        schSection.add(sec("SCHEMES DISPONIBLES"), BorderLayout.NORTH);
        schSection.add(checkWrap, BorderLayout.CENTER);
        schSection.add(selRow,    BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(schSection, BorderLayout.NORTH);
        center.add(new JScrollPane(tbl) {{ setBorder(BorderFactory.createLineBorder(C_BORDER)); }}, BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(panTitle("Generation Morphologique — racine doit exister dans l'AVL"), BorderLayout.NORTH);
        north.add(topBar, BorderLayout.CENTER);

        p.add(north,  BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════ VALIDATION ════════════════════════════

    private JPanel buildValidPanel() {
        JPanel p = dark(new BorderLayout(14, 14));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(310, 0));

        left.add(sec("MOT ARABE A VERIFIER")); left.add(box(5));
        JTextField wordF = arField(); left.add(wordF); left.add(box(12));
        left.add(sec("RACINE PRESUMEE"));      left.add(box(5));
        JTextField rootF = arField(); left.add(rootF); left.add(box(14));

        JPanel banner = new JPanel(new BorderLayout());
        banner.setPreferredSize(new Dimension(0, 46));
        JLabel bannerLbl = new JLabel("", SwingConstants.CENTER);
        bannerLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        banner.add(bannerLbl, BorderLayout.CENTER);
        banner.setVisible(false);

        JTextArea valOut = outArea(); valOut.setRows(10);

        left.add(btn("Valider", C_GOLD, e -> {
            String w = wordF.getText().trim(), r = rootF.getText().trim();
            if (w.isEmpty() || r.isEmpty()) { valOut.setText("Remplissez les deux champs."); return; }
            ValidationResult res = engine.validateMorphology(w, r);
            // La validation met a jour automatiquement derives+frequence dans le noeud AVL
            refreshRoots(); // repercuter dans la table
            banner.setVisible(true);
            if (res.isValid()) {
                banner.setBackground(new Color(30, 70, 40));
                bannerLbl.setForeground(C_OK);
                bannerLbl.setText("OUI  —  appartient a la racine");
            } else {
                banner.setBackground(new Color(70, 25, 25));
                bannerLbl.setForeground(C_ERR);
                bannerLbl.setText("NON  —  aucune correspondance");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Mot      : ").append(w).append("\n");
            sb.append("Racine   : ").append(r).append("\n");
            sb.append("-".repeat(38)).append("\n");
            sb.append("Resultat : ").append(res.isValid() ? "OUI" : "NON").append("\n");
            if (res.isValid() && res.getScheme() != null) {
                sb.append("Scheme   : ").append(res.getScheme().getName()).append("\n");
                sb.append("Categorie: ").append(res.getScheme().getCategory()).append("\n");
                sb.append("Squelette: ").append(res.getScheme().getSkeletonStr()).append("\n");
            }
            sb.append("Message  : ").append(res.getMessage());
            valOut.setText(sb.toString());
        }));
        left.add(box(6));
        left.add(btn("Effacer", C_MUTED, e -> {
            wordF.setText(""); rootF.setText("");
            valOut.setText(""); banner.setVisible(false);
        }));
        left.add(box(12));
        left.add(banner); left.add(box(8));
        left.add(sec("DETAIL")); left.add(box(5));
        left.add(new JScrollPane(valOut) {{ setBorder(null); }});

        // ── Exemples ──────────────────────────────────────────────────────────
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);
        right.add(sec("EXEMPLES — cliquez pour auto-remplir"), BorderLayout.NORTH);

        String[][] ex = {
            {"\u0643\u0627\u062a\u0628",  "\u0643\u062a\u0628", "OUI"},
            {"\u0645\u0643\u062a\u0648\u0628", "\u0643\u062a\u0628", "OUI"},
            {"\u0643\u062a\u0627\u0628",  "\u0643\u062a\u0628", "OUI"},
            {"\u0645\u0643\u062a\u0628",  "\u0643\u062a\u0628", "OUI"},
            {"\u062f\u0627\u0631\u0633",  "\u062f\u0631\u0633", "OUI"},
            {"\u0639\u0627\u0644\u0645",  "\u0639\u0644\u0645", "OUI"},
            {"\u0646\u0627\u0635\u0631",  "\u0646\u0635\u0631", "OUI"},
            {"\u0643\u0627\u062a\u0628",  "\u062f\u0631\u0633", "NON"},
        };
        DefaultTableModel em = new DefaultTableModel(new String[]{"Mot","Racine","Attendu"}, 0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        for (String[] row : ex) em.addRow(row);
        JTable etbl = styledTable(em);
        etbl.getColumnModel().getColumn(0).setCellRenderer(arRenderer());
        etbl.getColumnModel().getColumn(1).setCellRenderer(arRenderer());
        etbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = etbl.getSelectedRow(); if (row < 0) return;
                wordF.setText((String) em.getValueAt(row, 0));
                rootF.setText((String) em.getValueAt(row, 1));
            }
        });
        right.add(new JScrollPane(etbl) {{ setBorder(BorderFactory.createLineBorder(C_BORDER)); }}, BorderLayout.CENTER);
        right.add(new JLabel("  Cliquez sur une ligne, puis sur Valider.") {{
            setFont(F_MONO); setForeground(C_MUTED); }}, BorderLayout.SOUTH);

        p.add(panTitle("Validation Morphologique"), BorderLayout.NORTH);
        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════ ANALYSE ═══════════════════════════════

    private JPanel buildAnalyzePanel() {
        JPanel p = dark(new BorderLayout(14, 14));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);
        topBar.add(sec("MOT :"));
        JTextField wordF = arField(); wordF.setPreferredSize(new Dimension(180, 34));
        topBar.add(wordF);
        topBar.add(btn("Analyser", C_GOLD, e -> runAnalyze(wordF.getText().trim())));

        // Boutons exemples rapides
        JPanel exRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 4));
        exRow.setOpaque(false);
        exRow.add(new JLabel("Exemples :") {{ setFont(F_MONO); setForeground(C_MUTED); }});
        String[] exW = {"\u0643\u0627\u062a\u0628","\u0645\u0643\u062a\u0648\u0628",
                         "\u0643\u062a\u0627\u0628","\u0645\u0643\u062a\u0628",
                         "\u062f\u0627\u0631\u0633","\u0639\u0627\u0644\u0645"};
        for (String w : exW) {
            JButton eb = new JButton(w);
            eb.setFont(F_AR_SM); eb.setForeground(C_TEAL);
            eb.setBackground(C_SURFACE); eb.setBorder(BorderFactory.createLineBorder(C_TEALD));
            eb.setFocusPainted(false);
            eb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eb.addActionListener(ev -> { wordF.setText(w); runAnalyze(w); });
            exRow.add(eb);
        }

        String[] cols = {"Scheme","Racine Extraite","Dans AVL","Categorie"};
        anaModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl = styledTable(anaModel);
        tbl.getColumnModel().getColumn(0).setCellRenderer(arRenderer());
        tbl.getColumnModel().getColumn(1).setCellRenderer(arRendererLg());
        tbl.getColumnModel().getColumn(1).setPreferredWidth(130);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(panTitle("Analyse Morphologique — Decomposition d'un Mot"), BorderLayout.NORTH);
        north.add(topBar, BorderLayout.CENTER);
        north.add(exRow,  BorderLayout.SOUTH);

        p.add(north, BorderLayout.NORTH);
        p.add(new JScrollPane(tbl) {{ setBorder(BorderFactory.createLineBorder(C_BORDER)); }}, BorderLayout.CENTER);
        return p;
    }

    private void runAnalyze(String word) {
        anaModel.setRowCount(0);
        List<ValidationResult> matches = engine.analyzeWord(word);
        if (matches.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune decomposition trouvee pour ce mot.", "Analyse", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (ValidationResult vr : matches) {
            boolean inTree = engine.searchRoot(vr.getExtractedRoot()) != null;
            anaModel.addRow(new Object[]{
                vr.getScheme().getName(), vr.getExtractedRoot(),
                inTree ? "Oui" : "Non", vr.getScheme().getCategory()
            });
        }
    }

    // ═══════════════════════════════════ AVL CANVAS ════════════════════════════

    /**
     * Canvas qui dessine l'arbre AVL complet.
     * Sa taille prefere est calculee depuis les positions reelles des noeuds,
     * donc le JScrollPane peut afficher tout l'arbre meme s'il est grand.
     */
    private static class AVLCanvas extends JPanel {
        private final AVLTree tree;
        // positions calculees au moment du repaint / rebuild
        private Map<AVLNode, Point> positions = new LinkedHashMap<>();
        private int canvasW = 800, canvasH = 300;
        private static final int R  = 20;  // rayon des noeuds
        private static final int DX = 50;  // espacement horizontal
        private static final int DY = 60;  // espacement vertical
        private static final int PAD = 30; // marge

        AVLCanvas(AVLTree tree) {
            this.tree = tree;
            setBackground(new Color(14, 16, 22));
            rebuild();
        }

        /** Recalcule les positions de tous les noeuds, puis ajuste la taille preferee. */
        void rebuild() {
            positions = new LinkedHashMap<>();
            int[] xIdx = {0};
            assignPos(tree.getRoot(), 0, xIdx);
            // taille du canvas = positions max + marges
            int maxX = positions.values().stream().mapToInt(pt -> pt.x).max().orElse(PAD) + PAD + R;
            int maxY = positions.values().stream().mapToInt(pt -> pt.y).max().orElse(PAD) + PAD + R;
            canvasW = Math.max(maxX, 400);
            canvasH = Math.max(maxY, 250);
            setPreferredSize(new Dimension(canvasW, canvasH));
            repaint();
        }

        private void assignPos(AVLNode n, int depth, int[] xIdx) {
            if (n == null) return;
            assignPos(n.left, depth + 1, xIdx);
            positions.put(n, new Point(PAD + xIdx[0] * DX, PAD + depth * DY));
            xIdx[0]++;
            assignPos(n.right, depth + 1, xIdx);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (tree.getRoot() == null) {
                g.setColor(new Color(80,80,80));
                g.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g.drawString("(arbre vide)", 20, 30);
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Aretes
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new Color(212, 175, 82, 55));
            for (Map.Entry<AVLNode, Point> e : positions.entrySet()) {
                AVLNode n = e.getKey(); Point pt = e.getValue();
                if (n.left  != null && positions.containsKey(n.left))  { Point c=positions.get(n.left);  g2.drawLine(pt.x,pt.y,c.x,c.y); }
                if (n.right != null && positions.containsKey(n.right)) { Point c=positions.get(n.right); g2.drawLine(pt.x,pt.y,c.x,c.y); }
            }

            // Noeuds
            for (Map.Entry<AVLNode, Point> e : positions.entrySet()) {
                AVLNode n = e.getKey(); Point pt = e.getValue();
                boolean isRoot = (n == tree.getRoot());
                Color fillColor = isRoot ? new Color(212,175,82) : new Color(56,178,172);

                // Halo
                g2.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 35));
                g2.fillOval(pt.x-R-4, pt.y-R-4, (R+4)*2, (R+4)*2);
                // Cercle
                g2.setColor(fillColor);
                g2.fillOval(pt.x-R, pt.y-R, R*2, R*2);
                // Texte arabe
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.setColor(new Color(18,20,26));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(n.root, pt.x - fm.stringWidth(n.root)/2, pt.y + fm.getAscent()/2 - 1);
                // Hauteur (petite etiquette)
                g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
                g2.setColor(new Color(140,140,120));
                g2.drawString("h="+n.height, pt.x - R, pt.y + R + 11);
            }
        }
    }

    // ═══════════════════════════════════ UI HELPERS ════════════════════════════

    private JPanel dark(LayoutManager lm) { JPanel p=new JPanel(lm); p.setBackground(C_BG); return p; }

    private JLabel panTitle(String t) {
        JLabel l = new JLabel(t); l.setFont(F_TTL); l.setForeground(C_GOLD);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,C_GOLDD),
            BorderFactory.createEmptyBorder(0,0,10,0)
        ));
        return l;
    }

    private JLabel sec(String t) {
        JLabel l = new JLabel(t); l.setFont(F_HDR); l.setForeground(C_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }

    private Component box(int h) { return Box.createVerticalStrut(h); }

    private JTextField arField() {
        JTextField f = new JTextField();
        f.setFont(F_AR_MD);
        f.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        styleInp(f);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return f;
    }

    private void styleInp(JTextField f) {
        f.setBackground(C_SURFACE); f.setForeground(C_TEXT); f.setCaretColor(C_GOLD);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            BorderFactory.createEmptyBorder(5,10,5,10)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private JTextArea outArea() {
        JTextArea a = new JTextArea(5, 0);
        a.setFont(F_MONO); a.setBackground(new Color(14,16,22));
        a.setForeground(new Color(150,220,150)); a.setCaretColor(C_GOLD);
        a.setEditable(false); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        return a;
    }

    private JButton btn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(F_BOLD); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (al != null) b.addActionListener(al);
        return b;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(F_UI); t.setForeground(C_TEXT); t.setBackground(C_SURFACE);
        t.setSelectionBackground(new Color(56,178,172,80)); t.setSelectionForeground(C_TEXT);
        t.setRowHeight(30); t.setShowGrid(true); t.setGridColor(C_BORDER);
        t.getTableHeader().setFont(F_HDR);
        t.getTableHeader().setBackground(new Color(22,24,32)); t.getTableHeader().setForeground(C_MUTED);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_BORDER));
        return t;
    }

    private TableCellRenderer arRenderer() {
        return new DefaultTableCellRenderer() {
            { setFont(F_AR_MD); setHorizontalAlignment(CENTER); }
            public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c) {
                super.getTableCellRendererComponent(t,v,s,f,r,c);
                setBackground(s ? new Color(56,178,172,80) : C_SURFACE);
                setForeground(C_TEXT);
                setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                return this;
            }
        };
    }

    private TableCellRenderer arRendererLg() {
        return new DefaultTableCellRenderer() {
            { setFont(F_AR_LG); setHorizontalAlignment(CENTER); }
            public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c) {
                super.getTableCellRendererComponent(t,v,s,f,r,c);
                setBackground(s ? new Color(56,178,172,80) : C_SURFACE);
                setForeground(C_GOLD);
                setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                return this;
            }
        };
    }

    /** Layout qui retourne a la ligne automatiquement */
    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container t) { return layoutSize(t); }
        @Override public Dimension minimumLayoutSize(Container t)   { return layoutSize(t); }
        private Dimension layoutSize(Container target) {
            synchronized (target.getTreeLock()) {
                int tw = target.getSize().width; if (tw==0) tw=Integer.MAX_VALUE;
                int inset = getInsets(target).left + getInsets(target).right + getHgap()*2;
                tw -= inset;
                Dimension dim = new Dimension(0,0);
                int rw=0,rh=0;
                for (int i=0;i<target.getComponentCount();i++) {
                    Component m=target.getComponent(i); if(!m.isVisible()) continue;
                    Dimension d=m.getPreferredSize();
                    if(rw+d.width>tw){dim.width=Math.max(dim.width,rw);dim.height+=rh+getVgap();rw=0;rh=0;}
                    if(rw>0)rw+=getHgap(); rw+=d.width; rh=Math.max(rh,d.height);
                }
                dim.width=Math.max(dim.width,rw); dim.height+=rh;
                Insets ins=getInsets(target);
                dim.width+=ins.left+ins.right+getHgap()*2; dim.height+=ins.top+ins.bottom+getVgap()*2;
                return dim;
            }
        }
        private Insets getInsets(Container t){return t.getInsets()==null?new Insets(0,0,0,0):t.getInsets();}
    }
}
