/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.mine.business.graph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import jdsl.core.api.ObjectIterator;
import jdsl.core.api.Sequence;
import jdsl.graph.api.Edge;
import jdsl.graph.api.Graph;
import jdsl.graph.api.Vertex;
import uk.ac.ebi.intact.application.mine.business.Constants;
import uk.ac.ebi.intact.application.mine.business.IntactUserI;
import uk.ac.ebi.intact.application.mine.business.MineException;
import uk.ac.ebi.intact.application.mine.business.graph.model.GraphData;
import uk.ac.ebi.intact.application.mine.business.graph.model.SearchObject;

/**
 * The <tt>MineHelper</tt> is a helper class to communicate between the action
 * which is calling it and the Dijkstra implementation. <br>
 * It provides several methods which works on the database level.
 * 
 * @author Andreas Groscurth
 */
public class MineHelper {
	static transient Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

	// SQL statement to select the graphid for a given interactor
    private static final String SELECT_GRAPHKEY = "SELECT DISTINCT "+ Constants.COLUMN_graphid +
                                                  " FROM ia_interactions" +
                                                  " WHERE "+ Constants.COLUMN_protein1_ac +"=? or "+ Constants.COLUMN_protein2_ac +"=?";

	// SQL statement to select the shortlabels for a given set of interactors
	// TODO does it still usefull as the shortlabel is stored in the interaction table ?
    private static final String SELECT_SHORTLABEL = "SELECT shortlabel " +
                                                    "FROM ia_interactor " +
                                                    "WHERE ac IN";

	// delimiter to build a sql string from a collection
	private static final String SQL_DELIMITER = "'";

	private IntactUserI intactUser;

	/**
	 * Creates a new <tt>MineHelper</tt>
	 * 
	 * @param user the intacthelper
	 * @throws MineException
	 */
	public MineHelper(IntactUserI user) throws MineException {
		this.intactUser = user;
		checkMineTable();
	}

	private void checkMineTable() throws MineException {
		Connection con = intactUser.getDBConnection();
		Statement stm;
		try {
			stm = con.createStatement();
			stm.setFetchSize(1);
			ResultSet set =
				stm.executeQuery("SELECT "+ Constants.COLUMN_protein1_ac +
                                 " FROM "+ Constants.INTERACTION_TABLE +"");
			if (!set.next()) {
				throw new MineException();
			}
		} catch (SQLException e) {
			throw new MineException();
		}
	}

	/**
	 * Returns a map which maps a node of the graph to a <tt>SearchObject</tt>.
	 * <br>
	 * The <tt>SearchObject</tt> is the template object to use in the
	 * algorithm. <br>
	 * Which node is used as a key in the generated map is determined by the
	 * accession numbers which are given in the collection. In this collection
	 * one can find the label attached to a node. <br>
	 * To get the node via the label the graphMap is needed which maps a label
	 * to the actual node of the graph.
	 * 
	 * @param graphMap the map which maps the label to the actual node
	 * @param searchAC the collection of the search accession numbers
	 * @return @throws MineException whether a search accession number could not
	 *         been mapped to a node in the graph
	 */
	private Map getSearchNodes(Map graphMap, Collection searchAC)
		throws MineException {
		// map to store the mapping of a node to a SearchObject
		Map map = new Hashtable();
		String accessionNumber;
		Vertex node;
		int i = 0;
		int size = searchAC.size();

		// the accession numbers are iterated
		for (Iterator iter = searchAC.iterator(); iter.hasNext();) {
			// the current accession number is fetched
			accessionNumber = (String) iter.next();

			// the node for the current accession number is fetched
			node = (Vertex) graphMap.get(accessionNumber);

			// there should be an entry for the given accession number
			// so if not -> an exception is thrown.
			if (node == null) {
				throw new MineException();
			}
			// a new SearchObject is stored
			map.put(node, new SearchObject(i++, size));
		}
		return map;
	}

	/**
	 * Returns a map which maps for the unique graphid a collection of accession
	 * numbers which are in the graph represented by the key. <br>
	 * For every accession number it is looked to which graphid it belongs. If
	 * the graphid is already in the map, the number is added to the other
	 * numbers for the graphid. If not a new collection with the current number
	 * is initialised.
	 * 
	 * @param searchFor the accession numbers one wants to compute the minimal
	 *            network.
	 * @return a map
	 * @throws SQLException whether something failed on the database level
	 */
	public Map getNetworkMap(Collection searchFor) throws SQLException {
		Map networks = new Hashtable();
		// statement to select the taxis and graphid for every interactor
		PreparedStatement pstm = intactUser.getDBConnection().prepareStatement( SELECT_GRAPHKEY );

		String ac;
		Integer key;
		ResultSet set;
		Collection search;
		// every given accession number is taken and its graphid and taxid
		// is fetched
		for (Iterator iter = searchFor.iterator(); iter.hasNext();) {
			ac = (String) iter.next();
			// the accesion number is set in the sql statement
			pstm.setString(1, ac);
			pstm.setString(2, ac);
			set = pstm.executeQuery();

			// if no data was found for the given accession number
			// the accession number is added to the singletons of the map
			if (!set.next()) {
				key = Constants.SINGLETON_GRAPHID;
			} else {
				// a new graphid is generated
				key = new Integer(set.getInt(1));
			}
			// if the map contains the current key
			// the accession number is added to the other numbers belonging
			// to the key
			if (networks.containsKey(key)) {
				( (Collection) networks.get( key ) ).add( ac );
			} else {
				// a new collection is created to store the accession
				// numbers
				search = new ArrayList();
				search.add(ac);
				networks.put(key, search);
			}
			set.close();
		}
		pstm.close();
		return networks;
	}

