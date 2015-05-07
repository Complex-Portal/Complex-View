package uk.ac.ebi.intact.service.complex.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 21/01/14
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ComplexDetailsParticipants {
    /********************************/
    /*      Private attributes      */
    /********************************/
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
    private Collection<ComplexDetailsFeatures> linkedFeatures;
    private Collection<ComplexDetailsFeatures> otherFeatures;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexDetailsParticipants() {
        this.identifier     = null;
        this.identifierLink = null;
        this.name           = null;
        this.description    = null;
        this.stochiometry   = null;
        this.bioRole        = null;
        this.interactorType = null;
        this.linkedFeatures = new ArrayList<ComplexDetailsFeatures>();
        this.otherFeatures  = new ArrayList<ComplexDetailsFeatures>();
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

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    @XmlElement
    public String getBioRoleMI() {
        return bioRoleMI;
    }

    public void setBioRoleMI(String bioRoleMI) {
        this.bioRoleMI = bioRoleMI;
    }
    @XmlElement
    public String getBioRoleDefinition() {
        return bioRoleDefinition;
    }

    public void setBioRoleDefinition(String bioRoleDefinition) {
        this.bioRoleDefinition = bioRoleDefinition;
    }
    @XmlElement
    public String getInteractorTypeMI() {
        return interactorTypeMI;
    }

    public void setInteractorTypeMI(String interactorTypeMI) {
        this.interactorTypeMI = interactorTypeMI;
    }
    @XmlElement
    public String getInteractorTypeDefinition() {
        return interactorTypeDefinition;
    }

    public void setInteractorTypeDefinition(String interactorTypeDefinition) {
        this.interactorTypeDefinition = interactorTypeDefinition;
    }
    @XmlElement
    public String getInteractorAC() {
        return interactorAC;
    }

    public void setInteractorAC(String interactorAC) {
        this.interactorAC = interactorAC;
    }
    @XmlElement
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    @XmlElement
    public String getIdentifierLink() {
        return identifierLink;
    }

    public void setIdentifierLink(String identifierLink) {
        this.identifierLink = identifierLink;
    }
    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    @XmlElement
    public String getStochiometry() {
        return stochiometry;
    }

    public void setStochiometry(String stochiometry) {
        this.stochiometry = stochiometry;
    }
    @XmlElement
    public String getBioRole() {
        return bioRole;
    }

    public void setBioRole(String bioRole) {
        this.bioRole = bioRole;
    }
    @XmlElement
    public String getInteractorType() {
        return interactorType;
    }

    public void setInteractorType(String interactorType) {
        this.interactorType = interactorType;
    }
    @XmlElement
    public Collection<ComplexDetailsFeatures> getLinkedFeatures() {
        return linkedFeatures;
    }
    @XmlElement
    public Collection<ComplexDetailsFeatures> getOtherFeatures() {
        return otherFeatures;
    }

}
