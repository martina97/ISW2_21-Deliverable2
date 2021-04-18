package deliverable;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import entities.Ticket;

import org.json.JSONArray;

public class GetJIRAInfo {
	
	private static Map<LocalDateTime, String> releaseNames;
	private static Map<LocalDateTime, String> releaseID;
	private static List<LocalDateTime> releases;

	static Logger logger = Logger.getLogger(GetJIRAInfo.class.getName());


   private static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }


   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (

			// BufferedReader rd = new BufferedReader(new InputStreamReader(is,
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8.name()))) {

			String jsonText = readAll(rd);
			return (new JSONObject(jsonText));
		} finally {
			is.close();
		}
	}
   
   
   public static List<Release> getListRelease(String projName) throws IOException, JSONException {


		ArrayList<Release> releaseList = new ArrayList<>();

		// Fills the arraylist with releases dates and orders them
		// Ignores releases with missing dates
		releases = new ArrayList<>();
		Integer i;
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		JSONObject json = readJsonFromUrl(url);
		JSONArray versions = json.getJSONArray("versions");
		releaseNames = new HashMap<>();
		releaseID = new HashMap<>();
		for (i = 0; i < versions.length(); i++) {
			String name = "";
			String id = "";
			if (versions.getJSONObject(i).has("releaseDate")) {
				if (versions.getJSONObject(i).has("name"))
					name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id"))
					id = versions.getJSONObject(i).get("id").toString();
				addRelease(versions.getJSONObject(i).get("releaseDate").toString(), name, id);

			}
		}

		// order releases by date
		Collections.sort(releases, (o1, o2) -> o1.compareTo(o2));

		if (releases.size() < 6)
			return releaseList;
		String pathname = "D:\\" + "Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\Releases.csv";
		try (FileWriter fileWriter = new FileWriter(pathname)) {

			fileWriter.append("Index;Version ID;Version Name;Date");
			fileWriter.append("\n");
			for (i = 0; i < releases.size(); i++) {
				Integer index = i + 1;
				fileWriter.append(index.toString());
				fileWriter.append(";");
				fileWriter.append(releaseID.get(releases.get(i)));
				fileWriter.append(";");
				fileWriter.append(releaseNames.get(releases.get(i)));
				fileWriter.append(";");
				fileWriter.append(releases.get(i).toString());
				fileWriter.append("\n");
			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
		}

		// ordina la mappa con date crescenti
		Map<LocalDateTime, String> map = new TreeMap<>(releaseNames);

		Integer index = 1;

		for (Map.Entry<LocalDateTime, String> entry : map.entrySet()) {
			// la chiave e la data della release
			LocalDateTime key = LocalDateTime.parse(entry.getKey().toString());

			// il valore e il numero della release
			String value = entry.getValue();
			Release release = new Release(index, key, value);
			releaseList.add(release);
			index++;
		}
		return releaseList;
	}
   
   
   public static void addRelease(String strDate, String name, String id) {
	   /**
	    * Aggiunge a releases le release aventi nome,data,id 
	    * @param strDate: la data della release 
	    * @param name: nome release
	    * @param id: indice release 
	    * @return: none
	    */
	   
		LocalDate date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();

		if (!releases.contains(dateTime))
			releases.add(dateTime);
		releaseNames.put(dateTime, name);

		releaseID.put(dateTime, id);
	}
   
   

   

   
	//ritorna la lista di ticket con le corrispondenti resolutionDate e creationDate
	 public static List<Ticket> retrieveTickets2(String projName, List<Release> releases ) {
		  
		   Integer j = 0;
		   Integer i = 0;
		   Integer total = 1;
		   Integer myYear; 
		   TreeMap<Month, ArrayList<String>> ticketMonthMap = new TreeMap<>();
		   JSONArray issues ;
		 /// RITORNA UNA LISTA DI TICKET
		 ArrayList<Ticket> ticketList = new ArrayList<>();
	     //Get JSON API for closed bugs w/ AV in the project
	     do {
	        //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
	        j = i + 1000;
	        String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	               + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
	               + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,affectedVersion,versions,created&startAt="
	               + i.toString() + "&maxResults=" + j.toString();
	        try 
	        {
	        JSONObject json = readJsonFromUrl(url);
	        
	        issues = json.getJSONArray("issues");
	        total = json.getInt("total");
	        for (; i < total && i < j; i++) {
	           //Iterate through each bug
	           String key = issues.getJSONObject(i%1000).get("key").toString();
	           LocalDateTime creationDate= LocalDateTime.parse(issues.getJSONObject(i%1000).getJSONObject("fields").getString("created").substring(0,16));
	           
	           //System.out.println(issues.getJSONObject(i%1000));
	           
	           JSONArray versions = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("versions");
	           List<Integer> listAV = getAVList(versions, releases);
	           Ticket ticket = new Ticket(key, creationDate, listAV);
	           if (listAV.get(0) != null) {
					ticket.setIV(listAV.get(0));
				} else {
					ticket.setIV(0);
				}
	           
	           ticket.setOV(compareDateVersion(creationDate, releases));
	           
	           
	           ticketList.add(ticket);
	        
	        }
	
	        
	     }
		
			catch (JSONException e) 
			{
				System.out.println("Error during JSON document analysis.");
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				System.out.println("Error reading JSON file.");
				e.printStackTrace();
			}
			} 
	     	while (i < total);  
	          
	     
	     return ticketList;
	  }
  
 
	 public static Integer compareDateVersion(LocalDateTime date, List<Release> releases) {
		 
		 Integer releaseIndex =0;
		 for (int k = 0; k<releases.size(); k++) {
			 if (date.isBefore(releases.get(k).getDate())) {
				 releaseIndex = releases.get(k).getIndex();
				 break;
			 }
			 
			 if(date.isAfter(releases.get(releases.size()-1).getDate())) {
				 releaseIndex = releases.get(releases.size()-1).getIndex();
			 }
		 }
		 return releaseIndex;
	 }
	 
	  
	  public static List<Integer> getAVList(JSONArray versions, List<Release> releases) throws JSONException {
	
			ArrayList<Integer> listaAV = new ArrayList<>();
	
			if (versions.length() == 0) {
				listaAV.add(null);
	
			} else {
				for (int k = 0; k < versions.length(); k++) {
					String affectedVersion = versions.getJSONObject(k).getString("name");
					for (int g = 0; g < releases.size(); g++) {
						if (affectedVersion.equals(releases.get(g).getRelease())) {
							listaAV.add(releases.get(g).getIndex());
						}
					}
	
				}
			}
			return listaAV;
		}
	  
	  
	  public static void main(String[] args) throws IOException, JSONException {
			// Do nothing because is a main method
	
		}
 
}
