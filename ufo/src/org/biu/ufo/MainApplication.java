package org.biu.ufo;

import static edu.cmu.pocketsphinx.Assets.syncAssets;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.androidannotations.annotations.EApplication;

import android.app.Application;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import edu.cmu.pocketsphinx.SpeechRecognizer;

@EApplication
public class MainApplication extends Application {
	public static final String TAG = "MainApplication";
	public static final String VOICE_DESTINATION = "VOICE_DESTINATION";
	public static final String VOICE_POPUP = "POPUP";
	
	private TextToSpeech ttobj;
    private SpeechRecognizer recognizer;

	@Override
	public void onCreate() {
		super.onCreate();
		
		loadTextToSpeech();
		loadPocketPhinx();
	}
		
	private void loadTextToSpeech() {
		ttobj = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if(status != TextToSpeech.ERROR){
					ttobj.setLanguage(Locale.US);
				}				
			}
		});
	}
		
	public void startTextToSpeech(String text) {
		stopTextToSpeech();
		if(ttobj.isLanguageAvailable(Locale.ENGLISH) != TextToSpeech.LANG_NOT_SUPPORTED) {
//		if(ttobj.isLanguageAvailable(locale) != TextToSpeech.LANG_NOT_SUPPORTED) {		
		    ttobj.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	public void stopTextToSpeech() {
		if(ttobj !=null ){
			ttobj.stop();
		}
	}

    private void loadPocketPhinx() {
        File appDir;
        try {
            appDir = syncAssets(this);
        } catch (IOException e) {
            throw new RuntimeException("failed to synchronize assets", e);
        }
        recognizer = defaultSetup()
                .setAcousticModel(new File(appDir, "models/hmm/en-us-semi"))
                .setDictionary(new File(appDir, "models/lm/cmu07a.dic"))
                .setRawLogDir(appDir)
                .setKeywordThreshold(1e-5f)
                .getRecognizer();
        
        File popupGrammar = new File(appDir, "models/grammar/popup.gram");
        recognizer.addGrammarSearch(VOICE_POPUP, popupGrammar);
        
        File keywordsPath = new File(appDir, "models/keywords/keywords.txt");
        recognizer.addKeywordSearch(VOICE_DESTINATION, keywordsPath.getAbsolutePath());
    }
    
    public SpeechRecognizer getRecognizer() {
    	return recognizer;
    }
    
	public void startListening(String searchType) {
		Log.e("TEST", "startListening");

		//stopListening(searchType);
		recognizer.stop();
        recognizer.startListening(searchType);
	}
	
	public void stopListening(String searchType) {
		if(TextUtils.equals(recognizer.getSearchName(), searchType)) {
			Log.e("TEST", "stopListening");
	        recognizer.stop();			
		}
	}
	
}
