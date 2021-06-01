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
import weka.filters.Filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.WrapperSubsetEval;
import weka.core.Instances;
import weka.filters.Filter;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.evaluation.*;
import weka.classifiers.lazy.IBk;
import weka.filters.unsupervised.instance.RemovePercentage;
import deliverable.CSVWriter;
import deliverable.GetJIRAInfo;
import deliverable.Release;
import deliverable.Utils;
import entities.DBEntriesM2;
import entities.M2Entries;
import entities.Ticket;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Normalize;


public class TestWeka2{
	
	private static List<Release> releasesList;
	private static List<M2Entries> dBentriesList;
	private static final String NAME_PROJECT = "BOOKKEEPER";
	private static int datasetDimension;
	static Logger logger = Logger.getLogger(TestWekaEasy.class.getName());

	
	//public static final String NAME_PROJECT = "AVRO";

	public static void main(String args[]) throws Exception{
		//load datasets
		releasesList = GetJIRAInfo.getListRelease(NAME_PROJECT);
		removeHalf(releasesList);
		dBentriesList = new ArrayList<>();
		
		String arffPath = "D:"+"\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\FINITO\\CSV FINALE BOOKKEEPER.arff";
		//String arffPath = "D:"+"\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\MATTEO_BOOKKEEPERMetrics.arff";
		//String arffPath = "D:"+"\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\BookkeeperCecilia.arff";

		//prova(csvPath); 
		//prova2(csvPath, releasesList);
		walkForward3(arffPath, releasesList);
		//CSVWriter.writeCsvMilestone2(dBentriesList);
		System.out.println("DIMENSIONE LISTA DB ENTRIES == " +dBentriesList.size() );
		
	}
	
	
	public static void writeArff() {
		
		String csvPath = "D:"+"\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\FINITO\\CSV FINALE SYNCOPE_VIRGOLE.csv";

		// load CSV
	    CSVLoader loader = new CSVLoader();
	    try {
			loader.setSource(new File(csvPath));
		    Instances data = loader.getDataSet();//get instances object
		 // save ARFF
		    ArffSaver saver = new ArffSaver();
		    saver.setInstances(data);//set the dataset we want to convert
		    saver.setFile(new File("D:"+"\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\FINITO\\CSV FINALE SYNCOPE.arff"));
		    saver.writeBatch();
		    //and save as ARFF

		} catch (IOException e) {
			e.printStackTrace();
		}

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
	
	
	

	public static void walkForward3(String arffPath, List<Release> releasesList) {
		DataSource source;
		List<Instances> instancesList = new ArrayList<>();
		List<String> classifierNames = Arrays.asList("Random Forest", "IBk", "Naive Bayes");
		List<String> balancingNames = Arrays.asList("No sampling","oversampling", "undersampling","SMOTE");
		List<String> featureSelectionNames = Arrays.asList("No selection", "Best first");
	
	
		try {
			source = new DataSource(arffPath);
			Instances dataset = source.getDataSet();
		    datasetDimension = dataset.size();
	
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
		
		
		// ora instancesList contiene la lista di istanze, ossia tutte le righe che hanno come release1, poi 2, poi 3...
		/* devo prendermi 2 release e mettere 1 come train e 2 come test
		 * poi 1-2 come train e 3 come test ... 
		 */
		for (int k =2;k<releasesList.size()+1; k++) {
			Instances training = null;
			Instances testing = null;
			M2Entries entry = new M2Entries(NAME_PROJECT);
			//System.out.println(k);
			int numTrain = k-1;
			int numTest = k-(k-1);
			entry.setNumTrainingRelease(numTrain);
			
	
			// # train e' k-1
			// # test e' k-(k-1)
			System.out.println("numTrain == " + numTrain);
			System.out.println("numTest == " + numTest);
		    System.out.println("datasetDimension == " + datasetDimension);


			int m;
			//training = Instances.mergeInstances(instancesList.get(m), instancesList.get(m+1));
			
			//training set
			//System.out.println(" ###################### TRAINING SET ############################\n\n");
			training = new Instances(instancesList.get(0));
			
			for (m = 1; m<numTrain; m++) {
				//System.out.println(instancesList.get(m));
				for (Instance i : instancesList.get(m)) {
					training.add(i);
				}
			}
			//System.out.println("TRAINING ==== " + training);
	
			
			
			//System.out.println("\n\n ###################### TEST SET ############################\n\n");
	
			//test set
			//System.out.println("m == " + m);
			//System.out.println(instancesList.get(m));
			testing = instancesList.get(m);
			//System.out.println("\n\nTESTING ==== " + testing);
			int numAttr = training.numAttributes();
			training.setClassIndex(numAttr - 1);
			testing.setClassIndex(numAttr - 1);
			
			System.out.println("training.size() == " + training.size());
			System.out.println("testing.size() == " + testing.size());
			
			float percTrain = Utils.calculatePercentage(training.size(), datasetDimension);
			entry.setTrainingPerc(percTrain);
			
			System.out.println("%training == " + percTrain);

			int positiveInstancesTrain = calculateNumberBuggyClass(training);
			System.out.println("positiveInstancesTrain == " + positiveInstancesTrain);
			
			float percDefectTrain = Utils.calculatePercentage(positiveInstancesTrain, training.size());
			entry.setDefectPercTrain(percDefectTrain);
			System.out.println("percDefectTrain == " + percDefectTrain);

			int positiveInstancesTest = calculateNumberBuggyClass(testing);
			System.out.println("positiveInstancesTest == " + positiveInstancesTest);
			float percDefectTest = Utils.calculatePercentage(positiveInstancesTest, testing.size());
			entry.setDefectPercTest(percDefectTest);
			System.out.println("percDefectTest == " + percDefectTest);

			int numAttrTrainingNoFilter = training.numAttributes();
			System.out.println("numAttrTrainingNoFilter == " + numAttrTrainingNoFilter);
			
			chooseClassifier3(classifierNames, training, testing, entry);
	
			
			System.out.println("####################################################################################\n\n");
			//dBentriesList.add(entry);
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	public static void chooseClassifier3(List<String> classifierNames, Instances training, Instances testing, M2Entries entry) {
		AbstractClassifier classifier = null;
	
		
		for(int i = 0; i<classifierNames.size();i++) {
			Instances training2 = new Instances(training);
			Instances testing2 = new Instances(testing);
			M2Entries entry2 = new M2Entries(NAME_PROJECT);
			entry2.setNumTrainingRelease(entry.getNumTrainingRelease());
			entry2.setTrainingPerc(entry.getTrainingPerc());
			entry2.setDefectPercTrain(entry.getDefectPercTrain());
			entry2.setDefectPercTest(entry.getDefectPercTest());

			switch(i) {
				case 0: //Random Forest
					classifier = new RandomForest();
					entry2.setClassifierName("Random Forest");
				break;
				
				case 1: //IBk
					classifier = new IBk();
					entry2.setClassifierName("IBk");

				break;
				
				case 2: //Naive Bayes
					classifier = new NaiveBayes();
					entry2.setClassifierName("Naive Bayes");

				break;
				default:
					logger.log(Level.SEVERE,"Error in classifier selection ");
					System.exit(1);
				break;
			}
			
			//ho selezionato il classificatore, ora devo selezionare la tecnica di featureSelection
			//chooseFeatureSelection2(classifier,classifierNames.get(i), training2, testing2);
			chooseFeatureSelection3(classifier,entry2, training2, testing2);

			//dBentriesList.add(entry2);

		}
	
	}

	
	public static void chooseFeatureSelection3(AbstractClassifier classifier,M2Entries entry, Instances training, Instances testing ) {
		List<String> featureSelectionNames = Arrays.asList("No selection", "Best first");
		
		
		for(int i = 0; i<featureSelectionNames.size(); i++) {
			Instances training2 = new Instances(training);
			Instances testing2 = new Instances(testing);
			
			M2Entries entry2 = new M2Entries(NAME_PROJECT);
			entry2.setNumTrainingRelease(entry.getNumTrainingRelease());
			entry2.setClassifierName(entry.getClassifierName());
			entry2.setTrainingPerc(entry.getTrainingPerc());
			entry2.setDefectPercTrain(entry.getDefectPercTrain());
			entry2.setDefectPercTest(entry.getDefectPercTest());

			
			switch(i) {
				case 0: //No selection
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionNames.get(i));
					entry2.setFeatureSelection(featureSelectionNames.get(i));
				break;
				
				case 1: //Best first
					System.out.println("\n\nLA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionNames.get(i));		
					entry2.setFeatureSelection(featureSelectionNames.get(i));

					break;
	
				default:
					logger.log(Level.SEVERE,"Error in feature selection ");
					System.exit(1);
				break;
			}
			applyFeatureSelection3(classifier, entry2, training2, testing2);
			//dBentriesList.add(entry2);
			
		}
		System.out.println("----\n\n");
		
	}
	
	
	public static void chooseFeatureSelection2(AbstractClassifier classifier,String classifierName, Instances training, Instances testing ) {
		List<String> featureSelectionNames = Arrays.asList("No selection", "Best first");
		
		String featureSelection = "no";
		
		for(int i = 0; i<featureSelectionNames.size(); i++) {
			Instances training2 = new Instances(training);
			Instances testing2 = new Instances(testing);
			
			switch(i) {
				case 0: //No selection
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionNames.get(i));
					featureSelection = featureSelectionNames.get(i);

				break;
				
				case 1: //Best first
					System.out.println("\n\nLA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionNames.get(i));		
					featureSelection = featureSelectionNames.get(i);
					break;
	
				default:
					logger.log(Level.SEVERE,"Error in feature selection ");
					System.exit(1);
				break;
			}
			applyFeatureSelection2(classifier, classifierName, featureSelection, training2, testing2);
			
		}
		System.out.println("----\n\n");
		
	}
	
	
	public static void applyFeatureSelection3(AbstractClassifier classifier,M2Entries entry,Instances training, Instances testing) {
		
		Instances training2 = new Instances(training);
		Instances testing2 = new Instances(testing);
		String featureSelection = entry.getFeatureSelection();
		
		if(!featureSelection.equals("No selection")) {
			//applico featureSelection
			System.out.println("STO USANDO FEATURE SELECTION");
			
			//create AttributeSelection object
			AttributeSelection filter = new AttributeSelection();
			//create evaluator and search algorithm objects
			CfsSubsetEval eval = new CfsSubsetEval();
			BestFirst  search = new BestFirst ();
			//set the algorithm to search backward
			String [] bfSearchOpt = {"-D", "1",  "-N", "5"};
			try {
				search.setOptions(bfSearchOpt);
				
				//set the filter to use the evaluator and search algorithm
				filter.setEvaluator(eval);
				filter.setSearch(search);
				//specify the dataset
				Instances filteredTraining = null;
				Instances filteredTesting = null;
				
				filter.setInputFormat(training);
				//apply
				filteredTraining = Filter.useFilter(training2, filter);
				filteredTesting = Filter.useFilter(testing2, filter);
				
				int numAttrFiltered = filteredTraining.numAttributes();
				filteredTraining.setClassIndex(numAttrFiltered - 1);
				filteredTesting.setClassIndex(numAttrFiltered - 1);
				
				//lavora con feature selection
				//System.out.println("numAttrFiltered == " + numAttrFiltered);
				//chooseClassifier2(filteredTraining,filteredTesting );
				//chooseBalancing3(classifier, entry, featureSelection, filteredTraining,  filteredTesting);
				chooseBalancing4(classifier, entry, filteredTraining,  filteredTesting);

				//chooseBalancing(filteredTraining,filteredTesting);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
		else {
			//lavora senza feature selection
			//chooseBalancing(training,testing);
			//chooseClassifier2(training,testing);
			//chooseBalancing3(classifier, entry, featureSelection, training2,  testing2);
			chooseBalancing4(classifier, entry, training2,  testing2);



		}
	}
	
	
	public static void chooseBalancing4(AbstractClassifier classifier,M2Entries entry, Instances training, Instances testing) {
		
		List<String> balancingNames = Arrays.asList("No sampling","oversampling", "undersampling","SMOTE");
		String classifierName = entry.getClassifierName();
		String featureSelectionName = entry.getFeatureSelection();
		
		
		for(int i = 0; i<balancingNames.size(); i++) {
			FilteredClassifier filteredClassifier = null;
			Instances trainingBalanced = new Instances(training);
			Instances testingBalanced = new Instances(testing);

			M2Entries entry2 = new M2Entries(NAME_PROJECT);
			entry2.setNumTrainingRelease(entry.getNumTrainingRelease());
			entry2.setClassifierName(entry.getClassifierName());
			entry2.setFeatureSelection(entry.getFeatureSelection());
			entry2.setTrainingPerc(entry.getTrainingPerc());
			entry2.setDefectPercTrain(entry.getDefectPercTrain());
			entry2.setDefectPercTest(entry.getDefectPercTest());

			
			switch(i) {
				case 0: //No sampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					int positiveInstances2 = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances2);
					entry2.setBalancing(balancingNames.get(i));
				break;
				
				case 1: //oversampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );	
					entry2.setBalancing(balancingNames.get(i));

					Resample resample = null;
					
				try {
					resample = new Resample();
					resample.setInputFormat(trainingBalanced);
					resample.setNoReplacement(false);
					resample.setBiasToUniformClass(1.0f);
					
					int totInstances = trainingBalanced.size();
					int positiveInstances = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances);
					double percentage =  calculatePercentage(positiveInstances, totInstances);
					//System.out.println("percentage == " + percentage);
					resample.setSampleSizePercent(percentage*2);
					filteredClassifier = new FilteredClassifier();
					filteredClassifier.setClassifier(classifier);
					filteredClassifier.setFilter(resample);
					trainingBalanced = Filter.useFilter(trainingBalanced, resample);
					positiveInstances = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances);
					
				} catch (Exception e) {
					e.printStackTrace();
					//FileLogger.getLogger().error("Errore nell'instanziazione dell'oversample"); System.exit(1); }
				}
				break;
					
				case 2: //undersampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					entry2.setBalancing(balancingNames.get(i));

					SpreadSubsample spreadSubsample = null;
					int positiveInstances3 = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances3);
					try {
						spreadSubsample = new SpreadSubsample();
						spreadSubsample.setInputFormat(trainingBalanced);
						String[] opts = new String[]{"-M", "1.0"};
						spreadSubsample.setOptions(opts);
						filteredClassifier = new FilteredClassifier();
						filteredClassifier.setClassifier(classifier);
						filteredClassifier.setFilter(spreadSubsample);
						trainingBalanced = Filter.useFilter(trainingBalanced, spreadSubsample);
						positiveInstances3 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances3);
					} 
					
					catch (Exception e) { 
						e.printStackTrace();
						//FileLogger.getLogger().error("Errore nell'instanziazione dell'undersample");
					}
					break;
				
				case 3: //SMOTE
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					entry2.setBalancing(balancingNames.get(i));

					SMOTE smote = null;
					
					try {
						int positiveInstances4 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances4);
						smote = new SMOTE();
						smote.setInputFormat(trainingBalanced);
						filteredClassifier = new FilteredClassifier();
						filteredClassifier.setClassifier(classifier);
						filteredClassifier.setFilter(smote);
						trainingBalanced = Filter.useFilter(trainingBalanced, smote);
						positiveInstances4 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances4);
					} 
					catch (Exception e) { 
						e.printStackTrace();
						//FileLogger.getLogger().error("Errore nell'instanziazione dello SMOTE");
					}
					break;
				
					default:
						logger.log(Level.SEVERE,"Error in feature selection ");
						System.exit(1);
					break;
			}
			//chooseCostSensitive(classifier, classifierName, featureSelectionName, filteredClassifier, balancingNames.get(i) , trainingBalanced,  testingBalanced);
			chooseCostSensitive2(classifier, filteredClassifier, entry2 , trainingBalanced,  testingBalanced);
			//dBentriesList.add(entry2);

		}
		
	}
	
	
	public static void chooseCostSensitive2(AbstractClassifier classifier,FilteredClassifier filteredClassifier,M2Entries entry , Instances training, Instances testing) {
		List<String> costSensitiveNames = Arrays.asList("No cost sensitive", "Sensitive Threshold", "Sensitive Learning");
		
		
		for(int i = 0; i<costSensitiveNames.size(); i++) {
			CostSensitiveClassifier costSensitiveClassifier =null;
			Instances training2 = new Instances(training);
			Instances testing2 = new Instances(testing);
			
			M2Entries entry2 = new M2Entries(NAME_PROJECT);
			entry2.setNumTrainingRelease(entry.getNumTrainingRelease());
			entry2.setClassifierName(entry.getClassifierName());
			entry2.setFeatureSelection(entry.getFeatureSelection());
			entry2.setBalancing(entry.getBalancing());
			entry2.setTrainingPerc(entry.getTrainingPerc());
			entry2.setDefectPercTrain(entry.getDefectPercTrain());
			entry2.setDefectPercTest(entry.getDefectPercTest());



			switch(i) {
				case 0: //No cost sensitive
					//nulla, quindi CostSensitiveClassifier=null
					entry2.setSensitivity(costSensitiveNames.get(i));
					
				break;
				
				case 1: //Sensitive Threshold
					entry2.setSensitivity(costSensitiveNames.get(i));

					costSensitiveClassifier = new CostSensitiveClassifier();

					
					if (filteredClassifier == null){
						costSensitiveClassifier.setClassifier(classifier);
						costSensitiveClassifier.setMinimizeExpectedCost(true);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
					}
					else {
						costSensitiveClassifier.setClassifier(filteredClassifier);
						costSensitiveClassifier.setMinimizeExpectedCost(true);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
						
					}
				break;
					
				case 2: //Sensitive Learning
					entry2.setSensitivity(costSensitiveNames.get(i));

					costSensitiveClassifier = new CostSensitiveClassifier();
					
					if (filteredClassifier == null) {
						costSensitiveClassifier.setClassifier(classifier);
						costSensitiveClassifier.setMinimizeExpectedCost(false);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
						training2 = reweightClasses(training2); // duplico il numero di istanze che hanno come buggyness=yes, ossia i positivi
					}
					else {
						costSensitiveClassifier.setClassifier(filteredClassifier);
						costSensitiveClassifier.setMinimizeExpectedCost(false);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
						training2 = reweightClasses(training2); // duplico il numero di istanze che hanno come buggyness=yes, ossia i positivi
						
					}
					
					break;
	
			}
			//chooseCostSensitive(classifier, classifierName, featureSelectionName, filteredClassifier, balancingName , training,  testing);
			//checkModel(classifier, entry2.getClassifierName(), entry2.getFeatureSelection(), filteredClassifier, entry2.getBalancing() , costSensitiveClassifier,costSensitiveNames.get(i), training2,  testing2);
			checkModel2(classifier, filteredClassifier, entry2 , costSensitiveClassifier, training2,  testing2);
			dBentriesList.add(entry2);
		}
	}
	
	
	public static void checkModel2(AbstractClassifier classifier, FilteredClassifier filteredClassifier, M2Entries entry, 
			CostSensitiveClassifier costSensitiveClassifier ,Instances training, Instances testing) {

		//classifier e' sempre diverso da null
		//filteredClassifier puo' essere = null --> vuol dire che non sto usando balancing
		// costClassifier puo' essere = null --> vuol dire che non sto usando cost sensitive
		String classifierName = entry.getClassifierName();
		String featureSelectionName = entry.getFeatureSelection();
		String balancingName = entry.getBalancing();
		String costSensitiveName = entry.getSensitivity();
		
		System.out.println(classifierName + "--->\t" + featureSelectionName +  "--->\t" + balancingName +  "--->\t" + costSensitiveName );

		Evaluation eval = null;
		try {
			eval = new Evaluation(testing);
		} catch (Exception e1) {
			e1.printStackTrace();
			//FileLogger.getLogger().error("Errore nella inizializzazione dell' Evaluator")
		}
		
		if(costSensitiveClassifier == null) {
			System.out.println("NON STO USANDO COST SENSITIVE");

			//evaluateNoCost(eval, filteredClassifier, classifier, balancingName, training, testing);
			evaluateNoCost2(eval, filteredClassifier, classifier, entry, training, testing);


		}
		else {
			System.out.println("STO USANDO COST SENSITIVE");
			
			evaluateCost2(eval, filteredClassifier, classifier, costSensitiveClassifier, entry, training, testing);


		}
		

		
		System.out.println("\n\n" );

	}
	
	
	public static void evaluateNoCost2(Evaluation eval, FilteredClassifier filteredClassifier, AbstractClassifier classifier, M2Entries entry, Instances training, Instances testing) {

		String balancingName = entry.getBalancing();

		//controllo balancing, quindi filteredClassifier
		if (filteredClassifier == null) {
			System.out.println("NON STO USANDO BALANCING");
			try {
				classifier.buildClassifier(training);
				eval.evaluateModel(classifier, testing);
				addEntryEvaluation(eval,entry);
				
			} catch (Exception e) {
				e.printStackTrace();
				//FileLogger.getLogger().error("Errore nel build del classificatore con filtro");
			}
			
		}
		else {	//sto usando balancing, quindi filteredClassifier
			System.out.println("STO USANDO BALANCING ==  " + balancingName);
			try {
				filteredClassifier.buildClassifier(training);
				eval.evaluateModel(filteredClassifier, testing);
				addEntryEvaluation(eval,entry);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void addEntryEvaluation(Evaluation eval, M2Entries entry) {
		entry.setPrecision(eval.precision(1));
		entry.setRecall(eval.recall(1));
		entry.setAuc(eval.areaUnderROC(1));
		entry.setKappa(eval.kappa());
		System.out.println("AUC = "+eval.areaUnderROC(1));
		System.out.println("kappa = "+eval.kappa());
		System.out.println("precision = "+eval.precision(1));
		System.out.println("recall = "+eval.recall(1));
	}
	
	public static void evaluateCost2(Evaluation eval, FilteredClassifier filteredClassifier, AbstractClassifier classifier, CostSensitiveClassifier costSensitiveClassifier, M2Entries entry, Instances training, Instances testing) {
		String balancingName = entry.getBalancing();

		if (filteredClassifier == null) {
			System.out.println("NON STO USANDO BALANCING");
			try {
				costSensitiveClassifier.buildClassifier(training);
				eval.evaluateModel(costSensitiveClassifier, testing);
				addEntryEvaluation(eval,entry);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		else {
			System.out.println("STO USANDO BALANCING ==  " + balancingName);
			try {
				costSensitiveClassifier.buildClassifier(training);
				eval.evaluateModel(costSensitiveClassifier, testing);
				addEntryEvaluation(eval,entry);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	
	
	public static void chooseBalancing3(AbstractClassifier classifier,M2Entries entry,String featureSelectionName,Instances training, Instances testing) {
		List<String> balancingNames = Arrays.asList("No sampling","oversampling", "undersampling","SMOTE");
		String classifierName = entry.getClassifierName();
		for(int i = 0; i<balancingNames.size(); i++) {
			FilteredClassifier filteredClassifier = null;
			Instances trainingBalanced = new Instances(training);
			Instances testingBalanced = new Instances(testing);


			switch(i) {
				case 0: //No sampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					int positiveInstances2 = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances2);
				break;
				
				case 1: //oversampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );					
					Resample resample = null;
					
				try {
					resample = new Resample();
					resample.setInputFormat(trainingBalanced);
					resample.setNoReplacement(false);
					resample.setBiasToUniformClass(1.0f);
					
					int totInstances = trainingBalanced.size();
					int positiveInstances = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances);
					double percentage =  calculatePercentage(positiveInstances, totInstances);
					//System.out.println("percentage == " + percentage);
					resample.setSampleSizePercent(percentage*2);
					filteredClassifier = new FilteredClassifier();
					filteredClassifier.setClassifier(classifier);
					filteredClassifier.setFilter(resample);
					trainingBalanced = Filter.useFilter(trainingBalanced, resample);
					positiveInstances = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances);
					
				} catch (Exception e) {
					e.printStackTrace();
					//FileLogger.getLogger().error("Errore nell'instanziazione dell'oversample"); System.exit(1); }
				}
				break;
					
				case 2: //undersampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					SpreadSubsample spreadSubsample = null;
					int positiveInstances3 = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances3);
					try {
						spreadSubsample = new SpreadSubsample();
						spreadSubsample.setInputFormat(trainingBalanced);
						String[] opts = new String[]{"-M", "1.0"};
						spreadSubsample.setOptions(opts);
						filteredClassifier = new FilteredClassifier();
						filteredClassifier.setClassifier(classifier);
						filteredClassifier.setFilter(spreadSubsample);
						trainingBalanced = Filter.useFilter(trainingBalanced, spreadSubsample);
						positiveInstances3 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances3);
					} 
					
					catch (Exception e) { 
						e.printStackTrace();
						//FileLogger.getLogger().error("Errore nell'instanziazione dell'undersample");
					}
					break;
				
				case 3: //SMOTE
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					SMOTE smote = null;
					
					try {
						int positiveInstances4 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances4);
						smote = new SMOTE();
						smote.setInputFormat(trainingBalanced);
						filteredClassifier = new FilteredClassifier();
						filteredClassifier.setClassifier(classifier);
						filteredClassifier.setFilter(smote);
						trainingBalanced = Filter.useFilter(trainingBalanced, smote);
						positiveInstances4 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances4);
					} 
					catch (Exception e) { 
						e.printStackTrace();
						//FileLogger.getLogger().error("Errore nell'instanziazione dello SMOTE");
					}
					break;
				
					default:
						logger.log(Level.SEVERE,"Error in feature selection ");
						System.exit(1);
					break;
			}
			chooseCostSensitive(classifier, classifierName, featureSelectionName, filteredClassifier, balancingNames.get(i) , trainingBalanced,  testingBalanced);
			//evaluateModel(classifier, classifierName, featureSelectionName, filteredClassifier, balancingNames.get(i) , training,  testing);
		}
		
	}
	
	public static void applyFeatureSelection2(AbstractClassifier classifier,String classifierName,String featureSelection,Instances training, Instances testing) {
		
		Instances training2 = new Instances(training);
		Instances testing2 = new Instances(testing);
		
		if(!featureSelection.equals("no")) {
			//applico featureSelection
			System.out.println("STO USANDO FEATURE SELECTION");
			
			//create AttributeSelection object
			AttributeSelection filter = new AttributeSelection();
			//create evaluator and search algorithm objects
			CfsSubsetEval eval = new CfsSubsetEval();
			BestFirst  search = new BestFirst ();
			//set the algorithm to search backward
			String [] bfSearchOpt = {"-D", "1",  "-N", "5"};
			try {
				search.setOptions(bfSearchOpt);
				
				//set the filter to use the evaluator and search algorithm
				filter.setEvaluator(eval);
				filter.setSearch(search);
				//specify the dataset
				Instances filteredTraining = null;
				Instances filteredTesting = null;
				
				filter.setInputFormat(training);
				//apply
				filteredTraining = Filter.useFilter(training2, filter);
				filteredTesting = Filter.useFilter(testing2, filter);
				
				int numAttrFiltered = filteredTraining.numAttributes();
				filteredTraining.setClassIndex(numAttrFiltered - 1);
				filteredTesting.setClassIndex(numAttrFiltered - 1);
				
				//lavora con feature selection
				//System.out.println("numAttrFiltered == " + numAttrFiltered);
				//chooseClassifier2(filteredTraining,filteredTesting );
				chooseBalancing2(classifier, classifierName, featureSelection, filteredTraining,  filteredTesting);
				//chooseBalancing(filteredTraining,filteredTesting);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
		else {
			//lavora senza feature selection
			//chooseBalancing(training,testing);
			//chooseClassifier2(training,testing);
			chooseBalancing2(classifier, classifierName, featureSelection, training2,  testing2);


		}
	}
	
	
	public static void chooseBalancing2(AbstractClassifier classifier,String classifierName,String featureSelectionName,Instances training, Instances testing) {
		List<String> balancingNames = Arrays.asList("No sampling","oversampling", "undersampling","SMOTE");

		for(int i = 0; i<balancingNames.size(); i++) {
			FilteredClassifier filteredClassifier = null;
			Instances trainingBalanced = new Instances(training);
			Instances testingBalanced = new Instances(testing);


			switch(i) {
				case 0: //No sampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					int positiveInstances2 = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances2);
				break;
				
				case 1: //oversampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );					
					Resample resample = null;
					
				try {
					resample = new Resample();
					resample.setInputFormat(trainingBalanced);
					resample.setNoReplacement(false);
					resample.setBiasToUniformClass(1.0f);
					
					int totInstances = trainingBalanced.size();
					int positiveInstances = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances);
					double percentage =  calculatePercentage(positiveInstances, totInstances);
					//System.out.println("percentage == " + percentage);
					resample.setSampleSizePercent(percentage*2);
					filteredClassifier = new FilteredClassifier();
					filteredClassifier.setClassifier(classifier);
					filteredClassifier.setFilter(resample);
					trainingBalanced = Filter.useFilter(trainingBalanced, resample);
					positiveInstances = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances);
					
				} catch (Exception e) {
					e.printStackTrace();
					//FileLogger.getLogger().error("Errore nell'instanziazione dell'oversample"); System.exit(1); }
				}
				break;
					
				case 2: //undersampling
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					SpreadSubsample spreadSubsample = null;
					int positiveInstances3 = calculateNumberBuggyClass(trainingBalanced);
					System.out.println("positiveInstances == " + positiveInstances3);
					try {
						spreadSubsample = new SpreadSubsample();
						spreadSubsample.setInputFormat(trainingBalanced);
						String[] opts = new String[]{"-M", "1.0"};
						spreadSubsample.setOptions(opts);
						filteredClassifier = new FilteredClassifier();
						filteredClassifier.setClassifier(classifier);
						filteredClassifier.setFilter(spreadSubsample);
						trainingBalanced = Filter.useFilter(trainingBalanced, spreadSubsample);
						positiveInstances3 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances3);
					} 
					
					catch (Exception e) { 
						e.printStackTrace();
						//FileLogger.getLogger().error("Errore nell'instanziazione dell'undersample");
					}
					break;
				
				case 3: //SMOTE
					System.out.println("IL CLASSIFICATORE CHE STO USANDO E' == " + classifierName );
					System.out.println("LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' == " + featureSelectionName );
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
					SMOTE smote = null;
					
					try {
						int positiveInstances4 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances4);
						smote = new SMOTE();
						smote.setInputFormat(trainingBalanced);
						filteredClassifier = new FilteredClassifier();
						filteredClassifier.setClassifier(classifier);
						filteredClassifier.setFilter(smote);
						trainingBalanced = Filter.useFilter(trainingBalanced, smote);
						positiveInstances4 = calculateNumberBuggyClass(trainingBalanced);
						System.out.println("positiveInstances == " + positiveInstances4);
					} 
					catch (Exception e) { 
						e.printStackTrace();
						//FileLogger.getLogger().error("Errore nell'instanziazione dello SMOTE");
					}
					break;
				
					default:
						logger.log(Level.SEVERE,"Error in feature selection ");
						System.exit(1);
					break;
			}
			chooseCostSensitive(classifier, classifierName, featureSelectionName, filteredClassifier, balancingNames.get(i) , trainingBalanced,  testingBalanced);
			//evaluateModel(classifier, classifierName, featureSelectionName, filteredClassifier, balancingNames.get(i) , training,  testing);
		}
		
	}
	
	
	public static void chooseCostSensitive(AbstractClassifier classifier,String classifierName,String featureSelectionName, FilteredClassifier filteredClassifier, String balancingName, Instances training, Instances testing) {
		List<String> costSensitiveNames = Arrays.asList("No cost sensitive", "Sensitive Threshold", "Sensitive Learning");
		
		
		for(int i = 0; i<costSensitiveNames.size(); i++) {
			CostSensitiveClassifier costSensitiveClassifier =null;
			Instances training2 = new Instances(training);
			Instances testing2 = new Instances(testing);


			switch(i) {
				case 0: //No cost sensitive
					//nulla, quindi CostSensitiveClassifier=null
					
				break;
				
				case 1: //Sensitive Threshold
					costSensitiveClassifier = new CostSensitiveClassifier();
					
					if (filteredClassifier == null){
						costSensitiveClassifier.setClassifier(classifier);
						costSensitiveClassifier.setMinimizeExpectedCost(true);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
					}
					else {
						costSensitiveClassifier.setClassifier(filteredClassifier);
						costSensitiveClassifier.setMinimizeExpectedCost(true);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
						
					}
				break;
					
				case 2: //Sensitive Learning
					costSensitiveClassifier = new CostSensitiveClassifier();
					
					if (filteredClassifier == null) {
						costSensitiveClassifier.setClassifier(classifier);
						costSensitiveClassifier.setMinimizeExpectedCost(false);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
						training2 = reweightClasses(training2); // duplico il numero di istanze che hanno come buggyness=yes, ossia i positivi
					}
					else {
						costSensitiveClassifier.setClassifier(filteredClassifier);
						costSensitiveClassifier.setMinimizeExpectedCost(false);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
						training2 = reweightClasses(training2); // duplico il numero di istanze che hanno come buggyness=yes, ossia i positivi
						
					}
					
					break;
	
			}
			//chooseCostSensitive(classifier, classifierName, featureSelectionName, filteredClassifier, balancingName , training,  testing);
			checkModel(classifier, classifierName, featureSelectionName, filteredClassifier, balancingName , costSensitiveClassifier,costSensitiveNames.get(i), training2,  testing2);
		}
		
		
		
	}
	
