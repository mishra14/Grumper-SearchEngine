package edu.upenn.cis455.project.bean;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Rank {

	@PrimaryKey
	private String url;
	private Float rank;

	public Rank() {
		super();
	}

	public Rank(String url, float rank) {
		this.url = url;
		this.rank = rank;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Float getRank() {
		return rank;
	}

	public void setRank(Float rank) {
		this.rank = rank;
	}

	@Override
	public String toString() {
		return "Rank [url=" + url + ", rank=" + rank + "]";
	}

}
