import java.math.BigDecimal;

public class PolygonBuilder {
	public static final boolean DEBUG = false;
	
	private final int NEQS;
	private final Equation EQ1, EQ2, EQ3, EQ4;
	private PolygonBig finalPoly = new PolygonBig();
	private static int quality = CalcConst.PRESI;
	
	public PolygonBuilder(Equation eq1, Equation eq2) {
		NEQS = 2;
		EQ1 = eq1;
		EQ2 = eq2;
		EQ3 = EQ4 = null;
	}
	
	public PolygonBuilder(Equation eq1, Equation eq2, Equation eq3) {
		NEQS = 3;
		EQ1 = eq1;
		EQ2 = eq2;
		EQ3 = eq3;
		EQ4 = null;
	}
	
	public PolygonBuilder(Equation eq1, Equation eq2, Equation eq3, Equation eq4) {
		NEQS = 4;
		EQ1 = eq1;
		EQ2 = eq2;
		EQ3 = eq3;
		EQ4 = eq4;
	}
	
	public PolygonBuilder(Equation...equations) {
		NEQS = equations.length;
		if ((NEQS != 2) && (NEQS != 3) && (NEQS != 4)) throw new IllegalArgumentException("Invalid Number of Equations for PolygonBuilder");
		EQ1 = equations[0];
		EQ2 = equations[1];
		if (NEQS > 2) EQ3 = equations[2];
		else EQ3 = null;
		if (NEQS > 3) EQ4 = equations[3];
		else EQ4 = null;
	}
	
	public void build() throws PolygonBuildException {
		switch (NEQS) {
		case 2:
			if (!EQ1.isEqParsed() || !EQ2.isEqParsed()) throw new PolygonBuildException("Equations not parsed");
			build2();
			break;
		case 3:
			if (!EQ1.isEqParsed() || !EQ2.isEqParsed() || !EQ3.isEqParsed()) throw new PolygonBuildException("Equations not parsed");
			build3();
			break;
		case 4:
			if (!EQ1.isEqParsed() || !EQ2.isEqParsed() || !EQ3.isEqParsed() || !EQ4.isEqParsed()) throw new PolygonBuildException("Equations not parsed");
			build4();
			break;
		default:
		}
	}
	
	private void build2() throws PolygonBuildException {
		Point2DBig[] xSect = EQ1.intersectsWith(EQ2);
		
		if (xSect.length < 2) throw new PolygonBuildException("No region formed!");
		if (xSect.length > 2) throw new PolygonBuildException("More than 1 region formed!");
		
		BigDecimal eq1low, eq1high, eq2low, eq2high;
		if (EQ1.isEqInX()) {
			eq1low = xSect[0].getX();
			eq1high = xSect[1].getX();
		}
		else {
			eq1low = xSect[0].getY();
			eq1high = xSect[1].getY();
		}
		if (EQ2.isEqInX()) {
			eq2low = xSect[1].getX();
			eq2high = xSect[0].getX();
		}
		else {
			eq2low = xSect[1].getY();
			eq2high = xSect[0].getY();
		}
		PolygonBig poly = buildPolygon(eq1low, eq1high, eq2low, eq2high);
		if (!poly.isSimple()) throw new PolygonBuildException("Found region intersects itself.");
		finalPoly = poly;
	}
	
