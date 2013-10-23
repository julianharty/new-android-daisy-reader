package org.androiddaisyreader.model;

import java.util.ArrayList;
import java.util.List;

public class HeaderInfo {

    private String name;
    private List<DetailInfo> bookList = new ArrayList<DetailInfo>();;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DetailInfo> getBookList() {
        return bookList;
    }

    public void setBookList(List<DetailInfo> bookList) {
        this.bookList = bookList;
    }

}
