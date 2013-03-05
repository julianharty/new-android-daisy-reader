package org.androiddaisyreader.model;

import java.util.ListIterator;
import java.util.Stack;

/** 
 * Navigates through the structure of a book.
 * 
 * This doesn't process the contents, which is handled at the Section level.
 * 
 * @author jharty
 */
public class Navigator {
	private Book book;
	private Stack<ListIterator<? extends Navigable>> stack = new Stack<ListIterator<? extends Navigable>>();
	
	/**
	 * Creates a new navigator, which is initialised at the start of the book.
	 * 
	 * TODO 20120301 (jharty): add a mechanism to navigate to a chosen location.
	 * @param book the book to read.
	 */
	public Navigator(Book book) {
		this.book = book;
		gotoStartOfContent();
	}
	
	/**
	 * Reset navigation to the start of the content; generally the beginning of
	 * the book.
	 */
	public void gotoStartOfContent() {
		stack.clear();
		stack.push(book.getChildren().listIterator());
	}
	
	
	// think about goto(navigation_point);
	
	/**
	 * Is there a next section? 
	 * @return true if there is, else return false.
	 */
	public boolean hasNext() {
		while ((stack.size() > 1) && !stack.peek().hasNext()) {
			stack.pop();
		}
		return stack.peek().hasNext();
	}
	
	/**
	 * Is there a previous section?
	 * @return true if there is, else return false.
	 */
	public boolean hasPrevious() {
		if ((stack.size() > 1) || stack.peek().hasPrevious()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Navigate to the next section.
	 * @return the next item.
	 */
	public Navigable next() {
		Navigable item;
		
		if (!hasNext()) {
			return null;
		}
		
		item = stack.peek().next();
		
		if (item.getChildren().size() > 0) {
			stack.push(item.getChildren().listIterator());
		}
		return item;
	}
	
	/**
	 * Navigate to the previous section.
	 * @return the previous item.
	 */
	public Navigable previous() {
		// TODO 20120127 (jharty): I'm sure this logic is overly complicated. Simplify and make more elegant.
		Navigable item;
		
		if (!hasPrevious()) {
			return null;
		}
		
		if (stack.peek().hasPrevious()) {
			boolean pushedItem = false;
			
			item = stack.peek().previous();
			// Now try to get to the bottom right root
			while (item.getChildren().size() > 0) {
				ListIterator<? extends Navigable> nextLevel = item.getChildren().listIterator();
				if (!pushedItem) {
					// We need to reset the cursor to before the current item as we will not use it yet.
					// It will be used when the stack is popped again.
					stack.peek().next();
				}
				
				while (nextLevel.hasNext()) {
					item = nextLevel.next();
				}
				stack.push(nextLevel);
				pushedItem = true;
			}
			if (pushedItem) {
				// Reset the cursor to be before the item we're about to return. 
				stack.peek().previous();
			}
		} else {
			// We have finished at this level, go up one level
			stack.pop();
			// How do we get the current item on the stack
			item = stack.peek().previous();
		}

		return item;
		
	}

}
