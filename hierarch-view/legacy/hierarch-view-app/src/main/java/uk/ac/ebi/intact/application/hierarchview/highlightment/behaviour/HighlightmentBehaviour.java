/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.highlightment.behaviour;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.util.simplegraph.BasicGraphI;

import java.util.Collection;
import java.util.Iterator;


/**
 * Abstract class allowing to deals with the Highlightment behaviour,
 * the implementation of that class would just specify the behaviour
 * of one node of the graph.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public abstract class HighlightmentBehaviour {

    static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);

    /**
     * Provides a implementation of HighlightmentBehaviour from its name.<br>
     * for example you have an implementation of this abstract class called : <b>ColorHighlightmentBehaviour</b>.
     * so, you could call the following method to get an instance of this class :
     * <br>
     * <b>HighlightmentBehaviour.getHighlightmentBehaviour ("mypackage.ColorHighlightmentBehaviour");</b>
     * <br>
     * then you're able to use methods provided by this abstract class without to know
     * what implementation you are using.
     *
     * @param aClassName the name of the implementation class you want to get
     * @return an HighlightmentBehaviour object, or null if an error occurs.
     */
    public static HighlightmentBehaviour getHighlightmentBehaviour (String aClassName) {

        Object object = null;

        try {
            // create a class by its name
            Class cls = Class.forName(aClassName);

            // Create an instance of the class invoked
            object = cls.newInstance();

            if (false == (object instanceof HighlightmentBehaviour)) {
                // my object is not from the proper type
                logger.error (aClassName + " is not a HighlightmentBehaviour");
                return null;
            }
        } catch (Exception e) {
            // nothing to do, object is already setted to null
        }

        return (HighlightmentBehaviour) object;
    } // getHighlightmentBehaviour


    /**
     * Apply the implemented behaviour to the specific Node of the graph
     *
     * @param aProtein the node on which we want to apply the behaviour
     */
    abstract public void applyBehaviour (BasicGraphI aProtein);

    /**
     * Allow to apply a modification on the collection of protein to highlight.
     * for example select all the graph proteins which are not in the given collection
     *
     * The default behaviour of that method is to return the given Collection,
     * to change that you have to overwrite that method in your implementation.
     *
     * @param proteins the list of protein to highlight
     * @param aGraph the current interaction network
     *
     * @return the new collection of protein to highlight
     */
    public Collection modifyCollection (Collection proteins, InteractionNetwork aGraph) {
        return proteins;
    }

    /**
     * Apply the implemented behaviour to a set of nodes.
     *
     * @param proteins the set of protein on which to apply the behaviour
     * @param aGraph the interaction network they come from
     */
    public void apply (Collection proteins, InteractionNetwork aGraph) {

        proteins = modifyCollection (proteins, aGraph);

        if (null != proteins) {
            Iterator iterator = proteins.iterator();
            while (iterator.hasNext()) {
                applyBehaviour ((BasicGraphI) iterator.next());
            }
        } // if
    }
}