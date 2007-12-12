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
package uk.ac.ebi.intact.confidence.model;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *               30-Nov-2007
 *               </pre>
 */
public class IdentifierAttributeImpl<T extends Identifier> implements Attribute<Identifier> {
    private T firstElement;
    private T secondElement;

    public IdentifierAttributeImpl( T firstElement, T secondElement ) {
        this.firstElement = firstElement;
        this.secondElement = secondElement;
    }

    public Identifier getFirstElement() {
        return firstElement;
    }

    public Identifier getSecondElement() {
        return secondElement;
    }

    public String convertToString(){
        return firstElement+";" +secondElement;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj instanceof IdentifierAttributeImpl ) {
            IdentifierAttributeImpl objT = ( IdentifierAttributeImpl ) obj;
            boolean first = firstElement.equals( objT.getFirstElement() ) && secondElement.equals( objT.getSecondElement() );
            boolean rev = firstElement.equals( objT.getSecondElement() ) && secondElement.equals( objT.getFirstElement() );
            return first || rev;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return firstElement.hashCode() * secondElement.hashCode();
    }
}
