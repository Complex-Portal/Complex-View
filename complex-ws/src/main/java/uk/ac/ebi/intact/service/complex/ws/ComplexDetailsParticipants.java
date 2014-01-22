package uk.ac.ebi.intact.service.complex.ws;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 21/01/14
 */
public class ComplexDetailsParticipants {
    private String identifier;
    private String identifierLink;
    private String geneName;
    private String proteinName;
    private String stochiometry;
    private String bioRole;
    private String interactorType;
    private List<String> linkedFeatures;
    private List<String> otherFeatures;

    public ComplexDetailsParticipants() {
        this.identifier     = null;
        this.identifierLink = null;
        this.geneName       = null;
        this.proteinName    = null;
        this.stochiometry   = null;
        this.bioRole        = null;
        this.interactorType = null;
        this.linkedFeatures = new LinkedList<String>();
        this.otherFeatures  = new LinkedList<String>();
    }

    public ComplexDetailsParticipants(String identifier,
                                      String identifierLink,
                                      String geneName,
                                      String proteinName,
                                      String stochiometry,
                                      String bioRole,
                                      String interactorType,
                                      List<String> linkedFeatures,
                                      List<String> otherFeatures) {
        this.identifier = identifier;
        this.identifierLink = identifierLink;
        this.geneName = geneName;
        this.proteinName = proteinName;
        this.stochiometry = stochiometry;
        this.bioRole = bioRole;
        this.interactorType = interactorType;
        this.linkedFeatures = linkedFeatures;
        this.otherFeatures = otherFeatures;
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

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getProteinName() {
        return proteinName;
    }

    public void setProteinName(String proteinName) {
        this.proteinName = proteinName;
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

    public List<String> getLinkedFeatures() {
        return linkedFeatures;
    }

    public void setLinkedFeatures(List<String> linkedFeatures) {
        this.linkedFeatures = linkedFeatures;
    }

    public List<String> getOtherFeatures() {
        return otherFeatures;
    }

    public void setOtherFeatures(List<String> otherFeatures) {
        this.otherFeatures = otherFeatures;
    }
}
