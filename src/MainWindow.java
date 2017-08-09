import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MainWindow extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private final int GRID_SIZE = 70;
	private final Color DEFAULT_BACKGROUND = Color.GRAY;
	private final Color SNAKE_COLOR = Color.GREEN;
	private final Color SNAKE_DEAD_COLOR = Color.RED;
	private final Color COLLECTIBLE_COLOR = Color.RED;
	private final Color COLLECTIBLE_COLOR_2 = Color.ORANGE;
	private final Font defaultFont = new Font("Papyrus", Font.BOLD, 16);
	
	private static final Border snakeBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED);
	private static final Border objectBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED);
	
	// offset constants - these are to be added to the current position
	private static final Position leftOffset = new Position(0, -1);
	private static final Position rightOffset = new Position(0, 1);
	private static final Position upwardOffset = new Position(-1, 0);
	private static final Position downwardOffset = new Position(1, 0);
	
	private Timer snakeMoveTimer, blipTimer;
	private JPanel topPanel, gridPanel;
	private JPanel[][] gridCells;
	private Position colObjPos, prevColObjPos, directionOffset;
	private boolean gameOver, firstColObj;
	private int score;
	private ArrayList<Position> snakePos;
	
	private JLabel scoreLabel;
	
    private static Logger logger = LogManager.getLogger(MainWindow.class);
	
	public MainWindow() {
		super("Snake");
		
		score = 0;
		gameOver = false;
		firstColObj = true;
		directionOffset = rightOffset;
		snakePos = new ArrayList<Position>();
		
		snakeMoveTimer = new Timer();
		blipTimer = new Timer();

		topPanel= new JPanel();
		gridPanel = new JPanel();
		gridCells = new JPanel[GRID_SIZE][GRID_SIZE];
		gridPanel.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE, 0, 0));
		gridPanel.setBackground(Color.BLACK);  
		
		Random random = new Random();
		for(int i=0; i<gridCells.length; i++) {
			for(int j=0; j<gridCells[i].length; j++) {
				gridCells[i][j] = new JPanel();
				gridCells[i][j].setBackground(DEFAULT_BACKGROUND);
				gridPanel.add(gridCells[i][j]);
			}
		}
		setSnakePosition(new Position(GRID_SIZE/2, Math.abs(random.nextInt()%(GRID_SIZE/3)+2)));
		
		scoreLabel = new JLabel("Score: 0");
		scoreLabel.setFont(defaultFont);
		
		initGame();
		this.getContentPane().add(topPanel, BorderLayout.NORTH);
		this.getContentPane().add(gridPanel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void initGame() {
		this.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					directionOffset = downwardOffset;
				}
				else if(e.getKeyCode() == KeyEvent.VK_UP) {
					directionOffset = upwardOffset;
				}
				else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
					directionOffset = leftOffset;
				}
				else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
					directionOffset = rightOffset;
				}
			}
		});
		
		// some minor styling
		topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		topPanel.setBackground(Color.BLACK);
		topPanel.add(scoreLabel);
		gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		scoreLabel.setForeground(Color.GREEN);
		
		generateCollectibleItem();
		
		// when window not in focus, pause game
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if(!gameOver) {
					snakeMoveTimer = new Timer();
					snakeMoveTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							Position newPos = new Position(snakePos.get(0));
							newPos.addOffset(directionOffset);
							if(newPos.getX() >= GRID_SIZE-1 || newPos.getX() < 0)
								haltGame();
							else if(newPos.getY() >= GRID_SIZE-1 || newPos.getY() < 0)
								haltGame();
							else {
								setSnakePosition(newPos);
							}
						}
					}, 0, 50);
					
					blipTimer = new Timer();
					blipTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							pulsateObject();
						}
					}, 0, 200);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(!gameOver) {
					snakeMoveTimer.cancel();
					blipTimer.cancel();
				}
			}
		});
	}
	
	private void pulsateObject() {
		if(colObjPos != null) {
			Color c = gridCells[colObjPos.getX()][colObjPos.getY()].getBackground();
			if(c == COLLECTIBLE_COLOR)
				gridCells[colObjPos.getX()][colObjPos.getY()].setBackground(COLLECTIBLE_COLOR_2);
			else
				gridCells[colObjPos.getX()][colObjPos.getY()].setBackground(COLLECTIBLE_COLOR);
		}
	}
	
	private boolean placeCollectibeItem(Position position) {
		if(position.equals(snakePos))
			return false;
		else {
			colObjPos = new Position(position);
			gridCells[position.getX()][position.getY()].setBackground(COLLECTIBLE_COLOR);
			gridCells[position.getX()][position.getY()].setBorder(objectBorder);
			logger.debug("Collectible item set at position " + position);
			//System.out.println("Collectible item set at position " + position);
			return true;
		}
	}
	
	private void generateCollectibleItem() {
		while(!placeCollectibeItem(getRandomPosition()));
	}
	
	private Position getRandomPosition() {
		Random random = new Random();
		Position position = new Position(random.nextInt(GRID_SIZE-2)+1, random.nextInt(GRID_SIZE-2)+1);
		return position;
	}
	
	public void setScore(int newScore) {
		score = newScore;
		updateScoreLabel();
	}
	
	public int getScore() {
		return score;
	}
	
	private void incrementScore(int amount) {
		this.setScore(this.getScore() + amount);
	}
	
	private void incrementScore() {
		this.incrementScore(1);
	}
	
	public void updateScoreLabel() {
		scoreLabel.setText("Score: " + score);
	}
	
	public void setScoreLabel(String text) {
		scoreLabel.setText(text);
	}
	
	private void setSnakePosition(Position p) {
		// corner case
		if(score == 1 && firstColObj) {
			firstColObj = false;
			snakePos.add(prevColObjPos);
			gridCells[prevColObjPos.getX()][prevColObjPos.getY()].setBackground(SNAKE_COLOR);
			gridCells[prevColObjPos.getX()][prevColObjPos.getY()].setBorder(snakeBorder);
		}
		if(snakePos.size() != 0) {
			Position tailPos = snakePos.get(snakePos.size()-1);
			// updating body positions
			for(int i=snakePos.size()-1; i>0; i--) {
				snakePos.get(i).setX(snakePos.get(i-1).getX());
				snakePos.get(i).setY(snakePos.get(i-1).getY());
			}
			
			// processing tail
			if(prevColObjPos != null && tailPos.equals(prevColObjPos)){
				snakePos.add(prevColObjPos);
				prevColObjPos = null;
			}
			else {
				gridCells[tailPos.getX()][tailPos.getY()].setBackground(DEFAULT_BACKGROUND);
				gridCells[tailPos.getX()][tailPos.getY()].setBorder(null);
			}
			
			// moving head to the new position
			snakePos.get(0).setX(p.getX());
			snakePos.get(0).setY(p.getY());
			
			// painting the new head position
			Position headPos = snakePos.get(0);
			gridCells[headPos.getX()][headPos.getY()].setBackground(SNAKE_COLOR);
			gridCells[headPos.getX()][headPos.getY()].setBorder(snakeBorder);
			
			// checking if object has been captured
			if(colObjPos != null && headPos.equals(colObjPos)) {
				prevColObjPos = new Position(colObjPos);
				generateCollectibleItem();
				incrementScore();
			}
			
			// checking for collision with body
			for(int i=1; i<snakePos.size(); i++) {
				if(snakePos.get(0).equals(snakePos.get(i))) {
					haltGame();
					return;
				}
			}
		}
		else {
			// initialization
			snakePos.add(p);
			gridCells[p.getX()][p.getY()].setBackground(SNAKE_COLOR);
			gridCells[p.getX()][p.getY()].setBorder(snakeBorder);
		}
	}
	
	// to end game, stop all timers here
	private void haltGame() {
		for(Position p : snakePos) {
			gridCells[p.getX()][p.getY()].setBackground(SNAKE_DEAD_COLOR);
		}
		gameOver = true;
		snakeMoveTimer.cancel();
		blipTimer.cancel();
		JOptionPane.showMessageDialog(null, "Game Over!");
	}
}
