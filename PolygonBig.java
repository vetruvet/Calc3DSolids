import java.math.BigDecimal;
import java.util.*;

public class PolygonBig {
	public static final boolean DEBUG = false;
	
	private ArrayList<Point2DBig> ptList = new ArrayList<Point2DBig>();
	
	public PolygonBig() {
		
	}
	
	public PolygonBig(BigDecimal[] xPts, BigDecimal[] yPts, int nPts) {
		if ((nPts != xPts.length) || (nPts != yPts.length)) throw new IllegalArgumentException("Invalid coordinate array length!");
		for (int q = 0; q < nPts; q++) ptList.add(new Point2DBig(xPts[q], yPts[q]));
	}
	
	public PolygonBig(Point2DBig[] pts) {
		for (int q = 0; q < pts.length; q++) ptList.add(pts[q]);
	}
	
	public void addPoint(BigDecimal xCoord, BigDecimal yCoord) {
		ptList.add(new Point2DBig(xCoord, yCoord));
	}
	
	public void addPoint(Point2DBig pt) {
		ptList.add(pt);
	}
	
	public boolean contains(Point2DBig pt) {
		return contains(pt, false);
	}
	
	public boolean contains(Point2DBig pt, boolean incEdge) {
		if (DEBUG) System.out.println("");
		if (DEBUG) System.out.println("Starting container test code" + ((incEdge) ? ", including edges" : ""));
		if (DEBUG) System.out.println("Point: " + pt.toString());
		if ((pt.getX().compareTo(getMinX()) < 0) || (pt.getY().compareTo(getMinY()) < 0) || (pt.getX().compareTo(getMaxX()) > 0) || (pt.getY().compareTo(getMaxY()) > 0)) return false;
		if (DEBUG) System.out.println("Pt inside bounding box");
		for (Iterator<Point2DBig> it = ptList.iterator(); it.hasNext(); ) {
			if (it.next().equals(pt)) {
				if (DEBUG) System.out.println("Pt is a pt on poly");
				if (incEdge) return true;
				else return false;
			}
		}
		if (DEBUG) System.out.println("Pt not a pt on poly");
		Line2DBig[] sides = getSides();
		for (int q = 0; q < sides.length; q++) {
			if (sides[q].contains(pt)) {
				if (DEBUG) System.out.println("Pt is on a side: " + sides[q].toString());
				if (incEdge) return true;
				else return false;
			}
		}
		
		if (DEBUG) System.out.println("Not on edge");
		
		int lHits = 0, rHits = 0;
		for (int q = 0; q < sides.length; q++) {
			if (DEBUG) System.out.println("Side iteration: side " + q + " = " + sides[q].toString());
			if ((pt.getY().compareTo(sides[q].getLowY()) > 0) && (pt.getY().compareTo(sides[q].getHighY()) <= 0)) {
				if (DEBUG) System.out.println("Pt is between Y-coords of side");
				if (pt.getX().compareTo(sides[q].getHighX()) > 0) {
					if (DEBUG) System.out.println("Side is way on left of pt");
					lHits++;
					continue;
				}
				if (pt.getX().compareTo(sides[q].getLowX()) < 0) {
					if (DEBUG) System.out.println("Side is way on right of pt");
					rHits++;
					continue;
				}
				
				BigDecimal xOnLine = sides[q].getXOnLine(pt.getY());
				if (DEBUG) System.out.println("X-coord on the line: " + xOnLine);
				if (pt.getX().compareTo(xOnLine) > 0) {
					if (DEBUG) System.out.println("Side on left of pt");
					lHits++;
					continue;
				}
				if (pt.getX().compareTo(xOnLine) < 0) {
					if (DEBUG) System.out.println("Side on right of pt");
					rHits++;
					continue;
				}
			}
		}
		if (DEBUG) System.out.println("Hits on left: " + lHits + "; right: " + rHits);
		if ((lHits + rHits) % 2 != 0) return false;
		return (lHits % 2 != 0);
	}
	
