package org.androiddaisyreader.model;

import junit.framework.TestCase;

import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.controller.AudioPlayerController;
import org.androiddaisyreader.mock.MockAndroidAudioPlayer;

/**
 * Unit tests for the Mock Android Audio Player.
 * 
 * These tests are to help validate my assumptions which will be used to craft
 * the real player for the Android platform.
 * 
 * @author Julian Harty
 *
 */
public class MockAndroidAudioPlayerStateTest extends TestCase {

	private static final String AUDIO_PLAYER_STATE_INCORRECT = "The Audio Player State is incorrect";
	private MockAndroidAudioPlayer playerToTest;
	private Audio initialSegment;
	private Audio contiguousSegment;
	private Audio gapAfterContiguousSegments;
	private Audio overlapWithInitialSegment;
	private Audio newAudioFilename;
	private AudioPlayerController controller;
	AudioListener audioCallbackListener;

	private class AudioListener implements AudioCallbackListener {

		int endOfAudioCalled = 0;
		
		public void endOfAudio() {
			endOfAudioCalled++;
		}
	}
	
	@Override
	protected void setUp() {
		playerToTest = new MockAndroidAudioPlayer();
		controller = new AudioPlayerController(playerToTest);
		audioCallbackListener = new AudioListener();
		playerToTest.addCallbackListener(audioCallbackListener);
		
		initialSegment = new Audio("initial", "file1.mp3", 0, 1234);
		contiguousSegment = new Audio("contiguous", "file1.mp3", 1234, 7983);
		gapAfterContiguousSegments = new Audio("gap", "file1.mp3", 15001, 26771);
		overlapWithInitialSegment = new Audio("overlap", "file1.mp3", 900, 2086);
		newAudioFilename = new Audio("newfile", "new.mp3", 0, 11589);
		
		controller.playFileSegment(initialSegment);
	}

	public void testContiguousSegmentsAreRecognisedAsContiguous() {
		assertEquals(AUDIO_PLAYER_STATE_INCORRECT, 
				AudioPlayerState.PLAY_NEW_FILE, 
				playerToTest.getInternalPlayerState());
		controller.playFileSegment(contiguousSegment);
		assertAudioPlayerStateIs(AudioPlayerState.CONTINUE_PLAYING_EXISTING_FILE);
		assertEquals("Expected 2 calls, one per audio segment", 
				2, audioCallbackListener.endOfAudioCalled);
	}

	public void testGapBetweenSegmentsIsDetected() {
		controller.playFileSegment(contiguousSegment);
		controller.playFileSegment(gapAfterContiguousSegments);
		assertAudioPlayerStateIs(AudioPlayerState.GAP_BETWEEN_CONTENTS);
	}
	
	public void testOverlappingSegmentsAreDetected() {
		controller.playFileSegment(overlapWithInitialSegment);
		assertAudioPlayerStateIs(AudioPlayerState.OVERLAPPING_CONTENTS);
	}

	public void testNewAudioFileReplacesExistingOne() {
		controller.playFileSegment(newAudioFilename);
		assertAudioPlayerStateIs(AudioPlayerState.PLAY_NEW_FILE);
	}
	
	/**
	 * Private helper method that asserts the AudioPlayer is in the expected state.
	 * @param expectedState
	 */
	private void assertAudioPlayerStateIs(AudioPlayerState expectedState) {
		assertEquals(AUDIO_PLAYER_STATE_INCORRECT, expectedState, 
				playerToTest.getInternalPlayerState());
	}
}
