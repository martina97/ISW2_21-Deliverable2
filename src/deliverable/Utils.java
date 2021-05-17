package deliverable;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import entities.JavaFile;
import entities.Ticket;





public class Utils {
	//private static final String REPO = "D:/Programmi/Eclipse/eclipse-workspace/bookkeeper/.git";
	private static final String REPO = "D:/Universita/magistrale/isw2/Codici/bookkeeper/.git";

	//private static Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/bookkeeper");
	private static Path repoPath = Paths.get("D:/Universita/magistrale/isw2/Codici/bookkeeper");
	private static Repository repository;

	private static HashMap<RevCommit, Integer> releaseCommitMap = new HashMap<>(); // hashmap con commit e release a cui

	public static void printTreeMap(SortedMap<Object,Object> map) {
		  map.forEach((key, value) -> System.out.println(key + "= " + value + "\n\n"));
	}
	
	
	public static void printHashMap(HashMap<String, List<String>> fileAliasMap) {
		
		  for (Map.Entry<String,List<String>> entry : fileAliasMap.entrySet()) {
			    String key = entry.getKey(); 
			    List<String> oldPaths = entry.getValue();
			    System.out.println(key + "\n");
			    for (String name : oldPaths) {
				    System.out.println("old path == " + name + "\n");
			    }
			    System.out.println("############\n");
			  
		  }
	}
	
public static void checkTicket2(List<Ticket> listaTicket) {

		

		int i;

		
		ArrayList<Ticket> goodTicket = new ArrayList<>();
		ArrayList<Ticket> noIVTicket = new ArrayList<>();

		System.out.println("I Ticket iniziali sono: ");

		for (i = 0; i < listaTicket.size(); i++) {
			System.out.println("ticket = " + listaTicket.get(i).getID() + " \t\tIV = " + listaTicket.get(i).getIV()
					+ " \t\tOV = " + listaTicket.get(i).getOV() + " \t\tFV = " + listaTicket.get(i).getFV()
					+ " \t\tAV = " + listaTicket.get(i).getAV());
		}

		// analizzo la lista dei ticket presi da jira che hanno AV, IV, OV e FV

		for (i = 0; i < listaTicket.size(); i++) {

			// se OV = 1 e IV = 0 --> sicuramente IV = 1
			if (listaTicket.get(i).getOV() == 1 && listaTicket.get(i).getIV() == 0) {
				listaTicket.get(i).setIV(1);
				listaTicket.get(i).getAV().remove(null);
				listaTicket.get(i).getAV().add(1);
			}

			else {

				// se IV>OV o IV>FV (x es il 638 e 633), magari sono stati inseriti dati su JIRA
				// relativi
				// alla AV sbagliati (non affidabili), quindi setto IV=0 e lo calcolo tramite
				// proportion
				if (listaTicket.get(i).getIV() > listaTicket.get(i).getOV()
						|| listaTicket.get(i).getIV() > listaTicket.get(i).getFV()) {
					listaTicket.get(i).setIV(0);
					listaTicket.get(i).getAV().clear();
				}

			}
		}

		for (i = 0; i < listaTicket.size(); i++) {

			// se FV = OV e non ho AV (IV == 0) , rimuovo il ticket perche nel calcolo del
			// predictedIV con proportion, la cui formula e IV = FV-(FV-OV)*P, il termine a
			// dx fa zero
			// e quindi proportion non l'ho utilizzato

			// IV = 0 se
			// il ticket non ha AV presa da JIRA
			// AV presa da JIRA e inconsistente (IV>=OV o IV>FV)
			if (listaTicket.get(i).getIV() == 0 && listaTicket.get(i).getFV() == listaTicket.get(i).getOV()) {
				listaTicket.remove(i);
				i--;

			}
		}

		

		// System.out.println("listaTicket size = " + listaTicket.size())

		// RetrieveTicketsJIRA.printArrayList(listaTicket)

		

		
		
		

		// inverto ordine listaTicket per semplicita
		Collections.reverse(listaTicket);

		// ora ho la lista perfetta di ticket, non dovro piu toglierli, quindi associo
		// un indice a ogni ticket cosi da rendere piu semplice
		// il confronto tra le liste
		for (i = 0; i < listaTicket.size(); i++) {
			listaTicket.get(i).setIndex(i);
		}

		//System.out.println("\n***********  La lista ordinata di ticket e: ************\n ")
		// GetJIRAInfo.printArrayList(listaTicket)


		// prendo i ticket che rispettano le condizioni per implementare proportion
		for (i = 0; i < listaTicket.size(); i++) {

			// System.out.println(listaTicket.get(i).getID())

			LocalDateTime creationDate = listaTicket.get(i).getCreationDate();
			LocalDateTime resolutionDate = listaTicket.get(i).getResolutionDate();

			Integer FV = listaTicket.get(i).getFV();
			Integer OV = listaTicket.get(i).getOV();
			Integer IV = listaTicket.get(i).getIV();
			

			// IV!=0
			if (IV != 0) {
				// FV!=IV e FV!=OV senno P=0
				if (FV != IV && FV != OV && IV <= OV) { // ticket buoni per calcolare proportion
					// System.out.println(listaTicket.get(i).getID())
					/*
					 * System.out.println("ticket = " + listaTicket.get(i).getID() + "\t\tcreated: "
					 * + listaTicket.get(i).getCreationDate() + "\t\tresolution: " +
					 * listaTicket.get(i).getResolutionDate() +"\t\t IV: " +
					 * listaTicket.get(i).getIV() + "\t\t OV: "+ listaTicket.get(i).getOV()
					 * +"\t\tFV: " + listaTicket.get(i).getFV() )
					 */
					goodTicket.add(listaTicket.get(i));
				} 

			} else { // IV = 0
				// aggiungi alla lista per cui calcolare IV con proportion
				noIVTicket.add(listaTicket.get(i));
			}


			// SE HO OV = 1 E NON HO IV, SICURAMENTE, POICHe IV<= OV --> IV = 1 PER
			// FORZA!!!!!!!!!!!!!!

		}

		// GetJIRAInfo.printArrayList(goodTicket)

		//System.out.println("\n\nnoIVTicket.size  = " + noIVTicket.size())
		// GetJIRAInfo.printArrayList(noIVTicket)

		// chiamo il metodo proportion
		System.out.println("\n\n------------------PROPORTION----------------------\n");
		proportion(goodTicket, noIVTicket, listaTicket);

	}

public static void modifyListAV(List<Ticket> listaTicket) {

	System.out.println("\n\n-----------------------MODIFICO AV -----------------------------");
	for (int i = 0; i < listaTicket.size(); i++) {
		Ticket ticket = listaTicket.get(i);
		Integer IV = ticket.getIV();
		Integer OV = ticket.getOV();
		Integer FV = ticket.getFV();

		ticket.getAV().clear(); // elimino gli elementi di AV, poiche potrei avere dati inconsistenti presi da
								// JIRA, e setto manualmente gli altri.

		for (int k = IV; k < FV; k++) {

			ticket.getAV().add(k);
		}

		System.out.println(ticket.getID() + "\t\t IV: " + IV + "\t\t OV: " + OV + "\t\tFV: " + FV + "\t\t AV: "
				+ ticket.getAV());
	}

	System.out.println("listaTicket size = " + listaTicket.size());

	System.out.println("\n\n------------");

	/*
	 * for(int i = 0; i<listaTicket.size();i++) {
	 * 
	 * //controllo le AV, e se in AV c'e una release > 7, la cancello Ticket ticket
	 * = listaTicket.get(i); for(int k = 0 ; k <ticket.getAV().size(); k++) {
	 * if(ticket.getAV().get(k) > halfRelease) {
	 * ticket.getAV().remove(ticket.getAV().get(k)); k--; } }
	 * 
	 * System.out.println(ticket.getID() + "\t\t IV: " + ticket.getIV() +
	 * "\t\t OV: "+ ticket.getOV() +"\t\tFV: " + ticket.getFV() + "\t\t AV: " +
	 * ticket.getAV() ); }
	 */
	System.out.println("listaTicket size = " + listaTicket.size());

}



public static void proportion(List<Ticket> listGood, List<Ticket> listNoIV, List<Ticket> listaTicket) {

	Integer fV=0;
	Integer iV=0;
	Integer oV=0;
	// uso tree map perche ha i valori di keys in ordine
	TreeMap<Integer, Integer> proportionValue = new TreeMap<>(); // contiene indice del ticket e il valore di P

	int numTicket = listaTicket.size();
	System.out.println("numTicket = " + numTicket);
	float percentage = (float) (numTicket * 0.01);
	

	int perc = Math.round(percentage); // Math.round() converts a floating-point number to the nearest integer by
										// first adding 0.5 and then truncating value after decimal point
	System.out.println("percentage = " + (int) percentage);
	System.out.println("perc = " + (int) perc);

	for (int i = 0; i < listGood.size(); i++) {
		 iV = listGood.get(i).getIV();
		 oV = listGood.get(i).getOV();
		 fV = listGood.get(i).getFV();
		String id = listGood.get(i).getID();
		Integer index = listGood.get(i).getIndex();

		float proportion = (float) (fV - iV) / (fV - oV);
		
		Integer p = Math.round(proportion); // ARROTONDO PER ECCESSO
		
		System.out.println(index + ")\tTicket ID = " + id + "\t\tP_round = " + p);

		System.out.println("proportion = " + proportion);

		System.out.println("P = " + p);

		proportionValue.put(index, p);

	}



	for (int j = 0; j < listNoIV.size(); j++) {

		Ticket ticket = listNoIV.get(j);
		String ticketID = ticket.getID();
		Integer index = ticket.getIndex();
		oV = ticket.getOV();
		fV = ticket.getFV();


		System.out.println(index + ")\tticketID = " + ticketID);
		ArrayList<Integer> listIndex = new ArrayList<>();
		// scorro HashMap
		for (Integer i : proportionValue.keySet()) {

			

			if (index > i) {

				if (listIndex.size() < perc) { // non sono ancora arrivata a 4, quindi posso aggiungere

					listIndex.add(i);
				}

				else {
					listIndex.remove(0);
					listIndex.add(i);

				}

			}
		}

		System.out.println(listIndex + "\n\n");
		

		// somma degli eleementi dell'array(le P)
		Integer sum = 0;

		for (int i = 0; i < listIndex.size(); i++) {
			System.out.println("P = " + proportionValue.get(listIndex.get(i)));
			sum = sum + proportionValue.get(listIndex.get(i));
		}

		float average = (float) sum / perc;
		int P_average_round = Math.round(average); // media delle P dei 4 difetti precedenti
		int P_average_NOround = (int) average;
		System.out.println("P_average = " + P_average_round);
		System.out.println("P_average_NOround = " + P_average_NOround);

		// Predicted IV = FV -(FV - OV) * P
		Integer predicted_IV = fV - (fV - oV) * P_average_round;
		System.out.println("predicted_IV = " + predicted_IV);

		// devo settare IV dei ticket presenti nella listNoIV, ma poi dovro cambiare
		// anche quelli in listaTicket, che e la lista principale
		// da cui prendo informazioni e su cui lavoro sempre
		ticket.setIV(predicted_IV);

		/*
		 * ticket.getAV().clear(); //elimino gli elementi di AV, poiche potrei avere
		 * dati inconsistenti presi da JIRA, e setto manualmente gli altri.
		 * 
		 * for(int k = predicted_IV; k<FV;k++) { ticket.getAV().add(k); }
		 */

		// devo calcolare la media delle P!

		System.out.println("-----------------\n\n");

	}

	// modificando i ticket dentro listNoIV, automaticamente modifico i ticket
	// presenti in listaTicket, quindi ora avro la lista ticket
	// modificata con tutti i valori di IV e i valori di FV e OV coerenti
	// RetrieveTicketsJIRA.printArrayList(listaTicket);

	System.out.println("\n\nTicket con predictedIV = ");
	for (int i = 0; i < listNoIV.size(); i++) {
		/*
		 * System.out.println("L'ID del ticket e = " + listaTicket.get(i).getID());
		 * System.out.println("L'AV del ticket e = " + listaTicket.get(i).getAV());
		 * System.out.println("La data di creazione del ticket e = " +
		 * listaTicket.get(i).getCreationDate());
		 * System.out.println("La data di risoluzione del ticket e = " +
		 * listaTicket.get(i).getResolutionDate()); System.out.println("OV e = " +
		 * listaTicket.get(i).getOV()); System.out.println("FV e = " +
		 * listaTicket.get(i).getFV());
		 */

		System.out.println(listNoIV.get(i).getID() + "\t\t IV: " + listNoIV.get(i).getIV() + "\t\t OV: "
				+ listNoIV.get(i).getOV() + "\t\tFV: " + listNoIV.get(i).getFV() + "\t\t AV: "
				+ listNoIV.get(i).getAV());

		// System.out.println("-----------------\n");

	}

	/*
	 * //assegno correttamente AV ad ogni ticket for(int i = 0; i <
	 * listaTicket.size(); i++) { Ticket ticket = listaTicket.get(i);
	 * ticket.getAV().remove(null); Integer IV = ticket.getIV(); Integer FV =
	 * ticket.getFV();
	 * 
	 * }
	 */

	System.out.println("listaTicket size = " + listaTicket.size());

}
	

	public static void main(String[] args){
			// Do nothing because is a main method
	}
	
	
		
}
