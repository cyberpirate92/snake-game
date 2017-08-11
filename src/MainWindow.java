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
	private final int SNAKE_BLINK_INTERVAL = 500;
	private final int OBJECT_BLINK_INTERVAL = 250;
	private static final int SNAKE_MOVE_SPEED_FAST = 50;
	private static final int SNAKE_MOVE_SPEED_MEDIUM = 85;
	private static final int SNAKE_MOVE_SPEED_SLOW = 125;
	
	private static final Color DEFAULT_BACKGROUND = new Color(0, 0, 0);
	private static final Color DEFAULT_SNAKE_COLOR = Color.GREEN;
	private static final Color SNAKE_DEAD_COLOR = Color.RED;
	private static final Color COLLECTIBLE_COLOR = Color.RED;
	private static final Color COLLECTIBLE_COLOR_2 = DEFAULT_BACKGROUND;
	private static final Color WALL_COLOR = Color.YELLOW;
	private static final Font defaultFont = new Font("monospace", Font.BOLD, 16);
	
	private static final Border snakeBorder = null;
	//private static final Border snakeBorder = BorderFactory.createLineBorder(new Color(100, 220, 100));
	private static final Border objectBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED);
	private static final Border wallBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
	private static final Border defaultBorder = BorderFactory.createLineBorder(Color.DARK_GRAY);
	
	// offset constants - these are to be added to the current position
	private static final Position leftOffset = new Position(0, -1);
	private static final Position rightOffset = new Position(0, 1);
	private static final Position upwardOffset = new Position(-1, 0);
	private static final Position downwardOffset = new Position(1, 0);
	
	private Timer snakeMoveTimer, blipTimer, snakeBlinkTimer;
	private JPanel topPanel, gridPanel;
	private JPanel[][] gridCells;
	private Position colObjPos, prevColObjPos, directionOffset;
	private boolean gameOver, isPaused;
	private int score;
	private ArrayList<Position> snakePos;
	
	private JLabel scoreLabel;
	
    private static Logger logger = LogManager.getLogger(MainWindow.class);
	
	public MainWindow() {
		super("Snake");
		
		score = 0;
		gameOver = false;
		isPaused = true;	// needs to be true for resumeGame to execute the first time
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
				gridCells[i][j].setBorder(defaultBorder);
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
		this.setResizable(false);
		this.setVisible(true);
	}
	
	public void initGame() {
		createBoundaryWalls();
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
					if(!directionOffset.equals(upwardOffset))
						directionOffset = downwardOffset;
				}
				else if(e.getKeyCode() == KeyEvent.VK_UP) {
					if(!directionOffset.equals(downwardOffset))
						directionOffset = upwardOffset;
				}
				else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
					if(!directionOffset.equals(rightOffset))
						directionOffset = leftOffset;
				}
				else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
					if(!directionOffset.equals(leftOffset))
						directionOffset = rightOffset;
				}
				else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					if(isPaused)
						resumeGame();
					else
						pauseGame();
				}
				else if(e.getKeyCode() == KeyEvent.VK_N) {
					if(gameOver) {
						new MainWindow();
						MainWindow.this.dispose();
					}
				}
			}
		});
		
		// some minor styling
		topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		topPanel.setBackground(Color.BLACK);
		topPanel.add(scoreLabel);
		gridPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		scoreLabel.setForeground(Color.GREEN);
		
		generateCollectibleItem();
		
		// when window not in focus, pause game
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				resumeGame();
			}

			@Override
			public void focusLost(FocusEvent e) {
				pauseGame();
			}
		});
	}
	
	private void createBoundaryWalls() {
		for(int i=0; i<gridCells.length; i++) {
			gridCells[i][0].setBackground(WALL_COLOR);
			gridCells[i][0].setBorder(wallBorder);
			gridCells[i][gridCells[i].length-1].setBackground(WALL_COLOR);
			gridCells[i][gridCells[i].length-1].setBorder(wallBorder);
		}
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
		
		// can use snakePos.contains(position), if hashCode() is overridden in Position class
		for(Position p : snakePos)
			if(p.equals(position))
				return false;
		
		colObjPos = new Position(position);
		gridCells[position.getX()][position.getY()].setBackground(COLLECTIBLE_COLOR);
		gridCells[position.getX()][position.getY()].setBorder(objectBorder);
		logger.debug("Collectible item set at position " + position);
		return true;
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
		if(snakePos.size() == 0) {
			// initialization
			Position head, tail;
			tail = new Position(p);
			head = new Position(p);
			head.addOffset(directionOffset);
			snakePos.add(head);
			snakePos.add(tail);
			gridCells[head.getX()][head.getY()].setBackground(DEFAULT_SNAKE_COLOR);
			gridCells[head.getX()][head.getY()].setBorder(snakeBorder);
		}
		else {
			Position tailPos = getSnakeTailNode().getPosition();
			
			// updating body positions
			for(int i=snakePos.size()-1; i>0; i--) {
				snakePos.get(i).setPosition(snakePos.get(i-1));
			}
			
			// processing tail
			if(prevColObjPos != null && tailPos.equals(prevColObjPos)){
				snakePos.add(prevColObjPos);
				prevColObjPos = null;
			}
			else {
				gridCells[tailPos.getX()][tailPos.getY()].setBackground(DEFAULT_BACKGROUND);
				gridCells[tailPos.getX()][tailPos.getY()].setBorder(defaultBorder);
			}
			
			// moving head to the new position
			snakePos.get(0).setX(p.getX());
			snakePos.get(0).setY(p.getY());
			
			// painting the new head position
			Position headPos = snakePos.get(0);
			gridCells[headPos.getX()][headPos.getY()].setBackground(DEFAULT_SNAKE_COLOR);
			gridCells[headPos.getX()][headPos.getY()].setBorder(snakeBorder);
			
			// checking if object has been captured
			if(colObjPos != null && headPos.equals(colObjPos)) {
				prevColObjPos = new Position(colObjPos);
				generateCollectibleItem();
				incrementScore();
			}
			
			// checking for head collision with body
			for(int i=1; i<snakePos.size(); i++) {
				if(snakePos.get(0).equals(snakePos.get(i))) {
					haltGame();
					return;
				}
			}
		}
	}
	
	private void pauseGame() {
		if(!gameOver && !isPaused) {
			snakeMoveTimer.cancel();
			blipTimer.cancel();
			isPaused = true;
			startBlinkingSnake();
			displayDiagnostics();
		}
	}
	
	private void resumeGame() {
		if(!gameOver && isPaused) {
			stopBlinkingSnake();
			snakeMoveTimer = new Timer();
			snakeMoveTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					Position newPos = new Position(snakePos.get(0));
					Position offsetUsed = directionOffset; // since directionOffset could change via another thread
					
					newPos.addOffset(offsetUsed);
					if(newPos.getX() < 0) {
						newPos.setX(gridCells.length-1);
					}
					newPos.setX(newPos.getX()%gridCells.length);
					
					if(newPos.getY() >= GRID_SIZE-1 || newPos.getY() <= 0) {
						haltGame();
						return;
					}
					setSnakePosition(newPos);
				}
			}, 0, SNAKE_MOVE_SPEED_FAST);
			
			blipTimer = new Timer();
			blipTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					pulsateObject();
				}
			}, 0, OBJECT_BLINK_INTERVAL);
			isPaused = false;
		}
	}
	
	// utility function to blink the body of snake
	private void startBlinkingSnake() {
		final Color current = getSnakeColor();
		snakeBlinkTimer = new Timer();
		snakeBlinkTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(getSnakeColor() != DEFAULT_BACKGROUND)
					setSnakeColor(DEFAULT_BACKGROUND);
				else
					setSnakeColor(current);
			}
		}, 0, SNAKE_BLINK_INTERVAL);
	}
	
	private void stopBlinkingSnake() {
		if(snakeBlinkTimer != null) {
			snakeBlinkTimer.cancel();
			setSnakeColor(DEFAULT_SNAKE_COLOR);
			snakeBlinkTimer = null;
		}
	}
	
	private void setSnakeColor(Color c) {
		for(Position p: snakePos) {
			gridCells[p.getX()][p.getY()].setBackground(c);
		}
	}
	
	private Color getSnakeColor() {
		Position head = snakePos.get(0);
		return gridCells[head.getX()][head.getY()].getBackground();
	}
	
	private void setSnakeBorder(Border b) {
		for(Position p: snakePos) {
			gridCells[p.getX()][p.getY()].setBorder(b);
		}
	}
	
	private void setSnakeColorAndBorder(Color c, Border b) {
		setSnakeColor(c);
		setSnakeBorder(b);
	}
	
	private Node getSnakeHeadNode() {
		Position p = snakePos.get(0);
		return new Node(gridCells[p.getX()][p.getY()], p);
	}
	
	private Node getSnakeTailNode() {
		Position p = snakePos.get(snakePos.size()-1);
		return new Node(gridCells[p.getX()][p.getY()], p);
	}
	
	// to end game, stop all timers here
	private void haltGame() {
		setSnakeColor(SNAKE_DEAD_COLOR);
		startBlinkingSnake();
		gameOver = true;
		snakeMoveTimer.cancel();
		blipTimer.cancel();
		JOptionPane.showMessageDialog(null, "Game Over!");
	}
	
	private void resetGame() {
		if(snakeBlinkTimer != null) {
			snakeBlinkTimer.cancel();
		}
		snakePos.clear();
	}
	
	private void displayDiagnostics() {
		System.out.println("Snake Size: " + snakePos.size());
		System.out.println("Co-ordinates");
		int i = 1;
		for(Position p: snakePos) 
			System.out.println("["+(i++)+"] => " + p);
	}
}
