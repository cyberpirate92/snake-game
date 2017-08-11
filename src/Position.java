
public class Position {
	
	private int x, y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Position(Position other) {
		setPosition(other);
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public void setPosition(Position other) {
		this.x = other.getX();
		this.y = other.getY();
	}
	
	public boolean equals(Position p) {
		return (this.getX() == p.getX() && this.getY() == p.getY());
	}
	
	public String toString() {
		return "(" + this.getX() + ", " + this.getY() + ")";
	}
	
	public void addOffset(Position p) {
		this.x += p.getX();
		this.y += p.getY();
	}
}
