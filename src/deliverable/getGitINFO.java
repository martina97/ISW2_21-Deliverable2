package deliverable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import entities.Ticket;

public class getGitINFO {

	
	public static void getAllCommit2(Path repoPath, List<Ticket> ticketList) throws IllegalStateException, GitAPIException, IOException {
		
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
		    	for (Ticket ticket : ticketList) {
		    		String ticketID = ticket.getID();
		    		System.out.println("#######\n\nticketID == " + ticketID + "\n");
		    		 for (RevCommit rev : logs) {
			    		 String commit = rev.getFullMessage();
			    		 System.out.println("commit === " + commit);
			    		 //commitList.add(rev);
			    		 if (commit.contains(ticketID)){
				    		 System.out.println(rev.getId() + "\n" + commit + "#######\n\n");

			    		 }
			    		 /*
		    			 if (commit.contains(ticketID +",") || commit.contains(ticketID +"\r") || commit.contains(ticketID +"\n")|| commit.contains(ticketID + " ") || commit.contains(ticketID +":")
								 || commit.contains(ticketID +".")|| commit.contains(ticketID + "/") || commit.endsWith(ticketID) ||
								 commit.contains(ticketID + "]")|| commit.contains(ticketID+"_") || commit.contains(ticketID + "-") || commit.contains(ticketID + ")") ) {
				    		 System.out.println(rev.getId() + "\n" + commit + "#######\n\n");

			    		 }
			    		 */
			    		 //System.out.print(rev.getFullMessage());
			    		 //System.out.print(rev.getAuthorIdent().getName());
			    	 }
		    	}
		    	
		    }
	}
	
	
	public static  ArrayList<RevCommit> getAllCommit(Path repoPath) throws IllegalStateException, GitAPIException, IOException {
			
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
		    		 String message = rev.getFullMessage();
		    		 commitList.add(rev);
		    		 //System.out.print(rev.getFullMessage());
		    		 //System.out.print(rev.getAuthorIdent().getName());
		    	 }
		    }
		    return commitList;
		}
		
	
}
