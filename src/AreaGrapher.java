import java.math.*;
import java.util.*;
import javax.vecmath.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.*;

public class AreaGrapher extends Grapher {
	private static final long serialVersionUID = 3242743646013425267L;

	public static final boolean DEBUG = false;
	
	private final boolean HAS_ROT_AXIS;
	
	public AreaGrapher(PolygonBig poly, Equation[] equs, BigDecimal axis, boolean hAxis, int nInt, GraphType type) {
		super(poly, equs, axis, hAxis, nInt, type);
		
		HAS_ROT_AXIS = (axis != null); 
		
		double zFact, zoomFit = 2.0;
		if (HAS_ROT_AXIS) {
			if (H_AXIS) zFact = zoomFit * Math.max(AXIS.doubleValue() - POLY.getMinY().doubleValue(), 
					POLY.getMaxY().doubleValue() - AXIS.doubleValue());
			else zFact = zoomFit * Math.max(AXIS.doubleValue() - POLY.getMinX().doubleValue(), 
					POLY.getMaxX().doubleValue() - AXIS.doubleValue());
		}
		else {
			zFact = zoomFit * Math.max(POLY.getMaxX().doubleValue() - POLY.getMinX().doubleValue(), 
					POLY.getMaxY().doubleValue() - POLY.getMinY().doubleValue());
		}
		
		Transform3D pCenter = new Transform3D();
		pCenter.setTranslation(new Vector3d((POLY.getMaxX().doubleValue() + POLY.getMinX().doubleValue()) / 2.0, 
				(POLY.getMaxY().doubleValue() + POLY.getMinY().doubleValue()) / 2.0, zFact));
		
		orbit.setReverseTranslate(true);
		orbit.setRotateEnable(false);
		orbit.setHomeTransform(pCenter);
	}

	public AreaGrapher(Equation[] equs, BigDecimal axis, boolean hAxis) {
		super(null, equs, axis, hAxis, 0, GraphType.FLAT_NO_POLY);
		
		HAS_ROT_AXIS = (axis != null); 
		
		Transform3D pCenter = new Transform3D();
		pCenter.setTranslation(new Vector3d(0.0, 0.0, 30.0));

		orbit.setReverseTranslate(true);
		orbit.setRotateEnable(false);
		orbit.setHomeTransform(pCenter);
	}

	public void resetView() {
		orbit.goHome();
	}

