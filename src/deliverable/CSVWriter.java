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

	
	
	public static void writeCsvReleases(List<Ticket> ticketList) {
	 
	  try (
	   FileWriter fileWriter = new FileWriter("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\TicketsAndReleases4.csv")) {
	   
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
