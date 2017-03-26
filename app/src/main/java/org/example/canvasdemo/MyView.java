package org.example.canvasdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class MyView extends View{
	MainActivity activity; //Reference til, den aktivitet der bruger viewet, da det skal bruges metoder derfra.
	
	Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pacman); //Pacman bitmap
    Bitmap gostBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gost); //Gost bitmap


    //The coordinates for our dear pacman: (0,0) is the top-left corner
	private int pacx = 50;
	private int pacy = 400;
	private int h,w; //used for storing our height and width

	private int highScore;

    //Bruges til highscore beregning/hentning og gemning
	private SharedPreferences sharedPref;
	private String savedhightScore;

    private boolean gameOver = false; //Indiker om game over
    private int points; //Point counter
	private int level = 1; //Level counter

	private Direction direction = Direction.LEFT; //retningen pacmanen skal bevæge sig

	//Mønter pacman kan tage
	ArrayList<GoldCoin> coins = new ArrayList<>();

    //Enemies ArrayList -> så muligt med flere fjender
    ArrayList<Enemy> enemies = new ArrayList<>();

	/* The next 3 constructors are needed for the Android view system,
	when we have a custom view.
	 */
	public MyView(Context context) {
		super(context);

	}
	public MyView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}
	public MyView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context,attrs,defStyleAttr);
	}


	//In the onDraw we put all our code that should be
	//drawn whenever we update the screen.
	@Override
	protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); //Kald til super klassens metode

		//Får højde og bredde på canvas
		h = canvas.getHeight();
		w = canvas.getWidth();

        //Highscore sættelse
		this.sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		this.savedhightScore = activity.getString(R.string.saved_high_score);
		this.highScore = sharedPref.getInt(savedhightScore, 0);


		//Making a new paint object
		Paint paint = new Paint();


		//setting the color
		paint.setColor(Color.rgb(218,165,32)); //Den farve mønterne får
		canvas.drawColor(Color.WHITE); //clear entire canvas to white color


		//Et nyt spil laves hvis ingen mønter er der, da det her betyder spillet ikke er startet endnu
		if(coins.size() == 0){
			newGame();
		}

        if(points == coins.size())
        {
            addCoins(); //Tilføjer 10 nye mønter, da alle er taget.
            level += 1; //Går til næste level
            activity.resetGameTimer(activity.defaultTime-(level*10)+10); //Resætter spille timer
        }


		for(GoldCoin coin:coins)
		{
			//Tjekker om mønten er taget = mønt skal tegnes, hvis den ikke er
			if(!coin.isTaken()) {

				//Tjekker mønten er på skærmen, og laver ny position, hvis ikke er.
                // vilket er muligt, ved ændring at skærm orientation
				if(coin.getX() <= 20 || coin.getX() > w-20){
					coin.setX(randomInt(w-20));
				}

				if(coin.getY() <= 20 || coin.getY() > h-20){
					coin.setY(randomInt(h-20));
				}

				//Tegner mønt
				canvas.drawCircle(coin.getX(), coin.getY(), 10, paint);
			}
		}

		//Tilpasser spillets fjender ift. niveau
        switch (level)
        {
            case 3:
                enemies.get(0).setLevel(Level.NEUTRAL);

                if(enemies.size() == 1) {
                    addEnemy();
                }
                break;
            case 6:
                enemies.get(0).setLevel(Level.HARD);
                enemies.get(1).setLevel(Level.NEUTRAL);

                if(enemies.size() == 2) {
                    addEnemy();
                }
                break;
            case 9:
                enemies.get(0).setLevel(Level.SUPER_HARD);
                enemies.get(1).setLevel(Level.HARD);
                if(enemies.size() == 2) {
                   addEnemy();
                }

                break;
            case 10:
                enemies.get(1).setLevel(Level.SUPER_HARD);
                enemies.get(2).setLevel(Level.HARD);
                if(enemies.size() == 3) {
                    addEnemy();
                }

                break;
        }


        for(Enemy enemy:enemies){

            //Tjekker fjender er på skærmen, og laver ny position, hvis ikke er.
            //Hvilket er muligt, ved ændring at skærm orientation
            if(enemy.getXPos() < 0 || enemy.getXPos() > w-gostBitmap.getWidth()){
                enemy.setXPos(randomInt(w-gostBitmap.getWidth()));
            }

            if(enemy.getYPos() < 0 || enemy.getYPos() > h-gostBitmap.getHeight()){
                enemy.setYPos(randomInt(h-gostBitmap.getHeight()));
            }

            //Tegner fjende
            canvas.drawBitmap(gostBitmap, enemy.getXPos(), enemy.getYPos(), paint); //Tegner fjenen på skærmen
        }


		//Tjekker pacman er på skærmen, og laver ny position, hvis ikke er.
        //Hvilket er muligt, ved ændring at skærm orientation
		if(pacx < 0 || pacx > w-bitmap.getWidth()){ pacx =  randomInt(w-bitmap.getWidth()); }
		if(pacy < 0 || pacy > h-bitmap.getHeight()){ pacy = randomInt(h-bitmap.getHeight()); }

		activity.updatePointBoard();
		canvas.drawBitmap(bitmap, pacx, pacy, paint); //Tegner pacmanen på skærmen

        if(isGameOver()) {
            activity.timerStop(); //Stopper timers

            //Paint til tekst
            Paint paint2 = new Paint();

            //Sætter bg farve på paint
            paint2.setColor(Color.WHITE);

            //Sætter canvas paint
            canvas.drawPaint(paint2);

            //Ændre skrift
            paint2.setColor(Color.BLACK);
            paint2.setTextSize(175);
            paint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            //Beregner position
            int txtX = 50;
            int txtY = 70;


            if(h>w){
                canvas.rotate(45); //Roter, hvis portait mode
            }
            else
            {
                txtY= 200;
            }

            //tegner game over tekst
            canvas.drawText("GAME OVER", txtX, txtY, paint2);
        }
	}

	/** Metode til, at gentegne, da det skal bruges mange gange. */
	public void reDraw(){
        invalidate();
    }

	/**Laver og returner random int
       Metoden er lavet, da der skal bruges random mange gange. **/
	private int randomInt(int maks){
		Random random = new Random();
		return random.nextInt(maks);
	}


    /**
     * Flytter pacman og fjender
     * Beregner point
     * Beregner om, man er tæt på fjende
     * Pacman og fjernder gå til modsat hjørne, hvis vil være uden for skærm efter flytning **/
    public void move(int x){

        //Looper alle fjender
        for(Enemy enemy:enemies)
        {
            //Får postion
            int enemyYPost =  enemy.getYPos();
            int enemyXPost =  enemy.getXPos();

            //Angiver default hastighed
            int speed = 8;

            //Hastigheds tilpassning ift. level
            speed += enemy.getLevel().ordinal()*2;


            //Beregner om fjende skal skifte retning
            if(enemy.getDirectionChangeCounter() <= 0){
                enemy.moveCalc();
            }
            else
            {
                //Ændre på, hvornår der skal skiftets retning
                enemy.decreaseLength();
            }

            //Finder x og y postion på fjende
            switch (enemy.getDirection()){
                case DOWN:
                    if (enemyYPost+speed+gostBitmap.getHeight()<h)
                        enemyYPost=enemyYPost+speed;
                    else
                        enemyYPost = 0;
                    break;
                case UP:
                    if (enemyYPost-speed > 0)
                        enemyYPost=enemyYPost-speed;
                    else
                        enemyYPost = h-gostBitmap.getHeight();
                    break;
                case LEFT:
                    if (enemyXPost-speed > 0)
                        enemyXPost=enemyXPost-speed;
                    else
                        enemyXPost = w-gostBitmap.getWidth();
                    break;
                case RIGHT:
                    if (enemyXPost+speed+gostBitmap.getWidth()<w)
                        enemyXPost=enemyXPost+speed;
                    else
                        enemyXPost = 0;
                    break;
            }

            //Sætter ny postion
            enemy.setYPos(enemyYPost);
            enemy.setXPos(enemyXPost);
        }

        //Beregner ny pacman placering
		switch (direction){
            case DOWN:
                if (pacy+x+bitmap.getHeight()<h)
                    pacy=pacy+x;
                else
                    pacy = 0;
                break;
            case UP:
                if (pacy-x > 0)
                    pacy=pacy-x;
                else
                    pacy = h - bitmap.getHeight();
                break;
            case LEFT:
                if (pacx-x > 0)
                    pacx=pacx-x;
                else
                    pacx = w-bitmap.getWidth();
                break;
            case RIGHT:
                if (pacx+x+bitmap.getWidth()<w)
                    pacx=pacx+x;
                else
                    pacx = 0;
                break;
		}

		//Beregner point
        calcPoint();


        //Indiker GAME OVER, hvis ramt fjende
        if(isNearEnemy()){
            GameOver();
        }


        //Bemærk rækkefælge på ovenstående
        //Det betyder, at hvis man rammer en fjende, og er tæt på en mønt,
        //så får man point for mønten.
        // - for at være sød. :P

        //Gentegner spillet
        reDraw();
	}

	public Direction getDirection() {
		return direction;
	}
    public void setDirection(Direction direction){
        this.direction = direction;
    }

	public int getLevel() {
		return level;
	}

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

	public int getPoints(){
		return points;
	}
	/**Opdater points ift., hvor pacman er.
	   Der gives rt point pr. taget mønt **/
	private void calcPoint() {

        //Variabel til ny point beregning
		int tempPoints = 0;


        //Looper GoldCoins
		for(GoldCoin coin:coins)
		{
            //Beregner afstanden til mønt
			double distance = Math.hypot(coin.getX()-(pacx+(bitmap.getWidth()/2)), coin.getY()-(pacy+(bitmap.getHeight()/2)));

            //Beregner om, der du er tæt på en mønt, og sætter mønten til taken, hvis man er.
			if(distance <= 60)
			{
				coin.setTaken(true);
			}

			//Beregninger om, der skal tilføjes et point
			if(coin.isTaken()){
				tempPoints += 1;
			}
		}

		//Sætter point til det, som beregningen ovenover har medført
		points = tempPoints;


        //Opdater highscore, hvis du har fået flere points
		if(highScore < points) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(savedhightScore, points);
			editor.commit();
			highScore = points;
		}
	}


	public ArrayList<GoldCoin> getCoins() {
		return new ArrayList<>(coins);
	}
    /** Tilføjer 10 mønter */
	private void addCoins() {
		for (int i = 0; i < 10; i++) {
			coins.add(new GoldCoin(randomInt(w-20),
					randomInt(h-20), false));
		}
	}

    public ArrayList<Enemy> getEnemies() {
        return new ArrayList<>(enemies);
    }
    /** Tilføjer en fjende */
    private void addEnemy(){
        enemies.add(new Enemy(Level.ESAY, randomInt(w-gostBitmap.getWidth()),
                randomInt(h-gostBitmap.getHeight())));
    }
    /** Beregninger om, man er tæt på en fjende. **/
    private boolean isNearEnemy() {
        boolean res = false;

        //Looper alle fjender
        for(Enemy enemy:enemies)
        {
            //Beregninger afstand til fjenden
            double distance = Math.hypot((enemy.getXPos()+(gostBitmap.getWidth()/2))-(pacx+(bitmap.getWidth()/2)),
                    (enemy.getYPos()+(bitmap.getHeight()/2)-(pacy+(bitmap.getHeight()/2))));

            //Er man tæt på fjenden?
            if(distance <= 80)
            {
                res = true; //Angiver ja
                break; //Stopper loop
            }
        }

        return res;
    }


	public int getPacx() {
		return pacx;
	}
	public int getPacy() {
		return pacy;
	}

	public int getHighScore() {
		return highScore;
	}

    public boolean isGameOver()
    {
        return gameOver;
    }
    public void setGameOver(boolean nyValue)
    {
        gameOver = nyValue;

    }
    /**Gør sådan, at spillet gør til game over stadiet,
    hvilket vil sige, at der vises en kæmpe game over tekst */
    public void GameOver() {
        gameOver = true;
        activity.invalidateOptionsMenu();
    }

    //New game
    public void newGame(){
        coins.clear();
        enemies.clear();

        pacx = randomInt(w-bitmap.getWidth());
        pacy = randomInt(h-bitmap.getHeight());

        addCoins();
        addEnemy();
        points = 0;
        level = 1;
        gameOver = false;
        activity.resetGameTimer(activity.defaultTime);
        activity.resetMoveTimer();


        reDraw();
    }

	//Genskaber spil
	public void recreateGame(int x, int y,
							 ArrayList<GoldCoin> coinsSaved, int level, int points,
                             int highScore, ArrayList<Enemy> enemies, boolean gameover, int direction){



        this.enemies = enemies;

        for (Enemy enemey:enemies){
            //Bytter rundt på x og y, sådan fjenden vises "samme sted".
            int tempX = enemey.getXPos();
            enemey.setXPos(enemey.getYPos());
            enemey.setYPos(tempX);
        }

        coins = coinsSaved;

		for (GoldCoin coin:coins){
			//Bytter rundt på x og y, sådan mønter vises "samme sted".
			int tempX = coin.getX();
			coin.setX(coin.getY());
			coin.setY(tempX);
		}


		//Bytter rundt på x og y, sådan pacman vises "samme sted".
		pacx = y;
		pacy = x;


        //Settter yderligere værdier, sådan spillet kan fortsætter, hvor man slap
        this.direction = Direction.values()[direction];
		this.highScore = highScore;
		this.level = level;
		this.points = points;
        this.gameOver = gameover;

        //Gør der gentegnes
        reDraw();
	}






    //Move metoder
    @Deprecated
    public void moveRight(int x)
    {
        //still within our boundaries?
        if (pacx+x+bitmap.getWidth()<w)
            pacx=pacx+x;
        else
            pacx = 0;

        calcPoint();
        reDraw(); //redraw everything - this ensures onDraw() is called.
    }

    @Deprecated
    public void moveLeft(int x)
    {
        //still within our boundaries?
        if (pacx-x > 0)
            pacx=pacx-x;
        else
            pacx = w-bitmap.getWidth();

        calcPoint();
        reDraw(); //redraw everything - this ensures onDraw() is called.
    }

    @Deprecated
    public void moveDown(int x)
    {
        //still within our boundaries?
        if (pacy+x+bitmap.getHeight()<h)
            pacy=pacy+x;
        else
            pacy = 0;

        calcPoint();
        reDraw(); //redraw everything - this ensures onDraw() is called.
    }

    @Deprecated
    public void moveUp(int x)
    {
        //still within our boundaries?
        if (pacy-x > 0)
            pacy=pacy-x;
        else
            pacy = h - bitmap.getHeight();

        calcPoint();
        reDraw(); //redraw everything - this ensures onDraw() is called.
    }
}
