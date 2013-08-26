/**
 * This is constants
 * @author LogiGear
 * @date 2013.03.05
 */

package org.androiddaisyreader.utils;

public class Constants {
	public static String BUGSENSE_API_KEY = "cdc7bdaf";
	public static String Countly_APP_KEY = "95e39510858637ad9477871f296a7991db8e1d37";
	public static String Countly_URL_SERVER = "https://cloud.count.ly";
	public static int MY_DATA_CHECK_CODE = 1234;
	public static String NUMBER_OF_RECENT_BOOKS = "numberOfRecentBooks";
	public static String NUMBER_OF_BOOKMARKS = "numberOfBookmarks";
	public static String DAISY_PATH = "daisyPath";
	public static String LIST_CONTENTS = "listContents";
	public static String POSITION_SECTION = "positionSection";
	public static String POSITION_SENTENCE = "positionSentence";
	public static String TARGET_ACTIVITY = "targetActivity";
	public static String BOOK = "book";
	public static String SENTENCE = "sentence";
	public static String TIME = "time";
	public static String SECTION = "section";
	public static String POSITION = "position";
	public static String BRIGHTNESS = "brightness";
	public static String CURRRENT_BRIGHTNESS = "currentBrightness";
	public static String FONT_SIZE = "fontsize";
	public static String FIRST_RUN = "firstRun";
	public static String TEXT_COLOR = "textColor";
	public static String BACKGROUND_COLOR = "backgroundColor";
	public static String HIGHLIGHT_COLOR = "highlightColor";
	public static String NIGHT_MODE = "nightMode";
	public static String FILE_NCC_NAME_NOT_CAPS = "ncc.html";
	public static String FILE_NCC_NAME_CAPS = "NCC.HTML";
	public static String PREFIX_AUDIO_TEMP_FILE = "_DAISYTEMPAUDIO_";
	public static String SUFFIX_AUDIO_TEMP_FILE = ".mp3";
	public static String SUFFIX_ZIP_FILE = ".zip";
	public static String LINK_WEBSITE = "link";
	public static String NAME_WEBSITE = "name";
	public static String SERVICE_DONE = "serviceDone";
	public static String IS_RUN = "isRun";
	public static int FONTSIZE_DEFAULT = 20;
	public static int NUMBER_OF_BOOKMARK_DEFAULT = 10;
	public static int NUMBER_OF_RECENTBOOK_DEFAULT = 10;
	public static int TIME_WAIT_FOR_CLICK_SECTION = 2000;
	public static int TIME_WAIT_FOR_CLICK_SENTENCE = 300;
	public static int TIME_WAIT_TO_EXIT_APPLICATION = 2300;
	public static int DAISY_202_FORMAT = 2;
	public static int DAISY_30_FORMAT = 3;
	
	public static final int SUBMENU_MENU = 1;
	public static final int SUBMENU_LIBRARY = 2;
	public static final int SUBMENU_BOOKMARKS = 3;
	public static final int SUBMENU_TABLE_OF_CONTENTS = 4;
	public static final int SUBMENU_SIMPLE_MODE = 5;
	public static final int SUBMENU_SEARCH = 6;
	public static final int SUBMENU_SETTINGS = 7;

	// All message on simple mode activity.
	public final static int SIMPLE_MODE = 3;
	public final static int ERROR_WRONG_FORMAT_AUDIO = 4;
	public final static int ERROR_NO_AUDIO_FOUND = 5;
	public final static int AT_THE_END = 6;
	public final static int AT_THE_BEGIN = 7;
	public final static int NEXT_SECTION = 8;
	public final static int PREVIOUS_SECTION = 9;
	public final static int NEXT_SENTENCE = 10;
	public final static int PREVIOUS_SENTENCE = 11;
	public final static int PLAY = 12;
	public final static int PAUSE = 13;

	// All message on reader activity.
	public final static int VISUAL_MODE = 14;
	public final static int READER_ACTIVITY = 15;

	// All type of metadata.xml
	public final static String TYPE_DOWNLOAD_BOOK = "1";
	public final static String TYPE_RECENT_BOOK = "2";
	public final static String TYPE_SCAN_BOOK = "3";
	public final static String TYPE_DOWNLOADED_BOOK = "4";

	// All node in metadata.xml file.
	public final static String ATT_BOOKS = "books";
	public final static String ATT_WEBSITE = "website";
	public final static String ATT_URL = "url";
	public final static String ATT_BOOK = "book";
	public final static String ATT_LINK = "link";
	public final static String ATT_PATH = "path";
	public final static String ATT_TITLE = "title";
	public final static String ATT_AUTHOR = "author";
	public final static String ATT_PUBLISHER = "publisher";
	public final static String ATT_DATE = "date";
	public final static String ATT_TYPE = "type";
	public final static String META_DATA_FILE_NAME = "metadata.xml";
	public final static String META_DATA_SCAN_BOOK_FILE_NAME = "metadata_scanbook.xml";

	public final static String FOLDER_DOWNLOADED = "/download";
	public static String FOLDER_NAME = "dataDaisyBooks";
	public static String FOLDER_CONTAIN_METADATA = "";
	public static String DAISY_TEMP_MP3 = "daisyTemp.mp3";

	public final static int TYPE_WIFI = 1;
	public final static int TYPE_MOBILE = 2;
	public final static int TYPE_NOT_CONNECTED = 0;
	
}
