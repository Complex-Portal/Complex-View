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
import uk.ac.ebi.intact.service.graph.Node;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Contains the Attributes of a Node.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class NodeAttributes {

    public static final Log logger = LogFactory.getLog( NodeAttributes.class );

    //private final Node node;

    /**
     * *********************************************************************
     * StrutsConstants
     */
    public final static Color DEFAULT_NODE_COLOR = new Color( 0, 0, 255 );
    public final static Color DEFAULT_LABEL_COLOR = new Color( 255, 255, 255 );

    public final static Color NODE_COLOR;
    public final static Color LABEL_COLOR;

    static {
        Properties properties = IntactUserI.GRAPH_PROPERTIES;
        String stringColorNode = null;
        String stringColorLabel = null;
        // it is tried to parse the colors from the properties file
        if ( null != properties ) {
            stringColorNode = properties.getProperty( "hierarchView.image.color.default.node" );
            stringColorLabel = properties.getProperty( "hierarchView.image.color.default.label" );
        } else {
            logger.warn( "properties file GRAPH_PROPERTIES could not been read" );
        }
        // the color for the node is parsed
        NODE_COLOR = Utilities.parseColor( stringColorNode, DEFAULT_NODE_COLOR );
        // the color for the lables parsed
        LABEL_COLOR = Utilities.parseColor( stringColorLabel, DEFAULT_LABEL_COLOR );
    }

    private Map<String, Object> attributes;


    public NodeAttributes( Node node ) {
        //this.node = node;
        attributes = new HashMap();
    }

    public void put( String key, Object value ) {
        attributes.put( key, value );
    }

    public Object get( String key ) {
        return attributes.get( key );
    }
}
