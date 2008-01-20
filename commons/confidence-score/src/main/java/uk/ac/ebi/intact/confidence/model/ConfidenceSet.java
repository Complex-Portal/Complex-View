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
package uk.ac.ebi.intact.confidence.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        18-Jan-2008
 *        </pre>
 */
public class ConfidenceSet {
     /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( ConfidenceSet.class );

    private List<BinaryInteractionAttributes> binaryInteractions;
    private Confidence type;

    public ConfidenceSet(Confidence type){
        this.type = type;
        binaryInteractions = new ArrayList<BinaryInteractionAttributes>();
    }

    public ConfidenceSet(Confidence type, List<BinaryInteractionAttributes> binaryInteractionAttributes){
        this (type);
        if (binaryInteractionAttributes != null){
            this.binaryInteractions = binaryInteractionAttributes;
        }
    }


    public List<BinaryInteractionAttributes> getBinaryInteractions() {
        return binaryInteractions;
    }

    public void setBinaryInteractions( List<BinaryInteractionAttributes> binaryInteractions ) {
        this.binaryInteractions = binaryInteractions;
    }

    public void addBinaryInteraction(BinaryInteractionAttributes binaryInteractionAttributes){
        if (binaryInteractionAttributes == null){
            binaryInteractions = new ArrayList<BinaryInteractionAttributes>();
        }
        if (!binaryInteractionAttributes.getConfidence().equals( this.type )){
            if (log.isInfoEnabled()){
                log.info( "binaryInteraction did not have same confidnece as set : " + binaryInteractionAttributes.convertToString() +" " +
                binaryInteractionAttributes.getConfidence() + " changed to " + this.getType());
            }
            binaryInteractionAttributes.setConfidence( this.getType() );
        }
        binaryInteractions.add( binaryInteractionAttributes );
    }

    public Confidence getType() {
        return type;
    }

}
