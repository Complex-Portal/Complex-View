package uk.ac.ebi.intact.service.complex.ws;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Controller
public class SearchController {

    /*
     -- BASIC KNOWLEDGE ABOUT SPRING MVC CONTROLLERS --
      * They look like the next one:
      @RequestMapping(value = "/<path to listen>/{<variable>}")
	  public <ResultType> search(@PathVariable String <variable>) {
          ...
	  }

	  * First of all, we have the @RequestMapping annotation where you can
	    use these options:
	     - headers: Same format for any environment: a sequence of
	                "My-Header=myValue" style expressions
	     - method: The HTTP request methods to map to, narrowing the primary
	               mapping: GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE.
	     - params: Same format for any environment: a sequence of
	               "myParam=myValue" style expressions
	     - value: Ant-style path patterns are also supported (e.g. "/myPath/*.do").

      * Next we have the function signature, with the result type to return,
        the name of the function and the parameters to it. We could see the
        @PathVariable in the parameters it is to say that the content between
        { and } is assigned to this variable. NOTE: They must have the same name

        Moreover, we can have @RequestedParam if we need to read or use a parameter
        provided using "?name=value" way. WE WANT TO DO THAT WITH THE FORMAT,
        BUT THIS PARAMETER IS CONTROLLED BY THE ContentNegotiatingViewResolver BEAN
        IN THE SPRING FILE.
     */

    /********************************/
    /*      Private attributes      */
    /********************************/
    @Autowired
    private DataProvider dataProvider ;
    @PersistenceContext(unitName="intact-core-default")
    private EntityManager complexManager;
    @Autowired
    private DaoFactory daoFactory;
    private static final Log log = LogFactory.getLog(SearchController.class);

    /****************************/
    /*      Public methods      */
    /****************************/
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showHomeHelp(){
        return "home";
    }
    @RequestMapping(value = "/search/", method = RequestMethod.GET)
    public String showSearchHelp(){
        return "search";
    }
    @RequestMapping(value = "/details/", method = RequestMethod.GET)
    public String showDetailsHelp(){
        return "details";
    }
    @RequestMapping(value = "/count/{query}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String count(@PathVariable String query, ModelMap model) throws SolrServerException {
        String q = null;
        try {
            q = URIUtil.decode(query);
        } catch (URIException e) {
            e.printStackTrace();
        }
        long total = query(q, null, null, null, null).getTotalNumberOfResults();
        model.addAttribute("count", total);
        return "count";
    }
    /*
     - We can access to that method using:
         http://<servername>:<port>/search/<something to query>
       and
         http://<servername>:<port>/search/<something to query>?format=<type>
     - If we do not use the format parameter we will receive the answer in json
     - Only listen request via GET never via POST.
     - Does not change the query.
     */
    @RequestMapping(value = "/search/{query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ComplexRestResult search(@PathVariable String query,
                                    @RequestParam (required = false) String first,
                                    @RequestParam (required = false) String number,
                                    @RequestParam (required = false) String filters,
                                    @RequestParam (required = false) String facets) throws SolrServerException {
        return query(query, first, number, filters, facets);
	}

