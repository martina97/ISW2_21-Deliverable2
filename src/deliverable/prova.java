package deliverable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public class prova {
	
	private static final String REPO ="D:/Programmi/Eclipse/eclipse-workspace/daffodil/.git";
	private static Path repoPath = Paths.get("D:/Programmi/Eclipse/eclipse-workspace/daffodil");
	

public static void main(String[] args) throws IllegalStateException, GitAPIException, IOException {
		
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
		    	String ticketID = "DAFFODIL-661";
	    		String ticket2 = ticketID.replace("DAFFODIL","DFDL");

	    		 System.out.println("vecchia stringa = " + ticketID + "\tnuova stringa = " + ticket2);
	    		 Integer count = 0;
		    	 for (RevCommit rev : logs) {
		    		 
		    		 String commit = rev.getFullMessage();
		    		 //String ticketID = "DFDL-967";
					 if (commit.contains(ticketID +",") || commit.contains(ticketID +"\r")|| commit.contains(ticketID +"\n") || commit.contains(ticketID + " ") || commit.contains(ticketID +":")
							 || commit.contains(ticketID +".")|| commit.contains(ticket2 + ",") || commit.endsWith(ticketID) ||
							 commit.endsWith(ticket2) || commit.contains(ticket2 + " ") || commit.contains(ticket2 + ":") || commit.contains(ticket2 + "\n")|| commit.contains(ticket2 + ".")) {   

		    		 //if (rev.getFullMessage().contains(ticketID)) {
			    		 System.out.println("\n\n" + rev.getFullMessage());
			    		 System.out.println("##########");
			    		 count++;
		    		 }
		    		 //String message = rev.getFullMessage();
		    		 //System.out.print(rev.getAuthorIdent().getName());
		    	 }
		    	 System.out.println("\n\n\nCOUNT = " + count);
		    }
	}

}
