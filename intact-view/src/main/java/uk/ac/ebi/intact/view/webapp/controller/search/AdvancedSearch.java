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
package uk.ac.ebi.intact.view.webapp.controller.search;

import java.io.Serializable;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AdvancedSearch implements Serializable {

    private String identifier;
    private String detectionMethod;
    private boolean includeDetectionMethodChildren;
    private String pubId;
    private String pubFirstAuthor;
    private String taxid;
    private String interactionType;
    private String interactionId;
    private boolean includeInteractionTypeChildren;
    private boolean conjunction = true;

    public AdvancedSearch() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDetectionMethod() {
        return detectionMethod;
    }

    public void setDetectionMethod(String detectionMethod) {
        this.detectionMethod = detectionMethod;
    }

    public String getPubId() {
        return pubId;
    }

    public void setPubId(String pubId) {
        this.pubId = pubId;
    }

    public String getPubFirstAuthor() {
        return pubFirstAuthor;
    }

    public void setPubFirstAuthor(String pubFirstAuthor) {
        this.pubFirstAuthor = pubFirstAuthor;
    }

    public String getTaxid() {
        return taxid;
    }

    public void setTaxid(String taxid) {
        this.taxid = taxid;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public boolean isConjunction() {
        return conjunction;
    }

    public void setConjunction(boolean conjunction) {
        this.conjunction = conjunction;
    }


    public boolean isIncludeDetectionMethodChildren() {
        return includeDetectionMethodChildren;
    }

    public void setIncludeDetectionMethodChildren(boolean includeDetectionMethodChildren) {
        this.includeDetectionMethodChildren = includeDetectionMethodChildren;
    }

    public boolean isIncludeInteractionTypeChildren() {
        return includeInteractionTypeChildren;
    }

    public void setIncludeInteractionTypeChildren(boolean includeInteractionTypeChildren) {
        this.includeInteractionTypeChildren = includeInteractionTypeChildren;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(String interactionId) {
        this.interactionId = interactionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AdvancedSearch");
        sb.append("{conjunction=").append(conjunction);
        sb.append(", identifier='").append(identifier).append('\'');
        sb.append(", detectionMethod='").append(detectionMethod).append('\'');
        sb.append(", includeDetectionMethodChildren=").append(includeDetectionMethodChildren);
        sb.append(", pubId='").append(pubId).append('\'');
        sb.append(", pubFirstAuthor='").append(pubFirstAuthor).append('\'');
        sb.append(", taxid='").append(taxid).append('\'');
        sb.append(", interactionId='").append(interactionId).append('\'');
        sb.append(", interactionType='").append(interactionType).append('\'');
        sb.append(", includeInteractionTypeChildren=").append(includeInteractionTypeChildren);
        sb.append('}');
        return sb.toString();
    }
}