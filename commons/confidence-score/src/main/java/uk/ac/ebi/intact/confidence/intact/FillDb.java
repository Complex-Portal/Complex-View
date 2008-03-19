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
package uk.ac.ebi.intact.confidence.intact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.confidence.filter.FilterException;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.util.AttributeGetterException;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.CvConfidenceType;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class to for adding confidence values to interactions in IntAct.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class FillDb {
    /**
	 * Sets up a logger for that class.
	 */
	public static final Log log	= LogFactory.getLog( FillDb.class);
    private File workDir;

    private IntactScoreCalculator scoreCalculator;
    private DaoFactory daoFactory;

    ///////////////////
    // Constructor(s).
    public FillDb( File workDir, BlastConfig config, File highconfFile, File gisModelFile, File goaFile) throws IOException {
        workDir = workDir;
        if (log.isTraceEnabled()){
            log.trace("goaFile: "+ goaFile.getPath());
        }
        OpenNLPMaxEntClassifier classifier = new OpenNLPMaxEntClassifier( gisModelFile );
        
        Set<UniprotAc> againstProts = ParserUtils.parseProteins( highconfFile );

        scoreCalculator = new IntactConfidenceCalculator( classifier, config, againstProts, goaFile, workDir );
    }

    public void fillDb (boolean override) throws FilterException, AttributeGetterException, IntactTransactionException {
        checkCvConfidenceType();

        Set<String> interactionAcs = retrieveListOfInteractions(override);

        int totalNr = interactionAcs.size();
        if (log.isDebugEnabled()){
            log.debug(" total Nr of interactions to process: " + totalNr);
        }

        InteractionDao interactionDao = daoFactory.getInteractionDao();
        List<InteractionImpl> tmpInteractions = new ArrayList<InteractionImpl>();
        int i=0;

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        for ( Iterator<String> iterator = interactionAcs.iterator(); iterator.hasNext(); ) {
            String interactionAc = iterator.next();
            InteractionImpl interaction = interactionDao.getByAc( interactionAc );
            scoreCalculator.calculate( interaction, override );
            i++;
            tmpInteractions.add( interaction );
            if ((i% 50) == 0){
                if (log.isTraceEnabled()){
                    log.trace( "saving " + tmpInteractions.size() + " new interactions; total processed: " + i );
                }
                saveInteractionsToDb( tmpInteractions );
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();
                tmpInteractions.clear(); //= new ArrayList<InteractionImpl>(); // TODO: reinit or empty list
                IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            }
        }
        if (log.isTraceEnabled()){
           log.trace( "saving " + tmpInteractions.size() + " new interactions; total processed: " + i );
        }
        saveInteractionsToDb( tmpInteractions );
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }
    
    /////////////////////
    // Private Method(s).
     private void checkCvConfidenceType() {
         //save cvConfidenceType to db
        daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        CvConfidenceType cvConfidence = (CvConfidenceType) daoFactory.getCvObjectDao().getByShortLabel( "intact confidence" );

        if (cvConfidence == null){
            cvConfidence = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(), CvConfidenceType.class, null, "intact confidence" );
            PersisterHelper.saveOrUpdate( cvConfidence );
        }

        scoreCalculator.setConfidenceType( cvConfidence );
    }

     private Set<String> retrieveListOfInteractions(boolean override) {
         if (daoFactory == null){
             return null;
         }
         InteractionDao interactionDao = daoFactory.getInteractionDao();

         EntityManager em = daoFactory.getEntityManager();

          // select all interactions
         Query query = em.createQuery( "select i.ac from InteractionImpl i" );
         Set<String> interactionAcs = new HashSet<String>( query.getResultList() );
         if (log.isTraceEnabled()){
             log.trace( "allInteractions#: " + interactionAcs.size() );
         }

         if (override){
             // select interactions with a intact confidence
             query = em.createQuery( "select interaction.ac from Confidence c where c.cvConfidenceType.shortLabel = :confShortLabel" );
             query.setParameter( "confShortLabel", "intact confidence" );

             Set<String> interactionsWithConfidences = new HashSet<String>( query.getResultList() );
             if ( log.isTraceEnabled() ) {
                 log.trace( "interactionsWithConfidneces#: " + interactionsWithConfidences.size() );
             }

             // obtain interactions that do not have an intact confidence
             interactionAcs.removeAll( interactionsWithConfidences );
         }
         if ( log.isTraceEnabled() ) {
             log.trace( "interactions to be processed#: " + interactionAcs.size() );
         }

         return interactionAcs;
     }

      private void saveInteractionsToDb( List<InteractionImpl> interactions ) throws IntactTransactionException {
        CvConfidenceType cvConfidenceType = ( CvConfidenceType ) IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao().getByShortLabel( "intact confidence" );

        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction = iter.next();

            if ( interaction.getConfidences().size() != 0 ) {
                Confidence confidence = interaction.getConfidences().iterator().next();
                confidence.setCvConfidenceType( cvConfidenceType );

                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().update( interaction );
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getConfidenceDao().persist( confidence );
            }
        }
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }
}
