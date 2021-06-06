package deliverable;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import entities.Release;
import entities.Ticket;



public class GetGitInfo {
	
	private GetGitInfo() {}
	
	private static Logger logger = Logger.getLogger(GetGitInfo.class.getName());
	private static Repository repository;
	private static final String FILE_EXTENSION = ".java";
	private static final String RENAME = "RENAME";
	private static final String DELETE = "DELETE";
	private static final String MODIFY = "MODIFY";
	
	
	public static  List<RevCommit> getAllCommit(List<Release> releasesList, Path repoPath) throws IllegalStateException, GitAPIException, IOException {
			
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
	
	
	
	public static void getJavaFiles(Path repoPath, List<Release> releasesList, Map<String, List<String>> fileAliasMap) throws IOException, GitAPIException {

		try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
			// null
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
							addJavaFile(treeWalk,release, fileNameList, fileAliasMap);
						}
					
				} catch (IOException e) {
					logger.log(Level.SEVERE,"Errore nel prendere i file java associati al commit");
					System.exit(1);
				}	
			
				}
			}
		}
		
		for (int k = 0; k<releasesList.size(); k++) {
			if(releasesList.get(k).getFileList().isEmpty()) {
				releasesList.get(k).setFileList(releasesList.get(k-1).getFileList());
			}
		}

	}
	
	
	public static void addJavaFile(TreeWalk treeWalk, Release release, List<String> fileNameList,  Map<String, List<String>> fileAliasMap) {
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

			if (!checkAlias(nameFile, fileNameList,fileAliasMap, file ) && !fileNameList.contains(nameFile)) {
				fileNameList.add(nameFile);
				file.setBugg("No");
				file.setNr(0);
				file.setNAuth(new ArrayList<>());
				file.setChgSetSize(0);
				file.setChgSetSizeList(new ArrayList<>());
				file.setLOCadded(0);
				file.setLocAddedList(new ArrayList<>());
				file.setChurn(0);
				file.setChurnList(new ArrayList<>());

				try {
					file.setSize(Metrics.loc(treeWalk, repository));
				} catch (IOException e) {
					e.printStackTrace();
				}				
				release.getFileList().add(file);
			}
		}
	}	  
	
	public static boolean checkAlias(String nameFile,List<String> fileNameList, Map<String, List<String>> fileAliasMap, JavaFile file) {
		
		// devo controllare se fileNameList contiene nameFile oppure un alias di nameFile 
		// per prima cosa vedo se in fileAliasMap c'e il file, se c'e mi salvo tutti gli alias in una lista (compreso nameFile)
		// e poi controllo se fileNameList contiene gia' uno di questi 
		
		List<String> listAlias = new ArrayList<>();
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
	
	 
	  
	 public static Map<String, List<String>> checkRename(List<Release> releasesList, String repo ) throws IOException {
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
			repository = repositoryBuilder.setGitDir(new File(repo)).readEnvironment() // scan environment GIT_* variables
					.findGitDir() // scan up the file system tree
					.setMustExist(true).build();
		  for (Release release : releasesList) {
			  for (RevCommit commit : release.getCommitList()) {
				  analyzeDiffRename(commit, fileAliasMap);
					
			  	}
			  }
		  
		  //rimuovo duplicati nella lista degli oldPath
		  for (int i = 0; i<fileAliasMap.size(); i++) {
				 Object key = fileAliasMap.keySet().toArray()[i];
				 fileAliasMap.get(key).stream().distinct().collect(Collectors.toList());
		  }
		  
		  return fileAliasMap;
		  
	  }
	 

	 public static void analyzeDiffRename(RevCommit commit, Map<String, List<String>> fileAliasMap) {
		 List<DiffEntry> diffs;
		try {
			diffs = getDiffs(commit);
			if (diffs != null) {
				for (DiffEntry diff : diffs) {
					String type = diff.getChangeType().toString();

					String oldPath = diff.getOldPath();

					String newPath = diff.getNewPath();					
					
					if (type.equals(RENAME) && oldPath.endsWith(FILE_EXTENSION)) {
						populateMapAlias(newPath, oldPath, fileAliasMap);
					}
					
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE,"Errore nell analizzare diffEntries commit");
			System.exit(1);
		}
		
	 }

	 
	 
	 public static void populateMapAlias(String newPath, String oldPath, Map<String, List<String>> map) {
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

					List<String> oldPaths = map.get(key);
					if (key.equals(oldPath)) {
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
	 
	 
	 public static void checkBuggyness(List<Release> releasesList, List<Ticket> ticketList, Map<String, List<String>> fileAliasMap) throws IOException {
		 
		 /* per ogni ticket mi prendo la lista dei commit che contengono l'id del ticket,
		  * mi prendo i file .java DELETE e MODIFY nel commit
		  * vedo a che release appartiene il commit e vedo se essa è contenuta nelle AV del ticket
		  * 	se si --> setto buggyness = "si" per i file java
		  */
		 
		 for (Ticket ticket : ticketList) {
			 List<Integer> aV = ticket.getAV();
			 for (RevCommit commit : ticket.getCommitList()) {				
				 List<DiffEntry> diffs = getDiffs(commit);
				 if (diffs != null) {
					 analyzeDiffEntryBugg(diffs, releasesList, aV, fileAliasMap);
				}
			 }
		 }
	}
	 
	 
	 public static void analyzeDiffEntryBugg(List<DiffEntry> diffs, List<Release> releasesList, List<Integer> aV, Map<String, List<String>>  fileAliasMap) {
		 for (DiffEntry diff : diffs) {
				String type = diff.getChangeType().toString();
				
				if (diff.toString().contains(FILE_EXTENSION) && (type.equals(MODIFY)
						|| type.equals(DELETE) )) { 
				
					// vedo se releaseCommit e' contenuta nella AV del ticket, se si setto
					// come buggy il file nella relativa release
					checkFileBugg(diff, releasesList, aV, fileAliasMap);
				}
			}
	 }

	 public static void checkFileBugg(DiffEntry diff, List<Release> releasesList,List<Integer> aV, Map<String, List<String>> fileAliasMap) {
		 
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
		 /* scorro i file java nella lista della release, e se trovo un file java che ha il nome
		  * del file trovato, setto il file buggy secondo le AV
		  */
		 for (Release release : releasesList) {
			 for (JavaFile javaFile : release.getFileList()){ 
				 if(javaFile.getName().equals(file) || checkMapRename(javaFile.getName(), fileAliasMap)) {
					 //FILE TROVATO
					 compareReleaseAV(javaFile, aV, release);
				 }
			 }
		 }
	 }
	 

	 public static boolean checkMapRename(String nameFile,Map<String, List<String>> fileAliasMap ) {
		 for (Entry<String, List<String>> entry : fileAliasMap.entrySet()) {
			    String key = entry.getKey();
			    List<String> oldPaths = entry.getValue();
			    if (nameFile.equals(key) || oldPaths.contains(nameFile)) {
					//IL FILE STA NEGLI ALIAS
					 return true;
			    }
		 }
		 return false;
	 }
	 
	 public static void compareReleaseAV(JavaFile javaFile, List<Integer> aV, Release release) {
		 if (aV.contains(release.getIndex())) {
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
	 
	 
}
