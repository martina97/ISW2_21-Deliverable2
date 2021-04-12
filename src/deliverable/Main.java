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
	private static final String REPO = "D:/Programmi/Eclipse/eclipse-workspace/ISW2_21-Deliverable2_BOOKKEEPER/.git";
	private static Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/ISW2_21-Deliverable2_BOOKKEEPER");

	private static Repository repository;
	protected static List<Release> releases;
	private static List<Ticket> ticketList;



   public static void main(String[] args) throws IllegalStateException, GitAPIException, IOException, JSONException {
	   
	   // prendo tutta lista release progetto
	   releases = GetJIRAInfo.getListRelease(NAME_PROJECT);
	   
	   for (int i = 0 ; i<releases.size(); i++ ) {
		   System.out.println("RELEASES ===== " + releases.get(i).getIndex() + "--->" +  releases.get(i).getRelease()) ;
	   }
	   
	   
	   // prendo tutti i ticket di tipo bug ecc e i relativi campi che mi interessano
	   // DA JIRA e li metto in listaTicket
	   ticketList = GetJIRAInfo.retrieveTickets2(NAME_PROJECT, releases);
	   
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
   
   
   public static ArrayList<LocalDate> findCommitTicket(ArrayList<RevCommit> commitList,ArrayList<Ticket> ticketList ) throws IOException {
	   
	   ArrayList<LocalDate> commitDateList = new ArrayList<>();
	   ArrayList<LocalDate> resolutionDates = new ArrayList<>();
	   TreeMap<Integer, ArrayList<Month>> yearMonthMap  = new TreeMap<>();
	   FileWriter myWriter = new FileWriter("filename.txt");
	   Integer count = 0;
	   for (Ticket ticket : ticketList) {
		   count++;
		   System.out.println("IL TICKET E' : " + ticket.getID());
		   for (RevCommit rev : commitList) {
			   String commit = rev.getFullMessage();
			   //System.out.println("MESSAGGIO COMMIT : \n" + commit.getFullMessage() + "\n\n\n");
			   //myWriter.write(commit.getFullMessage() + "\n\n\n");
			   String ticketID1 = ticket.getID();
			   String ticketID2 = ticketID1.replace("DAFFODIL","DFDL");


			   //if (commit.getFullMessage().contains(ticketID1) || commit.getFullMessage().contains(ticketID2)) {
			   if (commit.contains(ticketID1 +",") || commit.contains(ticketID1 +"\r") || commit.contains(ticketID1 +"\n")|| commit.contains(ticketID1 + " ") || commit.contains(ticketID1 +":")
							 || commit.contains(ticketID1 +".")|| commit.contains(ticketID2 + ",") || commit.endsWith(ticketID1) || commit.endsWith(ticketID2) ||
							 commit.contains(ticketID2 + "\r")|| commit.contains(ticketID2+"\n") || commit.contains(ticketID2 + " ") || commit.contains(ticketID2 + ":") || commit.contains(ticketID2 + ".")) {				   
				   System.out.println(rev.getId());
				   System.out.println(commit);

				   /*
				   PersonIdent authorIdent = commit.getAuthorIdent();
				   Date authorDate = authorIdent.getWhen();
				   TimeZone authorTimeZone = authorIdent.getTimeZone();
				   PersonIdent committerIdent = commit.getCommitterIdent();
				   Instant data = Instant.ofEpochSecond(commit.getCommitTime());
				   LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(commit.getCommitTime()), ZoneOffset.UTC);
				   */
				   LocalDate commitDate = rev.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				   commitDateList.add(commitDate);
				   //System.out.println("commitDate = " + commitDate);


				   
				   //System.out.println(ldt + "mese : " + ldt.getMonth() + "anno : " + ldt.getYear());
			   }
		   }
		   

		   if (commitDateList.size() != 0) {
			   //System.out.println("lista = " + commitDateList);
			   Collections.sort(commitDateList);
			   //System.out.println("\n\nlista ordinate = " + commitDateList);
			   LocalDate resolutionDate = commitDateList.get(commitDateList.size()-1);
			   //System.out.println("data più recente = " + resolutionDate);
			   ticket.setResolutionDate(resolutionDate);

		   }
		   

		   commitDateList.clear();
		   

		   System.out.println("################################\n\n");
	   
	   }
	   System.out.println("ALEEEEEEEEEE " + count);
   
	   Iterator<Ticket> ticket = ticketList.iterator();
   	   
	   //rimuovo dalla lista dei ticket tutti i ticket che non hanno una resolutionDate, ossia che non hanno nessun commit associato
	   while (ticket.hasNext()) {
		   Ticket t = ticket.next();
		   //System.out.println("TICKET ID: " + t.getID() + " ---> " + t.getResolutionDate() + "\n\n");
		   
		   if (t.getResolutionDate() == null) {
			   ticket.remove();
		   }
		   else {
			   resolutionDates.add(t.getResolutionDate());
			   Integer year = t.getResolutionDate().getYear();
			   yearMonthMap.putIfAbsent(year, new ArrayList<Month>());
			   yearMonthMap.get(year).add(t.getResolutionDate().getMonth());
			   
		   }
		   
	   }
	   yearMonthMap.forEach((key, value) -> logger.log(Level.INFO, "key: {0} --> value: {1} ",new Object[] { key, value,}));
	   myWriter.close();
	   return resolutionDates;
   }
   
   
   public static void createEntriesCsv(ArrayList<LocalDate> resDates) {
	   
	  
	   
   }
   

   
   
   
   
   
   
   
}