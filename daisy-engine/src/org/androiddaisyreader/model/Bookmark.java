package org.androiddaisyreader.model;

public class Bookmark {
	private String book;
	private String text;
	private int time;
	private int section;
	private String id;
	private int position;
	private String textShow;

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
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
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public String getTextShow() {
		return textShow;
	}

	public void setTextShow(String textShow) {
		this.textShow = textShow;
	}

	public Bookmark(String book, String text, int time, int section, int position, String id) {
		this.book = book;
		this.text = text;
		this.time = time;
		this.section = section;
		this.position = position;
		this.id = id;
	}
	
	public Bookmark()
	{
		
	}
}
