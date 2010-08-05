/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller.curate.util;

import uk.ac.ebi.intact.model.IntactObject;

import java.util.Comparator;

/**
* @author Bruno Aranda (baranda@ebi.ac.uk)
* @version $Id$
*/
public class IntactObjectComparator implements Comparator<IntactObject> {
    @Override
    public int compare( IntactObject o1, IntactObject o2 ) {
        if ( o1.getAc() != null ) return 1;
        return 0;
    }
}
