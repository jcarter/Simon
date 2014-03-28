/* 
 * Name: Justin Carter
 * Date: 2/8/2014
 * Assignment: #1 - Simon
 * Class: CSCI 4020
 */ 
 
package edu.apsu.csci.simon;

import java.lang.ref.Reference;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class SimonButton extends ImageView {
	
	private int loadSoundId;
	private int playSoundId;
	private int image;
	private int pressedImage;

	public SimonButton(Context context) {
		super(context);
		init(null);
	}

	public SimonButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public SimonButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	/** Initializes member variables with XML values. */
	private void init(AttributeSet attrs) {
		loadSoundId = 0;
		image = 0;
		pressedImage = 0;
		
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.SimonButton);
		
		Log.i("SimonButton", "loading XML");
		loadSoundId = a.getResourceId(R.styleable.SimonButton_soundId, loadSoundId);
		image = a.getResourceId(R.styleable.SimonButton_image, image);
		pressedImage = a.getResourceId(R.styleable.SimonButton_pressedImage, pressedImage);
		
		setImageResource(image);

		// Don't forget this
		a.recycle();
	}
	
	/** Flips the button's image for 100 ms and plays the sound associated 
	 * with it by using the Game class' static method. */
	void blinkButton() {
		setImageResource(pressedImage);
		this.postDelayed(new Runnable() {			
			@Override
			public void run() {
				setImageResource(image);	
			}
		}, 100);
		Game.playSound(playSoundId);
	}
	
	/** Allows Game to load the SoundPool with the SimonButton sound. */
	void setPlaySoundId(int soundId) {
		playSoundId = soundId;
	}
	
	@Override
	public boolean isPressed() {
		// TODO Auto-generated method stub
		return super.isPressed();
	}
	
	int getImage() {
		return image;
	}
	
	int getPressedImage() {
		return pressedImage;
	}
	
	int getSoundId() {
		return loadSoundId;
	}
	
}