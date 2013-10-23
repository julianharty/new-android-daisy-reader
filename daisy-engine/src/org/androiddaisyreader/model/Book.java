package org.androiddaisyreader.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Book implements Navigable {
    // meta data
    protected String title;
    protected String author;
    protected List<Section> sections = new ArrayList<Section>();

    // Added by Logigear to resolve case: the daisy book is not audio.
    // Date: Jun-13-2013
    protected String totalTime;
    protected String publisher;

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public boolean hasAuthor() {
        return author != null;
    }

    // Added by Logigear to resolve case: the daisy book is not audio.
    // Date: Jun-13-2013
    public boolean hasTitle() {
        return title != null;
    }

    public boolean hasTotalTime() {
        return !totalTime.equals("0:00:00");
    }

    public String getPublisher() {
        return publisher;
    }
}
