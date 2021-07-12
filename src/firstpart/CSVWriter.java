package firstpart;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import entities.JavaFile;
import entities.M2Entries;
import entities.Release;
import entities.Ticket;

import java.util.logging.Level;


public class CSVWriter {
	
	
	
	static Logger logger = Logger.getLogger(CSVWriter.class.getName());
	private static final String ERROR = "Error in csv writer";  

	
	
	public static void writeCsvBugg(List<Release> releasesList, String nameProject) {
		try (
				   FileWriter fileWriter = new FileWriter("output\\first part\\Finale_" + nameProject + ".csv")) {

				   fileWriter.append("RELEASE;FILENAME;LOC;LOC_added;MAX_LOC_Added;AVG_LOC_Added;Churn;MAX_Churn;AVG_Churn;NR;NAUTH;ChgSetSize;MAX_ChgSet;AVG_ChgSet;BUGGYNESS\n");
				   for (Release release : releasesList) {
					   for (JavaFile file : release.getFileList()) {
						   appendMetrics(fileWriter, release, file);
					   }
				   } 
				  } catch (Exception ex) {
					  logger.log(Level.SEVERE,ERROR);
					  ex.printStackTrace();
				  }
				 }	
	
	public static void appendMetrics(FileWriter fileWriter, Release release, JavaFile file) throws IOException {
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
			   fileWriter.append(String.format("%.2f",avgChgSet));
			   
		   }
		   fileWriter.append(";");
		   fileWriter.append(file.getBugg());
		   fileWriter.append("\n");
	}
	
	public static void writeTickets(List<Ticket> ticketList, String test) {
	 
	  try (
	   FileWriter fileWriter = new FileWriter("output\\first part"+test+".csv")) {
	   
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
		  logger.log(Level.SEVERE,ERROR);
		  ex.printStackTrace();
	  
	  	}
	 }	

	
	public static void writeCsvMilestone2(List<M2Entries> dBentriesList, String projName) {
		try (
		   FileWriter fileWriter = new FileWriter("output\\second part"+projName+" Weka.csv")) {
		   
		   fileWriter.append("Dataset;#TrainingRelease;%training;%Defective in training;%Defective in testing;Classifier;"
		   		+ "Feature Selection;Balancing;Sensitivity;TP;FP;TN;FN;Precision;Recall;AUC;Kappa\n");
		   

		   for(M2Entries entry : dBentriesList) {
			  
			    fileWriter.append(entry.getDatasetName());
				fileWriter.append(";");
				fileWriter.append(entry.getNumTrainingRelease().toString());
				fileWriter.append(";");
				fileWriter.append(Utils.doubleTransform(entry.getTrainingPerc()));
				fileWriter.append(";");
				fileWriter.append(Utils.doubleTransform(entry.getDefectPercTrain()));
				fileWriter.append(";");
				fileWriter.append(Utils.doubleTransform(entry.getDefectPercTest()));
				fileWriter.append(";");
				fileWriter.append(entry.getClassifierName());
				fileWriter.append(";");
				fileWriter.append(entry.getFeatureSelection());
				fileWriter.append(";");
				fileWriter.append(entry.getBalancing());
				fileWriter.append(";");
				fileWriter.append(entry.getSensitivity());
				fileWriter.append(";");
				fileWriter.append(entry.getTP().toString());
				fileWriter.append(";");
				fileWriter.append(entry.getFP().toString());
				fileWriter.append(";");
				fileWriter.append(entry.getTN().toString());
				fileWriter.append(";");
				fileWriter.append(entry.getFN().toString());
				fileWriter.append(";");
				fileWriter.append(Utils.doubleTransform(entry.getPrecision()));
				fileWriter.append(";");
				fileWriter.append(Utils.doubleTransform(entry.getRecall()));
				fileWriter.append(";");
				fileWriter.append(Utils.doubleTransform(entry.getAuc()));
				fileWriter.append(";");
				fileWriter.append(Utils.doubleTransform(entry.getKappa()));
				fileWriter.append("\n");
		   }
	
		  } catch (Exception ex) {
			  logger.log(Level.SEVERE,ERROR);
			  ex.printStackTrace();
		  
		  	}	 
	}
	
	
	public static void main(String[] args) {
		 // main method
	}
		 
		 
}
