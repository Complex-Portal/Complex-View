/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.details.complex;

/**
 * A lightweight interactor for our complex display.
*
* @author Samuel Kerrien (skerrien@ebi.ac.uk)
* @version $Id$
* @since 1.6.0
*/
public class SimpleInteractor {

    private String ac;
    private String shortLabel;
    private String fullName;

    public SimpleInteractor( String ac, String shortLabel ) {
        this.ac = ac;
        this.shortLabel = shortLabel;
    }

    public SimpleInteractor( String ac, String shortLabel, String fullName ) {
        this(ac, shortLabel);
        this.fullName = fullName;
    }

    public String getAc() {
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel( String shortLabel ) {
        this.shortLabel = shortLabel;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName( String fullName ) {
        this.fullName = fullName;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        SimpleInteractor that = ( SimpleInteractor ) o;

        if ( !ac.equals( that.ac ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ac.hashCode();
    }

    @Override
    public String toString() {
        return "SimilarInteraction{" +
               "ac='" + ac + '\'' +
               ", shortLabel='" + shortLabel + '\'' +
               '}';
    }
}
