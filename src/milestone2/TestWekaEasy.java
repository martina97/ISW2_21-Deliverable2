package milestone2;
/*
 *  How to use WEKA API in Java 
 *  Copyright (C) 2014 
 *  @author Dr Noureddin M. Sadawi (noureddin.sadawi@gmail.com)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it as you wish ... 
 *  I ask you only, as a professional courtesy, to cite my name, web page 
 *  and my YouTube Channel!
 *  
 */

import weka.core.Instance;
//import required classes
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.supervised.instance.Resample;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.evaluation.*;
import weka.classifiers.lazy.IBk;
import weka.filters.unsupervised.instance.RemovePercentage;
import deliverable.GetJIRAInfo;
import deliverable.Release;
import entities.Ticket;


public class TestWekaEasy{
	
	protected static List<Release> releasesList;
	public static final String NAME_PROJECT = "BOOKKEEPER";

	public static void main(String args[]) throws Exception{
		//load datasets
		releasesList = GetJIRAInfo.getListRelease(NAME_PROJECT);
		removeHalf(releasesList);
		String arffPath = "D:"+"\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\datasetConVirgoleWEKA.arff";
		
		//prova(csvPath); 
		//prova2(csvPath, releasesList);
		prova3(arffPath, releasesList);
		
		
		
		DataSource source1 = new DataSource("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\datasetConVirgoleWEKA.arff");
		Instances training = source1.getDataSet();
		
		/*
		DataSource source2 = new DataSource("C:/Program Files/Weka-3-8/data/breast-cancerNOTK.arff");
		Instances testing = source2.getDataSet();

		int numAttr = training.numAttributes();
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);

		RandomForest classifier = new RandomForest();
		NaiveBayes classifier2 = new NaiveBayes();
		IBk classifier3 = new IBk();
		
		classifier.buildClassifier(training);

		Evaluation eval = new Evaluation(testing);	

		eval.evaluateModel(classifier, testing); 
		
		System.out.println("AUC = "+eval.areaUnderROC(1));
		System.out.println("kappa = "+eval.kappa());
		*/
	}
	
