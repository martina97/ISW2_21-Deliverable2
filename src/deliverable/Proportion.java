package deliverable;

import java.util.ArrayList;
import java.util.List;

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
		System.out.println("NUMERO TICKET = " + numTicket);
		perc = numTicket * 1/100; 	// numero dei ticket precedenti di cui calcolare P con moving window
		System.out.println("PERC = " + perc);
		
		List<Ticket> listProp = new ArrayList<>(); // lista che contiene i ticket precedenti (# ticket = perc)
		for (Ticket ticket : ticketList) {
			if( !additionalList.contains(ticket)) {
				if (ticket.getIV() != 0) {	//metto nella lista per proportion i 4 ticket precedenti che hanno IV != 0
					addTicketList(listProp, ticket);
				}
				else {		//il ticket ha IV = 0, quindi calcolo proportion
					System.out.println("ticket = " + ticket.getID() + "\n");
					for (Ticket ticket2 : listProp) {
						System.out.println("ticket precedenti = " + ticket2.getID());
					}
					setIvProp(listProp, ticket);
	
				}
				System.out.println("######\n\n");
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


	public static void main(String[] args) {
		 
		 // main
		 }
}
