package deliverable;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import entities.JavaFile;
import entities.Ticket;



import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;




public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class.getName());
	public static final String NAME_PROJECT = "BOOKKEEPER";
	private static final String REPO = "D:/Programmi/Eclipse/eclipse-workspace/bookkeeper/.git";
	private static Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/bookkeeper");

	private static Repository repository;
	private static List<Release> releasesList;
	private static List<Ticket> ticketList;
	private static List<RevCommit> commitList;




   public static void main(String[] args) throws IllegalStateException, GitAPIException, IOException, JSONException {
	   
	   //metto in releases tutta la lista delle release del progetto
	   releasesList = GetJIRAInfo.getListRelease(NAME_PROJECT);
	   
	   

	   //salvo in commitList tutti i commit del progetto
	   commitList = GetGitInfo.getAllCommit(releasesList);
	   
	   /*
	   for (Release release : releasesList) {
		   System.out.println("RELEASE NUMERO " + release.getIndex());
		   System.out.println("IL NUMERO DEI COMMIT E' " + release.getCommitList().size());
		   System.out.println("############\n\n\n\n");


	   }
	   */
	   
	   
	   // prendo tutti i ticket di tipo bug ecc e i relativi campi che mi interessano
	   // DA JIRA e li metto in listaTicket
	   ticketList = GetJIRAInfo.retrieveTickets(NAME_PROJECT, releasesList);
	   
	   getCommitTicket();
	   
	   for (Ticket ticket : ticketList) {
		   System.out.println("FV ===== " + ticket.getFV());
	   }
	   //CSVWriter.writeCsvReleases(ticketList);
	   
	   // modifico le IV-AV dei ticket 
	   setIv();
	   checkAV();
	   //CSVWriter.writeCsvReleases(ticketList);
	   
	   Collections.reverse(ticketList); //inverto l'ordine dei ticket nella lista per semplicita' nel calcolo proportion
	   Proportion.proportion(ticketList);
	   //CSVWriter.writeCsvReleases(ticketList);
	   checkAV();
	   //CSVWriter.writeCsvReleases(ticketList);
	   
	   // per ogni release prendo tutti i file java che sono stati toccati nei commit 
	  // GetGitInfo.commitHistory(releasesList.get(0));
	   //GetGitInfo.commitHistory2(repoPath, releasesList);
	   GetGitInfo.getJavaFiles(repoPath, releasesList);
	   System.out.println("\n\nSTAMPO FILE RELEASE 1 : " );

	   for (JavaFile file : releasesList.get(0).getFileList()) {
		   System.out.println(file.getName());
	   }
	   System.out.println("\n\n");

	   GetGitInfo.checkRename(releasesList);


   	}
   
   public static void setIv() {
	   /*
	    * Per i ticket che hanno FV = 1 o OV = 1, setto IV = 1 e aggiusto le AV
	    */
	   for(Ticket ticket : ticketList) {
		  
		   if (ticket.getOV() == 1) {
			   ticket.getAV().clear(); //svuoto la lista di AV per poi aggiornarla con valori corretti
			   ticket.setIV(1);
			   
			   if (ticket.getFV() != 1) {
				   System.out.println("TICKET = " + ticket.getID() + " IV = " + ticket.getIV() + " FV = " + ticket.getFV() + "\n\n");
				   for (int i = ticket.getIV(); i<ticket.getFV();i++) {
					   ticket.getAV().add(i);
				   }
			   }
			   if( ticket.getFV() == 1) {
				   ticket.getAV().add(0);
			   } 
		   }
	   }
	
   }
   
   public static void checkAV() {
	   
	   for (Ticket ticket : ticketList) {
		   if (ticket.getIV() != 0 ) {	//se IV = 0 --> AV=[null]
			   if (ticket.getFV() > ticket.getIV() && ticket.getOV() >= ticket.getIV()) {
				   setAV(ticket);
				 
			   }
			   if (ticket.getFV() >= ticket.getIV() && ticket.getOV() < ticket.getIV() || ticket.getFV() < ticket.getIV()) {
				   ticket.setIV(0);
				   ticket.getAV().clear();
				   ticket.getAV().add(0);
			   }
			   if (ticket.getFV().equals(ticket.getIV())) {
				   ticket.getAV().clear();
				   ticket.getAV().add(0);
			   }
			   
			
		   }
	   }
	   
   }
   
   public static void setAV(Ticket ticket) {
	   ticket.getAV().clear(); //svuoto la lista di AV per poi aggiornarla con valori corretti

	   for (int i = ticket.getIV(); i<ticket.getFV();i++) {
		   ticket.getAV().add(i);
	   }
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
				   count++;
				   LocalDateTime commitDate = commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				   commitDateList.add(commitDate);
				   ticket.getCommitList().add(commit);

				   System.out.println("COMMIT ID = " + commit.getId() + " COMMIT DATE = " + commitDate);

		   }
		   }
		   System.out.println("Il numero di commit relativi al ticket e': " + count);
		   if ( !commitDateList.isEmpty()) {
			   Collections.sort(commitDateList);
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
		   
		   if (t.getResolutionDate() == null) {
			   ticket.remove();
		   }
	   }
	   
	   
   }
   
}