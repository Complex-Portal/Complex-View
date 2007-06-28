package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.graph;


import java.awt.*;

/**
 * LayoutEdge which has a render method to draw onto a canvas.
 *
 * @author EGO
 * @version $Id$
 * @since 27.04.2005
 */
public class StrokeEdge implements LayoutEdge {

    Node parent;
    Node child;
    Shape route;
    Color color;
    Stroke stroke;

    public StrokeEdge(Node parent, Node child, Color color, Stroke stroke) {
        this.parent = parent;
        this.child = child;
        this.color = color;
        this.stroke = stroke;
    }

    public Node getParent() {
        return parent;
    }

    public Node getChild() {
        return child;
    }

    public void setRoute(Shape route) {
        this.route = route;
    }

    /**
     * Draw the edge.
     *
     * @param g2 Canvas
     */
    public void render(Graphics2D g2) {

        g2.setStroke(stroke);
        g2.setColor(color);

        g2.draw(route);


    }
}