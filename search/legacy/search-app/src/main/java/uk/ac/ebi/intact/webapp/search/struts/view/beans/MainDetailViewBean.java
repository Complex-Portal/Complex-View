package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactSession;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.searchengine.UrlCheckerThread;
import uk.ac.ebi.intact.util.AnnotationFilter;
import uk.ac.ebi.intact.util.SearchReplace;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;
import uk.ac.ebi.intact.webapp.search.business.Constants;

import java.util.*;

/**
 * This view bean is used to provide the information for JSP display relating to Interactions and Experiments. Both
 * these classes require the same JSP view to be displayed, therefore a single bean is used to handle data
 * transformations as required. Note that for large Experiments this bean is also responsible for providing the tabbed
 * content (NOT the means to display it!).
 *
 * @author Chris Lewington
 * @version $Id:MainDetailViewBean.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 */
public class MainDetailViewBean extends AbstractViewBean {

    private static final Log logger = LogFactory.getLog(MainDetailViewBean.class);

    /**
     * This view bean provides beans for an Experiment.
     */
    private Experiment obj;

    /**
     * Flag to identify the view details to be provided - true for Interaction context, false otherwise. Assumes
     * Experiment view by default. Avoids messing around with null values etc for a wrapped Interaction.
     */
    private boolean interactionView;

    /**
     * Cached search URL, set up on first request for it.
     */
    private String objSearchURL;

    /**
     * Cached search URL for the wrapped object's biosource
     */
    private String bioSourceSearchURL;

    /**
     * Cached CvInteraction search URL, (used only by Experiments) set up on first request for it.
     */
    private String cvInteractionSearchURL = "";

    /**
     * Cached CvIdentification search URL, (used only by Experiments) set up on first request for it.
     */
    private String cvIdentificationSearchURL = "";

    /**
     * The intact type of the wrapped AnnotatedObject. Note that only the interface types are relevant for display
     * purposes - thus any concrete 'Impl' types will be considered to be their interface types in this case (eg a
     * wrapped InteractionImpl will have the intact type of 'Interaction'). Would be nice to get rid of the proxies one
     * day ...:-)
     */
    private String intactType;

    /**
     * Cached String for the wrapped object's full biosource name. Must be set to something because it may not exist and
     * JSPs don't like nulls - this is handled in the get method for it.
     */
    private String bioSourceName;

    /**
     * Holds a list of Annotations that may be publicly displayed, ie a filtered list removing those to be excluded
     * (currently rrmakrs and uiprot exports)
     */
    private Collection<Annotation> annotationsForDisplay = new ArrayList<Annotation>();

    /**
     * Map of retrieved DB URLs already retrieved from the DB. This is basically a cache to avoid recomputation every
     * time a CvDatabase URL is requested.
     */
    private Map<CvObject,String> dbUrls;

    /**
     * The List of Interations for display. For Experiments of 'small' size this is the whole list - for 'large'
     * Interactions this is a sublist that is dynamically changed upon different user requests. This is marked as
     * transient because it seems that the java subList is not serializable.
     */
    private Collection<Interaction> interactionList = new ArrayList<Interaction>();

    /**
     * This is only defined for 'Interaction context' views and holds the Interaction that is to be viewed in the
     * context of its Experiment. It is returned transparently to clients wishing to display Interaction details and
     * should be set by bean creators. (eg result Action classes)
     */
    private Interaction wrappedInteraction;

    /**
     * ArrayList to provide a Filter on Gene Names
     */
    private static ArrayList<String> geneNameFilter = new ArrayList<String>( 4 );

    /**
     * The maximum number of pages that can be displayed. This is only of relevance to 'large' Experiments, ie those
     * with more than {@link Constants} MAX_PAGE_SIZE Interactions.
     */
    int maxPages;

    // TODO remove these constants so taht they become available through CVs on CvDatabase( intact ).
    private static final String PMID_PARAM = "${pmid}";
    private static final String YEAR_PARAM = "${year}";

    private static final String FTP_CURRENT_RELEASE = "ftp://ftp.ebi.ac.uk/pub/databases/intact/current";

    private static final String PSI1_URL = FTP_CURRENT_RELEASE + "/psi1/pmid/" + YEAR_PARAM + "/" + PMID_PARAM + ".zip";
    private static final String PSI25_URL = FTP_CURRENT_RELEASE + "/psi25/pmid/" + YEAR_PARAM + "/" + PMID_PARAM + ".zip";

    private static boolean hasYearParam = ( PSI1_URL.indexOf( YEAR_PARAM ) != -1 );
    private static boolean hasPubmedParam = ( PSI1_URL.indexOf( PMID_PARAM ) != -1 );

    private String psi1Url;
    private String psi25Url;

    private UrlCheckerThread urlCheckerPsi1;
    private UrlCheckerThread urlCheckerPsi25;
    private static final String SPACE = "&nbsp;";

