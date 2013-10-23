package org.androiddaisyreader.model;

public class XmlModel {
    private String id;
    private String text;
    private int level;
    private String smilHref;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSmilHref() {
        return smilHref;
    }

    public void setSmilHref(String smilHref) {
        this.smilHref = smilHref;
    }

    public XmlModel(String id, String smilHref, String text, int level) {
        this.id = id;
        this.smilHref = smilHref;
        this.text = text;
        this.level = level;
    }

    public XmlModel() {
    }

}
