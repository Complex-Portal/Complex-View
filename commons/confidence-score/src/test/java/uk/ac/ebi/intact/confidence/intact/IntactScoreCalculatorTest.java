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
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.utils.ParserUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.CvConfidenceType;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Test class: for given gisModel assign scores to medium confidence file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class IntactScoreCalculatorTest {
      /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( IntactScoreCalculatorTest.class );

    @Test
    @Ignore
    public void testCalculator() throws Exception {
        log.info( "mem: " + ( Runtime.getRuntime().maxMemory() ) / ( 1024 * 1024 ) );

        File workDir = new File( "/net/nfs7/vol22/sp-pro5/20080201_iarmean/TrainModel" );

        //TODO: specifiy dbFolder and blastArchiveDir and email
        File dbFolder = new File( workDir.getParentFile(), "blastDb" );
        dbFolder.mkdir();
        File blastArchiveDir = new File( workDir.getParentFile(), "archive" );
        blastArchiveDir.mkdir();
        String email = "iarmean@ebi.ac.uk";
        BlastConfig config = new BlastConfig( email );
        // need a blast Archive
        config.setBlastArchiveDir( blastArchiveDir );
        // db dir
        config.setDatabaseDir( dbFolder );

        //TODO: specifiy the gisModel file
        File gisModelFile = new File( workDir, "gisModel.txt" );
        OpenNLPMaxEntClassifier classifier = new OpenNLPMaxEntClassifier( gisModelFile );

        File hcSet = new File( workDir, "highconf_set.txt" );
        Set<UniprotAc> againstProts = ParserUtils.parseProteins( hcSet );


        IntactConfidenceCalculator ic = new IntactConfidenceCalculator( classifier, config, againstProts, workDir );

        File pgConfigFile = new File( TrainModel.class.getResource( "/hibernate.d003.cfg.xml" ).getFile() );
        IntactContext.initStandaloneContext( pgConfigFile );

        //save cvConfidenceType to db
        CvConfidenceType ctype = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(), CvConfidenceType.class, null, "intact confidence" );
        PersisterHelper.saveOrUpdate( ctype );

        InteractionDao interactionDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao();


        int totalNr = interactionDao.countAll();
        System.out.println("total: " + totalNr);
         int firstResult = 0;
        int maxResults = 50;

        boolean firstTime = true;
        List<InteractionImpl> interactions = null;
        for ( int i = 0; i < totalNr; i += maxResults ) {
            IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            interactions = interactionDao.getAll( firstResult, maxResults );

            ic.calculate( interactions, classifier );
            saveInteractionsToDb(interactions);

            if (firstTime){
                firstTime = false;
            }
            if ( log.isInfoEnabled() ) {
                int processed = firstResult + interactions.size();

                if ( firstResult != processed ) {
                    log.info( "\t\tProcessed " + ( firstResult + interactions.size() +" out of " + totalNr) );
                }
            }

           firstResult = firstResult + maxResults;

           IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        }
        if (log.isInfoEnabled()){
            log.info( "Processed " + totalNr + " IntAct interactions.");
        }
    }

    private void saveInteractionsToDb( List<InteractionImpl> interactions ) throws IntactTransactionException {
        CvConfidenceType cvConfidenceType = (CvConfidenceType) IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao().getByShortLabel( "intact confidence" );

        for ( Iterator<InteractionImpl> iter = interactions.iterator(); iter.hasNext(); ) {
            InteractionImpl interaction = iter.next();

            if (interaction.getConfidences().size() != 0){
                Confidence confidence = interaction.getConfidences().iterator().next();
                confidence.setCvConfidenceType( cvConfidenceType );

                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().update( interaction );
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getConfidenceDao().persist( confidence );
            }
        }
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

}
