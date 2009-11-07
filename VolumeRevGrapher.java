import java.math.*;
import javax.vecmath.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.*;

public class VolumeRevGrapher extends Grapher {
	private static final long serialVersionUID = -3113895888133881427L;
	
	public static final boolean DEBUG = false;
	
	public VolumeRevGrapher(PolygonBig poly, Equation[] equs, 
			BigDecimal axis, boolean hAxis, int nInt, boolean disc) {
		super(poly, equs, axis, hAxis, nInt, (disc) ? GraphType.VOL_REV_DISC : GraphType.VOL_REV_SHELL);
		
		Transform3D pCenter = new Transform3D();
		double xTrans, yTrans, zTrans, maxR, zoomFit = 3.0;
		if (H_AXIS) {
			xTrans = (POLY.getMaxX().doubleValue() + POLY.getMinX().doubleValue()) / 2.0;
			yTrans = AXIS.doubleValue();
			zTrans = 0.0;
			
			maxR = Math.max(AXIS.doubleValue() - POLY.getMinY().doubleValue(), 
					POLY.getMaxY().doubleValue() - AXIS.doubleValue());
		}
		else {
			xTrans = AXIS.doubleValue();
			yTrans = (POLY.getMaxY().doubleValue() + POLY.getMinY().doubleValue()) / 2.0;
			zTrans = 0.0;
			
			maxR = Math.max(AXIS.doubleValue() - POLY.getMinX().doubleValue(), 
					POLY.getMaxX().doubleValue() - AXIS.doubleValue());
		}
		xTrans += zoomFit * maxR * Math.cos(TILT3D_X);
		yTrans += -1 * zoomFit * maxR * Math.sin(TILT3D_X);
		zTrans += zoomFit * maxR * Math.sin(TILT3D_Y);
		pCenter.setTranslation(new Vector3d(xTrans, yTrans, zTrans));
		
		Transform3D rotX = new Transform3D();
		rotX.rotX(TILT3D_X);
		Transform3D rotY = new Transform3D();
		rotY.rotY(TILT3D_Y);
		pCenter.mul(rotY);
		pCenter.mul(rotX);
		
		orbit.setReverseRotate(true);
		orbit.setReverseTranslate(true);
		orbit.setHomeTransform(pCenter);
	}
	
	public void resetView() {
		orbit.goHome();
	}
	
