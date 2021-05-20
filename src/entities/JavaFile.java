package entities;

import java.util.List;

public class JavaFile {

	private String name;
	// private HashMap<Integer,String> fileMap = new HashMap<>(); //key = release,
	// value = buggyness (yes/no)
	private List<String> oldPaths;

	private String buggyness;
	private Integer size; // LOC
	private Integer locTouched;
	private Integer nr;
	private Integer nFix;
	private List<String> NAuth;
	private Integer locAdded;
	private Integer maxLocAdded;
	private Integer avgLocAdded;
	private Integer churn;
	private Integer maxChurn;
	private Integer avgChurn;
	private Integer chgSetSize;
	private Integer maxChgSetSize;
	private Integer avgChgSetSize;

	public JavaFile(String name) {
		this.name = name;
	}

	// get
	public String getName() {
		return name;
	}

	public String getBugg() {
		return buggyness;
	}

	public List<String> getoldPaths() {
		return oldPaths;
	}

	public Integer getSize() {
		return size;
	}

	public Integer getLOCtouched() {
		return locTouched;
	}

	public Integer getLOCadded() {
		return locAdded;
	}

	public Integer getChurn() {
		return churn;
	}

	public Integer getChgSetSize() {
		return chgSetSize;
	}

	public Integer getMaxlocAdded() {
		return maxLocAdded;
	}

	public Integer getAvglocAdded() {
		return avgLocAdded;
	}

	public Integer getNr() {
		return nr;
	}

	public Integer getMaxChgSetSize() {
		return maxChgSetSize;
	}

	public Integer getAvgChgSetSize() {
		return avgChgSetSize;
	}

	// set
	public void setName(String name) {
		this.name = name;
	}

	public void setBugg(String buggyness) {
		this.buggyness = buggyness;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public void setLOCadded(Integer locAdded) {
		this.locAdded = locAdded;
	}

	public void setLOCtouched(Integer locTouched) {
		this.locTouched = locTouched;
	}

	public void setChurn(Integer churn) {
		this.churn = churn;
	}

	public void setChgSetSize(Integer chgSetSize) {
		this.chgSetSize = chgSetSize;
	}

	public void setNr(Integer nr) {
		this.nr = nr;
	}

	public void setMaxLocAdded(Integer maxLOCAdded) {
		this.maxLocAdded = maxLOCAdded;
	}

	public void setAvgLOCAdded(Integer avgLOCAdded) {
		this.avgLocAdded = avgLOCAdded;
	}

	public void setOldPaths(List<String> oldPaths) {
		this.oldPaths = oldPaths;
	}

	public void setMaxChgSetSize(Integer maxChgSetSize) {
		this.maxChgSetSize = maxChgSetSize;
	}

	public void setAvgChgSetSize(Integer avgChgSetSize) {
		this.avgChgSetSize = avgChgSetSize;
	}

	public Integer getnFix() {
		return nFix;
	}

	public void setnFix(Integer nFix) {
		this.nFix = nFix;
	}

	public List<String> getNAuth() {
		return NAuth;
	}

	public void setNAuth(List<String> NAuth) {
		this.NAuth = NAuth;
	}

	public Integer getMAXChurn() {
		return maxChurn;
	}

	public void setMAXChurn(Integer maxChurn) {
		this.maxChurn = maxChurn;
	}

	public Integer getAVGChurn() {
		return avgChurn;
	}

	public void setAVGChurn(Integer aVGChurn) {
		this.avgChurn = aVGChurn;
	}
}