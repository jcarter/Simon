/*
 * Coding Style for Future Reference: https://source.android.com/source/code-style.html
 * 
 * Name: Justin Carter
 * Date: 2/8/2014
 * Assignment: #1 - Simon
 * Class: CSCI 4020
 * Description: This is the game Simon with a special appearance by Dr. Nicholson and Dr. Lee. 
 */

package edu.apsu.csci.simon;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
	
	private int highScore;
	private TextView highScoreTV;
	public final static String HIGH_SCORE_FIELD = "highScore";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        findViewById(R.id.beginButton).setOnClickListener(this);
        highScoreTV = (TextView)findViewById(R.id.highScoreTV);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Resets the highscore to 0
    	if (item.getItemId() == R.id.reset_high_score) {
    		highScore = 0;
    		highScoreTV.setText("High Score: " + highScore);
    		writeHighScore();    		
    		return true; // means the selection was handled
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
    	readHighScore();        
        highScoreTV.setText("High Score: " + highScore);
    	super.onResume();
    }

	@Override 
	// handles button image flash 
	public void onClick(View v) {
		Intent intent = new Intent(getApplicationContext(), Game.class);
		intent.putExtra(HIGH_SCORE_FIELD, highScore);
		startActivity(intent);
	}    
	
	/**  Reads in the file that contains the high score. If no file is 
	 *   found, a default value of 0 is used.*/
	void readHighScore() {
		Scanner scanner;
        try {
        	FileInputStream highScoreFile = openFileInput("high_scores");
        	scanner = new Scanner(highScoreFile);
        	
        	while (scanner.hasNext()) {
        		highScore = scanner.nextInt();
        		Log.i("SCORE", "is " + highScore);
        	}
        	scanner.close();
        }
        catch (FileNotFoundException e) { 
        	Log.i("CATCH", "File not found, default value of 0 set");
        	highScore = 0;
        }
	}  
	
	public void writeHighScore() {
		try {
			FileOutputStream outputFile = openFileOutput("high_scores", MODE_PRIVATE);
			OutputStreamWriter writer = new OutputStreamWriter(outputFile);
			BufferedWriter buf = new BufferedWriter(writer);
			PrintWriter printer = new PrintWriter(buf);
			
			printer.println(highScore);
			
			printer.close();
			Log.i("IT_WORKED", "File written, high score " + highScore);
			
		} catch (FileNotFoundException e) {
			Log.i("CATCH", "File could not be written out.");
		}
	}

}
