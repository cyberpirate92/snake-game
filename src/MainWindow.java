import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MainWindow extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private final int GRID_SIZE = 70;
	private final Color DEFAULT_BACKGROUND = Color.GRAY;
	private final Color SNAKE_COLOR = Color.GREEN;
	private final Color COLLECTIBLE_COLOR = Color.RED;
	
	private JPanel topPanel, gridPanel;
	private JPanel[][] gridCells;
	private Position snakePos, colObjPos;
	private boolean gameOver;
	private int score;
	
    private static Logger logger = LogManager.getLogger(MainWindow.class);
	
	public MainWindow() {
		super("Snake");
		
		score = 0;
		gameOver = false;
		topPanel= new JPanel();
		gridPanel = new JPanel();
		gridCells = new JPanel[GRID_SIZE][GRID_SIZE];
		gridPanel.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE, 0, 0));
		
		Random random = new Random();
		snakePos = new Position(GRID_SIZE/2, Math.abs(random.nextInt()%(GRID_SIZE/3)+2));
		logger.debug("Snake position set as " + snakePos);
		//System.out.println("Snake position set as " + snakePos);
		for(int i=0; i<gridCells.length; i++) {
			for(int j=0; j<gridCells[i].length; j++) {
				gridCells[i][j] = new JPanel();
				gridCells[i][j].setBackground(DEFAULT_BACKGROUND);
				gridPanel.add(gridCells[i][j]);
			}
		}
		
		initGame();
		this.getContentPane().add(topPanel, BorderLayout.NORTH);
		this.getContentPane().add(gridPanel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500, 600);
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
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				Position prevPos = new Position(snakePos.getX(), snakePos.getY());
				boolean hit = false;
				
				if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					if(snakePos.getX() != GRID_SIZE-1)
						snakePos.setX(snakePos.getX()+1);
					else hit = true;
				}
				else if(e.getKeyCode() == KeyEvent.VK_UP) {
					if(snakePos.getX() != 0)
						snakePos.setX(snakePos.getX()-1);
					else hit = true;
				}
				else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
					if(snakePos.getY() != 0)
						snakePos.setY(snakePos.getY()-1);
					else hit = true;
				}
				else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
					if(snakePos.getY() != GRID_SIZE-1)
						snakePos.setY(snakePos.getY()+1);
					else hit = true;
				}
				if(prevPos != snakePos) {
					gridCells[prevPos.getX()][prevPos.getY()].setBackground(DEFAULT_BACKGROUND);
					gridCells[snakePos.getX()][snakePos.getY()].setBackground(SNAKE_COLOR);
					logger.debug("Snake position updated to " + snakePos + " from " + prevPos);
					System.out.println("Snake position updated to " + snakePos + " from " + prevPos);
				}
				else if(hit) {
					JOptionPane.showMessageDialog(null, "Game Over!!");
					gameOver = true;
				}
				if(snakePos.equals(colObjPos))
					score++;
			}
			
		});
		
		topPanel.setBackground(Color.BLACK);
		topPanel.setForeground(Color.GREEN);
		gridCells[snakePos.getX()][snakePos.getY()].setBackground(SNAKE_COLOR);
		generateCollectibleItem();
	}
	
	private boolean placeCollectibeItem(Position position) {
		if(position.equals(snakePos))
			return false;
		else {
			colObjPos = new Position(position);
			gridCells[position.getX()][position.getY()].setBackground(COLLECTIBLE_COLOR);
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
}
