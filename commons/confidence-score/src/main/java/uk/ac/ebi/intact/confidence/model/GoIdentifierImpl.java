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
 * Gene Ontology identifier.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        30-Nov-2007
 *        </pre>
 */
public class GoIdentifierImpl implements Identifier {
    private String identifier;
    private static String goTermExpr = "GO:[0-9]{7}";

    public GoIdentifierImpl(String primaryId) {
         if (primaryId == null) {
			throw new IllegalArgumentException("primaryId must not be null! " + primaryId);
		}
        if ( Pattern.matches(goTermExpr, primaryId)) {
			this.identifier = primaryId;
        }
		else{
			throw new IllegalArgumentException("PrimaryId must be a valid GO primaryId! " + primaryId);

		}
	}

    public String convertToString() {
        return identifier;  
    }


    /**
	 * @return the id
	 */
	public String getId() {
		return identifier;
	}

    public static String getRegex() {
        return goTermExpr;
    }

    @Override
	public String toString() {
		return identifier;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GoIdentifierImpl) {
			GoIdentifierImpl go = (GoIdentifierImpl) obj;
			return this.identifier.equals(go.identifier);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.identifier.hashCode();
	}
}
