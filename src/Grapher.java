import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Raster;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

public class Grapher extends JPanel {
	private static final long serialVersionUID = -8262263806408076517L;
	
	private PolygonBig POLY = null;
	private Equation[] EQS = null;
	private BigDecimal AXIS = null;
	private BigDecimal HEIGHT = null;
	private boolean H_AXIS = false;
	private int N_INTERVAL = 0;
	private GraphType GTYPE = null;
	
	private final double TILT3D_X = Math.PI / -8.0;
	private final double TILT3D_Y = Math.PI / 4.0;
	
	private BranchGroup root;
	private OrbitBehavior orbit;
	private SimpleUniverse universe;
	
	private int capabilities = 0;
	private static final int SHOW_AXES = 1;
	private static final int[] SHOW_EQ = new int[] {2, 4, 8, 16};
	private static final int SHOW_ALL_EQ = SHOW_EQ[0] | SHOW_EQ[1] | SHOW_EQ[2] | SHOW_EQ[3];
	private static final int SHOW_FLAT_POLY = 32;
	private static final int SHOW_SOLID = 64;
	private static final int SHOW_REP_SHAPES = 128;
	private static final int SHOW_WIREFRAMES = 256;
	
	/**
	 * 
	 */
	public Grapher() {
		super();
		int maxDim = Math.max(CalcConst.SCREEN_HEIGHT, CalcConst.SCREEN_WIDTH);
		setPreferredSize(new Dimension((int) (maxDim / 2.0), (int) (maxDim / 2.0)));
		setMinimumSize(new Dimension(CalcConst.GRAPH_MIN_WIDTH, CalcConst.GRAPH_MIN_HEIGHT));
		
		setLayout(new BorderLayout());
		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();

		Canvas3D c3d = new Canvas3D(gc);
		c3d.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if (Character.toLowerCase(e.getKeyChar()) == 'r') resetView();
			}
			public void keyReleased(KeyEvent e) { }
			public void keyPressed(KeyEvent e) { }
		});
		add(BorderLayout.CENTER, c3d);

		root = new BranchGroup();
		root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		root.setCapability(BranchGroup.ALLOW_DETACH);
		root.compile();
		
		universe = new SimpleUniverse(c3d);
		ViewingPlatform view = universe.getViewingPlatform();
		view.setNominalViewingTransform();
		universe.addBranchGraph(root);

		BoundingBox bounds = new BoundingBox(new Point3d(CalcConst.MIN_COORD, CalcConst.MIN_COORD, CalcConst.MIN_COORD), 
				new Point3d(CalcConst.MAX_COORD, CalcConst.MAX_COORD, CalcConst.MAX_COORD));
			
		orbit = new OrbitBehavior(c3d);
		orbit.setReverseRotate(true);
		orbit.setReverseTranslate(true);
		orbit.setSchedulingBounds(bounds);
		view.setViewPlatformBehavior(orbit);
	}
	
	public void setupAreaGraph(PolygonBig poly, Equation[] equs, 
			BigDecimal axis, boolean hAxis, int nInt, GraphType type) {
		POLY = poly;
		EQS = equs;
		AXIS = axis;
		H_AXIS = hAxis;
		N_INTERVAL = nInt;
		GTYPE = type;
		
		double zFact, zoomFit = 2.0;
		if (AXIS != null) {
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
		
		orbit.setRotateEnable(false);
		orbit.setHomeTransform(pCenter);
		
		capabilities = 0;
		root.removeAllChildren();
		resetView();
	}
	
	public void setupEquGraph(Equation[] equs, BigDecimal axis, boolean hAxis) {
		POLY = null;
		EQS = equs;
		AXIS = axis;
		H_AXIS = hAxis;
		N_INTERVAL = 0;
		GTYPE = GraphType.FLAT_NO_POLY;
		
		Transform3D pCenter = new Transform3D();
		pCenter.setTranslation(new Vector3d(0.0, 0.0, 30.0));

		orbit.setRotateEnable(false);
		orbit.setHomeTransform(pCenter);
		
		capabilities = 0;
		root.removeAllChildren();
		resetView();
	}
	
	public void setupVRevGraph(PolygonBig poly, Equation[] equs, 
			BigDecimal axis, boolean hAxis, int nInt, boolean disc) {
		POLY = poly;
		EQS = equs;
		AXIS = axis;
		H_AXIS = hAxis;
		N_INTERVAL = nInt;
		GTYPE = (disc) ? GraphType.VOL_REV_DISC : GraphType.VOL_REV_SHELL;
		
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

		orbit.setRotateEnable(true);
		orbit.setHomeTransform(pCenter);
		
		capabilities = 0;
		root.removeAllChildren();
		resetView();
	}
	
	public void setupVSectGraph(PolygonBig poly, Equation[] equs, BigDecimal height,
			boolean sectPerpX, int nInt, GraphType type) {
		POLY = poly;
		EQS = equs;
		AXIS = null;
		HEIGHT = height;
		H_AXIS = sectPerpX;
		N_INTERVAL = nInt;
		GTYPE = type;
		
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

		orbit.setRotateEnable(true);
		orbit.setHomeTransform(pCenter);
		
		capabilities = 0;
		root.removeAllChildren();
		resetView();
	}
	
	/**
	 * 
	 */
	public void resetView() {
		orbit.goHome();
	}
	
	public BufferedImage captureImg() {
		BufferedImage buf = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		ImageComponent2D img2d = new ImageComponent2D(ImageComponent.FORMAT_RGB, buf);
		
		Raster ras = new Raster(new Point3f(-1.0f, -1.0f, -1.0f), 
				Raster.RASTER_COLOR, 0, 0, getWidth(), getHeight(), img2d, null);
		universe.getCanvas().getGraphicsContext3D().readRaster(ras);
		
		return ras.getImage().getImage();
	}
	
	/**
	 * @param mask
	 */
	public void addCapabilities(int mask) {
		if ((mask & SHOW_AXES) != 0) addAxes();
		if ((mask & SHOW_EQ[0]) != 0) addEquation(0);
		if ((mask & SHOW_EQ[1]) != 0) addEquation(1);
		if ((mask & SHOW_EQ[2]) != 0) addEquation(2);
		if ((mask & SHOW_EQ[3]) != 0) addEquation(3);
		if ((mask & SHOW_SOLID) != 0) addSolid();
		if ((mask & SHOW_WIREFRAMES) != 0) addWireframes();
		if ((mask & SHOW_FLAT_POLY) != 0) addFlatPoly();
		if ((mask & SHOW_REP_SHAPES) != 0) addRepShapes();
	}
	
	/**
	 * 
	 */
	public void addDefaults() {
		switch (GTYPE) {
		case FLAT_CROSS_SECT:
		case FLAT_NO_POLY:
		case FLAT_WITH_POLY_DISC:
		case FLAT_WITH_POLY_SHELL:
			addCapabilities(SHOW_FLAT_POLY | SHOW_AXES | SHOW_ALL_EQ | SHOW_REP_SHAPES);
			break;
		case VOL_REV_DISC:
		case VOL_REV_SHELL:
			addCapabilities(SHOW_FLAT_POLY | SHOW_AXES | SHOW_SOLID | SHOW_WIREFRAMES);
			break;
		case VOL_SECT_CIRCLE:
		case VOL_SECT_EQUI_TRIANGLE:
		case VOL_SECT_ISO_TRIANGLE:
		case VOL_SECT_SQUARE:
		case VOL_SECT_SEMICIRCLE:
		case VOL_SECT_RECTANGLE:
			addCapabilities(SHOW_FLAT_POLY | SHOW_AXES | SHOW_SOLID | SHOW_WIREFRAMES);
		default:
			return; // uninitialized graph...
		}
	}
	
	/**
	 * 
	 */
	public void addAllFeatures() {
		addAxes();
		addEquations();
		addSolid();
		addWireframes();
		addFlatPoly();
		addRepShapes();
	}
	
	/**
	 * 
	 */
	public void addEquations() {
		for (int q = 0; q < EQS.length; q++) {
			addEquation(q);
		}
	}
	
	/**
	 * 
	 */
	public void toggleAxes() {
		if ((capabilities & SHOW_AXES) == 0) {
			addAxes();
			return;
		}
		root.removeAllChildren();
		int newCaps = capabilities & ~SHOW_AXES;
		capabilities = 0;
		addCapabilities(newCaps);
	}
	
	/**
	 * 
	 */
	public void toggleSolid() {
		if ((capabilities & SHOW_SOLID) == 0) {
			addSolid();
			return;
		}
		root.removeAllChildren();
		int newCaps = capabilities & ~SHOW_SOLID;
		capabilities = 0;
		addCapabilities(newCaps);
	}
	
	/**
	 * 
	 */
	public void toggleWireframes() {
		if ((capabilities & SHOW_WIREFRAMES) == 0) {
			addWireframes();
			return;
		}
		root.removeAllChildren();
		int newCaps = capabilities & ~SHOW_WIREFRAMES;
		capabilities = 0;
		addCapabilities(newCaps);
	}
	
	/**
	 * 
	 */
	public void toggleFlatPoly() {
		if ((capabilities & SHOW_FLAT_POLY) == 0) {
			addFlatPoly();
			return;
		}
		root.removeAllChildren();
		int newCaps = capabilities & ~SHOW_FLAT_POLY;
		capabilities = 0;
		addCapabilities(newCaps);
	}
	
	/**
	 * 
	 */
	public void toggleRepShapes() {
		if ((capabilities & SHOW_REP_SHAPES) == 0) {
			addRepShapes();
			return;
		}
		root.removeAllChildren();
		int newCaps = capabilities & ~SHOW_REP_SHAPES;
		capabilities = 0;
		addCapabilities(newCaps);
	}
	
	/**
	 * 
	 */
	public void toggleEquations() {
		for (int q = 0; q < EQS.length; q++) {
			toggleEquation(q);
		}
	}

	/**
	 * @param index
	 */
	private void toggleEquation(int index) {
		if ((capabilities & SHOW_EQ[index]) == 0) {
			addEquation(index);
			return;
		}
		root.removeAllChildren();
		int newCaps = capabilities & ~SHOW_EQ[index];
		capabilities = 0;
		addCapabilities(newCaps);
	}
	
	/**
	 * @return
	 */
	public boolean isShowEqus() {
		for (int q = 0; q < EQS.length; q++) {
			if ((capabilities & SHOW_EQ[q]) == 0) return false;
		}
		return true;
	}
	
	/**
	 * @return
	 */
	public boolean isShowAxes() {
		return ((capabilities & SHOW_AXES) != 0);
	}
	
	/**
	 * @return
	 */
	public boolean isShowSolid() {
		return ((capabilities & SHOW_SOLID) != 0);
	}
	
	/**
	 * @return
	 */
	public boolean isShowWireframes() {
		return ((capabilities & SHOW_WIREFRAMES) != 0);
	}
	
	/**
	 * @return
	 */
	public boolean isShowFlatPoly() {
		return ((capabilities & SHOW_FLAT_POLY) != 0);
	}
	
	public boolean isShowRepShapes() {
		return ((capabilities & SHOW_REP_SHAPES) != 0);
	}
	
	public void addAxes() {
		if ((capabilities & SHOW_AXES) != 0) return;
		
		BranchGroup locRoot = new BranchGroup();
		
		if (AXIS != null || GTYPE == GraphType.VOL_REV_DISC || GTYPE == GraphType.VOL_REV_SHELL) {
			Appearance rApp = new Appearance();
			rApp.setColoringAttributes(new ColoringAttributes(
					new Color3f(CalcConst.R_AXIS), ColoringAttributes.SHADE_GOURAUD));
			
			Point3d[] rPts = null;
			if (H_AXIS) {
				rPts = new Point3d[] { new Point3d(CalcConst.MIN_COORD, AXIS.doubleValue(), 0.0), 
						new Point3d(CalcConst.MAX_COORD, AXIS.doubleValue(), 0.0) };
			}
			else {
				rPts = new Point3d[] { new Point3d(AXIS.doubleValue(), CalcConst.MIN_COORD, 0.0), 
						new Point3d(AXIS.doubleValue(), CalcConst.MAX_COORD, 0.0) };
			}
			
			LineStripArray rLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
			rLSA.setCoordinates(0, rPts);

			Shape3D rAxis = new Shape3D(rLSA, rApp);
			locRoot.addChild(rAxis);
		}
		
		if (GTYPE != GraphType.FLAT_CROSS_SECT && GTYPE != GraphType.FLAT_NO_POLY &&
				GTYPE != GraphType.FLAT_WITH_POLY_DISC && GTYPE != GraphType.FLAT_WITH_POLY_SHELL) {
			Appearance zApp = new Appearance();
			zApp.setColoringAttributes(new ColoringAttributes(
					new Color3f(CalcConst.Z_AXIS), ColoringAttributes.SHADE_GOURAUD));
			
			Point3d[] zPts = new Point3d[] { new Point3d(0.0, 0.0, CalcConst.MIN_COORD), 
					new Point3d(0.0, 0.0, CalcConst.MAX_COORD) };
			
			LineStripArray zLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
			zLSA.setCoordinates(0, zPts);

			Shape3D zAxis = new Shape3D(zLSA, zApp);
			locRoot.addChild(zAxis);
			
			Shape3D zSphere = new Sphere(0.05f, zApp).getShape();
			for (double q = Math.floor(CalcConst.MIN_COORD); q <= Math.ceil(CalcConst.MAX_COORD); q++) {
				Transform3D zDisp = new Transform3D();
				zDisp.setTranslation(new Vector3d(0.0, 0.0, q));
				
				TransformGroup ztg = new TransformGroup(zDisp);
				ztg.addChild(zSphere.cloneNode(true));

				locRoot.addChild(ztg);
			}
		}
		
		Appearance xApp = new Appearance();
		Appearance yApp = new Appearance();
		
		xApp.setColoringAttributes(new ColoringAttributes(
				new Color3f(CalcConst.X_AXIS), ColoringAttributes.SHADE_GOURAUD));
		yApp.setColoringAttributes(new ColoringAttributes(
				new Color3f(CalcConst.Y_AXIS), ColoringAttributes.SHADE_GOURAUD));
		
		Point3d[] xPts = new Point3d[] { new Point3d(CalcConst.MIN_COORD, 0.0, 0.0), 
				new Point3d(CalcConst.MAX_COORD, 0.0, 0.0) };
		Point3d[] yPts = new Point3d[] { new Point3d(0.0, CalcConst.MIN_COORD, 0.0), 
				new Point3d(0.0, CalcConst.MAX_COORD, 0.0) };
		
		LineStripArray xLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		xLSA.setCoordinates(0, xPts);
		LineStripArray yLSA = new LineStripArray(2, LineStripArray.COORDINATES, new int[]{2});
		yLSA.setCoordinates(0, yPts);
		
		Shape3D xAxis = new Shape3D(xLSA, xApp);
		Shape3D yAxis = new Shape3D(yLSA, yApp);
		
		locRoot.addChild(xAxis);
		locRoot.addChild(yAxis);
		
		Shape3D xSphere = new Sphere(0.05f, xApp).getShape();
		Shape3D ySphere = new Sphere(0.05f, yApp).getShape();
		for (double q = Math.floor(CalcConst.MIN_COORD); q <= Math.ceil(CalcConst.MAX_COORD); q++) {
			Transform3D xDisp = new Transform3D();
			Transform3D yDisp = new Transform3D();

			xDisp.setTranslation(new Vector3d(q, 0.0, 0.0));
			yDisp.setTranslation(new Vector3d(0.0, q, 0.0));

			TransformGroup xtg = new TransformGroup(xDisp);
			TransformGroup ytg = new TransformGroup(yDisp);

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
	
	public void addEquation(int index) {
		if (EQS == null || index >= EQS.length || EQS[index] == null) return;
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
		if (GTYPE == GraphType.FLAT_NO_POLY || POLY == null) return;
		if ((capabilities & SHOW_FLAT_POLY) != 0) return;
		
		Appearance app = new Appearance();
		Point2DBig[] polyPts = POLY.getPoints();
		Point3d[] pts = new Point3d[polyPts.length + 1];
		
		for (int q = 0; q < polyPts.length; q++) {
			pts[q] = new Point3d(polyPts[q].getX().doubleValue(), polyPts[q].getY().doubleValue(), 0.0);
		}
		pts[polyPts.length] = new Point3d(polyPts[0].getX().doubleValue(), polyPts[0].getY().doubleValue(), 0.0);
		
		Shape3D fPoly;
		switch (GTYPE) {
		case FLAT_CROSS_SECT:
		case FLAT_WITH_POLY_DISC:
		case FLAT_WITH_POLY_SHELL:
			PolygonAttributes pAtt = new PolygonAttributes();
			pAtt.setCullFace(PolygonAttributes.CULL_NONE);

			app.setPolygonAttributes(pAtt);
			app.setColoringAttributes(new ColoringAttributes(
					new Color3f(CalcConst.POLY_COLOR), ColoringAttributes.SHADE_GOURAUD));
			app.setTransparencyAttributes(new TransparencyAttributes(
					TransparencyAttributes.NICEST, CalcConst.POLY_TRANSPARENCY));

			GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
			gi.setCoordinates(pts);
			gi.setStripCounts(new int[] {pts.length});

			fPoly = new Shape3D(gi.getGeometryArray(), app);
			break;
		default:
			app.setColoringAttributes(new ColoringAttributes(
					new Color3f(CalcConst.SOLID_WIRE_COLOR), ColoringAttributes.SHADE_GOURAUD));
			
			LineStripArray lsa = new LineStripArray(pts.length, 
					LineStripArray.COORDINATES, new int[] { pts.length });
			lsa.setCoordinates(0, pts);
			
			fPoly = new Shape3D(lsa, app);
		}
		
		BranchGroup locRoot = new BranchGroup();
		locRoot.addChild(fPoly);
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_FLAT_POLY;
	}
	
	public void addSolid() {
		if (POLY == null) return;
		if ((capabilities & SHOW_SOLID) != 0) return;
		
		Shape3D poly;
		switch (GTYPE) {
		case VOL_SECT_CIRCLE:
			poly = SolidMaker.extrudeShape(POLY, H_AXIS, null, CrossSectionType.CIRCLE);
			break;
		case VOL_SECT_EQUI_TRIANGLE:
			poly = SolidMaker.extrudeShape(POLY, H_AXIS, null, CrossSectionType.EQUI_TRIANGLE);
			break;
		case VOL_SECT_ISO_TRIANGLE:
			if (HEIGHT == null) poly = SolidMaker.extrudeShape(POLY, H_AXIS, null, CrossSectionType.EQUI_TRIANGLE);
			else poly = SolidMaker.extrudeShape(POLY, H_AXIS, HEIGHT, CrossSectionType.ISO_TRIANGLE);
			break;
		case VOL_SECT_RECTANGLE:
			if (HEIGHT == null) poly = SolidMaker.extrudeShape(POLY, H_AXIS, null, CrossSectionType.SQUARE);
			else poly = SolidMaker.extrudeShape(POLY, H_AXIS, HEIGHT, CrossSectionType.RECTANGLE);
			break;
		case VOL_SECT_SEMICIRCLE:
			poly = SolidMaker.extrudeShape(POLY, H_AXIS, null, CrossSectionType.SEMICIRCLE);
			break;
		case VOL_SECT_SQUARE:
			poly = SolidMaker.extrudeShape(POLY, H_AXIS, null, CrossSectionType.SQUARE);
			break;
		case VOL_REV_DISC:
		case VOL_REV_SHELL:
			if (AXIS == null) return;
			poly = SolidMaker.rotateShape(POLY, AXIS, H_AXIS);
			break;
		default:
			return;
		}
		
		Appearance app = new Appearance();
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		
		app.setPolygonAttributes(pAtt);
		app.setColoringAttributes(new ColoringAttributes(
				new Color3f(CalcConst.SOLID_COLOR), ColoringAttributes.SHADE_GOURAUD));
		app.setTransparencyAttributes(new TransparencyAttributes(
				TransparencyAttributes.NICEST, CalcConst.SOLID_TRANSPARENCY));
		
		poly.setAppearance(app);
		
		BranchGroup locRoot = new BranchGroup();
		locRoot.addChild(poly);
		
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_SOLID;
	}
	
	public void addWireframes() {
		if (POLY == null) return;
		if ((capabilities & SHOW_WIREFRAMES) != 0) return;
		Appearance app = new Appearance();
		
		app.setColoringAttributes(new ColoringAttributes(
				new Color3f(CalcConst.SOLID_WIRE_COLOR), ColoringAttributes.SHADE_GOURAUD));
		app.setTransparencyAttributes(new TransparencyAttributes(
				TransparencyAttributes.NICEST, CalcConst.SOLID_TRANSPARENCY));
		
		BranchGroup locRoot = new BranchGroup();
		Shape3D wireShape = null;
		switch (GTYPE) {
		case VOL_SECT_CIRCLE:
			wireShape = SolidMaker.extrudeWires(POLY, H_AXIS, null, N_INTERVAL, CrossSectionType.CIRCLE);
			break;
		case VOL_SECT_EQUI_TRIANGLE:
			wireShape = SolidMaker.extrudeWires(POLY, H_AXIS, null, N_INTERVAL, CrossSectionType.EQUI_TRIANGLE);
			break;
		case VOL_SECT_ISO_TRIANGLE:
			if (HEIGHT == null) wireShape = SolidMaker.extrudeWires(POLY, H_AXIS, null, N_INTERVAL, CrossSectionType.EQUI_TRIANGLE);
			else wireShape = SolidMaker.extrudeWires(POLY, H_AXIS, HEIGHT, N_INTERVAL, CrossSectionType.ISO_TRIANGLE);
			break;
		case VOL_SECT_RECTANGLE:
			if (HEIGHT == null) wireShape = SolidMaker.extrudeWires(POLY, H_AXIS, null, N_INTERVAL, CrossSectionType.SQUARE);
			else wireShape = SolidMaker.extrudeWires(POLY, H_AXIS, HEIGHT, N_INTERVAL, CrossSectionType.RECTANGLE);
			break;
		case VOL_SECT_SEMICIRCLE:
			wireShape = SolidMaker.extrudeWires(POLY, H_AXIS, null, N_INTERVAL, CrossSectionType.SEMICIRCLE);
			break;
		case VOL_SECT_SQUARE:
			wireShape = SolidMaker.extrudeWires(POLY, H_AXIS, null, N_INTERVAL, CrossSectionType.SQUARE);
			break;
		case VOL_REV_DISC:
		case VOL_REV_SHELL:
			if (AXIS == null) return;
			Point2DBig[] pts = POLY.getPoints();
			for (int q = 0; q < pts.length; q += 4) {
				Shape3D wireLine = SolidMaker.rotatePoint(pts[q], AXIS, H_AXIS);
				wireLine.setAppearance(app);
				locRoot.addChild(wireLine);
			}
			break;
		default:
			return;
		}
		
		if (wireShape != null) locRoot.addChild(wireShape);
		
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_WIREFRAMES;
	}
	
	public void addRepShapes() {
		if (GTYPE == GraphType.FLAT_NO_POLY || POLY == null) return;
		if ((capabilities & SHOW_REP_SHAPES) != 0) return;
		
		BranchGroup locRoot = new BranchGroup();
		final BigDecimal TWO = new BigDecimal("2.0");
		
		if (GTYPE == GraphType.FLAT_CROSS_SECT || 
				GTYPE == GraphType.FLAT_WITH_POLY_DISC || 
				GTYPE == GraphType.FLAT_WITH_POLY_SHELL) {
			//TODO: add all rectangles instead of just one...
			final boolean H_RECT = (GTYPE == GraphType.FLAT_WITH_POLY_DISC) ^ H_AXIS;
			BigDecimal width, wCent;
			if (H_RECT) {
				width = POLY.getMaxY().subtract(POLY.getMinY(), CalcConst.MC).divide(new BigDecimal(N_INTERVAL), CalcConst.MC);
				wCent = POLY.getMaxY().add(POLY.getMinY(), CalcConst.MC).divide(TWO, CalcConst.MC);
			}
			else {
				width = POLY.getMaxX().subtract(POLY.getMinX(), CalcConst.MC).divide(new BigDecimal(N_INTERVAL), CalcConst.MC);
				wCent = POLY.getMaxX().add(POLY.getMinX(), CalcConst.MC).divide(TWO, CalcConst.MC);
			}
			BigDecimal halfWidth = width.divide(TWO, CalcConst.MC);

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
					new Point3d(rectPts[0].getX().doubleValue(), rectPts[0].getY().doubleValue(), 0.0) };
			
			LineStripArray lsa = new LineStripArray(5, LineStripArray.COORDINATES, new int[] { 5 });
			lsa.setCoordinates(0, rect3Pts);
			Shape3D rect = new Shape3D(lsa);

			Appearance rectApp = new Appearance();
			rectApp.setColoringAttributes(new ColoringAttributes(new Color3f(CalcConst.RECT_COLOR), ColoringAttributes.SHADE_GOURAUD));
			rect.setAppearance(rectApp);

			locRoot.addChild(rect);
		}
		else if (GTYPE == GraphType.VOL_REV_DISC || GTYPE == GraphType.VOL_REV_SHELL) {
			if (AXIS == null) return;
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
						new Point3d(rectPts[0].getX().doubleValue(), rectPts[0].getY().doubleValue(), 0.0) };
				LineStripArray lsa = new LineStripArray(5, LineStripArray.COORDINATES, new int[] { 5 });
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
		}
		else {
			// TODO Add representative shapes to x-sect graph
		}
		
		locRoot.setCapability(BranchGroup.ALLOW_DETACH);
		locRoot.compile();
		root.addChild(locRoot);
		capabilities = capabilities | SHOW_REP_SHAPES;
	}
}

enum GraphType {
	FLAT_NO_POLY, FLAT_WITH_POLY_DISC, FLAT_WITH_POLY_SHELL, FLAT_CROSS_SECT, 
	VOL_REV_SHELL, VOL_REV_DISC,
	VOL_SECT_CIRCLE, VOL_SECT_SEMICIRCLE,
	VOL_SECT_SQUARE, VOL_SECT_RECTANGLE,
	VOL_SECT_EQUI_TRIANGLE, VOL_SECT_ISO_TRIANGLE
}