package com.marakana.android.auldlangsyne;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class PlaybackActivity extends Activity implements OnTouchListener,
		OnErrorListener, OnPreparedListener, OnCompletionListener, Runnable {
	private static final String TAG = "PlaybackActivity";
	private final int MAX_VOLUME = 32; // the number of "fade" steps
	private final int FADE_DELAY = 2; // delay (in ms) between fade steps
	private int currentVolume = MAX_VOLUME;
	private View frame;
	private TextView content;
	private int color;
	private MediaPlayer mediaPlayer;
	private WakeLock wakeLock;
	private Handler fadeOutHandler = new Handler();
	private int toneR;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.setContentView(R.layout.activity_playback);

		Intent intent = super.getIntent();
		this.color = intent.getIntExtra("color", Color.BLACK);
		CharSequence text = intent.getCharSequenceExtra("text");
		float textSize = intent.getFloatExtra("textSize", 400f);
		String tone = intent.getStringExtra("tone");
		this.toneR = getToneResource(tone);

		if (this.toneR > 0) {
			Log.d(TAG, "Got resource [" + this.toneR + "] for tone [" + tone
					+ "]");
		} else {
			Log.wtf(TAG, "Failed to get soruce for tone [" + tone + "]: "
					+ this.toneR);
			this.finish();
			return;
		}

		this.frame = super.findViewById(R.id.fullscreen_frame);

		this.content = (TextView) super.findViewById(R.id.fullscreen_content);
		this.content.setText(text);
		this.content.setTextColor(this.color);
		this.content.setVisibility(View.INVISIBLE);
		this.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

		super.findViewById(R.id.showContent).setOnTouchListener(this);
		super.findViewById(R.id.playTone).setOnTouchListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		this.wakeLock = ((PowerManager) super.getSystemService(POWER_SERVICE))
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		this.wakeLock.acquire();

		if (this.toneR > 0) {
			this.mediaPlayer = new MediaPlayer();
			this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

			try {
				AssetFileDescriptor afd = this.getResources()
						.openRawResourceFd(this.toneR);
				try {
					this.mediaPlayer.setDataSource(afd.getFileDescriptor(),
							afd.getStartOffset(), afd.getLength());
					this.mediaPlayer.setOnErrorListener(this);
					this.mediaPlayer.setOnPreparedListener(this);
					this.mediaPlayer.setOnCompletionListener(this);
					this.mediaPlayer.prepareAsync();
				} finally {
					afd.close();
				}
			} catch (IOException e) {
				Log.wtf("Failed to load tone: " + this.toneR, e);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (this.mediaPlayer != null) {
			this.mediaPlayer.stop();
			this.mediaPlayer.release();
			this.mediaPlayer = null;
		}
		if (this.wakeLock != null) {
			this.wakeLock.release();
			this.wakeLock = null;
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (view.getId()) {
		case R.id.showContent:
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.d(TAG, "Showing content");
				this.content.setVisibility(View.VISIBLE);
				return true;
			case MotionEvent.ACTION_UP:
				Log.d(TAG, "Hiding content");
				this.content.setVisibility(View.INVISIBLE);
				return true;
			}
			break;
		case R.id.playTone:
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.d(TAG, "Playing tone");
				this.fadeOutHandler.removeCallbacks(this);
				this.currentVolume = MAX_VOLUME;
				this.mediaPlayer.setVolume(1, 1);
				this.mediaPlayer.seekTo(0);
				this.mediaPlayer.start();
				this.frame.setBackgroundColor(this.color);
				return true;
			case MotionEvent.ACTION_UP:
				Log.d(TAG, "Stopping playback");
				this.frame.setBackgroundColor(Color.BLACK);
				this.fadeOutHandler.post(this);
				return true;
			}
			break;
		}
		return false;
	}

	private int getToneResource(String tone) {
		if (tone == null) {
			Log.wtf(TAG, "No tone!");
			return -1;
		} else if (tone.equals("A3")) {
			return R.raw.a3;
		} else if (tone.equals("C3")) {
			return R.raw.c3;
		} else if (tone.equals("C4")) {
			return R.raw.c4;
		} else if (tone.equals("D3")) {
			return R.raw.d3;
		} else if (tone.equals("D4")) {
			return R.raw.d4;
		} else if (tone.equals("F3")) {
			return R.raw.f3;
		} else if (tone.equals("G3")) {
			return R.raw.g3;
		} else {
			Log.wtf(TAG, "No such tone: " + tone);
			return 0;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		Log.d(TAG, "Ready to rock'n'roll");
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		Log.e(TAG, String.format(
				"Music player encountered an error: what=%d, extra=%d", what,
				extra));
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		Log.d(TAG, "Finished playback");
		this.currentVolume = MAX_VOLUME;
		this.mediaPlayer.setVolume(1, 1);
		this.mediaPlayer.seekTo(0);
		this.fadeOutHandler.removeCallbacks(this);
	}

	public void run() {
		if (this.mediaPlayer != null) {
			if (this.currentVolume > 0 && this.mediaPlayer.isPlaying()) {
				this.currentVolume--;
				float volume = 1 - (float) (Math
						.log(MAX_VOLUME - currentVolume) / Math.log(MAX_VOLUME));
				this.mediaPlayer.setVolume(volume, volume);
				this.fadeOutHandler.postDelayed(this, FADE_DELAY);
				Log.d(TAG, "Fading to " + volume);
			} else {
				Log.d(TAG, "done fading");
				this.currentVolume = MAX_VOLUME;
				this.mediaPlayer.setVolume(1, 1);
				this.mediaPlayer.pause();
				this.mediaPlayer.seekTo(0);
			}
		}
	}
}
