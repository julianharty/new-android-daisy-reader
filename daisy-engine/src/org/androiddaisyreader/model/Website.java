/*
 * 
 */
package org.androiddaisyreader.model;

/**
 * The Website for download books.
 * 
 * @author LogiGear
 * @date Jul 8, 2013
 */

public class Website {

	/** The Site id. */
	private int siteID;

	/** The Site name. */
	private String siteName;

	/** The Site url. */
	private String siteURL;

	/**
	 * Gets the site id.
	 * 
	 * @return the site id
	 */
	public int getSiteID() {
		return siteID;
	}

	/**
	 * Sets the site id.
	 * 
	 * @param siteID the new site id
	 */
	public void setSiteID(int siteIDValue) {
		siteID = siteIDValue;
	}

	/**
	 * Gets the site name.
	 * 
	 * @return the site name
	 */
	public String getSiteName() {
		return siteName;
	}

	/**
	 * Sets the site name.
	 * 
	 * @param siteName the new site name
	 */
	public void setSiteName(String siteNameValue) {
		siteName = siteNameValue;
	}

	/**
	 * Gets the site url.
	 * 
	 * @return the site url
	 */
	public String getSiteURL() {
		return siteURL;
	}

	/**
	 * Sets the site url.
	 * 
	 * @param siteURL the new site url
	 */
	public void setSiteURL(String siteURLValue) {
		siteURL = siteURLValue;
	}

	/**
	 * Instantiates a new website.
	 * 
	 * @param siteName the site name
	 * @param siteURL the site url
	 * @param siteID the site id
	 */
	public Website(String siteNameValue, String siteURLValue, int siteIDValue) {
		this.siteName = siteNameValue;
		this.siteURL = siteURLValue;
		this.siteID = siteIDValue;
	}
}
