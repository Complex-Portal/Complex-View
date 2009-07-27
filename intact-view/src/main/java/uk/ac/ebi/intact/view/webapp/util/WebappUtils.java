/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.commons.util.DesEncrypter;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.faces.context.FacesContext;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class WebappUtils {

    private static final Log log = LogFactory.getLog(WebappUtils.class);

    public static final String INTERACTION_TYPE_TERM = WebappUtils.class + ".INTERACTION_TYPE_TERM";
    public static final String INTERACTION_TYPES = WebappUtils.class + ".INTERACTION_TYPES";
    public static final String DETECTION_METHOD_TERM = WebappUtils.class + ".DETECTION_METHOD_TERM";
    public static final String DETECTION_METHODS = WebappUtils.class + ".DETECTION_METHODS";


    private WebappUtils() {
    }

    public static String encrypt(FacesContext facesContext, String str) throws IntactViewException {
        return new DesEncrypter(secretKey(facesContext)).encrypt(str);
    }

    public static String decrypt(FacesContext facesContext, String str) throws IntactViewException {
        return new DesEncrypter(secretKey(facesContext)).decrypt(str);
    }

    private static SecretKey secretKey(FacesContext facesContext) {
        try {
            IntactViewConfiguration intactViewConfiguration = getIntactViewConfiguration(facesContext);

            String secret = intactViewConfiguration.getIntactSecret();
            byte[] bytes = new Base64().decode(secret.getBytes());

            return new SecretKeySpec(bytes, "DES");
        }
        catch (Exception e) {
            throw new IntactViewException(e);
        }
    }

    public static IntactViewConfiguration getIntactViewConfiguration(FacesContext facesContext) {
        IntactViewConfiguration intactViewConfiguration = (IntactViewConfiguration)
                facesContext.getApplication().getELResolver()
                .getValue(facesContext.getELContext(), null, "intactViewConfiguration");
        return intactViewConfiguration;
    }
}