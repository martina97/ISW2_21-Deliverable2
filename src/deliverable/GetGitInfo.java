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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import entities.JavaFile;




public class GetGitInfo {
	private static final String REPO = "D:/Programmi/Eclipse/eclipse-workspace/bookkeeper/.git";
	private static Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/bookkeeper");
	private static Repository repository;
	public static final String FILE_EXTENSION = ".java";
	
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
	
	/*
	public static void commitHistory(Release release) throws  GitAPIException, IOException	{
		Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/bookkeeper");

	
		try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
			 //
		    }
		    InitCommand init = Git.init();
		    init.setDirectory(repoPath.toFile());
		    try (Git git = init.call()) {
		    	//
		    }
		    try (Git git = Git.open(repoPath.toFile())) {
		    for (RevCommit commit : release.getCommitList()) {
		        String commitID = commit.getName();
		        if (commitID != null && !commitID.isEmpty())
		        {
		            LogCommand logs2 = git.log().all();
		            Repository repository = logs2.getRepository();
		            TreeWalk tw = new TreeWalk(repository);
		            tw.setRecursive(true);
		            RevCommit commitToCheck = commit;
		            tw.addTree(commitToCheck.getTree());
		            for (RevCommit parent : commitToCheck.getParents())
		            {
		                tw.addTree(parent.getTree());
		            }
		            while (tw.next())
		            {
		                int similarParents = 0;
		                for (int i = 1; i < tw.getTreeCount(); i++)
		                    if (tw.getFileMode(i) == tw.getFileMode(0) && tw.getObjectId(0).equals(tw.getObjectId(i)))
		                        similarParents++;
		                if (similarParents == 0) {
		                	if(tw.getPathString().endsWith(".java")) {
		                        System.out.println("File names: " + tw.getPathString());
		                        release.getFileList().add(tw.getPathString());
		                	}
		 
		                }
	            }
		        }
	        }
	    }
	}
	*/
	
	
	public static void commitHistory2(Path repoPath, List<Release> releasesList) throws  GitAPIException, IOException	{

	
		try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
			 //
		    }
		    InitCommand init = Git.init();
		    init.setDirectory(repoPath.toFile());
		    try (Git git = init.call()) {
		    	//
		    }
		    try (Git git = Git.open(repoPath.toFile())) {
		    	for (Release release : releasesList) {
				    for (RevCommit commit : release.getCommitList()) {
				    	analyzeCommit(commit, release, git);
				    }
				    List<JavaFile> listWithDuplicates = release.getFileList();
				    List<JavaFile> listWithoutDuplicates = listWithDuplicates.stream()
				     .distinct()
				     .collect(Collectors.toList());
					   System.out.println("NUMERO DI FILE JAVA DELLA RELEASE " + release.getIndex() +" == " + listWithoutDuplicates.size());
		    	}
		    }
	}
	
	public static void analyzeCommit(RevCommit commit, Release release, Git git) throws IOException {
		
		String commitID = commit.getName();
        if (commitID != null && !commitID.isEmpty())
        {
            LogCommand logs2 = git.log().all();
            Repository repository = logs2.getRepository();
            TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(true);
            RevCommit commitToCheck = commit;
            tw.addTree(commitToCheck.getTree());
            for (RevCommit parent : commitToCheck.getParents())
            {
                tw.addTree(parent.getTree());
            }
            while (tw.next())
            {
            	getFileJava(tw, release);
                
        }
        }
    }
	
	public static void getFileJava(TreeWalk tw, Release release) {
		int similarParents = 0;
        for (int i = 1; i < tw.getTreeCount(); i++)
            if (tw.getFileMode(i) == tw.getFileMode(0) && tw.getObjectId(0).equals(tw.getObjectId(i)))
                similarParents++;
        if (similarParents == 0 && tw.getPathString().endsWith(FILE_EXTENSION) ) {
                //System.out.println("File names: " + tw.getPathString());
        	JavaFile file = new JavaFile(tw.getPathString());
            release.getFileList().add(file);
        }
	}
	
	public static void getJavaFiles(Path repoPath, List<Release> releasesList) throws IOException, GitAPIException {

		try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {

		}

		InitCommand init = Git.init();
		init.setDirectory(repoPath.toFile());

		try (Git git = Git.open(repoPath.toFile())) {
			for (Release release : releasesList) {
				List<String> fileName = new ArrayList<>();
				for (RevCommit commit : release.getCommitList()) {
	
					ObjectId treeId = commit.getTree();
		
					try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
						treeWalk.reset(treeId);
						treeWalk.setRecursive(true);
		
						while (treeWalk.next()) {
							addJavaFile(treeWalk,release, fileName);
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
				List<JavaFile> listWithDuplicates = release.getFileList();
			    List<JavaFile> listWithoutDuplicates = listWithDuplicates.stream().distinct().collect(Collectors.toList());
				   System.out.println("NUMERO DI FILE JAVA DELLA RELEASE " + release.getIndex() +" == " + listWithoutDuplicates.size());
			}
			}
		
	}
	
	public static void addJavaFile(TreeWalk treeWalk, Release release, List<String> fileName) {
		/* 
		 * Aggiungo il file Java nella lista di file della release, 
		 * e inizialmente setto buggyness = "no"
		 */
		if (treeWalk.getPathString().endsWith(FILE_EXTENSION)) {
			String nameFile = treeWalk.getPathString();
			if (!fileName.contains(nameFile)) {
				fileName.add(nameFile);
				JavaFile file = new JavaFile(nameFile);
				file.setBugg("No");
				release.getFileList().add(file);

			}
		}
	}

	 public static void checkRename(List<Release> releasesList ) throws IOException {
	 //public static void checkRename(Release release ) throws IOException {
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
		  * 	se old path � una key della mappa, lo metto nella lista di old path e metto come nuova key il new path
		  * 	se old path sta nella lista di old path, vedo se la key = new path
		  * 		se si --> vado avanti
		  * 		altrimenti --> metto come key il new path e metto la vecchia key nella lista 
		  */
		 
		 FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
			repository = repositoryBuilder.setGitDir(new File(REPO)).readEnvironment() // scan environment GIT_* variables
					.findGitDir() // scan up the file system tree
					.setMustExist(true).build();
		  for (Release release : releasesList) {
			  System.out.println("\n\nRELEASE " + release.getIndex());
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
							
							
							if (type.equals("RENAME") && oldPath.endsWith(FILE_EXTENSION)) {
								//boolean oPCheck = true;
								//boolean nPCheck = true;
								System.out.println("oldPath = " + oldPath);
								System.out.println("newPath = " + newPath);
								prova(newPath, oldPath, fileAliasMap);
								System.out.println("#####\n\n");
							}
							
						}
					}
					
			  	}
			 

			  }
		  
		  System.out.println("\n\n\nFILE ALIAS MAP == ");
		  /*
		  for (Map.Entry<String,List<String>> entry : fileAliasMap.entrySet()) {
			    String key = entry.getKey(); 
			    List<String> oldPaths = entry.getValue();
			    System.out.println(key + "\n");
			    for (String name : oldPaths) {
				    System.out.println("old path == " + name + "\n");
			    }
			    System.out.println("############\n");
			  
		  }*/
		  
		  
		  for (int i = 0; i<fileAliasMap.size(); i++) {
				 Object key = fileAliasMap.keySet().toArray()[i];
				 //Object valueForFirstKey = fileAliasMap.get(key);
				 List<String> oldPaths = fileAliasMap.get(key);
				 System.out.println("new path == " + key + "\n");
				 for (String name : oldPaths) {
					    System.out.println("old path == " + name + "\n");
				 }
				 System.out.println("############\n");
		    //i = fileAliasMap.size()-1;
		  }
		  
	  }
	  
	 public static void populateMapAlias(String oldPath, String newPath, HashMap<String, List<String>> fileAliasMap) {
		 System.out.println("\nPOPULATE MAP ALIAS\nold path = " + oldPath+ "\nnewPath = " + newPath + "\n");
		 int count = 0;
		 String key  = null ;
		 List<String> oldPaths = new ArrayList<>();

		 if (fileAliasMap.isEmpty()) {
			 //List<String> oldPaths = new ArrayList<>();
			 System.out.println("mappa vuota");
			 oldPaths.add(oldPath);
			 fileAliasMap.put(newPath, oldPaths);
		 }
		 else {
			 System.out.println("mappa non vuota");
			 for (Map.Entry<String,List<String>> entry : fileAliasMap.entrySet()) {
				    key = entry.getKey(); 
				    //System.out.println("KEY == " + key);
				    //System.out.println("oldPAth == " + oldPath);

				    if (newPath.equals(key)) {
				    	//entry.getValue().add(oldPath); //aggiungo oldPath alla lista di oldPaths 
				    	count = 1;
				    	//break;
				    }
				    
				    if (oldPath.equals(key)) {
				    	//String newKey = newPath;
				    	//oldPaths.add(oldPath);
				    	count = 2;
				    	//fileAliasMap.remove(key);
				    	//fileAliasMap.put(newKey, oldPaths);
				    	//break;
				    }
				    
				    else {
				    	count = 3;
				    	/*
						 List<String> list = Arrays.asList(oldPath);

					    fileAliasMap.put(newPath, list);
					    */

				    	//break;
				    }
			 }
			 
			 
		 }
		 
		 
		 if (count == 1 ) {
			 System.out.println("count = 1 ");

			 fileAliasMap.get(key).add(oldPath);
		 }
		 if (count == 2) {
			 System.out.println("count = 2 ");

			 String newKey = newPath;
		     oldPaths.add(oldPath);
		     fileAliasMap.remove(key);
		     fileAliasMap.put(newKey, oldPaths);
		 }
		 if (count == 3) {
			 System.out.println("count = 3 ");

			 List<String> list = Arrays.asList(oldPath);
		     fileAliasMap.put(newPath, list);
		 }
		 System.out.println("###\n");
	 }

	 
	 public static void populateMapAlias2(String oldPath, String newPath, HashMap<String, List<String>> fileAliasMap) {
		 System.out.println("\nPOPULATE MAP ALIAS\nold path = " + oldPath+ "\nnewPath = " + newPath + "\n");
		 int count = 0;
		 String key  = null ;
		 List<String> oldPaths = new ArrayList<>();

		 if (fileAliasMap.isEmpty()) {
			 //List<String> oldPaths = new ArrayList<>();
			 System.out.println("mappa vuota");

			 oldPaths.add(oldPath);
			 fileAliasMap.put(newPath, oldPaths);
		 }
		 else {
			 System.out.println("mappa non vuota");
				for (int i = 0; i<fileAliasMap.size(); i++) {
					 key = (String) fileAliasMap.keySet().toArray()[i];
					 oldPaths = fileAliasMap.get(key);
				    //System.out.println("KEY == " + key);
				    //System.out.println("oldPAth == " + oldPath);
				if (count == 0 ) {
				    if (newPath.equals(key)) {
				    	//entry.getValue().add(oldPath); //aggiungo oldPath alla lista di oldPaths 
				    	count = 1;
				    	System.out.println("count = 1 ");

						fileAliasMap.get(key).add(oldPath);
				    }
				    
				    if (oldPath.equals(key)) {
				    	//String newKey = newPath;
				    	//oldPaths.add(oldPath);
				    	count = 2;
				    	//fileAliasMap.remove(key);
				    	//fileAliasMap.put(newKey, oldPaths);
				    	//break;
				    	System.out.println("count = 2 ");

						String newKey = newPath;
					    oldPaths.add(oldPath);
					    fileAliasMap.remove(key);
					    fileAliasMap.put(newKey, oldPaths);
				    }
				    
				    else {
				    	count = 3;
				    	System.out.println("count = 3 ");

						//List<String> list = new ArrayList<>();
						oldPaths.add(oldPath);
					    fileAliasMap.put(newPath, oldPaths);
				    	/*
						 List<String> list = Arrays.asList(oldPath);

					    fileAliasMap.put(newPath, list);
					    */

				    	//break;
				    }
			 }
				}
			 
		 }
		 
		 /*
		 if (count == 1 ) {
			 System.out.println("count = 1 ");

			 fileAliasMap.get(key).add(oldPath);
		 }
		 if (count == 2) {
			 System.out.println("count = 2 ");

			 String newKey = newPath;
		     oldPaths.add(oldPath);
		     fileAliasMap.remove(key);
		     fileAliasMap.put(newKey, oldPaths);
		 }
		 if (count == 3) {
			 System.out.println("count = 3 ");

			 List<String> list = Arrays.asList(oldPath);
		     fileAliasMap.put(newPath, list);
		 }
		 */
		 System.out.println("###\n");
	 }

	 public static void prova(String newPath, String oldPath, HashMap<String, List<String>> map) {
		 // i commit stanno nella lista con data in ordine crescente, quindi ogni file rinominato o non � mai 
		 //stato rinominato in passato, oppure � stato rinominato quindi key della mappa = oldPath
			if(map.isEmpty()) {
				List<String> oldPaths = new ArrayList<>();
				oldPaths.add(oldPath);
				map.put(newPath,oldPaths);

			}
			else {
				/*scorro la lista:
				 * 	se oldPath � una key, metto key = newPath e oldPath lo metto nella lista di oldPaths --> break 
				 *  altrimenti aggiungo alla mappa la coppia oldPath-newPath
				 */
				for(int i = 0 ; i<map.size();i++) {
					String key = (String) map.keySet().toArray()[i];
					//System.out.println("KEY == " + key);

					List<String> oldPaths = map.get(key);
					if (key.equals(oldPath)) {
						System.out.println("KEY == NEW PATH");
						System.out.println("KEY == " + key);
						System.out.println("oldPath == " + oldPath);


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
	  
	public static void main(String[] args) {
		// main
	}
		
	
}
