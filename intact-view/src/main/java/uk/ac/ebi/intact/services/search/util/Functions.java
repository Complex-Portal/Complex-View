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
package uk.ac.ebi.intact.services.search.util;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.services.search.model.*;
import uk.ac.ebi.intact.services.search.component.resultpanel.CategoryItem;
import uk.ac.ebi.intact.services.search.component.resultpanel.CategoryModel;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Functions to be used in the UI
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class Functions {
    private static final String MI_TO_XREF_URL_MAP_PARAM = Functions.class+".MI_TO_XREF_URL_MAP";

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

    public static <T extends AnnotatedObject> AnnotatedObjectWrapper<T> wrap(T ao) {
        AnnotatedObjectWrapper aod;
        
        if (ao instanceof Interaction) {
            aod = new InteractionWrapper((Interaction) ao);
        } else if (ao instanceof Interactor) {
            aod = new InteractorWrapper((Interactor) ao);
        } else if (ao instanceof Experiment) {
            aod = new ExperimentWrapper((Experiment) ao);
        } else if (ao instanceof CvObject) {
            aod = new CvObjectWrapper((CvObject) ao);
        } else {
            aod = new AnnotatedObjectWrapper(ao);
        }

        return aod;
    }

    public static CategoryModel toXrefCategoryModel(Collection<Xref> xrefs) {
        List<CategoryItem> items = new ArrayList<CategoryItem>(xrefs.size());

        for (Xref xref : xrefs) {
            items.add(new CategoryItem(xref.getCvDatabase().getMiIdentifier(), xref));
        }

        return new CategoryModel(items);
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
    
}
