import javax.swing.JPanel;

// POJO class for passing data related to a grid cell
// specifically the cells representing the snake

public class Node {
	
	private JPanel panel;
	private Position position;
	
	public Node(JPanel panel, Position position) {
		this.panel = panel;
		this.position = position;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void setPanel(JPanel panel) {
		this.panel = panel;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}
}
