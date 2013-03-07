package org.androiddaisyreader.model;

public class RecentBooks {
	private String name;
	private String path;
	private int sort;
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getPath(){
		return path;
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public int getSort(){
		return sort;
	}
	
	public void setSort(int sort){
		this.sort = sort;
	}
	
	public RecentBooks(String name, String path, int sort)
	{
		this.name = name;
		this.path = path;
		this.sort = sort;
	}
	
}
