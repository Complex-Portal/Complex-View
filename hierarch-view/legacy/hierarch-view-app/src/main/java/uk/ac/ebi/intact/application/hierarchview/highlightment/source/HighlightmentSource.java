package uk.ac.ebi.intact.application.hierarchview.highlightment.source;


import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.business.IntactException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;


/**
 * Abstract class allowing to wrap an highlightment source.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk) & Alexandre Liban (aliban@ebi.ac.uk)
 */

public abstract class HighlightmentSource {

    static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);

    /**
     * Provides a implementation of HighlightmentSource by its name.
     * for example you have an implementation of this abstract class called : <b>GoHighlightmentSource</b>.
     * so, you could call the following method to get an instance of this class :
     * <br>
     * <b>HighlightmentSource.getHighlightmentSource ("mypackage.GoHighlightmentSource");</b>
     * <br>
     * then you're able to use methods provided by this abstract class without to know
     * what implementation you are using.
     *
     * @param aClassName the name of the implementation class you want to get
     * @return an HighlightmentSource object, or null if an error occurs.
     */
    public static HighlightmentSource getHighlightmentSource (String aClassName) {

        Object object = null;

        try {

            // create a class by its name
            Class cls = Class.forName(aClassName);

            // Create an instance of the class invoked
            object = cls.newInstance();

            if (false == (object instanceof HighlightmentSource)) {
                // my object is not from the proper type
                logger.error (aClassName + " is not a HighlightmentSource");
                return null;
            }

        } catch (Exception e) {
            logger.error ("Unable to instanciate object:" + aClassName);
            // nothing to do, object is already setted to null
        }

        return (HighlightmentSource) object;

    } // HighlightmentSource

    /**
     * Return the html code for specific options of the source to be integrated
     * in the highlighting form.
     * if the method return null, the source hasn't options.
     *
     * @param aSession the current session.
     * @return the html code for specific options of the source.
     */
    abstract public String getHtmlCodeOption(HttpSession aSession);


    /**
     * Return a collection of keys specific to the selected protein and the current source.
     * e.g. If the source is GO, we will send the collection of GO term owned by the given protein.
     * Those informations are retreived from the Intact database
     *
     * @param aProteinAC a protein identifier (AC).
     * @param aSession session in which we'll retreive the datasource
     * @return a collection of keys
     */
    abstract public Collection getKeysFromIntAct (String aProteinAC, HttpSession aSession);


    /**
     * Create a set of protein we must highlight in the graph given in parameter.
     * The protein selection is done according to the source keys stored in the IntactUser.
     * Some options (specific to each implementation) could have been set and stored in the
     * session, that method has to get them and care about.
     *
     * @param aSession the session where to find selected keys.
     * @param aGraph the graph we want to highlight.
     * @return a collection of nodes to highlight.
     */
    abstract public Collection proteinToHightlight (HttpSession aSession, InteractionNetwork aGraph);


    /**
     * Allows to update the session object with options stored in the request.
     * These parameters are specific of the implementation.
     *
     * @param aRequest request in which we have to get parameters to save in the session.
     * @param aSession session in which we have to save the parameter.
     */
    abstract public void saveOptions (HttpServletRequest aRequest, HttpSession aSession);


    /**
     * Return a collection of URL corresponding to the selected protein and source
     * eg. produce a list of GO terms if GO is the source.<br>
     * if the method send back no URL, the given parameter is wrong.
     *
     * @param xRefs The collection of XRef from which we want to get the list of corresponding URL
     * @param selectedXRefs The collection of selected XRef
     * @param applicationPath our application path
     * @param user the current user
     * @return a set of URL pointing on the highlightment source.
     */
    abstract public List getSourceUrls (Collection xRefs, Collection selectedXRefs,
                                        String applicationPath, IntactUserI user)
            throws IntactException, SQLException;


    /**
     * Parse the set of key generate by the source and give back a collection of keys.
     *
     * @param someKeys a string which contains some key separates by a character.
     * @return the splitted version of the key string as a collection of String.
     */
    abstract public Collection parseKeys (String someKeys);

} // HighlightmentSource
