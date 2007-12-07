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
package uk.ac.ebi.intact.application.hierarchview.exception;

/**
 * Thrown
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class NetworkBuildException extends Exception {
    public NetworkBuildException() {
        super();
    }

    public NetworkBuildException( String message ) {
        super( message );
    }

    public NetworkBuildException( String message, Throwable cause ) {
        super( message, cause );
    }

    public NetworkBuildException( Throwable cause ) {
        super( cause );
    }
}
