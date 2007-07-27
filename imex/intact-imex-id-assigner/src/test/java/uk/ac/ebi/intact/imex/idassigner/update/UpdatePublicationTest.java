package uk.ac.ebi.intact.imex.idassigner.update;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.imex.idassigner.helpers.CvHelper;
import uk.ac.ebi.intact.imex.idassigner.helpers.InteractionHelper;
import uk.ac.ebi.intact.imex.idassigner.id.IMExIdTransformer;
import uk.ac.ebi.intact.imex.idassigner.id.IMExRange;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.DummyKeyAssignerService;
import uk.ac.ebi.intact.imex.idassigner.keyassigner.KeyAssignerServiceException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.AnnotationDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.ExperimentDao;
import uk.ac.ebi.intact.persistence.dao.PublicationDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * UpdatePublication Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
@Ignore
public class UpdatePublicationTest {

    //////////////////////////
    // Initialisation

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    ///////////////////////
    // Helper methods

    private void acceptAllExperiment( String pmid, int expectedExperimentCount ) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        ExperimentDao edao = daoFactory.getExperimentDao();
        AnnotationDao adao = daoFactory.getAnnotationDao();

        List<Experiment> experiments = edao.getByXrefLike( CvHelper.getPubmed(), pmid );
        assertNotNull( experiments );
        assertEquals( expectedExperimentCount, experiments.size() );

        Institution owner = IntactContext.getCurrentInstance().getConfig().getInstitution();
        CvTopic accepted = CvHelper.getAccepted();
        for ( Experiment experiment : experiments ) {
            Annotation a = new Annotation( owner, accepted );
            if ( !experiment.getAnnotations().contains( a ) ) {
                adao.persist( a );

                experiment.addAnnotation( a );
                edao.update( experiment );
            }
        }
    }

    private Collection<IMExRange> buildRangeForPublication( String pmid, int expectedInteractionCount ) {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        ExperimentDao edao = daoFactory.getExperimentDao();

        List<Experiment> experiments = edao.getByXrefLike( CvHelper.getPubmed(), pmid );
        assertNotNull( experiments );
        assertFalse( experiments.isEmpty() );

        Collection<Interaction> interactions = new ArrayList<Interaction>();
        for ( Experiment e : experiments ) {
            interactions.addAll( e.getInteractions() );
        }

        assertEquals( expectedInteractionCount, interactions.size() );

        Collection<Long> ids = new ArrayList<Long>( interactions.size() );
        for ( Interaction interaction : interactions ) {
            String imexId = InteractionHelper.getIMExId( interaction );
            ids.add( IMExIdTransformer.parseIMExId( imexId ) );
        }

        List<IMExRange> ranges = IMExIdTransformer.buildRanges( ids );

        return ranges;
    }

    private void assertHasAnnotation( Publication pub, CvTopic topic, String annotation ) {
        assertNotNull( pub );
        assertNotNull( topic );
        for ( Annotation annot : pub.getAnnotations() ) {
            if ( topic.equals( annot.getCvTopic() ) ) {
                if ( annot.getAnnotationText().equals( annotation ) ) {
                    return; // found it !! exit.
                }
            }
        }
        fail( "Could not find Annotation( CvTopic(" + topic.getShortLabel() + "), '" + annotation + "' )." );
    }

    private String printCollection(Collection<?> col) {
        StringBuilder sb = new StringBuilder( 128 );
        int i = 1;
        for ( Object o : col ) {
            sb.append( i++ ).append( ':' ).append( ' ' ).append( o ).append( '\n' );
        }
        return sb.toString();
    }

    ////////////////////
    // Tests

    @Test
    public void update() throws KeyAssignerServiceException, IntactTransactionException {

        final String pmid = "15014444"; // 15039447

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        acceptAllExperiment( pmid, 2 );
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();


        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        DummyKeyAssignerService keyAssigner = new DummyKeyAssignerService( 5L, 2L );
        UpdatePublication updator = new UpdatePublication( keyAssigner );
        updator.update( pmid );
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();

        // check on the current state of the key assigner.
        assertEquals( 23L, keyAssigner.getLastAccessionReturned() );
        assertEquals( 2L, keyAssigner.getLastSubmissionId() );

        // TODO check on the range associated on the interactions !!
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        ExperimentDao edao = daoFactory.getExperimentDao();
        List<Experiment> experiments = edao.getByXrefLike( CvHelper.getPubmed(), pmid );
        assertEquals( 2, experiments.size() );

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        Collection<IMExRange> ranges = buildRangeForPublication( pmid, 12 );
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();

        assertNotNull( ranges );
        assertEquals( printCollection(ranges), 1, ranges.size() );
        IMExRange range = ranges.iterator().next();

        assertEquals( 5L, range.getFrom() );
        assertEquals( 13L, range.getTo() );

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        PublicationDao dao = daoFactory.getPublicationDao();
        List<Publication> publications = dao.getByXrefLike( CvHelper.getPubmed(), CvHelper.getPrimaryReference(), pmid );
        assertNotNull( publications );
        assertEquals( 1, publications.size() );
        Publication pub = publications.iterator().next();

        assertHasAnnotation( pub, CvHelper.getImexRangeAssigned(), "5..13" );
        assertHasAnnotation( pub, CvHelper.getImexRangeRequested(), "5..23" );
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }
}