    /**
     * The bean constructor requires an Experiment to wrap, plus beans on the context path to the search webapp and
     * the help link.
     *
     * @param obj         The Experiment whose beans are to be displayed
     *
     * @throws NullPointerException     thrown if the object to wrap is null
     * @throws IllegalArgumentException thrown if the parameter is not an Experiment
     */
    public MainDetailViewBean( Experiment obj, List<Interaction> interactionList) {
        super( );
        this.interactionList = interactionList;

        if ( obj == null ) {
            throw new NullPointerException( "MainDetailViewBean: can't display null object!" );
        }

        this.obj = obj;
        dbUrls = new HashMap<CvObject, String>();

        // TODO centralize where the PSI links are created.
        psi1Url = PSI1_URL;
        psi25Url = PSI25_URL;

        if ( hasPubmedParam ) {
            String pubmedId = getPubmedId( obj );

            if ( pubmedId != null ) {
                // create PSI 1 URL
                psi1Url = SearchReplace.replace( psi1Url, PMID_PARAM, pubmedId );

                // create PSI 2.5 URL
                psi25Url = SearchReplace.replace( psi25Url, PMID_PARAM, pubmedId );
            }
        }

        if ( hasYearParam ) {
            String year = getCreatedYear( obj );
            if ( year != null ) {
                // create PSI 1 URL
                psi1Url = SearchReplace.replace( psi1Url, YEAR_PARAM, year );

                // create PSI 2.5 URL
                psi25Url = SearchReplace.replace( psi25Url, YEAR_PARAM, year );
            }
        }

        // Check if the URLs are present
        urlCheckerPsi1 = new UrlCheckerThread( psi1Url );
        urlCheckerPsi1.start();

        urlCheckerPsi25 = new UrlCheckerThread( psi25Url );
        urlCheckerPsi25.start();
    }

    /**
     * Adds the shortLabel of the AnnotatedObject to an internal list used later for highlighting in a display. NOT SURE
     * IF WE STILL NEED THIS!!
     */
    @Override
    public void initHighlightMap() {
        Set<String> set = new HashSet<String>( 1 );
        set.add( obj.getShortLabel() );
        setHighlightMap( set );
    }

    /**
     * Returns the help section. Needs to be reviewed.
     */
    @Override
    public String getHelpSection() {
        return "protein.single.view";
    }

    /**
     * Used to tell the bean whether or not it is to be used for a 'full' Experiment view or simply an 'Interaction
     * context' view.
     *
     * @param interaction The Interaction for which the context is required
     */
    public void setWrappedInteraction( Interaction interaction ) {
        wrappedInteraction = interaction;
        interactionView = true; //set the boolean flag
    }

    /**
     * Determines whether the bean is currentlly set to render a full Experiment view or an 'Interaction context@ view.
     *
     * @return true for a Interaction context view, false otherwise.
     */
    public boolean isInteractionView() {
        return interactionView;
    }

    /**
     * The intact name for an object is its shortLabel. Required in all view types.
     *
     * @return String the object's Intact name.
     */
    public String getObjIntactName() {
        return obj.getShortLabel();
    }

    /**
     * The AnnotatedObject's AC. Required in all view types.
     *
     * @return String the AC of the wrapped object.
     */
    public String getObjAc() {
        return obj.getAc();
    }

    /**
     * This is currently assumed to be the AnnotatedObject's full name. Required by all view types.
     *
     * @return String a description of the AnnotatedObject, or a "-" if there is none.
     */
    public String getObjDescription() {
        if ( obj.getFullName() != null ) {
            return obj.getFullName();
        }
        return "-";
    }

    /**
     * Provides direct access to the wrapped Experiment itself.
     *
     * @return Experiment The reference to the wrapped object.
     */
    public Experiment getObject() {
        return obj;
    }

    /**
     * Provides the basic Intact type of the wrapped Experiment (ie no java package beans). NOTE: only the INTERFACE
     * types are provided as these are the only ones of interest in the model - display pages are not interested in
     * objects of type XXXImpl.
     *
     * @return String The intact type of the wrapped Experiment
     */
    public String getIntactType() {

        if ( intactType == null ) {
            //set on first call
            String className = obj.getClass().getName();
            String basicType = className.substring( className.lastIndexOf( "." ) + 1 );

            //now check for 'Impl' and ignore it...
            intactType = ( ( basicType.indexOf( "Impl" ) == -1 ) ?
                           basicType : basicType.substring( 0, basicType.indexOf( "Impl" ) ) );
        }
        return intactType;

    }

    /**
     * Returns the BioSource of the given Experiment, returns "-" if the BioSource got no valid fullname
     *
     * @return String which contains the fullname of the biosource object
     */
    public String getBioSourceName() {

        if ( bioSourceName == null ) {
            //set on fiirst call
            bioSourceName = obj.getBioSource().getFullName();
            if ( bioSourceName == null ) {
                bioSourceName = "-";   //may not have one, and can't use null in JSPs
            }

        }
        return bioSourceName;
    }

    /**
     * Provides the actual Annotation list of the wrapped object. Clients can use this method to gain access to
     * Annotation data for display. Note: this method filters out those Annotations which are not for public view;
     * currently these are 'remark' and 'uniprot-dr-export' Annotations.
     *
     * @return Collection the list of Annotation objects for the wrapped instance.
     */
    public Collection<Annotation> getFilteredAnnotations() {

        if ( annotationsForDisplay.isEmpty() ) {
            //set on first call
            for (Annotation annotation : obj.getAnnotations())
            {
                //run through the filter
                if (!AnnotationFilter.getInstance().isFilteredOut(annotation))
                {
                    annotationsForDisplay.add(annotation);
                }
            }
        }

        return annotationsForDisplay;
    }

