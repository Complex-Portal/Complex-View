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
 *        11-Dec-2007
 *        </pre>
 */
public class BinaryInteraction {
    private Identifier firstId;
    private Identifier secondId;

    private Confidence confidence;

//    public BinaryInteraction(){
//
//    }

    public BinaryInteraction(Identifier firstIdentifier, Identifier secondIdentifier, Confidence confidence){
        if (firstIdentifier instanceof UniprotIdentifierImpl && secondIdentifier instanceof UniprotIdentifierImpl){
            firstId = firstIdentifier;
            secondId = secondIdentifier;
            this.confidence = confidence;
        }   else {
            throw new IllegalArgumentException( "Both identifiers must be uniprot identifiers!");
        }
    }

    public Identifier getFirstId() {
        return firstId;
    }

    public void setFirstId( Identifier firstId ) {
        this.firstId = firstId;
    }

    public Identifier getSecondId() {
        return secondId;
    }

    public void setSecondId( Identifier secondId ) {
        this.secondId = secondId;
    }

    public Confidence getConfidence(){
        return confidence;
    }

    public void setConfidence(Confidence confidence){
        this.confidence = confidence;
    }

    public String convertToString(){
        return firstId + ";" + secondId;
    }
}
