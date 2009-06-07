/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.util;

import org.joda.time.DateTime;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Functions to be used in the UI
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class Functions {

    private static final Log log = LogFactory.getLog( Functions.class );

    private static final String MI_TO_XREF_URL_MAP_PARAM = Functions.class+".MI_TO_XREF_URL_MAP";
    private static final String MI_TO_CV_MAP_PARAM = Functions.class+".MI_TO_CV_MAP";
    private static final String PUBMED_NCBI_URL="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=${ac}&dopt=Abstract";
    private static final String CITEXPLORE_URL="http://www.ebi.ac.uk/citexplore/citationDetails.do?externalId=${ac}&dataSource=MED";

    private static final String OLD_NEWT_URL = "http://www.ebi.ac.uk/newt/display?search=${ac}";
    private static final String NEW_NEWT_URL = "http://www.uniprot.org/taxonomy/${ac}";

    private Functions() {
    }

    /**
     * Calculates which ID to pass to Dasty, depending on the interactor type
     */
    public static String getIdentifierForDasty(Interactor interactor) {
        if (interactor instanceof Protein && ProteinUtils.isFromUniprot((Protein) interactor)) {
            return ProteinUtils.getUniprotXref((Protein)interactor).getPrimaryId();
        }

        return interactor.getAc();
    }


    /**
     * Calculates the XREFs, associated to an MI and for the AC/query provided
     * @param facesContext Needed to cache the map of URLs in the session
     * @param mi Category (MI) to use
     * @param ac Accession to use in the URL
     * @return
     */
    public static String calculateXrefUrl(FacesContext facesContext, String mi, String ac) {
        Map<String, String> miToXrefUrl = (Map<String, String>) ((HttpSession) facesContext.getExternalContext().getSession(false))
                .getAttribute(MI_TO_XREF_URL_MAP_PARAM);

        if (miToXrefUrl == null) {
            miToXrefUrl = new HashMap<String, String>();
            ((HttpSession) facesContext.getExternalContext().getSession(false))
                    .setAttribute(MI_TO_XREF_URL_MAP_PARAM, miToXrefUrl);
        }

        String xrefUrl = null;

        if (miToXrefUrl.containsKey(mi)) {
            xrefUrl = miToXrefUrl.get(mi);
        } else {
            CvObjectDao<CvObject> cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();

            CvObject cvObject = cvObjectDao.getByPsiMiRef(mi);

            if (cvObject != null) {
                Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(cvObject, CvTopic.SEARCH_URL);

                if (annotation != null) {
                    xrefUrl = annotation.getAnnotationText();
                }
            }

            // even store nulls, so the queries are not performed again
            miToXrefUrl.put(mi, xrefUrl);
        }

        String replacedUrl = null;

        if (xrefUrl != null) {
            replacedUrl = xrefUrl.replaceAll("\\$\\{ac\\}", ac);
        }

        return replacedUrl;
    }


    public static CvObject getCvObjectFromIdentifier( FacesContext facesContext, String mi,String cvType ) {

        final HttpSession session = ( HttpSession ) facesContext.getExternalContext().getSession( false );
        Map<String, CvObject> miToCv = ( Map<String, CvObject> ) session.getAttribute( MI_TO_CV_MAP_PARAM );

        if ( miToCv == null ) {
            miToCv = new HashMap<String, CvObject>();
            session.setAttribute( MI_TO_CV_MAP_PARAM, miToCv );
        }

        CvObject cvObject;
        if ( miToCv.containsKey( mi ) ) {
            cvObject = miToCv.get( mi );
        } else {
            //have to do this way, because query with MI:0499 (unspecified role) returns more than one row and IntactException is thrown
            CvObjectDao<CvObject> cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
            if ( "expRole".equals( cvType ) ) {
                final CvExperimentalRole cvExpRole = cvObjectDao.getByPrimaryId( CvExperimentalRole.class, mi );
                cvObject = cvExpRole;
            } else if ( "bioRole".equals( cvType ) ) {
                final CvBiologicalRole cvBioRole = cvObjectDao.getByPrimaryId( CvBiologicalRole.class, mi );
                cvObject = cvBioRole;
            } else {
                cvObject = cvObjectDao.getByPsiMiRef( mi );
            }
            // even store nulls, so the queries are not performed again
            miToCv.put( mi, cvObject );
        }
        return cvObject;
    }

    /**
     * Calculates the XREFs, associated to a database and for the AC/query provided
     * @param db the CvDatabase to get the URL template from
     * @param ac Accession to use in the URL
     * @return
     */
    public static String calculateXrefUrl(CvDatabase db, String ac) {
        String xrefUrl = null;

        if (db != null) {
            Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(db, CvTopic.SEARCH_URL);

            if (annotation != null) {
                xrefUrl = annotation.getAnnotationText();
            }
            if(PUBMED_NCBI_URL.equals( xrefUrl )){
                xrefUrl = CITEXPLORE_URL;
            }
            if(OLD_NEWT_URL.equals( xrefUrl )){
                xrefUrl = NEW_NEWT_URL;
            }
        }

        String replacedUrl = null;


        if (xrefUrl != null) {
            replacedUrl = xrefUrl.replaceAll("\\$\\{ac\\}", ac);
        }

        return replacedUrl;
    }

    public static Interactor getInteractorByAc( String intactAc ) {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractorDao().getByAc( intactAc );
    }

    public static DateTime toDateTime(Long dateInTimeMillis) {
        return new DateTime(dateInTimeMillis);
    }

    public static boolean isProtein(Interactor interactor) {
        return (interactor instanceof Protein);
    }

    public static boolean isSmallMolecule(Interactor interactor) {
        return (interactor instanceof SmallMolecule);
    }

    public static boolean isNucleicAcid(Interactor interactor) {
        return (interactor instanceof NucleicAcid);
    }
}