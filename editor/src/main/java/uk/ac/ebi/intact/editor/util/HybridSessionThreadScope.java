/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.util;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * This scopes checks if the bean exists in the session, and if it is not available, it
 * checks the thread/
 */
public class HybridSessionThreadScope extends SimpleThreadScope {

    @Override
    public Object get( String name, ObjectFactory objectFactory ) {
        Object scopedObject;

        if ( RequestContextHolder.getRequestAttributes() != null ) {
            Object mutex = RequestContextHolder.currentRequestAttributes().getSessionMutex();

            synchronized ( mutex ) {
                RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
                scopedObject = attributes.getAttribute( name, RequestAttributes.SCOPE_SESSION );

                if ( scopedObject == null ) {
                    scopedObject = super.get( name, objectFactory );
                    attributes.setAttribute( name, scopedObject, RequestAttributes.SCOPE_SESSION );
                }
            }
        } else {
            scopedObject = super.get( name, objectFactory );
        }

        return scopedObject;
    }

    public String getConversationId() {
        if ( RequestContextHolder.getRequestAttributes() != null ) {
            return RequestContextHolder.currentRequestAttributes().getSessionId();
        }

        return super.getConversationId();
    }

    @Override
    public Object remove( String name ) {
        if ( RequestContextHolder.getRequestAttributes() != null ) {
            Object mutex = RequestContextHolder.currentRequestAttributes().getSessionMutex();
            synchronized ( mutex ) {
                return super.remove( name );
            }
        }
        return super.remove( name );
    }

    public void registerDestructionCallback( String name, Runnable callback ) {
        if ( RequestContextHolder.getRequestAttributes() != null ) {
            RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
            attributes.registerDestructionCallback( name, callback, RequestAttributes.SCOPE_SESSION );
        }
    }

    public Object resolveContextualObject( String key ) {
        if ( RequestContextHolder.getRequestAttributes() != null ) {
            RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
            return attributes.resolveReference( key );
        }

        return null;
    }


}