	public static Instances reweightClasses(Instances training) {
		
		Instances trainingReweighted = new Instances(training);
		Instances training2 = new Instances(trainingReweighted);

		
		System.out.println("size training == " + training2.size());

		int numAttr = trainingReweighted.numAttributes();
		
		int count = 0;
		
		
		for (Instance instance : trainingReweighted) {
			double target = instance.value(numAttr-1);

			if(target==1.0) {
				training2.add(instance);
				//trainingReweighted.add(instance);
				count++;
			}
			
		}
	
		
		System.out.println("size training == " + training2.size());

		System.out.println("num positivi == " + count);
		//System.out.println("num positivi2 == " + count2);

		return training2;
		
	}

	
	
	public static CostMatrix createCostMatrix(double weightFalsePositive, double weightFalseNegative) {
	    CostMatrix costMatrix = new CostMatrix(2);
	    costMatrix.setCell(0, 0, 0.0);
	    costMatrix.setCell(1, 0, weightFalsePositive);
	    costMatrix.setCell(0, 1, weightFalseNegative);
	    costMatrix.setCell(1, 1, 0.0);
	    return costMatrix;
	}

	
	
	public static void checkModel(AbstractClassifier classifier,String classifierName,String featureSelectionName, FilteredClassifier filteredClassifier, String balancingName, 
			CostSensitiveClassifier costSensitiveClassifier , String costSensitiveName, Instances training, Instances testing) {

		//classifier e' sempre diverso da null
		//filteredClassifier puo' essere = null --> vuol dire che non sto usando balancing
		// costClassifier puo' essere = null --> vuol dire che non sto usando cost sensitive
		System.out.println(classifierName + "--->\t" + featureSelectionName +  "--->\t" + balancingName +  "--->\t" + costSensitiveName );

		Evaluation eval = null;
		try {
			eval = new Evaluation(testing);
		} catch (Exception e1) {
			e1.printStackTrace();
			//FileLogger.getLogger().error("Errore nella inizializzazione dell' Evaluator")
		}
		
		if(costSensitiveClassifier == null) {
			System.out.println("NON STO USANDO COST SENSITIVE");

			evaluateNoCost(eval, filteredClassifier, classifier, balancingName, training, testing);
			

		}
		else {
			System.out.println("STO USANDO COST SENSITIVE");
			
			evaluateCost(eval, filteredClassifier, classifier, costSensitiveClassifier, balancingName, training, testing);


		}
		

		
		System.out.println("\n\n" );

	}