    /*
     - We can access to that method using:
         http://<servername>:<port>/interactor/<something to query>
       and
         http://<servername>:<port>/interactor/<something to query>?format=<type>
     - If we do not use the format parameter we will receive the answer in json
     - Only listen request via GET never via POST.
     - Force to query only in the id, alias and pxref fields.
     */
    @RequestMapping(value = "/interactor/{query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComplexRestResult searchInteractor(@PathVariable String query,
                                              @RequestParam (required = false) String first,
                                              @RequestParam (required = false) String number) throws SolrServerException {

        // Query improvement. Force to query only in the id, alias and pxref
        // fields.
        List<String> fields = new ArrayList<String>();
        fields.add(ComplexFieldNames.INTERACTOR_ID);
        fields.add(ComplexFieldNames.INTERACTOR_ALIAS);
        fields.add(ComplexFieldNames.INTERACTOR_XREF);
        // Retrieve data using that parameters and return it
        return query(improveQuery(query, fields), first, number, null, null);
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/complex/<something to query>
       and
         http://<servername>:<port>/complex/<something to query>?format=<type>
     - If we do not use the format parameter we will receive the answer in json
     - Only listen request via GET never via POST.
     - Force to query only in the complex_id, complex_alias and complex_xref
       fields.
     */
    @RequestMapping(value = "/complex/{query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComplexRestResult searchInteraction(@PathVariable String query,
                                               @RequestParam (required = false) String first,
                                               @RequestParam (required = false) String number) throws SolrServerException {

        // Query improvement. Force to query only in the complex_id,
        // complex_alias and complex_xref fields.
        List<String> fields = new ArrayList<String>();
        fields.add(ComplexFieldNames.COMPLEX_ID);
        fields.add(ComplexFieldNames.COMPLEX_ALIAS);
        fields.add(ComplexFieldNames.COMPLEX_XREF);
        // Retrieve data using that parameters and return it
        return query(improveQuery(query, fields), first, number, null, null);
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/organism/<something to query>
       and
         http://<servername>:<port>/organism/<something to query>?format=<type>
     - If we do not use the format parameter we will receive the answer in json
     - Only listen request via GET never via POST.
     - Force to query only in the organism_name and species fields.
     */
    @RequestMapping(value = "/organism/{query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComplexRestResult searchOrganism(@PathVariable String query,
                                            @RequestParam (required = false) String first,
                                            @RequestParam (required = false) String number) throws SolrServerException {

        // Query improvement. Force to query only in the organism_name and
        // species (complex_organism) fields.
        List<String> fields = new ArrayList<String>();
        fields.add(ComplexFieldNames.ORGANISM_NAME);
        fields.add(ComplexFieldNames.COMPLEX_ORGANISM);
        // Retrieve data using that parameters and return it
        return query(improveQuery(query, fields), first, number, null, null);
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/details/<ac of a complex>
       and
         http://<servername>:<port>/details/<ac of a complex>?format=<type>
     - If we do not use the format parameter we will receive the answer in json
     - Only listen request via GET never via POST.
     - Query the information in our database about the ac of the complex.
     */
    @RequestMapping(value = "/details/{ac}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public @ResponseBody ComplexDetails retrieveComplex(@PathVariable String ac) throws Exception {
        InteractionDao interactionDao = daoFactory.getInteractionDao();
        InteractionImpl complex = interactionDao.getByAc(ac);
        ComplexDetails details = null;
        // Function
        if ( complex != null ) {
            details = new ComplexDetails();
            details.setAc(complex.getAc());
            details.setFunction         ( ComplexUtils.getFunction          ( complex ) );
            details.setProperties       ( ComplexUtils.getProperties        ( complex ) );
            details.setDisease          ( ComplexUtils.getDisease           ( complex ) );
            details.setLigand           ( ComplexUtils.getLigand            ( complex ) );
            details.setComplexAssembly  ( ComplexUtils.getComplexAssembly   ( complex ) );
            details.setName             ( getComplexName                    ( complex ) );
            details.setSynonyms         ( getComplexSynonyms                ( complex ) );
            details.setSystematicName   ( ComplexUtils.getSystematicName    ( complex ) );
            details.setSpecies          ( ComplexUtils.getSpeciesName       ( complex ) + "; " +
                                          ComplexUtils.getSpeciesTaxId      ( complex ) );

            setParticipants(complex, details);
            setCrossReferences(complex, details);
        }
        else{
            throw new Exception();
        }
        return details;
    }

    /*******************************/
    /*      Protected methods      */
    /*******************************/
    protected static List<String> getComplexSynonyms(InteractionImpl complex) {
        List<String> synosyms = new ArrayList<String>();
        for (Alias alias : complex.getAliases()) {
            if (alias.getName() != null && alias.getCvAliasType() != null && alias.getCvAliasType().getIdentifier().equals(CvAliasType.COMPLEX_SYNONYM_NAME_MI_REF)) {
                synosyms.add(alias.getName());
            }
        }
        return synosyms;
    }

    protected String getComplexName(InteractionImpl complex){
        String name = ComplexUtils.getRecommendedName(complex);
        if (name != null) return name;
        name = ComplexUtils.getSystematicName(complex);
        if (name != null) return name;
        List<String> synonyms = getComplexSynonyms(complex);
        if (! synonyms.isEmpty()) return synonyms.get(0);
        name = ComplexUtils.getFirstAlias(complex);
        if (name != null) return name;
        return complex.getShortLabel();
    }

    // This method controls the first and number parameters and retrieve data
    protected ComplexRestResult query(String query, String first, String number, String filters, String facets) throws SolrServerException {
        // Get parameters (if we have them)
        int f, n;
        // If we have first parameter parse it to integer
        if ( first != null ) f = Integer.parseInt(first);
            // else set first parameter to 0
        else f = 0;
        // If we have number parameter parse it to integer
        if ( number != null ) n = Integer.parseInt(number);
            // else set number parameter to max integer - first (to avoid problems)
        else n = Integer.MAX_VALUE - f;
        // Retrieve data using that parameters and return it
        return this.dataProvider.getData( query, f, n, filters , facets);
    }

    // This method is to force to query only for a list of fields
    protected String improveQuery(String query, List<String> fields) {
        StringBuilder improvedQuery = new StringBuilder();
        for ( String field : fields ) {
            improvedQuery.append(field)
                    .append(":(")
                    .append(query)
                    .append(")");
        }
        return improvedQuery.toString();
    }

    // This method fills the cross references table for the view
    protected void setCrossReferences(InteractionImpl complex, ComplexDetails details) {
        Collection<ComplexDetailsCrossReferences> crossReferences = details.getCrossReferences();
        ComplexDetailsCrossReferences cross;
        for ( Xref xref : complex.getXrefs()) {
            cross = new ComplexDetailsCrossReferences();
            CvDatabase cvDatabase = xref.getCvDatabase();
            CvXrefQualifier cvXrefQualifier = xref.getCvXrefQualifier();
            String primaryId = xref.getPrimaryId();
            String secondayId = xref.getSecondaryId();
            cross.setIdentifier(primaryId);
            cross.setDescription(secondayId);
            cross.setDatabase(cvDatabase.getFullName() != null ? cvDatabase.getFullName() : cvDatabase.getShortLabel());
            cross.setDbMI(cvDatabase.getIdentifier());
            for ( Annotation annotation : cvDatabase.getAnnotations() ) {
                if ( annotation.getCvTopic() != null && CvTopic.SEARCH_URL_MI_REF.equals(annotation.getCvTopic().getIdentifier()) ) {
                    cross.setSearchURL(annotation.getAnnotationText().replaceAll("\\$*\\{ac\\}",primaryId));
                }
                else if( annotation.getCvTopic().getShortLabel().equalsIgnoreCase(CvTopic.DEFINITION) ){
                    cross.setDbdefinition(annotation.getAnnotationText());
                }
            }
            if( cvXrefQualifier != null ) {
                setXrefQualifier(cross, cvXrefQualifier);
            }
            crossReferences.add(cross);
        }
    }

    // This method fills the participants table for the view
    protected void setParticipants(InteractionImpl complex, ComplexDetails details) {
        Collection<ComplexDetailsParticipants> participants = details.getParticipants();
        ComplexDetailsParticipants part;
        for ( Component component : complex.getComponents() ) {
            part = new ComplexDetailsParticipants();
            Interactor interactor = component.getInteractor();
            if ( interactor != null ) {
                part.setInteractorAC(interactor.getAc());
                part.setDescription(interactor.getFullName());
                Xref xref = null;
                if (CvObjectUtils.isProteinType(interactor.getCvInteractorType())) {
                    xref = ProteinUtils.getUniprotXref(interactor);

                    String geneName = null;
                    for (Alias alias : interactor.getAliases()) {
                        if ( alias.getCvAliasType() != null && CvAliasType.GENE_NAME_MI_REF.equals(alias.getCvAliasType().getIdentifier())) {
                            geneName = alias.getName();
                            break;
                        }
                    }
                    part.setName(geneName !=null ? geneName : interactor.getShortLabel());

                }
                else if( CvObjectUtils.isSmallMoleculeType(interactor.getCvInteractorType()) || CvObjectUtils.isPolysaccharideType(interactor.getCvInteractorType()) ){
                    xref = SmallMoleculeUtils.getChebiXref(interactor);
                    part.setName(interactor.getShortLabel());
                }
                else {
                    part.setName(interactor.getShortLabel());
                    xref = XrefUtils.getIdentityXref(interactor, CvDatabase.ENSEMBL_MI_REF);
                    xref = xref != null ? xref : XrefUtils.getIdentityXref(interactor, "MI:1013");
                }
                if (xref != null) {
                    part.setIdentifier(xref.getPrimaryId());
                    for ( Annotation annotation : xref.getCvDatabase().getAnnotations() ) {
                        if ( annotation.getCvTopic() != null && CvTopic.SEARCH_URL_MI_REF.equals(annotation.getCvTopic().getIdentifier()) ) {
                            part.setIdentifierLink(annotation.getAnnotationText().replaceAll("\\$*\\{ac\\}", xref.getPrimaryId()));
                        }
                    }
                }
                setInteractorType(part, interactor.getCvInteractorType());
            }
            part.setStochiometry(component.getStoichiometry() == 0.0f ? null : Float.toString(component.getStoichiometry()));
            if (component.getCvBiologicalRole() != null) {
                setBiologicalRole(part, component.getCvBiologicalRole());
            }

            setFeatures(part, component);

            participants.add(part);
        }
    }

    // this method fills the linked features and the other features cells in the participants table
    protected void setFeatures(ComplexDetailsParticipants part, Component component) {
        for( Feature feature : component.getFeatures() ) {
            ComplexDetailsFeatures complexDetailsFeatures = new ComplexDetailsFeatures();
            if ( feature.getBoundDomain() != null ) {
                part.getLinkedFeatures().add(complexDetailsFeatures);
                Component featureComponent = feature.getBoundDomain().getComponent();
                if (featureComponent != null) {
                    Interactor linkedInteractor = featureComponent.getInteractor();
                    if ( linkedInteractor != null ) {
                        Xref xref = null;
                        if (CvObjectUtils.isProteinType(linkedInteractor.getCvInteractorType())) {
                            xref = ProteinUtils.getUniprotXref(linkedInteractor);
                        }
                        else if( CvObjectUtils.isSmallMoleculeType(linkedInteractor.getCvInteractorType()) || CvObjectUtils.isPolysaccharideType(linkedInteractor.getCvInteractorType()) ){
                            xref = SmallMoleculeUtils.getChebiXref(linkedInteractor);
                        }
                        else {
                            xref = XrefUtils.getIdentityXref(linkedInteractor, CvDatabase.ENSEMBL_MI_REF);
                            xref = xref != null ? xref : XrefUtils.getIdentityXref(linkedInteractor, "MI:1013");
                        }
                        if (xref != null) {
                            complexDetailsFeatures.setParticipantId(xref.getPrimaryId());
                        }
                    }
                }
            }
            else {
                part.getOtherFeatures().add(complexDetailsFeatures);
            }
            if (feature.getCvFeatureType() != null) {
                setFeatureType(complexDetailsFeatures, feature.getCvFeatureType(), component);
            }
            for ( Range range : feature.getRanges() ) {
                complexDetailsFeatures.getRanges().add(FeatureUtils.convertRangeIntoString(range));
            }
        }
    }


    // This method is a generic method to get the annotations of a CvDagObject
    protected String getAnnotation(CvDagObject cv) {
        if (cv != null){
            for ( Annotation annotation : cv.getAnnotations() ) {
                if( annotation.getCvTopic().getShortLabel().equalsIgnoreCase(CvTopic.DEFINITION) ){
                    return annotation.getAnnotationText();
                }
            }
        }
        return null;
    }

    // This method sets the interactor type information
    protected void setInteractorType(ComplexDetailsParticipants part, CvInteractorType cvInteractorType) {
        part.setInteractorType(cvInteractorType.getFullName() != null ? cvInteractorType.getFullName() : cvInteractorType.getShortLabel());
        part.setInteractorTypeMI(cvInteractorType.getIdentifier());
        String annotation = getAnnotation(cvInteractorType);
        if (annotation != null) {
            part.setInteractorTypeDefinition(annotation);
        }
    }

    // This method sets the biological role information
    protected void setBiologicalRole(ComplexDetailsParticipants part, CvBiologicalRole cvBiologicalRole) {
        part.setBioRole(cvBiologicalRole.getFullName() != null ? cvBiologicalRole.getFullName() : cvBiologicalRole.getShortLabel());
        part.setBioRoleMI(cvBiologicalRole.getIdentifier());
        String annotation = getAnnotation(cvBiologicalRole);
        if (annotation != null) {
            part.setBioRoleDefinition(annotation);
        }
    }

    // This method sets the feature type information
    protected void setFeatureType(ComplexDetailsFeatures complexDetailsFeatures, CvFeatureType feature, Component component) {
        complexDetailsFeatures.setFeatureType(feature.getFullName() != null ? feature.getFullName() : feature.getShortLabel());
        complexDetailsFeatures.setFeatureTypeMI(feature.getIdentifier());
        String annotation = getAnnotation(component.getCvBiologicalRole());
        if (annotation != null) {
            complexDetailsFeatures.setFeatureTypeDefinition(annotation);
        }
    }

    // This method sets the xref qualifier information
    protected void setXrefQualifier(ComplexDetailsCrossReferences cross, CvXrefQualifier cvXrefQualifier) {
        cross.setQualifier(cvXrefQualifier.getFullName() != null ? cvXrefQualifier.getFullName() : cvXrefQualifier.getShortLabel());
        cross.setQualifierMI(cvXrefQualifier.getIdentifier());
        String annotation = getAnnotation(cvXrefQualifier);
        if (annotation != null) {
            cross.setQualifierDefinition(annotation);
        }
    }

    @ExceptionHandler(SolrServerException.class)
    public ModelAndView handleSolrServerException(SolrServerException e, HttpServletResponse response){
        ModelAndView model = new ModelAndView("error/503");
        response.setStatus(503);
        return model;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception e, HttpServletResponse response){
        ModelAndView model = new ModelAndView("error/404");
        response.setStatus(404);
        return model;
    }

}
