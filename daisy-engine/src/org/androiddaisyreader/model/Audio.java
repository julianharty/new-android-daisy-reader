package org.androiddaisyreader.model;

/**
 * Audio represents an audio segment used in a DAISY book.
 * 
 * A single file can contain several audio segments, so the starting and
 * ending offsets need to be provided.
 * 
 * @author Julian Harty
 */
public class Audio {

	private String audioFilename;
	private int clipBegin;
	private int clipEnd;
	private String id;

	/**
	 * Create a new Audio segment using the supplied parameters.
	 * @param id The unique identifier for the element.
	 * @param audioFilename the filename that contains the recorded audio.
	 * @param clipBegin the offset in milliseconds for the start of this segment.
	 * @param clipEnd the offset in milliseconds for the end of this segment.
	 */
	public Audio(String id, String audioFilename, int clipBegin, int clipEnd) {
		this.id = id;
		this.audioFilename = audioFilename;
		this.clipBegin = clipBegin;
		this.clipEnd = clipEnd;
	}
	
	/**
	 * @return the name of the file containing the recorded audio.
	 */
	public String getAudioFilename() {
		return audioFilename;
	}
	
	/**
	 * @return the starting offset in milliseconds.
	 */
	public int getClipBegin() {
		return clipBegin;
	}
	
	/**
	 * @return the ending offset in milliseconds.
	 */
	public int getClipEnd() {
		return clipEnd;
	}
	
	/**
	 * @return the unique identifier used to externally identify this audio
	 * segment.
	 */
	public String getId() {
		return id;
	}
}
