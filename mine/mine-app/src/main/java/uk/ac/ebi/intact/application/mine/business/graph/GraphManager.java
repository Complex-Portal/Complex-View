/*
 * Created on 22.07.2004
 */

package uk.ac.ebi.intact.application.mine.business.graph;

import org.apache.log4j.Logger;
import org.shiftone.cache.Cache;
import org.shiftone.cache.policy.lru.LruCacheFactory;
import uk.ac.ebi.intact.application.mine.business.Constants;
import uk.ac.ebi.intact.application.mine.business.IntactUserI;
import uk.ac.ebi.intact.application.mine.business.graph.model.GraphData;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author Andreas Groscurth
 */
public class GraphManager extends Thread {
	static transient Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

	private static final GraphManager INSTANCE = new GraphManager();

	// a cache structure to store the graphs efficiently
	private Cache cache;
	// the collection stores all ids which are currently proceeded
	private Set running;
	// the stack stores all ids which have to be proceeded
	private Stack incoming;

	/**
	 * Creates a new GraphManager.
	 * 
	 */
	private GraphManager() {
		/**
		 * the cache strucuture is created with <br>
		 * a dummy name <br>
		 * <br>
		 * the time_out which indicates after which time the least valueable
		 * object is removed <br>
		 * <br>
		 * the maximal size of the cache
		 */
		// the timeout for the cache after which elements are erazed
		// the time is measured in milliseconds and therefore
		// 5 minutes are written in this way !
		long TIME_OUT =
			Long.parseLong(
				IntactUserI.MINE_PROPERTIES.getProperty(
					"cache.timeOut",
					"300000"));
		// the maximal size of the cache
		int MAX_SIZE =
			Integer.parseInt(
				IntactUserI.MINE_PROPERTIES.getProperty("cache.maxSize", "50"));
		cache =
			new LruCacheFactory().newInstance("mineCache", TIME_OUT, MAX_SIZE);

		running = new HashSet();

		incoming = new Stack();
		// the monitoring of the incoming stack starts
		start();
	}

	/**
	 * Called by the client to retrieve a graphdata object for the given id.
	 * 
	 * @param id the graphid
	 * @param user the intactUser of the current user
	 * @return the graphData or null if not avaiable
	 */
	public GraphData getGraphData(Integer id, IntactUserI user) {
		GraphData graphData = (GraphData) cache.getObject(id);
		// no data available for the given id
		if (graphData == null) {
			GraphBuildData data = new GraphBuildData(user, id);
			//logger.warn("cache has " + cache.size());
			// the id is pushed into the incoming stack
			incoming.push(data);
			// because its no data available yet null is returned
			return null;
		}
		// the graphdata for the given id is returned
		return graphData;
	}

	/**
	 * returns the instance of this class (singleton implementation). <br>
	 * 
	 * @return GraphManager the instance
	 */
	public static GraphManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Method checks every 25 ms if a new id is in the incoming stack. <br>
	 * If a new id is in the stack it is popped out and tested whether:
	 * <ol>
	 * <li>one can find it already in the cache (which means a graph was
	 * already built)</li>
	 * <li>one can find it in the running structure (which means a graph is
	 * built currently)</li>
	 * </ol>
	 */
	public void run() {
		try {
			while (true) {
				// if there are graphids to work with
				if (!incoming.isEmpty()) {
					// get the next graphid for which the graph should be
					// built
					final GraphBuildData buildData =
						(GraphBuildData) incoming.pop();

					synchronized (running) {
						// if for the given ID nothing can be found in the
						// cache and in the running structure -> a new thread
						// starts for building the graph for the given ID
						if (cache.getObject(buildData.toProceed) == null
							&& !running.contains(buildData.toProceed)) {
							running.add(buildData.toProceed);

							// a new Thread builds the graph for the given graph ID and with
							// the given intact user
							new GraphBuildThread(
								buildData.toProceed,
								cache,
								running)
								.start();
						}
					}
				}
				Thread.sleep(25);
			}
		} catch (InterruptedException e) {
			logger.warn("building for graph was interrupted");
		}
	}

	private static class GraphBuildData {
		private IntactUserI user;
		private Integer toProceed;

		public GraphBuildData(IntactUserI user, Integer toProceed) {
			this.user = user;
			this.toProceed = toProceed;
		}

		public String toString() {
			return user.getUserName() + " " + toProceed;
		}
	}
}