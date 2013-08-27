package org.androiddaisyreader.model;

public class DaisyBookInfo {
	private String id;
	private String title;
	private String path;
	private String author;
	private String publisher;
	private String date;
	private int sort;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public DaisyBookInfo(String id, String title, String path, String author, String publisher, String date, int sort) {
		this.id = id;
		this.title = title;
		this.path = path;
		this.author = author;
		this.publisher = publisher;
		this.sort = sort;
		this.date = date;
	}

	public DaisyBookInfo() {
	}


}
