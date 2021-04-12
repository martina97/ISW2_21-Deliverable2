package deliverable;

import java.io.FileWriter;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.logging.Logger;

import entities.Ticket;

import java.util.logging.Level;


public class CSVWriter {
	
	
	
	static Logger logger = Logger.getLogger(CSVWriter.class.getName());

	
	public static void writeCsv(String filePath, SortedMap<Month, ArrayList<String>> ticketMonthMap) {
		 
		
	    logger.log(Level.INFO, "starting write user.csv file: {0}.",filePath); 

		  try (
		   FileWriter fileWriter = new FileWriter(filePath)) {
		   
		   fileWriter.append("Month ; Ticket ID\n");
		   for(Entry<Month, ArrayList<String>> entry : ticketMonthMap.entrySet()) {
			   Month month = entry.getKey();
			   for(String listTicketId : ticketMonthMap.get(month)) {
				   fileWriter.append(String.valueOf(month));
				   fileWriter.append(";");
				   fileWriter.append(listTicketId);
				   fileWriter.append("\n");
			   }
		   }
		   
		   
		  } catch (Exception ex) {
			  logger.log(Level.SEVERE,"Error in csv writer");
			  ex.printStackTrace();
		  
		  }
		 }
	
public static void writeCsv2(String filePath, ArrayList<Ticket> ticketList) {
		 
		
	    logger.log(Level.INFO, "starting write user.csv file: {0}.",filePath); 

		  try (
		   FileWriter fileWriter = new FileWriter(filePath)) {
		   
		   fileWriter.append("Month ; Ticket ID\n");
		   for (Ticket ticket : ticketList) {
			   fileWriter.append(ticket.getResolutionDate().toString());
			   fileWriter.append(";");
			   fileWriter.append(ticket.getID());
			   fileWriter.append("\n");
		   }
		   
		   
		   
		  } catch (Exception ex) {
			  logger.log(Level.SEVERE,"Error in csv writer");
			  ex.printStackTrace();
		  
		  }
		 }	


public static void writeCsvReleases(List<Ticket> ticketList) {
	 
	

	  try (
	   FileWriter fileWriter = new FileWriter("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\csvReleases.csv")) {
	   
	   fileWriter.append("TICKET ID ; IV ; OV ; FV ; AV\n");
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
