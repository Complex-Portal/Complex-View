package uk.ac.ebi.intact.service.complex.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 22/01/14
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ComplexDetailsFeatures {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private String participantId;
    private String featureType;
    private String featureTypeMI;
    private String featureTypeDefinition;
    private Collection<String> ranges;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexDetailsFeatures() {
        this.participantId = null;
        this.featureType = null;
        this.featureTypeMI = null;
        this.featureTypeDefinition = null;
        this.ranges = new ArrayList<String>();
    }

    public ComplexDetailsFeatures(String participantId, String featureType, String featureTypeMI, String featureTypeDefinition, Collection<String> ranges) {
        this.participantId = participantId;
        this.featureType = featureType;
        this.featureTypeMI = featureTypeMI;
        this.featureTypeDefinition = featureTypeDefinition;
        this.ranges = ranges;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    @XmlElement
    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }
    @XmlElement
    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }
    @XmlElement
    public String getFeatureTypeMI() {
        return featureTypeMI;
    }

    public void setFeatureTypeMI(String featureTypeMI) {
        this.featureTypeMI = featureTypeMI;
    }
    @XmlElement
    public String getFeatureTypeDefinition() {
        return featureTypeDefinition;
    }

    public void setFeatureTypeDefinition(String featureTypeDefinition) {
        this.featureTypeDefinition = featureTypeDefinition;
    }
    @XmlElement
    public Collection<String> getRanges() {
        return ranges;
    }

}
