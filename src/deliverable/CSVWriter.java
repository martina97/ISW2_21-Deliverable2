package deliverable;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import entities.DBEntriesM2;
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
						   fileWriter.append(release.getIndex().toString());	//release
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
				   FileWriter fileWriter = new FileWriter("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\finali\\CSV FINALE BOOKKEEPER_PROPORTIONv2.csv")) {

				   fileWriter.append("RELEASE;FILENAME;LOC;LOC_added;MAX_LOC_Added;AVG_LOC_Added;Churn;MAX_Churn;AVG_Churn;NR;NAUTH;ChgSetSize;MAX_ChgSet;AVG_ChgSet;BUGGYNESS\n");
				   for (Release release : releasesList) {
					   //System.out.println("RELEASE CSV == " + release.getIndex());
					   for (JavaFile file : release.getFileList()) {
						   fileWriter.append(release.getIndex().toString());
						   fileWriter.append(";");
						   fileWriter.append(file.getName());
						   fileWriter.append(";");
						   fileWriter.append(file.getSize().toString());
						   fileWriter.append(";");
						   fileWriter.append(file.getLOCadded().toString());
						   fileWriter.append(";");
						   
						   if(file.getLOCadded().equals(0)) {
							   fileWriter.append("0");
							   fileWriter.append(";");
							   fileWriter.append("0");
						   }
						   else {
							   int maxLocAdded = Collections.max((file.getLocAddedList()));
							   fileWriter.append(String.valueOf(maxLocAdded));
							   fileWriter.append(";");
							   double avgLocAdded = Utils.calculateAverage(file.getLocAddedList());
							   //fileWriter.append(String.valueOf(avgChgSet));
							   fileWriter.append(String.format("%.2f",avgLocAdded));
						   }
						   fileWriter.append(";");
						   fileWriter.append(file.getChurn().toString());
						   fileWriter.append(";");
						   if(file.getChurn().equals(0)) {
							   fileWriter.append("0");
							   fileWriter.append(";");
							   fileWriter.append("0");
						   }
						   else {
							   int maxChurn = Collections.max((file.getChurnList()));
							   fileWriter.append(String.valueOf(maxChurn));
							   fileWriter.append(";");
							   double avgChurn = Utils.calculateAverage(file.getChurnList());
							   //fileWriter.append(String.valueOf(avgChgSet));
							   fileWriter.append(String.format("%.2f",avgChurn));
						   }
						   fileWriter.append(";");

						   fileWriter.append(file.getNr().toString());
						   fileWriter.append(";");
						   int size = file.getNAuth().size();
						   fileWriter.append(String.valueOf(size));
						   fileWriter.append(";");
						   fileWriter.append(file.getChgSetSize().toString());
						   fileWriter.append(";");
						   if(file.getChgSetSize().equals(0)) {
							   fileWriter.append("0");
							   fileWriter.append(";");
							   fileWriter.append("0");
						   }
						   else {
							   int maxChgSet = Collections.max((file.getChgSetSizeList()));
							   fileWriter.append(String.valueOf(maxChgSet));
							   fileWriter.append(";");
							   double avgChgSet = Utils.calculateAverage(file.getChgSetSizeList());
							   //fileWriter.append(String.valueOf(avgChgSet));
							   fileWriter.append(String.format("%.2f",avgChgSet));
							   
						   }
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
	
	
	public static void writeCsvMilestone2(List<DBEntriesM2> dBentriesList) {
		try (
		   FileWriter fileWriter = new FileWriter("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\BookkeeperCeciliaWeka.csv")) {
		   
		   fileWriter.append("Dataset;#TrainingRelease;Classifier;Precision;Recall;AUC;Kappa\n");
		   

		   for(DBEntriesM2 entry : dBentriesList) {
			   
			   Map<String, List<Double>> classifierMap = entry.getClassifier();

			   for (Map.Entry<String,List<Double>> mapEntry : classifierMap.entrySet()) {
				    String classifierName = mapEntry.getKey();
				    List<Double> value = mapEntry.getValue();
				    fileWriter.append(entry.getDatasetName());
					fileWriter.append(";");
					fileWriter.append(entry.getNumTrainingRelease().toString());
					fileWriter.append(";");
					fileWriter.append(classifierName);
					fileWriter.append(";");
					for(int i = 0;i<value.size();i++) {
						fileWriter.append(value.get(i).toString());
						fileWriter.append(";");
					}
					fileWriter.append("\n");

				  }

			   
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