    /**
     * Convenience method to provide a filtered list of Annotations for a given Interaction. Useful in JSP display to
     * apply the same filters of the wrapped Experiment to one of its Interactions.
     *
     * @return Collection the filtered List of Annotations (empty if there are none)
     */
    public Collection<Annotation> getFilteredAnnotations( Interaction interaction ) {

        Collection<Annotation> filteredAnnots = new ArrayList<Annotation>();

        for (Annotation annotation : interaction.getAnnotations())
        {
            //run through the filter
            if (!AnnotationFilter.getInstance().isFilteredOut(annotation))
            {
                filteredAnnots.add(annotation);
            }
        }

        return filteredAnnots;
    }

    /**
     * Filters the Annotations for the 'comment' ones only. This is useful for JSPs wishing to display Annotations in
     * certain orders.
     *
     * @param annots a Collection of Annotations to filter
     *
     * @return Collection a list of the 'comment' Annotations, or empty if there are none.
     */
    public Collection<Annotation> getComments( Collection<Annotation> annots ) {

        Collection<Annotation> comments = new ArrayList<Annotation>();
        for (Annotation annot : annots)
        {
            if (annot.getCvTopic().getShortLabel().equals("comment"))
            {
                comments.add(annot);
            }
        }
        return comments;
    }

    /**
     * Convenience method for obtaining the wrapped object's xrefs. Useful for clients to use in conjunction with other
     * methods on this view bean to provide suitably formatted Xref data.
     *
     * @return Collection the list of xrefs for the wrapped object.
     */
    public Collection<ExperimentXref> getXrefs() {
        return obj.getXrefs();
    }

    /**
     * Convenience method for obtaining the uniprot ID for a given Protein.
     *
     * @param interactor The protein we want the uniprot ID for
     *
     * @return String the uniprot ID of the Protein, or '-' if none found.
     */
    //public String getUniprotLabel( Protein protein ) { //used 3 times in detail.jsp
    public String getPrimaryIdFromXrefIdentity( Interactor interactor ) {

        String primaryId = "-";  //default for display
        Collection<InteractorXref> xrefs = interactor.getXrefs();
        for ( Iterator<InteractorXref> it = xrefs.iterator(); it.hasNext(); ) {
            Xref xref = it.next();
            if ( xref.getCvXrefQualifier() != null ) {
                if ( CvXrefQualifier.IDENTITY.equals( xref.getCvXrefQualifier().getShortLabel() ) ) {
                    primaryId = xref.getPrimaryId();
                    break;  //done
                }
            }
        }
        return primaryId;
    }

    public String getInteractorType( Interactor interactor ) {
        CvInteractorType cvInteractorType = interactor.getCvInteractorType();
        if ( cvInteractorType != null && cvInteractorType.getShortLabel() != null ) {
            return cvInteractorType.getShortLabel();
        } else {
            if ( interactor instanceof Protein ) {
                return "Protein";
            } else if ( interactor instanceof NucleicAcid ) {
                return "Nucleic Acid";
            } else {
                return "-";
            }
        }
    }

    /**
     * Provides the Interactions for the wrapped Experiment.
     * <p/>
     * NOTE: For Experiments that have a large number of Interactions, this method will provide a page of the total
     * list. If the Interaction list is small enough
     * then the complete list is returned in any case. </p>
     *
     * @return Collection a list of Interactions - either all of them or a chunk (of pre-defined size).
     */
    public Collection<Interaction> getInteractions() {

        Collection<Interaction> result = new ArrayList<Interaction>();
        //first check for an 'Interaction context' view - if so then return that one only
        if ( isInteractionView() ) {
            result.add( wrappedInteraction );
        } else {
            result = interactionList;
        }

        return result;
    }


    /**
     * Can be useful for a JSP to find out how many pages can be displayed for the warpped Experiment.
     *
     * @return int The page number of the last possible page for display.
     */
    public int getMaxPage() {
        return maxPages;
    }

    /**
     * This method provides beans of all the Features for an Interaction which are linked to other Features. Feature
     * view beans, rather than the Features themselves, are provided to allow JSPs an easier display method.
     *
     * @param interaction The Interaction we want the linked Features for
     *
     * @return Collection a List of FeatureViewBeans providing a view on each linked Feature of the Interaction.
     */
    public Collection<FeatureViewBean> getLinkedFeatures( Interaction interaction ) {

        //TODO: needs refactoring - perhaps cache some beans for Interactions done...
        Collection<FeatureViewBean> linkedFeatures = new ArrayList<FeatureViewBean>();

        // while we populate the collection of feature to display, we keep track of those are are already going to
        // be display. eg. F1 interacts with F2, when F2 comes again, we don't create a bean for it.
        Collection<Feature> seen = new ArrayList<Feature>();

        //Go through the Features and for each linked one, build a view bean and save it...
        //NB The Feature beans are held inside Collections within each Component of the
        //Interaction - they are called 'binding domains'...
        Collection<Component> components = interaction.getComponents();

        for (Component component : components)
        {
            Collection<Feature> features = component.getBindingDomains();

            for (Feature feature : features)
            {
                if (feature.getBoundDomain() != null)
                {
                    if (!seen.contains(feature))
                    {
                        linkedFeatures.add(new FeatureViewBean(feature));

                        seen.add(feature);

                        if (feature.getBoundDomain().getBoundDomain() == feature)
                        {
                            // if the features relate to each other
                            seen.add(feature.getBoundDomain());
                        }
                    }
                }
            } // features

        } // components

        return linkedFeatures;
    }

