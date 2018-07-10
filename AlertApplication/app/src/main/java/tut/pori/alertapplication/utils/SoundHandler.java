/*
 * Copyright 2017 Tampere University of Technology, Pori Department
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tut.pori.alertapplication.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Locale;

import tut.pori.alertapplication.R;
import tut.pori.alertapplication.datatypes.Alert;

/**
 *
 */
public class SoundHandler extends UtteranceProgressListener implements Closeable, TextToSpeech.OnInitListener {
	private static final String CLASS_NAME = SoundHandler.class.toString();
	private Context _context = null;
	private SoundPool _soundPool = null;
	private HashMap<Object, Integer> _soundIds = null;
	private TextToSpeech _tts = null;

	@Override
	public void onStart(String utteranceId) {
		Log.d(CLASS_NAME, "onStart: "+utteranceId);
	}

	@Override
	public void onDone(String utteranceId) {
		Log.d(CLASS_NAME, "onDone: "+utteranceId);
	}

	@Override
	public void onError(String utteranceId) {
		// deprecated Android listener
	}

	@Override
	public void onError(String utteranceId, int errorCode) {
		Log.w(CLASS_NAME, "onError: "+utteranceId+", errorCode: "+errorCode);
	}

	/**
	 * sounds for various events
	 */
	public enum EventType {
		/**
		 * new alerts have become in-range
		 */
		NEW_ALERTS_IN_RANGE,
		/**
		 * no active alert
		 */
		NO_ACTIVE_ALERT,
		/**
		 * no alerts are in range
		 */
		NO_ALERTS_IN_RANGE,
		/**
		 * main (idle) view is visible
		 */
		MAIN_VIEW;
		;

		/**
		 *
		 * @return audio resource if
		 */
		public int getAudioResource(){
			switch(this){
				case NEW_ALERTS_IN_RANGE:
					return R.raw.new_alerts;
				case NO_ACTIVE_ALERT:
					return R.raw.no_active;
				case NO_ALERTS_IN_RANGE:
					return R.raw.no_alerts_in_range;
				case MAIN_VIEW:
					return R.raw.main_view;
				default:
					throw new IllegalArgumentException("Unknown audio resource: "+this.name());
			}
		}
	} // enum SoundEvent

	@Override
	public void close() {
		_soundPool.release();
		shutdownTTS();
	}

	/**
	 * helper method for shutting down TTS
	 */
	private void shutdownTTS(){
		if(_tts != null){
			_tts.stop();
			_tts.shutdown();
			_tts = null;
		}
	}

	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS){
			int result = _tts.setLanguage(Locale.US);
			if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
				Log.e("error", "Language is not supported: "+Locale.US.toLanguageTag());
				shutdownTTS();
			}else{
				_tts.setOnUtteranceProgressListener(this);
			}
		}else{
			Log.e(CLASS_NAME, "Failes to initialize TTS");
			shutdownTTS();
		}
	}

	/**
	 *
	 * @param context
	 */
	public SoundHandler(Context context){
		_context = context;
		AudioAttributes attributes = new AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
				.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
				.build();

		_soundPool = new SoundPool.Builder()
				.setAudioAttributes(attributes)
				.setMaxStreams(2)
				.build();

		_soundIds = new HashMap<>();
		loadSounds(context);
	}

	/**
	 * load all sounds
	 *
	 * @param context
	 */
	private void loadSounds(Context context){
		for(Alert.AlertType type : Alert.AlertType.values()){
			_soundIds.put(type, _soundPool.load(context, type.getAudioResource(), 0));
		}
		for(EventType type : EventType.values()){
			_soundIds.put(type, _soundPool.load(context, type.getAudioResource(), 0));
		}
	}

	/**
	 *
	 * @param type
	 */
	public void play(EventType type) {
		if(isEnableTTS()){
			speak(type.name());
		}else if(_soundPool.play(_soundIds.get(type), 1f, 1f, 0, 0, 1f) < 0){
			Log.w(CLASS_NAME, "Failed to play sound of type "+type.name());
		}
	}

	/**
	 *
	 * @param type
	 * @throws IllegalArgumentException on unsupported type ({}@link Alert.AlertType#UNKNOWN})
	 */
	public void play(Alert.AlertType type) throws IllegalArgumentException {
		if(isEnableTTS()){
			speak(type.toAlertTypeString());
		}else if(_soundPool.play(_soundIds.get(type), 1f, 1f, 0, 0, 1f) < 0){
			Log.w(CLASS_NAME, "Failed to play sound of type "+type.name());
		}
	}

	/**
	 *
	 * @param text
	 */
	private void speak(String text){
		text = text.replace('_',' '); // remove underscores from the spoken text if present
		if(_tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text) != TextToSpeech.SUCCESS){
			Log.w(CLASS_NAME, "Failed to queue TTS for text: "+text);
		}
	}

	/**
	 *
	 * @param enable
	 * @see #isEnableTTS()
	 */
	public void setEnableTTS(boolean enable) {
		if(enable){
			if(_tts == null){
				_tts = new TextToSpeech(_context, this);
			}
		}else if(_tts != null){
			shutdownTTS();
		}
	}

	/**
	 *
	 * @return true if TTS is enabled
	 * @see #setEnableTTS(boolean)
	 */
	public boolean isEnableTTS() {
		return (_tts != null);
	}
}
