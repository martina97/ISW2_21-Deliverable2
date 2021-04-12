package deliverable;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.json.JSONException;

import entities.Ticket;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;



public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class.getName());
	public static final String NAME_PROJECT = "BOOKKEEPER";
	private static final String REPO = "D:/Programmi/Eclipse/eclipse-workspace/bookkeeper/.git";
	private static Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/bookkeeper");

	private static Repository repository;
	protected static List<Release> releasesList;
	private static List<Ticket> ticketList;
	private static ArrayList<RevCommit> commitList;




   public static void main(String[] args) throws IllegalStateException, GitAPIException, IOException, JSONException {
	   
	   //metto in releases tutta la lista delle release del progetto
	   releasesList = GetJIRAInfo.getListRelease(NAME_PROJECT);

	   //salvo in commitList tutti i commit del progetto
	   commitList = getGitINFO.getAllCommit(repoPath);
	   
	   // prendo tutti i ticket di tipo bug ecc e i relativi campi che mi interessano
	   // DA JIRA e li metto in listaTicket
	   ticketList = GetJIRAInfo.retrieveTickets2(NAME_PROJECT, releasesList);
	   
	   getCommitTicket();
	   
	   for (Ticket ticket : ticketList) {
		   System.out.println("FV ===== " + ticket.getFV());
	   }
	   CSVWriter.writeCsvReleases(ticketList);
	   
	   
	   /*
	   ArrayList<RevCommit> commitList = new ArrayList<>();
	   String filePath = "D:\\" + "Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable1\\csv\\TicketsAndMonths.csv";

	   //CSVWriter.writeCsv(filePath, ticketMonthMap);
	   //trovo l'anno in cui ci sono stati la maggior parte di ticket risolti
	   
	   getGitINFO.getAllCommit(repoPath, commitList);
	   ArrayList<LocalDate> resolutionDates = findCommitTicket(commitList, ticketList);
	   System.out.println("\n\nresolutionDates size == " + resolutionDates.size());
	   createEntriesCsv(resolutionDates);
	   //CSVWriter.writeCsv2(filePath, ticketList);
	    */


   	}
   
   public static void getCommitTicket() {
	   
	   ArrayList<LocalDateTime> commitDateList = new ArrayList<>();

	   for (Ticket ticket : ticketList) {
		   Integer count = 0;
		   String ticketID = ticket.getID();
		   System.out.println("TICKET ID = " + ticketID);
		   for(RevCommit commit : commitList) {
			   String message = commit.getFullMessage();
			   if (message.contains(ticketID +",") || message.contains(ticketID +"\r") || message.contains(ticketID +"\n")|| message.contains(ticketID + " ") || message.contains(ticketID +":")
						 || message.contains(ticketID +".")|| message.contains(ticketID + "/") || message.endsWith(ticketID) ||
						 message.contains(ticketID + "]")|| message.contains(ticketID+"_") || message.contains(ticketID + "-") || message.contains(ticketID + ")") ) {
			   //if (message.contains(ticketID)) {
				   count++;
				   LocalDateTime commitDate = commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				   commitDateList.add(commitDate);

				   System.out.println("COMMIT ID = " + commit.getId() + " COMMIT DATE = " + commitDate);
				   //System.out.println("COMMIT MESSAGE = " + commit.getFullMessage() + "\n");

			   //}
		   }
		   }
		   System.out.println("Il numero di commit relativi al ticket e': " + count);
		   if (commitDateList.size() != 0) {
			   //System.out.println("lista = " + commitDateList);
			   Collections.sort(commitDateList);
			   //System.out.println("\n\nlista ordinate = " + commitDateList);
			   LocalDateTime resolutionDate = commitDateList.get(commitDateList.size()-1);
			   System.out.println("data più recente = " + resolutionDate);
			   ticket.setResolutionDate(resolutionDate);
	           ticket.setFV(GetJIRAInfo.compareDateVersion(resolutionDate, releasesList));
	           System.out.println("FV ===" + ticket.getFV());

		   }
		   System.out.println("\n\n#######\n\n");
		   commitDateList.clear();


	   }
	   //rimuovo dalla lista dei ticket tutti i ticket che non hanno una resolutionDate, ossia che non hanno nessun commit associato

	   Iterator<Ticket> ticket = ticketList.iterator();

	   while (ticket.hasNext()) {
		   Ticket t = ticket.next();
		   //System.out.println("TICKET ID: " + t.getID() + " ---> " + t.getResolutionDate() + "\n\n");
		   
		   if (t.getResolutionDate() == null) {
			   ticket.remove();
		   }
	   }
	   
	   
   }
   
   
   
   
   public static void createEntriesCsv(ArrayList<LocalDate> resDates) {
	   
	  
	   
   }
   

   
   
   
   
   
   
   
}