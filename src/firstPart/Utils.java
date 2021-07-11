package firstPart;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.List;
import java.util.Locale;



public class Utils {
	
	private Utils() {}


	public static double calculateAverage(List<Integer> list) {
		/**
		 * Calcola la media degli elementi nella lista. 
		 * 
		 * @param list	lista di interi
		 * @return		media 
		 */
		Integer sum = 0;
	    if(!list.isEmpty()) {
	    for (Integer mark : list) {
	        sum += mark;
	    }
	    return sum.doubleValue() / list.size();
	  }
	  return sum;
	}
	
	
	public static float calculatePercentage(Integer numerator, Integer total) {
		/*
		 * Calcola il rapporto in percentuale tra numerator e total.
		 */
		return (float)100*numerator/total;
	}
	
	
	
	public static String doubleTransform(Double value) {		
		DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
		return df.format(value);
	}

		
}
