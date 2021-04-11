package deliverable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public class getGitINFO {

	
	public static void getAllCommit(Path repoPath, ArrayList<RevCommit> commitList) throws IllegalStateException, GitAPIException, IOException {
		
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
	}
		
	
}
