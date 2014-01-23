package uk.ac.ebi.intact.service.complex.view;


/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 21/01/14
 */
public class ComplexDetailsCrossReferences {
    private String database;
    private String qualifier;
    private String identifier;
    private String description;

    private String searchURL;
    private String dbMI;
    private String qualifierMI;
    private String dbdefinition;
    private String qualifierDefinition;

    public ComplexDetailsCrossReferences() {
        this.database    = null;
        this.qualifier   = null;
        this.identifier  = null;
        this.description = null;
    }

    public ComplexDetailsCrossReferences(String database, String qualifier, String identifier, String description) {
        this.database = database;
        this.qualifier = qualifier;
        this.identifier = identifier;
        this.description = description;
    }
    
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }
    
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSearchURL() {
        return searchURL;
    }

    public void setSearchURL(String searchURL) {
        this.searchURL = searchURL;
    }
    
    public String getDbMI() {
        return dbMI;
    }

    public void setDbMI(String dbMI) {
        this.dbMI = dbMI;
    }
    
    public String getQualifierMI() {
        return qualifierMI;
    }

    public void setQualifierMI(String qualifierMI) {
        this.qualifierMI = qualifierMI;
    }
    
    public String getDbdefinition() {
        return dbdefinition;
    }

    public void setDbdefinition(String dbdefinition) {
        this.dbdefinition = dbdefinition;
    }
    
    public String getQualifierDefinition() {
        return qualifierDefinition;
    }

    public void setQualifierDefinition(String qualifierDefinition) {
        this.qualifierDefinition = qualifierDefinition;
    }

}
