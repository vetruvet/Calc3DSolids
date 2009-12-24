import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

public class CalcUtils {
	public static final boolean DEBUG = false;
	
	private static final byte[] HEX_CHAR_TBL = new byte[] {(byte) '0', (byte) '1', (byte) '2', (byte) '3', 
		(byte) '4', (byte) '5', (byte) '6', (byte) '7', 
		(byte) '8', (byte) '9', (byte) 'a', (byte) 'b', 
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'};
	
	public static int matchCount(String src, String toMatch) {
		int occurs = 0;
		int lastIndex = src.indexOf(toMatch);
		
		while (lastIndex != -1) {
			occurs++;
			lastIndex = src.indexOf(toMatch, lastIndex + 1);
		}
		
		return occurs;
	}
	
	public static Image getImage(String path) {
		ImageIcon ii = getImageIcon(path);
		if (ii != null) return ii.getImage();
		else return null;
	}
	
	public static ImageIcon getImageIcon(String path) {
		URL iconURL = CalcSolidsWindow.class.getResource(path);
		if (iconURL != null) return new ImageIcon(iconURL);
		else return null;
	}
	
	public static Point2DBig flipXY(Point2DBig p) {
		return new Point2DBig(p.getY(), p.getX());
	}
	
	public static String arrayAsStr(int[] in) {
		String out = "";
		for (int q = 0; q < in.length - 1; q++) {
			out += in[q] + " ";
		}
		out += in[in.length - 1];
		return out;
	}
	
	public static String subarrayAsStr(int[] in, int lowInd, int highInd) {
		String out = "";
		for (int q = lowInd; q < highInd; q++) {
			out += in[q] + " ";
		}
		out += in[highInd];
		return out;
	}
	
	public static String byteArrayToHexStr(byte[] rawData) throws UnsupportedEncodingException {
		byte[] hexChars = new byte[rawData.length * 2];
		int q = 0;
		for (byte b : rawData) {
			int filt = b & 0xFF; //guarantee that it's 255 or less
			hexChars[q++] = HEX_CHAR_TBL[filt >>> 4]; //get 4 high bits (discard 4 low bits)
			hexChars[q++] = HEX_CHAR_TBL[filt & 0xF]; //get 4 low bits (discard 4 high bits)
		}
		return new String(hexChars, "ASCII");
	}
	
	public static boolean deleteWithRetry(String path, int timeout, int tries) {
		File file = new File(path);
		file.deleteOnExit();
		if (!file.exists()) return true;
		boolean deled = file.delete();
		if (deled) return true;
		else {
			if (tries > 0) return deleteWithRetry(path, timeout, tries - 1);
			else return !file.exists();
		}
	}
	
	/**
	 * Rewritten from source of java.awt.geom.Line2D.relativeCCW()
	 * Needs to use BigDecimal for precision purposes.
	 */
	protected static int relativeCCW(BigDecimal x1, BigDecimal y1, 
			BigDecimal x2, BigDecimal y2, 
			BigDecimal px, BigDecimal py) {
		x2 = x2.subtract(x1, CalcConst.MC);
		y2 = y2.subtract(y1, CalcConst.MC);
		px = px.subtract(x1, CalcConst.MC);
		py = py.subtract(y1, CalcConst.MC);
		BigDecimal ccw = px.multiply(y2, CalcConst.MC).subtract(py.multiply(x2, CalcConst.MC), CalcConst.MC);
		if (ccw.compareTo(BigDecimal.ZERO) == 0) {
			ccw = px.multiply(x2, CalcConst.MC).add(py.multiply(y2, CalcConst.MC), CalcConst.MC);
			if (ccw.compareTo(BigDecimal.ZERO) > 0) {
				px = px.subtract(x2, CalcConst.MC);
				py = py.subtract(y2, CalcConst.MC);
				ccw = px.multiply(x2, CalcConst.MC).add(py.multiply(y2, CalcConst.MC), CalcConst.MC);
				if (ccw.compareTo(BigDecimal.ZERO) < 0) {
					ccw = BigDecimal.ZERO;
				}
			}
		}
		if (ccw.compareTo(BigDecimal.ZERO) < 0) return -1;
		else if (ccw.compareTo(BigDecimal.ZERO) > 0) return 1;
		else return 0;
	}
	
	/**
	 * Rewritten from source of java.awt.geom.Line2D.linesIntersect()
	 * Needs to use BigDecimal for precision purposes.
	 */
	protected static boolean linesIntersect(BigDecimal x1, BigDecimal y1, 
			BigDecimal x2, BigDecimal y2, 
			BigDecimal x3, BigDecimal y3, 
			BigDecimal x4, BigDecimal y4) {
		return ((relativeCCW(x1, y1, x2, y2, x3, y3) * 
				relativeCCW(x1, y1, x2, y2, x4, y4) <= 0) && 
				(relativeCCW(x3, y3, x4, y4, x1, y1) * 
						relativeCCW(x3, y3, x4, y4, x2, y2) <= 0));
	}
	
	protected static boolean linesIntersect(Line2DBig l1, Line2DBig l2) {
		return linesIntersect(l1.getX1(), l1.getY1(), l1.getX2(), l1.getY2(), l2.getX1(), l2.getY1(), l2.getX2(), l2.getY2());
	}
	
	public static <E> ArrayList<E> removeRepeats(ArrayList<E> list) {
		ArrayList<E> unique = new ArrayList<E>();
		for (Iterator<E> itL = list.iterator(); itL.hasNext(); ) {
			E obj = itL.next();
			boolean found = false;
			
			for (Iterator<E> itU = unique.iterator(); itU.hasNext(); ) {
				if (itU.next().equals(obj)) {
					found = true;
					break;
				}
			}
			if (!found) unique.add(obj); 
		}
		return unique;
	}
	
	public static String operandAsString(Operand op) {
		switch(op) {
		case PLUS:
			return "+";
		case MINUS:
			return "-";
		case MULT:
			return "*";
		case DIV:
			return "/";
		case POW:
			return "^";
		case SIN:
			return "sin";
		case COS:
			return "cos";
		case TAN:
			return "tan";
		case SEC:
			return "sec";
		case CSC:
			return "csc";
		case COT:
			return "cot";
		case ARCSIN:
			return "arcsin";
		case ARCCOS:
			return "arccos";
		case ARCTAN:
			return "arctan";
		case ARCSEC:
			return "arcsec";
		case ARCCSC:
			return "arccsc";
		case ARCCOT:
			return "arccot";
		case NLOG:
			return "ln";
		case LOG:
			return "log";
		case ABS:
			return "abs";
		case SQRT:
			return "sqrt";
		case CBRT:
			return "cbrt";
		case OP:
			return "(";
		case CP:
			return ")";
		case NEG:
			return "-";
		case CONSTANT:
			return "#";
		case CONST_E:
			return "e";
		case CONST_PI:
			return "pi";
		case INDEP_VAR:
			return "xy";
		default:
			return "";
		}
	}
	
	public static int operandStrLen(Operand op) {
		return operandAsString(op).length();
	}
	
	public static String crossSectShapeAsStr(CrossSectionType shape) {
		switch (shape) {
		case CIRCLE:
			return "Circle";
		case SEMICIRCLE:
			return "Semicircle";
		case SQUARE:
			return "Square";
		case RECTANGLE:
			return "Rectangle";
		case EQUI_TRIANGLE:
			return "Equilateral Triangle";
		case ISO_TRIANGLE:
			return "Isosceles Triangle";
		default:
			return "";
		}
	}
}