	private void build3() throws PolygonBuildException {
		Point2DBig[] xSect12 = EQ1.intersectsWith(EQ2);
		Point2DBig[] xSect13 = EQ1.intersectsWith(EQ3);
		Point2DBig[] xSect23 = EQ2.intersectsWith(EQ3);
		
		BigDecimal eq1low, eq1high, eq2low, eq2high, eq3low, eq3high;
		int nValidPoly = 0, v12 = 0, v13 = 0, v23 = 0;
		for (int q12 = 0; q12 < xSect12.length; q12++) {
			for (int q13 = 0; q13 < xSect13.length; q13++) {
				for (int q23 = 0; q23 < xSect23.length; q23++) {
					if (EQ1.isEqInX()) {
						eq1low = xSect12[q12].getX();
						eq1high = xSect13[q13].getX();
					}
					else {
						eq1low = xSect12[q12].getY();
						eq1high = xSect13[q13].getY();
					}
					if (EQ3.isEqInX()) {
						eq3low = xSect13[q13].getX();
						eq3high = xSect23[q23].getX();
					}
					else {
						eq3low = xSect13[q13].getY();
						eq3high = xSect23[q23].getY();
					}
					if (EQ2.isEqInX()) {
						eq2low = xSect23[q23].getX();
						eq2high = xSect12[q12].getX();
					}
					else {
						eq2low = xSect23[q23].getY();
						eq2high = xSect12[q12].getY();
					}
					PolygonBig poly = buildPolygon(eq1low, eq1high, eq2low, eq2high, eq3low, eq3high);
					poly.cleanUp();
					if (poly.getNPoints() < 3) continue;
					if (!poly.isSimple()) continue;
					if (poly.contains(EQ1) || poly.contains(EQ2) || poly.contains(EQ3)) continue;
					v12 = q12;
					v23 = q23;
					v13 = q13;
					nValidPoly++;
				}
			}
		}
		
		if (nValidPoly == 0) throw new PolygonBuildException("No valid region formed.");
		if (nValidPoly > 1) throw new PolygonBuildException("Multiple valid regions formed.");
		
		if (EQ1.isEqInX()) {
			eq1low = xSect12[v12].getX();
			eq1high = xSect13[v13].getX();
		}
		else {
			eq1low = xSect12[v12].getY();
			eq1high = xSect13[v13].getY();
		}
		if (EQ3.isEqInX()) {
			eq3low = xSect13[v13].getX();
			eq3high = xSect23[v23].getX();
		}
		else {
			eq3low = xSect13[v13].getY();
			eq3high = xSect23[v23].getY();
		}
		if (EQ2.isEqInX()) {
			eq2low = xSect23[v23].getX();
			eq2high = xSect12[v12].getX();
		}
		else {
			eq2low = xSect23[v23].getY();
			eq2high = xSect12[v12].getY();
		}
		finalPoly = buildPolygon(eq1low, eq1high, eq2low, eq2high, eq3low, eq3high);
		finalPoly.cleanUp();
	}
	
