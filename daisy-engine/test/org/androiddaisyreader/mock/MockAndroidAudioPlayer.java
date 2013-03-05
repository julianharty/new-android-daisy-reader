package org.androiddaisyreader.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.AudioPlayer;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.AudioPlayerState;

public class MockAndroidAudioPlayer implements AudioPlayer {

	Logger log = Logger.getLogger("MockAudioPlayer");
	Integer currentVolume = 0;
	Boolean muted = false;
	Boolean playing = true;
	private Audio currentSegment;
	AudioPlayerState state = AudioPlayerState.INITIALIZED;
	
	
	public void increaseVolume() {
		currentVolume++;
	}

	public void decreaseVolume() {
		currentVolume--;
	}

	public void toggleMute() {
		muted = !muted;
	}

	public void addCallbackListener(AudioCallbackListener listener) {
		// TODO Auto-generated method stub
	}
	
	public Integer getCurrentVolume() {
		return currentVolume;
	}
	
	public Boolean isMuted() {
		return muted;
	}

	public Boolean isPlaying() {
		return playing;
	}

	public AudioPlayerState getInternalPlayerState() {
		return state;
	}

	public Audio getCurrentSegment() {
		return currentSegment;
	}

	public void play() {
		playing = true;
	}
	
	public void seekTo(int newTimeInMilliseconds) {
		
	}
	
	public void setInternalPlayerState(AudioPlayerState audioState) {
		state = audioState;
	}

	public void setCurrentSegment(Audio audioSegment) {
		currentSegment = audioSegment;
	}

}
