/**
 * Controls the Audio Player and adapts to the quirks of the Android AudioPlayer. 
 */
package org.androiddaisyreader.controller;

import java.util.logging.Logger;

import org.androiddaisyreader.AudioPlayer;
import org.androiddaisyreader.player.SegmentTimeInfo;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.AudioPlayerState;

/**
 * OK, let's go...
 * @author Julian Harty
 */
public class AudioPlayerController {

	private AudioPlayer player;
	Logger log = Logger.getLogger("AudioPlayerController");
	
	@SuppressWarnings("unused")
	private AudioPlayerController() {
		// Stop people from calling me without providing a player.
	}
	
	public AudioPlayerController(AudioPlayer player) {
		this.player = player;
	}
	
	/**
	 * Play the audio from part of a file.
	 * Starts at the time specified in the from parameter. Finishes when the
	 * duration has been reached, or at the end of the file, whichever's sooner.
	 * 
	 * Android's MediaPlayer does not include a facility to 'stop' at a
	 * particular duration. Instead it plays to the end of the file. Therefore
	 * we want to emulate the behaviour of the Android player in this Mock
	 * audio player.
	 * 
	 * @param segment The audio segment to play
	 */
	public void playFileSegment(Audio audioSegment) {

		Audio currentSegment = player.getCurrentSegment();
		if (currentSegment == null) {
			log.info("Starting to play a new file");
			player.setInternalPlayerState(AudioPlayerState.PLAY_NEW_FILE);
			player.setCurrentSegment(audioSegment);
			player.play();
			return;
		}

		// Note: if we are able to determine the overall duration of the fileToPlay
		// we could check whether the duration is valid. For now, we'll assume it is.
		
		if (currentSegment.getAudioFilename().matches(audioSegment.getAudioFilename())) {
			int newClipStartsAt = audioSegment.getClipBegin();
			int previousClipEndsAt = currentSegment.getClipEnd();
			SegmentTimeInfo interval = SegmentTimeInfo.compareTimesForAudioSegments(
					newClipStartsAt, previousClipEndsAt);
			
			// Update the Player with details of the new segment.
			player.setCurrentSegment(audioSegment);
			switch(interval) {
			case CONTIGUOUS:
				log.info("The player will continue playing the existing audio, without interruption.");
				player.setInternalPlayerState(AudioPlayerState.CONTINUE_PLAYING_EXISTING_FILE);
				break;
			case GAP:
				log.warning(String.format("There is a gap between the audio segments, last " +
											"segment finished at %d, next segment starts at %d",
											newClipStartsAt, previousClipEndsAt));
				player.setInternalPlayerState(AudioPlayerState.GAP_BETWEEN_CONTENTS);
				player.seekTo(audioSegment.getClipBegin());
				player.play();
				break;
			case OVERLAPPING:
				log.warning(
						String.format(
								"The player was asked to play earlier in the file than expected." + 
										" Generally the next request %d should be contiguous with the" +
										" end of the previous section %d", newClipStartsAt, previousClipEndsAt));
				player.setInternalPlayerState(AudioPlayerState.OVERLAPPING_CONTENTS);
				player.seekTo(audioSegment.getClipBegin());
				player.play();
				break;
			}
		} else {
			log.info(String.format(
					"Stop playing the current file %s & start playing the newly specified file %s",
					currentSegment.getAudioFilename(), audioSegment.getAudioFilename()));
			player.setInternalPlayerState(AudioPlayerState.PLAY_NEW_FILE);
			player.setCurrentSegment(audioSegment);
			player.play();
			log.warning("Why aren't you playing?");
		}
		currentSegment = audioSegment;
		log.info(String.format("Playing %s from %d to %d", currentSegment.getAudioFilename(), 
								currentSegment.getClipBegin(), currentSegment.getClipEnd()));
	}

}
