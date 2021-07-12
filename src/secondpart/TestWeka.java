package secondpart;
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


import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;

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
import weka.classifiers.lazy.IBk;
import entities.M2Entries;
import entities.Release;
import firstpart.CSVWriter;
import firstpart.GetJIRAInfo;
import firstpart.Utils;
import weka.filters.supervised.instance.SpreadSubsample;


public class TestWeka{
	
	private static List<M2Entries> dBentriesList;
	private static final String NAME_PROJECT = "BOOKKEEPER";
	static Logger logger = Logger.getLogger(TestWeka.class.getName());
	private static final String ERROR_CLASSIFIER = "Error in building classifier";

	
	public static void main(String[] args) throws Exception{
		
		//load datasets
		List<Release> releasesList = GetJIRAInfo.getListRelease(NAME_PROJECT);
		removeHalf(releasesList);
		dBentriesList = new ArrayList<>();
		
		String arffPath = "D:"+"\\Programmi\\Eclipse\\eclipse-workspace\\ISW2_21-Deliverable2_BOOKKEEPER\\csv\\FINITO\\CSV FINALE "+ NAME_PROJECT +".arff";

		walkForward(arffPath, releasesList);
		CSVWriter.writeCsvMilestone2(dBentriesList,NAME_PROJECT);		
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
		/**
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
			int numTrain = k-1;	//# RELEASE TRAINING SET 
			//numTest = k-(k-1)
			entry.setNumTrainingRelease(numTrain);
			
	
			// # train e' k-1
			// # test e' k-(k-1)

			int m;
			
			//training set
			training = new Instances(instancesList.get(0));
			
			for (m = 1; m<numTrain; m++) {
				for (Instance i : instancesList.get(m)) {
					training.add(i);
				}
			}
			
	
			//test set
			testing = instancesList.get(m);
			int numAttr = training.numAttributes();
			training.setClassIndex(numAttr - 1);
			testing.setClassIndex(numAttr - 1);
			
			
			float percTrain = Utils.calculatePercentage(training.size(), datasetDimension);	//%TRAINING
			entry.setTrainingPerc(percTrain);
			
			int positiveInstancesTrain = calculateNumberBuggyClass(training);
			
			float percDefectTrain = Utils.calculatePercentage(positiveInstancesTrain, training.size());
			entry.setDefectPercTrain(percDefectTrain);

			int positiveInstancesTest = calculateNumberBuggyClass(testing);
			float percDefectTest = Utils.calculatePercentage(positiveInstancesTest, testing.size());
			entry.setDefectPercTest(percDefectTest);

			// numAttrTrainingNoFilter = training.numAttributes()
			
			chooseClassifier(classifierNames, training, testing, entry);
			
		}
		
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error in walkForward");
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
					// LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' =  featureSelectionNames.get(i))
					entry2.setFeatureSelection(featureSelectionNames.get(i));
				break;
				
				case 1: //Best first
					// LA TECNICA DI FEATURE SELECTION CHE STO USANDO E' =  featureSelectionNames.get(i))
					entry2.setFeatureSelection(featureSelectionNames.get(i));

					break;
	
				default:
					logger.log(Level.SEVERE,"Error in feature selection ");
					System.exit(1);
				break;
			}
			applyFeatureSelection(classifier, entry2, training2, testing2);
			
		}
		
	}
	
	
	
	
	public static void applyFeatureSelection(AbstractClassifier classifier,M2Entries entry,Instances training, Instances testing) {
		
		Instances training2 = new Instances(training);
		Instances testing2 = new Instances(testing);
		String featureSelection = entry.getFeatureSelection();
		
		if(!featureSelection.equals("No")) {
			//STO USANDO FEATURE SELECTION
			
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
				chooseBalancing(classifier, entry, filteredTraining,  filteredTesting);				
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,"Error in applying feature selection ");
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
					//LA TECNICA DI BALANCING CHE STO USANDO E' = + balancingNames.get(i) 
					entry2.setBalancing(balancingNames.get(i));
				break;
				
				case 1: //oversampling
					//LA TECNICA DI BALANCING CHE STO USANDO E' = + balancingNames.get(i) 	
					entry2.setBalancing(balancingNames.get(i));

					Resample resample = null;
					
				try {
					resample = new Resample();
					resample.setInputFormat(trainingBalanced);
					resample.setNoReplacement(false);
					resample.setBiasToUniformClass(1.0f);
					
					int totInstances = trainingBalanced.size();
					int positiveInstances = calculateNumberBuggyClass(trainingBalanced);
					double percentage =  calculatePercentage(positiveInstances, totInstances);
					resample.setSampleSizePercent(percentage*2);
					filteredClassifier = new FilteredClassifier();
					filteredClassifier.setClassifier(classifier);
					filteredClassifier.setFilter(resample);
					trainingBalanced = Filter.useFilter(trainingBalanced, resample);
					
				} catch (Exception e) {
					logger.log(Level.SEVERE,"Error in oversampling");
					e.printStackTrace();
				}
				break;
					
				case 2: //undersampling
					//LA TECNICA DI BALANCING CHE STO USANDO E' = + balancingNames.get(i) 

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
						logger.log(Level.SEVERE,"Error in undersampling");
						e.printStackTrace();
					}
					break;
				
				case 3: //SMOTE
					//LA TECNICA DI BALANCING CHE STO USANDO E' = + balancingNames.get(i) 

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
						logger.log(Level.SEVERE,"Error in SMOTE");
						e.printStackTrace();
					}
					break;
				
					default:
						logger.log(Level.SEVERE,"Error in balancing ");
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
					}
					else {
						costSensitiveClassifier.setClassifier(filteredClassifier);
						costSensitiveClassifier.setMinimizeExpectedCost(false);
						costSensitiveClassifier.setCostMatrix(createCostMatrix(1.0,10.0));
					}
					
					break;
					
					default:
						logger.log(Level.SEVERE,"Error in cost sensitive ");
						System.exit(1);
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
		
		Evaluation eval = null;
		try {
			eval = new Evaluation(testing);
			if(costSensitiveClassifier == null) {
				// NON STO USANDO COST SENSITIVE

				evaluateNoCost(eval, filteredClassifier, classifier, entry, training, testing);


			}
			else {
				// STO USANDO COST SENSITIVE
				evaluateWithCost(eval, costSensitiveClassifier, entry, training, testing);
				
			}
		} catch (Exception e1) {
			logger.log(Level.SEVERE,"Error in initializing the evaluator");
			e1.printStackTrace();
		}
		
		
	}
	
	
	public static void evaluateNoCost(Evaluation eval, FilteredClassifier filteredClassifier, AbstractClassifier classifier, M2Entries entry, Instances training, Instances testing) {

		//controllo balancing, quindi filteredClassifier
		if (filteredClassifier == null) {
			// NON STO USANDO BALANCING
			try {
				classifier.buildClassifier(training);
				eval.evaluateModel(classifier, testing);
				addEntryEvaluation(eval,entry);
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,ERROR_CLASSIFIER);

				e.printStackTrace();
			}
			
		}
		else {	//sto usando balancing, quindi filteredClassifier
			//STO USANDO BALANCING
			try {
				filteredClassifier.buildClassifier(training);
				eval.evaluateModel(filteredClassifier, testing);
				addEntryEvaluation(eval,entry);
			} catch (Exception e) {
				logger.log(Level.SEVERE,ERROR_CLASSIFIER);
				e.printStackTrace();
			}
		}
	}
	
	public static void evaluateWithCost(Evaluation eval, CostSensitiveClassifier costSensitiveClassifier, M2Entries entry, Instances training, Instances testing) {
		
		try {
			costSensitiveClassifier.buildClassifier(training);
			eval.evaluateModel(costSensitiveClassifier, testing);
			addEntryEvaluation(eval,entry);

		} catch (Exception e) {
			logger.log(Level.SEVERE,ERROR_CLASSIFIER);

			e.printStackTrace();
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
