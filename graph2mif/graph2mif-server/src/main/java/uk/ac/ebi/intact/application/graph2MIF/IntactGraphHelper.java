package uk.ac.ebi.intact.application.graph2MIF;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.simplegraph.BasicGraphI;
import uk.ac.ebi.intact.util.simplegraph.Edge;
import uk.ac.ebi.intact.util.simplegraph.EdgeI;
import uk.ac.ebi.intact.util.simplegraph.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class handles Intact graph routines which were previouslt part of
 * IntactHelper
 *
 * @author Intact
 * @version $Id$
 */
public class IntactGraphHelper {

    private CvTopic negativeTopic;
    private boolean negativeAlreadySearched = false;

    /**
     * Constructs an instance of this class with an Intact helper.
     *
     */
    public IntactGraphHelper() {
    }

    /**
     * Returns a subgraph centered on startNode.
     * The subgraph will contain all nodes which are up to graphDepth interactions away from startNode.
     * Only Interactions which belong to one of the Experiments in experiments will be taken into account.
     * If experiments is empty, all Interactions are taken into account.
     * <p/>
     * Graph depth:
     * This parameter limits the size of the returned interaction graph. All baits are shown with all
     * the interacting preys, even if they would normally be on the "rim" of the graph.
     * Therefore the actual diameter of the graph may be 2*(graphDepth+1).
     * <p/>
     * Expansion:
     * If an Interaction has more than two interactors, it has to be defined how pairwise interactions
     * are generated from the complex data. The possible values are defined in the beginning of this file.
     *
     * @param startNode - the start node of the subgraph.
     * @param graphDepth - depth of the graph
     * @param experiments - Experiments which should be taken into account
     * @param complexExpansion - Mode of expansion of complexes into pairwise interactions
     * @param graph - the graph we have to fill with interaction data
     * @return a GraphI object.
     * @throws IntactException - thrown if problems are encountered
     */
    public Graph subGraph(Interactor startNode,
                          int graphDepth,
                          Collection experiments,
                          int complexExpansion,
                          Graph graph) throws IntactException
    {
        if (startNode instanceof Interaction) {
            if (!isNegative((Interaction) startNode)) {
                graph = subGraphPartial((Interaction) startNode, graphDepth, experiments, complexExpansion, graph);
            }
        }
        else if (startNode instanceof Interactor) {
            graph = subGraphPartial(startNode, graphDepth, experiments, complexExpansion, graph);
        }

        return graph;
    }

    /**
     * Answers the question: is that AnnotatedObject (Interaction, Experiment) annotated as negative ?
     *
     * @param annotatedObject the object we want to introspect
     * @return true if the object is annotated with the 'negative' CvTopic, otherwise false.
     */
    private boolean hasNegativeAnnotation(AnnotatedObject annotatedObject) {
        // get the necessary vocabulary (CvTopic: negative)
        if (!negativeAlreadySearched) {
            try {
                negativeTopic = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                    .getCvObjectDao(CvTopic.class).getByShortLabel(CvTopic.NEGATIVE);
            }
            catch (IntactException e) {
                e.printStackTrace();
            }
        }

        if (null == negativeTopic) {
            return false;
        }

        boolean isNegative = false;

        Collection annotations = annotatedObject.getAnnotations();
        for (Iterator iterator = annotations.iterator(); iterator.hasNext() && false == isNegative;) {
            Annotation annotation = (Annotation) iterator.next();

            if (negativeTopic.equals(annotation.getCvTopic())) {
                isNegative = true;
            }
        }

        return isNegative;
    }

    /**
     * Answers the question: is that Interaction negative ?
     * <br>
     * That takes into account a negative annotation at the Interaction level as well as at
     * the experiment level
     *
     * @param interaction the interaction we want to introspect
     * @return true if the interaction is negative, otherwise false.
     */
    private boolean isNegative(Interaction interaction) {

        boolean isNegative = hasNegativeAnnotation(interaction);

        if (!isNegative) {
            // check all its experiments as well
            Collection experiments = interaction.getExperiments();
            // stops if all experiment checked or one is found to be annotated as negative.
            for (Iterator iterator = experiments.iterator(); iterator.hasNext() && !isNegative;) {
                Experiment experiment = (Experiment) iterator.next();
                isNegative = hasNegativeAnnotation(experiment);
            }
        }

        return isNegative;
    }

