package org.biu.ufo.services;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.biu.ufo.OttoBus;
import org.biu.ufo.events.SpeechStartCommand;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

@EService
public class AlwaysListeningSpeechRecognizerService extends Service
{
	private static final String TAG = "AlwaysListeningSpeechRecognizerService";

	protected AudioManager mAudioManager; 
	protected SpeechRecognizer mSpeechRecognizer;
	protected Intent mSpeechRecognizerIntent;
	protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

	protected boolean mIsListening;
	protected volatile boolean mIsCountDownOn;
	private boolean mIsStreamSolo;

	static final int MSG_RECOGNIZER_START_LISTENING = 1;
	static final int MSG_RECOGNIZER_CANCEL = 2;

	@Bean
	OttoBus bus;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
				this.getPackageName());
		
		try {
			mServerMessenger.send(Message.obtain(null, MSG_RECOGNIZER_START_LISTENING));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static class IncomingHandler extends Handler
	{
		private WeakReference<AlwaysListeningSpeechRecognizerService> mtarget;

		IncomingHandler(AlwaysListeningSpeechRecognizerService target)
		{
			mtarget = new WeakReference<AlwaysListeningSpeechRecognizerService>(target);
		}


		@Override
		public void handleMessage(Message msg)
		{
			final AlwaysListeningSpeechRecognizerService target = mtarget.get();
			if(target == null)
				return;

			switch (msg.what)
			{
			case MSG_RECOGNIZER_START_LISTENING:

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				{
					// turn off beep sound  
					if (!target.mIsStreamSolo)
					{
						target.mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
						target.mIsStreamSolo = true;
					}
				}
				if (!target.mIsListening)
				{
					target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
					target.mIsListening = true;
					//Log.d(TAG, "message start listening"); //$NON-NLS-1$
				}
				break;

			case MSG_RECOGNIZER_CANCEL:
				if (target.mIsStreamSolo)
				{
					target.mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
					target.mIsStreamSolo = false;
				}
				target.mSpeechRecognizer.cancel();
				target.mIsListening = false;
				//Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
				break;
			}
		} 
	} 

	// Count down timer for Jelly Bean work around
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000)
	{

		@Override
		public void onTick(long millisUntilFinished)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onFinish()
		{
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
			try
			{
				mServerMessenger.send(message);
				message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
				mServerMessenger.send(message);
			}
			catch (RemoteException e)
			{

			}
		}
	};

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (mIsCountDownOn)
		{
			mNoSpeechCountDown.cancel();
		}
		if (mSpeechRecognizer != null)
		{
			mSpeechRecognizer.destroy();
		}
	}

	protected class SpeechRecognitionListener implements RecognitionListener
	{

		@Override
		public void onBeginningOfSpeech()
		{
			// speech input will be processed, so there is no need for count down anymore
			if (mIsCountDownOn)
			{
				mIsCountDownOn = false;
				mNoSpeechCountDown.cancel();
			}               
			//Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
		}

		@Override
		public void onBufferReceived(byte[] buffer)
		{

		}

		@Override
		public void onEndOfSpeech()
		{
			//Log.d(TAG, "onEndOfSpeech"); //$NON-NLS-1$
		}

		@Override
		public void onError(int error)
		{
			if (mIsCountDownOn)
			{
				mIsCountDownOn = false;
				mNoSpeechCountDown.cancel();
			}
			mIsListening = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
			try
			{
				mServerMessenger.send(message);
			}
			catch (RemoteException e)
			{

			}
			//Log.d(TAG, "error = " + error); //$NON-NLS-1$
		}

		@Override
		public void onEvent(int eventType, Bundle params)
		{

		}

		@Override
		public void onPartialResults(Bundle partialResults)
		{

		}

		@Override
		public void onReadyForSpeech(Bundle params)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			{
				mIsCountDownOn = true;
				mNoSpeechCountDown.start();

			}
			Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
		}

		@Override
		public void onResults(Bundle results)
		{
			ArrayList<String> values = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			for(String value : values) {
				Log.d(TAG, value); //$NON-NLS-1$
				if(value.toLowerCase().contains("hey ufo") || value.toLowerCase().contains("oracle")) {
					bus.post(new SpeechStartCommand());
					break;
				}
			}
			
			mIsListening = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
			try
			{
				mServerMessenger.send(message);
			}
			catch (RemoteException e)
			{

			}

			//Log.d(TAG, "onResults"); //$NON-NLS-1$

		}

		@Override
		public void onRmsChanged(float rmsdB)
		{

		}

	}


	public class AlwaysListeningSpeechRecognizerBinder extends Binder {
		AlwaysListeningSpeechRecognizerService getService() {
			return AlwaysListeningSpeechRecognizerService.this;
		}
	};
	
    private final IBinder mBinder = new AlwaysListeningSpeechRecognizerBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}
