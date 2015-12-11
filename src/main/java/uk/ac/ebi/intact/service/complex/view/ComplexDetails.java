package uk.ac.ebi.intact.service.complex.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *  This class is to map the details of a complex retrieved from the DB
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 09/12/13
 */
@XmlRootElement(name = "ComplexDetails")
@XmlAccessorType(XmlAccessType.NONE)
public class ComplexDetails {

    /********************************/
    /*      Private attributes      */
    /********************************/
    private String systematicName;
    private Collection<String> synonyms;
    private Collection<String> functions;
    private Collection<String> properties;
    private String ac;
    private String name;
    private String species;
    private Collection<String> ligands;
    private Collection<String> complexAssemblies;
    private Collection<String> diseases;
    private Collection<ComplexDetailsParticipants> participants;
    private Collection<ComplexDetailsCrossReferences> crossReferences;
    private String institution;
    private Collection<String> agonists;
    private Collection<String> antagonists;
    private Collection<String> comments;



    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexDetails() {
        this.synonyms = new LinkedList<String>();
        this.participants = new ArrayList<ComplexDetailsParticipants>();
        this.crossReferences = new ArrayList<ComplexDetailsCrossReferences>();
        this.functions = new ArrayList<String>();
        this.properties = new ArrayList<String>();
        this.ligands = new ArrayList<String>();
        this.complexAssemblies = new ArrayList<String>();
        this.diseases = new ArrayList<String>();
        this.agonists = new ArrayList<String>();
        this.antagonists = new ArrayList<String>();
        this.comments = new ArrayList<String>();
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public void setSystematicName ( String systematic ) {
        this.systematicName = systematic;
    }
    @XmlElement
    public String getSystematicName () { return this.systematicName; }
    public void setSynonyms ( List<String> syns ) { this.synonyms = syns; }
    public void addSynonym ( String syn ) { this.synonyms.add(syn); }
    @XmlElement
    public Collection<String> getSynonyms() { return this.synonyms; }
    public void setFunctions(List<String> func) { this.functions = func; }
    public void addFunction(String function){
        functions.add(function);
    }
    @XmlElement
    public Collection<String> getFunctions() { return this.functions; }
    public void setProperties ( List<String> poper ) { this.properties = poper; }
    @XmlElement
    public Collection<String> getProperties () { return this.properties; }
    public void addProperty(String property){
        properties.add(property);
    }
    public void setAc ( String id ) { this.ac = id; }
    @XmlElement
    public String getAc () { return this.ac; }
    public void setName ( String n ) { this.name = n; }
    @XmlElement
    public String getName () { return this.name; }
    public void setSpecies ( String s ) { this.species = s; }
    @XmlElement
    public String getSpecies () { return this.species; }
    @XmlElement
    public Collection<String> getLigands() { return ligands; }
    public void setLigands(List<String> ligands) { this.ligands = ligands; }
    public void addLigand(String ligand){
        ligands.add(ligand);
    }
    @XmlElement
    public Collection<String> getComplexAssemblies() { return complexAssemblies; }
    public void setComplexAssemblies(List<String> complexAssemblies) { this.complexAssemblies = complexAssemblies; }
    public void addComplexAssembly(String complexAssembly){
        complexAssemblies.add(complexAssembly);
    }
    @XmlElement
    public Collection<String> getDiseases() { return diseases; }
    public void setDiseases(List<String> diseases) { this.diseases = diseases; }
    public void addDisease(String disease){
        diseases.add(disease);
    }
    public Collection<ComplexDetailsParticipants> getParticipants() {
        return participants;
    }
    public Collection<ComplexDetailsCrossReferences> getCrossReferences() {
        return crossReferences;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Collection<String> getAgonists() {
        return agonists;
    }

    public void setAgonists(Collection<String> agonists) {
        this.agonists = agonists;
    }
    
    public void addAgonist(String agonist){
        agonists.add(agonist);        
    }

    public Collection<String> getAntagonists() {
        return antagonists;
    }

    public void setAntagonists(Collection<String> antagonists) {
        this.antagonists = antagonists;
    }
    
    public void addAntagonist(String antagonist){
        this.antagonists.add(antagonist);        
    }

    public Collection<String> getComments() {
        return comments;
    }

    public void setComments(Collection<String> comments) {
        this.comments = comments;
    }
    
    public void addComment(String comment){
        this.comments.add(comment);
    }
}
