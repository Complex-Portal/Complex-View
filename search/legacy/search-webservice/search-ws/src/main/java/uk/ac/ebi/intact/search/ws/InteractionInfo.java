/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.search.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains information about an interaction
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-Sep-2006</pre>
 */
public class InteractionInfo
{

    private static final Log log = LogFactory.getLog(InteractionInfo.class);

    private String intactAc;
    private String shortName;
    private String fullName;
    private String interactionType;
    private String description;
    private String definition;


    public InteractionInfo()
    {
    }

    public InteractionInfo(String intactAc, String shortName, String fullName, String interactionType, String description, String definition)
    {
        this.intactAc = intactAc;
        this.shortName = shortName;
        this.fullName = fullName;
        this.interactionType = interactionType;
        this.description = description;
        this.definition = definition;
    }

    public String getIntactAc()
    {
        return intactAc;
    }

    public void setIntactAc(String intactAc)
    {
        this.intactAc = intactAc;
    }

    public String getShortName()
    {
        return shortName;
    }

    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getInteractionType()
    {
        return interactionType;
    }

    public void setInteractionType(String interactionType)
    {
        this.interactionType = interactionType;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDefinition()
    {
        return definition;
    }

    public void setDefinition(String definition)
    {
        this.definition = definition;
    }
}
