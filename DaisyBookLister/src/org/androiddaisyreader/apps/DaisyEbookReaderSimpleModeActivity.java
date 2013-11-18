package org.androiddaisyreader.apps;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.controller.AudioPlayerController;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.DaisySection;
import org.androiddaisyreader.model.Navigable;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.OpfSpecification;
import org.androiddaisyreader.model.Part;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.player.AndroidAudioPlayer;
import org.androiddaisyreader.player.IntentController;
import org.androiddaisyreader.sqlite.SQLiteCurrentInformationHelper;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.actionbarsherlock.view.MenuItem;
import com.google.marvin.widget.GestureOverlay;
import com.google.marvin.widget.GestureOverlay.Gesture;
import com.google.marvin.widget.GestureOverlay.GestureListener;

/**
 * This activity is simple mode which play audio.
 * 
 * @author LogiGear
 * @date 2013.03.05
 */

public class DaisyEbookReaderSimpleModeActivity extends DaisyEbookReaderBaseActivity {
    private boolean mIsFirstNext = false;
    private boolean mIsFirstPrevious = true;
    private BookContext mBookContext;
    private DaisyBook mBook;
    private Navigator mNavigator;
    private Navigator mNavigatorOfTableContents;
    private NavigationListener mNavigationListener;
    private Controller mController;
    private AudioPlayerController mAudioPlayer;
    private MediaPlayer mPlayer;
    private List<String> mListStringText;
    private List<Integer> mListTimeEnd;
    private List<Integer> mListTimeBegin;
    private IntentController mIntentController;
    private String mTime;
    private String mAudioFileName;
    private int mPositionSentence = 0;
    private boolean mIsRunable = true;
    private Runnable mRunnalbe;
    private Handler mHandler;
    // if audio is over, mIsEndOf will equal true.
    private boolean mIsEndOf = false;
    private static final int TIME_FOR_PROCESS = 400;
    private boolean mIsFound = true;
    private int mOldMessage;
    private CurrentInformation mCurrent;
    private SQLiteCurrentInformationHelper mSql;
    private int mPositionSection = 0;
    private boolean mIsPlaying = false;
    private String mPath;
    private boolean isFormat202 = false;
    private List<Audio> listAudio;
    private int countAudio = 0;
    private Map<String, List<Integer>> mHashMapBegin;
    private Map<String, List<Integer>> mHashMapEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daisy_ebook_reader_simple_mode);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mIntentController = new IntentController(DaisyEbookReaderSimpleModeActivity.this);
        mSql = new SQLiteCurrentInformationHelper(DaisyEbookReaderSimpleModeActivity.this);

        mNavigationListener = new NavigationListener();
        mController = new Controller(mNavigationListener);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.daisyReaderSimpleModeLayout);
        GestureOverlay mGestureOverlay = new GestureOverlay(this, gestureListener);
        relativeLayout.addView(mGestureOverlay);
        setContentView(relativeLayout);
        mHandler = new Handler();
        mPath = getIntent().getStringExtra(Constants.DAISY_PATH);
        isFormat202 = DaisyBookUtil.findDaisyFormat(mPath) == Constants.DAISY_202_FORMAT;
        openBook();
        readBook();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    /**
     * Start reading book.
     */
    private void readBook() {
        try {
            String section;
            mCurrent = mSql.getCurrentInformation();
            if (mCurrent != null
                    && mCurrent.getActivity().equals(
                            getString(R.string.title_activity_daisy_ebook_reader_simple_mode))) {
                mCurrent.setAtTheEnd(false);
                mSql.updateCurrentInformation(mCurrent);
            }
            if (mCurrent != null
                    && !mCurrent.getActivity().equals(
                            getString(R.string.title_activity_daisy_ebook_reader_simple_mode))) {
                section = String.valueOf(mCurrent.getSection());
                mTime = String.valueOf(mCurrent.getTime());
                mAudioFileName = mCurrent.getAudioName();
                mPositionSentence = 0;
                mCurrent.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
                mSql.updateCurrentInformation(mCurrent);
            } else {
                section = getIntent().getStringExtra(Constants.POSITION_SECTION);
                mTime = getIntent().getStringExtra(Constants.TIME);
            }
            if (section != null) {
                int countLoop = Integer.valueOf(section) - mPositionSection;
                Navigable n = getNavigable(countLoop);
                if (n instanceof Section) {
                    mNavigationListener.onNext((Section) n);
                }
                // Bookmark for daisy 3.0
                if (!isFormat202) {
                    for (int i = 0; i < listAudio.size(); i++) {
                        Audio audio = listAudio.get(i);
                        if (audio.getAudioFilename().equals(mAudioFileName)) {
                            countAudio = i;
                            mAudioPlayer.playFileSegment(audio);
                            break;
                        }
                    }
                    // seek to time when user loading from book mark.
                    if (mTime != null) {
                        mPlayer.seekTo(Integer.valueOf(mTime));
                        mTime = null;
                    }

                    // get status of audio
                    if (mCurrent != null) {
                        mSql.updateCurrentInformation(mCurrent);
                        if (mCurrent.getPlaying()) {
                            setMediaPlay();
                        } else {
                            setMediaPause();
                        }
                    }
                }
            } else {
                // if user do not load from table of contents, play reading book
                // at normal.
                togglePlay();
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this,
                    mPath);
            ex.showDialogException(mIntentController);
        }
    }

    /**
     * Get current section which user want to play.
     * 
     * @param countLoop
     * @return Navigable
     */
    private Navigable getNavigable(int countLoop) {
        Navigable n = null;
        // case 1: variable > 0, user want to next section.
        if (countLoop > 0) {
            n = nextSectionByCountLoop(countLoop);
        }
        // case 2: variable > 0, user want to go to sentence.
        else if (countLoop == 0 && isFormat202) {
            n = mNavigator.previous();
            n = mNavigator.next();
        }
        // case 3: variable < 0, user want to previous section.
        else {
            n = previousSectionByCountLoop(-countLoop);
        }
        return n;
    }

    /**
     * Get next section.
     * 
     * @param countLoop
     * @return Navigable
     */
    private Navigable nextSectionByCountLoop(int countLoop) {
        Navigable n = null;
        for (int j = 0; j < countLoop; j++) {
            n = mNavigator.next();
            if (mCurrent != null
                    && mCurrent.getActivity().equals(
                            getString(R.string.title_activity_daisy_ebook_reader_visual_mode))) {
                n = mNavigator.next();
                mIsFirstPrevious = true;
                mIsFirstNext = false;
            }
            mPositionSection += 1;
        }
        return n;
    }

    /**
     * Get previous section
     * 
     * @param countLoop
     * @return Navigable
     */
    private Navigable previousSectionByCountLoop(int countLoop) {
        Navigable n = null;
        for (int j = 0; j < countLoop; j++) {
            n = mNavigator.previous();
            {
                if (mCurrent != null
                        && mCurrent.getActivity().equals(
                                getString(R.string.title_activity_daisy_ebook_reader_visual_mode))) {
                    n = mNavigator.previous();
                    mIsFirstPrevious = false;
                    mIsFirstNext = true;
                }
            }
            mPositionSection -= 1;
        }
        return n;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mBook != null) {
            mIsPlaying = mPlayer.isPlaying();
            if (mIsPlaying) {
                setMediaPause();
            }

            if (mCurrent == null) {
                createCurrentInformation();
            } else {
                updateCurrentInformation();
            }
            finish();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Create current information.
     */
    private void createCurrentInformation() {
        // create a current information
        CurrentInformation current = new CurrentInformation();
        try {
            if (isFormat202) {
                current.setAudioName("");
            } else {
                current.setAudioName(listAudio.get(countAudio).getAudioFilename());
            }
            current.setPath(getIntent().getStringExtra(Constants.DAISY_PATH));
            current.setSection(mPositionSection);
            current.setTime(mPlayer.getCurrentPosition());
            current.setPlaying(mIsPlaying);
            current.setSentence(1);
            current.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
            current.setAtTheEnd(false);
            current.setId(UUID.randomUUID().toString());
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
            ex.writeLogException();
        }
        mSql.addCurrentInformation(current);
    }

    /**
     * Update current information.
     */
    private void updateCurrentInformation() {
        if (isFormat202) {
            mCurrent.setAudioName("");
        } else {
            mCurrent.setAudioName(listAudio.get(countAudio).getAudioFilename());
        }
        mCurrent.setPlaying(mIsPlaying);
        mCurrent.setTime(mPlayer.getCurrentPosition());
        mCurrent.setSection(mPositionSection);
        mCurrent.setSentence(mPositionSentence);
        mCurrent.setActivity(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
        mSql.updateCurrentInformation(mCurrent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mPlayer != null & mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            mTts.shutdown();
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
            ex.writeLogException();
        }
        mHandler.removeCallbacks(mRunnalbe);
    }

    @Override
    protected void onResume() {
        super.onResume();
        speakOut(Constants.SIMPLE_MODE);
        speakOut(mOldMessage);

        if (mBook != null) {
            mNavigatorOfTableContents = new Navigator(mBook);
        }
    }

    @Override
    protected void onRestart() {
        mCurrent = mSql.getCurrentInformation();
        if (mCurrent != null) {
            if (mCurrent.getPlaying()) {
                setMediaPlay();
            } else {
                setMediaPause();
            }
            if (!mCurrent.getActivity().equals(
                    getString(R.string.title_activity_daisy_ebook_reader_simple_mode))) {
                readBook();
            }
        }
        super.onRestart();
    }

    private AudioCallbackListener audioCallbackListener = new AudioCallbackListener() {

        public void endOfAudio() {
            Log.i("DAISYBOOKLISTENERACTIVITY", "Audio is over...");
            if (!isFormat202 && listAudio != null && listAudio.size() - 1 > countAudio) {
                countAudio = countAudio + 1;
                mAudioPlayer.playFileSegment(listAudio.get(countAudio));
            } else if (!mIsEndOf && mIsFound) {
                mController.next();
            }
        }
    };
    private List<String> listId;

    /**
     * open book from path
     */
    private void openBook() {
        try {
			if (isFormat202) {
			    openBook202();
			} else {
			    openBook30();
			}
		} catch (PrivateException e) {
            e.showDialogException(mIntentController);
            this.finish();
		}

        AndroidAudioPlayer androidAudioPlayer = new AndroidAudioPlayer(mBookContext);
        androidAudioPlayer.addCallbackListener(audioCallbackListener);
        mAudioPlayer = new AudioPlayerController(androidAudioPlayer);
        mPlayer = androidAudioPlayer.getCurrentPlayer();
        // get all navigator of book to push to table of contents.
        mNavigatorOfTableContents = new Navigator(mBook);
        mNavigator = mNavigatorOfTableContents;
        if (!isFormat202) {
            Navigator temp = new Navigator(mBook);
            listId = new ArrayList<String>();
            while (temp.hasNext()) {
                Section n = (Section) temp.next();
                listId.add(splitHref(n.getHref())[1]);
            }
        }
    }

    /**
     * Split href.
     * 
     * @param href the href
     * @return the string[]
     */
    private String[] splitHref(String href) {
        return href.split("#");
    }

    /**
     * Open Daisy book with format 2.02.
     * @throws PrivateException 
     */
    private void openBook202() throws PrivateException {
    	try {
    		InputStream contents;
    		mBookContext = DaisyBookUtil.openBook(mPath);
    		contents = mBookContext.getResource(Constants.FILE_NCC_NAME_NOT_CAPS);
    		mBook = NccSpecification.readFromStream(contents);
    		if (!mBook.hasTotalTime()) {
    			mIntentController.pushToDialog(getString(R.string.error_wrong_format_audio),
    					getString(R.string.error_title), R.raw.error, false, false, null);
    		}

    	} catch (Exception e) {
    		PrivateException ex = new PrivateException(e,
    				DaisyEbookReaderSimpleModeActivity.this, mPath);
    		throw ex;
    	}
    }

    /**
     * Open Daisy book with format 3.0.
     * @throws PrivateException 
     */
    private void openBook30() throws PrivateException {

    	try {
    		InputStream contents;
    		String opfName = "";
    		if (mPath.endsWith(Constants.SUFFIX_ZIP_FILE)) {
    			mBookContext = DaisyBookUtil.openBook(mPath);
    			opfName = DaisyBookUtil.getOpfFileNameInZipFolder(mPath);
    		} else {
    			opfName = DaisyBookUtil.getOpfFileName(mPath);
    			mBookContext = DaisyBookUtil.openBook(mPath + File.separator + opfName);
    		}
    		contents = mBookContext.getResource(opfName);
    		mBook = OpfSpecification.readFromStream(contents, mBookContext);

    	} catch (Exception e) {
    		PrivateException ex = new PrivateException(e,
    				DaisyEbookReaderSimpleModeActivity.this, mPath);
    		throw ex;
    	}

    }

    /**
     * Listens to Navigation Events.
     * 
     * @author Julian Harty
     */
    private class NavigationListener {
        public void onNext(Section section) {
            try {
                if (isFormat202) {
                    getSnippetAndAudioForDaisy202(section);
                } else {
                    getSnippetAndAudioForDaisy30(section);
                }
                // seek to time when user change from visual mode
                if (mListTimeEnd.size() > 0) {
                    if (isFormat202 && mTime != null) {
                        mPlayer.seekTo(Integer.valueOf(mTime));
                        mTime = null;
                    }
                    if (mCurrent != null) {
                        mSql.updateCurrentInformation(mCurrent);
                        if (mCurrent.getPlaying()) {
                            setMediaPlay();
                        } else {
                            setMediaPause();
                        }
                    }
                    getCurrentPositionSentence();
                }
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderSimpleModeActivity.this);
                ex.showDialogException(mIntentController);
            }
        }

        /**
         * Gets the snippet and audio for daisy202.
         * 
         * @param section the section
         */
        private void getSnippetAndAudioForDaisy202(Section section) {
            Part[] parts = null;
            DaisySection currentSection = null;
            currentSection = new DaisySection.Builder().setHref(section.getHref())
                    .setContext(mBookContext).build();
            parts = currentSection.getParts(isFormat202);
            getSnippetsOfCurrentSection(parts);
            getAudioElementsOfCurrentSectionForDaisy202(parts);
        }

        /**
         * Gets the snippet and audio for daisy30.
         * 
         * @param section the section
         */
        private void getSnippetAndAudioForDaisy30(Section section) {
            Part[] parts = null;
            DaisySection currentSection = null;
            boolean isCurrentPart = false;
            currentSection = new DaisySection.Builder().setHref(section.getHref())
                    .setContext(mBookContext).build();
            Part[] tempParts = currentSection.getParts(isFormat202);
            List<Part> listPart = new ArrayList<Part>();
            for (Part part : tempParts) {
                if (part.getId().equals(listId.get(mPositionSection - 1))) {
                    isCurrentPart = true;
                }
                if (isCurrentPart) {
                    if (listId.size() == mPositionSection) {
                        listPart.add(part);
                    } else if (!part.getId().equals(listId.get(mPositionSection))) {
                        listPart.add(part);
                    } else {
                        break;
                    }
                }
            }
            parts = listPart.toArray(new Part[0]);
            getSnippetsOfCurrentSection(parts);
            getAudioElementsOfCurrentSectionForDaisy30(parts);
        }

        /**
         * Get all text from parts.
         * 
         * @param parts
         */
        private void getSnippetsOfCurrentSection(Part[] parts) {
            mListStringText = new ArrayList<String>();
            mListTimeEnd = new ArrayList<Integer>();
            mListTimeBegin = new ArrayList<Integer>();
            mHashMapBegin = new LinkedHashMap<String, List<Integer>>();
            mHashMapEnd = new LinkedHashMap<String, List<Integer>>();
            StringBuilder snippetText = new StringBuilder();
            List<Integer> listClipBegin = new ArrayList<Integer>();
            List<Integer> listClipEnd = new ArrayList<Integer>();
            String fileName = null;
            try {
                for (Part part : parts) {
                    int sizeOfPart = part.getSnippets().size();
                    for (int i = 0; i < sizeOfPart; i++) {
                        if (i > 0) {
                            snippetText.append(getString(R.string.space));
                        }
                        String text = part.getSnippets().get(i).getText();
                        snippetText.append(text);
                        mListStringText.add(text);
                    }
                    snippetText.append(getString(R.string.space));
                    List<Audio> audioElements = part.getAudioElements();
                    if (audioElements.size() > 0) {
                        mListTimeBegin.add(audioElements.get(0).getClipBegin());
                        mListTimeEnd.add(audioElements.get(audioElements.size() - 1).getClipEnd());
                    }

                    int audioElementsSize = audioElements.size();
                    if (audioElementsSize > 0) {
                        Audio audio = audioElements.get(0);
                        if (fileName == null || !fileName.equals(audio.getAudioFilename())) {
                            mHashMapBegin.put(fileName, listClipBegin);
                            mHashMapEnd.put(fileName, listClipEnd);
                            listClipBegin = new ArrayList<Integer>();
                            listClipEnd = new ArrayList<Integer>();
                            fileName = audio.getAudioFilename();
                        }
                        listClipBegin.add(audio.getClipBegin());
                        listClipEnd.add(audioElements.get(audioElementsSize - 1).getClipEnd());
                    }
                }
                mHashMapBegin.put(fileName, listClipBegin);
                mHashMapEnd.put(fileName, listClipEnd);
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderSimpleModeActivity.this);
                ex.writeLogException();
            }

        }

        /**
         * Get all audio from parts.
         * 
         * @param parts
         */
        private void getAudioElementsOfCurrentSectionForDaisy202(Part[] parts) {
            try {
                for (Part part : parts) {
                    for (Audio audioSegment : part.getAudioElements()) {
                        mAudioPlayer.playFileSegment(audioSegment);
                    }
                }
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderSimpleModeActivity.this, mPath);
                ex.showDialogException(mIntentController);
                mOldMessage = Constants.ERROR_NO_AUDIO_FOUND;
                speakOut(Constants.ERROR_NO_AUDIO_FOUND);
                mIsFound = false;
            }
        }

        /**
         * Gets the audio elements of current section for daisy30.
         * 
         * @param parts the parts
         * @return the audio elements of current section for daisy30
         */
        private void getAudioElementsOfCurrentSectionForDaisy30(final Part[] parts) {
            try {
                String audiFileName = "";
                listAudio = new ArrayList<Audio>();
                countAudio = 0;
                for (Part part : parts) {
                    if (part.getAudioElements().size() > 0) {
                        Audio audioSegment = part.getAudioElements().get(0);
                        if (!audioSegment.getAudioFilename().equals(audiFileName)) {
                            listAudio.add(audioSegment);
                            audiFileName = audioSegment.getAudioFilename();
                        }
                    }
                }
                if (listAudio.size() > 0) {
                    mAudioPlayer.playFileSegment(listAudio.get(0));
                }
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderSimpleModeActivity.this, mPath);
                ex.showDialogException(mIntentController);
                mIsFound = false;
                mOldMessage = Constants.ERROR_NO_AUDIO_FOUND;
                speakOut(Constants.ERROR_NO_AUDIO_FOUND);
            }
        }

        /**
         * Speak message at the end of book.
         */
        public void atEndOfBook() {
            speakOut(Constants.AT_THE_END);
            int currentTime = mPlayer.getCurrentPosition();
            if (currentTime == -1 || currentTime == mPlayer.getDuration() || currentTime == 0) {
                mIsRunable = false;
                mIsEndOf = true;
            }
            if (mCurrent != null) {
                mCurrent.setAtTheEnd(mIsEndOf);
                mSql.updateCurrentInformation(mCurrent);
            }
        }

        /**
         * Speak message at the begin of book.
         */
        public void atBeginOfBook() {
            speakOut(Constants.AT_THE_BEGIN);
        }
    }

    /**
     * Here is our nano-controller which calls methods on the Navigation
     * Listener. We could include a method to add additional listeners.
     * 
     * @author Julian Harty
     */
    private class Controller {
        private NavigationListener navigationListener;
        private Navigable n;

        Controller(NavigationListener navigationListener) {
            this.navigationListener = navigationListener;
        }

        /**
         * Go to next section
         */
        public void next() {
            if (mNavigator.hasNext()) {
                if (mIsFirstNext) {
                    // Make sure no repeat section is playing.
                    mNavigator.next();
                    mIsFirstPrevious = true;
                    mIsFirstNext = false;
                }
                n = mNavigator.next();
                if (n instanceof Section) {
                    mHandler.removeCallbacks(mRunnalbe);
                    mIsRunable = true;
                    mPositionSection += 1;
                    mPositionSentence = 0;
                    navigationListener.onNext((Section) n);
                }

            } else {
                navigationListener.atEndOfBook();
            }

        }

        /**
         * Go to previous section
         */
        public void previous() {
            if (mNavigator.hasPrevious()) {
                if (mIsFirstPrevious) {
                    // Make sure the section is playing no repeat.
                    mNavigator.previous();
                    mIsFirstPrevious = false;
                    mIsFirstNext = true;
                }
                n = mNavigator.previous();
                if (n instanceof Section) {
                    mHandler.removeCallbacks(mRunnalbe);
                    mIsRunable = true;
                    mPositionSection -= 1;
                    mPositionSentence = 0;
                    navigationListener.onNext((Section) n);
                }

                if (mCurrent != null) {
                    mCurrent.setFirstNext(true);
                    mCurrent.setFirstPrevious(false);
                    mSql.updateCurrentInformation(mCurrent);
                }
            } else {
                navigationListener.atBeginOfBook();
            }

        }
    }

    @Override
    public void onClick(View v) {
    }

    /**
     * Handle all gesture actions.
     */
    private GestureListener gestureListener = new GestureListener() {

        @Override
        public void onGestureStart(int g) {
        }

        @Override
        public void onGestureFinish(int g) {
            String gesture = "GESTURE";
            try {
                // If user double tap will go to table of contents.
                boolean isDoubleTap = handleClickItem(0);
                if (isDoubleTap) {
                    Log.i(gesture, "Action: Double Tap");
                    mIsPlaying = mPlayer.isPlaying();
                    if (mIsPlaying) {
                        setMediaPause();
                    }
                    if (mCurrent != null) {
                        updateCurrentInformation();
                    } else {
                        createCurrentInformation();
                    }
                    String path = getIntent().getStringExtra(Constants.DAISY_PATH);
                    mIntentController.pushToTableOfContentsIntent(path, mNavigatorOfTableContents,
                            getString(R.string.simple_mode));
                } else {
                    switch (g) {
                    case Gesture.CENTER:
                        Log.i(gesture, "Action: CENTER");
                        togglePlay();
                        break;
                    case Gesture.DOWN:
                        Log.i(gesture, "Action: DOWN");
                        if (mNavigator.hasNext()) {
                            speakOut(Constants.NEXT_SECTION);
                        }
                        nextSection();
                        break;
                    case Gesture.UP:
                        Log.i(gesture, "Action: UP");

                        if (mNavigator.hasPrevious()) {
                            speakOut(Constants.PREVIOUS_SECTION);
                        }
                        previousSection();
                        break;
                    case Gesture.LEFT:
                        Log.i(gesture, "Action: LEFT");
                        speakOut(Constants.PREVIOUS_SENTENCE);
                        previousSentence();
                        if (mPositionSentence > 0) {
                            mPositionSentence -= 1;
                            mHandler.removeCallbacks(mRunnalbe);
                            mIsRunable = true;
                            getCurrentPositionSentence();
                        }
                        break;
                    case Gesture.RIGHT:
                        Log.i(gesture, "Action: RIGHT");
                        speakOut(Constants.NEXT_SENTENCE);
                        nextSentence();
                        if (mPositionSentence < mListTimeBegin.size() - 1) {
                            mPositionSentence += 1;
                            mHandler.removeCallbacks(mRunnalbe);
                            mIsRunable = true;
                            getCurrentPositionSentence();
                        }
                        break;
                    default:
                        break;
                    }
                }
            } catch (Exception e) {
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderSimpleModeActivity.this);
                ex.writeLogException();
            }
        }

        @Override
        public void onGestureChange(int g) {
        }
    };

    /**
     * Go to next sentence by seek to time of clip end nearest position.
     */
    private void nextSentence() {
        try {
            if (isFormat202) {
                nextSentenceDaisy202();
            }
            // For daisy format 3.0
            else {
                nextSentenceDaisy30();
            }

        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
            ex.writeLogException();
        }
    }

    /**
     * Next sentence daisy202.
     */
    private void nextSentenceDaisy202() {
        int currentTime = mPlayer.getCurrentPosition();
        if (mCurrent != null) {
            mIsEndOf = mCurrent.getAtTheEnd();
        }
        // this case for user press next sentence at the end of book.
        if (currentTime == 0 && !mNavigator.hasNext() && mPositionSentence == mListTimeBegin.size()
                || mIsEndOf) {
            mNavigationListener.atEndOfBook();
        }
        // this case for user press next sentence.
        else if (mPositionSentence < mListTimeBegin.size() - 1) {
            mPlayer.seekTo(mListTimeBegin.get(mPositionSentence + 1));
        }
        // this case for user press next sentence at the end of section.
        else {
            nextSection();
            mPositionSentence -= 1;
        }
    }

    private void nextSentenceDaisy30() {
        if (mCurrent != null) {
            mIsEndOf = mCurrent.getAtTheEnd();
        }
        // this case for user press next sentence at the end of book
        if (mPlayer.getCurrentPosition() == 0 && !mNavigator.hasNext()
                && mPositionSentence == mListTimeBegin.size() || mIsEndOf) {
            mNavigationListener.atEndOfBook();
        }
        // this case for user press next sentence.
        else if (mPositionSentence < mListTimeBegin.size() - 1) {
            boolean isBreak = false;
            int currentTimeBegin = mListTimeBegin.get(mPositionSentence + 1);
            int currentTimeEnd = mListTimeEnd.get(mPositionSentence + 1);
            // Find and play the next audio (If daisy book has many audio files
            // on 1 chapter).
            for (Entry<String, List<Integer>> entry : mHashMapBegin.entrySet()) {
                List<Integer> listValue = entry.getValue();
                if (!isBreak) {
                    for (int value : listValue) {
                        if (value == currentTimeBegin) {
                            if (!entry.getKey()
                                    .equals(listAudio.get(countAudio).getAudioFilename())) {
                                List<Integer> listValueEnd = mHashMapEnd.get(entry.getKey());
                                if (listValueEnd.contains(currentTimeEnd)) {
                                    isBreak = true;
                                    countAudio = countAudio + 1;
                                    mAudioPlayer.playFileSegment(listAudio.get(countAudio));
                                }
                            }
                            if (isBreak) {
                                break;
                            }
                        }
                    }
                }
            }
            mPlayer.seekTo(mListTimeBegin.get(mPositionSentence + 1));
        }
        // this case for user press next sentence at the end of section.
        else {
            nextSection();
            mPositionSentence -= 1;
        }
    }

    /**
     * Go to next section.
     */
    private void nextSection() {
        boolean isPlaying = mPlayer.isPlaying();
        mController.next();
        if (!isPlaying && !mIsEndOf) {
            setMediaPause();
        }
    }

    /**
     * Go to previous sentence by seek to time of clip end before two units.
     */
    private void previousSentence() {
        boolean isPlaying = mPlayer.isPlaying();
        if (mCurrent != null) {
            mIsEndOf = mCurrent.getAtTheEnd();
        }
        try {
            if (isFormat202) {
                previousSentenceDaisy202();
            }
            // For daisy format 3.0
            else {
                previousSentenceDaisy30();
            }
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
            ex.writeLogException();
        }
        // keep current state media player.
        if (!isPlaying) {
            setMediaPause();
        }
    }

    /**
     * Previous sentence daisy202.
     */
    private void previousSentenceDaisy202() {
        // this case for user press previous sentence at the begin of
        // book.
        if (mPositionSection == 1 && mPositionSentence == 0) {
            mNavigationListener.atBeginOfBook();
        }
        // this case for user press previous sentence at the end of
        // book.
        else if (mIsEndOf) {
            // It is code to resolve previous sentence when the end
            // of the book.
            mCurrent = mSql.getCurrentInformation();
            if (mCurrent != null) {
                mCurrent.setAtTheEnd(false);
                mSql.updateCurrentInformation(mCurrent);
            }
            Navigable n = mNavigator.previous();
            n = mNavigator.next();
            mNavigationListener.onNext((Section) n);
            mIsEndOf = false;
            mPlayer.seekTo(mListTimeBegin.get(mListTimeBegin.size() - 1));
            mPositionSentence = mListTimeBegin.size() - 1;
        }
        // this case for user press previous sentence.
        else if (mPositionSentence > 0) {
            mPlayer.seekTo(mListTimeBegin.get(mPositionSentence - 1));
        }
        // this case for user press previous sentence at the begin of
        // section.
        else {
            mController.previous();
            int sizeOfListEnd = mListTimeEnd.size();
            mPositionSentence = sizeOfListEnd - 1;
            if (sizeOfListEnd > 1) {
                mPlayer.seekTo(mListTimeEnd.get(sizeOfListEnd - 2));
            }
        }
    }

    /**
     * Previous sentence daisy30.
     */
    private void previousSentenceDaisy30() {
        // this case for user press previous sentence at the begin of book.
        if (mPositionSection == 1 && mPositionSentence == 0) {
            mNavigationListener.atBeginOfBook();
        }
        // this case for user press previous sentence at the end of book.
        else if (mIsEndOf) {
            // It is code to resolve previous sentence when the end
            // of the book.
            mCurrent = mSql.getCurrentInformation();
            if (mCurrent != null) {
                mCurrent.setAtTheEnd(false);
                mSql.updateCurrentInformation(mCurrent);
            }
            mIsRunable = true;
            Navigable n = mNavigator.previous();
            n = mNavigator.next();
            mNavigationListener.onNext((Section) n);
            mIsEndOf = false;

            mAudioPlayer.playFileSegment(listAudio.get(listAudio.size() - 1));
            countAudio = listAudio.size() - 1;
            mPlayer.seekTo(mListTimeBegin.get(mListTimeBegin.size() - 1));
            mPositionSentence = mListTimeBegin.size() - 1;
        } else if (mPositionSentence > 0) {
            boolean isBreak = false;
            int currentTimeBegin = mListTimeBegin.get(mPositionSentence - 1);
            int currentTimeEnd = mListTimeEnd.get(mPositionSentence - 1);
            // Find and play the next audio (If daisy book has many audio files
            // on 1 chapter).
            for (Entry<String, List<Integer>> entry : mHashMapBegin.entrySet()) {
                List<Integer> listValue = entry.getValue();
                if (!isBreak) {
                    for (int value : listValue) {
                        if (value == currentTimeBegin) {
                            if (!entry.getKey()
                                    .equals(listAudio.get(countAudio).getAudioFilename())) {
                                List<Integer> listValueEnd = mHashMapEnd.get(entry.getKey());
                                if (listValueEnd.contains(currentTimeEnd)) {
                                    isBreak = true;
                                    countAudio = countAudio - 1;
                                    mAudioPlayer.playFileSegment(listAudio.get(countAudio));
                                }
                            }
                            if (isBreak) {
                                break;
                            }
                        }
                    }
                }
            }
            mPlayer.seekTo(mListTimeBegin.get(mPositionSentence - 1));
        }
        // this case for user press previous sentence at the begin of section.
        else {
            mController.previous();
            mPositionSentence = mListTimeBegin.size() - 1;
            if (mListTimeEnd.size() > 1) {
                // get all text of text view
                mAudioPlayer.playFileSegment(listAudio.get(listAudio.size() - 1));
                countAudio = listAudio.size() - 1;
                mPlayer.seekTo(mListTimeEnd.get(mListTimeEnd.size() - 2));
            }
        }
    }

    /**
     * Go to previous section.
     */
    private void previousSection() {
        mCurrent = mSql.getCurrentInformation();
        boolean isPlaying = mPlayer.isPlaying();
        mIsEndOf = false;
        if (mCurrent != null) {
            mCurrent.setAtTheEnd(false);
            mSql.updateCurrentInformation(mCurrent);
        }
        mController.previous();
        if (!isPlaying) {
            setMediaPause();
        }
    }

    /**
     * Set media pause and remove call back
     */
    private void setMediaPause() {
        mHandler.removeCallbacks(mRunnalbe);
        mPlayer.pause();
        mIsRunable = false;
    }

    private long mTimePause = 0;

    /**
     * Set media play and post runnable
     */
    private void setMediaPlay() {
        mCurrent = mSql.getCurrentInformation();
        if (mCurrent != null) {
            mIsEndOf = mCurrent.getAtTheEnd();
        }
        if (mIsEndOf) {
            mNavigationListener.atEndOfBook();
        } else {
            mPlayer.start();
            if (mCurrent != null) {
                mCurrent.setPlaying(true);
                mSql.updateCurrentInformation(mCurrent);
            }
            mIsRunable = true;
            if (mPlayer.getCurrentPosition() != 0 && mListTimeEnd != null) {
                // if you pause while audio playing. You need to know time pause
                // to
                // high light text more correctly.
                mTimePause = mListTimeEnd.get(mPositionSentence) - mPlayer.getCurrentPosition();
            }
            mHandler.post(mRunnalbe);
        }
    }

    /**
     * Toggles the Media Player between Play and Pause states.
     */
    private void togglePlay() {
        mIsPlaying = mPlayer.isPlaying();
        if (mIsPlaying) {
            setMediaPause();
            speakOut(Constants.PAUSE);
        } else {
            try {
                speakOut(Constants.PLAY);
                setMediaPlay();
            } catch (Exception e) {
                speakOut(Constants.ERROR_WRONG_FORMAT_AUDIO);
                PrivateException ex = new PrivateException(e,
                        DaisyEbookReaderSimpleModeActivity.this);
                ex.writeLogException();

            }
        }
    }

    /**
     * This function help to get current position sentence to support next
     * sentence, previous sentence.
     */
    private void getCurrentPositionSentence() {
        try {
            mRunnalbe = new Runnable() {
                @Override
                public void run() {
                    if (mIsRunable) {
                        int sizeOfStringText = mListStringText.size();
                        for (int i = mPositionSentence; i < sizeOfStringText; i++) {
                            int currentPosition = mPlayer.getCurrentPosition();
                            if (mListTimeBegin.get(i) <= currentPosition + TIME_FOR_PROCESS
                                    && currentPosition < mListTimeEnd.get(i)) {
                                mPositionSentence = i;
                                break;
                            }
                            // This case for daisy 3.0. Some audio files won't
                            // play until it finish, it was splitted and move to
                            // the next chapter
                            else if (mPositionSentence + 1 >= sizeOfStringText && !mIsEndOf) {
                                nextSection();
                            }
                        }
                    }
                    if (mTimePause == 0) {
                        int timeReadSentence = mListTimeEnd.get(mPositionSentence)
                                - mListTimeBegin.get(mPositionSentence);
                        mHandler.postDelayed(this, timeReadSentence);
                    } else {
                        // If user choose pause and play. 400 is time delay
                        // when
                        // you touch on your phone.
                        mHandler.postDelayed(this, mTimePause + TIME_FOR_PROCESS);
                    }
                    mTimePause = 0;
                }
            };
            mHandler.post(mRunnalbe);
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, DaisyEbookReaderSimpleModeActivity.this);
            ex.writeLogException();
        }
    }

    /**
     * This function will speak out message.
     * 
     * @param message
     */
    private void speakOut(int message) {
        switch (message) {
        case Constants.ERROR_NO_AUDIO_FOUND:
            speakText(getString(R.string.error_no_audio_found));
            break;
        case Constants.SIMPLE_MODE:
            speakText(getString(R.string.title_activity_daisy_ebook_reader_simple_mode));
            break;
        case Constants.ERROR_WRONG_FORMAT_AUDIO:
            speakText(getString(R.string.error_wrong_format_audio));
            break;
        case Constants.AT_THE_END:
            speakText(getString(R.string.atEnd) + mBook.getTitle());
            break;
        case Constants.AT_THE_BEGIN:
            speakText(getString(R.string.atBegin) + mBook.getTitle());
            break;
        case Constants.NEXT_SECTION:
            speakText(getString(R.string.next_section));
            break;
        case Constants.PREVIOUS_SECTION:
            speakText(getString(R.string.previous_section));
            break;
        case Constants.NEXT_SENTENCE:
            speakText(getString(R.string.next_sentence));
            break;
        case Constants.PREVIOUS_SENTENCE:
            speakText(getString(R.string.previous_sentence));
            break;
        case Constants.PLAY:
            speakText(getString(R.string.play));
            break;
        case Constants.PAUSE:
            speakText(getString(R.string.pause));
            break;
        default:
            break;
        }
    }

}