	protected void addEquation(int index) {
		if (index >= EQS.length) return;
		if ((capabilities & SHOW_EQ[index]) != 0) return;

		BigDecimal pMin, pMax;
		if (GTYPE != GraphType.FLAT_NO_POLY) {
			if (EQS[index].isEqInX()) {
				pMin = POLY.getMinX();
				pMax = POLY.getMaxX();
			}
			else {
				pMin = POLY.getMinY();
				pMax = POLY.getMaxY();
			}
		}
		else {
			pMin = new BigDecimal("-10.0");
			pMax = new BigDecimal("10.0");
		}

		ArrayList<Point3d> beforeList = new ArrayList<Point3d>();
		ArrayList<Point3d> duringList = new ArrayList<Point3d>();
		ArrayList<Point3d> afterList = new ArrayList<Point3d>();

		for (BigDecimal q = pMin; q.compareTo(new BigDecimal(CalcConst.MIN_COORD)) >= 0; q = q.subtract(CalcConst.PRESO, CalcConst.MC)) {
			if (EQS[index].isEqInX()) {
				try {
					beforeList.add(new Point3d(q.doubleValue(), EQS[index].eval(q).doubleValue(), 0.0));
				}
				catch (OperatorDomainException eOD) { continue; }
			}
			else {
				try {
					beforeList.add(new Point3d(EQS[index].eval(q).doubleValue(), q.doubleValue(), 0.0));
				}
				catch (OperatorDomainException eOD) { continue; }
			}
		}

		for (BigDecimal q = pMax; q.compareTo(new BigDecimal(CalcConst.MAX_COORD)) <= 0; q = q.add(CalcConst.PRESO, CalcConst.MC)) {
			if (EQS[index].isEqInX()) {
				try {
					afterList.add(new Point3d(q.doubleValue(), EQS[index].eval(q).doubleValue(), 0.0));
				}
				catch (OperatorDomainException eOD) { continue; }
			}
			else {
				try {
					afterList.add(new Point3d(EQS[index].eval(q).doubleValue(), q.doubleValue(), 0.0));
				}
				catch (OperatorDomainException eOD) { continue; }
			}
		}

		BigDecimal iStep = pMax.subtract(pMin, CalcConst.MC).divide(new BigDecimal(CalcConst.PRESI), CalcConst.MC);
		iStep = iStep.min(CalcConst.PRESO);
		for (BigDecimal q = pMin; q.compareTo(pMax) <= 0; q = q.add(iStep, CalcConst.MC)) {
			if (EQS[index].isEqInX()) {
				try {
					duringList.add(new Point3d(q.doubleValue(), EQS[index].eval(q).doubleValue(), 0.0));
				}
				catch (OperatorDomainException eOD) { continue; }
			}
			else {
				try {
					duringList.add(new Point3d(EQS[index].eval(q).doubleValue(), q.doubleValue(), 0.0));
				}
				catch (OperatorDomainException eOD) { continue; }
			}
		}
		
		int bls = beforeList.size();
		Point3d[] eqPts = new Point3d[bls + duringList.size() + afterList.size()];
		int q = 0;
		for (Iterator<Point3d> it = beforeList.iterator(); it.hasNext(); ) {
			eqPts[bls - q++ - 1] = it.next();
		}
		for (Iterator<Point3d> it = duringList.iterator(); it.hasNext(); ) {
			eqPts[q++] = it.next();
		}
		for (Iterator<Point3d> it = afterList.iterator(); it.hasNext(); ) {
			eqPts[q++] = it.next();
		}

		Appearance app = new Appearance();
		app.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.EQ_COLOR[index]), ColoringAttributes.SHADE_GOURAUD));

		LineStripArray lsa = new LineStripArray(eqPts.length, LineStripArray.COORDINATES, new int[] {eqPts.length});
		lsa.setCoordinates(0, eqPts);

		Shape3D eqShape = new Shape3D(lsa, app);

		BranchGroup locRoot = new BranchGroup();
		locRoot.addChild(eqShape);
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_EQ[index];
	}

	public void addFlatPoly() {
		if (GTYPE == GraphType.FLAT_NO_POLY) return;
		if ((capabilities & SHOW_FLAT_POLY) != 0) return;
		Appearance app = new Appearance();
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);

		app.setPolygonAttributes(pAtt);
		app.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.POLY_COLOR), ColoringAttributes.SHADE_GOURAUD));
		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, CalcConst.POLY_TRANSPARENCY));

		Point2DBig[] polyPts = POLY.getPoints();
		Point3d[] pts = new Point3d[polyPts.length + 1];
		for (int q = 0; q < polyPts.length; q++) {
			pts[q] = new Point3d(polyPts[q].getX().doubleValue(), polyPts[q].getY().doubleValue(), 0.0);
		}
		pts[polyPts.length] = new Point3d(polyPts[0].getX().doubleValue(), polyPts[0].getY().doubleValue(), 0.0);

		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(pts);
		gi.setStripCounts(new int[] {pts.length});

		Shape3D fPoly = new Shape3D(gi.getGeometryArray(), app);
		BranchGroup locRoot = new BranchGroup();
		locRoot.addChild(fPoly);
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_FLAT_POLY;
	}

	public void addAxes() {
		if ((capabilities & SHOW_AXES) != 0) return;
		Appearance xApp = new Appearance();
		Appearance yApp = new Appearance();
		Appearance rApp = new Appearance();

		xApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.X_AXIS), ColoringAttributes.SHADE_GOURAUD));
		yApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.Y_AXIS), ColoringAttributes.SHADE_GOURAUD));
		rApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.R_AXIS), ColoringAttributes.SHADE_GOURAUD));

		BranchGroup locRoot = new BranchGroup();

		Point3d[] xPts = new Point3d[]{new Point3d(CalcConst.MIN_COORD, 0.0, 0.0), new Point3d(CalcConst.MAX_COORD, 0.0, 0.0)};
		Point3d[] yPts = new Point3d[]{new Point3d(0.0, CalcConst.MIN_COORD, 0.0), new Point3d(0.0, CalcConst.MAX_COORD, 0.0)};
		Point3d[] rPts = null;
		if (HAS_ROT_AXIS) {
			if (H_AXIS) {
				rPts = new Point3d[]{new Point3d(CalcConst.MIN_COORD, AXIS.doubleValue(), 0.0), new Point3d(CalcConst.MAX_COORD, AXIS.doubleValue(), 0.0)};
			}
			else {
				rPts = new Point3d[]{new Point3d(AXIS.doubleValue(), CalcConst.MIN_COORD, 0.0), new Point3d(AXIS.doubleValue(), CalcConst.MAX_COORD, 0.0)};
			}
		}

		LineStripArray xLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		xLSA.setCoordinates(0, xPts);
		LineStripArray yLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		yLSA.setCoordinates(0, yPts);
		LineStripArray rLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		if (HAS_ROT_AXIS) rLSA.setCoordinates(0, rPts);

		Shape3D xAxis = new Shape3D(xLSA, xApp);
		Shape3D yAxis = new Shape3D(yLSA, yApp);
		Shape3D rAxis = new Shape3D(rLSA, rApp);

		locRoot.addChild(xAxis);
		locRoot.addChild(yAxis);
		if (HAS_ROT_AXIS) locRoot.addChild(rAxis);

		for (double q = CalcConst.MIN_COORD; q <= CalcConst.MAX_COORD; q++) {
			Transform3D xDisp = new Transform3D();
			Transform3D yDisp = new Transform3D();

			xDisp.setTranslation(new Vector3d(q, 0.0, 0.0));
			yDisp.setTranslation(new Vector3d(0.0, q, 0.0));

			TransformGroup xtg = new TransformGroup(xDisp);
			TransformGroup ytg = new TransformGroup(yDisp);

			Shape3D xSphere = new Sphere(0.05f, xApp).getShape();
			Shape3D ySphere = new Sphere(0.05f, yApp).getShape();

			xtg.addChild(xSphere.cloneNode(true));
			ytg.addChild(ySphere.cloneNode(true));

			locRoot.addChild(xtg);
			locRoot.addChild(ytg);
		}

		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_AXES;
	}

	public void addRepShapes() {
		//TODO: add all rectangles instead of just one...
		if (GTYPE == GraphType.FLAT_NO_POLY) return;
		if ((capabilities & SHOW_REP_SHAPES) != 0) return;
		boolean H_RECT = (GTYPE == GraphType.FLAT_WITH_POLY_DISC) ^ H_AXIS;
		BigDecimal width, wCent;
		if (H_RECT) {
			width = POLY.getMaxY().subtract(POLY.getMinY(), CalcConst.MC).divide(new BigDecimal(N_INTERVAL), CalcConst.MC);
			wCent = POLY.getMaxY().add(POLY.getMinY(), CalcConst.MC).divide(new BigDecimal("2.0"), CalcConst.MC);
		}
		else {
			width = POLY.getMaxX().subtract(POLY.getMinX(), CalcConst.MC).divide(new BigDecimal(N_INTERVAL), CalcConst.MC);
			wCent = POLY.getMaxX().add(POLY.getMinX(), CalcConst.MC).divide(new BigDecimal("2.0"), CalcConst.MC);
		}
		BigDecimal halfWidth = width.divide(new BigDecimal("2.0"), CalcConst.MC);

		BigDecimal[] length;
		if (H_RECT) length = POLY.getXBoundsAt(wCent);
		else length = POLY.getYBoundsAt(wCent);
		if (length == null) return;

		BigDecimal top, bottom, left, right;
		if (H_RECT) {
			top = wCent.add(halfWidth, CalcConst.MC);
			bottom = top.subtract(width, CalcConst.MC);
			left = length[0];
			right = length[1];
		}
		else {
			top = length[1];
			bottom = length[0];
			left = wCent.subtract(halfWidth, CalcConst.MC);
			right = left.add(width, CalcConst.MC);
		}

		Point2DBig[] rectPts = new Point2DBig[] { new Point2DBig(left, bottom),
				new Point2DBig(left, top),
				new Point2DBig(right, top),
				new Point2DBig(right, bottom) };
		Point3d[] rect3Pts = new Point3d[] { new Point3d(rectPts[0].getX().doubleValue(), rectPts[0].getY().doubleValue(), 0.0), 
				new Point3d(rectPts[1].getX().doubleValue(), rectPts[1].getY().doubleValue(), 0.0), 
				new Point3d(rectPts[2].getX().doubleValue(), rectPts[2].getY().doubleValue(), 0.0), 
				new Point3d(rectPts[3].getX().doubleValue(), rectPts[3].getY().doubleValue(), 0.0),
				new Point3d(rectPts[0].getX().doubleValue(), rectPts[0].getY().doubleValue(), 0.0)};
		LineStripArray lsa = new LineStripArray(5, LineStripArray.COORDINATES, new int[]{5});
		lsa.setCoordinates(0, rect3Pts);
		Shape3D rect = new Shape3D(lsa);

		Appearance rectApp = new Appearance();
		rectApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.RECT_COLOR), ColoringAttributes.SHADE_GOURAUD));
		rect.setAppearance(rectApp);

		BranchGroup locRoot = new BranchGroup();
		locRoot.addChild(rect);
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_REP_SHAPES;
	}

	public void addSolid() {
		//Do nothing, solid isn't present in 2D graph..
		return;
	}

	public void addWireframes() {
		//Do nothing, wireframe isn't present in 2D graph..
		return;
	}

	public void addDefaults() {
		addFlatPoly();
		addAxes();
		addEquations();
		addRepShapes();
	}
}