    /**
     * Provides a List of view beans for unlinked Features of an Interaction.
     *
     * @param interaction The Interaction we want the single Feature information for.
     *
     * @return Collection a List of FeatureViewBeans for the INteraction's unlinked Features.
     */
    public Collection<FeatureViewBean> getSingleFeatures( Interaction interaction ) {

        //TODO: Needs refactoring - see above....
        Collection<FeatureViewBean> singleFeatures = new ArrayList<FeatureViewBean>();
        //Go through the Features and for each single one, build a view bean and save it...
        //NB The Feature beans are held inside Collections within each Component of the
        //Interaction - they are called 'binding domains'...
        Collection<Component> components = interaction.getComponents();

        for (Component component : components)
        {
            Collection<Feature> features = component.getBindingDomains();

            for (Feature feature : features)
            {
                if (feature.getBoundDomain() == null)
                {
                    singleFeatures.add(new FeatureViewBean(feature));
                }
            }
        } //

        return singleFeatures;
    }

    private boolean hasPsiReference( final AnnotatedObject<? extends Xref, ? extends Alias> ao, final String psiRef ) {
        for (Xref xref : ao.getXrefs())
        {
            if (CvDatabase.PSI_MI.equals(xref.getCvDatabase().getShortLabel()))
            {
                // found a PSI Xref
                if (xref.getCvXrefQualifier() != null
                        &&
                        CvXrefQualifier.IDENTITY.equals(xref.getCvXrefQualifier().getShortLabel()))
                {
                    // found identity
                    return psiRef.equals(xref.getPrimaryId());
                }
            }
        }

        return false;
    }

    private boolean isExperimentalFeature( Feature feature ) {

        if ( feature == null ) {
            throw new IllegalArgumentException();
        }

        CvFeatureType type = feature.getCvFeatureType();
        if ( type == null ) {
            return false;
        }

        // Stack to handle recursive call
        Stack<CvDagObject> stack = new Stack<CvDagObject>();
        stack.push( type );
        while ( ! stack.empty() ) {
            CvDagObject term = stack.pop();

            if ( hasPsiReference( term, CvFeatureType.EXPERIMENTAL_FEATURE_MI_REF ) ) {
                return true;
            }

            // add all children to the stack
            for (CvDagObject parent : term.getParents())
            {
                stack.push(parent);
            }
        }

        return false;
    }

