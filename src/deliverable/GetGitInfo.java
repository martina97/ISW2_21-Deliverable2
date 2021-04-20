package deliverable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import entities.JavaFile;




public class GetGitInfo {

	
	public static  List<RevCommit> getAllCommit(Path repoPath,List<Release> releasesList) throws IllegalStateException, GitAPIException, IOException {
			
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
        if (similarParents == 0 && tw.getPathString().endsWith(".java") ) {
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
							addJavaFiles(treeWalk,release, fileName);
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
	
	public static void addJavaFiles(TreeWalk treeWalk, Release release, List<String> fileName) {
		if (treeWalk.getPathString().endsWith(".java")) {
			String nameFile = treeWalk.getPathString();
			if (!fileName.contains(nameFile)) {
				fileName.add(nameFile);
				JavaFile file = new JavaFile(nameFile);
				file.setBugg("No");
				release.getFileList().add(file);

			}
		}
	}


	public static void main(String[] args) {
		// main
	}
		
	
}
