import java.math.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import java.util.*;
import static java.lang.Math.*;

public class SolidMaker {
	public static Shape3D rotateShape(PolygonBig poly, BigDecimal axisVal, boolean hAxis) {
		int nPtsInStrip = 2 * (CalcConst.VRES + 1);
		int nStrips = poly.getNPoints();
		int nPts = nPtsInStrip * nStrips;
		int[] ptCount = new int[nStrips];
		Arrays.fill(ptCount, nPtsInStrip);
		
		Point3d[][] pts = new Point3d[nStrips][CalcConst.VRES + 1];
		
		BigDecimal[] polyX = poly.getXPoints();
		BigDecimal[] polyY = poly.getYPoints();
		final double STEP = 2.0 * PI / CalcConst.VRES; 
		if (hAxis) {
			for (int q = 0; q < nStrips; q++) {
				double rad = polyY[q].doubleValue() - axisVal.doubleValue();
				for (int w = 0; w < CalcConst.VRES; w++) {
					double theta = STEP * w;
					pts[q][w] = new Point3d(polyX[q].doubleValue(), axisVal.doubleValue() + rad * cos(theta), rad * sin(theta));
				}
				pts[q][CalcConst.VRES] = pts[q][0];
			}
		}
		else {
			for (int q = 0; q < nStrips; q++) {
				double rad = polyX[q].doubleValue() - axisVal.doubleValue();
				for (int w = 0; w < CalcConst.VRES; w++) {
					double theta = STEP * w;
					pts[q][w] = new Point3d(axisVal.doubleValue() + rad * cos(theta), polyY[q].doubleValue(), rad * sin(theta));
				}
				pts[q][CalcConst.VRES] = pts[q][0];
			}
		}
		
		Point3d[] strips = new Point3d[nPts];
		int q = 0;
		for (int w = 0; w < nStrips - 1; w++) {
			for (int e = 0; e < pts[w].length; e++) {
				strips[q++] = pts[w][e];
				strips[q++] = pts[w + 1][e];
			}
		}
		for (int e = 0; e < pts[0].length; e++) {
			strips[q++] = pts[nStrips - 1][e];
			strips[q++] = pts[0][e];
		}
		
		TriangleStripArray poly3d = new TriangleStripArray(nPts, TriangleStripArray.COORDINATES, ptCount);
		poly3d.setCoordinates(0, strips);
		return new Shape3D(poly3d);
	}
	
	public static Shape3D rotatePoint(Point2DBig pt, BigDecimal axisVal, boolean hAxis) {
		Point3d[] pts = new Point3d[CalcConst.VRES + 1];
		
		final double STEP = 2.0 * PI / CalcConst.VRES; 
		if (hAxis) {
			double rad = pt.getY().doubleValue() - axisVal.doubleValue();
			for (int q = 0; q < CalcConst.VRES; q++) {
				double theta = STEP * q;
				pts[q] = new Point3d(pt.getX().doubleValue(), axisVal.doubleValue() + rad * cos(theta), rad * sin(theta));
			}
			pts[CalcConst.VRES] = pts[0];
		}
		else {
			double rad = pt.getX().doubleValue() - axisVal.doubleValue();
			for (int q = 0; q < CalcConst.VRES; q++) {
				double theta = STEP * q;
				pts[q] = new Point3d(axisVal.doubleValue() + rad * cos(theta), pt.getY().doubleValue(), rad * sin(theta));
			}
			pts[CalcConst.VRES] = pts[0];
		}
		
		LineStripArray line3d = new LineStripArray(pts.length, LineStripArray.COORDINATES, new int[]{pts.length});
		line3d.setCoordinates(0, pts);
		return new Shape3D(line3d);
	}
	
