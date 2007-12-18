/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.model;

import uk.ac.ebi.intact.bridges.blast.model.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Protein class holding the protien data.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 *        <pre>
 *        07-Dec-2007
 *        </pre>
 */
public class ProteinAnnotation {
    private Identifier id;
    private Collection<Identifier> annotations;
//    //TODO: reconsider if its good to have the sequence here (seq is ignored by reader/writer
//    private Sequence sequence;

    public ProteinAnnotation(Identifier id){
        this.id = id;
        annotations = new ArrayList<Identifier>();
    }

     public ProteinAnnotation(Identifier id, Collection<Identifier> annotations){
         this(id);
         if(annotations != null) {
             this.annotations = annotations;
         }
     }

    public Identifier getId() {
        return id;
    }

//    public Sequence getSeqeunce(){
//        return this.sequence;
//    }
//
//    public void setSequence(Sequence seq){
//        sequence = seq;
//    }

    public Collection<Identifier> getAnnotations() {
          return annotations;
    }

    public void setAnnotations( Collection<Identifier> annotations ) {
          this.annotations = annotations;
    }

    public void addAnnotation(Identifier annotation){
       annotations.add( annotation);
    }

    public void removeAnnotation(Identifier annotation){
        annotations.remove( annotation);
    }

    public Collection<Identifier> getGOAnnotations(){
        List<Identifier> gos = new ArrayList<Identifier>();
        for ( Iterator<Identifier> iterator = annotations.iterator(); iterator.hasNext(); ) {
            Identifier identifier = iterator.next();
            if (identifier instanceof GoIdentifierImpl){
                gos.add( identifier);
            }
        }
        return gos;
    }

     public Collection<Identifier> getIpAnnotations(){
        List<Identifier> ips = new ArrayList<Identifier>();
        for ( Iterator<Identifier> iterator = annotations.iterator(); iterator.hasNext(); ) {
            Identifier identifier = iterator.next();
            if (identifier instanceof InterProIdentifierImpl){
                ips.add( identifier);
            }
        }
        return ips;
    }

     public Collection<Identifier> getBlastHitsAnnotations(){
        List<Identifier> uniprotAcs = new ArrayList<Identifier>();
        for ( Iterator<Identifier> iterator = annotations.iterator(); iterator.hasNext(); ) {
            Identifier identifier = iterator.next();
            if (identifier instanceof UniprotIdentifierImpl){
                uniprotAcs.add( identifier);
            }
        }
        return uniprotAcs;
    }

    public String convertToString() {
        String result = id.convertToString() + ",";
        for ( Iterator<Identifier> identifierIterator = annotations.iterator(); identifierIterator.hasNext(); ) {
            Identifier identifier =  identifierIterator.next();
            result += identifier.convertToString()+",";
        }
       return result;
    }

    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( !( o instanceof ProteinAnnotation ) ) return false;

        ProteinAnnotation that = ( ProteinAnnotation ) o;

        if ( id != null ? !id.equals( that.id ) : that.id != null ) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = ( id != null ? id.hashCode() : 0 );
        result = 31 * result + ( annotations != null ? annotations.hashCode() : 0 );
        return result;
    }
}
