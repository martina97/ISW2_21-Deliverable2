package deliverable;

import java.time.LocalDateTime;


public class Release {

	private Integer index;
	private LocalDateTime date;
	private String rel;


	public Release(Integer index, LocalDateTime date, String release) {

		this.index = index;
		this.date = date;
		this.rel = release;

	}

	public String getRelease() {
		return rel;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public Integer getIndex() {
		return index;
	}


	public void setRelease(String release) {
		this.rel = release;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public void setIndec(Integer index) {
		this.index = index;
	}


}