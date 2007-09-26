package uk.ac.ebi.intact.service.exporterWebApp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;

import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.service.exporter.IntactExporter;

public class Servlet extends HttpServlet{
	
	public static final Log log = LogFactory.getLog(Servlet.class);
	
	private String [] acs;
	
	private String version;
	
	private IntactExporter ie = new IntactExporter();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException{
		
		

		acs = checkAccessionNumbers(request.getParameterValues("p"));			 			
		version = request.getParameter("version");
		
		if (version == null ){
			request.setAttribute("ErrorMessage", "No version found. Please select a valid version");
			request.setAttribute("ErrorType", "SimpleError");				
			RequestDispatcher view = request.getRequestDispatcher("error.jsp");
			view.forward(request, response);
		}
		
		if (acs == null) {
			request.setAttribute("ErrorMessage", "No interactor found. Please enter valid AccessionIdentifier");
			request.setAttribute("ErrorType", "SimpleError");
			RequestDispatcher view = request.getRequestDispatcher("error.jsp");
			view.forward(request, response);
		}
		
		if (version.equals("MITAB") && acs != null){

			try {	
				Collection<BinaryInteraction> interactions = ie.exportToPsiMiTab(acs);
				
				if (!interactions.isEmpty()){
					response.setContentType("text/plain");
					
					PsimiTabWriter writer = new PsimiTabWriter();
					PrintWriter out = response.getWriter();
					writer.write(interactions, out);
				} else {
					throw new Exception("BinaryInteractions are null");
				}
				
			} catch (Exception e) {
				request.setAttribute("ErrorMessage", e);
				request.setAttribute("ErrorType", "Exception");	
				RequestDispatcher view = request.getRequestDispatcher("error.jsp");
				view.forward(request, response);
			}
		}
		
		if (version.equals("2.5") && acs != null){	 

			try {
				EntrySet entrySet = ie.exportToPsiMi25(acs);
				
				if (entrySet != null){
					response.setContentType("text/xml");
					
					PsimiXmlWriter writer = new PsimiXmlWriter();
					PrintWriter out = response.getWriter();
					writer.write(entrySet, out);
				} else {
					throw new Exception("EntrySet is null");
				}
		 
			} catch (Exception e) {
				request.setAttribute("ErrorMessage", e);
				request.setAttribute("ErrorType", "Exception");	
				RequestDispatcher view = request.getRequestDispatcher("error.jsp");
				view.forward(request, response);
			} 
		}
		
 		if (version.equals("1.0") && acs != null){
			PrintWriter out = response.getWriter();
			//TODO
			out.println("TODO");
		}
 	
		if (version.equals("GraphML") && acs != null){
			
			try {
				Document doc = ie.exportToGraphML(acs);
				
				if (doc != null){
					response.setContentType("text/xml");
					
					XMLOutputter outputter = new XMLOutputter(" ",true);
					File file = new File("C:/Dokumente und Einstellungen/Nadin/Desktop/graphml.xml");
					FileWriter writer = new FileWriter(file);
					outputter.output(doc, writer);
					PrintWriter out = response.getWriter();	
					outputter.output(doc, out);
				
				} else {
					throw new Exception("GraphML is null");
				}
		 
			} catch (Exception e) {
				request.setAttribute("ErrorMessage", e);
				request.setAttribute("ErrorType", "Exception");	
				RequestDispatcher view = request.getRequestDispatcher("error.jsp");
				view.forward(request, response);
			} 
		}

	}

	private String[] checkAccessionNumbers(String[] acs) {
		
		Collection<String> accessionNumbers = new ArrayList<String>();
		
		if (acs.length != 0){	
			
			for (int i = 0; i < acs.length; i++){
				if (acs[i].equals("")){
					break;
				}
				if (acs[i].contains(",")){
					String [] splitAcs = acs[i].split(",");
					for (int j = 0; j < splitAcs.length; j++){
						accessionNumbers.add(splitAcs[j]);
					}
				} else {
					accessionNumbers.add(acs[i]);
				}
			}
		}
 		if (accessionNumbers.isEmpty()){
 			return null;
 		} else {
 			return accessionNumbers.toArray(new String[0]);
 		}
	}

}
