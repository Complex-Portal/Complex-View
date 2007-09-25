package uk.ac.ebi.intact.service.graph.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.PickableVertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.io.GraphMLFile;
import edu.uci.ics.jung.io.GraphMLFileHandler;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.Renderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class GraphVisualization {

	private VisualizationViewer viewer;
	private Dimension dimension = new Dimension(500, 500);
	private String graphMLFileName;
	private Graph graph;

	/////////////////////////////
	/// Getters & Setters
	
	/**
	 * Sets the Dimension of the VisualizationViewer
	 * and update the GraphVisualization
	 * @param width, height
	 */
	public void setDimension(int width, int height) {
		this.dimension = new Dimension(width, height);
		run();
	}
	
	/**
	 * @return current Dimension of the VisualizationViewer
	 */
	public Dimension getDimension() {
		return dimension;
	}

	/** 
	 * @return actual VisualizationViewer 
	 */
	public VisualizationViewer getViewer() {
		return viewer;
	}

	/** 
	 * @return current Renderer
	 */
	private Renderer getRenderer() {
		PluggableRenderer renderer = new PluggableRenderer();
		renderer.setVertexStringer(StringLabeller.getLabeller(graph));
		VertexPaintFunction vpf = new PickableVertexPaintFunction(renderer,
				new Color(0, 150, 250), new Color(0, 125, 130), Color.red);
		renderer.setVertexPaintFunction(vpf);
		renderer.setVertexLabelCentering(true);

		EdgePaintFunction epf = new PickableEdgePaintFunction(renderer,
				new Color(0, 150, 100), null);
		renderer.setEdgePaintFunction(epf);

		renderer.setEdgeShapeFunction(new EdgeShape.Line());

		return renderer;
	}
	
	/**
	 * @return current Layout
	 */
	private Layout getLayout() {
		// StaticLayout layout = new StaticLayout(graph);
		// SpringLayout layout = new SpringLayout(graph);
		FRLayout layout = new FRLayout(graph);
		int w = (int) dimension.getWidth();
		int h = (int) dimension.getHeight();

		Dimension dimension = new Dimension(w, h);
		layout.initialize(dimension);

		return layout;
	}

	/////////////////////////////
	/// Constructors
	
	/**
	 * Constructor which called the run-Methode
	 * @param fileName
	 */
	public GraphVisualization(String fileName) {
		this.graphMLFileName = fileName;
		run();
	}

	/////////////////////////////
	/// Additional methods.
	
	/**
	 * Reads the GraphML File and create the VisualizationViewer
	 */
	public void run() {
		GraphMLFileHandler handler = new MyGraphMLFileHandler();
		GraphMLFile file = new GraphMLFile(handler);
		graph = file.load(graphMLFileName);

		Layout layout = getLayout();
		Renderer renderer = getRenderer();

		viewer = new VisualizationViewer(layout, renderer);
		viewer.setBackground(Color.white);
		viewer.setSize(dimension);
	}

	/**
	 * Exports the VisualizationViewer image to the file.
	 * @param file
	 * @param formatName ("jpg" or "png")
	 */
	public void writeImage(File file, String formatName) {
		if (viewer == null) {
			run();
		}

		int width = viewer.getWidth();
		int height = viewer.getHeight();

		BufferedImage bi = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D graphics = bi.createGraphics();
		viewer.paint(graphics);
		graphics.dispose();

		try {
			ImageIO.write(bi, formatName, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Exports the VisualizationViewer image to the OutputStream
	 * @param stream
	 * @param formatName ("jpg" or "png")
	 */
	public void writeImage(OutputStream stream, String formatName) {
		if (viewer == null) {
			run();
		}

		int width = viewer.getWidth();
		int height = viewer.getHeight();

		BufferedImage bi = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D graphics = bi.createGraphics();
		viewer.paint(graphics);
		graphics.dispose();

		try {
			ImageIO.write(bi, formatName, stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MAIN-Methods for testings.
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String fileName = "src/test/java/uk/ac/ebi/intact/service/graph/binary/graphml.xml";
		GraphVisualization vis = new GraphVisualization(fileName);
		vis.setDimension(1500, 1500);
		jf.getContentPane().add(vis.getViewer());
		jf.pack();
		jf.setVisible(true);

		File jpegFile = new File(
				"src/main/java/uk/ac/ebi/intact/service/graph/visualization/graphml.jpg");
		vis.writeImage(jpegFile, "jpg");

		File pngFile = new File(
				"src/main/java/uk/ac/ebi/intact/service/graph/visualization/graphml.png");
		vis.writeImage(pngFile, "png");
	}

}
