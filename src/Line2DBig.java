import java.math.BigDecimal;

public class Line2DBig {
	private BigDecimal X1, X2, Y1, Y2;
	private final boolean VERTICAL;
	
	public Line2DBig(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2) {
		X1 = x1;
		X2 = x2;
		Y1 = y1;
		Y2 = y2;
		VERTICAL = (X1.compareTo(X2) == 0);
	}
	
	public Line2DBig(Point2DBig pt1, Point2DBig pt2) {
		this(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
	}
	
	public BigDecimal getX1() {
		return X1;
	}
	
	public BigDecimal getY1() {
		return Y1;
	}
	
	public BigDecimal getX2() {
		return X2;
	}
	
	public BigDecimal getY2() {
		return Y2;
	}
	
	public BigDecimal getLowX() {
		return X1.min(X2);
	}
	
	public BigDecimal getHighX() {
		return X1.max(X2);
	}
	
	public BigDecimal getLowY() {
		return Y1.min(Y2);
	}
	
	public BigDecimal getHighY() {
		return Y1.max(Y2);
	}
	
	public BigDecimal getSlope() {
		if (VERTICAL) return null;
		BigDecimal dX = X2.subtract(X1, CalcConst.MC);
		BigDecimal dY = Y2.subtract(Y1, CalcConst.MC);
		return dY.divide(dX, CalcConst.MC);
	}
	
	public BigDecimal getYOnLine(BigDecimal x) {
		if ((x.compareTo(getLowX()) < 0) || (x.compareTo(getHighX()) > 0)) return null;
		if (VERTICAL) return Y1;
		BigDecimal slope = getSlope();
		BigDecimal dx = x.subtract(X1, CalcConst.MC);
		return Y1.add(dx.multiply(slope, CalcConst.MC), CalcConst.MC);
	}
	
	public BigDecimal getXOnLine(BigDecimal y) {
		if ((y.compareTo(getLowY()) < 0) || (y.compareTo(getHighY()) > 0)) return null;
		if (VERTICAL) return X1;
		BigDecimal slope = getSlope();
		BigDecimal dy = y.subtract(Y1, CalcConst.MC);
		return X1.add(dy.divide(slope, CalcConst.MC), CalcConst.MC);
	}
	
	public boolean contains(Point2DBig pt) {
		return contains(pt.getX(), pt.getY());
	}
	
	public boolean contains(BigDecimal x, BigDecimal y) {
		if (X1.compareTo(X2) == 0) {
			if (x.compareTo(X1) != 0) return false;
			if ((y.compareTo(getLowY()) < 0) || (y.compareTo(getHighY()) > 0)) return false;
			else return true;
		}
		
		if ((x.compareTo(getLowX()) < 0) || (x.compareTo(getHighX()) > 0)) return false;
		if ((y.compareTo(getLowY()) < 0) || (y.compareTo(getHighY()) > 0)) return false;
		
		BigDecimal slp = getSlope();
		BigDecimal a = slp.negate(CalcConst.MC);
		BigDecimal c = X1.multiply(slp, CalcConst.MC).subtract(Y1, CalcConst.MC);
		BigDecimal num = a.multiply(x, CalcConst.MC).add(y, CalcConst.MC).add(c, CalcConst.MC);
		BigDecimal denom = a.multiply(a, CalcConst.MC).add(BigDecimal.ONE, CalcConst.MC);
		BigDecimal d2 = num.multiply(num, CalcConst.MC).divide(denom, CalcConst.MC);
		
		return (d2.compareTo(CalcConst.TOLERANCE) <= 0);
	}
	
	public String toString() {
		String out = "";
		out += "Line: from (";
		out += X1;
		out += ", ";
		out += Y1;
		out += ") to (";
		out += X2;
		out += ", ";
		out += Y2;
		out += ")";
		return out;
	}
}
