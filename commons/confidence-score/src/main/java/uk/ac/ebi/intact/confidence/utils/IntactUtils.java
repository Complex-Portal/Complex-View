/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.confidence.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.InterProIdentifierImpl;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *  Assists the retrieval of annotation form IntAct proteins.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class IntactUtils {

    public static final Log log = LogFactory.getLog( IntactUtils.class );


    public static Set<Identifier> getGOs( DaoFactory daoFactory, Protein protein){
        Set<Identifier> gos = new HashSet<Identifier>();
        CvObjectDao<CvDatabase> cvDatabase = daoFactory.getCvObjectDao( CvDatabase.class );
        CvDatabase cvGo = cvDatabase.getByPsiMiRef( CvDatabase.GO_MI_REF );
        if ( cvGo == null ) {
            throw new NullPointerException( "CvDatabase GO must not be null, check that it exists in the database!" );
        }
        Collection<Xref> goRefs = AnnotatedObjectUtils.searchXrefs( protein, cvGo );
        for ( Xref xref : goRefs ) {
            Identifier go = new GoIdentifierImpl( xref.getPrimaryId() );
            gos.add( go );
        }

       return gos; 
    }

    public static Set<Identifier> getIPs( DaoFactory daoFactory, Protein protein){
        Set<Identifier> ips = new HashSet<Identifier>();
        CvObjectDao<CvDatabase> cvDatabase = daoFactory.getCvObjectDao( CvDatabase.class );
        CvDatabase cvIP = cvDatabase.getByPsiMiRef( CvDatabase.INTERPRO_MI_REF );
        if ( cvIP == null ) {
            throw new NullPointerException( "CvDatabase InterPro must not be null, check that it exists in the database!" );
        }
        Collection<Xref> ipRefs = AnnotatedObjectUtils.searchXrefs( protein, cvIP );
        for ( Xref xref : ipRefs ) {
            Identifier ip = new InterProIdentifierImpl( xref.getPrimaryId() );
            ips.add( ip );
        }

       return ips; 
     }

    public static Sequence getSequence (Protein protein){
        if (protein == null){
            throw new IllegalArgumentException( "Protein must not be null!");
        }

        if ( protein.getSequence() != null ) {
            return new Sequence( protein.getSequence() );
        } else {
            if (log.isTraceEnabled()){
                log.trace( "seq was null for: " + protein.getAc() );
            }
            return null;
        }
    }
}
