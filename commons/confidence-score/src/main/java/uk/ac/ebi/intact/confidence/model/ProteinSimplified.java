/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * ProteinSimplified class represents a IntAct protein object,
 * containing only the uniprotAc, the role (for expanding),
 * the sequence ( for blast), GO and InterPro annotation
 * and a list of blast hits for generating the attributes.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @since 1.6.0
 * <pre> 14 - Aug - 2007 </pre>
 */
public class ProteinSimplified {

    private UniprotAc uniprotAc;
    private String role;            // bait or prey or neutral
    private Sequence seq;
    private Set<Identifier> goSet;
    private Set<Identifier> interProSet;
    private Set<UniprotAc> alignments;

    //// Constructors
    //
    public ProteinSimplified() {
        goSet = new HashSet<Identifier>(0);
        interProSet = new HashSet<Identifier>(0);
        alignments = new HashSet<UniprotAc>(0);
    }

    public ProteinSimplified( UniprotAc uniprotAc ) {
        this();
        this.uniprotAc = uniprotAc;
    }

    public ProteinSimplified( UniprotAc uniprotAc, String role ) {
        this.uniprotAc = uniprotAc;
        this.role = role;
    }

    public ProteinSimplified( UniprotAc uniprotAc, Sequence seq ) {
        this.uniprotAc = uniprotAc;
        this.seq = seq;
    }

    //// Getters /Settert
    //
    public UniprotAc getUniprotAc() {
        return uniprotAc;
    }

    public String getRole() {
        return role;
    }

    public void setUniprotAc( UniprotAc uniprotAc ) {
        this.uniprotAc = uniprotAc;
    }

    public void setRole( String role ) {
        this.role = role;
    }

    public Sequence getSequence() {
        return seq;
    }

    public void setSequence( Sequence seq ) {
        this.seq = seq;
    }

    public Set<Identifier> getGoSet() {
        return goSet;
    }

    public void setGoSet( Set<Identifier> goSet ) {
        this.goSet = goSet;
    }

    public void addGo( GoIdentifierImpl go ) {
        if ( goSet == null ) {
            goSet = new HashSet<Identifier>();
        }
        goSet.add( go );
    }

    /**
     * @return the interProSet
     */
    public Set<Identifier> getInterProSet() {
        return interProSet;
    }

    /**
     * @param interProSet the interProSet to set
     */
    public void setInterProSet( Set<Identifier> interProSet ) {
        this.interProSet = interProSet;
    }

    public void addInterProId( InterProIdentifierImpl ip ) {
        if ( interProSet == null ) {
            interProSet = new HashSet<Identifier>();
        }
        interProSet.add( ip );
    }

    public Set<UniprotAc> getAlignments() {
        return alignments;
    }

    public void setAlignments( Set<UniprotAc> alignments ) {
        this.alignments = alignments;
    }

    public void addAlignment(UniprotAc ac){
        if (this.alignments == null){
            this.alignments = new HashSet<UniprotAc>();
        }
        this.alignments.add( ac);
    }


    public String convertToString(){
        return this.getUniprotAc().getAcNr() + "-" + this.role;
    }

    public String convertGOAnnotationToString(){
        String result = uniprotAc.getAcNr();
        for ( Iterator<Identifier> identifierIterator = goSet.iterator(); identifierIterator.hasNext(); ) {
            Identifier identifier = identifierIterator.next();
            result +="," + identifier.getId();
        }

        return result;
    }

    public String convertIpAnnotationToString(){
        String result = uniprotAc.getAcNr();
        for ( Iterator<Identifier> identifierIterator = interProSet.iterator(); identifierIterator.hasNext(); ) {
            Identifier identifier = identifierIterator.next();
            result +="," + identifier.getId();
        }

        return result;
    }

    public String convertSeqAnnotationToFasta(){
        if (seq != null) {
            String result = ">" + uniprotAc.getAcNr() + "|description\n";
            result += seq.getSeq();
            return result;
        } else {
            return null;
        }
        
    }

    // public Collection<String> getDomains() {
    // return domains;
    // }
    //
    // public void setDomains(Collection<String> domains) {
    // this.domains = domains;
    // }
   
    /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#hashCode()
      */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( role == null ) ? 0 : role.hashCode() );
        result = prime * result + ( ( seq == null ) ? 0 : seq.hashCode() );
        result = prime * result + ( ( uniprotAc == null ) ? 0 : uniprotAc.hashCode() );
        return result;
    }

    /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#equals(java.lang.Object)
      */
    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        final ProteinSimplified other = ( ProteinSimplified ) obj;
        if ( role == null ) {
            if ( other.role != null )
                return false;
        } else if ( !role.equals( other.role ) )
            return false;
        if ( seq == null ) {
            if ( other.seq != null )
                return false;
        } else if ( !seq.equals( other.seq ) )
            return false;
        if ( uniprotAc == null ) {
            if ( other.uniprotAc != null )
                return false;
        } else if ( !uniprotAc.equals( other.uniprotAc ) )
            return false;
        return true;
    }

    /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
    @Override
    public String toString() {
        return uniprotAc.toString();
    }

}