	private void build4() throws PolygonBuildException {
		Point2DBig[] xSect12 = EQ1.intersectsWith(EQ2);
		Point2DBig[] xSect13 = EQ1.intersectsWith(EQ3);
		Point2DBig[] xSect14 = EQ1.intersectsWith(EQ4);
		Point2DBig[] xSect23 = EQ2.intersectsWith(EQ3);
		Point2DBig[] xSect24 = EQ2.intersectsWith(EQ4);
		Point2DBig[] xSect34 = EQ3.intersectsWith(EQ4);
		
		Point2DBig[] xSectOn1 = new Point2DBig[xSect12.length + xSect13.length + xSect14.length];
		int[] x1Target = new int[xSectOn1.length];
		Point2DBig[] xSectOn2 = new Point2DBig[xSect12.length + xSect23.length + xSect24.length];
		int[] x2Target = new int[xSectOn2.length];
		Point2DBig[] xSectOn3 = new Point2DBig[xSect13.length + xSect23.length + xSect34.length];
		int[] x3Target = new int[xSectOn3.length];
		Point2DBig[] xSectOn4 = new Point2DBig[xSect14.length + xSect24.length + xSect34.length];
		int[] x4Target = new int[xSectOn4.length];
		
		for (int q = 0; q < xSect12.length; q++) {
			xSectOn1[q] = xSect12[q];
			x1Target[q] = 2;
			xSectOn2[q] = xSect12[q];
			x2Target[q] = 1;
		}
		for (int q = 0; q < xSect13.length; q++) {
			xSectOn1[xSect12.length + q] = xSect13[q];
			x1Target[xSect12.length + q] = 3;
			xSectOn3[q] = xSect13[q];
			x3Target[q] = 1;
		}
		for (int q = 0; q < xSect14.length; q++) {
			xSectOn1[xSect12.length + xSect13.length + q] = xSect14[q];
			x1Target[xSect12.length + xSect13.length + q] = 4;
			xSectOn4[q] = xSect14[q];
			x4Target[q] = 1;
		}
		for (int q = 0; q < xSect23.length; q++) {
			xSectOn2[xSect12.length + q] = xSect23[q];
			x2Target[xSect12.length + q] = 3;
			xSectOn3[xSect13.length + q] = xSect23[q];
			x3Target[xSect13.length + q] = 2;
		}
		for (int q = 0; q < xSect24.length; q++) {
			xSectOn2[xSect12.length + xSect23.length + q] = xSect24[q];
			x2Target[xSect12.length + xSect23.length + q] = 4;
			xSectOn4[xSect14.length + q] = xSect24[q];
			x4Target[xSect14.length + q] = 2;
		}
		for (int q = 0; q < xSect34.length; q++) {
			xSectOn3[xSect13.length + xSect23.length + q] = xSect34[q];
			x3Target[xSect13.length + xSect23.length + q] = 4;
			xSectOn4[xSect14.length + xSect24.length + q] = xSect34[q];
			x4Target[xSect14.length + xSect24.length + q] = 3;
		}
		
		boolean usedEq2, usedEq3, usedEq4;
		usedEq2 = usedEq3 = usedEq4 = false;
		int nValidPoly = 0;
		Point2DBig vEq1From, vEq1To, vEq2From, vEq2To, vEq3From, vEq3To, vEq4From, vEq4To;
		vEq1From = vEq1To = vEq2From = vEq2To = vEq3From = vEq3To = vEq4From = vEq4To = null;
		int[] vOrder = new int[4];
		for (int q = 0; q < xSectOn1.length; q++) {
			Point2DBig[] nextSect1;
			int[] nextTar1;
			switch (x1Target[q]) {
			case 2:
				if (usedEq2) continue;
				usedEq2 = true;
				nextSect1 = xSectOn2;
				nextTar1 = x2Target;
				break;
			case 3:
				if (usedEq3) continue;
				usedEq3 = true;
				nextSect1 = xSectOn3;
				nextTar1 = x3Target;
				break;
			case 4:
				if (usedEq4) continue;
				usedEq4 = true;
				nextSect1 = xSectOn4;
				nextTar1 = x4Target;
				break;
			default:
				continue;
			}
			
			for (int w = 0; w < nextSect1.length; w++) {
				Point2DBig[] nextSect2;
				int[] nextTar2;
				switch (nextTar1[w]) {
				case 2:
					if (usedEq2) continue;
					usedEq2 = true;
					nextSect2 = xSectOn2;
					nextTar2 = x2Target;
					break;
				case 3:
					if (usedEq3) continue;
					usedEq3 = true;
					nextSect2 = xSectOn3;
					nextTar2 = x3Target;
					break;
				case 4:
					if (usedEq4) continue;
					usedEq4 = true;
					nextSect2 = xSectOn4;
					nextTar2 = x4Target;
					break;
				default:
					continue;
				}
				
				for (int e = 0; e < nextSect2.length; e++) {
					if ((nextTar2[e] == nextTar1[w]) || (nextTar2[e] == 1)) continue;
					
					Point2DBig[] nextSect3;
					int[] nextTar3;
					switch (nextTar2[e]) {
					case 2:
						if (usedEq2) continue;
						usedEq2 = true;
						nextSect3 = xSectOn2;
						nextTar3 = x2Target;
						break;
					case 3:
						if (usedEq3) continue;
						usedEq3 = true;
						nextSect3 = xSectOn3;
						nextTar3 = x3Target;
						break;
					case 4:
						if (usedEq4) continue;
						usedEq4 = true;
						nextSect3 = xSectOn4;
						nextTar3 = x4Target;
						break;
					default:
						continue;
					}
					
					for (int r = 0; r < nextSect3.length; r++) {
						if (nextTar3[r] != 1) continue;
						if (!usedEq2 || !usedEq3 || !usedEq4) continue;
						
						int[] order = new int[] { 1, x1Target[q], nextTar1[w], nextTar2[e] };
						
						Point2DBig eq1from, eq1to, eq2from, eq2to, eq3from, eq3to, eq4from, eq4to;
						BigDecimal eq1low, eq1high, eq2low, eq2high, eq3low, eq3high, eq4low, eq4high;
						eq1from = eq1to = eq2from = eq2to = eq3from = eq3to = eq4from = eq4to = null;
						
						eq1from = nextSect3[r];
						eq1to = xSectOn1[q];
						
						switch (x1Target[q]) {
						case 2:
							eq2from = xSectOn1[q];
							eq2to = nextSect1[w];
							break;
						case 3:
							eq3from = xSectOn1[q];
							eq3to = nextSect1[w];
							break;
						case 4:
							eq4from = xSectOn1[q];
							eq4to = nextSect1[w];
							break;
						default: 
							continue;
						}
						switch (nextTar1[w]) {
						case 2:
							eq2from = nextSect1[w];
							eq2to = nextSect2[e];
							break;
						case 3:
							eq3from = nextSect1[w];
							eq3to = nextSect2[e];
							break;
						case 4:
							eq4from = nextSect1[w];
							eq4to = nextSect2[e];
							break;
						default: 
							continue;
						}
						switch (nextTar2[e]) {
						case 2:
							eq2from = nextSect2[e];
							eq2to = nextSect3[r];
							break;
						case 3:
							eq3from = nextSect2[e];
							eq3to = nextSect3[r];
							break;
						case 4:
							eq4from = nextSect2[e];
							eq4to = nextSect3[r];
							break;
						default: 
							continue;
						}
						
						if (EQ1.isEqInX()) {
							eq1low = eq1from.getX();
							eq1high = eq1to.getX();
						}
						else {
							eq1low = eq1from.getY();
							eq1high = eq1to.getY();
						}
						if (EQ2.isEqInX()) {
							eq2low = eq2from.getX();
							eq2high = eq2to.getX();
						}
						else {
							eq2low = eq2from.getY();
							eq2high = eq2to.getY();
						}
						if (EQ3.isEqInX()) {
							eq3low = eq3from.getX();
							eq3high = eq3to.getX();
						}
						else {
							eq3low = eq3from.getY();
							eq3high = eq3to.getY();
						}
						if (EQ4.isEqInX()) {
							eq4low = eq4from.getX();
							eq4high = eq4to.getX();
						}
						else {
							eq4low = eq4from.getY();
							eq4high = eq4to.getY();
						}
						
						PolygonBig poly = buildPolygon(eq1low, eq1high,
								eq2low, eq2high,
								eq3low, eq3high,
								eq4low, eq4high,
								order);
						poly.cleanUp();
						if (poly.getNPoints() < 3) continue;
						if (!poly.isSimple()) continue;
						if (poly.contains(EQ1) || poly.contains(EQ2) || poly.contains(EQ3) || poly.contains(EQ4)) continue;
						nValidPoly++;
						if (nValidPoly > 1) {
							if (((vEq1From.equals(eq1from) && vEq1To.equals(eq1to)) || (vEq1From.equals(eq1to) && vEq1To.equals(eq1from))) &&
									((vEq2From.equals(eq2from) && vEq2To.equals(eq2to)) || (vEq2From.equals(eq2to) && vEq2To.equals(eq2from))) &&
									((vEq3From.equals(eq3from) && vEq3To.equals(eq3to)) || (vEq3From.equals(eq3to) && vEq3To.equals(eq3from))) &&
									((vEq4From.equals(eq4from) && vEq4To.equals(eq4to)) || (vEq4From.equals(eq4to) && vEq4To.equals(eq4from)))) 
								nValidPoly--;
						}
						
						vEq1From = eq1from;
						vEq1To = eq1to;
						vEq2From = eq2from;
						vEq2To = eq2to;
						vEq3From = eq3from;
						vEq3To = eq3to;
						vEq4From = eq4from;
						vEq4To = eq4to;
						vOrder = order;
					}
					switch (nextTar2[e]) {
					case 2:
						usedEq2 = false;
					case 3:
						usedEq3 = false;
					case 4:
						usedEq4 = false;
					}
				}
				switch (nextTar1[w]) {
				case 2:
					usedEq2 = false;
				case 3:
					usedEq3 = false;
				case 4:
					usedEq4 = false;
				}
			}
			switch (x1Target[q]) {
			case 2:
				usedEq2 = false;
			case 3:
				usedEq3 = false;
			case 4:
				usedEq4 = false;
			}
		}
		if (nValidPoly == 0) throw new PolygonBuildException("No valid region formed.");
		if (nValidPoly > 1) throw new PolygonBuildException("Multiple valid regions formed.");
		
		BigDecimal eq1low, eq1high, eq2low, eq2high, eq3low, eq3high, eq4low, eq4high;
		if (EQ1.isEqInX()) {
			eq1low = vEq1From.getX();
			eq1high = vEq1To.getX();
		}
		else {
			eq1low = vEq1From.getY();
			eq1high = vEq1To.getY();
		}
		if (EQ2.isEqInX()) {
			eq2low = vEq2From.getX();
			eq2high = vEq2To.getX();
		}
		else {
			eq2low = vEq2From.getY();
			eq2high = vEq2To.getY();
		}
		if (EQ3.isEqInX()) {
			eq3low = vEq3From.getX();
			eq3high = vEq3To.getX();
		}
		else {
			eq3low = vEq3From.getY();
			eq3high = vEq3To.getY();
		}
		if (EQ4.isEqInX()) {
			eq4low = vEq4From.getX();
			eq4high = vEq4To.getX();
		}
		else {
			eq4low = vEq4From.getY();
			eq4high = vEq4To.getY();
		}
		finalPoly = buildPolygon(eq1low, eq1high, eq2low, eq2high, eq3low, eq3high, eq4low, eq4high, vOrder);
		finalPoly.cleanUp();
	}
	
