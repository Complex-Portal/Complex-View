package uk.ac.ebi.intact.service.complex.ws;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 09/12/13
 */
@XmlRootElement(name = "ComplexDetails")
public class ComplexDetails {
    private String systematicName;
    private List<String> synonyms;
    private String function;
    private String properties;

    public ComplexDetails() {
        this.synonyms = new LinkedList<String>();
    }

    public void setSystematicName ( String systematic ) {
        this.systematicName = systematic;
    }
    @XmlElement
    public String getSystematicName () { return this.systematicName; }
    public void setSynonyms ( List<String> syns ) { this.synonyms = syns; }
    public void addSynonym ( String syn ) { this.synonyms.add(syn); }
    @XmlElement
    public List<String> getSynonyms() { return this.synonyms; }
    public void setFunction ( String func ) { this.function = func; }
    @XmlElement
    public String getFunction () { return this.function; }
    public void setProperties ( String poper ) { this.properties = poper; }
    @XmlElement
    public String getProperties () { return this.properties; }
}
