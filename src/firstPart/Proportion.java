package firstPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import entities.Ticket;

public class Proportion {

	
	private static int perc; //1% dei ticket da usare per proportion
	
	private Proportion() {}
	
	
	//  ################     PRIMO METODO PER PROPORTION    ######################### 
	
	public static void initialCheck(List<Ticket> ticketList, List<Ticket> additionalList) {
		/*
		 * P = (FV -IV) / (FV -OV)
		 * 
		 * Poich� predicted IV = FV -(FV -OV) * P , se FV = OV --> non serve calcolare P, poiche' predicted IV = FV,
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
		
		float avg = (float)sum/perc;
		int avgRound = Math.round(avg);
		//con avgRound arrotonda in eccesso, se volessi arrotondare in difetto dovrei fare avg_no_round = (int)avg
		int fV = ticket.getFV();
		int oV = ticket.getOV();
		int iV = fV - (fV - oV)*avgRound;
		
		//predicted IV potrebbe essere > OV, in questo caso IV = OV
		if (iV > oV) {
			ticket.setIV(oV);
		}
		else {
			ticket.setIV(iV);
		}
	}
	
	public static int calculateP(Ticket ticket) {
		int fV = ticket.getFV();
		int oV = ticket.getOV();
		int iV = ticket.getIV();
		int pRound=0;
		if (fV != oV) {
			// se FV = OV --> P = 0
			float p = (float) (fV - iV)/ (fV - oV); 
			pRound = Math.round(p);	//arrotonda p in eccesso
			//pRound arrotonda in eccesso, se invece volessi arrotondare in difetto dovrei fare p_noRound = (int)p
		}
		return pRound;
	}
	
	
	public static void proportion(List<Ticket> ticketList) {
		
		List<Ticket> additionalList = new ArrayList<>();	//contiene i ticket di cui effetto il check iniziale FV = OV
		initialCheck(ticketList, additionalList);
		
	
		int numTicket = ticketList.size();
		perc = numTicket * 1/100; 	// numero dei ticket precedenti di cui calcolare P con moving window
		
		List<Ticket> listProp = new ArrayList<>(); // lista che contiene i ticket precedenti (# ticket = perc)
		for (Ticket ticket : ticketList) {
			if( !additionalList.contains(ticket)) {
				if (ticket.getIV() != 0) {	//metto nella lista per proportion i 4 ticket precedenti che hanno IV != 0
					addTicketList(listProp, ticket);
				}
				else {		//il ticket ha IV = 0, quindi calcolo proportion
					
					setIvProp(listProp, ticket);
				}
			}
		}
	}
	
	public static void addTicketList(List<Ticket> listProp, Ticket ticket) {
		
			if (listProp.size() < perc ) {
				listProp.add(ticket);
			}
			else if(listProp.size() >= 4) {
				listProp.remove(0);
				listProp.add(ticket);
			}
	}

	
	
	//  ################     SECONDO METODO PER PROPORTION    ######################### 
	
	public static void checkTicket2(List<Ticket> ticketList) {

		int i;
		
		ArrayList<Ticket> goodTicket = new ArrayList<>();
		ArrayList<Ticket> noIVTicket = new ArrayList<>();


		// analizzo la lista dei ticket presi da jira che hanno AV, IV, OV e FV
		for (i = 0; i < ticketList.size(); i++) {
			firstAnalysis(ticketList, i);
		}

		for (i = 0; i < ticketList.size(); i++) {

			// se FV = OV e non ho AV (IV == 0) , rimuovo il ticket perche nel calcolo del
			// predictedIV con proportion, la cui formula e IV = FV-(FV-OV)*P, il termine a
			// dx fa zero
			// e quindi proportion non l'ho utilizzato

			// IV = 0 se
			// il ticket non ha AV presa da JIRA
			// AV presa da JIRA e inconsistente (IV>=OV o IV>FV)
			if (ticketList.get(i).getIV() == 0 && ticketList.get(i).getFV().equals(ticketList.get(i).getOV())) {
				ticketList.remove(i);
				i--;
			}
		}

		// inverto ordine listaTicket per semplicita
		Collections.reverse(ticketList);

		// ora ho la lista perfetta di ticket, non dovro piu toglierli, quindi associo
		// un indice a ogni ticket cosi da rendere piu semplice
		// il confronto tra le liste
		for (i = 0; i < ticketList.size(); i++) {
			ticketList.get(i).setIndex(i);
		}

		// prendo i ticket che rispettano le condizioni per implementare proportion
		for (i = 0; i < ticketList.size(); i++) {

			Integer fV = ticketList.get(i).getFV();
			Integer oV = ticketList.get(i).getOV();
			Integer iV = ticketList.get(i).getIV();
			

			// IV!=0
			if (iV != 0) {
				// FV!=IV e FV!=OV senno P=0
				if ( !fV.equals(iV) && !fV.equals(oV) && iV <= oV) { // ticket buoni per calcolare proportion
					goodTicket.add(ticketList.get(i));
				} 

			} else { // IV = 0
				// aggiungi alla lista per cui calcolare IV con proportion
				noIVTicket.add(ticketList.get(i));
			}

			// SE HO OV = 1 E NON HO IV, SICURAMENTE, POICHe IV<= OV --> IV = 1 PER FORZA!!!!!!!!!!!!!!

		}

		// chiamo il metodo proportion
		int numTicket = ticketList.size();

		proportion2(goodTicket, noIVTicket, numTicket);

	}
	
	
	public static void firstAnalysis(List<Ticket> ticketList, int i) {

		// se OV = 1 e IV = 0 --> sicuramente IV = 1
		if (ticketList.get(i).getOV() == 1 && ticketList.get(i).getIV() == 0) {
			ticketList.get(i).setIV(1);
			ticketList.get(i).getAV().remove(null);
			ticketList.get(i).getAV().add(1);
		}

		else {

			// se IV>OV o IV>FV , magari sono stati inseriti dati su JIRA
			// relativi
			// alla AV sbagliati (non affidabili), quindi setto IV=0 e lo calcolo tramite
			// proportion
			if (ticketList.get(i).getIV() > ticketList.get(i).getOV() || ticketList.get(i).getIV() > ticketList.get(i).getFV()) {
				ticketList.get(i).setIV(0);
				ticketList.get(i).getAV().clear();
			}
		}
	}
	

	public static void proportion2(List<Ticket> listGood, List<Ticket> listNoIV, int numTicket) {
	
		Integer fV=0;
		Integer iV=0;
		Integer oV=0;
		// uso tree map perche ha i valori di keys in ordine
		TreeMap<Integer, Integer> proportionValue = new TreeMap<>(); // contiene indice del ticket e il valore di P
	
		float percentage = (float) (numTicket * 0.01);
		
	
		int perc = Math.round(percentage); // Math.round() converts a floating-point number to the nearest integer by
											// first adding 0.5 and then truncating value after decimal point
	
		for (int i = 0; i < listGood.size(); i++) {
			 iV = listGood.get(i).getIV();
			 oV = listGood.get(i).getOV();
			 fV = listGood.get(i).getFV();
			String id = listGood.get(i).getID();
			Integer index = listGood.get(i).getIndex();
	
			float proportion = (float) (fV - iV) / (fV - oV);
			Integer p = Math.round(proportion); // ARROTONDO PER ECCESSO
			proportionValue.put(index, p);
		}
	
	
	
		for (int j = 0; j < listNoIV.size(); j++) {
	
			Ticket ticket = listNoIV.get(j);
			String ticketID = ticket.getID();
			Integer index = ticket.getIndex();
			oV = ticket.getOV();
			fV = ticket.getFV();
	
	
			ArrayList<Integer> listIndex = new ArrayList<>();
			// scorro TreeMap
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
	
	
			// somma degli elementi dell'array(le P)
			Integer sum = 0;
	
			for (int i = 0; i < listIndex.size(); i++) {
				sum = sum + proportionValue.get(listIndex.get(i));
			}
	
			float average = (float) sum / perc;
			int pAverageRound = Math.round(average); // media delle P dei 4 difetti precedenti
			//int P_average_NOround = (int) average
	
			// Predicted IV = FV -(FV - OV) * P
			Integer predictedIV = fV - (fV - oV) * pAverageRound;
	
			// devo settare IV dei ticket presenti nella listNoIV, ma poi dovro cambiare
			// anche quelli in listaTicket, che e la lista principale
			// da cui prendo informazioni e su cui lavoro sempre
			ticket.setIV(predictedIV);
	
			// devo calcolare la media delle P!
		
		}
	
		// modificando i ticket dentro listNoIV, automaticamente modifico i ticket
		// presenti in listaTicket, quindi ora avro la lista ticket
		// modificata con tutti i valori di IV e i valori di FV e OV coerenti
	
	}
	
	
	public static void modifyListAV(List<Ticket> listaTicket) {
	
		for (int i = 0; i < listaTicket.size(); i++) {
			Ticket ticket = listaTicket.get(i);
			Integer iV = ticket.getIV();
			Integer fV = ticket.getFV();
	
			ticket.getAV().clear(); // elimino gli elementi di AV, poiche potrei avere dati inconsistenti presi da JIRA, e setto manualmente gli altri.
	
			for (int k = iV; k < fV; k++) {
				ticket.getAV().add(k);
			}
		}
	
	}
	
}
