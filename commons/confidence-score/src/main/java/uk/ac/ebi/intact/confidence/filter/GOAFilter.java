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
package uk.ac.ebi.intact.confidence.filter;

import uk.ac.ebi.intact.confidence.attribute.AnnotationConstants;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;

import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * Strategy for filtering the GO Annotation tags.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 19.02.2008
 */
public interface GOAFilter extends AnnotationConstants {
    static Identifier forbiddenGO = new GoIdentifierImpl( "GO:0005515");
    static String forbiddenCode = "IEA";

    public void initialize( File goaFile ) throws FilterException;

    public void clean ();

    public void filterGO ( Identifier id, Set<Identifier> gos) throws FilterException;

    public void filterGO( ProteinAnnotation proteinAnnotation ) throws FilterException;

    public void filterGO( Collection<ProteinAnnotation> proteinAnnotations) throws FilterException;
}
