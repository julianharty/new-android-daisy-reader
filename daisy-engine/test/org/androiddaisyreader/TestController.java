package org.androiddaisyreader;

import junit.framework.TestCase;

import org.androiddaisyreader.mock.MockAndroidAudioPlayer;

public class TestController extends TestCase {
	
	private PresentationController newController(AudioPlayer player) {
		PresentationController.Builder builder = new PresentationController.Builder();
		builder.setAudioPlayer(player);
		return builder.build();
		
	}
	public void testAudioPlays() {
		MockAndroidAudioPlayer player = new MockAndroidAudioPlayer();
		PresentationController controller = newController(player);
		assertTrue("", player.isPlaying());
	}

}