	public static void removeHalf(List<Release> releasesList) {
		int releaseNumber = releasesList.size();
		   float half = (float) releaseNumber / 2;
		   int halfRelease = (int) half; // arrotondo in difetto, ora il numero di release che voglio e' la meta'
		   		   
		   Iterator<Release> i = releasesList.iterator();
		   while (i.hasNext()) {
		      Release s = i.next(); 
		      if (s.getIndex() > halfRelease) {
		      // Do something
		    	  i.remove();
		      }
		   }
	}
	
	
	public static void prova3(String arffPath, List<Release> releasesList) {
		
		DataSource source;
		List<Instances> instancesList = new ArrayList<>();
		
		try {
			source = new DataSource(arffPath);
			//Instances instances = source.getDataSet();	//instances = tutte le righe del dataset 

			
		for (Release release : releasesList) {
			Instances instances = source.getDataSet();	//instances = tutte le righe del dataset 

			Iterator<Instance> instance = instances.iterator();
			int indexRelease = release.getIndex();
			while (instance.hasNext()) {
			      Instance i = instance.next(); 
			      int index = (int)(i.value(0));
			      if (index != indexRelease) {	
			    	  instance.remove();
			      }
			}
			
			instancesList.add(instances);
		}
		
		/*
		for (int j = 0; j<instancesList.size(); j++) {
			System.out.println(instancesList.get(j));
			System.out.println("##############\n\n\n");
		}*/
		
		// ora instancesList contiene la lista di istanze, ossia tutte le righe che hanno come release1, poi 2, poi 3...
		/* devo prendermi 2 release e mettere 1 come train e 2 come test
		 * poi 1-2 come train e 3 come test ... 
		 */
		for (int k =2;k<releasesList.size(); k++) {
			System.out.println(k);
			int numTrain = k-1;
			int numTest = k-(k-1);
			// # train e' k-1
			// # test e' k-(k-1)
			System.out.println("numTrain == " + numTrain);
			System.out.println("numTest == " + numTest);
			int m;
			
			//training set
			for (m = 0; m<numTrain; m++) {
				System.out.println(instancesList.get(m));
			}
			
			//test set
			System.out.println("m == " + m);
			System.out.println(instancesList.get(m));

			
			System.out.println("#####\n\n");


		}
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	public static void prova2(String csvPath, List<Release> releasesList) {
		/* mi creo tot csv per training e test per fare walkForward
		 * 
		 */
		String extension = "csv";
		/* la prima volta avro' release 1 x train e release 2 x test 
		 * poi release 1-2 per train e 3 x test
		 * poi release 1-2-3 per train e 4 x test
		 * ...
		 */
		
		/*
		Reader reader = new FileReader(csvPath);
		List<String[]> rows = new CSVReader(reader).readAll();

		for(String[] column: rows) {
			if (column[1].equals("6")){
			    System.out.println("Found row No. is " + rows.indexOf(column));
			}
		}*/
		
		/*
		for (int i = 0; i<releasesList.size(); i++) {
			System.out.println(" relase == " + releasesList.get(i).getIndex());
			while ((line = br.readLine()) != null) {
			    football = line.split(csvSplit);
			    if(football[1].equals("Chelsea") {
			        System.out.println("I found Chelsea!");
			    }
			}
		}*/
		
		
		try (Scanner s = new Scanner(new FileReader(String.format("%s", csvPath)))) {
	        int file = 0;
	        int cnt = 0;
	        BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s_%d.%s", csvPath, file, extension)));

	        while (s.hasNext()) {
	            writer.write(s.next() + System.lineSeparator());
	            if (++cnt == 5 && s.hasNext()) {
	                writer.close();
	                writer = new BufferedWriter(new  FileWriter(String.format("%s_%d.%s", csvPath, ++file, extension)));
	                cnt = 0;
	            }
	        }
	        writer.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	}
	
	
	
	public static void prova (String csvPath) {
		try {
		 int lines = 10;  //set this to whatever number of lines you need in each file
	     int count = 0;
	     File file = new File(csvPath);  
	     Scanner scanner = new Scanner(file);  
	     while (scanner.hasNextLine()) {  //counting the lines in the input file
	        scanner.nextLine();  
	        count++;  
	      }  
	     System.out.println(count);
	     int files=0;  
	     if((count%lines)==0){  
	        files= (count/lines);  
	      }  
	      else{  
	         files=(count/lines)+1;  
	      }   
	      System.out.println(files); //number of files that shall be created

	      myFunction(lines,files, csvPath);
	 }

	 catch (FileNotFoundException e) {
	       e.printStackTrace();
	 }
	 catch (IOException e) {
	  e.printStackTrace();
	 }
	

	}
	public static void myFunction(int lines, int files, String csvPath) throws FileNotFoundException, IOException{

	    BufferedReader br = new BufferedReader(new FileReader(csvPath)); //reader for input file intitialized only once
	    String strLine = null; 
	    for (int i=1;i<=files;i++) { 
	        FileWriter fstream1 = new FileWriter("D:\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\prova" + i + ".csv"); //creating a new file writer.       
	        BufferedWriter out = new BufferedWriter(fstream1);  
	        for(int j=0;j<lines;j++){   //iterating the reader to read only the first few lines of the csv as defined earlier
	             strLine = br.readLine();   
	            if (strLine!= null) { 
	               String strar[] = strLine.split(",");
	               out.write(strar[0]);   //acquring the first column
	               out.newLine();   
	            } 
	        }
	        out.close(); 
	        }  
	   }
	
	public static void walkForward(String arffPath) {
		
		Instances data = null;
		
		DataSource source;
		try {
			source = new DataSource(arffPath);
			data = source.getDataSet();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (data.classIndex() == -1) {
		       data.setClassIndex(data.numAttributes() - 1);
		}
		
		int numTraining;
	    int numTesting;
	    Instances training2=null;
	    Instances testing2=null;
	    
	    
		
	}
}
