package uk.ac.ebi.intact.service.exporterWebApp;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImageServlet extends HttpServlet{
	
	private HashMap<String, ImageProducer> producerCache = new HashMap<String, ImageProducer>();
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{

		try {
			ImageProducer imageProducer = null;
			
			if (producerCache.containsKey(request.getQueryString())){
				System.out.println("IF "+producerCache.get(request.getQueryString()));
				imageProducer = (ImageProducer) producerCache.get(request.getQueryString());
			} else {
				String className = "uk.ac.ebi.intact.service.exporterWebApp.".concat(request.getQueryString());
				System.out.println("ELSE "+className);
				imageProducer = (ImageProducer) Class.forName(className).newInstance();
				if (className.contains("NetworkProducer")){
					NetworkProducer producer = (NetworkProducer)imageProducer;
					producer.setSource("C:/Dokumente und Einstellungen/Nadin/Desktop/graphml.xml");
					imageProducer = (ImageProducer)producer;
				}
				producerCache.put(request.getQueryString(), imageProducer);
			}
			
			String type = imageProducer.createImage(response.getOutputStream());
			response.setContentType(type);
		}catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
