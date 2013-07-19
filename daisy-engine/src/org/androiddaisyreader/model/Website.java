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
	private int SiteID;
	
	/** The Site name. */
	private String SiteName;
	
	/** The Site url. */
	private String SiteURL;
	
	/**
	 * Gets the site id.
	 *
	 * @return the site id
	 */
	public int getSiteID() {
		return SiteID;
	}
	
	/**
	 * Sets the site id.
	 *
	 * @param siteID the new site id
	 */
	public void setSiteID(int siteID) {
		SiteID = siteID;
	}
	
	/**
	 * Gets the site name.
	 *
	 * @return the site name
	 */
	public String getSiteName() {
		return SiteName;
	}
	
	/**
	 * Sets the site name.
	 *
	 * @param siteName the new site name
	 */
	public void setSiteName(String siteName) {
		SiteName = siteName;
	}
	
	/**
	 * Gets the site url.
	 *
	 * @return the site url
	 */
	public String getSiteURL() {
		return SiteURL;
	}
	
	/**
	 * Sets the site url.
	 *
	 * @param siteURL the new site url
	 */
	public void setSiteURL(String siteURL) {
		SiteURL = siteURL;
	}
	
	/**
	 * Instantiates a new website.
	 *
	 * @param siteName the site name
	 * @param siteURL the site url
	 * @param siteID the site id
	 */
	public Website (String siteName, String siteURL, int siteID){
		this.SiteName = siteName;
		this.SiteURL = siteURL;
		this.SiteID = siteID;
	}
}
