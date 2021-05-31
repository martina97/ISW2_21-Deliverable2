package entities;

import java.util.List;
import java.util.Map;

public class M2Entries {

		private String datasetName;
		private Integer numTrainingRelease;
		private String classifierName;
		private String featureSelection;
		private String balancing;

		private Integer precision;
		private Integer recall;
		private Integer auc;
		private Integer kappa;

		public M2Entries(String name) {
			this.datasetName = name;
		}
		
		// get
		public String getDatasetName() {
			return datasetName;
		}
		public String getClassifierName() {
			return classifierName;
		}
		public String getFeatureSelection() {
			return featureSelection;
		}
		public String getBalancing() {
			return balancing;
		}
		public Integer getNumTrainingRelease() {
			return numTrainingRelease;
		}
		
		
		
		public Integer getPrecision() {
			return precision;
		}
		public Integer getRecall() {
			return recall;
		}
		public Integer getAuc() {
			return auc;
		}
		public Integer getKappa() {
			return kappa;
		}
		
		
		//set
		public void setDatasetName(String datasetName) {
			this.datasetName = datasetName;
		}
		public void setClassifierName(String classifierName) {
			this.classifierName = classifierName;
		}
		public void setFeatureSelection(String featureSelection) {
			this.featureSelection = featureSelection;
		}
		public void setBalancing(String balancing) {
			this.balancing = balancing;
		}
		public void setNumTrainingRelease(Integer numTrainingRelease) {
			this.numTrainingRelease = numTrainingRelease;
		}
	
		public void setPrecision(Integer precision) {
			this.precision = precision;
		}
		public void setRecall(Integer recall) {
			this.recall = recall;
		}
		public void setAuc(Integer auc) {
			this.auc = auc;
		}
		public void setKappa(Integer kappa) {
			this.kappa = kappa;
		}
	}


