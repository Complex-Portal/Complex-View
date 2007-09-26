package uk.ac.ebi.intact.service.exporterWebApp;

import java.io.IOException;
import java.io.OutputStream;

import uk.ac.ebi.intact.service.graph.visualization.GraphVisualization;

public class NetworkProducer implements ImageProducer{
	
	private String filename;
	
	public String createImage(OutputStream stream) throws IOException {
		
		GraphVisualization vis = new GraphVisualization(filename);
		vis.setDimension(500, 500);
		
		vis.writeImage(stream,"png");
		
		return "image/png";
	}
	
	public void setSource(String filename){
		this.filename = filename;
	}
	
}
