import javax.swing.JPanel;


public class Node {
	
	private JPanel panel;
	private Position position;
	
	public Node(JPanel panel, Position position) {
		this.panel = panel;
		this.position = position;
	}
	
	private JPanel getPanel() {
		return panel;
	}
	
	private void setPanel(JPanel panel) {
		this.panel = panel;
	}
	
	private Position getPosition() {
		return position;
	}
	
	private void setPosition(Position position) {
		this.position = position;
	}
}
