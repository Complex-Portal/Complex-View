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
package uk.ac.ebi.intact.confidence.util;

import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.*;
import uk.ac.ebi.intact.confidence.dataRetriever.uniprot.UniprotDataRetriever;
import uk.ac.ebi.intact.confidence.model.GoId;
import uk.ac.ebi.intact.confidence.model.Id;
import uk.ac.ebi.intact.confidence.model.InterProId;

import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *               29-Nov-2007
 *               </pre>
 */
public class AttributeGetterImpl implements AttributeGetter {
    private UniprotDataRetriever uniprotKb;

    public AttributeGetterImpl() {
        uniprotKb = new UniprotDataRetriever();
    }

    public List<Attribute> fetchGoAttributes( ProteinPair proteinPair ) {
        Set<GoId> gosA = uniprotKb.getGos( new UniprotAc( proteinPair.getFirstId() ) );
        Set<GoId> gosB = uniprotKb.getGos( new UniprotAc( proteinPair.getSecondId() ) );
        Set<Id> goA = new HashSet<Id>(gosA);
        Set<Id> goB = new HashSet<Id>(gosB);
        List<Attribute> attribs = combine( goA, goB );
        return attribs;  
    }

    protected List<Attribute> combine( Set<Id> gosA, Set<Id> gosB ) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for ( Iterator<Id> idItA = gosA.iterator(); idItA.hasNext(); ) {
            Id idA = idItA.next();
            for ( Iterator<Id> idItB = gosB.iterator(); idItB.hasNext(); ) {
                Id idB = idItB.next();
                if ( !idA.equals( idB ) ) {
                    Attribute attr = properAttribute(idA, idB); //new GoPairAttribute( new GoTermPair( goIdA.getId(), goIdB.getId() ) );
                    // for the reverse part the GoTermPair comes in action, because it sorts the names
                    if ( !attributes.contains( attr ) ) {
                        attributes.add( attr );
                    }
                }
            }
        }
        return attributes;
    }

    private Attribute properAttribute( Id idA, Id idB ) {
        if (idA instanceof GoId && idB instanceof GoId){
            return new GoPairAttribute( new GoTermPair( idA.getId(), idB.getId()));
        } else if (idA instanceof InterProId && idB instanceof InterProId){
            return new IpPairAttribute(new IpTermPair(idA.getId(), idB.getId()));
        }
        return null;
    }

    public List<Attribute> fetchIpAttributes( ProteinPair proteinPair ) {
        Set<InterProId> ipsA = uniprotKb.getIps( new UniprotAc( proteinPair.getFirstId()));
        Set<InterProId> ipsB = uniprotKb.getIps( new UniprotAc(proteinPair.getSecondId()));
        Set<Id> ipA = new HashSet<Id>(ipsA);
        Set<Id> ipB = new HashSet<Id>(ipsB);

        List<Attribute> attribs = combine(ipA, ipB);
        return attribs;
    }

    public List<Attribute> fetchAlignAttributes( ProteinPair proteinPair ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Attribute> fetchAllAttributes( ProteinPair proteinPair ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
