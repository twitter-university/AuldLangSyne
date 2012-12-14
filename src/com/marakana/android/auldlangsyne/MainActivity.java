package com.marakana.android.auldlangsyne;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private static final int COLOR_INPUT_REQUEST = 0;
	private Spinner toneInput;
	private EditText textInput;
	private EditText textSize;
	private int color = Color.WHITE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_main);
		this.toneInput = (Spinner) super.findViewById(R.id.tone_input);
		this.textInput = (EditText) super.findViewById(R.id.text_input);
		this.textSize = (EditText) super.findViewById(R.id.text_size);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("color", this.color);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		this.color = savedInstanceState.getInt("color", Color.WHITE);
	}

	public void chooseColor(View view) {
		Intent intent = new Intent(this, ColorChooserActivity.class);
		intent.putExtra("color", this.color);
		super.startActivityForResult(intent, COLOR_INPUT_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == COLOR_INPUT_REQUEST && resultCode == RESULT_OK) {
			this.color = data.getIntExtra("color", Color.WHITE);
			Log.d(TAG, "Got color " + Integer.toHexString(color));
		}
	}

	public void run(View view) {
		Object tone = this.toneInput.getSelectedItem();
		if (tone == null) {
			Toast.makeText(this, "Select a tone to play", Toast.LENGTH_SHORT)
					.show();
		} else if (TextUtils.isEmpty(this.textInput.getText())) {
			this.textInput.setError("Enter text to display");
		} else {
			CharSequence textSizeCS = this.textSize.getText();
			try {
				float textSize = TextUtils.isEmpty(textSizeCS) ? 400f : Float
						.parseFloat(textSizeCS.toString());

				if (this.color == Color.BLACK) {
					Toast.makeText(this,
							"You may want to select a color other than black",
							Toast.LENGTH_SHORT).show();
				}
				Intent intent = new Intent(this, PlaybackActivity.class);
				intent.putExtra("tone", tone.toString());
				intent.putExtra("color", this.color);
				intent.putExtra("text", this.textInput.getText());
				intent.putExtra("textSize", textSize);
				super.startActivity(intent);
			} catch (NumberFormatException e) {
				this.textSize.setError("Enter the size of the text to display");
			}
		}
	}
}
