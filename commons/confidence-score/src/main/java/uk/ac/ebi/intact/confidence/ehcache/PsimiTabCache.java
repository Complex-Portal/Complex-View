/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.confidence.ehcache;

import net.sf.ehcache.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.io.File;

/**
 * When assigning scores to a PSI-MI TAB file, the specified output file exists,
 * it stores the contents of the file in the cache and offers teh possibility to use this information.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class PsimiTabCache {
      /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog( PsimiTabCache.class);

    private static Cache cache;

    private final static PsimiTabCache instance = new PsimiTabCache();

    private PsimiTabCache(){}

    /////////////////////
    // Public Method(s).
    public static PsimiTabCache getInstance(){
        return instance;
    }

    public void loadPsimiTabFile( File psimiTab){

    }

    public BinaryInteraction fetchBy (BinaryInteraction binaryInteraction){
       return null;

    }

}