    private Graph subGraphPartial(Interactor startNode,
                                  int graphDepth,
                                  Collection experiments,
                                  int complexExpansion,
                                  Graph partialGraph) throws IntactException {

        /* This should not occur, but is ok. */
        if (null == startNode) {
            return partialGraph;
        }

//        System.out.println("subGraphPartial (Interactor) called: " + startNode.getAc() + " Depth: " + graphDepth);

        /* If the Interaction has already been visited, return,
           else mark it.
        */
        if (partialGraph.isVisited(startNode)) {
            return partialGraph;
        }
        else {
            partialGraph.addVisited(startNode);
        }

        /* End of recursion, return */
        if (0 == graphDepth) {
            return partialGraph;
        }

        Iterator i = startNode.getActiveInstances().iterator();

        Component current = null;
        while (i.hasNext()) {
            current = (Component) i.next();

            if (null == current) {
                continue;
            }

            Interaction interaction = current.getInteraction();

            // Don't take into account the negative interaction.
            if (!isNegative(interaction)) {

                /* Explore the next Interaction if not negative */
                partialGraph = subGraphPartial(current.getInteraction(),
                        graphDepth,
                        experiments,
                        complexExpansion,
                        partialGraph);
            }
//            else {
//                System.out.println( interaction.getShortLabel() + " is negative. SKIP IT." );
//            }
        }

        return partialGraph;
    }

    private Graph subGraphPartial(Interaction current,
                                  int graphDepth,
                                  Collection experiments,
                                  int complexExpansion,
                                  Graph partialGraph) throws IntactException {


/* This should not occur, but is ok.
*/
        if (null == current) {
            return partialGraph;
        }

//System.out.println("subGraphPartial (Interaction) called: " + current.getAc() + " Depth: " + graphDepth);

/* If the Interaction has already been visited, return,
else mark it.
*/
        if (partialGraph.isVisited(current)) {
            return partialGraph;
        }
        else {
            partialGraph.addVisited(current);
        }

/* Create list of baits - the size is set later according to what we have to store */
        ArrayList<Component> baits = null;

        switch (complexExpansion) {
            case Constants.EXPANSION_ALL:
                {
                    baits = new ArrayList<Component>(current.getComponents().size());

/* all components are considered as baits */
                    Iterator<Component> i = current.getComponents().iterator();
                    while (i.hasNext()) {
                        baits.add(i.next());
                    }
                }
                break;
            case Constants.EXPANSION_BAITPREY:
                {
/* only report bait-prey relations.
* If there is no bait, select one arbitrarily. Choose the first.
*/
                    Component bait = current.getBait();
                    if (null == bait) {
                        baits = new ArrayList<Component>(current.getComponents().size());
                        Iterator<Component> i = current.getComponents().iterator();
                        if (i.hasNext()) {
                            baits.add(i.next());
                        }
                    }
                    else {
                        baits = new ArrayList<Component>(1);
                        baits.add(bait);
                    }
                }
        }

/* Create list of preys */
        ArrayList preys = new ArrayList(current.getComponents().size());
        Iterator i = current.getComponents().iterator();
        while (i.hasNext()) {
            preys.add(i.next());
        }

/* Generate all bait-prey pairs */
        int countBaits = baits.size();
        int countPreys = preys.size();

        for (int j = 0; j < countBaits; j++) {
//System.out.println("Bait: " + ((Component) baits.get(j)).getInteractor().getAc());
            for (int k = j; k < countPreys; k++) {
//System.out.println("Prey: " + ((Component) preys.get(k)).getInteractor().getAc());

                Component baitComponent = (Component) baits.get(j);
                Interactor baitInteractor = baitComponent.getInteractor();
                Component preyComponent = (Component) preys.get(k);
                Interactor preyInteractor = preyComponent.getInteractor();

                if (baitInteractor != preyInteractor) {
                    BasicGraphI node1 = partialGraph.addNode(baitInteractor);
                    BasicGraphI node2 = partialGraph.addNode(preyInteractor);

                    if (!baitComponent.getInteraction().equals(preyComponent.getInteraction()))
                    {
                        throw new RuntimeException ("Interaction for bait and prey must be the same");
                    }
                    EdgeI edge = new Edge(baitComponent.getInteraction().getAc(), baitComponent.getInteraction().getShortLabel());

                    edge.setNode1(node1);
                    edge.setComponent1(baitComponent);
                    edge.setNode2(node2);
                    edge.setComponent2(preyComponent);
                    partialGraph.addEdge(edge);
//System.out.println("Adding: " + node1.getAc() + " -> " + node2.getAc());
                }
            }
        }

/* recursively explore all Interactors linked to current Interaction */
        for (Iterator iterator = current.getComponents().iterator(); iterator.hasNext();) {
            Component component = (Component) iterator.next();
            partialGraph = subGraphPartial(component.getInteractor(),
                    graphDepth - 1,
                    experiments,
                    complexExpansion,
                    partialGraph);
        }

        return partialGraph;
    }

}
