package deliverable;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import entities.JavaFile;
import entities.Release;
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
import java.util.Map;
import java.util.Scanner;




public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class.getName());

	private static List<Release> releasesList;
	private static List<Ticket> ticketList;
	private static List<RevCommit> commitList;
	private static Map<String, List<String>> fileAliasMap;


   public static void main(String[] args) throws IllegalStateException, GitAPIException, IOException, JSONException {
	   
	   // inserisco come input il nome del progetto che voglio analizzare (BOOKKEEPER / SYNCOPE)
		Scanner input = new Scanner(System.in);
		logger.log(Level.INFO,"INSERIRE IN MAIUSCOLO IL NOME DEL PROGETTO CHE SI DESIDERA ANALIZZARE.\n\nIN PARTICOLARE "
				+ "INSERIRE LA STRINGA ''BOOKKEEPER'' OPPURE ''SYNCOPE''" );
		
		String nameProject = input.next();
		String nameProjectLowerCase = nameProject.toLowerCase();
		String repo = "D:/Programmi/Eclipse/eclipse-workspace/" + nameProjectLowerCase + "/.git";
		Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/" + nameProjectLowerCase);
		input.close();

		
	   //metto in releases tutta la lista delle release del progetto
	   releasesList = GetJIRAInfo.getListRelease(nameProject);
	   
	   

	   //salvo in commitList tutti i commit del progetto
	   commitList = GetGitInfo.getAllCommit(releasesList, repoPath);
	

	   // inverto l'ordine dei commit appartenenti alle release per gestire poi i rename 
	   for (Release release : releasesList) {
		   Collections.reverse(release.getCommitList()); 
	   }
	   
	   
	   // prendo tutti i ticket di tipo bug ecc e i relativi campi che mi interessano DA JIRA e li metto in listaTicket
	   ticketList = GetJIRAInfo.retrieveTickets(nameProject, releasesList);
	   
	   getCommitTicket();
	   	   
	   // modifico le IV-AV dei ticket 
	   setIv();
	   checkAV();
	   
	   logger.log(Level.INFO,"Numero ticket = {0}.", ticketList.size());
	  	   
	   // PROPORTION VECCHIO 
	   Proportion.checkTicket2(ticketList);
	   Proportion.modifyListAV(ticketList);
	   

	   /*
	   // PROPORTION NUOVO
	   Collections.reverse(ticketList); //inverto l'ordine dei ticket nella lista per semplicita' nel calcolo proportion

	   Proportion.proportion(ticketList); //PROPORTION NUOVO
	   
	   checkAV();	//PROPORTION NUOVO
	   */
	   
	   
	   /* per ogni release prendo tutti i file java che sono stati toccati nei commit 
	    * e setto inizialmente buggyness = "no" 
	    */
	   
	  logger.log(Level.INFO,"###### checkRename ###### ");

	  fileAliasMap = GetGitInfo.checkRename(releasesList, repo);
	  logger.log(Level.INFO,"FileAliasMap SIZE = {0}.", fileAliasMap.size());
	   
	   removeHalfRelease(releasesList, ticketList);
	   

	   logger.log(Level.INFO,"###### getJavaFiles ###### ");

	   GetGitInfo.getJavaFiles(repoPath, releasesList, fileAliasMap);
	   
	   
	   logger.log(Level.INFO,"###### checkBuggyness ###### ");
	   GetGitInfo.checkBuggyness(releasesList, ticketList,fileAliasMap );
	   

	   logger.log(Level.INFO,"###### getMetrics ###### ");

	   Metrics.getMetrics(releasesList, repo);

	   logger.log(Level.INFO,"\n\nSTAMPO BUGGYNESS");
	   int numBugg = 0;
	   int numFile = 0;
	   for (Release release : releasesList) {
		   for (JavaFile file : release.getFileList()) {
			   numFile++;
			   if (file.getBugg().equals("Yes")) {
				   numBugg++;
			   }

		   }
	   }
	   System.out.println("\n\nnumFile == " + numFile + "\tnumBugg == " + numBugg);
   	}                                                                                                                                                                                                                                                                                                                                           

   
   public static void removeHalfRelease(List<Release> releasesList, List<Ticket> ticketList) {
	   int releaseNumber = releasesList.size();
	   float half = (float) releaseNumber / 2;
	   int halfRelease = (int) half; // arrotondo in difetto, ora il numero di release che voglio e' la meta'
	   
	   logger.log(Level.INFO,"NUMERO RELEASE == = {0}.", releaseNumber);
	   logger.log(Level.INFO,"HALF RELEASE == = {0}.", halfRelease);

	   Iterator<Release> i = releasesList.iterator();
	   while (i.hasNext()) {
	      Release s = i.next(); 
	      if (s.getIndex() > halfRelease) {
	      // Do something
	    	  i.remove();
	      }
	   }
	   
	   removeTickets(halfRelease, ticketList);
   }

   
   public static void removeTickets(int halfRelease, List<Ticket> ticketList) {
	   
	   Iterator<Ticket> i = ticketList.iterator();
	   while (i.hasNext()) {
		  Ticket t = i.next(); 
	      if (t.getIV() > halfRelease) {	//se IV>halfRelease --> rimuovo ticket
	    	  i.remove();
	      }
	      if(t.getOV() > halfRelease || t.getFV() > halfRelease) {
	    	  List<Integer> aV = new ArrayList<>();
	    	  for(int k = t.getIV(); k<halfRelease + 1; k++) {
	    		  aV.add(k);
	    	  }
	    	  t.setAV(aV);
	      }
	   }
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
		   String ticketID = ticket.getID();
		   for(RevCommit commit : commitList) {
			   String message = commit.getFullMessage();
			   if (message.contains(ticketID +",") || message.contains(ticketID +"\r") || message.contains(ticketID +"\n")|| message.contains(ticketID + " ") || message.contains(ticketID +":")
						 || message.contains(ticketID +".")|| message.contains(ticketID + "/") || message.endsWith(ticketID) ||
						 message.contains(ticketID + "]")|| message.contains(ticketID+"_") || message.contains(ticketID + "-") || message.contains(ticketID + ")") ) {
				   LocalDateTime commitDate = commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				   commitDateList.add(commitDate);
				   ticket.getCommitList().add(commit);

			   }
		   }
		   if ( !commitDateList.isEmpty()) {
			   Collections.sort(commitDateList);
			   LocalDateTime resolutionDate = commitDateList.get(commitDateList.size()-1);
			   ticket.setResolutionDate(resolutionDate);
	           ticket.setFV(GetJIRAInfo.compareDateVersion(resolutionDate, releasesList));

		   }
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