    public Collection<String> getFeaturesSummary( final Interaction interaction ) {

        Collection<String> lines = new ArrayList<String>( 4 );

        Collection<FeatureViewBean> linkedFeatures = getLinkedFeatures( interaction );
        Collection<FeatureViewBean> singleFeatures = getSingleFeatures( interaction );

        int featureCount = linkedFeatures.size() + singleFeatures.size();

        if ( featureCount > 0 ) {

            //  sequence features' title cell, linked to help (spans all feature's rows)
            // The rowspan of this cell is equal to the sum of linked + unlinked Features

            boolean firstItem = true;
            while ( linkedFeatures.size() + singleFeatures.size() > 0 ) {


                boolean islinked = false;
                Iterator<FeatureViewBean> iterator = null;

                if ( ! linkedFeatures.isEmpty() ) {
                    // process lnked feature first
                    iterator = linkedFeatures.iterator();
                    islinked = true;
                } else {
                    iterator = singleFeatures.iterator();
                }

                FeatureViewBean firstFeature = iterator.next();
                iterator.remove(); // take it out of the collection.

                // display that row

                StringBuffer buffer = null;

                if ( islinked ) {

                    buffer = new StringBuffer( 1000 ); // where the current line is stored

                    // check for experimental feature
                    if ( isExperimentalFeature( firstFeature.getFeature() ) ) {
                        buffer.append( "Experimental modification: " );
                    }

                    buffer.append( "<a href=\"" ).append( firstFeature.getCvFeatureTypeSearchURL() );
                    buffer.append( "\">" ).append( firstFeature.getFeatureType( true ) ).append( "</a>" ).append( SPACE );
                    buffer.append( firstFeature.getFeatureName() ).append( " of " ).append( firstFeature.getProteinName() );
                    buffer.append( SPACE );

                    // Now do the Ranges...
                    buffer.append( generateRange( firstFeature.getFeature() ) );

                    //only need some brackets if we have Xrefs to display..
                    if ( !firstFeature.getFeatureXrefs().isEmpty() ) {

                        buffer.append( SPACE ).append( '(' );

                        // link 2
                        for ( Iterator<FeatureXref> iter1 = firstFeature.getFeatureXrefs().iterator(); iter1.hasNext(); ) {
                            Xref xref = iter1.next();

                            buffer.append( "<a href=\"" ).append( firstFeature.getPrimaryIdURL( xref ) ).append( "\">" );
                            buffer.append( xref.getPrimaryId() ).append( "</a>" );

                            if ( iter1.hasNext() ) {
                                buffer.append( ',' ).append( SPACE );
                            }

                        }   //end of Feature Xref loop

                        buffer.append( ')' );

                    }  //end of Xref check

                    if ( firstFeature.hasCvFeatureIdentification() ) {

                        // only display detected by XXX if there is a method
                        buffer.append( " detected by " );
                        buffer.append( "<a href=\"" ).append( firstFeature.getCvFeatureIdentSearchURL() ).append( "\">" );
                        buffer.append( firstFeature.getFeatureIdentificationName() ).append( "</a>" );
                    }

                    //there must be one as we are dealing with linked Features here
                    FeatureViewBean firstBoundFeature = firstFeature.getBoundFeatureView();

                    buffer.append( ", interacts with " );
                    buffer.append( "<a href=\"" ).append( firstBoundFeature.getCvFeatureTypeSearchURL() ).append( "\">" );
                    buffer.append( firstBoundFeature.getFeatureType( false ) ).append( "</a>" ).append( SPACE );
                    buffer.append( firstBoundFeature.getFeatureName() );
                    buffer.append( " of " );
                    buffer.append( firstBoundFeature.getProteinName() );
                    buffer.append( SPACE );

                    //now do the Ranges for the linked Feature (may reuse the other vars)...
                    buffer.append( generateRange( firstBoundFeature.getFeature() ) );

                    //only need brackets if we have Xrefs to display..
                    if ( ! firstBoundFeature.getFeatureXrefs().isEmpty() ) {

                        buffer.append( SPACE ).append( "(" );

                        for ( Iterator<FeatureXref> iter1 = firstBoundFeature.getFeatureXrefs().iterator(); iter1.hasNext(); ) {
                            Xref xref = iter1.next();

                            buffer.append( "<a href=\"" ).append( firstBoundFeature.getPrimaryIdURL( xref ) ).append( "\">" );
                            buffer.append( xref.getPrimaryId() ).append( "</a>" );

                            if ( iter1.hasNext() ) {
                                buffer.append( ',' ).append( SPACE );
                            }

                        }   //end of Feature Xref loop

                        buffer.append( ')' );

                    }  //end of Xref check

                    if ( firstFeature.hasCvFeatureIdentification() ) {
                        // only display detected by XXX if there is a method

                        buffer.append( " detected by " );
                        buffer.append( "<a href=\"" ).append( firstFeature.getCvFeatureIdentSearchURL() ).append( "\">" );
                        buffer.append( firstFeature.getFeatureIdentificationName() ).append( "</a>" );

                    }

                    buffer.append( '.' );

                    // end of linked feature
                } else {

                    // not a linked feature
                    buffer = new StringBuffer( 400 ); // where the current line is stored

                    if ( isExperimentalFeature( firstFeature.getFeature() ) ) {
                        buffer.append( "Experimental modification: " );
                    }

                    buffer.append( "<a href=\"" ).append( firstFeature.getCvFeatureTypeSearchURL() ).append( "\">" );
                    buffer.append( firstFeature.getFeatureType( true ) ).append( "</a>" ).append( SPACE );
                    buffer.append( firstFeature.getFeatureName() );
                    buffer.append( " of " );
                    buffer.append( firstFeature.getProteinName() ).append( SPACE );

                    //now do the Ranges...
                    buffer.append( generateRange( firstFeature.getFeature() ) );

                    if ( ! firstFeature.getFeatureXrefs().isEmpty() ) {

                        buffer.append( SPACE ).append( "(" );

                        for ( Iterator<FeatureXref> iter1 = firstFeature.getFeatureXrefs().iterator(); iter1.hasNext(); ) {
                            Xref xref = iter1.next();

                            buffer.append( "<a href=\"" ).append( firstFeature.getPrimaryIdURL( xref ) ).append( "\">" );
                            buffer.append( xref.getPrimaryId() ).append( "</a>" );

                            if ( iter1.hasNext() ) {
                                buffer.append( ',' ).append( SPACE );
                            }

                        }   //end of Feature Xref loop

                        buffer.append( ')' );

                    }  //end of Xref check

                    //it seems sometimes the detection beans are not present!
                    if ( ! firstFeature.getCvFeatureIdentSearchURL().equals( "" ) ) {

                        buffer.append( SPACE );
                        buffer.append( "<a href=\"" ).append( firstFeature.getCvFeatureIdentSearchURL() ).append( "\">" );
                        buffer.append( firstFeature.getFeatureIdentFullName() ).append( "</a>" );
                    }

                    buffer.append( '.' );

                }  // end of simple feature (no related feature)

                lines.add( buffer.toString() );

            } // while more feature to display
        } // if any feature

        return lines;
    }

    private String generateRange( Feature feature ) {

        Collection<? extends Range> ranges = feature.getRanges();
        String rangeString = "";   //will hold the result (if there is one)
        if ( !ranges.isEmpty() ) {

            StringBuffer buf = new StringBuffer();
            buf.append( "[" );

            for ( Iterator<? extends Range> it1 = ranges.iterator(); it1.hasNext(); ) {
                buf.append( it1.next().toString() );  //The toString of Range does the display format for us
                if ( it1.hasNext() ) {
                    buf.append( ( "," ) );
                }
            }

            buf.append( "]" );
            rangeString = buf.toString();

            if ( rangeString.equalsIgnoreCase( "[?-?]" ) ) {
                rangeString = "[range undetermined]";
            }
        }

        return rangeString;
    }

