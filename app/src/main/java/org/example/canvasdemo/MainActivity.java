package org.example.canvasdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
	
	MyView myView; //Mit custom view

	//Timers til, movement og game længde
	private Timer moveTimer;
	private Timer gameTimer;


	//Køre spillet?
	private boolean running = false;

	//Tids counter
	public final int defaultTime = 90; //Default tid
	private int counter = defaultTime; //Tidscounter der bruges


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		//Finder det View, hvor spillet skal tegnes
		myView = (MyView) findViewById(R.id.gameView);
		myView.setActivity(this); //Giver viewet en reference til aktiviteten

		if (savedInstanceState!=null)
		{
			//Genskaber værdier der er gemt
			ArrayList<Enemy> enemies = savedInstanceState.getParcelableArrayList("enemies");
			ArrayList<GoldCoin> coins = savedInstanceState.getParcelableArrayList("coins");
			myView.recreateGame(savedInstanceState.getInt("xPos"), savedInstanceState.getInt("yPos"),
					coins,
					savedInstanceState.getInt("level"), savedInstanceState.getInt("points"),
					savedInstanceState.getInt("highScore"), enemies, savedInstanceState.getBoolean("gameover"),savedInstanceState.getInt("direction"));

			running = savedInstanceState.getBoolean("running");
			counter = savedInstanceState.getInt("count");
		}
		else
		{
			running = true; // Starter spillet, hvis ingen stadier er gemt.
		}


		//Finder knapper
		Button buttonRight = (Button) findViewById(R.id.moveRight);
		Button buttonUp = (Button) findViewById(R.id.moveUp);
		Button buttonDown = (Button) findViewById(R.id.moveDown);
		Button buttonLeft = (Button) findViewById(R.id.moveLeft);

		//listener of our pacman
		buttonRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myView.setDirection(Direction.RIGHT);

				//myView.moveRight(5);
				//updatePointBoard();
			}
		});
		buttonLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myView.setDirection(Direction.LEFT);
				//updatePointBoard();
			}
		});
		buttonUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myView.setDirection(Direction.UP);
				//myView.moveUp(5);
				//updatePointBoard();
			}
		});
		buttonDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myView.setDirection(Direction.DOWN);
				//myView.moveDown(5);
				//updatePointBoard();
			}
		});

		//updatePointBoard();


		//make a new timer
		moveTimer = new Timer();

		//We will call the timer 5 times each second
		moveTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		}, 0, 120); //0 indicates we start now, 120
		//is the number of miliseconds between each call


		//Game timer laves
		gameTimer = new Timer();

		//Game timer planlægges
		//Starter med det samme, da 0, som først int parameter angiver.
		//Kør en gang i sec. (1000 milisekunder), som andet int parameter angiver.
		gameTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateGameTime();
			}
		}, 0, 1000);
	}

	@Override
	protected void onStop() {

		//Sørger for, at hvis brugeren stopper appen, så håndters dette, sådan der holdes pause for spillet.
		super.onStop();
		running = false;
		invalidateOptionsMenu(); //Gentegner menuen, da nederste punkt er ændret
	}


	@Override
	protected void onRestart() {
		//Sørger for, at hvis brugeren genåbner appen, så håndters dette, sådan spillet fortsættes.
		running = true;
		invalidateOptionsMenu(); //Gentegner menuen, da nederste punkt er ændret
		super.onRestart();
	}

	//*
	// Opdater spilletiden, og sætter game over, hvis tiden er brugt.
	// Ved hvert level fornyes tiden i det custom view
	// */
	private void updateGameTime(){
		if (running)
		{
			counter--;

			//Tiden er gået
			if(counter == 0){
				myView.setGameOver(true);
				invalidateOptionsMenu(); //Gentegner menuen, da nederste punkt er ændret
			}
		}
	}

	private void TimerMethod()
	{
		//This method is called directly by the timer
		//and runs in the same thread as the timer.

		//We call the method that will work with the UI
		//through the runOnUiThread method.
		this.runOnUiThread(Timer_Tick);
	}


	private Runnable Timer_Tick = new Runnable() {
		public void run() {

			//This method runs in the same thread as the UI.
			// so we can draw
			if (running)
			{
				int minTime = 25;
				//Beregner hastighed på pacman, hvilket er baseret på level
				int speed = minTime - myView.getLevel()*3;
				if(speed < 5) {
					speed = minTime;
				}
				myView.move(speed); //Bevæger pacman og fjender.
			}

		}
	};


	/** Opdater game board, hvilket er der, hvor brugeren ser spillets status. */
	public void updatePointBoard(){

		int currentPoints = myView.getPoints();
		TextView pointsBoard = (TextView) findViewById(R.id.points);
		pointsBoard.setText("Points: " + currentPoints + ", Level: " + myView.getLevel() +", Time left: " + counter + " sec." +
				", Highscore: " +myView.getHighScore());


	}

	/** Resætter game timer **/
	public void resetGameTimer(int time){
		gameTimer.cancel();
		gameTimer = new Timer();
		gameTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateGameTime();
			}
		}, 0, 1000);

		if(time <= 30) {
			time = 30;
		}

		counter = time;
	}

	/** Resætter move timer **/
	public void resetMoveTimer(){
		moveTimer.cancel();
		moveTimer = new Timer();
		moveTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		}, 0, 120);

		running = true;
	}

	public void timerStop(){
		//Logik der skal til, hvis game over

		gameTimer.cancel();
		gameTimer.cancel();
		running = false;

		if(counter < 0){
			counter = 0;
		}
	}

	/**
	 * Beregner teksten der skal bruges til nederste menupunkt.
	 * Hvilket er en knap til, at pause og fortsæt spillet.
	 * **/
	private String calcRunBtnText()
	{
		String titleItem = "Pause";
		if(!running) {
			titleItem = "Resume";
		}

		return titleItem;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		MenuItem item= menu.findItem(R.id.runStatus);
		item.setTitle(calcRunBtnText());

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem item= menu.findItem(R.id.runStatus);

		if(myView.isGameOver())	{
			item.setEnabled(false);
		}
		else {  item.setEnabled(true); }

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.


		int id = item.getItemId();
		if(id == R.id.newgame) {
			myView.newGame(); //Starter nyt spil
			updatePointBoard(); //Opdater game board, hvilket visningen af spillets status.
			running = true;
			invalidateOptionsMenu();
		}
		else if(id == R.id.shareScore) {
			//Deling af highscore og point
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, "Pacman highscore is " + myView.getHighScore() + ", og my points is " +myView.getPoints()+ ".");
			sendIntent.setType("text/plain");
			startActivity(sendIntent);
		}
		else if(id == R.id.runStatus){
			//Pauser eller fortsætter spillet
			running = !running;
			item.setTitle(calcRunBtnText());
		}

		return super.onOptionsItemSelected(item);
	}


	//** Gemmer værder der skal bruges til, at genskabe spil, når skærm orientation ændres. /når app dør. *//
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelableArrayList("coins", myView.getCoins());
		outState.putInt("xPos", myView.getPacx());
		outState.putInt("yPos", myView.getPacy());
		outState.putInt("level", myView.getLevel());
		outState.putInt("points", myView.getPoints());
		outState.putInt("highScore", myView.getHighScore());
		outState.putBoolean("running", running);
		outState.putInt("count", counter);

		outState.putBoolean("gameover", myView.isGameOver());
		outState.putParcelableArrayList("enemies", myView.getEnemies());
		outState.putInt("direction", myView.getDirection().ordinal());
	}
}