	/**
	 * Computes with minimal connecting network for the accession numbers in the
	 * given collection.
	 * 
	 * @param graphData the wrapper class with the graph
	 * @param searchAc the accession numbers to search for
	 * @return @throws MineException whether the computation fails
	 * @throws SQLException
	 */
	public void computeMiNe(GraphData graphData, Collection searchAc)
		throws MineException {
		// the map which maps the nodes of the graph and the SearchObjects is
		// created (Vertex -> SearchObject)
		Map searchMap = getSearchNodes(graphData.getAccMap(), searchAc);

		Graph graph = graphData.getGraph();
		// the structure to store the informations for the algorithm
		Storage storage = new MineStorage(graph.numVertices());
		Dijkstra d = new Dijkstra(storage, searchMap);

		// for easy access the nodes are rearranged into an array
		Vertex[] nodes =
			(Vertex[]) searchMap.keySet().toArray(
				new Vertex[searchMap.keySet().size()]);

		// all possible combinations of the nodes are considered.
		// e.g. if someone searches for A, B, C the following searches are
		// considered:
		// A -> B
		// A -> C
		// B -> C
		SearchObject so1, so2;
		for (int i = 0; i < nodes.length; i++) {
			// the searchobject for the current node is fetched
			so1 = (SearchObject) searchMap.get(nodes[i]);

			for (int j = i + 1; j < nodes.length; j++) {
				// the searchobject for the other node is fetched
				so2 = (SearchObject) searchMap.get(nodes[j]);

				// if a path was already found for the current two searchobjects
				// no search is started !
				if (!so1.hasPathAlreadyFound(so2)) {
					// the shortest path for the two nodes
					logger.info(
						"search starts from " + nodes[i] + " to " + nodes[j]);
					d.execute(graph, nodes[i], nodes[j]);
					storage.cleanup();
				}
			}
		}

		// the collection stores the minimal connecting network.
		// to avoid duplicates a HashSet is used.
		Collection miNe = new HashSet();
		Edge edge;
		Sequence shortestPath;
		for (Iterator iter = searchMap.values().iterator(); iter.hasNext();) {
			so1 = (SearchObject) iter.next();
			shortestPath = so1.getPath();
			// it can happen that a search has no path e.g. if the node is
			// to far away from every other node ! then no path could have found
			// and therefore the path is null
			if (shortestPath == null) {
				continue;
			}
			for (ObjectIterator edgeIter = shortestPath.elements();
				edgeIter.hasNext();
				) {
				// the current edge of the path
				edge = (Edge) edgeIter.nextObject();
				// the end nodes of the edges are fetched
				nodes = graph.endVertices(edge);

				// no check for duplicates has to been done because its a
				// hashset. either of the nodes have to be added to the
				// collection because the first element does not indicate the
				// start node of the edge it can also be the end node of the
				// edge !!! e.g. A-B-C can be: 1) A-B and 2) B-C
				// but also 1) B-A and 2) B-C and so on
				miNe.add(nodes[0].element());
				miNe.add(nodes[1].element());
			}
		}

		// if no path was found the search accession numbers are added with
		// their shortlabels to the singletons of the user
		if (miNe.isEmpty()) {
			try {
				intactUser.addToSingletons(getShortLabels(searchAc));
			}
			// something went wrong and no shortlabels could be found. to
			// provide that the application nevertheless go on the accession
			// numbers are stored
			catch (SQLException e) {
				intactUser.addToSingletons(searchAc);
			}
		}
		// the minimal connecting network is added to the path of the user
		else {
			intactUser.addToPath(miNe);
		}
	}

	/**
	 * Returns the shortlabels for the given accession numbers.
	 * 
	 * @param acs the accession numbers
	 * @return the shortlabels of the accession numbers
	 * @throws SQLException whether something failed on the database level
	 */
	public Collection getShortLabels(Collection acs) throws SQLException {
		// easily allows 8 acs with the size of 11 {e.g. EBI-1111111} letters
		// which is enough in most cases
		StringBuffer buf = new StringBuffer(96);
		String ac;

		// the collection of the accession numbers is exploded to the
		// stringbuffer for the sql statement.
		// [1,2,3,4] -> "'1','2','3','4'"
		for (Iterator iter = acs.iterator(); iter.hasNext();) {
			ac = iter.next().toString();
			buf.append(SQL_DELIMITER).append(ac).append(SQL_DELIMITER);
			if (iter.hasNext()) {
				buf.append(Constants.COMMA);
			}
		}

		// the collection with the accession numbers is cleared
		// because it is reused for the shortlabels
		acs.clear();
		Statement stm = intactUser.getDBConnection().createStatement();
		ResultSet set = stm.executeQuery(SELECT_SHORTLABEL + "(" + buf + ")");
		// every shortLabel is added to the collection.
		// the order of the shortlabel is not relevant !
		while (set.next()) {
			acs.add(set.getString(1));
		}
		set.close();
		stm.close();

		return acs;
	}
}