    /**
     * Convenience method to obtain all of the Proteins for a given Interaction. Note that this method will only return
     * the Proteins, NOT complexes that might be a Component's Interactor.
     *
     * @param interaction The Interaction we want the Proteins for
     *
     * @return Collection a List of the Interaction's Proteins, or empty if none found
     */
//    public Collection getProteins( Interaction interaction ) {// method not used
    public Collection<? extends Interactor> getInteractors( Interaction interaction ) {// method not used

        // sort here with Collections.sort() with the Protein Comparator as
        // so that we get in that the baits before the preys

        Collection<Interactor> results = new ArrayList<Interactor>();
        for ( Iterator<Component> it = interaction.getComponents().iterator(); it.hasNext(); ) {
            Component comp = it.next();
            Interactor interactor = comp.getInteractor();
//            if ( interactor instanceof Protein ) {
            results.add( interactor );
//            }
        }

        return results;
    }

    /**
     * Provides the Component that holds a Protein for a given Interaction. Assumes that a Protein only appears ONCE as
     * a Component of an Interaction.
     *
     * @param interactor  The Protein we are interested in
     * @param interaction The Interaction for which the Protein is of relevance
     *
     * @return Component the Component of the Interaction which holds the Protein, or null if it is not present.
     */
//    public Component getComponent( Protein protein, Interaction interaction ) { // no usage
    public Component getComponent( Interactor interactor, Interaction interaction ) {

        //go through the Components holding the Protein and pull out the Interaction match...
        for ( Iterator<Component> it = interactor.getActiveInstances().iterator(); it.hasNext(); ) {
            Component comp = it.next();
            if ( comp.getInteraction().equals( interaction ) ) {
                return comp;
            }
        }

        return null;

    }

    /**
     * Convenience method to obtain all Gene Names of a Proteins
     *
     * @param interactor The Protein we are interested in
     *
     * @return Collection a Set of Gene Names as Strings, empty if none found
     */
//    public Collection getGeneNames( Protein protein ) {  //1 usage in detail.jsp
    public Collection<String> getGeneNames( Interactor interactor ) {  //1 usage in detail.jsp

        Collection<String> geneNames = new HashSet<String>();
        //geneNames = new StringBuffer();
        //the gene names are obtained from the Aliases for the Protein
        //which are of type 'gene name'...
        Collection<InteractorAlias> aliases = interactor.getAliases();
        for (InteractorAlias alias : aliases)
        {
            if (geneNameFilter.contains(alias.getCvAliasType().getShortLabel()))
            {
                geneNames.add(alias.getName());
            }
        }
        //now strip off trailing comma - if there are any names....
        if ( geneNames.size() == 0 ) {
            geneNames.add( "-" );
        }
        return geneNames;
    }

    /**
     * Provides a String representation of a URL to access the CV related to the Xref (ie the Cv beans describing the
     * Xref's database).
     *
     * @param xref The Xref for which the URL is required
     *
     * @return String a String representation of a URL link for the Xref beans (CvDatabase)
     */
    public String getCvDbURL( Xref xref ) {

        return ( SearchWebappContext.getCurrentInstance().getSearchUrl() + xref.getCvDatabase().getAc() +
                 "&amp;searchClass=CvDatabase&amp;" );
    }

    /**
     * Provides a String representation of a URL to access the CV qualifier info related to the Xref (ie the Cv beans
     * describing the Xref's qualifier info).
     *
     * @return String a String representation of a URL link for the Xref beans (CvXrefQualifier)
     */
    public String getCvQualifierURL( CvXrefQualifier cvXrefQualifier) {
         return ( SearchWebappContext.getCurrentInstance().getSearchUrl() + cvXrefQualifier.getAc() +
                     "&amp;searchClass=CvXrefQualifier&amp;" );
    }

    public String getCvDatabaseSearchUrl( CvDatabase cvDatabase ) {
        String searchUrl = null;
        Collection<Annotation> annotations = cvDatabase.getAnnotations();
        for ( Iterator<Annotation> iterator = annotations.iterator(); iterator.hasNext(); ) {
            Annotation annotation = iterator.next();
            if ( annotation.getCvTopic().getShortLabel().equals( CvTopic.SEARCH_URL ) ) {
                searchUrl = annotation.getAnnotationText();
                break;
            }
        }
        return searchUrl;
    }

    /**
     * Provides a String representation of a URL to provide acces to an Xrefs' database (curently via AC). The URL is at
     * present stored via an Annotation for the Xref in the Intact DB itself.
     *
     * @param xref The Xref for which the DB URL is required
     *
     * @return String a String representation of a DB URL link for the Xref, or a '-' if there is no stored URL link for
     *         this Xref
     */
    public String getPrimaryIdURL( Xref xref ) {

        // Check if the id can be hyperlinked
        String searchUrl = dbUrls.get( xref.getCvDatabase() );
        if ( searchUrl == null ) {
            searchUrl = getCvDatabaseSearchUrl( xref.getCvDatabase() );
            dbUrls.put( xref.getCvDatabase(), searchUrl );
        }

        //if it isn't null, fill it in properly and return
        if ( searchUrl != null ) {
            //An Xref's primary can't be null - the constructor doesn't allow it..
            searchUrl = SearchReplace.replace( searchUrl, "${ac}", xref.getPrimaryId() );

        }
        return searchUrl;
    }

