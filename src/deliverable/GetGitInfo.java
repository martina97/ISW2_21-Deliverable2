package deliverable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.LogCommand;
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
import org.json.JSONException;

import entities.JavaFile;
import entities.Ticket;






public class GetGitInfo {
	
	// BOOKKEEPER 
	private static final String REPO = "D:/Programmi/Eclipse/eclipse-workspace/bookkeeper/.git";
	private static Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/bookkeeper");
	
	/* SYNCOPE 
	 * 	private static final String REPO = "D:/Programmi/Eclipse/eclipse-workspace/syncope/.git";
	 *  private static Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/syncope");
	 */
	
	private static Repository repository;
	private static final String FILE_EXTENSION = ".java";
	private static final String RENAME = "RENAME";
	private static final String DELETE = "DELETE";
	private static final String MODIFY = "MODIFY";
	
	
	public static  List<RevCommit> getAllCommit(List<Release> releasesList) throws IllegalStateException, GitAPIException, IOException {
			
		 ArrayList<RevCommit> commitList = new ArrayList<>();
		try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
			 //
		    }
		    InitCommand init = Git.init();
		    init.setDirectory(repoPath.toFile());
		    try (Git git = init.call()) {
		    	//
		    }
		    try (Git git = Git.open(repoPath.toFile())) {
		    	Iterable<RevCommit> logs = git.log().all().call();
		    	 for (RevCommit rev : logs) {
		    		 commitList.add(rev);
		    		 // a ogni commit assegno la release e lo metto nella lista dei commit di quella release 
					   LocalDateTime commitDate = rev.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
					   //LocalDateTime date = Instant.ofEpochSecond(rev.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
					   int releaseCommit = GetJIRAInfo.compareDateVersion(commitDate, releasesList);
					   addListCommitRelease(releaseCommit, releasesList, rev);
		    	 }
		    }
		    return commitList;
		}
	
	
	
	public static void addListCommitRelease(int releaseCommit,List<Release> releasesList, RevCommit commit) {
		/*
		 * Aggiungo il commit nella lista dei commit appartenenti alla release  
		 */
		for (Release release : releasesList ) {
			if (release.getIndex().equals(releaseCommit)) {
					release.getCommitList().add(commit);
			}
		}
	}
	
	
	
	public static void getJavaFiles(Path repoPath, List<Release> releasesList, HashMap<String, List<String>> fileAliasMap) throws IOException, GitAPIException {

		try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
			// ciao 
		}

		InitCommand init = Git.init();
		init.setDirectory(repoPath.toFile());

		try (Git git = Git.open(repoPath.toFile())) {
			for (Release release : releasesList) {
				List<String> fileNameList = new ArrayList<>();
				for (RevCommit commit : release.getCommitList()) {
	
					ObjectId treeId = commit.getTree();
		
					try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
						treeWalk.reset(treeId);
						treeWalk.setRecursive(true);
		
						while (treeWalk.next()) {
							addJavaFile2(treeWalk,release, fileNameList, fileAliasMap);
						}
					
				} catch (IOException e) {
					//Log.errorLog("Errore nel prendere i file java associati al commit");
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					//Log.errorLog(sw.toString());
				}
				//Log.infoLog("\n\nIl numero di file .java relativi alla release e: " + filePath.size());
	
				// per ogni release mi devo prendere tutti i file, cerco quel file se e nel
				// commit e mi prendo le differenze
	
				}
			}
		}
		
		for (int k = 0; k<releasesList.size(); k++) {
			if(releasesList.get(k).getFileList().isEmpty()) {
				releasesList.get(k).setFileList(releasesList.get(k-1).getFileList());
			}
		}

	}
	
	public static void addJavaFile(TreeWalk treeWalk, Release release, List<String> fileNameList,  HashMap<String, List<String>> fileAliasMap) {
		/* 
		 * Aggiungo il file Java nella lista di file della release, 
		 * e inizialmente setto buggyness = "no"
		 */
		if (treeWalk.getPathString().endsWith(FILE_EXTENSION) ) {
			String nameFile = treeWalk.getPathString();
			
			//controllo se nameFile e' gia' nella lista fileName oppure se e' un alias di un altro file presente
			// nella lista fileName 
			// devo controllare se fileName contiene gia' uno degli alias del file 
			if (!checkAlias(nameFile, fileNameList,fileAliasMap ) && !fileNameList.contains(nameFile)) {
				fileNameList.add(nameFile);
				JavaFile file = new JavaFile(nameFile);
				file.setBugg("No");
				file.setNr(0);
				file.setNAuth(new ArrayList<>());
				release.getFileList().add(file);
			}
		}
	}	  
	
	public static boolean checkAlias(String nameFile,List<String> fileNameList, HashMap<String, List<String>> fileAliasMap) {
		
		// devo controllare se fileNameList contiene nameFile oppure un alias di nameFile 
		// per prima cosa vedo se in fileAliasMap c'e il file, se c'e mi salvo tutti gli alias in una lista (compreso nameFile)
		// e poi controllo se fileNameList contiene gia' uno di questi 
		
		List<String> listAlias = new ArrayList<>();
		//System.out.println("############################# checkAlias ########################\n\n");
		for (Entry<String, List<String>> entry : fileAliasMap.entrySet()) {
		    String key = entry.getKey();
		    List<String> oldPaths = entry.getValue();
		    if (nameFile.equals(key) || oldPaths.contains(nameFile)) {
		    	for (String file : oldPaths) {
		    		listAlias.add(file);
		    	}
		    	listAlias.add(nameFile);
		    	listAlias.add(key);
		    	listAlias = listAlias.stream().distinct().collect(Collectors.toList());
		    }
	 }
		
	// ora devo vedere se fileNameList contiene gia' un file contenuto in listAlias 
	// se si --> non aggiungo il file in fileName
	// se no --> lo aggiungo
	for (String file : listAlias) {
		if (fileNameList.contains(file)) {

			return true;
		}
	}
	/*
	if(fileNameList.contains(nameFile)) {
		return true;
	}
	*/
	return false;
	}
	
	public static void addJavaFile2(TreeWalk treeWalk, Release release, List<String> fileNameList,  HashMap<String, List<String>> fileAliasMap) {
		/* 
		 * Aggiungo il file Java nella lista di file della release, 
		 * e inizialmente setto buggyness = "no"
		 */
		if (treeWalk.getPathString().endsWith(FILE_EXTENSION) ) {
			String nameFile = treeWalk.getPathString();
			
			//controllo se nameFile e' gia' nella lista fileName oppure se e' un alias di un altro file presente
			// nella lista fileName 
			// devo controllare se fileName contiene gia' uno degli alias del file 
			JavaFile file = new JavaFile(nameFile);

			if (!checkAlias2(nameFile, fileNameList,fileAliasMap, file ) && !fileNameList.contains(nameFile)) {
				fileNameList.add(nameFile);
				//JavaFile file = new JavaFile(nameFile);
				file.setBugg("No");
				file.setNr(0);
				file.setNAuth(new ArrayList<>());
				file.setChgSetSize(0);
				//System.out.println("FILE == " + file.getName() + "\nLIST ALIAS == " + file.getoldPaths());
				
				release.getFileList().add(file);
			}
		}
	}	  
	
	public static boolean checkAlias2(String nameFile,List<String> fileNameList, HashMap<String, List<String>> fileAliasMap, JavaFile file) {
		
		// devo controllare se fileNameList contiene nameFile oppure un alias di nameFile 
		// per prima cosa vedo se in fileAliasMap c'e il file, se c'e mi salvo tutti gli alias in una lista (compreso nameFile)
		// e poi controllo se fileNameList contiene gia' uno di questi 
		
		List<String> listAlias = new ArrayList<>();
		//System.out.println("############################# checkAlias ########################\n\n");
		for (Entry<String, List<String>> entry : fileAliasMap.entrySet()) {
		    String key = entry.getKey();
		    List<String> oldPaths = entry.getValue();
		    if (nameFile.equals(key) || oldPaths.contains(nameFile)) {
		    	for (String oldPath : oldPaths) {
		    		listAlias.add(oldPath);
		    	}
		    	listAlias.add(nameFile);
		    	listAlias.add(key);
		    	listAlias = listAlias.stream().distinct().collect(Collectors.toList());
		    	addOldPaths(file, listAlias);
		    	//file.setOldPaths(listAlias);
		    }
	 }
		
	// ora devo vedere se fileNameList contiene gia' un file contenuto in listAlias 
	// se si --> non aggiungo il file in fileName
	// se no --> lo aggiungo
	for (String oldPath : listAlias) {
		if (fileNameList.contains(oldPath)) {

			return true;
		}
	}
	/*
	if(fileNameList.contains(nameFile)) {
		return true;
	}
	*/
	return false;
	}
	
	public static void addOldPaths(JavaFile file, List<String> listAlias) {
		
		// rimuovo da listAlias il nome originale del file, ossia "file"
		Iterator<String> i = listAlias.iterator();
		while (i.hasNext()) {
		
		   String s = i.next(); // must be called before you can call i.remove()
		   if(s.equals(file.getName())) {
			   i.remove();

		   }
		   // Do something
		}
		file.setOldPaths(listAlias);

	}
	
	 public static HashMap<String, List<String>> checkRename(List<Release> releasesList ) throws IOException {
		 /*
		  * Nella lista dei file in ogni release potrebbero esserci delle classi che sono state rinominate, sia tra una release
		  * e l'altra, sia nella stessa release, quindi devo gestire i file in una stessa release che hanno nomi diversi ma sono gli stessi, 
		  * sia file tra una release e l'altra 
		  * alla fine mettero' in ogni release il nome finale che avra' la classe
		  * IDEA: scorro tutti i commit in ogni release e mi prendo i file rinominati, e a ogni file mi vado a mettere una lista
		  * di file alias, ossia file che hanno nomi diversi ma sono stessi file 
		  */
		 HashMap<String, List<String>> fileAliasMap = new HashMap<>();
		 /* Lavoro con hashMap, dove:
		  * key = new Path , value = lista old path 
		  * vedo old path, e vedo:
		  * 	se old path è una key della mappa, lo metto nella lista di old path e metto come nuova key il new path
		  * 	se old path sta nella lista di old path, vedo se la key = new path
		  * 		se si --> vado avanti
		  * 		altrimenti --> metto come key il new path e metto la vecchia key nella lista 
		  */
		 
		 FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
			repository = repositoryBuilder.setGitDir(new File(REPO)).readEnvironment() // scan environment GIT_* variables
					.findGitDir() // scan up the file system tree
					.setMustExist(true).build();
		  for (Release release : releasesList) {
			  int count = 0;
			  //System.out.println("\n\nRELEASE " + release.getIndex());
			  for (RevCommit commit : release.getCommitList()) {
					List<DiffEntry> diffs = getDiffs(commit);
					if (diffs != null) {
						for (DiffEntry diff : diffs) {
							String type = diff.getChangeType().toString();
							//System.out.println("TYPE = " + type);

							String oldPath = diff.getOldPath();
							//System.out.println("oldPath = " + oldPath);

							String newPath = diff.getNewPath();
							//System.out.println("newPath = " + newPath);
							
							
							if (type.equals(RENAME) && oldPath.endsWith(FILE_EXTENSION)) {
								count++;
								populateMapAlias(newPath, oldPath, fileAliasMap);
							}
							
						}
					}
			  	}
			  }
		  
		  
		  //rimuovo duplicati nella lista degli oldPath
		  for (int i = 0; i<fileAliasMap.size(); i++) {
				 Object key = fileAliasMap.keySet().toArray()[i];
				 List<String> oldPaths = fileAliasMap.get(key);
				 fileAliasMap.get(key).stream().distinct().collect(Collectors.toList());
		  }
		  
		  return fileAliasMap;
		  
	  }
	  

	 

	 public static void populateMapAlias(String newPath, String oldPath, HashMap<String, List<String>> map) {
		 // i commit stanno nella lista con data in ordine crescente, quindi ogni file rinominato o non è mai 
		 //stato rinominato in passato, oppure è stato rinominato quindi key della mappa = oldPath
			if(map.isEmpty()) {
				List<String> oldPaths = new ArrayList<>();
				oldPaths.add(oldPath);
				map.put(newPath,oldPaths);

			}
			else {
				/*scorro la lista:
				 * 	se oldPath è una key, metto key = newPath e oldPath lo metto nella lista di oldPaths --> break 
				 *  altrimenti aggiungo alla mappa la coppia oldPath-newPath
				 */
				for(int i = 0 ; i<map.size();i++) {
					String key = (String) map.keySet().toArray()[i];
					//System.out.println("KEY == " + key);

					List<String> oldPaths = map.get(key);
					if (key.equals(oldPath)) {
						//System.out.println("KEY == NEW PATH");
						//System.out.println("KEY == " + key);
						//System.out.println("oldPath == " + oldPath);


						String newKey = newPath;
					    oldPaths.add(oldPath);
					    map.remove(key);
					    map.put(newKey, oldPaths);
					    break;
					}
					else {
						List<String> oldPaths2 = new ArrayList<>();
						oldPaths2.add(oldPath);
					    map.put(newPath, oldPaths2);

					}
					
				}
			}
			
		}
	 
	 
	 public static void checkBuggyness(List<Release> releasesList, List<Ticket> ticketList, HashMap<String, List<String>> fileAliasMap) throws IOException {
		 
		 /* per ogni ticket mi prendo la lista dei commit che contengono l'id del ticket,
		  * mi prendo i file .java DELETE e MODIFY nel commit
		  * vedo a che release appartiene il commit e vedo se essa è contenuta nelle AV del ticket
		  * 	se si --> setto buggyness = "si" per i file java
		  */
		 
		 for (Ticket ticket : ticketList) {
			 List<Integer> aV = ticket.getAV();
			 for (RevCommit commit : ticket.getCommitList()) {
				 LocalDateTime commitDate = commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				 //System.out.println("COMMIT DATE === " + commitDate);
				 //LocalDateTime date = Instant.ofEpochSecond(commit.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
				 int releaseCommit = GetJIRAInfo.compareDateVersion(commitDate, releasesList);
				 //System.out.println("LA RELEASE DEL COMMIT E' == " + releaseCommit);
				 List<DiffEntry> diffs = getDiffs(commit);
					if (diffs != null) {
						analyzeDiffEntryBugg(diffs, releasesList, releaseCommit, aV, fileAliasMap);
					}
			 	}
		 	}
		 }
	 
	 
	 public static void analyzeDiffEntryBugg(List<DiffEntry> diffs, List<Release> releasesList, int releaseCommit, List<Integer> aV, HashMap<String, List<String>>  fileAliasMap) {
		 for (DiffEntry diff : diffs) {
				String type = diff.getChangeType().toString();
				//System.out.println("TYPE === " + type);
				
				if (diff.toString().contains(FILE_EXTENSION) && (type.equals(MODIFY)
						|| type.equals(DELETE) )) { 
				
					// vedo se releaseCommit e' contenuta nella AV del ticket, se si setto
					// come buggy il file nella relativa release
					checkFileBugg(diff, releasesList, releaseCommit, aV, fileAliasMap);
				}
			}
	 }

	 public static void checkFileBugg(DiffEntry diff, List<Release> releasesList, int releaseCommit, List<Integer> aV, HashMap<String, List<String>> fileAliasMap) {
		 
		 /* se AV e' vuota, allora non faccio niente (il file ha gia' buggyness "no")
		  * se AV non e' vuota --> prendo il file e lo setto buggy o no se la release del 
		  * commit appartiene alle AV del ticket, in particolare prendo il nome del file, prendo la release
		  * da releasesList, e lo setto buggy 
		  */
		 String file;
		 if (diff.getChangeType() == DiffEntry.ChangeType.DELETE ||diff.getChangeType() == DiffEntry.ChangeType.RENAME ) {
			 file = diff.getOldPath();
		 }
		 else {
			 file = diff.getNewPath();
		 }
		 //Release release = releasesList.get(releaseCommit-1); // la release a cui appartiene il commit
		 /* scorro i file java nella lista della release, e se trovo un file java che ha il nome
		  * del file trovato, setto il file buggy secondo le AV
		  */
		 for (Release release : releasesList) {
			 for (JavaFile javaFile : release.getFileList()){ 
				 if(javaFile.getName().equals(file) || checkMapRename(javaFile.getName(), fileAliasMap)) {
					 //System.out.println("FILE TROVATO");
					 compareReleaseAV(javaFile, aV, release);
				 }
			 }
		 }
	 }
	 

	 public static void getMetrics(List<Release> releasesList, HashMap<String, List<String>> fileAliasMap) throws IOException {
		 
		 for (Release release : releasesList ) {
			 //Release release = releasesList.get(0);
			 //System.out.println("RELEASE == " + release.getIndex());
			 
			 /* creo hashMap che ha come 
			  * key --> nome file
			  * value --> HashMap con key = NR , value = lista autori 
			  */
			 List<JavaFile> fileList = new ArrayList<>();	//lista che contiene i nomi dei file dentro diffs dei commit per ogni release 
			 for (RevCommit commit : release.getCommitList()) {
				 String authName = commit.getAuthorIdent().getName();
				 List<DiffEntry> diffs = getDiffs(commit);
				 if (diffs != null) {
					analyzeDiffEntryMetrics(diffs, fileList, authName);
				 }
			 }
			 System.out.println("###\n\n");
			 setFileRelease(fileList,  release);
		 }
	}
	 
	 public static void analyzeDiffEntryMetrics(List<DiffEntry> diffs, List<JavaFile> fileList, String authName) {
		 	int numDiff = 0 ; 
		 	for (DiffEntry diffEntry : diffs) {
				if (diffEntry.toString().contains(FILE_EXTENSION)) { 
					numDiff++;
				}
		 	}

			for (DiffEntry diff : diffs) {
				String type = diff.getChangeType().toString();
			
				if (diff.toString().contains(FILE_EXTENSION) && (type.equals(MODIFY) || type.equals(DELETE)  || type.equals("ADD")||type.equals(RENAME) )) { 
					String file;
					
					if (type.equals(DELETE) || type.equals(RENAME) ) {
						 file = diff.getOldPath();
					 }
					 else {
						 file = diff.getNewPath();
					 }
					//System.out.println("FILE == " + file);
					
					addFileList(fileList, file, authName, numDiff);
					//System.out.println("######\n\n");

				}
			}
		 }
	 
	 public static void addFileList(List<JavaFile> fileList, String fileName, String authName, int numDiff) {
		 int count = 0 ; 
		 if (fileList.isEmpty()) {
			 //System.out.println("LISTA VUOTA");
			 JavaFile javaFile = new JavaFile(fileName);
			 javaFile.setNr(1);
			 List<String> listAuth = new ArrayList<>();
			 listAuth.add(authName);
			 javaFile.setNAuth(listAuth);
			 javaFile.setChgSetSize(numDiff);
			 //javaFile.getNAuth().add(authName);
			 fileList.add(javaFile);
			 count = 1;
		}
		 else {
			 for ( JavaFile file : fileList) {
				 if (file.getName().equals(fileName)) {
					 //System.out.println("FILE PRESENTE NELLA LISTA ");

					 file.setNr(file.getNr()+1);
					 file.getNAuth().add(authName);
					 file.setChgSetSize(file.getChgSetSize()+ numDiff);
					 count =1;
				 }
			 }
		 }
		 
		 if (count == 0) { //vuol dire che il nome del file non e' presente in fileList, quindi lo aggiungo
			 //System.out.println("FILE NON PRESENTE NELLA LISTA ");

			 JavaFile javaFile = new JavaFile(fileName);
			 javaFile.setNr(1);
			 List<String> listAuth = new ArrayList<>();
			 listAuth.add(authName);
			 javaFile.setNAuth(listAuth);
			 javaFile.getNAuth().add(authName);
			 javaFile.setChgSetSize(numDiff);
			 fileList.add(javaFile);
		 }
	 }

	 
	 public static void setFileRelease(List<JavaFile> fileList, Release release) {
		 for (JavaFile javaFile : fileList) {
			 //System.out.println("javaFile == " + javaFile.getName());
			 List<String> nAuth = javaFile.getNAuth();
			 //System.out.println("javaFile --> \tnR == " + javaFile.getNr() + "\tnAuth == " + nAuth.size());

			 for (JavaFile fileRel : release.getFileList()) {
				 if (javaFile.getName().equals(fileRel.getName())) {
					 //System.out.println("IL NOME DEL FILE STA NELLA RELEASE ");
					 //System.out.println("fileRel --> \tnR == " + fileRel.getNr() + "\tnAuth == " + fileRel.getNAuth().size());
					 fileRel.setNr(fileRel.getNr() + javaFile.getNr());
					 List<String> listAuth = fileRel.getNAuth();
					 listAuth.addAll(nAuth);
					 listAuth = listAuth.stream().distinct().collect(Collectors.toList());
					 fileRel.setNAuth(listAuth);
					 fileRel.setChgSetSize(fileRel.getChgSetSize()+javaFile.getChgSetSize());
					 //System.out.println("fileRel --> \tnR == " + fileRel.getNr() + "\tnAuth == " + fileRel.getNAuth().size() +"\n\n");
					 
				 }
				 
				 if(fileRel.getoldPaths()!=null && fileRel.getoldPaths().contains(javaFile.getName())) {
					 //System.out.println("IL NOME DEL FILE STA NEGLI ALIAS ");
					 //System.out.println("fileRel --> \tnR == " + fileRel.getNr() + "\tnAuth == " + fileRel.getNAuth().size());

					 fileRel.setNr(fileRel.getNr() + javaFile.getNr());
					 List<String> listAuth = fileRel.getNAuth();
					 listAuth.addAll(nAuth);
					 listAuth = listAuth.stream().distinct().collect(Collectors.toList());
					 fileRel.setNAuth(listAuth);
					 fileRel.setChgSetSize(fileRel.getChgSetSize() + javaFile.getChgSetSize());
					 //System.out.println("fileRel --> \tnR == " + fileRel.getNr() + "\tnAuth == " + fileRel.getNAuth().size());

				 }
			 }
			 //System.out.println("#####################\n\n");

		 }
	 }

	 
	 public static boolean checkMapRename(String nameFile,HashMap<String, List<String>> fileAliasMap ) {
		 //System.out.println("############## checkMapRename ############## ");
		 for (Entry<String, List<String>> entry : fileAliasMap.entrySet()) {
			    String key = entry.getKey();
			    List<String> oldPaths = entry.getValue();
			    if (nameFile.equals(key) || oldPaths.contains(nameFile)) {
					//System.out.println("il file sta negli alias ");
					 return true;
			    }
		 }
		 return false;
	 }
	 
	 public static void compareReleaseAV(JavaFile javaFile, List<Integer> aV, Release release) {
		 //System.out.println("RELEASE COMMIT == " + release.getIndex() + "\tAV == " + aV);
		 if (aV.contains(release.getIndex())) {
			 //System.out.println("LA CLASSE E' BUGGY\n###\n");
			 javaFile.setBugg("Yes");
		 }
	 }
	 
	 
	 
	 public static List<DiffEntry> getDiffs(RevCommit commit) throws IOException {
			List<DiffEntry> diffs;
			DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
			df.setRepository(repository);
			df.setDiffComparator(RawTextComparator.DEFAULT);
			df.setContext(0);
			df.setDetectRenames(true);
			if (commit.getParentCount() != 0) {
				RevCommit parent = (RevCommit) commit.getParent(0).getId();
				diffs = df.scan(parent.getTree(), commit.getTree());
			} else {
				RevWalk rw = new RevWalk(repository);
				ObjectReader reader = rw.getObjectReader();
				diffs = df.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, reader, commit.getTree()));
			}
			return diffs;

	 }
	 
	 public static void main(String[] args) throws IOException, JSONException {
			// Do nothing because is a main method
	
		}

	 
}
