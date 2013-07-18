package org.androiddaisyreader.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Daisy202Book extends Book {
	private Date date;

	public static class Builder {
		private Daisy202Book book = new Daisy202Book();

		public Builder addSection(Section section) {
			book.sections.add(section);
			return this;
		}

		public Builder setDate(Date date) {
			book.date = date;
			return this;
		}

		public Builder setTitle(String content) {
			// TODO 20120124 (jharty): consider cleaning up the content.
			book.title = content.trim();
			return this;
		}

		// Added by Logigear to resolve case: the daisy book is not audio.
		// Date: Jun-13-2013
		public Builder setTotalTime(String totalTime) {
			book.totalTime = totalTime.trim();
			return this;
		}

		public Builder setCreator(String creator) {
			book.author = creator;
			return this;
		}
		
		public Builder setPublisher(String publisher)
		{
			book.publisher = publisher;
			return this;
		}

		public Daisy202Book build() {
			book.sections = Collections.unmodifiableList(book.sections);
			return book;
		}
	}

	private Daisy202Book() {
		super();
	}

	public List<? extends Navigable> getChildren() {
		return sections;
	}

	public Date getDate() {
		return date;
	}

}