    /**
     * Provides a serahc URL for the Uniprot databse entry of the specified Protein.
     *
     * @param interactor The Protein we are interested in
     *
     * @return String a uniprot search URL for the Protein, or '-' if none found
     */
//    public String getUniprotSearchURL( Protein protein ) { // 1 usage in detail.jsp
    public String getIdentityXrefSearchURL( Interactor interactor ) { // 1 usage in detail.jsp

        Collection<InteractorXref> xrefs = interactor.getXrefs();
        String url = "-";
        for ( Iterator<InteractorXref> it = xrefs.iterator(); it.hasNext(); ) {
            Xref xref = it.next();
            if ( xref.getCvXrefQualifier() != null ) {
                if ( CvXrefQualifier.IDENTITY.equals( xref.getCvXrefQualifier().getShortLabel() ) ) {
                    url = this.getPrimaryIdURL( xref );
                    break;  //done
                }
            }
        }
        logger.debug( "Interactor " + interactor.getAc() + " has url " + url );
        return url;

    }

    /**
     * Provides a String representation of a URL to perform a search on this Experiment's beans (curently via AC)
     *
     * @return String a String representation of a search URL link for the wrapped Experiment
     */
    public String getObjSearchURL() {

        if ( objSearchURL == null ) {
            //set it on the first call
            //NB need to get the correct intact type of the wrapped object
            objSearchURL = SearchWebappContext.getCurrentInstance().getSearchUrl() + obj.getAc() + "&amp;searchClass=" + getIntactType() +
                           "&filter=ac";
        }
        return objSearchURL;
    }

    /**
     * Provides a String representation of a URL to perform a search on CvInteraction
     *
     * @return String a String representation of a search URL link for CvInteraction, or "-" if there is No
     *         CvInteraction defined for the object.
     */
    public String getCvInteractionSearchURL() {

        if ( obj.getCvInteraction() == null ) {
            return "-";
        }
        if ( cvInteractionSearchURL == "" ) {
            //set it on the first call
            //get the CvInteraction object and pull out its AC
            cvInteractionSearchURL = SearchWebappContext.getCurrentInstance().getSearchUrl() + obj.getCvInteraction().getAc()
                                     + "&amp;searchClass=CvInteraction&filter=ac";
        }
        return cvInteractionSearchURL;
    }

    /**
     * Provides a String representation of a URL to perform a search on CvIdentification (Experiments only)
     *
     * @return String a String representation of a search URL link for CvIdentification, or "-" if there is NO
     *         CvIdentification specified for the object.
     */
    public String getCvIdentificationSearchURL() {

        if ( obj.getCvIdentification() == null ) {
            return "-";
        }
        if ( cvIdentificationSearchURL == "" ) {
            //set it on the first call
            //get the CvIdentification object and pull out its AC
            cvIdentificationSearchURL = SearchWebappContext.getCurrentInstance().getSearchUrl() + obj.getCvIdentification().getAc()
                                        + "&amp;searchClass=CvIdentification&filter=ac";
        }
        return cvIdentificationSearchURL;
    }

    /**
     * Provides a String representation of a URL to perform a search on CvInteractionType (Interactions only)
     *
     * @return String a String representation of a search URL link for CvInteractionType, or "-" if there is NO
     *         CvInteractionType specified for the interaction.
     */
    public String getCvInteractionTypeSearchURL( Interaction interaction ) {

        if ( interaction.getCvInteractionType() == null ) {
            return "-";
        }
        return SearchWebappContext.getCurrentInstance().getSearchUrl() + interaction.getCvInteractionType().getAc()
               + "&amp;searchClass=CvInteractionType&filter=ac";
    }

    /**
     * Provides a String representation of a URL to perform a search on BioSource.
     *
     * @return String a String representation of a search URL link for BioSource.
     */
    public String getBioSourceSearchURL() {

        if ( bioSourceSearchURL == null ) {
            //set it on the first call
            bioSourceSearchURL = SearchWebappContext.getCurrentInstance().getSearchUrl() + obj.getBioSource().getAc()
                                 + "&amp;searchClass=BioSource";
        }
        return bioSourceSearchURL;
    }

    /**
     * Provides a String representation of URL to perform a search to the BioSource Object from the give Protein
     *
     * @param interactor the Protein in which we are interested in
     *
     * @return String contains the URL
     */
//    public String getProteinBiosourceURL( Protein protein ) { // no usage
    public String getInteractorBiosourceURL( Interactor interactor ) {
        return SearchWebappContext.getCurrentInstance().getSearchUrl() + interactor.getBioSource().getAc() + "&amp;searchClass=BioSource";
    }

    /**
     * Provides a String representation of the URL to perform a search to the BioSource Object
     *
     * @param bioSource the BioSource in which we are interested in
     *
     * @return Sring contains the URL
     */
    public String getBiosourceURL( BioSource bioSource ) {

        if ( bioSource != null ) {
            return SearchWebappContext.getCurrentInstance().getSearchUrl() + bioSource.getAc() + "&amp;searchClass=BioSource";
        } else {
            return "-";
        }
    }

