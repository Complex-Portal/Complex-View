package uk.ac.ebi.intact.service.complex.view;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 09/12/13
 */
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
   public String getSystematicName () { return this.systematicName; }
    public void setSynonyms ( List<String> syns ) { this.synonyms = syns; }
    public void addSynonym ( String syn ) { this.synonyms.add(syn); }
    public List<String> getSynonyms() { return this.synonyms; }
    public void setFunction ( String func ) { this.function = func; }
    public String getFunction () { return this.function; }
    public void setProperties ( String poper ) { this.properties = poper; }
    public String getProperties () { return this.properties; }
    public void setAc ( String id ) { this.ac = id; }
    public String getAc () { return this.ac; }
    public void setName ( String n ) { this.name = n; }
    public String getName () { return this.name; }
    public void setSpecie ( String s ) { this.specie = s; }
    public String getSpecie () { return this.specie; }
    public void setComponentsAC (List<String> list) { this.componentsAC = list; }
    public void addComponentAC(String value) { this.componentsAC.add(value); }
    public List<String> getComponentsAC() { return this.componentsAC; }
    public void setComponentsName (List<String> list) { this.componentsName = list; }
    public void addComponentName(String value) { this.componentsName.add(value); }
    public List<String> getComponentsName() { return this.componentsName; }

}
