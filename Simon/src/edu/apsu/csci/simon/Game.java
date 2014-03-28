/* 
 * Name: Justin Carter
 * Date: 2/8/2014
 * Assignment: #1 - Simon
 * Class: CSCI 4020
 */

package edu.apsu.csci.simon;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Runs the game. It includes instances of SimonButton
 * and an inner Computer class that controls Simon.
 */
public class Game extends Activity implements OnTouchListener {

	private static SoundPool soundPool;
	private ImageView centerImageIV;
	private ImageView magicLeeIV;
	private SimonButton[] button = new SimonButton[4];
	private Button newGameButton;
	private TextView playersTurnTV;
	private TextView roundNumberTV;
	private boolean isPlayersTurn;
	private boolean playerFinishedTurn;
	private List<Integer> computersChoices;
	private int playersChoices;
	private int turnNumber;
	private int roundNumber;
	
	private HashMap<Integer, Integer> endingSounds; //stores possible sounds for losing
	private int amIGoingTooFastSoundId;
	private int askToReceiveSoundId;
	private int highScore;
	final private int GREEN_BUTTON = 0, YELLOW_BUTTON = 1, BLUE_BUTTON = 2,	RED_BUTTON = 3;

	private Simon simon; // is null by default

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_player);
		
	// GET HIGH SCORE FROM MAIN_ACTIVITY
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		highScore = extras.getInt(MainActivity.HIGH_SCORE_FIELD);
		
	// INITIALIZE VARIABLES
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		computersChoices = new ArrayList<Integer>();
		roundNumber = 1;
		playerFinishedTurn = true; // must be initialized to true for the first turn
		int buttonIds[] = {R.id.greenButton, R.id.yellowButton, R.id.blueButton, R.id.redButton};
		newGameButton = (Button) findViewById(R.id.newGameButton);
		newGameButton.setOnTouchListener(this);
		
		centerImageIV = (ImageView) findViewById(R.id.centerImage);
		magicLeeIV = (ImageView) findViewById(R.id.magicLee);
		playersTurnTV = (TextView) findViewById(R.id.playersTurnTV);
		roundNumberTV = (TextView) findViewById(R.id.roundNumberTV);
		
	// INITIALIZE BUTTON SOUNDS AND TOUCH LISTENERS
		for (int b = 0; b < button.length; b++) {
			button[b] = (SimonButton)findViewById(buttonIds[b]);
			button[b].setPlaySoundId(soundPool.load(this, button[b].getSoundId(), 1));
			button[b].setOnTouchListener(this);
		}		
		
	// POSSIBLE LOSING SOUNDS. JUST ADD TO MAP AND IT'LL HAVE A CHANCE OF BEING SELECTED
		endingSounds = new HashMap<Integer, Integer>();
		endingSounds.put(0, soundPool.load(this, R.raw.haha, 1));
		endingSounds.put(1, soundPool.load(this, R.raw.i_finally_win_one, 1));
		endingSounds.put(2, soundPool.load(this, R.raw.what_the, 1));
		endingSounds.put(3, soundPool.load(this, R.raw.he_had_balls, 1));
		endingSounds.put(4, soundPool.load(this, R.raw.holy_crap, 1));
		
		amIGoingTooFastSoundId = soundPool.load(this, R.raw.am_i_going_too_fast, 1);
		askToReceiveSoundId = soundPool.load(this, R.raw.ask_ye_receiver, 1);		
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("RESUME", "onResume called" + isPlayersTurn + " " + playerFinishedTurn); 
		
	// CREATE COMPUTER IF NEEDED 
		if (simon == null) {
			simon = new Simon();
			simon.execute();
		} else {
			Log.i("INFO", "Computer already running");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("PAUSE", "onPause called" + isPlayersTurn + " " + playerFinishedTurn);
		if (simon != null) {
			simon.cancel(true);
			simon = null;
		}
		soundPool.autoPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("DESTROYED", "onDestroy called");
		soundPool.release();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Method that is used by the SimonButton class to access the SoundPool in order to play a sound. */
	public static void playSound(int soundId) {
		soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.i("INFO", "onTouch Called");
			if (isPlayersTurn) {
				if(v.getId() == button[0].getId()) {
					button[0].blinkButton();
					playersChoices = GREEN_BUTTON;
				} else if(v.getId() == button[1].getId()) {
					button[1].blinkButton();	
					playersChoices = YELLOW_BUTTON;
				} else if(v.getId() == button[2].getId()) {
					button[2].blinkButton();
					playersChoices = BLUE_BUTTON;
				} else {
					button[3].blinkButton();
					playersChoices = RED_BUTTON;
				}
				
				checkPlayerChoice();
				
				return true;
			} else {
				if (v.getId() == newGameButton.getId()) {
					startNewGame();
				}
			}
		}		
		return false;
	}
	
	/** Checks each button that the player touches to make sure it's the right one. */
	void checkPlayerChoice() {
		if (playersChoices == computersChoices.get(turnNumber-1)) { //If player's button choice is correct.
			if (simon == null && turnNumber == computersChoices.size()) { // If player wins the round. 
				playerFinishedTurn = true;
				simon = new Simon();
				
				// Check for new high score.
				if(roundNumber > highScore) {
					highScore = roundNumber;
					Toast toast = Toast.makeText(this, "NEW HIGH SCORE!!!", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					writeHighScore();
				}
				
				// Make Dr. Lee pop up every 5 turns
				if (roundNumber % 5 == 0) {
					popUpDrLee();
				}
				
				roundNumber++;
				simon.execute(); // make Simon start next round
			} else {
				Log.i("Game.checkPlayersChoice", "Player still has " + (computersChoices.size()-turnNumber) + " buttons left");
			}
		} else { // This else runs if the player chooses the wrong button.
			Log.i("Game.playersChoice", "INCORRECT");
			isPlayersTurn = false;
			playerFinishedTurn = false;
			roundNumberTV.setVisibility(View.INVISIBLE);
			newGameButton.setVisibility(View.VISIBLE);
			playersTurnTV.setText("You Lose...");
			
			spinNicholsonHead();			
			playLosingSound();
		} 
		 
		turnNumber++;
	}
	
	/** Plays a random sound that has been loaded into the endSounds hashmap. */
	public void playLosingSound() {
		Random r = new Random();
		soundPool.play(endingSounds.get(r.nextInt(endingSounds.size())), 1, 1, 0, 0, 1);
	}
	
	/** Restarts the game by resetting the computersChoices array and the roundNumber. */
	public void startNewGame() {
		Log.i("INFO", "startNewGame called");
		roundNumber = 1;
		playerFinishedTurn = true;
		computersChoices.clear(); 
		soundPool.play(askToReceiveSoundId, 1, 1, 0, 0, 1);
		centerImageIV.setImageResource(R.drawable.small_eyes_with_background);
		newGameButton.setVisibility(View.INVISIBLE);
		roundNumberTV.setVisibility(View.VISIBLE);
		if (simon == null) {
			simon = new Simon();
			simon.execute();
		} else {
			Log.i("INFO", "Computer already running");
		}
	}
	
	public void writeHighScore() {
		try {
			FileOutputStream outputFile = openFileOutput("high_scores", MODE_PRIVATE);
			OutputStreamWriter writer = new OutputStreamWriter(outputFile);
			BufferedWriter buf = new BufferedWriter(writer);
			PrintWriter printer = new PrintWriter(buf);
			
			printer.println(turnNumber);
			
			printer.close();
			Log.i("HIGH_SCORE", "File written, high score " + turnNumber);
			
		} catch (FileNotFoundException e) {
			Log.i("CATCH", "File could not be written out.");
		}
	}

	/** Controls Dr. Lee animation. */
	void popUpDrLee() {
		//http://developer.android.com/guide/topics/graphics/view-animation.html 
		Animation leePopup = AnimationUtils.loadAnimation(this, R.anim.lee_popup);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				soundPool.play(amIGoingTooFastSoundId, 1, 1, 0, 0, 1);
			}
		}, 2000);
		magicLeeIV.startAnimation(leePopup);
	}
	
	/** Controls Dr. Nicholson animation. */
	void spinNicholsonHead() {
		Animation bugEyes = AnimationUtils.loadAnimation(this, R.anim.spin_head);
		centerImageIV.startAnimation(bugEyes); 
		centerImageIV.setImageResource(R.drawable.big_eyes_with_background);
	}
	
	/** Inner class that is used to generate and save random button presses by Simon. */
	public class Simon extends AsyncTask<Void, Integer, Void> {
		
		Random randomButtonGenerator = new Random();

		@Override
		protected void onPreExecute() {
			if (newGameButton.getVisibility() != View.VISIBLE) {
				playersTurnTV.setText("");
				roundNumberTV.setText("Round " + roundNumber);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				isPlayersTurn = false;
				turnNumber = 1;
				if (playerFinishedTurn) {
					int buttonNumber = randomButtonGenerator.nextInt(4);
					computersChoices.add(buttonNumber);
					playerFinishedTurn = false;
					Log.i("RANDOM_BUTTON", "i = " + buttonNumber);
				} 
				
				// fixes the problem when player loses and then exits the game so they can start again
				if(newGameButton.getVisibility() != View.VISIBLE) {
					Thread.sleep(1500); 
					for (int i = 0; i < computersChoices.size(); i++) {
						Thread.sleep(500); // controls game speed
						publishProgress(computersChoices.get(i));
					}
				}
				

			}
			catch (InterruptedException e) {
				Log.i("VALUES", "----- INTERUPTED -----");
			}
			simon = null;
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.i("INFO", "inside onProgress Update");
			button[values[0]].blinkButton();
		}

		@Override
		protected void onPostExecute(Void result) {
			if(newGameButton.getVisibility() != View.VISIBLE) {
				playersTurnTV.setText("Your Turn...");
				isPlayersTurn = true;
			}
		}
	}

}