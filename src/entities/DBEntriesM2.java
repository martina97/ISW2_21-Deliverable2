package entities;

import java.util.List;
import java.util.Map;

public class DBEntriesM2 {
	
	private String datasetName;
	private Integer numTrainingRelease;
	private Map<String, List<Double>> classifier;
	private Integer precision;
	private Integer recall;
	private Integer auc;
	private Integer kappa;

	public DBEntriesM2(String name) {
		this.datasetName = name;
	}
	
	// get
	public String getDatasetName() {
		return datasetName;
	}
	
	public Integer getNumTrainingRelease() {
		return numTrainingRelease;
	}
	
	public Map<String, List<Double>> getClassifier() {
		return classifier;
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
	public void setNumTrainingRelease(Integer numTrainingRelease) {
		this.numTrainingRelease = numTrainingRelease;
	}
	public void setClassifier(Map<String, List<Double>> classifier) {
		this.classifier = classifier;
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
