package uk.ac.ebi.intact.webapp.search.struts.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.Protein;

import java.util.*;

/**
 * Provides utility methods for Protein instances to calculate interactions.
 *
 * @author Michael Kleen
 * @version ProteinUtils.java Date: Feb 11, 2005 Time: 10:51:08 AM
 */
public class ProteinUtils {

    private static final Log log = LogFactory.getLog(ProteinUtils.class);

    /**
     * ProteinUtils should not be instantiated.
     */
    private ProteinUtils() {
    }

    /**
     * Returns a collection of binary interactions in which all proteins are involved.
     *
     * @param someProteins a collection proteins, must no be null
     *
     * @return a collection of binary interactions in which all proteins are involved
     */
    public static Collection getBinaryInteractions( final Collection someProteins ) {

        // first we check the type to make clear nobody is putting rubbish in here
        final Collection myProteins = new ArrayList();
        for ( Iterator iterator = someProteins.iterator(); iterator.hasNext(); ) {
            final Object o = iterator.next();
            if ( !Protein.class.isAssignableFrom( o.getClass() ) ) {
                // someone put rubbish in, throw an exception
                throw new IllegalArgumentException( "Wrong datatype in protein collection" );
            } else {
                myProteins.add( o );
            }
        } // for

        // let's start first to calculating all interactions in which all proteins involved
        Collection<Interaction> intersection = new HashSet<Interaction>();

        final Iterator iterator = myProteins.iterator();
        final Protein first = (Protein) iterator.next();
        intersection.addAll( getNnaryInteractions( first ) );

        while ( iterator.hasNext() ) {
            final Protein protein = (Protein) iterator.next();
            // get Nnary Interaction for every protein
            final Collection proteinInteraction = getNnaryInteractions( protein );
            // and calculate the intersection to the other proteins
            intersection = CollectionUtils.intersection( intersection, proteinInteraction );
        }

        Collection<Interaction> result = new HashSet<Interaction>();

        // now check for every interaction if it's binary
        for (Interaction interaction : intersection)
        {
            if (isBinaryInteraction(interaction))
            {
                result.add(interaction);
            }
        } // for

        if ( result.isEmpty() ) {
            // we found no interactions, so throw a empty set back
            return Collections.EMPTY_SET;
        }
        return result;
    }

    /**
     * Returns a collection containing the n-ary interactions of the give protein.
     *
     * @param anInteractor a protein, must no be null
     *
     * @return a collection containing the n-ary interactions of the given protein.
     */
    public static Collection<Interaction> getNnaryInteractions( final Interactor anInteractor ) {
        // first get all Components
        //TODO (BA) This needs to be paginated to avoid potential OutOfMemoryExceptions
        final Collection<Component> componentSet = anInteractor.getActiveInstances();
        final Set<Interaction> someInteractions = new HashSet<Interaction>();
        // now get all Interactions from the Components
        for (Component component : componentSet)
        {
            someInteractions.add(component.getInteraction());
        }

        return someInteractions;
    }


    /**
     * Returns a collection containing the self interactions of the give protein.
     * <p>
     * A Self Interaction is an Interaction which got 2 or lesser Compoenents and the sum of the stoichemetry is 2.
     *
     *
     * @return a collection containing the self interactions of the given protein.
     */
//    public static Collection getSelfInteractions( final Protein aProtein ) {      // 4 usage 1 in BinaryPorteinAction, 3 in PartnerViewBean
    public static Collection<Interaction> getSelfInteractions( final Interactor anInteractor ) {

        final Set<Interaction> result = new HashSet<Interaction>();
        final Collection<Interaction> someInteractions = getNnaryInteractions( anInteractor );

        // now check for every interaction
        for (Interaction interaction : someInteractions)
        {
            if (isBinaryInteraction(interaction))
            {
                result.add(interaction);
            }
        } // for

        if ( result.isEmpty() ) {
            // we found no interactions, so throw a empty set back
            return Collections.EMPTY_SET;
        }
        return result;
    }

    public static boolean isBinaryInteraction(Interaction interaction) {
        boolean isBinaryInteraction = false;

        int stoichiometrySum = 0;
        Collection<Component> components = interaction.getComponents();
        int componentCount = components.size();

        for ( Component component : components ) {
            stoichiometrySum += component.getStoichiometry();
        }

        if (stoichiometrySum == 0 && componentCount == 2) {
            log.debug("Binary interaction. Stoichiometry 0, components 2");
            isBinaryInteraction = true;
        } else {

            if ( componentCount == 2 ) {

                // check that the stochiometry is 1 for each component
                Iterator<Component> iterator1 = components.iterator();

                Component component1 = iterator1.next();
                float stochio1 = component1.getStoichiometry();

                Component component2 = iterator1.next();
                float stochio2 = component2.getStoichiometry();

                if ( stochio1 == 1 && stochio2 == 1 ) {
                    log.debug("Binary interaction. Stoichiometry 2, each component with stoichiometry 1");
                    isBinaryInteraction = true;

                }

            }
        }

        return isBinaryInteraction;
    }

}