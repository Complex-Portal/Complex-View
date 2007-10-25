/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.service.graph.binary;

import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.visualization.PluggableRenderer;

import java.awt.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryRenderer extends PluggableRenderer{

    private BinaryGraphNetwork graph;

    public BinaryRenderer(BinaryGraphNetwork graph) {
        this.graph = graph;
    }

    @Override
    public VertexStringer getVertexStringer() {
        return StringLabeller.getLabeller(graph);
    }

    @Override
    public VertexPaintFunction getVertexPaintFunction() {
        return new PickableVertexPaintFunction(this, new Color(0, 150, 250), new Color(0, 125, 130), Color.red);
    }

    @Override
    public EdgePaintFunction getEdgePaintFunction() {
        return new PickableEdgePaintFunction(this, new Color(0, 150, 100), null);
    }

    @Override
    public EdgeShapeFunction getEdgeShapeFunction() {
        return new EdgeShape.QuadCurve();
    }

    @Override
    public boolean getVertexLabelCentering() {
        return true;
    }
}