/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.mine.business.graph;

import java.util.Hashtable;
import java.util.Map;

import jdsl.core.api.Locator;
import jdsl.graph.api.Edge;
import jdsl.graph.api.Vertex;

/**
 * The class implements the interface <tt>Storage</tt> and uses a
 * <tt>Hashtable</tt> as storage structure.
 * 
 * @author Andreas Groscurth
 */
public class MineStorage implements Storage {
    private Map storageMap;

    /**
     * Creates a new storage structure.
     * 
     * @param numberOfNodes the number of nodes in the graph
     */
    public MineStorage(int numberOfNodes) {
        // to avoid too much increasing of the hashtable and to avoid
        // that the size of the table is initially too high
        // the default size of the map is the half of the number of nodes in the
        // graph. This is just an estimation and not based on facts !!
        storageMap = new Hashtable( numberOfNodes / 2 );
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#cleanup()
     */
    public void cleanup() {
        storageMap.clear();
    }

    /**
     * Returns the storage entry class which stores the elements for the given
     * node.
     * 
     * @param v the node to look for
     * @return the storage entry element
     */
    private StorageEntry getEntry(Vertex v) {
        StorageEntry se = (StorageEntry) storageMap.get( v );
        // if for the current node no data is given a new one is created.
        if ( se == null ) {
            se = new StorageEntry();
            storageMap.put( v, se );
        }
        return se;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#setDistance(jdsl.graph.api.Vertex,
     *      int)
     */
    public void setDistance(Vertex v, int dis) {
        getEntry( v ).distance = dis;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#setEdgeToParent(jdsl.graph.api.Vertex,
     *      jdsl.graph.api.Edge)
     */
    public void setEdgeToParent(Vertex v, Edge edge) {
        getEntry( v ).edge = edge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#setLocator(jdsl.graph.api.Vertex,
     *      jdsl.core.api.Locator)
     */
    public void setLocator(Vertex v, Locator loc) {
        getEntry( v ).locator = loc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#hasDistance(jdsl.graph.api.Vertex)
     */
    public boolean hasDistance(Vertex v) {
        StorageEntry se = (StorageEntry) storageMap.get( v );
        return se != null && se.distance != UNREACHABLE_DISTANCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#hasEdgeToParent(jdsl.graph.api.Vertex)
     */
    public boolean hasEdgeToParent(Vertex v) {
        StorageEntry se = (StorageEntry) storageMap.get( v );
        return se != null && se.edge != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#hasLocator(jdsl.graph.api.Vertex)
     */
    public boolean hasLocator(Vertex v) {
        StorageEntry se = (StorageEntry) storageMap.get( v );
        return se != null && se.locator != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#getLocator(jdsl.graph.api.Vertex)
     */
    public Locator getLocator(Vertex v) {
        StorageEntry se = (StorageEntry) storageMap.get( v );
        return se == null ? null : se.locator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#getEdgeToParent(jdsl.graph.api.Vertex)
     */
    public Edge getEdgeToParent(Vertex v) {
        StorageEntry se = (StorageEntry) storageMap.get( v );
        return se == null ? null : se.edge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ebi.intact.application.mine.business.graph.Storage#getDistance(jdsl.graph.api.Vertex)
     */
    public int getDistance(Vertex v) {
        StorageEntry se = (StorageEntry) storageMap.get( v );
        return se == null ? UNREACHABLE_DISTANCE : se.distance;
    }

    private static class StorageEntry {
        private int distance = UNREACHABLE_DISTANCE;
        private Locator locator;
        private Edge edge;
    }
}