	public boolean contains(Equation eq) {
		final BigDecimal STEPS = new BigDecimal(PolygonBuilder.getQuality());
		BigDecimal max, min;
		if (eq.isEqInX()) {
			max = getMaxX();
			min = getMinX();
		}
		else {
			max = getMaxY();
			min = getMinY();
		}
		boolean isInPoly = false;
		for (BigDecimal q = min; q.compareTo(max) < 0; q = q.add(max.subtract(min, CalcConst.MC).divide(STEPS, CalcConst.MC), CalcConst.MC)) {
			try {
				Point2DBig ptTest;
				if (eq.isEqInX()) ptTest = new Point2DBig(q.round(CalcConst.MC), eq.eval(q));
				else ptTest = new Point2DBig(eq.eval(q), q.round(CalcConst.MC));
				isInPoly = contains(ptTest, false);
				if (isInPoly) break;
				else continue;
			}
			catch (OperatorDomainException eOD) {
				isInPoly = false;
				continue;
			}
		}
		return isInPoly;
	}

	public boolean isSimple() {
		int nPts = ptList.size();
		if (nPts < 4) return true;
		Line2DBig[] sides = new Line2DBig[nPts];
		for (int q = 0; q < nPts - 1; q++) {
			sides[q] = new Line2DBig(ptList.get(q), ptList.get(q + 1));
		}
		sides[nPts - 1] = new Line2DBig(ptList.get(nPts - 1), ptList.get(0));
		
		for (int q = 0; q < sides.length; q++) {
			for (int w = 0; w < sides.length; w++) {
				if ((w == (q + 1 + nPts) % nPts) || (w == (q - 1 + nPts) % nPts) || (w == q)) continue;
				if (CalcUtils.linesIntersect(sides[q], sides[w])) return false;
			}
		}
		
		return true;
	}

	public void cleanUp() {
		ArrayList<Point2DBig> unique = new ArrayList<Point2DBig>();
		unique.add(ptList.get(0));
		for (int q = 1; q < ptList.size(); q++) {
			if (!ptList.get(q).equals(ptList.get(q - 1))) unique.add(ptList.get(q));
		}
		ptList = unique;
	}
	
	public BigDecimal[] getXBoundsAt(BigDecimal y) {
		if ((y.compareTo(getMaxY()) > 0) || (y.compareTo(getMinY()) < 0)) return null;
		Line2DBig[] sides = getSides();
		
		ArrayList<BigDecimal> pts = new ArrayList<BigDecimal>();
		for (int q = 0; q < sides.length; q++) {
			BigDecimal xFind = sides[q].getXOnLine(y);
			if (xFind == null) continue;
			pts.add(xFind);
		}
		pts = CalcUtils.removeRepeats(pts);
		if (pts.size() > 2) return null;
		BigDecimal x1 = pts.get(0), x2 = pts.get(1);
		return new BigDecimal[]{x1.min(x2), x2.max(x1)};
	}
	
	public BigDecimal[] getYBoundsAt(BigDecimal x) {
		if ((x.compareTo(getMaxX()) > 0) || (x.compareTo(getMinX()) < 0)) return null;
		Line2DBig[] sides = getSides();
		
		ArrayList<BigDecimal> pts = new ArrayList<BigDecimal>();
		for (int q = 0; q < sides.length; q++) {
			BigDecimal yFind = sides[q].getYOnLine(x);
			if (yFind == null) continue;
			pts.add(yFind);
		}
		pts = CalcUtils.removeRepeats(pts);
		if (pts.size() > 2) return null;
		BigDecimal y1 = pts.get(0), y2 = pts.get(1);
		return new BigDecimal[]{y1.min(y2), y2.max(y1)};
	}
	
	public Line2DBig[] getSides() {
		int nPts = ptList.size();
		if (nPts < 2) return null;
		Line2DBig[] sides = new Line2DBig[nPts];
		for (int q = 0; q < nPts - 1; q++) {
			sides[q] = new Line2DBig(ptList.get(q), ptList.get(q + 1));
		}
		sides[nPts - 1] = new Line2DBig(ptList.get(nPts - 1), ptList.get(0));
		return sides;
	}