	public static Shape3D extrudeShape(PolygonBig poly, boolean shapePerpX, BigDecimal height, CrossSectionType shape) {
		if (shape == CrossSectionType.ISO_TRIANGLE || shape == CrossSectionType.RECTANGLE) {
			if (height == null)
				throw new IllegalArgumentException("Constant height required for Iso. Tri. and Rect.");
		}
		
		Point3d[][] pts;
		switch (shape) {
		case CIRCLE:
			pts = extrudeCircle(poly, shapePerpX);
			break;
		case EQUI_TRIANGLE:
			pts = extrudeEquiTri(poly, shapePerpX);
			break;
		case SEMICIRCLE:
			pts = extrudeSemicircle(poly, shapePerpX);
			break;
		case SQUARE:
			pts = extrudeSquare(poly, shapePerpX);
			break;
		case ISO_TRIANGLE:
			pts = extrudeIsoTri(poly, shapePerpX, height);
			break;
		case RECTANGLE:
			pts = extrudeRect(poly, shapePerpX, height);
			break;
		default:
			return null;
		}
		
		Point3d[] strips = new Point3d[2 * (pts.length - 1) * pts[0].length];
		int[] ptCount = new int[pts.length - 1];
		int q = 0;
		for (int w = 0; w < pts.length - 1; w++) {
			for (int e = 0; e < pts[w].length; e++) {
				strips[q++] = pts[w][e];
				strips[q++] = pts[w + 1][e];
			}
			ptCount[w] = 2 * pts[w].length;
		}
		
		TriangleStripArray poly3d = new TriangleStripArray(strips.length, TriangleStripArray.COORDINATES, ptCount);
		poly3d.setCoordinates(0, strips);
		return new Shape3D(poly3d);
	}
	
	public static Shape3D extrudeWires(PolygonBig poly, boolean shapePerpX, BigDecimal height, int nCuts, CrossSectionType shape) {
		if (shape == CrossSectionType.ISO_TRIANGLE || shape == CrossSectionType.RECTANGLE) {
			if (height == null)
				throw new IllegalArgumentException("Constant height required for Iso. Tri. and Rect.");
		}
		
		Point3d[][] pts;
		switch (shape) {
		case CIRCLE:
			pts = extrudeCircle(poly, shapePerpX);
			break;
		case EQUI_TRIANGLE:
			pts = extrudeEquiTri(poly, shapePerpX);
			break;
		case SEMICIRCLE:
			pts = extrudeSemicircle(poly, shapePerpX);
			break;
		case SQUARE:
			pts = extrudeSquare(poly, shapePerpX);
			break;
		case ISO_TRIANGLE:
			pts = extrudeIsoTri(poly, shapePerpX, height);
			break;
		case RECTANGLE:
			pts = extrudeRect(poly, shapePerpX, height);
			break;
		default:
			return null;
		}
		
		int indInt = (int) Math.round(((double) pts.length) / ((double) nCuts));
		
		int[] cutInd = new int[nCuts + 1];
		for (int q = 1; q < nCuts; q++) {
			cutInd[q] = indInt * q - 1; 
		}
		cutInd[cutInd.length - 1] = pts.length - 1;
		
		int nRad = (pts[0].length <= nCuts) ? pts[0].length : 10;
		indInt = (int) Math.round(((double) pts[0].length) / ((double) nRad));
		
		int[] radInd = new int[nRad + ((shape == CrossSectionType.SEMICIRCLE) ? 2 : 1)];
		for (int q = 1; q < nRad; q++) {
			radInd[q] = indInt * q - 1;
		}
		if (shape == CrossSectionType.SEMICIRCLE) radInd[radInd.length - 2] = pts[0].length - 2;
		radInd[radInd.length - 1] = pts[0].length - 1;
		
		Point3d[][] lenWires = new Point3d[radInd.length][pts.length];
		for (int q = 0; q < radInd.length; q++) {
			for (int w = 0; w < pts.length; w++) {
				lenWires[q][w] = pts[w][radInd[q]];
			}
		}
		
		Point3d[][] cutWires = new Point3d[cutInd.length][pts[0].length];
		for (int q = 0; q < cutInd.length; q++) {
			for (int w = 0; w < pts[0].length; w++) {
				cutWires[q][w] = pts[cutInd[q]][w];
			}
		}
		
		Point3d[] wires = new Point3d[lenWires.length * lenWires[0].length + cutWires.length * cutWires[0].length];
		int[] wireLen = new int[lenWires.length + cutWires.length];
		int c = 0;
		int cL = 0;
		for (int q = 0; q < lenWires.length; q++) {
			for (int w = 0; w < lenWires[q].length; w++) {
				wires[c++] = lenWires[q][w];
			}
			wireLen[cL++] = lenWires[q].length;
		}
		for (int q = 0; q < cutWires.length; q++) {
			for (int w = 0; w < cutWires[q].length; w++) {
				wires[c++] = cutWires[q][w];
			}
			wireLen[cL++] = cutWires[q].length;
		}
		
		LineStripArray line3d = new LineStripArray(wires.length, LineStripArray.COORDINATES, wireLen);
		line3d.setCoordinates(0, wires);
		return new Shape3D(line3d);
	}
	
