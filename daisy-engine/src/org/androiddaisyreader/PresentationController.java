package org.androiddaisyreader;

public class PresentationController implements AudioCallbackListener, NavigationListener, SnippetCallbackListener {
	private AudioPlayer player;
	private DisplayTextView view;

	public void completedSection() {
		// TODO Auto-generated method stub
		
	}

	public void endOfAudio() {
		// TODO Auto-generated method stub
	}
	
	public static class Builder {
		private PresentationController controller = new PresentationController();
		
		public void setAudioPlayer(AudioPlayer player) {
			controller.player = player;
		}
		
		public void setDisplayTextView(DisplayTextView view) {
			controller.view = view;
		}
		
		public PresentationController build() {
			
			// implement any rules and checks here before returning the controller
			// e.g. throw a new IllegalStateException if we're not happy
			return controller;
		}
	}

	public void next() {
		// TODO Auto-generated method stub
		
	}

	public void previous() {
		// TODO Auto-generated method stub
		
	}

	public void goTo(LocationInBook location) {
		// TODO Auto-generated method stub
		
	}

	public NavigationListener build(
			NavigationEventListener navigationEventListener) {
		// TODO Auto-generated method stub
		return null;
	}

}
