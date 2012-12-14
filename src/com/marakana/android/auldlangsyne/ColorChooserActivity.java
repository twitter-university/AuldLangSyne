package com.marakana.android.auldlangsyne;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ColorChooserActivity extends Activity {
	private static final String TAG = "ColorChooserActivity";
	private EditText colorInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_color_chooser);
		this.colorInput = (EditText) super
				.findViewById(R.id.color_chooser_entry);
		Intent intent = super.getIntent();
		int color = intent.getIntExtra("color", Color.BLACK);
		if (color != Color.BLACK) {
			this.colorInput.setText(toStringColor(color));
		}
	}

	public void pickColor(View view) {
		this.returnColor(((ColorDrawable) view.getBackground()).getColor());
	}

	public void ok(View view) {
		String colorInput = this.colorInput.getText().toString();
		try {
			returnColor(0xff000000 | Integer.parseInt(colorInput, 16));
		} catch (NumberFormatException e) {
			Log.w(TAG, "Failed to parse " + colorInput, e);
			this.colorInput.setError("Please enter color as RRGGBB");
		}
	}

	private void returnColor(int color) {
		Log.d(TAG, "Got color " + Integer.toHexString(color));
		Intent data = new Intent();
		data.putExtra("color", color);
		super.setResult(RESULT_OK, data);
		super.finish();
	}

	public void cancel(View view) {
		super.setResult(RESULT_CANCELED);
		super.finish();
	}

	private String toStringColor(int color) {
		color &= 0x00ffffff;
		String sColor = Integer.toHexString(color);
		sColor = sColor.toUpperCase(Locale.US);
		while (sColor.length() < 6) {
			sColor = "0" + sColor;
		}
		return sColor;
	}
}
