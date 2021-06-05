package deliverable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import entities.JavaFile;
import entities.Release;


public class Metrics {
	
	private Metrics() {}

	
	private static final String FILE_EXTENSION = ".java";
	private static final String RENAME = "RENAME";
	private static final String DELETE = "DELETE";
	private static final String MODIFY = "MODIFY";

	
	public static void getMetrics(List<Release> releasesList, String repo) throws IOException {
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		Repository repository = repositoryBuilder.setGitDir(new File(repo)).readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.setMustExist(true).build();
		
		 for (Release release : releasesList ) {
			 //System.out.println("RELEASE == " + release.getIndex());
			 
			 /* creo hashMap che ha come 
			  * key --> nome file
			  * value --> HashMap con key = NR , value = lista autori 
			  */
			 List<JavaFile> fileList = new ArrayList<>();	//lista che contiene i nomi dei file dentro diffs dei commit per ogni release 
			 List<Integer> chgSetSizeList = new ArrayList<>();
			 
			 for (RevCommit commit : release.getCommitList()) {
				 DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);	
				 df.setRepository(repository);
				 df.setDiffComparator(RawTextComparator.DEFAULT);
				 df.setDetectRenames(true);
				
				 String authName = commit.getAuthorIdent().getName();
				 List<DiffEntry> diffs = GetGitInfo.getDiffs(commit);
				 
				 if (diffs != null) {
					analyzeDiffEntryMetrics(diffs, fileList, authName, chgSetSizeList, df);
				 }

			 }
			 setFileRelease(fileList,  release);
		 }
	}
	
	public static void analyzeDiffEntryMetrics(List<DiffEntry> diffs, List<JavaFile> fileList, String authName, List<Integer> chgSetSizeList,DiffFormatter df) {
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
				addFileList(fileList, file, authName, numDiff, diff, df);
			}
		}
		
	 }
	
	public static void addFileList(List<JavaFile> fileList, String fileName, String authName, int numDiff, DiffEntry diff,  DiffFormatter df) {
		 int count = 0 ; 
		 int locAdded = 0;
		 int locDeleted = 0;
		 try {
			for(Edit edit : df.toFileHeader(diff).toEditList()) {

					locAdded += edit.getEndB() - edit.getBeginB();
					locDeleted += edit.getEndA() - edit.getBeginA();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		 int churn = locAdded - locDeleted;
		 
		 if (fileList.isEmpty()) {
			 //System.out.println("LISTA VUOTA");
			 JavaFile javaFile = new JavaFile(fileName);
			 javaFile.setNr(1);
			 List<String> listAuth = new ArrayList<>();
			 listAuth.add(authName);
			 javaFile.setNAuth(listAuth);
			 javaFile.setChgSetSize(numDiff);
			 List<Integer> chgSetSizeList = new ArrayList<>();
			 chgSetSizeList.add(numDiff);
			 javaFile.setChgSetSizeList(chgSetSizeList);
			 javaFile.setLOCadded(locAdded);
			 List<Integer> locAddedList = new ArrayList<>();
			 locAddedList.add(locAdded);
			 javaFile.setLocAddedList(locAddedList);
			 javaFile.setChurn(churn);
			 List<Integer> churnList = new ArrayList<>();
			 churnList.add(churn);
			 javaFile.setChurnList(churnList);
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
					 file.getChgSetSizeList().add(numDiff);
					 file.setLOCadded(file.getLOCadded()+locAdded);
					 file.getLocAddedList().add(locAdded);
					 file.setChurn(file.getChurn() + churn);
					 file.getChurnList().add(churn);
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
			 List<Integer> chgSetSizeList = new ArrayList<>();
			 chgSetSizeList.add(numDiff);
			 javaFile.setChgSetSizeList(chgSetSizeList);
			 javaFile.setLOCadded(locAdded);
			 List<Integer> locAddedList = new ArrayList<>();
			 locAddedList.add(locAdded);
			 javaFile.setLocAddedList(locAddedList);
			 javaFile.setChurn(churn);
			 List<Integer> churnList = new ArrayList<>();
			 churnList.add(churn);
			 javaFile.setChurnList(churnList);
			 
			 fileList.add(javaFile);
		 }
	 }
	
	 public static void setFileRelease(List<JavaFile> fileList, Release release) {
		 for (JavaFile javaFile : fileList) {
			 //System.out.println("javaFile == " + javaFile.getName());
			 List<String> nAuth = javaFile.getNAuth();
			 List<Integer> chgSetSize = javaFile.getChgSetSizeList();
			 List<Integer> locAdded = javaFile.getLocAddedList();
			 List<Integer> churn = javaFile.getChurnList();

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
					 List<Integer> chgSetSizeList = fileRel.getChgSetSizeList();
					 chgSetSizeList.addAll(chgSetSize);
					 fileRel.setChgSetSizeList(chgSetSizeList);
					 fileRel.setLOCadded(fileRel.getLOCadded()+javaFile.getLOCadded());
					 List<Integer> locAddedList = fileRel.getLocAddedList();
					 locAddedList.addAll(locAdded);
					 fileRel.setLocAddedList(locAddedList);
					 
					 fileRel.setChurn(fileRel.getChurn()+javaFile.getChurn());
					 List<Integer> churnList = fileRel.getChurnList();
					 churnList.addAll(churn);
					 fileRel.setChurnList(churnList);
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
					 List<Integer> chgSetSizeList = fileRel.getChgSetSizeList();
					 chgSetSizeList.addAll(chgSetSize);
					 fileRel.setChgSetSizeList(chgSetSizeList);
					 
					 fileRel.setLOCadded(fileRel.getLOCadded() + javaFile.getLOCadded());
					 List<Integer> locAddedList = fileRel.getLocAddedList();
					 locAddedList.addAll(locAdded);
					 fileRel.setLocAddedList(locAddedList);
					 
					 fileRel.setChurn(fileRel.getChurn()+javaFile.getChurn());
					 List<Integer> churnList = fileRel.getChurnList();
					 churnList.addAll(churn);
					 fileRel.setChurnList(churnList);
					 //System.out.println("fileRel --> \tnR == " + fileRel.getNr() + "\tnAuth == " + fileRel.getNAuth().size());

				 }
			 }

		 }
	 }
	 
	 public static int loc(TreeWalk treewalk, Repository repository) throws IOException {
			
			ObjectLoader loader = repository.open(treewalk.getObjectId(0));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
			loader.copyTo(output);
			
			String filecontent = output.toString();
			StringTokenizer token= new StringTokenizer(filecontent,"\n");
			
			int count=0;
			while(token.hasMoreTokens()) {
				count++;
				token.nextToken();
			}
			return count;
	}
}
