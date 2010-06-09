import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public final class CalcConst {
	public static final String UPDATE_LOCATION = "http://sites.google.com/site/vetruvet/c3s/";
	
	public static final String JAVA_BIN_PATH = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
	public static final String CURRENT_DIR;
	public static final boolean RUNNING_FROM_JAR;
	public static final String JAR_PATH;
	static {
		String cp = System.getProperty("java.class.path");
		RUNNING_FROM_JAR = (cp.indexOf("Calc3DSolids.jar") != -1);
		
		if (RUNNING_FROM_JAR) {
			String jarPath, dirPath;
			try {
				jarPath = new File("./Calc3DSolids.jar").getCanonicalPath();
				dirPath = new File(jarPath).getParentFile().getCanonicalPath();
			}
			catch (IOException e) {
				jarPath = null;
				dirPath = null;
			}
			JAR_PATH = jarPath;
			CURRENT_DIR = dirPath;
		}
		else {
			int firstSemi = cp.indexOf(";");
			if (firstSemi == -1) {
				CURRENT_DIR = cp;
			}
			else {
				CURRENT_DIR = cp.substring(0, firstSemi);
			}
			JAR_PATH = null;
		}
	}
	
	public static final BigDecimal TOLERANCE = new BigDecimal("0.00005");
	public static final int PRESI = 100;
	public static final BigDecimal PRESO = new BigDecimal("0.1");
	public static final int GRES = 50;
	public static final int VRES = 40;
	public static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);
	public static final DecimalFormat DBL_FMT; static {
		String pat = "0.0";
		for (int q = 1; q < MC.getPrecision(); q++) {
			pat += "#";
		}
		DBL_FMT = new DecimalFormat(pat);
	}
	public static final DecimalFormat PT_FMT = new DecimalFormat("0.###");
	public static final BigDecimal I_ACCURACY = new BigDecimal("0.1");
	public static final BigDecimal T_ACCURACY; static {
		String acc = "0.0";
		for (int q = 1; q < MC.getPrecision() - 1; q++) {
			acc += "0";
		}
		acc += "1";
		T_ACCURACY = new BigDecimal(acc);
	}
	public static final BigDecimal VERT_DIFF = new BigDecimal("0.0005");
	
	public static final BigDecimal PI = new BigDecimal("3.14159265358979323856264338327960288419706939937511");
	public static final BigDecimal E  = new BigDecimal("2.71828182845904523536028747135266249775724709369995");
	
	public static final double MIN_COORD = -100.0;
	public static final double MAX_COORD = 100.0;
	
	public static final float POLY_TRANSPARENCY = 0.666f;
	public static final float SOLID_TRANSPARENCY = POLY_TRANSPARENCY;
	public static final float DISC_SHELL_TRANSPARENCY = 0.25f;
	
	public static final Color POLY_COLOR = new Color(255, 127, 100);
	public static final Color SOLID_COLOR = POLY_COLOR;
	public static final Color EQ1_COLOR = new Color(255, 255, 66);
	public static final Color EQ2_COLOR = new Color(66, 127, 255);
	public static final Color EQ3_COLOR = new Color(127, 66, 255);
	public static final Color EQ4_COLOR = new Color(127, 127, 127);
	public static final Color[] EQ_COLOR = new Color[] { EQ1_COLOR, EQ2_COLOR, EQ3_COLOR, EQ4_COLOR };
	public static final Color X_AXIS = new Color(255, 0, 0);
	public static final Color Y_AXIS = new Color(0, 255, 0);
	public static final Color Z_AXIS = new Color(0, 0, 255);
	public static final Color R_AXIS = new Color(255, 0, 127);
	public static final Color RECT_COLOR = new Color(255 - POLY_COLOR.getRed(), 255 - POLY_COLOR.getGreen(), 255 - POLY_COLOR.getBlue());
	public static final Color DISC_SHELL_COLOR = RECT_COLOR;
	public static final Color SOLID_WIRE_COLOR; static {
		final int WHITE_GAIN = 32;
		int tRed = SOLID_COLOR.getRed() + WHITE_GAIN;
		int tBlue = SOLID_COLOR.getBlue() + WHITE_GAIN;
		int tGreen = SOLID_COLOR.getGreen() + WHITE_GAIN;
		SOLID_WIRE_COLOR = new Color(Math.min(255, tRed), Math.min(255, tGreen), Math.min(255, tBlue));
	}
	public static final Color DISC_SHELL_WIRE_COLOR; static {
		final int WHITE_GAIN = 32;
		int tRed = DISC_SHELL_COLOR.getRed() + WHITE_GAIN;
		int tBlue = DISC_SHELL_COLOR.getBlue() + WHITE_GAIN;
		int tGreen = DISC_SHELL_COLOR.getGreen() + WHITE_GAIN;
		DISC_SHELL_WIRE_COLOR = new Color(Math.min(255, tRed), Math.min(255, tGreen), Math.min(255, tBlue));
	}
	
	public static final int SCREEN_WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().width;
	public static final int SCREEN_HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().height;
	public static final int GRAPH_MIN_WIDTH = 200;
	public static final int GRAPH_MIN_HEIGHT = 200;
}