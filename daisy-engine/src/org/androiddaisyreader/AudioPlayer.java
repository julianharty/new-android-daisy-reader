package org.androiddaisyreader;

import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.AudioPlayerState;

public interface AudioPlayer extends VolumeControlListener {
	
	/**
	 * Add a callback listener.
	 * 
	 * Each callback listener will be called when the audio player completes.
	 * 
	 * @param listener
	 */
	public void addCallbackListener(AudioCallbackListener listener);

	/**
	 * Get details of the current Audio segment.
	 * 
	 * @return the Audio object containing details of the current segment.
	 */
	public Audio getCurrentSegment();
	
	/**
	 * Get the internal state of the player.
	 * 
	 * This method is primarily to help with testing the audio player.
	 * 
	 * @return the state of the model representing the audio player.
	 */
	public AudioPlayerState getInternalPlayerState();

	/**
	 * Start playing the audio.
	 */
	public void play();
	
	/**
	 * Seek To a new location in the current audio file.
	 * 
	 * @param newTimeInMilliseconds The new time offset to use.
	 */
	public void seekTo(int newTimeInMilliseconds);
	
	/**
	 * Sets the player to use this audio segment.
	 * 
	 * @param audioSegment we want the player to play.
	 */
	public void setCurrentSegment(Audio audioSegment);

	/**
	 * Set the internal player state. 
	 * 
	 * Note: this isn't a great name or practice (to set the internal state). I
	 * expect to refactor this class to remove code that acts on the internals.
	 * 
	 * @param audioState the new state to set the model to.
	 */
	public void setInternalPlayerState(AudioPlayerState audioState);

}
