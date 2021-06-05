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
import deliverable.Utils;
import entities.M2Entries;
import entities.Release;
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


public class TestWeka{
	
	private static List<M2Entries> dBentriesList;
	private static final String NAME_PROJECT = "BOOKKEEPER";
	static Logger logger = Logger.getLogger(TestWeka.class.getName());

	
	public static void main(String args[]) throws Exception{
		
		//load datasets
		List<Release> releasesList = GetJIRAInfo.getListRelease(NAME_PROJECT);
		removeHalf(releasesList);
		dBentriesList = new ArrayList<>();
		
		String arffPath = "D:"+"\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\FINITO\\CSV FINALE "+ NAME_PROJECT +".arff";

		walkForward(arffPath, releasesList);
		CSVWriter.writeCsvMilestone2(dBentriesList,NAME_PROJECT);
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
		/*
		 * Rimuovo la seconda meta' delle release.
		 * 
		 * @param releasesList	lista di release prese da JIRA
		 */
		
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
	
	
	

	public static void walkForward(String arffPath, List<Release> releasesList) {
		DataSource source;
		List<Instances> instancesList = new ArrayList<>();
		List<String> classifierNames = Arrays.asList("Random Forest", "IBk", "Naive Bayes");
	
	
		try {
			source = new DataSource(arffPath);
			Instances dataset = source.getDataSet();
		    int datasetDimension = dataset.size();
	
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
		
		
		/* ora instancesList contiene la lista di istanze, ossia tutte le righe che hanno come release1, poi 2, poi 3...
		 * devo prendermi 2 release e mettere 1 come train e 2 come test
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
			
			//training set
			training = new Instances(instancesList.get(0));
			
			for (m = 1; m<numTrain; m++) {
				//System.out.println(instancesList.get(m));
				for (Instance i : instancesList.get(m)) {
					training.add(i);
				}
			}
			
	
			//test set
			testing = instancesList.get(m);
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
			
			chooseClassifier(classifierNames, training, testing, entry);
	
			
			System.out.println("####################################################################################\n\n");
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	public static void chooseClassifier(List<String> classifierNames, Instances training, Instances testing, M2Entries entry) {
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
			chooseFeatureSelection(classifier,entry2, training2, testing2);
		}
	}

	
	public static void chooseFeatureSelection(AbstractClassifier classifier,M2Entries entry, Instances training, Instances testing ) {
		List<String> featureSelectionNames = Arrays.asList("No", "Best first");
		
		
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
			applyFeatureSelection(classifier, entry2, training2, testing2);
			
		}
		System.out.println("----\n\n");
		
	}
	
	
	
	
	public static void applyFeatureSelection(AbstractClassifier classifier,M2Entries entry,Instances training, Instances testing) {
		
		Instances training2 = new Instances(training);
		Instances testing2 = new Instances(testing);
		String featureSelection = entry.getFeatureSelection();
		
		if(!featureSelection.equals("No")) {
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
				
				chooseBalancing(classifier, entry, filteredTraining,  filteredTesting);				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
		else {
			//lavora senza feature selection
			chooseBalancing(classifier, entry, training2,  testing2);



		}
	}
	
	
	public static void chooseBalancing(AbstractClassifier classifier,M2Entries entry, Instances training, Instances testing) {
		
		List<String> balancingNames = Arrays.asList("No","oversampling", "undersampling","SMOTE");
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
					try {
						spreadSubsample = new SpreadSubsample();
						spreadSubsample.setInputFormat(trainingBalanced);
						String[] opts = new String[]{"-M", "1.0"};
						spreadSubsample.setOptions(opts);
						filteredClassifier = new FilteredClassifier();
						filteredClassifier.setClassifier(classifier);
						filteredClassifier.setFilter(spreadSubsample);
						trainingBalanced = Filter.useFilter(trainingBalanced, spreadSubsample);
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
					
						smote = new SMOTE();
						smote.setInputFormat(trainingBalanced);
						filteredClassifier = new FilteredClassifier();
						filteredClassifier.setClassifier(classifier);
						filteredClassifier.setFilter(smote);
						trainingBalanced = Filter.useFilter(trainingBalanced, smote);
						
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
			chooseCostSensitive(classifier, filteredClassifier, entry2 , trainingBalanced,  testingBalanced);
		}
	}
	
	
	public static void chooseCostSensitive(AbstractClassifier classifier,FilteredClassifier filteredClassifier,M2Entries entry , Instances training, Instances testing) {
		List<String> costSensitiveNames = Arrays.asList("No", "Sensitive Threshold", "Sensitive Learning");
		
		
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
			checkModel(classifier, filteredClassifier, entry2 , costSensitiveClassifier, training2,  testing2);
			dBentriesList.add(entry2);
		}
	}
	
	
	public static void checkModel(AbstractClassifier classifier, FilteredClassifier filteredClassifier, M2Entries entry, 
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

			evaluateNoCost(eval, filteredClassifier, classifier, entry, training, testing);


		}
		else {
			System.out.println("STO USANDO COST SENSITIVE");
			
			evaluateCost(eval, filteredClassifier, classifier, costSensitiveClassifier, entry, training, testing);
		}
		System.out.println("\n\n" );
	}
	
	
	public static void evaluateNoCost(Evaluation eval, FilteredClassifier filteredClassifier, AbstractClassifier classifier, M2Entries entry, Instances training, Instances testing) {

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
		entry.setTP((int)eval.numTruePositives(1));
		entry.setTN((int)eval.numTrueNegatives(1));
		entry.setFP((int)eval.numFalsePositives(1));
		entry.setFN((int)eval.numFalseNegatives(1));

		
		System.out.println("AUC = "+eval.areaUnderROC(1));
		System.out.println("kappa = "+eval.kappa());
		System.out.println("precision = "+eval.precision(1));
		System.out.println("recall = "+eval.recall(1));
		System.out.println("numTruePositives = "+eval.numTruePositives(1));
		System.out.println("numTruePositives = "+(int)eval.numTruePositives(1));

		System.out.println("numTrueNegatives = "+eval.numTrueNegatives(1));
		System.out.println("numFalsePositives = "+eval.numFalsePositives(1));
		System.out.println("numFalseNegatives = "+eval.numFalseNegatives(1));


	}
	
	public static void evaluateCost(Evaluation eval, FilteredClassifier filteredClassifier, AbstractClassifier classifier, CostSensitiveClassifier costSensitiveClassifier, M2Entries entry, Instances training, Instances testing) {
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
				count++;
			}
			
		}
	
		
		System.out.println("size training == " + training2.size());

		System.out.println("num positivi == " + count);

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

	

	
	public static double calculatePercentage(int positiveInstances, int totInstances) {
		
		double percentage = 0;
		
		if(positiveInstances > (totInstances-positiveInstances)) {
			
			percentage = (positiveInstances*100)/(double) totInstances;
		}else {
			
			percentage = ((totInstances-positiveInstances)*100)/(double) totInstances;
		}
		
		return percentage;
	}
	
	
	public static int calculateNumberBuggyClass(Instances dataset) {
		/*
		 * Calcola il numero di classi buggy (quindi aventi "Yes" nella colonna buggyness) nel dataset.
		 * 
		 * @param dataset	dataset del quale si vuole calcolare il numero di classi buggy
		 * @return 			il numero di classi buggy nel dataset
		 */
		int sizeTraining = dataset.size();
		int positiveInstances = 0;
			
		for(int d=0;d<sizeTraining;d++) {
			if(dataset.get(d).toString(dataset.numAttributes()-1).equals("Yes")) {
				
				positiveInstances++;
			}
		}
		return positiveInstances;
	}
	

}
