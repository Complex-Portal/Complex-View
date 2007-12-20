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
package uk.ac.ebi.intact.application.hierarchview.business.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.image.Utilities;
import uk.ac.ebi.intact.service.graph.Edge;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class EdgeAttributes {
    private static final Log logger = LogFactory.getLog( NodeAttributes.class );

    //private final Node node;

    /**
     * *********************************************************************
     * StrutsConstants
     */
    public final static Color DEFAULT_EDGE_COLOR = new Color( 75, 158, 179 );

    public final static Color EDGE_COLOR;

    static {
        Properties properties = IntactUserI.GRAPH_PROPERTIES;
        String stringColorNode = null;

        // it is tried to parse the colors from the properties file
        if ( null != properties ) {
            stringColorNode = properties.getProperty( "hierarchView.image.color.default.edge" );
        } else {
            logger.warn( "properties file GRAPH_PROPERTIES could not been read" );
        }
        // the color for the edge is parsed
        EDGE_COLOR = Utilities.parseColor( stringColorNode, DEFAULT_EDGE_COLOR );
    }

    private Map<String, Object> attributes;


    public EdgeAttributes( Edge edge ) {
        //this.edge = edge;
        attributes = new HashMap();
    }

    public void put( String key, Object value ) {
        attributes.put( key, value );
    }

    public Object get( String key ) {
        return attributes.get( key );
    }

    @Override
    public String toString() {
        StringBuffer attribute = new StringBuffer();
        attribute.append( "EdgeAttributes { " );
        for ( String key : attributes.keySet() ) {
            attribute.append( key ).append( " : " ).append( attributes.get( key ) ).append( "," );
        }
        attribute.append( " }" );
        return attribute.toString();
    }
}
