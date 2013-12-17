package uk.ac.ebi.intact.service.complex.ws;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private String ac;
    private String name;
    private String specie;
    private List<String> componentsAC;
    private List<String> componentsName;

    public ComplexDetails() {
        this.synonyms = new LinkedList<String>();
        this.componentsAC = new LinkedList<String>();
        this.componentsName = new LinkedList<String>();
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
    public void setAc ( String id ) { this.ac = id; }
    @XmlElement
    public String getAc () { return this.ac; }
    public void setName ( String n ) { this.name = n; }
    @XmlElement
    public String getName () { return this.name; }
    public void setSpecie ( String s ) { this.specie = s; }
    @XmlElement
    public String getSpecie () { return this.specie; }
    public void setComponentsAC (List<String> list) { this.componentsAC = list; }
    public void addComponentAC(String value) { this.componentsAC.add(value); }
    @XmlElement
    public List<String> getComponentsAC() { return this.componentsAC; }
    public void setComponentsName (List<String> list) { this.componentsName = list; }
    public void addComponentName(String value) { this.componentsName.add(value); }
    @XmlElement
    public List<String> getComponentsName() { return this.componentsName; }

}
