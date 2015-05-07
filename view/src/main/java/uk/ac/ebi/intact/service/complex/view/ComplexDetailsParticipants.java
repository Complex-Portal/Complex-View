package uk.ac.ebi.intact.service.complex.view;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 21/01/14
 */
public class ComplexDetailsParticipants {
    private String interactorAC;
    private String identifier;
    private String identifierLink;
    private String name;
    private String description;
    private String stochiometry;
    private String bioRole;
    private String bioRoleMI;
    private String bioRoleDefinition;
    private String interactorType;
    private String interactorTypeMI;
    private String interactorTypeDefinition;
    private List<ComplexDetailsFeatures> linkedFeatures;
    private List<ComplexDetailsFeatures> otherFeatures;

    public ComplexDetailsParticipants() {
        this.identifier     = null;
        this.identifierLink = null;
        this.name           = null;
        this.description    = null;
        this.stochiometry   = null;
        this.bioRole        = null;
        this.interactorType = null;
        this.linkedFeatures = new LinkedList<ComplexDetailsFeatures>();
        this.otherFeatures  = new LinkedList<ComplexDetailsFeatures>();
    }

    public ComplexDetailsParticipants(String identifier,
                                      String identifierLink,
                                      String name,
                                      String description,
                                      String stochiometry,
                                      String bioRole,
                                      String interactorType,
                                      List<ComplexDetailsFeatures> linkedFeatures,
                                      List<ComplexDetailsFeatures> otherFeatures) {
        this.identifier = identifier;
        this.identifierLink = identifierLink;
        this.name = name;
        this.description = description;
        this.stochiometry = stochiometry;
        this.bioRole = bioRole;
        this.interactorType = interactorType;
        this.linkedFeatures = linkedFeatures;
        this.otherFeatures = otherFeatures;
    }
    
    public String getBioRoleMI() {
        return bioRoleMI;
    }

    public void setBioRoleMI(String bioRoleMI) {
        this.bioRoleMI = bioRoleMI;
    }
    
    public String getBioRoleDefinition() {
        return bioRoleDefinition;
    }

    public void setBioRoleDefinition(String bioRoleDefinition) {
        this.bioRoleDefinition = bioRoleDefinition;
    }
    
    public String getInteractorTypeMI() {
        return interactorTypeMI;
    }

    public void setInteractorTypeMI(String interactorTypeMI) {
        this.interactorTypeMI = interactorTypeMI;
    }
    
    public String getInteractorTypeDefinition() {
        return interactorTypeDefinition;
    }

    public void setInteractorTypeDefinition(String interactorTypeDefinition) {
        this.interactorTypeDefinition = interactorTypeDefinition;
    }
    
    public String getInteractorAC() {
        return interactorAC;
    }

    public void setInteractorAC(String interactorAC) {
        this.interactorAC = interactorAC;
    }
    
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getIdentifierLink() {
        return identifierLink;
    }

    public void setIdentifierLink(String identifierLink) {
        this.identifierLink = identifierLink;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStochiometry() {
        return stochiometry;
    }

    public void setStochiometry(String stochiometry) {
        this.stochiometry = stochiometry;
    }
    
    public String getBioRole() {
        return bioRole;
    }

    public void setBioRole(String bioRole) {
        this.bioRole = bioRole;
    }
    
    public String getInteractorType() {
        return interactorType;
    }

    public void setInteractorType(String interactorType) {
        this.interactorType = interactorType;
    }
    
    public List<ComplexDetailsFeatures> getLinkedFeatures() {
        return linkedFeatures;
    }
    
    public List<ComplexDetailsFeatures> getOtherFeatures() {
        return otherFeatures;
    }

}
