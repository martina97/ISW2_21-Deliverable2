package deliverable;


import java.util.SortedMap;


public class Utils {
	
	
	public static void printTreeMap(SortedMap<Object,Object> map) {
		  map.forEach((key, value) -> System.out.println(key + "= " + value + "\n\n"));
	}

	public static void main(String[] args){
			// Do nothing because is a main method
	}
}
