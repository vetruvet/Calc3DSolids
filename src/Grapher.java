import java.awt.*;
import java.awt.event.*;
import java.math.*;
import javax.swing.JPanel;
import javax.vecmath.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.vp.*;

public abstract class Grapher extends JPanel {
	private static final long serialVersionUID = -8262263806408076517L;
	
	protected final PolygonBig POLY;
	protected final Equation[] EQS;
	protected final BigDecimal AXIS;
	protected final boolean H_AXIS;
	protected final int N_INTERVAL;
	protected final GraphType GTYPE;
	
	protected final double TILT3D_X = Math.PI / -8.0;
	protected final double TILT3D_Y = Math.PI / 4.0;
	
	protected final BranchGroup root;
	protected final OrbitBehavior orbit;
	
	protected int capabilities = 0;
	protected static final int SHOW_AXES = 1;
	protected static final int[] SHOW_EQ = new int[] {2, 4, 8, 16};
	protected static final int SHOW_ALL_EQ = SHOW_EQ[0] | SHOW_EQ[1] | SHOW_EQ[2] | SHOW_EQ[3];
	protected static final int SHOW_FLAT_POLY = 32;
	protected static final int SHOW_SOLID = 64;
	protected static final int SHOW_REP_SHAPES = 128;
	protected static final int SHOW_WIREFRAMES = 256;
	protected static int DEFAULT_CAP = 0;
	
	/**
	 * @param poly
	 * @param equs
	 * @param axis
	 * @param hAxis
	 * @param nInt
	 * @param type
	 */
	public Grapher(PolygonBig poly, Equation[] equs,
			BigDecimal axis, boolean hAxis,
			int nInt, GraphType type) {
		super();
		int maxDim = Math.max(CalcConst.SCREEN_HEIGHT, CalcConst.SCREEN_WIDTH);
		setPreferredSize(new Dimension((int) (maxDim / 2.0), (int) (maxDim / 2.0)));
		setMinimumSize(new Dimension(CalcConst.GRAPH_MIN_WIDTH, CalcConst.GRAPH_MIN_HEIGHT));
		
		POLY = poly;
		EQS = equs;
		AXIS = axis;
		H_AXIS = hAxis;
		N_INTERVAL = nInt;
		GTYPE = type;
		
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
		root.compile();
		
		SimpleUniverse univ = new SimpleUniverse(c3d);
		ViewingPlatform view = univ.getViewingPlatform();
		view.setNominalViewingTransform();
		univ.addBranchGraph(root);

		BoundingBox bounds = new BoundingBox(new Point3d(CalcConst.MIN_COORD, CalcConst.MIN_COORD, CalcConst.MIN_COORD), 
				new Point3d(CalcConst.MAX_COORD, CalcConst.MAX_COORD, CalcConst.MAX_COORD));
			
		orbit = new OrbitBehavior(c3d);
		orbit.setSchedulingBounds(bounds);
		view.setViewPlatformBehavior(orbit);
	}
	
	public void finalize() {
		System.out.println("Goodby grapher!");
	}
	
	/**
	 * 
	 */
	public void resetView() {
		orbit.goHome();
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
	
	public abstract void addDefaults();
	public abstract void addAxes();
	protected abstract void addEquation(int eq);
	public abstract void addFlatPoly();
	public abstract void addSolid();
	public abstract void addWireframes();
	public abstract void addRepShapes();
}

enum GraphType {
	FLAT_NO_POLY, FLAT_WITH_POLY_DISC, FLAT_WITH_POLY_SHELL, FLAT_CROSS_SECT, 
	VOL_REV_SHELL, VOL_REV_DISC,
	VOL_SECT_CIRCLE, VOL_SECT_SEMICIRCLE,
	VOL_SECT_SQUARE, VOL_SECT_RECTANGLE,
	VOL_SECT_EQUI_TRIANGLE, VOL_SECT_ISO_TRIANGLE
}