	public BigDecimal getMinX() {
		BigDecimal[] xs = getXPoints();
		if (xs.length == 0) return null;
		BigDecimal min = xs[0];
		for (int q = 1; q < xs.length; q++) {
			if (xs[q].compareTo(min) < 0) min = xs[q];
		}
		return min;
	}
	
	public BigDecimal getYAtMinX() {
		BigDecimal[] xs = getXPoints();
		BigDecimal[] ys = getYPoints();
		if (xs.length == 0) return null;
		int minInd = 0;
		for (int q = 1; q < xs.length; q++) {
			if (xs[q].compareTo(xs[minInd]) < 0) minInd = q;
		}
		return ys[minInd];
	}
	
	public BigDecimal getMaxX() {
		BigDecimal[] xs = getXPoints();
		if (xs.length == 0) return null;
		BigDecimal max = xs[0];
		for (int q = 1; q < xs.length; q++) {
			if (xs[q].compareTo(max) > 0) max = xs[q];
		}
		return max;
	}
	
	public BigDecimal getYAtMaxX() {
		BigDecimal[] xs = getXPoints();
		BigDecimal[] ys = getYPoints();
		if (xs.length == 0) return null;
		int maxInd = 0;
		for (int q = 1; q < xs.length; q++) {
			if (xs[q].compareTo(xs[maxInd]) > 0) maxInd = q;
		}
		return ys[maxInd];
	}
	
	public BigDecimal getMinY() {
		BigDecimal[] ys = getYPoints();
		if (ys.length == 0) return null;
		BigDecimal min = ys[0];
		for (int q = 1; q < ys.length; q++) {
			if (ys[q].compareTo(min) < 0) min = ys[q];
		}
		return min;
	}
	
	public BigDecimal getXAtMinY() {
		BigDecimal[] ys = getYPoints();
		BigDecimal[] xs = getXPoints();
		if (ys.length == 0) return null;
		int minInd = 0;
		for (int q = 1; q < ys.length; q++) {
			if (ys[q].compareTo(ys[minInd]) < 0) minInd = q;
		}
		return xs[minInd];
	}
	
	public BigDecimal getMaxY() {
		BigDecimal[] ys = getYPoints();
		if (ys.length == 0) return null;
		BigDecimal max = ys[0];
		for (int q = 1; q < ys.length; q++) {
			if (ys[q].compareTo(max) > 0) max = ys[q];
		}
		return max;
	}
	
	public BigDecimal getXAtMaxY() {
		BigDecimal[] ys = getYPoints();
		BigDecimal[] xs = getXPoints();
		if (ys.length == 0) return null;
		int maxInd = 0;
		for (int q = 1; q < ys.length; q++) {
			if (ys[q].compareTo(ys[maxInd]) > 0) maxInd = q;
		}
		return xs[maxInd];
	}
	
	public BigDecimal[] getXPoints() {
		BigDecimal[] retArr = new BigDecimal[ptList.size()];
		int q = 0;
		for (Iterator<Point2DBig> it = ptList.iterator(); it.hasNext(); ) {
			retArr[q++] = it.next().getX();
		}
		return retArr;
	}

	public BigDecimal[] getYPoints() {
		BigDecimal[] retArr = new BigDecimal[ptList.size()];
		int q = 0;
		for (Iterator<Point2DBig> it = ptList.iterator(); it.hasNext(); ) {
			retArr[q++] = it.next().getY();
		}
		return retArr;
	}

	public int getNPoints() {
		return ptList.size();
	}
	
	public Point2DBig[] getPoints() {
		Point2DBig[] retArr = new Point2DBig[ptList.size()];
		int q = 0;
		for (Iterator<Point2DBig> it = ptList.iterator(); it.hasNext(); ) {
			retArr[q++] = it.next();
		}
		return retArr;
	}
	
	public String toString() {
		String out = "";
		out += ptList.size();
		out += " pt(s): ";
		for (Iterator<Point2DBig> it = ptList.iterator(); it.hasNext(); ) {
			Point2DBig pt = it.next();
			out += "(";
			out += pt.getX();
			out += ", ";
			out += pt.getY();
			out += ") ";
		}
		return out;
	}
}
