package morphology;

/**
 * Encapsule le resultat d'une validation morphologique.
 */
public class ValidationResult {

    private final boolean valid;
    private final Scheme  scheme;
    private final String  message;
    private       String  extractedRoot;

    public ValidationResult(boolean valid, Scheme scheme, String message) {
        this.valid   = valid;
        this.scheme  = scheme;
        this.message = message;
    }

    public boolean isValid()          { return valid; }
    public Scheme  getScheme()        { return scheme; }
    public String  getMessage()       { return message; }
    public String  getExtractedRoot() { return extractedRoot; }
    public void    setExtractedRoot(String r) { this.extractedRoot = r; }

    @Override
    public String toString() {
        if (!valid) return "NON — " + message;
        String info = "OUI — Scheme reconnu : " + scheme.getName()
                    + " (" + scheme.getCategory() + ")";
        if (extractedRoot != null) info += " | Racine extraite : " + extractedRoot;
        return info;
    }
}
