package uk.ac.ebi.intact.service.complex.view;

import java.util.Collection;
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
    private Collection<ComplexDetailsParticipants> participants;
    private Collection<ComplexDetailsCrossReferences> crossReferences;

    public ComplexDetails() {
        this.synonyms = new LinkedList<String>();
        this.participants = new LinkedList<ComplexDetailsParticipants>();
        this.crossReferences = new LinkedList<ComplexDetailsCrossReferences>();
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
    public Collection<ComplexDetailsParticipants> getParticipants() { return participants; }
    public Collection<ComplexDetailsCrossReferences> getCrossReferences() { return crossReferences; }
}
