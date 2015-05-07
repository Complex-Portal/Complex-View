package uk.ac.ebi.intact.service.complex.view;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 22/01/14
 */
public class ComplexDetailsFeatures {
    private String participantId;
    private String featureType;
    private String featureTypeMI;
    private String featureTypeDefinition;
    private Collection<String> ranges;

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
    
    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }
    
    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }
    
    public String getFeatureTypeMI() {
        return featureTypeMI;
    }

    public void setFeatureTypeMI(String featureTypeMI) {
        this.featureTypeMI = featureTypeMI;
    }
    
    public String getFeatureTypeDefinition() {
        return featureTypeDefinition;
    }

    public void setFeatureTypeDefinition(String featureTypeDefinition) {
        this.featureTypeDefinition = featureTypeDefinition;
    }
    
    public Collection<String> getRanges() {
        return ranges;
    }

}