	public static void evaluateNoCost(Evaluation eval, FilteredClassifier filteredClassifier, AbstractClassifier classifier, String balancingName, Instances training, Instances testing) {

		//controllo balancing, quindi filteredClassifier
		if (filteredClassifier == null) {
			System.out.println("NON STO USANDO BALANCING");
			try {
				classifier.buildClassifier(training);
				eval.evaluateModel(classifier, testing);
				
			} catch (Exception e) {
				e.printStackTrace();
				//FileLogger.getLogger().error("Errore nel build del classificatore con filtro");
			}
			
		}
		else {	//sto usando balancing, quindi filteredClassifier
			System.out.println("STO USANDO BALANCING ==  " + balancingName);
			try {
				filteredClassifier.buildClassifier(training);
				eval.evaluateModel(filteredClassifier, testing);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void evaluateCost(Evaluation eval, FilteredClassifier filteredClassifier, AbstractClassifier classifier, CostSensitiveClassifier costSensitiveClassifier, String balancingName, Instances training, Instances testing) {

		if (filteredClassifier == null) {
			System.out.println("NON STO USANDO BALANCING");
			try {
				costSensitiveClassifier.buildClassifier(training);
				eval.evaluateModel(costSensitiveClassifier, testing);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		else {
			System.out.println("STO USANDO BALANCING ==  " + balancingName);
			try {
				costSensitiveClassifier.buildClassifier(training);
				eval.evaluateModel(costSensitiveClassifier, testing);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	public static void prova() {
		AbstractClassifier classifier = null;
		classifier = new RandomForest();
		
		FilteredClassifier filteredClassifier = null;
		filteredClassifier = new FilteredClassifier();
		filteredClassifier.setClassifier(classifier);
		
		CostSensitiveClassifier c1 = new CostSensitiveClassifier();
		c1.setClassifier(filteredClassifier);
		
		
	}
	
	
	public static void chooseBalancing(Instances training,Instances testing) {
		List<String> balancingNames = Arrays.asList("No sampling","oversampling", "undersampling","SMOTE");
		
		
		
		for(int i = 0; i<balancingNames.size(); i++) {
			switch(i) {
				case 0: //No sampling
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i) );
				break;
				
				case 1: //oversampling
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i));		
					Resample resample = null;
					
				try {
					resample = new Resample();
					resample.setInputFormat(training);
					resample.setNoReplacement(false);
					resample.setBiasToUniformClass(1.0);
					
					int totInstances = training.size();
					int positiveInstances = calculateNumberBuggyClass(training);
					System.out.println("positiveInstances == " + positiveInstances);
					double percentage =  calculatePercentage(positiveInstances, totInstances);
					System.out.println("percentage == " + percentage);
					resample.setSampleSizePercent(percentage*2);
					
				} catch (Exception e) {
					e.printStackTrace();
					//FileLogger.getLogger().error("Errore nell'instanziazione dell'oversample"); System.exit(1); }

				}
					
					
					/*
					int totInstances = training.size();
					int positiveInstances = calculateNumberBuggyClass(training);
					System.out.println("positiveInstances == " + positiveInstances);
					
					
					
					double percentage =  calculatePercentage(positiveInstances, totInstances);
					
					String[] optsOverSampling = new String[]{"-B", "1.0", "-Z", String.valueOf(2*percentage)};
					
					resample.setOptions(optsOverSampling);
					
					fc.setFilter(resample);
					*/
					break;
					
				case 2: //undersampling
					System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i));		
				break;
				
				case 3: //SMOTE
						System.out.println("LA TECNICA DI BALANCING CHE STO USANDO E' == " + balancingNames.get(i));		
				break;
				
				default:
					logger.log(Level.SEVERE,"Error in feature selection ");
					System.exit(1);
				break;
			}
			
		}
	}

	
	private static double getMinorityPercentage(int size, int defects) {
		int minority = defects;
		if (size - defects < defects)
			minority = size-defects;
		
		return (double) minority/size * 100;
	}
	

	
	public static double calculatePercentage(int positiveInstances, int totInstances) {
		
		double percentage = 0;
		
		if(positiveInstances > (totInstances-positiveInstances)) {
			
			percentage = (positiveInstances*100)/(double) totInstances;
		}else {
			
			percentage = ((totInstances-positiveInstances)*100)/(double) totInstances;
		}
		
		return percentage;
	}
	
	
	public static int calculateNumberBuggyClass(Instances training) {
			
			int sizeTraining = training.size();
			int positiveInstances = 0;
			
			for(int d=0;d<sizeTraining;d++) {
				if(training.get(d).toString(training.numAttributes()-1).equals("Yes")) {
					
					positiveInstances++;
				}
			}
			return positiveInstances;
		}
	
	
	



		
		
	public static void classification(Instances training, Instances testing, DBEntriesM2 entry ) {
		
		Map<String, List<Double>> classifierMap = new HashMap<>();
		RandomForest classifier = new RandomForest();
		NaiveBayes classifier2 = new NaiveBayes();
		IBk classifier3 = new IBk();
		
		try {
			
			// RandomForest
			System.out.println(" \n\nRandomForest \n");
			classifier.buildClassifier(training);
			Evaluation eval = new Evaluation(testing);	
			eval.evaluateModel(classifier, testing); 
			System.out.println("AUC = "+eval.areaUnderROC(1));
			System.out.println("kappa = "+eval.kappa());
			System.out.println("precision = "+eval.precision(1));
			System.out.println("recall = "+eval.recall(1));
			List<Double> listRF = new ArrayList<>();
			listRF.add(eval.precision(1));
			listRF.add(eval.recall(1));
			listRF.add(eval.areaUnderROC(1));
			listRF.add(eval.kappa());
			System.out.println("listRF == " + listRF);
			classifierMap.put("RandomForest", listRF);
			

			
			
			// NaiveBayes
			System.out.println("\n\nNaiveBayes \n");
			classifier2.buildClassifier(training);
			Evaluation eval2 = new Evaluation(testing);	
			eval2.evaluateModel(classifier2, testing); 
			System.out.println("AUC = "+eval2.areaUnderROC(1));
			System.out.println("kappa = "+eval2.kappa());
			System.out.println("precision = "+eval2.precision(1));
			System.out.println("recall = "+eval2.recall(1));
			List<Double> listNB = new ArrayList<>();
			listNB.add(eval2.precision(1));
			listNB.add(eval2.recall(1));
			listNB.add(eval2.areaUnderROC(1));
			listNB.add(eval2.kappa());
			System.out.println("listNB == " + listNB);
			classifierMap.put("NaiveBayes", listNB);
			
			// IBk
			System.out.println(" \n\nIBk \n");
			classifier3.buildClassifier(training);
			Evaluation eval3 = new Evaluation(testing);	
			eval3.evaluateModel(classifier3, testing); 
			System.out.println("AUC = "+eval3.areaUnderROC(1));
			System.out.println("kappa = "+eval3.kappa());
			System.out.println("precision = "+eval3.precision(1));
			System.out.println("recall = "+eval3.recall(1));
			List<Double> listIBk = new ArrayList<>();
			listIBk.add(eval3.precision(1));
			listIBk.add(eval3.recall(1));
			listIBk.add(eval3.areaUnderROC(1));
			listIBk.add(eval3.kappa());
			System.out.println("listIBk == " + listIBk);
			classifierMap.put("IBk", listIBk);
			entry.setClassifier(classifierMap);


		} catch (Exception e) {
			// TODO Auto-generated catch block
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