	private static Point3d[][] extrudeSquare(PolygonBig poly, boolean shapePerpX) {
		Point3d[][] pts = new Point3d[CalcConst.VRES + 1][5];
		
		final double STEP = ((shapePerpX) ? poly.getMaxX().doubleValue() - poly.getMinX().doubleValue() :
			poly.getMaxY().doubleValue() - poly.getMinY().doubleValue()) / CalcConst.VRES;
		if (shapePerpX) {
			pts[0][0] = pts[0][1] = pts[0][2] = pts[0][3] = pts[0][4] = 
				new Point3d(poly.getMinX().doubleValue(), poly.getYAtMinX().doubleValue(), 0.0);
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal xToUse = poly.getMinX().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] heightCoord = poly.getYBoundsAt(xToUse);
				double height = heightCoord[1].doubleValue() - heightCoord[0].doubleValue();
				pts[q][0] = new Point3d(xToUse.doubleValue(), heightCoord[1].doubleValue(), 0.0);
				pts[q][1] = new Point3d(xToUse.doubleValue(), heightCoord[0].doubleValue(), 0.0);
				pts[q][2] = new Point3d(xToUse.doubleValue(), heightCoord[0].doubleValue(), height);
				pts[q][3] = new Point3d(xToUse.doubleValue(), heightCoord[1].doubleValue(), height);
				pts[q][4] = pts[q][0];
			}
			pts[CalcConst.VRES][0] = pts[CalcConst.VRES][1] = pts[CalcConst.VRES][2] = pts[CalcConst.VRES][3] = pts[CalcConst.VRES][4] = 
				new Point3d(poly.getMaxX().doubleValue(), poly.getYAtMaxX().doubleValue(), 0.0);
		}
		else {
			pts[0][0] = pts[0][1] = pts[0][2] = pts[0][3] = pts[0][4] = 
				new Point3d(poly.getXAtMinY().doubleValue(), poly.getMinY().doubleValue(), 0.0);
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal yToUse = poly.getMinY().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] widthCoord = poly.getXBoundsAt(yToUse);
				double width = widthCoord[1].doubleValue() - widthCoord[0].doubleValue();
				pts[q][0] = new Point3d(widthCoord[1].doubleValue(), yToUse.doubleValue(), 0.0);
				pts[q][1] = new Point3d(widthCoord[0].doubleValue(), yToUse.doubleValue(), 0.0);
				pts[q][2] = new Point3d(widthCoord[0].doubleValue(), yToUse.doubleValue(), width);
				pts[q][3] = new Point3d(widthCoord[1].doubleValue(), yToUse.doubleValue(), width);
				pts[q][4] = pts[q][0];
			}
			pts[CalcConst.VRES][0] = pts[CalcConst.VRES][1] = pts[CalcConst.VRES][2] = pts[CalcConst.VRES][3] = pts[CalcConst.VRES][4] = 
				new Point3d(poly.getXAtMaxY().doubleValue(), poly.getMaxY().doubleValue(), 0.0);
		}
		
		return pts;
	}
	
	private static Point3d[][] extrudeCircle(PolygonBig poly, boolean shapePerpX) {
		Point3d[][] pts = new Point3d[CalcConst.VRES + 1][CalcConst.VRES + 1];
		
		final double STEP = ((shapePerpX) ? poly.getMaxX().doubleValue() - poly.getMinX().doubleValue() :
			poly.getMaxY().doubleValue() - poly.getMinY().doubleValue()) / CalcConst.VRES;
		final double THETA_STEP = 2 * Math.PI / CalcConst.VRES;
		
		if (shapePerpX) {
			for (int q = 0; q <= CalcConst.VRES; q++) 
				pts[0][q] = new Point3d(poly.getMinX().doubleValue(), poly.getYAtMinX().doubleValue(), 0.0);
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal xToUse = poly.getMinX().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] heightCoord = poly.getYBoundsAt(xToUse);
				double center = (heightCoord[1].doubleValue() + heightCoord[0].doubleValue()) / 2.0;
				double radius = (heightCoord[1].doubleValue() - heightCoord[0].doubleValue()) / 2.0;
				for (int w = 0; w < CalcConst.VRES; w++) {
					double theta = THETA_STEP * w;
					pts[q][w] = new Point3d(xToUse.doubleValue(), center + radius * Math.cos(theta), radius * Math.sin(theta));
				}
				pts[q][pts[q].length - 1] = pts[q][0];
			}
			for (int q = 0; q <= CalcConst.VRES; q++) 
				pts[CalcConst.VRES][q] = new Point3d(poly.getMaxX().doubleValue(), poly.getYAtMaxX().doubleValue(), 0.0);
		}
		else {
			for (int q = 0; q <= CalcConst.VRES; q++)
				pts[0][q] = new Point3d(poly.getXAtMinY().doubleValue(), poly.getMinY().doubleValue(), 0.0);
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal yToUse = poly.getMinY().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] widthCoord = poly.getXBoundsAt(yToUse);
				double center = (widthCoord[1].doubleValue() + widthCoord[0].doubleValue()) / 2.0;
				double radius = (widthCoord[1].doubleValue() - widthCoord[0].doubleValue()) / 2.0;
				for (int w = 0; w < CalcConst.VRES; w++) {
					double theta = THETA_STEP * w;
					pts[q][w] = new Point3d(center + radius * Math.cos(theta), yToUse.doubleValue(), radius * Math.sin(theta));
				}
				pts[q][pts[q].length - 1] = pts[q][0];
			}
			for (int q = 0; q <= CalcConst.VRES; q++)
				pts[CalcConst.VRES][q] = new Point3d(poly.getXAtMaxY().doubleValue(), poly.getMaxY().doubleValue(), 0.0);
		}
		
		return pts;
	}
	
	private static Point3d[][] extrudeSemicircle(PolygonBig poly, boolean shapePerpX) {
		Point3d[][] pts = new Point3d[CalcConst.VRES + 1][(int) (CalcConst.VRES / 2.0) + 2];
		
		final double STEP = ((shapePerpX) ? poly.getMaxX().doubleValue() - poly.getMinX().doubleValue() :
			poly.getMaxY().doubleValue() - poly.getMinY().doubleValue()) / CalcConst.VRES;
		final double THETA_STEP = 2 * Math.PI / CalcConst.VRES;
		
		if (shapePerpX) {
			for (int q = 0; q < pts[0].length; q++) 
				pts[0][q] = new Point3d(poly.getMinX().doubleValue(), poly.getYAtMinX().doubleValue(), 0.0);
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal xToUse = poly.getMinX().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] heightCoord = poly.getYBoundsAt(xToUse);
				double center = (heightCoord[1].doubleValue() + heightCoord[0].doubleValue()) / 2.0;
				double radius = (heightCoord[1].doubleValue() - heightCoord[0].doubleValue()) / 2.0;
				for (int w = 0; w < pts[q].length - 1; w++) {
					double theta = THETA_STEP * w;
					pts[q][w] = new Point3d(xToUse.doubleValue(), center + radius * Math.cos(theta), radius * Math.sin(theta));
				}
				pts[q][pts[q].length - 1] = pts[q][0];
			}
			for (int q = 0; q < pts[0].length; q++) 
				pts[CalcConst.VRES][q] = new Point3d(poly.getMaxX().doubleValue(), poly.getYAtMaxX().doubleValue(), 0.0);
		}
		else {
			for (int q = 0; q < pts[0].length; q++)
				pts[0][q] = new Point3d(poly.getXAtMinY().doubleValue(), poly.getMinY().doubleValue(), 0.0);
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal yToUse = poly.getMinY().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] widthCoord = poly.getXBoundsAt(yToUse);
				double center = (widthCoord[1].doubleValue() + widthCoord[0].doubleValue()) / 2.0;
				double radius = (widthCoord[1].doubleValue() - widthCoord[0].doubleValue()) / 2.0;
				for (int w = 0; w < pts[q].length - 1; w++) {
					double theta = THETA_STEP * w;
					pts[q][w] = new Point3d(center + radius * Math.cos(theta), yToUse.doubleValue(), radius * Math.sin(theta));
				}
				pts[q][pts[q].length - 1] = pts[q][0];
			}
			for (int q = 0; q < pts[0].length; q++)
				pts[CalcConst.VRES][q] = new Point3d(poly.getXAtMaxY().doubleValue(), poly.getMaxY().doubleValue(), 0.0);
		}
		
		return pts;
	}
	
	private static Point3d[][] extrudeEquiTri(PolygonBig poly, boolean shapePerpX) {
		Point3d[][] pts = new Point3d[CalcConst.VRES + 1][4];
		
		final double STEP = ((shapePerpX) ? poly.getMaxX().doubleValue() - poly.getMinX().doubleValue() :
			poly.getMaxY().doubleValue() - poly.getMinY().doubleValue()) / CalcConst.VRES;
		
		if (shapePerpX) {
			pts[0][0] = pts[0][1] = pts[0][2] = pts[0][3] = 
				new Point3d(poly.getMinX().doubleValue(), poly.getYAtMinX().doubleValue(), 0.0);
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal xToUse = poly.getMinX().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] heightCoord = poly.getYBoundsAt(xToUse);
				double height = heightCoord[1].doubleValue() - heightCoord[0].doubleValue();
				double centerY = (heightCoord[1].doubleValue() + heightCoord[0].doubleValue()) / 2.0;
				pts[q][0] = new Point3d(xToUse.doubleValue(), heightCoord[1].doubleValue(), 0.0);
				pts[q][1] = new Point3d(xToUse.doubleValue(), centerY, height / 2.0 * Math.sqrt(3.0));
				pts[q][2] = new Point3d(xToUse.doubleValue(), heightCoord[0].doubleValue(), 0.0);
				pts[q][3] = pts[q][0];
			}
			pts[CalcConst.VRES][0] = pts[CalcConst.VRES][1] = pts[CalcConst.VRES][2] = pts[CalcConst.VRES][3] =  
				new Point3d(poly.getMaxX().doubleValue(), poly.getYAtMaxX().doubleValue(), 0.0);
		}
		else {
			pts[0][0] = pts[0][1] = pts[0][2] = pts[0][3] = 
				new Point3d(poly.getXAtMinY().doubleValue(), poly.getMinY().doubleValue(), 0.0);
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal yToUse = poly.getMinY().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] widthCoord = poly.getXBoundsAt(yToUse);
				double width = widthCoord[1].doubleValue() - widthCoord[0].doubleValue();
				double centerX = (widthCoord[1].doubleValue() + widthCoord[0].doubleValue()) / 2.0;
				pts[q][0] = new Point3d(widthCoord[1].doubleValue(), yToUse.doubleValue(), 0.0);
				pts[q][1] = new Point3d(centerX, yToUse.doubleValue(), width / 2.0 * Math.sqrt(3.0));
				pts[q][2] = new Point3d(widthCoord[0].doubleValue(), yToUse.doubleValue(), 0.0);
				pts[q][3] = pts[q][0];
			}
			pts[CalcConst.VRES][0] = pts[CalcConst.VRES][1] = pts[CalcConst.VRES][2] = pts[CalcConst.VRES][3] =
				new Point3d(poly.getXAtMaxY().doubleValue(), poly.getMaxY().doubleValue(), 0.0);
		}
		
		return pts;
	}
	
	private static Point3d[][] extrudeRect(PolygonBig poly, boolean shapePerpX, BigDecimal height) {
		Point3d[][] pts = new Point3d[CalcConst.VRES + 1][5];
		
		final double STEP = ((shapePerpX) ? poly.getMaxX().doubleValue() - poly.getMinX().doubleValue() :
			poly.getMaxY().doubleValue() - poly.getMinY().doubleValue()) / CalcConst.VRES;
		if (shapePerpX) {
			pts[0][0] = pts[0][1] = pts[0][4] =
				new Point3d(poly.getMinX().doubleValue(), poly.getYAtMinX().doubleValue(), 0.0);
			pts[0][2] = pts[0][3] = 
				new Point3d(poly.getMinX().doubleValue(), poly.getYAtMinX().doubleValue(), height.doubleValue());
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal xToUse = poly.getMinX().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] heightCoord = poly.getYBoundsAt(xToUse);
				pts[q][0] = new Point3d(xToUse.doubleValue(), heightCoord[1].doubleValue(), 0.0);
				pts[q][1] = new Point3d(xToUse.doubleValue(), heightCoord[0].doubleValue(), 0.0);
				pts[q][2] = new Point3d(xToUse.doubleValue(), heightCoord[0].doubleValue(), height.doubleValue());
				pts[q][3] = new Point3d(xToUse.doubleValue(), heightCoord[1].doubleValue(), height.doubleValue());
				pts[q][4] = pts[q][0];
			}
			pts[CalcConst.VRES][0] = pts[CalcConst.VRES][1] = pts[CalcConst.VRES][4] = 
				new Point3d(poly.getMaxX().doubleValue(), poly.getYAtMaxX().doubleValue(), 0.0);
			pts[CalcConst.VRES][2] = pts[CalcConst.VRES][3] = 
				new Point3d(poly.getMaxX().doubleValue(), poly.getYAtMaxX().doubleValue(), height.doubleValue());
		}
		else {
			pts[0][0] = pts[0][1] = pts[0][4] = 
				new Point3d(poly.getXAtMinY().doubleValue(), poly.getMinY().doubleValue(), 0.0);
			pts[0][2] = pts[0][3] = 
				new Point3d(poly.getXAtMinY().doubleValue(), poly.getMinY().doubleValue(), height.doubleValue());
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal yToUse = poly.getMinY().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] widthCoord = poly.getXBoundsAt(yToUse);
				pts[q][0] = new Point3d(widthCoord[1].doubleValue(), yToUse.doubleValue(), 0.0);
				pts[q][1] = new Point3d(widthCoord[0].doubleValue(), yToUse.doubleValue(), 0.0);
				pts[q][2] = new Point3d(widthCoord[0].doubleValue(), yToUse.doubleValue(), height.doubleValue());
				pts[q][3] = new Point3d(widthCoord[1].doubleValue(), yToUse.doubleValue(), height.doubleValue());
				pts[q][4] = pts[q][0];
			}
			pts[CalcConst.VRES][0] = pts[CalcConst.VRES][1] = pts[CalcConst.VRES][4] = 
				new Point3d(poly.getXAtMaxY().doubleValue(), poly.getMaxY().doubleValue(), 0.0);
			pts[CalcConst.VRES][2] = pts[CalcConst.VRES][3] = 
				new Point3d(poly.getXAtMaxY().doubleValue(), poly.getMaxY().doubleValue(), height.doubleValue());
		}
		
		return pts;
	}
	
	private static Point3d[][] extrudeIsoTri(PolygonBig poly, boolean shapePerpX, BigDecimal height) {
		Point3d[][] pts = new Point3d[CalcConst.VRES + 1][4];
		
		final double STEP = ((shapePerpX) ? poly.getMaxX().doubleValue() - poly.getMinX().doubleValue() :
			poly.getMaxY().doubleValue() - poly.getMinY().doubleValue()) / CalcConst.VRES;
		
		if (shapePerpX) {
			pts[0][0] = pts[0][2] = pts[0][3] = 
				new Point3d(poly.getMinX().doubleValue(), poly.getYAtMinX().doubleValue(), 0.0);
			pts[0][1] = new Point3d(poly.getMinX().doubleValue(), poly.getYAtMinX().doubleValue(), height.doubleValue());
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal xToUse = poly.getMinX().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] heightCoord = poly.getYBoundsAt(xToUse);
				double centerY = (heightCoord[1].doubleValue() + heightCoord[0].doubleValue()) / 2.0;
				pts[q][0] = new Point3d(xToUse.doubleValue(), heightCoord[1].doubleValue(), 0.0);
				pts[q][1] = new Point3d(xToUse.doubleValue(), centerY, height.doubleValue());
				pts[q][2] = new Point3d(xToUse.doubleValue(), heightCoord[0].doubleValue(), 0.0);
				pts[q][3] = pts[q][0];
			}
			pts[CalcConst.VRES][0] = pts[CalcConst.VRES][2] = pts[CalcConst.VRES][3] =  
				new Point3d(poly.getMaxX().doubleValue(), poly.getYAtMaxX().doubleValue(), 0.0);
			pts[CalcConst.VRES][1] = new Point3d(poly.getMaxX().doubleValue(), poly.getYAtMaxX().doubleValue(), height.doubleValue());
		}
		else {
			pts[0][0] = pts[0][2] = pts[0][3] = 
				new Point3d(poly.getXAtMinY().doubleValue(), poly.getMinY().doubleValue(), 0.0);
			pts[0][1] = new Point3d(poly.getXAtMinY().doubleValue(), poly.getMinY().doubleValue(), height.doubleValue()); 
			for (int q = 1; q < CalcConst.VRES; q++) {
				BigDecimal yToUse = poly.getMinY().add((new BigDecimal(STEP)).
						multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
				BigDecimal[] widthCoord = poly.getXBoundsAt(yToUse);
				double centerX = (widthCoord[1].doubleValue() + widthCoord[0].doubleValue()) / 2.0;
				pts[q][0] = new Point3d(widthCoord[1].doubleValue(), yToUse.doubleValue(), 0.0);
				pts[q][1] = new Point3d(centerX, yToUse.doubleValue(), height.doubleValue());
				pts[q][2] = new Point3d(widthCoord[0].doubleValue(), yToUse.doubleValue(), 0.0);
				pts[q][3] = pts[q][0];
			}
			pts[CalcConst.VRES][0] = pts[CalcConst.VRES][2] = pts[CalcConst.VRES][3] =
				new Point3d(poly.getXAtMaxY().doubleValue(), poly.getMaxY().doubleValue(), 0.0);
			pts[CalcConst.VRES][1] = new Point3d(poly.getXAtMaxY().doubleValue(), poly.getMaxY().doubleValue(), height.doubleValue());
		}
		
		return pts;
	}
}