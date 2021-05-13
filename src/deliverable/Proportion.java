package deliverable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import entities.Ticket;

public class Proportion {

	
	private static int perc; //1% dei ticket da usare per proportion
	
	public static void initialCheck(List<Ticket> ticketList, List<Ticket> additionalList) {
		/*
		 * P = (FV -IV) / (FV -OV)
		 * 
		 * Poichè predicted IV = FV -(FV -OV) * P , se FV = OV --> non serve calcolare P, poiche' predicted IV = FV,
		 * essendo FV - OV = 0
		 */
		for (Ticket ticket : ticketList) {
			if(ticket.getOV().equals(ticket.getFV()) && ticket.getIV() == 0) {
				ticket.setIV(ticket.getFV());
				additionalList.add(ticket);
			}
		}
	}
	
	public static void setIvProp(List<Ticket> listProp, Ticket ticket) {
		
		List<Integer> listP = new ArrayList<>(); //lista delle P dei a ticket precedenti al ticket passato in input 
		int p;
		int sum = 0;
		
		for (Ticket t : listProp) {
			p = calculateP(t);
			listP.add(p);
			sum = sum + p;
		}
		
		System.out.println("LISTA P = " + listP);
		System.out.println("SOMMA P = " + sum);
		
		float avg = (float)sum/perc;
		int avg_round = Math.round(avg);
		int avg_no_round = (int)avg;
		System.out.println("MEDIA P = " + avg + "\tavg_round = " + avg_round + "\tavg_no_round = " + avg_no_round);
		int fV = ticket.getFV();
		int oV = ticket.getOV();
		int iV = fV - (fV - oV)*avg_round;
		System.out.println("PREDICTED IV  = " + iV);
		System.out.println("FV  = " + fV);
		System.out.println("OV  = " + oV);
		
		//predicted IV potrebbe essere > OV, in questo caso IV = OV
		if (iV > oV) {
			ticket.setIV(oV);
		}
		else {
			ticket.setIV(iV);
		}
		System.out.println("\n\nIV  = " + ticket.getIV());
		System.out.println("FV  = " + fV);
		System.out.println("OV  = " + oV);
		
		
	}
	
	public static int calculateP(Ticket ticket) {
		int fV = ticket.getFV();
		int oV = ticket.getOV();
		int iV = ticket.getIV();
		int p_round=0;
		int p_noRound =0;
		if (fV != oV) {
			// se FV = OV --> P = 0
			float p = (float) (fV - iV)/ (fV - oV); 
			p_round = Math.round(p);	//arrotonda p in eccesso
			p_noRound = (int)p;		// arrotonda in difetto
			System.out.println("p_round==== " + p_round);
			System.out.println("p_noRound==== " + p_noRound);

		}
		return p_round;
	}
	
	
	public static void proportion(List<Ticket> ticketList) {
		
		List<Ticket> additionalList = new ArrayList<>();	//contiene i ticket di cui effetto il check iniziale FV = OV
		initialCheck(ticketList, additionalList);
		
	
		int numTicket = ticketList.size();
		//System.out.println("NUMERO TICKET = " + numTicket);
		perc = numTicket * 1/100; 	// numero dei ticket precedenti di cui calcolare P con moving window
		//System.out.println("PERC = " + perc);
		
		List<Ticket> listProp = new ArrayList<>(); // lista che contiene i ticket precedenti (# ticket = perc)
		for (Ticket ticket : ticketList) {
			if( !additionalList.contains(ticket)) {
				if (ticket.getIV() != 0) {	//metto nella lista per proportion i 4 ticket precedenti che hanno IV != 0
					addTicketList(listProp, ticket);
				}
				else {		//il ticket ha IV = 0, quindi calcolo proportion
					//System.out.println("ticket = " + ticket.getID() + "\n");
					for (Ticket ticket2 : listProp) {
						//System.out.println("ticket precedenti = " + ticket2.getID());
					}
					setIvProp(listProp, ticket);

	
				}
				//System.out.println("######\n\n");
			}

		}
		
	}
	
	public static void addTicketList(List<Ticket> listProp, Ticket ticket) {
		
			if (listProp.size() < perc ) {
				//System.out.println("SIZE < 4 " + " TICKET = " + ticket.getID());
				listProp.add(ticket);
			}
			else if(listProp.size() >= 4) {
				//System.out.println("SIZE > 4 " + " TICKET = " + ticket.getID());

				listProp.remove(0);
				listProp.add(ticket);
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

			

			if (creationDate.isAfter(resolutionDate)) {
				//Log.infoLog(listaTicket.get(i).getID() + "ERRORE: creationDate is after resolutionDate");
			}

			// SE HO OV = 1 E NON HO IV, SICURAMENTE, POICHe IV<= OV --> IV = 1 PER
			// FORZA!!!!!!!!!!!!!!

		}

		// GetJIRAInfo.printArrayList(goodTicket)

		//System.out.println("\n\nnoIVTicket.size  = " + noIVTicket.size())
		// GetJIRAInfo.printArrayList(noIVTicket)

		// chiamo il metodo proportion
		System.out.println("\n\n------------------PROPORTION----------------------\n");
		int numTicket = listaTicket.size();

		proportion2(goodTicket, noIVTicket, numTicket);

	}

public static void proportion2(List<Ticket> listGood, List<Ticket> listNoIV, int numTicket) {

	Integer fV=0;
	Integer iV=0;
	Integer oV=0;
	// uso tree map perche ha i valori di keys in ordine
	TreeMap<Integer, Integer> proportionValue = new TreeMap<>(); // contiene indice del ticket e il valore di P

	
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

	//System.out.println("listaTicket size = " + listaTicket.size());

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

	//System.out.println("listaTicket size = " + listaTicket.size());

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
	//System.out.println("listaTicket size = " + listaTicket.size());

}
	public static void main(String[] args) {
		 
		 // main
		 }
}
