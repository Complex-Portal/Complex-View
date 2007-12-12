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

import java.util.regex.Pattern;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        30-Nov-2007
 *        </pre>
 */
public class IntActIdentifierImpl implements Identifier {
    private String identifier;
    private static String intactTermExpr ="EBI-[0-9]+|IA:[0-9]+";

    public IntActIdentifierImpl(String id){
             if (id == null) {
			throw new IllegalArgumentException("IntAct Id must not be null! " + id);
		}
		if ( Pattern.matches(intactTermExpr, id)) {
			this.identifier = id;
		} else {
			throw new IllegalArgumentException("IntAct Id must be a valid InterPro primaryId! " + id);

		}
    }
    public String getId() {
        return identifier;
    }

    public static String getRegex() {
        return intactTermExpr;
    }

    public String convertToString() {
        return identifier;
    }
}
