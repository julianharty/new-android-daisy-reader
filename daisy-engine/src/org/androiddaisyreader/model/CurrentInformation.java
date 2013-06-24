package org.androiddaisyreader.model;

public class CurrentInformation {
	private String path;
	private int section;
	private int time;
	private boolean playing;
	private int sentence;
	private String activity;
	private String id;
	private boolean firstNext;
	private boolean firstPrevious;
	private boolean atTheEnd;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public boolean getPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public int getSentence() {
		return sentence;
	}

	public void setSentence(int sentence) {
		this.sentence = sentence;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public boolean getFirstNext() {
		return firstNext;
	}

	public void setFirstNext(boolean firstNext) {
		this.firstNext = firstNext;
	}

	public boolean getFirstPrevious() {
		return firstPrevious;
	}

	public void setFirstPrevious(boolean firstPrevious) {
		this.firstPrevious = firstPrevious;
	}

	public boolean getAtTheEnd() {
		return atTheEnd;
	}

	public void setAtTheEnd(boolean atTheEnd) {
		this.atTheEnd = atTheEnd;
	}

	public CurrentInformation(String path, int section, int time, boolean playing, int sentence,
			String activity, String id, boolean firstNext, boolean firstPrevious, boolean atTheEnd) {
		this.path = path;
		this.section = section;
		this.time = time;
		this.playing = playing;
		this.sentence = sentence;
		this.activity = activity;
		this.id = id;
		this.firstNext = firstNext;
		this.firstPrevious = firstPrevious;
		this.atTheEnd = atTheEnd;
	}

	public CurrentInformation() {
	}

}
