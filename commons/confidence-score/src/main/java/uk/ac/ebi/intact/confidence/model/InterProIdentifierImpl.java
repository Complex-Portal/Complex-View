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
public class InterProIdentifierImpl implements Identifier {
    private String identifier;
    private static String	ipTermExpr	= "IPR[0-9]{6}";

    public InterProIdentifierImpl(String primaryId){
        if (primaryId == null) {
			throw new IllegalArgumentException("PrimaryId must not be null! " + primaryId);
		}
		if ( Pattern.matches(ipTermExpr, primaryId)) {
			this.identifier = primaryId;
		} else {
			throw new IllegalArgumentException("PrimaryId must be a valid InterPro primaryId! " + primaryId);

		}
    }

    public String getId() {
        return identifier;
    }

    public static String getRegex() {
        return ipTermExpr;
    }

    public String convertToString() {
        return identifier;
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
		if (obj instanceof InterProIdentifierImpl) {
			InterProIdentifierImpl ip = (InterProIdentifierImpl) obj;
			return this.identifier.equals(ip.identifier);
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
