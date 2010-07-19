import java.math.BigDecimal;

public class Point2DBig {
	BigDecimal x, y;
	
	public Point2DBig(BigDecimal x, BigDecimal y) {
		this.x = x;
		this.y = y;
	}
	
	public Point2DBig() {
		x = y = null;
	}
	
	public void setX(BigDecimal x) {
		this.x = x;
	}
	
	public void setY(BigDecimal y) {
		this.y = y;
	}
	
	public BigDecimal getX() {
		return x;
	}
	
	public BigDecimal getY() {
		return y;
	}
	
	public boolean equals(Point2DBig pt) {
		return ((this.getX().compareTo(pt.getX()) == 0) && (this.getY().compareTo(pt.getY()) == 0));
	}
	
	@Override
	public String toString() {
		return "(" + x.toString() + ", " + y.toString() + ")";
	}
}
