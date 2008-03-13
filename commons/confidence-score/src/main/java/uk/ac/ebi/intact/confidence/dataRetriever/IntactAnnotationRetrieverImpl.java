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
package uk.ac.ebi.intact.confidence.dataRetriever;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.UniprotIdentifierImpl;
import uk.ac.ebi.intact.confidence.utils.IntactUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.ProteinImpl;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.ProteinDao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Retrieving annotation information (GO, InterPro, Sequence) from IntAct.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class IntactAnnotationRetrieverImpl implements AnnotationRetrieverStrategy {
   /**
	 * Sets up a logger for that class.
	 */
	public static final Log log	= LogFactory.getLog(IntactAnnotationRetrieverImpl.class);
    private ProteinDao proteinDao;
    private DaoFactory factoryDao;

    public IntactAnnotationRetrieverImpl(){
        factoryDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        proteinDao = factoryDao.getProteinDao();
    }

    public Sequence getSeq( Identifier id ) {
        List<ProteinImpl> proteins = proteinDao.getByUniprotId( id.getId() );
        if (proteins.size() == 0){
            log.warn( "Protein not found in the IntAct database! " + id.convertToString() );
            return null;
        }
        if (proteins.size() > 1){
            log.warn( "Found more than one protein in IntAct with the same uniprotAc! "+ id.convertToString() );
        }
        return IntactUtils.getSequence( proteins.get( 0));
    }

    public Sequence getSeq( UniprotAc ac ) {
        return getSeq( new UniprotIdentifierImpl( ac) );
    }

    public Set<Identifier> getGOs( Identifier id ) {
        List<ProteinImpl> proteins = proteinDao.getByUniprotId( id.getId() );
        if (proteins.size() == 0){
            log.warn( "Protein not found in the IntAct database! " + id.convertToString() );
            return null;
        }
        if (proteins.size() > 1){
            log.warn( "Found more than one protein in IntAct with the same uniprotAc! "+ id.convertToString() );
        }
        return IntactUtils.getGOs( factoryDao, proteins.get( 0 ));
    }

    public Set<Identifier> getGOs( UniprotAc ac ) {
        return getGOs( new UniprotIdentifierImpl( ac) );
    }

    public Set<Identifier> getIps( Identifier id ) {
        List<ProteinImpl> proteins = proteinDao.getByUniprotId( id.getId() );
         if (proteins.size() == 0){
            log.warn( "Protein not found in the IntAct database! " + id.convertToString() );
            return null;
        }
        if (proteins.size() > 1){
            log.warn( "Found more than one protein in IntAct with the same uniprotAc! "+ id.convertToString() );
        }
        return IntactUtils.getIPs( factoryDao, proteins.get( 0 ));
    }

    public Set<Identifier> getIps( UniprotAc ac ) {
        return getIps(new UniprotIdentifierImpl( ac));
    }


    public void getSequences( List<ProteinSimplified> proteins ) {
        for ( Iterator<ProteinSimplified> iter = proteins.iterator(); iter.hasNext(); ) {
            ProteinSimplified proteinS = iter.next();
            Sequence seq = getSeq( proteinS.getUniprotAc() );
            proteinS.setSequence( seq );
        }
    }

    public void getGOs( List<ProteinSimplified> proteins ) {
         for ( Iterator<ProteinSimplified> iter = proteins.iterator(); iter.hasNext(); ) {
            ProteinSimplified proteinS = iter.next();
            Set<Identifier> gos = getGOs( proteinS.getUniprotAc() );
            proteinS.setGoSet( gos );
        }
    }

    public void getIps( List<ProteinSimplified> proteins ) {
        for ( Iterator<ProteinSimplified> iter = proteins.iterator(); iter.hasNext(); ) {
            ProteinSimplified proteinS = iter.next();
            Set<Identifier> ips = getIps( proteinS.getUniprotAc() );
            proteinS.setGoSet( ips );
        }
    }

    public List<String> getUniprotProteins(){
        return proteinDao.getAllUniprotAcs();
    }
}
