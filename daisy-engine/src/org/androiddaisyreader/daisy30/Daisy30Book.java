package org.androiddaisyreader.daisy30;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.androiddaisyreader.model.Daisy202Book;
import org.androiddaisyreader.model.Navigable;
import org.androiddaisyreader.model.Section;

public class Daisy30Book extends Daisy202Book {
	private Date date;

	public static class Builder {
		private Daisy30Book book = new Daisy30Book();

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

		public Builder setCreator(String creator) {
			book.author = creator;
			return this;
		}
		
		public Builder setPublisher(String publisher)
		{
			book.publisher = publisher;
			return this;
		}

		public Daisy30Book build() {
			book.sections = Collections.unmodifiableList(book.sections);
			return book;
		}
	}

	private Daisy30Book() {
		super();
	}

	public List<? extends Navigable> getChildren() {
		return sections;
	}

	public Date getDate() {
		return date;
	}

}
