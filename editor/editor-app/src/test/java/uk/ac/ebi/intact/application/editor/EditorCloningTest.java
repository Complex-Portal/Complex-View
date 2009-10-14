package uk.ac.ebi.intact.application.editor;

import org.junit.Test;
import org.junit.Assert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.util.InteractionIntactCloner;
import uk.ac.ebi.intact.application.editor.util.EditorIntactCloner;

import java.util.List;

/**
 * TODO document this !
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.9.3
 */
public class EditorCloningTest extends EditorBasicTestCase {

     @Test
     @Transactional( propagation = Propagation.NEVER )
     public void doClone() throws Exception {

         TransactionStatus transactionStatus = getDataContext().beginTransaction();

         final Experiment experiment = getMockBuilder().createExperimentEmpty( "sam-2009-1", "12345678" );
         Assert.assertEquals( 0, experiment.getAnnotations().size() );
         Assert.assertEquals( 1, experiment.getXrefs().size() );
         Assert.assertEquals( 0, experiment.getAliases().size() );

         getPersisterHelper().save( experiment );

         // reload the experiment from the database
         List<Experiment> allExperiments = getDaoFactory().getExperimentDao().getAll();
         Assert.assertEquals( 1 , allExperiments.size() );

         Experiment reloadedExperiment = allExperiments.iterator().next();
         Assert.assertEquals( 0, reloadedExperiment.getAnnotations().size() );
         Assert.assertEquals( 1, reloadedExperiment.getXrefs().size() );
         Assert.assertEquals( 0, reloadedExperiment.getAliases().size() );

         getDataContext().commitTransaction( transactionStatus );

//         getEntityManager().flush();
//         getEntityManager().clear();

         transactionStatus = getDataContext().beginTransaction();

         final CvTopic comment = getMockBuilder().createCvObject( CvTopic.class, CvTopic.COMMENT_MI_REF, CvTopic.COMMENT );
         reloadedExperiment.addAnnotation( getMockBuilder().createAnnotation("a nice comment", comment ) );
         Assert.assertEquals( 1, reloadedExperiment.getAnnotations().size() );

         getPersisterHelper().save( comment, reloadedExperiment );

         // reload again the experiment from the database
         allExperiments = getDaoFactory().getExperimentDao().getAll();
         Assert.assertEquals( 1 , allExperiments.size() );

         reloadedExperiment = allExperiments.iterator().next();
         Assert.assertEquals( 1, reloadedExperiment.getAnnotations().size() );
         Assert.assertEquals( 1, reloadedExperiment.getXrefs().size() );
         Assert.assertEquals( 0, reloadedExperiment.getAliases().size() );

         getDataContext().commitTransaction( transactionStatus );


         IntactCloner cloner = new EditorIntactCloner();
         final Experiment clonedExperiment = cloner.clone( reloadedExperiment );

         Assert.assertEquals( 1, clonedExperiment.getAnnotations().size() );
         getPersisterHelper().save( clonedExperiment );
     }
}
