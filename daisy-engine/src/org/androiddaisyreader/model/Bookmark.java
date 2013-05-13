package org.androiddaisyreader.model;

public class Bookmark {
	private String path;
	private String text;
	private int time;
	private int section;
	private String id;
	private int sort;
	private String textShow;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}
	
	public String getTextShow() {
		return textShow;
	}

	public void setTextShow(String textShow) {
		this.textShow = textShow;
	}

	public Bookmark(String path, String text, int time, int section, int sort, String id) {
		this.path = path;
		this.text = text;
		this.time = time;
		this.section = section;
		this.sort = sort;
		this.id = id;
	}
	
	public Bookmark()
	{
		
	}
}
