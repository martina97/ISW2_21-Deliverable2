package deliverable;

import java.io.FileWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.logging.Logger;

import entities.JavaFile;
import entities.Ticket;

import java.util.logging.Level;


public class CSVWriter {
	
	
	
	static Logger logger = Logger.getLogger(CSVWriter.class.getName());

	
	public static void writeCsvBugg(List<Release> releasesList) {
		try (
				   FileWriter fileWriter = new FileWriter("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\CSV FINALE13.csv")) {
				   
				   fileWriter.append("RELEASE ; FILENAME ; NR ; NAUTH ; BUGGYNESS \n");
				   for (Release release : releasesList) {
					   System.out.println("RELEASE CSV == " + release.getIndex());
					   for (JavaFile file : release.getFileList()) {
						   fileWriter.append(release.getIndex().toString());
						   fileWriter.append(";");
						   fileWriter.append(file.getName());
						   fileWriter.append(";");
						   fileWriter.append(file.getNr().toString());
						   fileWriter.append(";");
						   fileWriter.append(file.getNAuth().toString());
						   fileWriter.append(";");
						   fileWriter.append(file.getBugg());
						   fileWriter.append("\n");
					   }
				   } 
				  } catch (Exception ex) {
					  logger.log(Level.SEVERE,"Error in csv writer");
					  ex.printStackTrace();
				  
				  }
				 }	
				
	
	
	public static void writeCsvBugg2(List<Release> releasesList) {
		try (
				   FileWriter fileWriter = new FileWriter("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\CSV FINALE14.csv")) {
				   
				   fileWriter.append("RELEASE ; FILENAME ; NR ; NAUTH; ChgSetSize ; MAX_ChgSet ; BUGGYNESS \n");
				   for (Release release : releasesList) {
					   System.out.println("RELEASE CSV == " + release.getIndex());
					   for (JavaFile file : release.getFileList()) {
						   fileWriter.append(release.getIndex().toString());
						   fileWriter.append(";");
						   fileWriter.append(file.getName());
						   fileWriter.append(";");
						   fileWriter.append(file.getNr().toString());
						   fileWriter.append(";");
						   int size = file.getNAuth().size();
						   fileWriter.append(String.valueOf(size));
						   fileWriter.append(";");
						   fileWriter.append(file.getChgSetSize().toString());
						   fileWriter.append(";");
						   int maxChgSet = Collections.max((file.getChgSetSizeList()));
						   fileWriter.append(String.valueOf(maxChgSet));
						   fileWriter.append(";");
						   fileWriter.append(file.getBugg());
						   fileWriter.append("\n");
					   }
				   } 
				  } catch (Exception ex) {
					  logger.log(Level.SEVERE,"Error in csv writer");
					  ex.printStackTrace();
				  
				  }
				 }	
	
	public static void writeCsvReleases(List<Ticket> ticketList) {
	 
	  try (
	   FileWriter fileWriter = new FileWriter("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\DOPO PROP VECCHIO.csv")) {
	   
	   fileWriter.append("TICKET ID ; IV ; OV ; FV ; AV \n");
	   for (Ticket ticket : ticketList) {
		   fileWriter.append(ticket.getID());
		   fileWriter.append(";");
		   fileWriter.append(ticket.getIV().toString());
		   fileWriter.append(";");
		   fileWriter.append(ticket.getOV().toString());
		   fileWriter.append(";");
		   fileWriter.append(ticket.getFV().toString());
		   fileWriter.append(";");
		   fileWriter.append(ticket.getAV().toString());
		   fileWriter.append("\n");
	   }
	   
	   
	   
	  } catch (Exception ex) {
		  logger.log(Level.SEVERE,"Error in csv writer");
		  ex.printStackTrace();
	  
	  	}
	 }	
	
	public static void main(String[] args) {
		 
		 // main
	}
		 
		 
}
