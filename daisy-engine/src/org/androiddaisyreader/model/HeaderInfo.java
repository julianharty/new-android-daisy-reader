package org.androiddaisyreader.model;

import java.util.ArrayList;

public class HeaderInfo {

	private String name;
	private ArrayList<DetailInfo> bookList = new ArrayList<DetailInfo>();;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<DetailInfo> getBookList() {
		return bookList;
	}

	public void setBookList(ArrayList<DetailInfo> bookList) {
		this.bookList = bookList;
	}

}
