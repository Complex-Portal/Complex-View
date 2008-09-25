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
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.util;

import uk.ac.ebi.intact.core.persister.finder.DefaultFinder;
import uk.ac.ebi.intact.model.*;

/**
 * A finder for Editor extending DefaultFinder for fixing cloning issues
 * If the ao is an instance of exp or interaction and it doesn't have an ac
 * already then return ac=null
 *
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class EditorFinder extends DefaultFinder {


    public String findAc( AnnotatedObject annotatedObject ) {

        String ac;

        if ( annotatedObject.getAc() != null ) {
            return annotatedObject.getAc();
        }

        if ( annotatedObject instanceof Institution ) {
            ac = findAcForInstitution( ( Institution ) annotatedObject );
        } else if ( annotatedObject instanceof Publication ) {
            ac = findAcForPublication( ( Publication ) annotatedObject );
        } else if ( annotatedObject instanceof CvObject ) {
            ac = findAcForCvObject( ( CvObject ) annotatedObject );
        } else if ( annotatedObject instanceof Experiment ) {
            ac = null;
        } else if ( annotatedObject instanceof Interaction ) {
            ac = null;
        } else if ( annotatedObject instanceof Interactor ) {
            ac = findAcForInteractor( ( InteractorImpl ) annotatedObject );
        } else if ( annotatedObject instanceof BioSource ) {
            ac = findAcForBioSource( ( BioSource ) annotatedObject );
        } else if ( annotatedObject instanceof Component ) {
            ac = findAcForComponent( ( Component ) annotatedObject );
        } else if ( annotatedObject instanceof Feature ) {
            ac = findAcForFeature( ( Feature ) annotatedObject );
        } else {
            throw new IllegalArgumentException( "Cannot find Ac for type: " + annotatedObject.getClass().getName() );
        }

        return ac;
    }


}