    /**
     * Provides a String representation of the bioSource name for the used in the for the interacting molecules.
     *
     * @param bioSource
     *
     * @return String a String representation of the name of the BioSource
     */
    public String getBioSourceName( BioSource bioSource ) {

        if ( bioSource != null && bioSource.getShortLabel() != null ) {
            return bioSource.getShortLabel();
        }
        return "-";

    }

    /**
     * Provides a String representation of the bioSource name for the used in the for the host in the experiemnt
     *
     * @return String a String representation of the host name of the Experiment
     */
    public String getExperimentBioSourceName() {
        BioSource bioSource = obj.getBioSource();
        if ( bioSource != null ) {
            return bioSource.getShortLabel();
        } else {
            return "-";
        }
    }

    /**
     * Convenience method to provide a String representation of a URL to perform a search on CvTopic for a particular
     * Annotation
     *
     * @param annot The Annotation we want a search URL for
     *
     * @return String a String representation of a search URL link for CvInteraction.
     */
    public String getCvTopicSearchURL( Annotation annot ) {

        return SearchWebappContext.getCurrentInstance().getSearchUrl() + annot.getCvTopic().getAc() + "&amp;searchClass=CvTopic&filter=ac";
    }

    /**
     * Convenience method to provide a String representation of a URL to perform a search on CvComponentRole for a
     * particular Protein/Interaction pair
     *
     * @param comp The Component role we want a search URL for
     *
     * @return String a String representation of a search URL link for CvComponentRole.
     */
    public String getCvExperimentalRoleSearchURL( Component comp ) {

        return SearchWebappContext.getCurrentInstance().getSearchUrl() + comp.getCvExperimentalRole().getAc() +
               "&amp;searchClass=CvExperimentalRole&filter=ac";
    }

    /**
     * Convenience method to provide a String representation of a URL to perform a search on CvComponentRole for a
     * particular Protein/Interaction pair
     *
     * @param comp The Component role we want a search URL for
     *
     * @return String a String representation of a search URL link for CvComponentRole.
     */
    public String getCvBiologicalRoleSearchURL( Component comp ) {

        return SearchWebappContext.getCurrentInstance().getSearchUrl() + comp.getCvBiologicalRole().getAc() +
               "&amp;searchClass=CvBiologicalRole&filter=ac";
    }

    /**
     * Convenience method to provide a String representation of a URL to perform a search on Protein. This method will
     * provide a Protein 'detail' view.
     *
     * @param interactor The interactor we want a search URL for
     *
     * @return String a String representation of a search URL link for Protein.
     */

//public String getProteinSearchURL( Protein prot ) { // 1 usage in detail.jsp
    public String getInteractorSearchURL( Interactor interactor ) {

        SearchClass sc = SearchClass.valueOfMappedClass(interactor.getClass());

        return SearchWebappContext.getCurrentInstance().getSearchUrl() + interactor.getAc() + "&amp;searchClass="+sc.getShortName()+"&amp;view=single&filter=ac";
    }

    /**
     * Convenience method to provide a String representation of a URL to perform a search on Protein. This method will
     * provide a Protein 'partners' view.
     *
     * @param interactor The Protein we want a search URL for
     *
     * @return String a String representation of a search URL link for Protein.
     */
//    public String getProteinPartnerURL( Protein prot ) {  // 1 usage in detail.jsp
    public String getInteractorPartnerURL( Interactor interactor ) {  // 1 usage in detail.jsp
        SearchClass searchClass = SearchClass.valueOfMappedClass(interactor.getClass());
        return SearchWebappContext.getCurrentInstance().getSearchUrl() + interactor.getAc() + "&amp;searchClass="+searchClass.getShortName()+"&amp;view=partner&filter=ac";
    }

    //////////////////////////
    // PSI links

    private static String getCreatedYear( Experiment exp ) {

        Date created = exp.getCreated();
        java.sql.Date d = new java.sql.Date( created.getTime() );
        Calendar c = new GregorianCalendar();
        c.setTime( d );

        int year = c.get( Calendar.YEAR );

        return String.valueOf( year );
    }

    private String getPubmedId( Experiment experiment ) {

        for ( Iterator<ExperimentXref> iterator = experiment.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = iterator.next();

            if ( xref.getCvDatabase().getShortLabel().equals( CvDatabase.PUBMED ) ) {
                CvXrefQualifier qualifier = xref.getCvXrefQualifier();

                if ( qualifier != null && qualifier.getShortLabel().equals( CvXrefQualifier.PRIMARY_REFERENCE ) ) {
                    // found it
                    return xref.getPrimaryId();
                }
            }
        }
        return null;
    }

    public String getPsi1Url() {
        return psi1Url;
    }

    public String getPsi25Url() {
        return psi25Url;
    }

    public boolean hasPsi1URL() {
        if ( ! urlCheckerPsi1.hasFinished( 500 ) ) {
            logger.error( "The checking of PSI 1.0 URL (" + urlCheckerPsi1.getUrl() + ") could not complete on time." );
        }

        return urlCheckerPsi1.isValidUrl();
    }

    public boolean hasPsi25URL() {
        if ( ! urlCheckerPsi25.hasFinished( 500 ) ) {
            logger.error( "The checking of PSI 2.5 URL (" + urlCheckerPsi25.getUrl() + ") could not complete on time." );
        }

        return urlCheckerPsi25.isValidUrl();
    }
}