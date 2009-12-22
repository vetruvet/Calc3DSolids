import java.math.*;
import javax.vecmath.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.*;

public class VolumeSectGrapher extends Grapher {
	private static final long serialVersionUID = 1630089335941003370L;

	public static final boolean DEBUG = false;
	
	private final boolean SECT_PERP_X;
	private BigDecimal HEIGHT;
	private static final CrossSectionType DEF_SHAPE = CrossSectionType.SQUARE;
	
	public VolumeSectGrapher(PolygonBig poly, Equation[] equs,
			boolean sectPerpX, int nInt, GraphType type) {
		super(poly, equs, BigDecimal.ZERO, sectPerpX, nInt, type);
		SECT_PERP_X = sectPerpX;
		
		Transform3D pCenter = new Transform3D();
		double xTrans, yTrans, zTrans, maxD, zoomFit = 3.0;
		xTrans = (POLY.getMaxX().doubleValue() + POLY.getMinX().doubleValue()) / 2.0;
		yTrans = (POLY.getMaxY().doubleValue() + POLY.getMinY().doubleValue()) / 2.0;
		zTrans = 0.0;
		
		maxD = Math.max(POLY.getMaxX().doubleValue() - xTrans, 
				POLY.getMaxY().doubleValue() - yTrans);
		
		xTrans += zoomFit * maxD * Math.cos(TILT3D_X);
		yTrans += -1 * zoomFit * maxD * Math.sin(TILT3D_X);
		zTrans += zoomFit * maxD * Math.sin(TILT3D_Y);
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
	
	public void setConstantHeight(BigDecimal height) {
		HEIGHT = height;
	}

	public void addDefaults() {
		addSolid();
		addWireframes();
		addFlatPoly();
		addAxes();
	}

	public void addAxes() {
		if ((capabilities & SHOW_AXES) != 0) return;
		Appearance xApp = new Appearance();
		Appearance yApp = new Appearance();
		Appearance zApp = new Appearance();
		
		xApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.X_AXIS), ColoringAttributes.SHADE_GOURAUD));
		yApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.Y_AXIS), ColoringAttributes.SHADE_GOURAUD));
		zApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.Z_AXIS), ColoringAttributes.SHADE_GOURAUD));
		
		BranchGroup locRoot = new BranchGroup();
		
		Point3d[] xPts = new Point3d[]{new Point3d(CalcConst.MIN_COORD, 0.0, 0.0), new Point3d(CalcConst.MAX_COORD, 0.0, 0.0)};
		Point3d[] yPts = new Point3d[]{new Point3d(0.0, CalcConst.MIN_COORD, 0.0), new Point3d(0.0, CalcConst.MAX_COORD, 0.0)};
		Point3d[] zPts = new Point3d[]{new Point3d(0.0, 0.0, CalcConst.MIN_COORD), new Point3d(0.0, 0.0, CalcConst.MAX_COORD)};
		
		LineStripArray xLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		xLSA.setCoordinates(0, xPts);
		LineStripArray yLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		yLSA.setCoordinates(0, yPts);
		LineStripArray zLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		zLSA.setCoordinates(0, zPts);
		
		Shape3D xAxis = new Shape3D(xLSA, xApp);
		Shape3D yAxis = new Shape3D(yLSA, yApp);
		Shape3D zAxis = new Shape3D(zLSA, zApp);
		
		locRoot.addChild(xAxis);
		locRoot.addChild(yAxis);
		locRoot.addChild(zAxis);
		
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

	public void addRepShapes() {
		// TODO Auto-generated method stub
		
	}

	public void addSolid() {
		if ((capabilities & SHOW_SOLID) != 0) return;
		
		Shape3D ePoly;
		switch (GTYPE) {
		case VOL_SECT_CIRCLE:
			ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, null, CrossSectionType.CIRCLE);
			break;
		case VOL_SECT_EQUI_TRIANGLE:
			ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, null, CrossSectionType.EQUI_TRIANGLE);
			break;
		case VOL_SECT_ISO_TRIANGLE:
			if (HEIGHT == null) ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, null, CrossSectionType.EQUI_TRIANGLE);
			else ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, HEIGHT, CrossSectionType.ISO_TRIANGLE);
			break;
		case VOL_SECT_RECTANGLE:
			if (HEIGHT == null) ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, null, CrossSectionType.SQUARE);
			else ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, HEIGHT, CrossSectionType.RECTANGLE);
			break;
		case VOL_SECT_SEMICIRCLE:
			ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, null, CrossSectionType.SEMICIRCLE);
			break;
		case VOL_SECT_SQUARE:
			ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, null, CrossSectionType.SQUARE);
			break;
		default:
			ePoly = SolidMaker.extrudeShape(POLY, SECT_PERP_X, null, DEF_SHAPE);
		}
		
		Appearance app = new Appearance();
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		
		app.setPolygonAttributes(pAtt);
		app.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.SOLID_COLOR), ColoringAttributes.SHADE_GOURAUD));
		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, CalcConst.SOLID_TRANSPARENCY));
		
		ePoly.setAppearance(app);
		
		BranchGroup locRoot = new BranchGroup();
		locRoot.addChild(ePoly);
		
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
		
		Shape3D wireShape;
		switch (GTYPE) {
		case VOL_SECT_CIRCLE:
			wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, null, N_INTERVAL, CrossSectionType.CIRCLE);
			break;
		case VOL_SECT_EQUI_TRIANGLE:
			wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, null, N_INTERVAL, CrossSectionType.EQUI_TRIANGLE);
			break;
		case VOL_SECT_ISO_TRIANGLE:
			if (HEIGHT == null) wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, null, N_INTERVAL, CrossSectionType.EQUI_TRIANGLE);
			else wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, HEIGHT, N_INTERVAL, CrossSectionType.ISO_TRIANGLE);
			break;
		case VOL_SECT_RECTANGLE:
			if (HEIGHT == null) wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, null, N_INTERVAL, CrossSectionType.SQUARE);
			else wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, HEIGHT, N_INTERVAL, CrossSectionType.RECTANGLE);
			break;
		case VOL_SECT_SEMICIRCLE:
			wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, null, N_INTERVAL, CrossSectionType.SEMICIRCLE);
			break;
		case VOL_SECT_SQUARE:
			wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, null, N_INTERVAL, CrossSectionType.SQUARE);
			break;
		default:
			wireShape = SolidMaker.extrudeWires(POLY, SECT_PERP_X, null, N_INTERVAL, DEF_SHAPE);
		}
		
		BranchGroup locRoot = new BranchGroup();
		locRoot.addChild(wireShape);
		
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_WIREFRAMES;
	}
	
	protected void addEquation(int eq) {
		//Do nothing, equations aren't present in 3D graph
		return;	
	}
}