	private PolygonBig buildPolygon(BigDecimal eq1low, BigDecimal eq1high,
			BigDecimal eq2low, BigDecimal eq2high) {
		PolygonBig poly = new PolygonBig();
		BigDecimal eq1step = eq1high.subtract(eq1low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		BigDecimal eq2step = eq2high.subtract(eq2low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		if (DEBUG) System.out.println("EQ1 step: " + eq1step);
		if (DEBUG) System.out.println("EQ2 step: " + eq2step);
		for (int q = 0; q < quality; q++) {
			BigDecimal iv = eq1low.add(eq1step.multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
			if (DEBUG) System.out.println("EQ1, XY=" + iv);
			try {
				if (EQ1.isEqInX()) poly.addPoint(new Point2DBig(iv, EQ1.eval(iv)));
				else poly.addPoint(new Point2DBig(EQ1.eval(iv), iv));
			}
			catch (OperatorDomainException eOD) { }
		}
		for (int q = 0; q < quality; q++) {
			BigDecimal iv = eq2low.add(eq2step.multiply(new BigDecimal(q), CalcConst.MC), CalcConst.MC);
			if (DEBUG) System.out.println("EQ2, XY=" + iv);
			try {
				if (EQ2.isEqInX()) poly.addPoint(new Point2DBig(iv, EQ2.eval(iv)));
				else poly.addPoint(new Point2DBig(EQ2.eval(iv), iv));
			}
			catch (OperatorDomainException eOD) { }
		}
		return poly;
	}
	
	private PolygonBig buildPolygon(BigDecimal eq1low, BigDecimal eq1high,
			BigDecimal eq2low, BigDecimal eq2high, 
			BigDecimal eq3low, BigDecimal eq3high) {
		PolygonBig poly = new PolygonBig();
		BigDecimal eq1step = eq1high.subtract(eq1low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		BigDecimal eq2step = eq2high.subtract(eq2low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		BigDecimal eq3step = eq3high.subtract(eq3low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		BigDecimal iv = eq1low;
		for (int q = 0; q < quality; q++, iv = iv.add(eq1step, CalcConst.MC)) {
			if (DEBUG) System.out.println("EQ1, XY=" + iv);
			try {
				if (EQ1.isEqInX()) poly.addPoint(new Point2DBig(iv, EQ1.eval(iv)));
				else poly.addPoint(new Point2DBig(EQ1.eval(iv), iv));
			}
			catch (OperatorDomainException eOD) { }
		}
		iv = eq3low;
		for (int q = 0; q < quality; q++, iv = iv.add(eq3step, CalcConst.MC)) {
			if (DEBUG) System.out.println("EQ3, XY=" + iv);
			try {
				if (EQ3.isEqInX()) poly.addPoint(new Point2DBig(iv, EQ3.eval(iv)));
				else poly.addPoint(new Point2DBig(EQ3.eval(iv), iv));
			}
			catch (OperatorDomainException eOD) { }
		}
		iv = eq2low;
		for (int q = 0; q < quality; q++, iv = iv.add(eq2step, CalcConst.MC)) {
			if (DEBUG) System.out.println("EQ2, XY=" + iv);
			try {
				if (EQ2.isEqInX()) poly.addPoint(new Point2DBig(iv, EQ2.eval(iv)));
				else poly.addPoint(new Point2DBig(EQ2.eval(iv), iv));
			}
			catch (OperatorDomainException eOD) { }
		}
		return poly;
	}
	
	private PolygonBig buildPolygon(BigDecimal eq1low, BigDecimal eq1high,
			BigDecimal eq2low, BigDecimal eq2high, 
			BigDecimal eq3low, BigDecimal eq3high, 
			BigDecimal eq4low, BigDecimal eq4high,
			int[] eqOrder) {
		PolygonBig poly = new PolygonBig();
		BigDecimal eq1step = eq1high.subtract(eq1low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		BigDecimal eq2step = eq2high.subtract(eq2low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		BigDecimal eq3step = eq3high.subtract(eq3low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		BigDecimal eq4step = eq4high.subtract(eq4low, CalcConst.MC).divide(new BigDecimal(quality), CalcConst.MC);
		
		BigDecimal[] lows = new BigDecimal[] { eq1low, eq2low, eq3low, eq4low };
		BigDecimal[] steps = new BigDecimal[] { eq1step, eq2step, eq3step, eq4step };
		Equation[] equs = new Equation[] { EQ1, EQ2, EQ3, EQ4 };
		
		for (int q = 0; q < 4; q++) {
			BigDecimal iv = lows[eqOrder[q] - 1];
			for (int w = 0; w < quality; w++) {
				iv = iv.add(steps[eqOrder[q] - 1]);
				try {
					if (equs[eqOrder[q] - 1].isEqInX()) poly.addPoint(new Point2DBig(iv, equs[eqOrder[q] - 1].eval(iv)));
					else poly.addPoint(new Point2DBig(equs[eqOrder[q] - 1].eval(iv), iv));
				}
				catch (OperatorDomainException eOD) { }
			}
		}
		
		return poly;
	}
	
	public PolygonBig getPoly() {
		return finalPoly;
	}
	
	public static void setQuality(int qual) {
		quality = qual;
	}
	
	public static int getQuality() {
		return quality;
	}
}
