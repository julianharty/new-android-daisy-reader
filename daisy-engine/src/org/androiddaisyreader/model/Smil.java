package org.androiddaisyreader.model;

public class Smil {

	public String format;

	public static class Builder {
		Smil smil = new Smil(); 

		public Builder setFormat(String format) {
			smil.format = format;
			return this;
		}

	}

}