	public void addSolid() {
		if ((capabilities & SHOW_SOLID) != 0) return;
		Shape3D lPoly = SolidMaker.rotateShape(POLY, AXIS, H_AXIS);
		Appearance app = new Appearance();
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		
		app.setPolygonAttributes(pAtt);
		app.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.SOLID_COLOR), ColoringAttributes.SHADE_GOURAUD));
		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, CalcConst.SOLID_TRANSPARENCY));
		
		lPoly.setAppearance(app);
		
		BranchGroup locRoot = new BranchGroup();
		locRoot.addChild(lPoly);
		
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_SOLID;
	}
	
	public void addWireframes() {
		if ((capabilities & SHOW_WIREFRAMES) != 0) return;
		Appearance app = new Appearance();
		
		app.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.SOLID_WIRE_COLOR), ColoringAttributes.SHADE_GOURAUD));
		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, CalcConst.SOLID_TRANSPARENCY));
		
		BranchGroup locRoot = new BranchGroup();
		
		Point2DBig[] pts = POLY.getPoints();
		for (int q = 0; q < pts.length; q += 4) {
			Shape3D wireLine = SolidMaker.rotatePoint(pts[q], AXIS, H_AXIS);
			wireLine.setAppearance(app);
			locRoot.addChild(wireLine);
		}
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_WIREFRAMES;
	}
	
	public void addFlatPoly() {
		if ((capabilities & SHOW_FLAT_POLY) != 0) return;
		Appearance app = new Appearance();
		app.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.SOLID_WIRE_COLOR), ColoringAttributes.SHADE_GOURAUD));
		
		Point2DBig[] polyPts = POLY.getPoints();
		Point3d[] pts = new Point3d[polyPts.length + 1];
		for (int q = 0; q < polyPts.length; q++) {
			pts[q] = new Point3d(polyPts[q].getX().doubleValue(), polyPts[q].getY().doubleValue(), 0.0);
		}
		pts[polyPts.length] = new Point3d(polyPts[0].getX().doubleValue(), polyPts[0].getY().doubleValue(), 0.0);
		
		LineStripArray lsa = new LineStripArray(pts.length, LineStripArray.COORDINATES, new int[]{pts.length});
		lsa.setCoordinates(0, pts);
		
		BranchGroup locRoot = new BranchGroup();
		Shape3D fPoly = new Shape3D(lsa, app);
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
		Appearance zApp = new Appearance();
		Appearance rApp = new Appearance();
		
		xApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.X_AXIS), ColoringAttributes.SHADE_GOURAUD));
		yApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.Y_AXIS), ColoringAttributes.SHADE_GOURAUD));
		zApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.Z_AXIS), ColoringAttributes.SHADE_GOURAUD));
		rApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.R_AXIS), ColoringAttributes.SHADE_GOURAUD));
		
		BranchGroup locRoot = new BranchGroup();
		
		Point3d[] xPts = new Point3d[]{new Point3d(CalcConst.MIN_COORD, 0.0, 0.0), new Point3d(CalcConst.MAX_COORD, 0.0, 0.0)};
		Point3d[] yPts = new Point3d[]{new Point3d(0.0, CalcConst.MIN_COORD, 0.0), new Point3d(0.0, CalcConst.MAX_COORD, 0.0)};
		Point3d[] zPts = new Point3d[]{new Point3d(0.0, 0.0, CalcConst.MIN_COORD), new Point3d(0.0, 0.0, CalcConst.MAX_COORD)};
		Point3d[] rPts;
		if (H_AXIS) {
			rPts = new Point3d[]{new Point3d(-100.0, AXIS.doubleValue(), 0.0), new Point3d(100.0, AXIS.doubleValue(), 0.0)};
		}
		else {
			rPts = new Point3d[]{new Point3d(AXIS.doubleValue(), -100.0, 0.0), new Point3d(AXIS.doubleValue(), 100.0, 0.0)};
		}
		
		LineStripArray xLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		xLSA.setCoordinates(0, xPts);
		LineStripArray yLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		yLSA.setCoordinates(0, yPts);
		LineStripArray zLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		zLSA.setCoordinates(0, zPts);
		LineStripArray rLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		rLSA.setCoordinates(0, rPts);
		
		Shape3D xAxis = new Shape3D(xLSA, xApp);
		Shape3D yAxis = new Shape3D(yLSA, yApp);
		Shape3D zAxis = new Shape3D(zLSA, zApp);
		Shape3D rAxis = new Shape3D(rLSA, rApp);
		
		locRoot.addChild(xAxis);
		locRoot.addChild(yAxis);
		locRoot.addChild(zAxis);
		locRoot.addChild(rAxis);
		
		for (double q = CalcConst.MIN_COORD; q <= CalcConst.MAX_COORD; q++) {
			Transform3D xDisp = new Transform3D();
			Transform3D yDisp = new Transform3D();
			Transform3D zDisp = new Transform3D();
			
			xDisp.setTranslation(new Vector3d(q, 0.0, 0.0));
			yDisp.setTranslation(new Vector3d(0.0, q, 0.0));
			zDisp.setTranslation(new Vector3d(0.0, 0.0, q));
			
			TransformGroup xtg = new TransformGroup(xDisp);
			TransformGroup ytg = new TransformGroup(yDisp);
			TransformGroup ztg = new TransformGroup(zDisp);
			
			Shape3D xSphere = new Sphere(0.05f, xApp).getShape();
			Shape3D ySphere = new Sphere(0.05f, yApp).getShape();
			Shape3D zSphere = new Sphere(0.05f, zApp).getShape();
			
			xtg.addChild(xSphere.cloneNode(true));
			ytg.addChild(ySphere.cloneNode(true));
			ztg.addChild(zSphere.cloneNode(true));
			
			locRoot.addChild(xtg);
			locRoot.addChild(ytg);
			locRoot.addChild(ztg);
		}
		
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_AXES;
	}
	
	public void addRepShapes() {
		if ((capabilities & SHOW_REP_SHAPES) != 0) return;
		BranchGroup locRoot = new BranchGroup();
		
		Appearance discShellApp = new Appearance();
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		
		discShellApp.setPolygonAttributes(pAtt);
		discShellApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.DISC_SHELL_COLOR), ColoringAttributes.SHADE_GOURAUD));
		discShellApp.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, CalcConst.DISC_SHELL_TRANSPARENCY));
		
		Appearance discShellWireApp = new Appearance();
		discShellWireApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.DISC_SHELL_WIRE_COLOR), ColoringAttributes.SHADE_GOURAUD));
		discShellWireApp.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, CalcConst.DISC_SHELL_TRANSPARENCY));
		
		Appearance discShellFlatApp = new Appearance();
		discShellFlatApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.DISC_SHELL_WIRE_COLOR), ColoringAttributes.SHADE_GOURAUD));
		
		final BigDecimal TWO = new BigDecimal("2.0");
		final boolean H_RECT = (GTYPE == GraphType.VOL_REV_DISC) ^ H_AXIS;
		BigDecimal width, start;
		if (H_RECT) {
			width = POLY.getMaxY().subtract(POLY.getMinY(), CalcConst.MC).divide(new BigDecimal(N_INTERVAL), CalcConst.MC);
			start = POLY.getMinY();
		}
		else {
			width = POLY.getMaxX().subtract(POLY.getMinX(), CalcConst.MC).divide(new BigDecimal(N_INTERVAL), CalcConst.MC);
			start = POLY.getMinX();
		}
		BigDecimal halfWidth = width.divide(TWO, CalcConst.MC);
		
		for (int q = 0; q < N_INTERVAL; q++) {
			BigDecimal wCent = start.add(halfWidth.add(width.multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC), CalcConst.MC);
			BigDecimal left, right, top, bottom;
			if (H_RECT) {
				BigDecimal[] length;
				length = POLY.getXBoundsAt(wCent);
				if (length == null) continue;
				left = length[0];
				right = length[1]; 
				bottom = wCent.subtract(halfWidth, CalcConst.MC);
				top = bottom.add(width, CalcConst.MC);
			}
			else {
				BigDecimal[] length;
				length = POLY.getYBoundsAt(wCent);
				if (length == null) continue;
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
			
			Shape3D discShell = SolidMaker.rotateShape(new PolygonBig(rectPts), AXIS, H_AXIS);
			Shape3D wire1 = SolidMaker.rotatePoint(rectPts[0], AXIS, H_AXIS);
			Shape3D wire2 = SolidMaker.rotatePoint(rectPts[1], AXIS, H_AXIS);
			Shape3D wire3 = SolidMaker.rotatePoint(rectPts[2], AXIS, H_AXIS);
			Shape3D wire4 = SolidMaker.rotatePoint(rectPts[3], AXIS, H_AXIS);
			Shape3D rect = new Shape3D(lsa, discShellFlatApp);
			
			discShell.setAppearance(discShellApp);
			wire1.setAppearance(discShellWireApp);
			wire2.setAppearance(discShellWireApp);
			wire3.setAppearance(discShellWireApp);
			wire4.setAppearance(discShellWireApp);
			
			locRoot.addChild(discShell);
			locRoot.addChild(wire1);
			locRoot.addChild(wire2);
			locRoot.addChild(wire3);
			locRoot.addChild(wire4);
			locRoot.addChild(rect);
		}
		
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_REP_SHAPES;
	}
	
	public void addDefaults() {
		addAxes();
		addSolid();
		addWireframes();
		addFlatPoly();
	}
	
	protected void addEquation(int eq) {
		//Do nothing, equations aren't present in 3D graph.
		//Maybe later...
		return;
	}
}