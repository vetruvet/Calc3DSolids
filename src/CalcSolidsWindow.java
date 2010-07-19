/*
 * Calculus 3D Solids Program
 * [Main Window Class]
 * 
 * Written by Valera Trubachev (C) 2009-2010.
 * 
 * Special thanks to Mr. Piesen for teaching me all this!
 * Also thanks to the Eclipse Foundation and
 * the Apache Foundation for their great
 * IDE (Eclipse), which was used (almost exclusively)
 * for the development of this program.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CalcSolidsWindow extends JFrame {
	private static final long serialVersionUID = 3947075375906163527L;

	private static final int EQU_WIDTH = 30;
	private static final String EQ1HOLDER = "[Enter equation 1 here]";
	private static final String EQ2HOLDER = "[Enter equation 2 here]";
	private static final String EQ3HOLDER = "[Enter equation 3 here]";
	private static final String EQ4HOLDER = "[Enter equation 4 here]";
	private static final String AXSHOLDER = "[Axis value]";
	private static final String HEIGHTHOLDER = "[Height]";
	private static final String DIRTY_MARKER = " [OUT OF SYNC WITH INPUT]";
	private static final Color HOLDER_COLOR = new Color(128, 128, 128);
	private static final String PROGRESS_READY = "Ready.";
	private File lastPath;
	private String lastText;
	
	private JTextField eq1Text, eq2Text, eq3Text, eq4Text, axisText, heightText;
	private JCheckBox useEq1, useEq2, useEq3, useEq4;
	private JComboBox xyEq1, xyEq2, xyEq3, xyEq4, axisXY, sectionBox;
	private JSpinner nSpinRev, nSpinSect;
	private JRadioButton discRadio, shellRadio, sectPerpX, sectPerpY;
	private JMenuItem graphEquMenu, graph2DMenu, graph3DMenu;
	private JButton graphEqu, graph2D, graph3D;
	private JProgressBar progress;
	private JTabbedPane solidTabs;
	private JPanel sectionHeightPanel;
	private JLabel repShapeLabel;
	
	private JPanel graphContentPanel;
	private Grapher graph;
	private HashMap<String, JLabel> graphLabels = new HashMap<String, JLabel>();
	
	private final ActionListener EQU_LISTEN = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			(new EquTask()).execute();
		}
	};
	
	private final ActionListener G2D_LISTEN = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			(new AreaTask()).execute();
		}
	};
	
	private final ActionListener G3D_LISTEN = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			(new VolumeTask()).execute();
		}
	};
	
	public CalcSolidsWindow(String title) {
		super(title);
	}
	
	public static void main(String[] args) {
		if (args.length >= 1 && args[0].equals("UNLOADLIBS")) {
			final NativeLibLoader unLoader = NativeLibLoader.getInstance();
			boolean success;
			if (args.length == 1) success = unLoader.unloadLibs();
			else {
				String[] libPaths = new String[args.length - 1];
				for (int q = 1; q < args.length; q++) 
					libPaths[q - 1] = args[q];
				success = unLoader.unloadLibs(libPaths);
			}
			if (!success) {
				JOptionPane.showMessageDialog(null, "Unable to delete native libraries", "Error Unloading", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
			System.exit(0);
		}
		
		if (args.length >= 1 && args[0].equals("DELUPDATER")) {
			try { Thread.sleep(1333); }
			catch (InterruptedException eI) { }
			boolean success;
			if (args.length == 1) success = (new File("CalcUpdater.jar")).delete();
			else success = (new File(args[1])).delete();
			if (!success) 
				JOptionPane.showMessageDialog(null, "Unables to delete temporary updater file", "Error finalizing Update", JOptionPane.WARNING_MESSAGE);
		}
		
		final NativeLibLoader loader = NativeLibLoader.getInstance();
		final JDialog progDiag = new JDialog((Frame) null, "Loading...", true);
		final LoaderTask loadTask = new LoaderTask(progDiag, loader);
		final JProgressBar loadProg = new JProgressBar(SwingConstants.HORIZONTAL, 0, loader.getNBytes());
		
		progDiag.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		progDiag.getContentPane().setLayout(new FlowLayout());
		
		loadProg.setIndeterminate(false);
		loadProg.setValue(0);
		loadProg.setPreferredSize(new Dimension(200, 40));
		loadProg.setString("Loading Resources...");
		loadProg.setStringPainted(true);
		
		Font origFont = loadProg.getFont();
		loadProg.setFont(new Font(origFont.getFamily(), origFont.getStyle(), origFont.getSize() + 4));
		
		loadTask.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("progress")) {
					loadProg.setValue(((Integer) e.getNewValue()).intValue());
				}
			}
		});
		
		progDiag.getContentPane().add(loadProg);
		progDiag.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
				loadTask.execute();
			}
			public void windowActivated(WindowEvent arg0) { }
			public void windowClosed(WindowEvent e) { }
			public void windowClosing(WindowEvent e) { }
			public void windowDeactivated(WindowEvent e) { }
			public void windowDeiconified(WindowEvent e) { }
			public void windowIconified(WindowEvent e) { }
		});
		
		progDiag.pack();
		progDiag.setResizable(false);
		progDiag.setLocationRelativeTo(null);
		progDiag.setVisible(true); 
		
		final CalcSolidsWindow CSW = new CalcSolidsWindow("Calculus Solids");
		CSW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		CSW.setIconImage(CalcUtils.getImage("Images/integral.png"));
		CSW.createGUI();
		CSW.createMenuBar();
		CSW.pack();
		CSW.setMinimumSize(CSW.getSize());
		CSW.setResizable(false);
		CSW.setVisible(true);
		
		CSW.addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent e) {
				try {
					String execStr = "\"" + CalcConst.JAVA_BIN_PATH + "\"";
					if (CalcConst.RUNNING_FROM_JAR) execStr += " -jar \"" + CalcConst.JAR_PATH + "\"";
					else execStr += " CalcSolidsWindow";
					
					execStr += " UNLOADLIBS";
					
					String[] libs = loader.getLoadedLibs();
					for (String lib : libs) {
						execStr += " \"" + lib + "\"";
					}
					
					Runtime.getRuntime().exec(execStr, null, new File(CalcConst.CURRENT_DIR));
				} 
				catch (IOException eIO) { 
					eIO.printStackTrace();
					JOptionPane.showMessageDialog(null, "Unable to start native library unloading", "Error Unloading", JOptionPane.ERROR_MESSAGE);
				}
			}
			public void windowOpened(WindowEvent e) { }
			public void windowIconified(WindowEvent e) { }
			public void windowDeiconified(WindowEvent e) { }
			public void windowDeactivated(WindowEvent e) { }
			public void windowClosed(WindowEvent e) { }
			public void windowActivated(WindowEvent e) { }
		});
		
		if (args.length == 1) {
			if (args[0].endsWith(".c3s")) {
				CSW.loadFromFile(args[0]);
			}
		}
	}
	
	public void createGUI() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
		
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.PAGE_AXIS));
		inputPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		getContentPane().add(inputPanel);
		
		JLabel title = new JLabel("Calculus 3D Solids");
		title.setFont(new Font("Serif", Font.BOLD, 30));
		title.setForeground(Color.RED.darker());
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		title.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		inputPanel.add(title);
		
		JPanel equPanel = new JPanel();
		equPanel.setLayout(new BoxLayout(equPanel, BoxLayout.PAGE_AXIS));
		equPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Equations"));
		inputPanel.add(equPanel);
		
		// ==== EQUATION PANEL 1 ====
		JPanel e1Panel = new JPanel(new FlowLayout());
		equPanel.add(e1Panel);
		
		useEq1 = new JCheckBox("", true);
		useEq1.getAccessibleContext().setAccessibleDescription("Specifiy whether or not to use this equation");
		e1Panel.add(useEq1);
		
		xyEq1 = new JComboBox(new String[]{"y1(x)=", "x1(y)="});
		xyEq1.getAccessibleContext().setAccessibleDescription("Specify what variable this equation is in");
		xyEq1.setSelectedIndex(0);
		e1Panel.add(xyEq1);
		
		eq1Text = new JTextField(EQ1HOLDER, EQU_WIDTH);
		eq1Text.getAccessibleContext().setAccessibleDescription("Enter the equation here");
		eq1Text.setForeground(HOLDER_COLOR);
		eq1Text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (eq1Text.getText().equals(EQ1HOLDER)) eq1Text.setText("");
				eq1Text.setForeground(Color.BLACK);
				eq1Text.selectAll();
				lastText = eq1Text.getText();
			}
			public void focusLost(FocusEvent e) {
				if (eq1Text.getText().equals("")) {
					eq1Text.setText(EQ1HOLDER);
					eq1Text.setForeground(HOLDER_COLOR);
				}
				if (!eq1Text.getText().equalsIgnoreCase(lastText)) {
					TitledBorder border = ((TitledBorder) graphContentPanel.getBorder());
					if (!border.getTitle().endsWith(DIRTY_MARKER)) {
						border.setTitle(border.getTitle() + DIRTY_MARKER);
						graphContentPanel.repaint();
					}
				}
			}
		});
		e1Panel.add(eq1Text);
		
		// ==== EQUATION PANEL 2 ====
		JPanel e2Panel = new JPanel(new FlowLayout());
		equPanel.add(e2Panel);
		
		useEq2 = new JCheckBox("", true);
		useEq2.getAccessibleContext().setAccessibleDescription("Specifiy whether or not to use this equation");
		e2Panel.add(useEq2);
		
		xyEq2 = new JComboBox(new String[]{"y2(x)=", "x2(y)="});
		xyEq2.getAccessibleContext().setAccessibleDescription("Specify what variable this equation is in");
		xyEq2.setSelectedIndex(0);
		e2Panel.add(xyEq2);
		
		eq2Text = new JTextField(EQ2HOLDER, EQU_WIDTH);
		eq2Text.getAccessibleContext().setAccessibleDescription("Enter the equation here");
		eq2Text.setForeground(HOLDER_COLOR);
		eq2Text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (eq2Text.getText().equals(EQ2HOLDER)) eq2Text.setText("");
				eq2Text.setForeground(Color.BLACK);
				eq2Text.selectAll();
				lastText = eq2Text.getText();
			}
			public void focusLost(FocusEvent e) {
				if (eq2Text.getText().equals("")) {
					eq2Text.setText(EQ2HOLDER);
					eq2Text.setForeground(HOLDER_COLOR);
				}
				if (!eq2Text.getText().equalsIgnoreCase(lastText)) {
					TitledBorder border = ((TitledBorder) graphContentPanel.getBorder());
					if (!border.getTitle().endsWith(DIRTY_MARKER)) {
						border.setTitle(border.getTitle() + DIRTY_MARKER);
						graphContentPanel.repaint();
					}
				}
			}
		});
		e2Panel.add(eq2Text);
		
		// ==== EQUATION PANEL 3 ====
		JPanel e3Panel = new JPanel(new FlowLayout());
		equPanel.add(e3Panel);
		
		useEq3 = new JCheckBox("", false);
		useEq3.getAccessibleContext().setAccessibleDescription("Specifiy whether or not to use this equation");
		e3Panel.add(useEq3);
		
		xyEq3 = new JComboBox(new String[]{"y3(x)=", "x3(y)="});
		xyEq3.getAccessibleContext().setAccessibleDescription("Specify what variable this equation is in");
		xyEq3.setSelectedIndex(0);
		e3Panel.add(xyEq3);
		
		eq3Text = new JTextField(EQ3HOLDER, EQU_WIDTH);
		eq3Text.getAccessibleContext().setAccessibleDescription("Enter the equation here");
		eq3Text.setForeground(HOLDER_COLOR);
		eq3Text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (eq3Text.getText().equals(EQ3HOLDER)) eq3Text.setText("");
				eq3Text.setForeground(Color.BLACK);
				eq3Text.selectAll();
				lastText = eq3Text.getText();
			}
			public void focusLost(FocusEvent e) {
				if (eq3Text.getText().equals("")) {
					eq3Text.setText(EQ3HOLDER);
					eq3Text.setForeground(HOLDER_COLOR);
				}
				if (!eq3Text.getText().equalsIgnoreCase(lastText)) {
					TitledBorder border = ((TitledBorder) graphContentPanel.getBorder());
					if (!border.getTitle().endsWith(DIRTY_MARKER)) {
						border.setTitle(border.getTitle() + DIRTY_MARKER);
						graphContentPanel.repaint();
					}
				}
			}
		});
		e3Panel.add(eq3Text);
		
		// ==== EQUATION PANEL 4 ====
		JPanel e4Panel = new JPanel(new FlowLayout());
		equPanel.add(e4Panel);
		
		useEq4 = new JCheckBox("", false);
		useEq4.getAccessibleContext().setAccessibleDescription("Specifiy whether or not to use this equation");
		e4Panel.add(useEq4);
		
		xyEq4 = new JComboBox(new String[]{"y4(x)=", "x4(y)="});
		xyEq4.getAccessibleContext().setAccessibleDescription("Specify what variable this equation is in");
		xyEq4.setSelectedIndex(0);
		e4Panel.add(xyEq4);
		
		eq4Text = new JTextField(EQ4HOLDER, EQU_WIDTH);
		eq4Text.getAccessibleContext().setAccessibleDescription("Enter the equation here");
		eq4Text.setForeground(HOLDER_COLOR);
		eq4Text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (eq4Text.getText().equals(EQ4HOLDER)) eq4Text.setText("");
				eq4Text.setForeground(Color.BLACK);
				eq4Text.selectAll();
				lastText = eq4Text.getText();
			}
			public void focusLost(FocusEvent e) {
				if (eq4Text.getText().equals("")) {
					eq4Text.setText(EQ4HOLDER);
					eq4Text.setForeground(HOLDER_COLOR);
				}
				if (!eq4Text.getText().equalsIgnoreCase(lastText)) {
					TitledBorder border = ((TitledBorder) graphContentPanel.getBorder());
					if (!border.getTitle().endsWith(DIRTY_MARKER)) {
						border.setTitle(border.getTitle() + DIRTY_MARKER);
						graphContentPanel.repaint();
					}
				}
			}
		});
		e4Panel.add(eq4Text);
		
		JPanel revPanel = new JPanel();
		revPanel.setLayout(new BoxLayout(revPanel, BoxLayout.PAGE_AXIS));
		
		JPanel axisPanel = new JPanel(new FlowLayout());
		axisPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Axis of Revolution"));
		revPanel.add(axisPanel);
		
		axisPanel.add(new JLabel("Revolve around: "));
		
		axisXY = new JComboBox(new String[]{"y=", "x="});
		axisXY.setSelectedIndex(0);
		axisPanel.add(axisXY);
		
		axisText = new JTextField(AXSHOLDER, 10);
		axisText.getAccessibleContext().setAccessibleDescription("Enter axis value here");
		axisText.setForeground(HOLDER_COLOR);
		axisText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (axisText.getText().equals(AXSHOLDER)) axisText.setText("");
				axisText.setForeground(Color.BLACK);
				axisText.selectAll();
				lastText = axisText.getText();
			}
			public void focusLost(FocusEvent e) {
				if (axisText.getText().equals("")) {
					axisText.setText(AXSHOLDER);
					axisText.setForeground(HOLDER_COLOR);
				}
				if (!axisText.getText().equalsIgnoreCase(lastText)) {
					TitledBorder border = ((TitledBorder) graphContentPanel.getBorder());
					if (!border.getTitle().endsWith(DIRTY_MARKER)) {
						border.setTitle(border.getTitle() + DIRTY_MARKER);
						graphContentPanel.repaint();
					}
				}
			}
		});
		axisPanel.add(axisText);
		
		JPanel methPanel = new JPanel(new FlowLayout());
		methPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Method"));
		revPanel.add(methPanel);
		
		ButtonGroup methGroup = new ButtonGroup();
		
		nSpinRev = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
		nSpinRev.getAccessibleContext().setAccessibleDescription("Number of Discs, Shells, or Washers to be graphed");
		methPanel.add(nSpinRev);
		
		discRadio = new JRadioButton("Discs/Washers", true);
		discRadio.getAccessibleContext().setAccessibleDescription("Use the disc/washer method of finding volume");
		methGroup.add(discRadio);
		methPanel.add(discRadio);
		
		shellRadio = new JRadioButton("Shells", false);
		shellRadio.getAccessibleContext().setAccessibleDescription("Use the shell method of finding volume");
		methGroup.add(shellRadio);
		methPanel.add(shellRadio);
		
		JPanel crossPanel = new JPanel();
		crossPanel.setLayout(new BoxLayout(crossPanel, BoxLayout.PAGE_AXIS));
		
		JPanel sectionPanel = new JPanel(new FlowLayout());
		crossPanel.add(sectionPanel);
		
		sectionPanel.add(new JLabel("Cross-Section:"));
		
		sectionHeightPanel = new JPanel(new FlowLayout());
		sectionHeightPanel.setVisible(false);
		crossPanel.add(sectionHeightPanel);
		
		repShapeLabel = new JLabel("Number of Cross-Section Shapes: ");
		
		sectionBox = new JComboBox(new String[]{CalcUtils.crossSectShapeAsStr(CrossSectionType.CIRCLE), 
				CalcUtils.crossSectShapeAsStr(CrossSectionType.SEMICIRCLE),
				CalcUtils.crossSectShapeAsStr(CrossSectionType.SQUARE),
				CalcUtils.crossSectShapeAsStr(CrossSectionType.RECTANGLE),
				CalcUtils.crossSectShapeAsStr(CrossSectionType.EQUI_TRIANGLE),
				CalcUtils.crossSectShapeAsStr(CrossSectionType.ISO_TRIANGLE) });
		sectionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newSel = (String) sectionBox.getSelectedItem();
				if (newSel.equals(CalcUtils.crossSectShapeAsStr(CrossSectionType.RECTANGLE)) ||
						newSel.equals(CalcUtils.crossSectShapeAsStr(CrossSectionType.ISO_TRIANGLE)))
					sectionHeightPanel.setVisible(true);
				else sectionHeightPanel.setVisible(false);
				//repShapeLabel.setText("Number of Representative " + sectionBox.getSelectedItem() + "s: ");
				pack();
			}
		});
		sectionBox.setSelectedIndex(2);
		sectionPanel.add(sectionBox);
		
		sectionHeightPanel.add(new JLabel("Constant Height: "));
		
		heightText = new JTextField(HEIGHTHOLDER, 10);
		heightText.getAccessibleContext().setAccessibleDescription("Enter constant height here");
		heightText.setForeground(HOLDER_COLOR);
		heightText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (heightText.getText().equals(HEIGHTHOLDER)) heightText.setText("");
				heightText.setForeground(Color.BLACK);
				heightText.selectAll();
				lastText = heightText.getText();
			}
			public void focusLost(FocusEvent e) {
				if (heightText.getText().equals("")) {
					heightText.setText(HEIGHTHOLDER);
					heightText.setForeground(HOLDER_COLOR);
				}
				if (!heightText.getText().equalsIgnoreCase(lastText)) {
					TitledBorder border = ((TitledBorder) graphContentPanel.getBorder());
					if (!border.getTitle().endsWith(DIRTY_MARKER)) {
						border.setTitle(border.getTitle() + DIRTY_MARKER);
						graphContentPanel.repaint();
					}
				}
			}
		});
		sectionHeightPanel.add(heightText);
		
		JPanel sectionXYpanel = new JPanel(new FlowLayout());
		crossPanel.add(sectionXYpanel);
		ButtonGroup perpGroup = new ButtonGroup();
		
		sectionXYpanel.add(new JLabel("Cross-Sections Perpendicular to: "));
		
		sectPerpX = new JRadioButton("X-Axis");
		sectPerpX.setSelected(true);
		sectionXYpanel.add(sectPerpX);
		perpGroup.add(sectPerpX);
		
		sectPerpY = new JRadioButton("Y-Axis");
		sectionXYpanel.add(sectPerpY);
		perpGroup.add(sectPerpY);
		
		JPanel numRepShape = new JPanel(new FlowLayout());
		crossPanel.add(numRepShape);
		
		numRepShape.add(repShapeLabel);
		
		nSpinSect = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
		numRepShape.add(nSpinSect);
		
		solidTabs = new JTabbedPane();
		solidTabs.addTab("Revolution", null, revPanel, "Create Solids of Revolution");
		solidTabs.addTab("Known Cross-Section", null, crossPanel, "Create Solids with Known Cross Sections");
		inputPanel.add(solidTabs);
		
		JPanel graphPanel = new JPanel(new FlowLayout());
		inputPanel.add(graphPanel);
		
		graphEqu = new JButton("Graph Equations");
		graphEqu.getAccessibleContext().setAccessibleDescription("Graph just the equations and axes");
		graphEqu.addActionListener(EQU_LISTEN);
		graphPanel.add(graphEqu);
		
		graph2D = new JButton("Graph 2D Graph");
		graph2D.getAccessibleContext().setAccessibleDescription("Find the region bounded by the equations and graph everything");
		graph2D.addActionListener(G2D_LISTEN);
		graphPanel.add(graph2D);
		
		graph3D = new JButton("Graph 3D Volume");
		graph3D.getAccessibleContext().setAccessibleDescription("Find the region and solid of revolution and graph everything");
		graph3D.addActionListener(G3D_LISTEN);
		graphPanel.add(graph3D);
		
		progress = new JProgressBar(SwingConstants.HORIZONTAL);
		progress.setIndeterminate(false);
		progress.setString(PROGRESS_READY);
		progress.setStringPainted(true);
		inputPanel.add(progress);
		
		inputPanel.setMinimumSize(inputPanel.getSize());
		
		// ==== GRAPH PANEL SETUP ====
		graphContentPanel = new JPanel(new BorderLayout());
		graphContentPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Graph Area"));
		graphContentPanel.setVisible(false);
		getContentPane().add(graphContentPanel);
		
		graph = new Grapher();
		graphContentPanel.add(graph, BorderLayout.CENTER);
		
		JPanel togglePan = new JPanel(new FlowLayout());
		togglePan.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Graph Components"));
		graphContentPanel.add(togglePan, BorderLayout.PAGE_END);
		
		JToggleButton axisTg = new JToggleButton("Axes", true);
		axisTg.getAccessibleContext().setAccessibleDescription("Toggle the axes on or off");
		axisTg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				graph.toggleAxes();
			}
		});
		togglePan.add(axisTg);
		
		JToggleButton polyTg = new JToggleButton("Region", true);
		polyTg.getAccessibleContext().setAccessibleDescription("Toggle the shaded area on or off");
		polyTg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				graph.toggleSolid();
			}
		});
		togglePan.add(polyTg);
		
		JToggleButton equTg = new JToggleButton("Functions", true);
		equTg.getAccessibleContext().setAccessibleDescription("Toggle the equations on or off");
		equTg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				graph.toggleEquations();
			}
		});
		togglePan.add(equTg);
		
		JToggleButton shapeTg = new JToggleButton("Rep. Shapes", true);
		shapeTg.getAccessibleContext().setAccessibleDescription("Toggle the representative shapes on or off");
		shapeTg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				graph.toggleRepShapes();
			}
		});
		togglePan.add(shapeTg);
		
		JToggleButton solidTg = new JToggleButton("Solid", true);
		solidTg.getAccessibleContext().setAccessibleDescription("Toggle the 3D solid on or off");
		solidTg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				graph.toggleSolid();
			}
		});
		togglePan.add(solidTg);
		
		JToggleButton wireTg = new JToggleButton("Wireframe Solid", true);
		wireTg.getAccessibleContext().setAccessibleDescription("Toggle the wireframe solid on or off");
		wireTg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				graph.toggleWireframes();
			}
		});
		togglePan.add(wireTg);
		
		JPanel colorRoot = new JPanel();
		colorRoot.setLayout(new BoxLayout(colorRoot, BoxLayout.PAGE_AXIS));
		graphContentPanel.add(colorRoot, BorderLayout.LINE_END);
		
		JPanel colorPanel = new JPanel();
		colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.PAGE_AXIS));
		colorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Colors"));
		
		colorRoot.add(Box.createVerticalGlue());
		colorRoot.add(colorPanel);
		colorRoot.add(Box.createVerticalGlue());

		JButton viewReset = new JButton("Reset View");
		viewReset.setAlignmentX(Component.CENTER_ALIGNMENT);
		viewReset.getAccessibleContext().setAccessibleDescription("Reset the view of the graph");
		viewReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				graph.resetView();
			}
		});
		colorRoot.add(viewReset);
		
		JButton graphHide = new JButton("Hide Graph");
		graphHide.setAlignmentX(Component.CENTER_ALIGNMENT);
		graphHide.getAccessibleContext().setAccessibleDescription("Hide the graph");
		graphHide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphContentPanel.setVisible(false);
				CalcSolidsWindow.this.pack();
			}
		});
		colorRoot.add(graphHide);
		
		JPanel graphLblPanel = new JPanel();
		graphLblPanel.setLayout(new BoxLayout(graphLblPanel, BoxLayout.PAGE_AXIS));
		
		colorPanel.add(Box.createHorizontalGlue());
		colorPanel.add(graphLblPanel);
		colorPanel.add(Box.createHorizontalGlue());
		
		JLabel xAxisLbl = new JLabel("X Axis");
		xAxisLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		xAxisLbl.setForeground(CalcConst.X_AXIS);
		graphLblPanel.add(xAxisLbl);
		graphLabels.put("xAxis", xAxisLbl);
		
		JLabel yAxisLbl = new JLabel("Y Axis");
		yAxisLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		yAxisLbl.setForeground(CalcConst.Y_AXIS);
		graphLblPanel.add(yAxisLbl);
		graphLabels.put("yAxis", yAxisLbl);
		
		JLabel zAxisLbl = new JLabel("Z Axis");
		zAxisLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		zAxisLbl.setForeground(CalcConst.Z_AXIS);
		graphLblPanel.add(zAxisLbl);
		graphLabels.put("zAxis", zAxisLbl);
		
		JLabel rAxisLbl = new JLabel("Axis of Rev.");
		rAxisLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		rAxisLbl.setForeground(CalcConst.R_AXIS);
		graphLblPanel.add(rAxisLbl);
		graphLabels.put("rAxis", rAxisLbl);
		
		JLabel solidLbl = new JLabel("Solid");
		solidLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		solidLbl.setForeground(CalcConst.SOLID_COLOR);
		graphLblPanel.add(solidLbl);
		graphLabels.put("solid", solidLbl);
		
		JLabel repRectLbl = new JLabel("Rep. Rect.");
		repRectLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		repRectLbl.setForeground(CalcConst.RECT_COLOR);
		graphLblPanel.add(repRectLbl);
		graphLabels.put("repRect", repRectLbl);
		
		JLabel discShellLbl = new JLabel("Discs/Washers/Shells");
		discShellLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		discShellLbl.setForeground(CalcConst.DISC_SHELL_COLOR);
		graphLblPanel.add(discShellLbl);
		graphLabels.put("discs", discShellLbl);
		
		JLabel regionLbl = new JLabel("Region");
		regionLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		regionLbl.setForeground(CalcConst.POLY_COLOR);
		graphLblPanel.add(regionLbl);
		graphLabels.put("region", regionLbl);
		
		for (int q = 0; q < 4; q++) {
			JLabel eqLbl = new JLabel("y" + (q + 1) + "(x)");
			eqLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
			eqLbl.setForeground(CalcConst.EQ_COLOR[q]);
			graphLblPanel.add(eqLbl);
			graphLabels.put("eq" + q, eqLbl);
		}
	}
	
	public void createMenuBar() {
		JMenuBar rootBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		rootBar.add(fileMenu);
		
		JMenuItem saveItem = new JMenuItem("Save", CalcUtils.getImageIcon("Images/save.gif"));
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		saveItem.getAccessibleContext().setAccessibleDescription("Save the current set of inputs to a file");
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc;
				if (lastPath == null) fc = new JFileChooser();
				else fc = new JFileChooser(lastPath);
				
				fc.setMultiSelectionEnabled(false);
				fc.setDialogTitle("Save Current Setup");
				fc.setFileFilter(new FileNameExtensionFilter("Calculus 3D States (*.c3s)", "c3s"));
				fc.setAcceptAllFileFilterUsed(false);
				
				if (fc.showSaveDialog(CalcSolidsWindow.this) == JFileChooser.APPROVE_OPTION) {
					File file;
					if (fc.getSelectedFile().getAbsolutePath().endsWith(".c3s")) file = fc.getSelectedFile();
					else file = new File(fc.getSelectedFile().getAbsolutePath() + ".c3s");

					saveToFile(file);

					lastPath = fc.getCurrentDirectory();
				}
			}
		});
		fileMenu.add(saveItem);
		
		JMenuItem openItem = new JMenuItem("Open", CalcUtils.getImageIcon("Images/open.gif"));
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		openItem.getAccessibleContext().setAccessibleDescription("Read inputs from a previously saved file.");
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc;
				if (lastPath == null) fc = new JFileChooser();
				else fc = new JFileChooser(lastPath);
				
				fc.setMultiSelectionEnabled(false);
				fc.setDialogTitle("Open Existing Setup");
				fc.setFileFilter(new FileNameExtensionFilter("Calculus 3D States (*.c3s)", "c3s"));
				fc.setAcceptAllFileFilterUsed(false);
				
				if (fc.showOpenDialog(CalcSolidsWindow.this) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					loadFromFile(file);
				}
			}
		});
		fileMenu.add(openItem);
		
		JMenuItem resetItem = new JMenuItem("Reset", CalcUtils.getImageIcon("Images/new.gif"));
		resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		resetItem.getAccessibleContext().setAccessibleDescription("Reset all inputs to their default state");
		resetItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eq1Text.setText(EQ1HOLDER);
				eq2Text.setText(EQ2HOLDER);
				eq3Text.setText(EQ3HOLDER);
				eq4Text.setText(EQ4HOLDER);
				
				eq1Text.setForeground(HOLDER_COLOR);
				eq2Text.setForeground(HOLDER_COLOR);
				eq3Text.setForeground(HOLDER_COLOR);
				eq4Text.setForeground(HOLDER_COLOR);
				
				useEq1.setSelected(true);
				useEq2.setSelected(true);
				useEq3.setSelected(false);
				useEq4.setSelected(false);
				
				xyEq1.setSelectedIndex(0);
				xyEq2.setSelectedIndex(0);
				xyEq3.setSelectedIndex(0);
				xyEq4.setSelectedIndex(0);
				
				axisText.setText(AXSHOLDER);
				axisText.setForeground(HOLDER_COLOR);
				axisXY.setSelectedIndex(0);
				
				((SpinnerNumberModel) nSpinRev.getModel()).setValue(new Integer(10));
				discRadio.setSelected(true);
				shellRadio.setSelected(false);
				
				solidTabs.setSelectedIndex(0);
				
				sectionBox.setSelectedIndex(2);
				sectPerpX.setSelected(true);
				sectPerpY.setSelected(false);
				((SpinnerNumberModel) nSpinSect.getModel()).setValue(new Integer(10));
				
				heightText.setText(HEIGHTHOLDER);
				heightText.setForeground(HOLDER_COLOR);
			}
		});
		fileMenu.add(resetItem);
		
		fileMenu.addSeparator();
		
		JMenuItem exitItem = new JMenuItem("Exit", CalcUtils.getImageIcon("Images/stop.gif"));
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		exitItem.getAccessibleContext().setAccessibleDescription("Exit the program");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CalcSolidsWindow.this.setVisible(false);
				CalcSolidsWindow.this.dispose();
			}
		});
		fileMenu.add(exitItem);
		
		JMenu graphMenu = new JMenu("Graph");
		rootBar.add(graphMenu);
		
		graphEquMenu = new JMenuItem("Equation Graph");
		graphEquMenu.getAccessibleContext().setAccessibleDescription("Graph just the equations");
		graphEquMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));
		graphEquMenu.addActionListener(EQU_LISTEN);
		graphMenu.add(graphEquMenu);
		
		graph2DMenu = new JMenuItem("2D Graph");
		graph2DMenu.getAccessibleContext().setAccessibleDescription("Find the enclosed region and graph everything");
		graph2DMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK));
		graph2DMenu.addActionListener(G2D_LISTEN);
		graphMenu.add(graph2DMenu);
		
		graph3DMenu = new JMenuItem("3D Graph");
		graph3DMenu.getAccessibleContext().setAccessibleDescription("Find the enclosed region and solid and graph everything");
		graph3DMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK));
		graph3DMenu.addActionListener(G3D_LISTEN);
		graphMenu.add(graph3DMenu);
		
		graphMenu.addSeparator();
		
		JMenuItem captureItem = new JMenuItem("Save Graph", CalcUtils.getImageIcon("Images/saveAs.gif"));
		captureItem.getAccessibleContext().setAccessibleDescription("Save the image currently displayed by the graph");
		captureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
		captureItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc;
					if (lastPath == null) fc = new JFileChooser();
					else fc = new JFileChooser(lastPath);
					
					fc.setMultiSelectionEnabled(false);
					fc.setDialogTitle("Save Graph Image");
					fc.setAcceptAllFileFilterUsed(false);
					
					FileNameExtensionFilter[] filters = new FileNameExtensionFilter[] {
							new FileNameExtensionFilter("All Images", "jpg", "gif", "bmp", "png"),
							new FileNameExtensionFilter("PNG Files", "png"),
							new FileNameExtensionFilter("JPEG Files", "jpg"),
							new FileNameExtensionFilter("GIF Files", "gif"),
							new FileNameExtensionFilter("BMP Files", "bmp"),
					};
					for (FileNameExtensionFilter filter : filters) fc.addChoosableFileFilter(filter);
					fc.setFileFilter(filters[1]);
					
					if (fc.showSaveDialog(CalcSolidsWindow.this) == JFileChooser.APPROVE_OPTION) {
						String filePath = fc.getSelectedFile().getAbsolutePath();
						
						BufferedImage img = graph.captureImg();
						if (filePath.endsWith(".png") || fc.getFileFilter() == filters[1]) {
							if (!filePath.endsWith(".png")) filePath += ".png";
							ImageIO.write(img, "png", new File(filePath));
						}
						else if (filePath.endsWith(".jpg") || fc.getFileFilter() == filters[2]) {
							if (!filePath.endsWith(".jpg")) filePath += ".jpg";
							ImageIO.write(img, "jpg", new File(filePath));
						}
						else if (filePath.endsWith(".gif") || fc.getFileFilter() == filters[3]) {
							if (!filePath.endsWith(".gif")) filePath += ".gif";
							ImageIO.write(img, "gif", new File(filePath));
						}
						else if (filePath.endsWith(".bmp") || fc.getFileFilter() == filters[4]) {
							if (!filePath.endsWith(".mbp")) filePath += ".bmp";
							ImageIO.write(img, "bmp", new File(filePath));
						}
						else {
							JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Invalid file type for saving!", 
									"Bad File Type", JOptionPane.ERROR_MESSAGE);
						}
						
						lastPath = fc.getCurrentDirectory();
					}
				}
				catch (IOException eIO) {
					JOptionPane.showMessageDialog(CalcSolidsWindow.this, "An Error occurred while writing the image!",
							"Error Writing Image", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		graphMenu.add(captureItem);
		
		rootBar.add(Box.createHorizontalGlue());
		
		JMenu helpMenu = new JMenu("Help");
		rootBar.add(helpMenu);
		
		JMenuItem helpItem = new JMenuItem("Help", CalcUtils.getImageIcon("Images/help.gif"));
		helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpItem.getAccessibleContext().setAccessibleDescription("Get help with using this program");
		helpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JDialog diag = new JDialog(CalcSolidsWindow.this, "Help", true);
				diag.getContentPane().setLayout(new BorderLayout(3, 3));
				
				final JEditorPane helpPane = new JEditorPane();
				helpPane.setEditable(false);
				helpPane.setContentType("text/html");
				
				String helpPath = "Help/";
				if (CalcConst.RUNNING_FROM_JAR) helpPath = "jar:file:" + CalcConst.JAR_PATH + "!/" + helpPath;
				
				final URL homeURL = CalcSolidsWindow.class.getResource(helpPath + "general.html");
				try {
					helpPane.setPage(homeURL);
				}
				catch (IOException eIO) {
					JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Could not locate Help file!", "Help not found!", JOptionPane.ERROR_MESSAGE);
					helpPane.setText("Help file not found: " + homeURL.toString() + "!!!");
				}
				
				final ArrayList<URL> backList = new ArrayList<URL>();
				final ArrayList<URL> fwdList = new ArrayList<URL>();
				
				final JButton backButton = new JButton("< Back");
				final JButton homeButton = new JButton("Home");
				final JButton fwdButton = new JButton("Forward >");
				
				backButton.setEnabled(false);
				backButton.getAccessibleContext().setAccessibleDescription("Go back a page");
				backButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						URL lastPage = backList.remove(backList.size() - 1);
						try {
							if (backList.size() == 0) backButton.setEnabled(false);
							fwdList.add(helpPane.getPage());
							helpPane.setPage(lastPage);
							fwdButton.setEnabled(true);
						}
						catch (IOException eIO) {
							JOptionPane.showMessageDialog(diag, "URL not found: \n" + lastPage.toString(), "Bad URL!", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				
				homeButton.setEnabled(true);
				homeButton.getAccessibleContext().setAccessibleDescription("Go to the first page");
				homeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						try {
							backList.add(helpPane.getPage());
							backButton.setEnabled(true);
							fwdList.clear();
							fwdButton.setEnabled(false);
							helpPane.setPage(homeURL);
						}
						catch (IOException eIO) {
							JOptionPane.showMessageDialog(diag, "URL not found: \n" + homeURL.toString(), "Bad URL!", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				
				fwdButton.setEnabled(false);
				fwdButton.getAccessibleContext().setAccessibleDescription("Go forward a page");
				fwdButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						URL nextPage = fwdList.remove(fwdList.size() - 1);
						try {
							if (fwdList.size() == 0) fwdButton.setEnabled(false);
							backList.add(helpPane.getPage());
							helpPane.setPage(nextPage);
							backButton.setEnabled(true);
						}
						catch (IOException eIO) {
							JOptionPane.showMessageDialog(diag, "URL not found: \n" + nextPage.toString(), "Bad URL!", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				
				helpPane.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent evt) {
						if (evt.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
						try {
							URL evtURL = evt.getURL();
							if (evtURL.getProtocol().equals("mailto")) {
								if (Desktop.isDesktopSupported()) {
									Desktop desk = Desktop.getDesktop();
									if (desk.isSupported(Desktop.Action.MAIL)) {
										desk.mail(evtURL.toURI());
									}
									else {
										JOptionPane.showMessageDialog(diag, "Your Desktop doesn't support mailing actions", "Unsupported Desktop", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									JOptionPane.showMessageDialog(diag, "Your Desktop doesn't support mailing actions", "Unsupported Desktop", JOptionPane.ERROR_MESSAGE);
								}
							}
							else {
								backList.add(helpPane.getPage());
								fwdList.clear();
								helpPane.setPage(evtURL);
								backButton.setEnabled(true);
								fwdButton.setEnabled(false);
							}
						}
						catch (IOException eIO) {
							JOptionPane.showMessageDialog(diag, "URL not found: \n" + evt.getURL().toString(), "Bad URL!", JOptionPane.ERROR_MESSAGE);
						} 
						catch (URISyntaxException e) {
							JOptionPane.showMessageDialog(diag, "Bad EMail format: " + evt.getURL().toString(), "Bad EMail Address!", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				
				JPanel helpPanel = new JPanel(new BorderLayout());
				
				JScrollPane genScroll = new JScrollPane(helpPane);
				genScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				genScroll.setPreferredSize(new Dimension(400, 400));
				genScroll.setMinimumSize(new Dimension(100, 100));
				helpPanel.add(genScroll);
				
				JButton closeButton = new JButton("Close");
				closeButton.getAccessibleContext().setAccessibleDescription("Close the help dialog");
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						diag.setVisible(false);
						diag.dispose();
					}
				});
				
				JPanel leftButs = new JPanel(new FlowLayout());
				leftButs.add(backButton);
				leftButs.add(homeButton);
				leftButs.add(fwdButton);
				
				JPanel rightButs = new JPanel(new FlowLayout());
				rightButs.add(closeButton);
				
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
				buttonPanel.add(leftButs);
				buttonPanel.add(Box.createHorizontalGlue());
				buttonPanel.add(rightButs);
				
				diag.getContentPane().add(helpPanel, BorderLayout.CENTER);
				diag.getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
				diag.pack();
				diag.setResizable(true);
				diag.setVisible(true);
			}
		});
		helpMenu.add(helpItem);
		
		JMenuItem updateItem = new JMenuItem("Update", CalcUtils.getImageIcon("Images/refresh.gif"));
		updateItem.getAccessibleContext().setAccessibleDescription("Check for Available Updates to this program");
		updateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!CalcConst.RUNNING_FROM_JAR) {
					JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Cannot update from outside JAR", "Run from JAR to update!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					URL hashURL;
					BufferedReader URLIn;
					
					//retrieve MD5 hash
					hashURL = new URL(CalcConst.UPDATE_LOCATION + "Calc3DSolids.md5");
					URLIn = new BufferedReader(new InputStreamReader(hashURL.openStream()));
					String newMD5Hash = URLIn.readLine().toUpperCase();
					URLIn.close();
					
					//retrieve SHA1 hash
					hashURL = new URL(CalcConst.UPDATE_LOCATION + "Calc3DSolids.sha");
					URLIn = new BufferedReader(new InputStreamReader(hashURL.openStream()));
					String newSHA1Hash = URLIn.readLine().toUpperCase();
					URLIn.close();
					
					//generate hashes of current JAR
					InputStream jarIn = new FileInputStream(CalcConst.JAR_PATH);
					MessageDigest MD5Digest = MessageDigest.getInstance("MD5");
					MessageDigest SHA1Digest = MessageDigest.getInstance("SHA-1");
					
					byte[] buf = new byte[1024];
					int nRead = -1;
					while ((nRead = jarIn.read(buf)) != -1) {
						MD5Digest.update(buf, 0, nRead);
						SHA1Digest.update(buf, 0, nRead);
					}
					jarIn.close();
					
					String oldMD5Hash = CalcUtils.byteArrayToHexStr(MD5Digest.digest());
					String oldSHA1Hash = CalcUtils.byteArrayToHexStr(SHA1Digest.digest());
					
					if (!newMD5Hash.equalsIgnoreCase(oldMD5Hash) && !newSHA1Hash.equalsIgnoreCase(oldSHA1Hash)) {
						int resp = JOptionPane.showConfirmDialog(CalcSolidsWindow.this, "There is a newer version available.\nUpdate now?", "Update Available", JOptionPane.YES_NO_OPTION);
						if (resp == JOptionPane.YES_OPTION) {
							File updaterFile = new File("CalcUpdater.jar");
							if (updaterFile.exists()) updaterFile.delete();
							
							URL updaterURL = new URL(CalcConst.UPDATE_LOCATION + "CalcUpdater.jar");
							BufferedInputStream updateIn = new BufferedInputStream(updaterURL.openStream());
							BufferedOutputStream updateOut = new BufferedOutputStream(new FileOutputStream(updaterFile));
							
							byte[] buffer = new byte[1024];
							int nBytes = -1;
							while ((nBytes = updateIn.read(buffer)) != -1) {
								updateOut.write(buffer, 0, nBytes);
							}
							
							updateIn.close();
							updateOut.flush();
							updateOut.close();
							
							try {
								String execStr = "\"" + CalcConst.JAVA_BIN_PATH + "\" -jar \"" + updaterFile.getCanonicalPath() + "\" \"" + CalcConst.UPDATE_LOCATION + "\"";
								CalcSolidsWindow.this.setVisible(false);
								CalcSolidsWindow.this.dispose();
								Runtime.getRuntime().exec(execStr, null, new File(CalcConst.CURRENT_DIR));
							}
							catch (IOException eIO) {
								eIO.printStackTrace();
								JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Unable to start updater!", "Unable to update", JOptionPane.ERROR_MESSAGE);
							}
						}
						else {
							JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Please select \"Update\" from the menu\nwhen you are ready to update", "Update Postponed", JOptionPane.INFORMATION_MESSAGE);
						}
					}
					else {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this, "This program is Up-to-Date!", "Nothing to Update", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				catch (MalformedURLException eMURL) {
					JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Update hashes not found on update server!", "Unable to update", JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException eIO) {
					JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Unable to retrieve update hashes!", "Unable to update", JOptionPane.ERROR_MESSAGE);
				}
				catch (NoSuchAlgorithmException eNSA) {
					JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Unable to hash JAR file!", "Unable to update", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		helpMenu.add(updateItem);
		
		helpMenu.addSeparator();
		
		JMenuItem aboutItem = new JMenuItem("About", CalcUtils.getImageIcon("Images/info.gif"));
		aboutItem.getAccessibleContext().setAccessibleDescription("About this program");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(CalcSolidsWindow.this, 
						"Calculus 3D Solids v2.9.9b (6274 SLoC)\n" +
						"\n" +
						"Written by: Valera Trubachev\n" + 
						"   \u00A9 2009-2010\n" + 
						"\n" + 
						"Special thanks to:\n" +
						"   Mr. Piesen\n" +
						"   Eclipse Foundation and contributors\n" + 
						"   Apache Software Foundation and contributors",
						"About this Program", 
						JOptionPane.INFORMATION_MESSAGE, CalcUtils.getImageIcon("Images/bugs.png"));
			}
		});
		helpMenu.add(aboutItem);
		
		setJMenuBar(rootBar);
	}
	
	private void saveToFile(File file) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			writer.write(eq1Text.getText());
			writer.newLine();
			writer.write(eq2Text.getText());
			writer.newLine();
			writer.write(eq3Text.getText());
			writer.newLine();
			writer.write(eq4Text.getText());
			writer.newLine();
			
			writer.write(xyEq1.getSelectedIndex() + "");
			writer.newLine();
			writer.write(xyEq2.getSelectedIndex() + "");
			writer.newLine();
			writer.write(xyEq3.getSelectedIndex() + "");
			writer.newLine();
			writer.write(xyEq4.getSelectedIndex() + "");
			writer.newLine();
			
			writer.write(useEq1.isSelected() + "");
			writer.newLine();
			writer.write(useEq2.isSelected() + "");
			writer.newLine();
			writer.write(useEq3.isSelected() + "");
			writer.newLine();
			writer.write(useEq4.isSelected() + "");
			writer.newLine();
			
			writer.write(axisText.getText());
			writer.newLine();
			writer.write(axisXY.getSelectedIndex() + "");
			writer.newLine();
			
			writer.write(solidTabs.getSelectedIndex() + "");
			writer.newLine();
			
			writer.write(((SpinnerNumberModel) nSpinRev.getModel()).getNumber() + "");
			writer.newLine();
			writer.write(discRadio.isSelected() + "");
			writer.newLine();
			writer.write(shellRadio.isSelected() + "");
			writer.newLine();
			
			writer.write(sectionBox.getSelectedIndex() + "");
			writer.newLine();
			writer.write(heightText.getText());
			writer.newLine();
			writer.write(sectPerpX.isSelected() + "");
			writer.newLine();
			writer.write(sectPerpY.isSelected() + "");
			writer.newLine();
			writer.write(((SpinnerNumberModel) nSpinSect.getModel()).getNumber() + "");			
			
			writer.close();
		}
		catch (IOException eIO) {
			JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Error Writing to File!!!", "I/O Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void loadFromFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) return;
		loadFromFile(file);
	}
	
	private void loadFromFile(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			
			if ((line = reader.readLine()) != null) 
				eq1Text.setText(line);
			if (!eq1Text.getText().equals(EQ1HOLDER)) eq1Text.setForeground(Color.BLACK);
			else eq1Text.setForeground(HOLDER_COLOR);
			
			if ((line = reader.readLine()) != null) 
				eq2Text.setText(line);
			if (!eq2Text.getText().equals(EQ2HOLDER)) eq2Text.setForeground(Color.BLACK);
			else eq2Text.setForeground(HOLDER_COLOR);
			
			if ((line = reader.readLine()) != null) 
				eq3Text.setText(line);
			if (!eq3Text.getText().equals(EQ3HOLDER)) eq3Text.setForeground(Color.BLACK);
			else eq3Text.setForeground(HOLDER_COLOR);
			
			if ((line = reader.readLine()) != null) 
				eq4Text.setText(line);
			if (!eq4Text.getText().equals(EQ4HOLDER)) eq4Text.setForeground(Color.BLACK);
			else eq4Text.setForeground(HOLDER_COLOR);
			
			if ((line = reader.readLine()) != null) 
				xyEq1.setSelectedIndex(Integer.parseInt(line));
			if ((line = reader.readLine()) != null) 
				xyEq2.setSelectedIndex(Integer.parseInt(line));
			if ((line = reader.readLine()) != null) 
				xyEq3.setSelectedIndex(Integer.parseInt(line));
			if ((line = reader.readLine()) != null) 
				xyEq4.setSelectedIndex(Integer.parseInt(line));
			
			if ((line = reader.readLine()) != null) 
				useEq1.setSelected(Boolean.parseBoolean(line));
			if ((line = reader.readLine()) != null) 
				useEq2.setSelected(Boolean.parseBoolean(line));
			if ((line = reader.readLine()) != null) 
				useEq3.setSelected(Boolean.parseBoolean(line));
			if ((line = reader.readLine()) != null) 
				useEq4.setSelected(Boolean.parseBoolean(line));
			
			if ((line = reader.readLine()) != null) 
				axisText.setText(line);
			if (!axisText.getText().equals(AXSHOLDER)) axisText.setForeground(Color.BLACK);
			else axisText.setForeground(HOLDER_COLOR);
			if ((line = reader.readLine()) != null) 
				axisXY.setSelectedIndex(Integer.parseInt(line));
			
			if ((line = reader.readLine()) != null) 
				solidTabs.setSelectedIndex(Integer.parseInt(line));
			
			if ((line = reader.readLine()) != null) 
				((SpinnerNumberModel) nSpinRev.getModel()).setValue(new Integer(line));
			if ((line = reader.readLine()) != null) 
				discRadio.setSelected(Boolean.parseBoolean(line));
			if ((line = reader.readLine()) != null) 
				shellRadio.setSelected(Boolean.parseBoolean(line));
			
			if ((line = reader.readLine()) != null) 
				sectionBox.setSelectedIndex(Integer.parseInt(line));
			
			if ((line = reader.readLine()) != null) 
				heightText.setText(line);
			if (!heightText.getText().equals(HEIGHTHOLDER)) heightText.setForeground(Color.BLACK);
			else heightText.setForeground(HOLDER_COLOR);
			
			if ((line = reader.readLine()) != null) 
				sectPerpX.setSelected(Boolean.parseBoolean(line));
			if ((line = reader.readLine()) != null) 
				sectPerpY.setSelected(Boolean.parseBoolean(line));
			if ((line = reader.readLine()) != null) 
				((SpinnerNumberModel) nSpinSect.getModel()).setValue(new Integer(line));
			
			reader.close();
		}
		catch (FileNotFoundException eFNF) {
			JOptionPane.showMessageDialog(this, "File Not Found!!!", "File not Found", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException eIO) {
			JOptionPane.showMessageDialog(this, "Error Reading from File!!!", "I/O Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (NullPointerException eNP) {
			JOptionPane.showMessageDialog(this, "Invalid File Format!!!", "Invalid File", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void setProgressBar(String newText) {
		SwingUtilities.invokeLater(new ProgRun(newText));
	}
	
	private class EquTask extends GraphTask {
		protected Void doInBackground() {
			setProgressBar("Parsing Inputs...");
			if (!init(false)) {
				setProgressBar(PROGRESS_READY);
				return null;
			}
			
			((TitledBorder) graphContentPanel.getBorder()).setTitle("2D Graph of Equations");
			graphContentPanel.repaint();
			
			setProgressBar("Initializing Graph...");
			graph.setupEquGraph(initEqus, initAxis, (initSolidRev) ? initAxisY : initSectPerpX);
			graph.getAccessibleContext().setAccessibleDescription("2D Graph of Equations");
			setProgressBar("Adding Equations to Graph");
			graph.addEquations();
			setProgressBar("Adding Axes To Graph");
			graph.addAxes();
			setProgressBar("Graph Creation Complete.");
			graph.resetView();
			
			for (String lbl : graphLabels.keySet()) graphLabels.get(lbl).setVisible(false);
			
			graphLabels.get("xAxis").setVisible(true);
			graphLabels.get("yAxis").setVisible(true);
			if (initAxis != null) graphLabels.get("rAxis").setVisible(true); 
			
			for (int q = 0; q < initEqus.length; q++) {
				JLabel eqLbl = graphLabels.get("eq" + q);
				eqLbl.setText(initEqus[q].isEqInX() ? "y" + (q + 1) + "(x)" : "x" + (q + 1) + "(y)");
				eqLbl.setVisible(true);
			}

			graphContentPanel.setVisible(true);
			CalcSolidsWindow.this.pack();
			setProgressBar("Showing Graph.");
			setProgressBar(PROGRESS_READY);
			return null;
		}
	}
	
	private class AreaTask extends GraphTask {
		protected Void doInBackground() {
			setProgressBar("Parsing Inputs...");
			if (!init(true)) {
				setProgressBar(PROGRESS_READY);
				return null;
			}
			
			((TitledBorder) graphContentPanel.getBorder()).setTitle("2D Graph of Area");
			graphContentPanel.repaint();
			
			GraphType gType;
			if (initSolidRev) {
				if (initUseDisc) gType = GraphType.FLAT_WITH_POLY_DISC;
				else gType = GraphType.FLAT_WITH_POLY_SHELL;
			}
			else {
				gType = GraphType.FLAT_CROSS_SECT;
			}
			
			setProgressBar("Initializing Graph...");
			graph.setupAreaGraph(initPoly, initEqus, (initSolidRev) ? initAxis : null, 
					(initSolidRev) ? initAxisY : initSectPerpX, 
							(initSolidRev) ? initNDiscShell : initNRepShape, gType);
			graph.getAccessibleContext().setAccessibleDescription("2D Graph of Region and Equations");
			setProgressBar("Adding Equations to Graph");
			graph.addEquations();
			setProgressBar("Adding Found Region");
			graph.addFlatPoly();
			setProgressBar("Adding Axes To Graph");
			graph.addAxes();
			setProgressBar("Adding Representative Rectangle");
			graph.addRepShapes();
			setProgressBar("Graph Creation Complete.");
			graph.resetView();
			
			for (String lbl : graphLabels.keySet()) graphLabels.get(lbl).setVisible(false);
			
			graphLabels.get("xAxis").setVisible(true);
			graphLabels.get("yAxis").setVisible(true);
			graphLabels.get("repRect").setVisible(true);
			graphLabels.get("region").setVisible(true);
			if (initSolidRev) graphLabels.get("rAxis").setVisible(true); 
			
			for (int q = 0; q < initEqus.length; q++) {
				JLabel eqLbl = graphLabels.get("eq" + q);
				eqLbl.setText(initEqus[q].isEqInX() ? "y" + (q + 1) + "(x)" : "x" + (q + 1) + "(y)");
				eqLbl.setVisible(true);
			}
			
			graphContentPanel.setVisible(true);
			CalcSolidsWindow.this.pack();
			setProgressBar("Showing Graph.");
			setProgressBar(PROGRESS_READY);
			return null;
		}
	}
	
	private class VolumeTask extends GraphTask {
		protected Void doInBackground() {
			setProgressBar("Parsing Inputs...");
			if (!init(true)) {
				setProgressBar(PROGRESS_READY);
				return null;
			}
			
			((TitledBorder) graphContentPanel.getBorder()).setTitle("3D Graph of Volume");
			graphContentPanel.repaint();
			
			setProgressBar("Initializing Graph...");
			if (initSolidRev) {
				graph.setupVRevGraph(initPoly, initEqus, initAxis, initAxisY, initNDiscShell, initUseDisc);
			}
			else {
				GraphType gType;
				switch (initSectType) {
				case SQUARE:
					gType = GraphType.VOL_SECT_SQUARE;
					break;
				case CIRCLE:
					gType = GraphType.VOL_SECT_CIRCLE;
					break;
				case EQUI_TRIANGLE:
					gType = GraphType.VOL_SECT_EQUI_TRIANGLE;
					break;
				case SEMICIRCLE:
					gType = GraphType.VOL_SECT_SEMICIRCLE;
					break;
				case ISO_TRIANGLE:
					gType = GraphType.VOL_SECT_ISO_TRIANGLE;
					break;
				case RECTANGLE:
					gType = GraphType.VOL_SECT_RECTANGLE;
					break;
				default:
					gType = null;
				}
				graph.setupVSectGraph(initPoly, initEqus, initHeight, initSectPerpX, initNRepShape, gType);
			}
			graph.getAccessibleContext().setAccessibleDescription("3D graph of Region and Solid");
			setProgressBar("Adding Solid");
			graph.addSolid();
			setProgressBar("Adding Wireframe Solid(s)");
			graph.addWireframes();
			setProgressBar("Adding Graph Axes");
			graph.addAxes();
			setProgressBar("Adding Original Region");
			graph.addFlatPoly();
			setProgressBar("Graph Creation Complete.");
			graph.resetView();
			
			for (String lbl : graphLabels.keySet()) graphLabels.get(lbl).setVisible(false);
			
			graphLabels.get("xAxis").setVisible(true);
			graphLabels.get("yAxis").setVisible(true);
			graphLabels.get("zAxis").setVisible(true);
			graphLabels.get("solid").setVisible(true);
			if (initSolidRev) graphLabels.get("rAxis").setVisible(true); 
			
			JLabel discLbl = graphLabels.get("discs");
			discLbl.setText((initUseDisc) ? "Discs/Washers" : "Shells");
			discLbl.setVisible(true);
			
			graphContentPanel.setVisible(true);
			CalcSolidsWindow.this.pack();
			setProgressBar("Showing Graph.");
			setProgressBar(PROGRESS_READY);
			return null;
		}
	}
	
	private abstract class GraphTask extends SwingWorker<Void, Void> {
		protected PolygonBig initPoly;
		protected BigDecimal initAxis, initHeight;
		protected boolean initAxisY, initUseDisc, initSolidRev, initSectPerpX;
		protected Equation[] initEqus;
		protected CrossSectionType initSectType;
		protected int initNDiscShell, initNRepShape;
		
		protected boolean init(boolean findRegion) {
			boolean solidOfRev = solidTabs.getSelectedIndex() == 0;
			
			CrossSectionType sect = null;
			if (!solidOfRev) {
				String shapeSelect = (String) sectionBox.getSelectedItem();
				if (shapeSelect.equals(CalcUtils.crossSectShapeAsStr(CrossSectionType.CIRCLE))) 
					sect = CrossSectionType.CIRCLE;
				else if (shapeSelect.equals(CalcUtils.crossSectShapeAsStr(CrossSectionType.SEMICIRCLE)))
					sect = CrossSectionType.SEMICIRCLE;
				else if (shapeSelect.equals(CalcUtils.crossSectShapeAsStr(CrossSectionType.SQUARE)))
					sect = CrossSectionType.SQUARE;
				else if (shapeSelect.equals(CalcUtils.crossSectShapeAsStr(CrossSectionType.EQUI_TRIANGLE))) 
					sect = CrossSectionType.EQUI_TRIANGLE;
				else if (shapeSelect.equals(CalcUtils.crossSectShapeAsStr(CrossSectionType.ISO_TRIANGLE)))
					sect = CrossSectionType.ISO_TRIANGLE;
				else if (shapeSelect.equals(CalcUtils.crossSectShapeAsStr(CrossSectionType.RECTANGLE))) 
					sect = CrossSectionType.RECTANGLE;
				else return false;	
			}
			
			int nEqUsed = 0;
			if (useEq1.isSelected()) nEqUsed++;
			if (useEq2.isSelected()) nEqUsed++;
			if (useEq3.isSelected()) nEqUsed++;
			if (useEq4.isSelected()) nEqUsed++;
			
			setProgressBar("Checking Input");
			if (nEqUsed < ((findRegion) ? 2 : 1)) {
				JOptionPane.showMessageDialog(CalcSolidsWindow.this, "At least " + ((findRegion) ? "2" : "1") + " functions need to be used!", "Select Functions", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			String[] eqTextInput = new String[] { eq1Text.getText(),
					eq2Text.getText(),
					eq3Text.getText(),
					eq4Text.getText() };
			boolean[] useEqs = new boolean[] { useEq1.isSelected(), 
					useEq2.isSelected(), 
					useEq3.isSelected(), 
					useEq4.isSelected() };
			boolean[] eqInYInput = new boolean[] { (xyEq1.getSelectedIndex() == 1), 
					(xyEq2.getSelectedIndex() == 1), 
					(xyEq3.getSelectedIndex() == 1), 
					(xyEq4.getSelectedIndex() == 1)};
			
			boolean missEq = false;
			if ((eq1Text.getText().equals(EQ1HOLDER) && useEq1.isSelected())) missEq = true;
			if ((eq2Text.getText().equals(EQ2HOLDER) && useEq2.isSelected())) missEq = true;
			if ((eq3Text.getText().equals(EQ3HOLDER) && useEq3.isSelected())) missEq = true;
			if ((eq4Text.getText().equals(EQ4HOLDER) && useEq4.isSelected())) missEq = true;
			if (missEq) {
				JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Not all selected equations have been entered!", "Missing Equations", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			boolean axisEntered = true;
			if (solidOfRev) {
				if (axisText.getText().equals(AXSHOLDER)) {
					axisEntered = false;
					if (findRegion) {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Axis value not entered!", "Missing Axis", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
			else {
				if (sect == CrossSectionType.RECTANGLE || sect == CrossSectionType.ISO_TRIANGLE) {
					if (heightText.getText().equals(HEIGHTHOLDER)) {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Constant height not entered!", "Missing Height", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
			
			String[] eqText = new String[nEqUsed];
			boolean[] eqInY = new boolean[nEqUsed];
			int w = 0;
			for (int q = 0; q < 4; q++) {
				if (useEqs[q]) {
					eqText[w] = eqTextInput[q];
					eqInY[w] = eqInYInput[q];
					w++;
				}
			}
			
			Equation[] equs = new Equation[nEqUsed];
			setProgressBar("Parsing Equations");
			for (int q = 0; q < nEqUsed; q++) {
				equs[q] = new Equation(eqText[q], (eqInY[q]) ? "y" : "x");
				try {
					equs[q].parseFunc();
				}
				catch (FunctionParseException eFP) {
					JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Syntax error in function " + (q + 1), "Syntax Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			
			boolean axisY = false;
			BigDecimal axisVal = null, heightVal = null;
			if (solidOfRev) {
				if (axisEntered) {
					setProgressBar("Parsing Axis");
					axisY = axisXY.getSelectedIndex() == 0;
					Equation axisEq = new Equation(axisText.getText(), (axisY) ? "x" : "y");
					try {
						axisEq.parseFunc();
					}
					catch (FunctionParseException eFP) {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this, 
								"Error in axis value!", "Invalid Axis Value", 
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					if (!axisEq.getRootOp().isConstant()) {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this, 
								"Error in axis value!", "Invalid Axis Value", 
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					try {
						axisVal = axisEq.eval(BigDecimal.ZERO);
					}
					catch (OperatorDomainException eOD) {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this, 
								"Error in axis value!", "Invalid Axis Value",
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
			else {
				if (sect == CrossSectionType.RECTANGLE || sect == CrossSectionType.ISO_TRIANGLE) {
					setProgressBar("Parsing Height");
					Equation heightEq = new Equation(heightText.getText(), "x");
					try {
						heightEq.parseFunc();
					} catch (FunctionParseException eFP) {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this,
								"Error in constant height!", "Invalid Height",
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					if (!heightEq.getRootOp().isConstant()) {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this,
								"Error in constant height!", "Invalid Height",
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					try {
						heightVal = heightEq.eval(BigDecimal.ZERO);
					} catch (OperatorDomainException eOD) {
						JOptionPane.showMessageDialog(CalcSolidsWindow.this,
								"Error in constant height!", "Invalid Height",
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
			
			if (!findRegion) {
				initEqus = equs;
				initSolidRev = solidOfRev;
				if (solidOfRev) {
					if (axisEntered) {
						initAxis = axisVal;
						initAxisY = axisY;
						initSectPerpX = sectPerpX.isSelected();
					}
					else initAxis = null;
				}
				return true;
			}
			
			PolygonBig poly = null;
			PolygonBuilder polyBuild;
			try {
				polyBuild = new PolygonBuilder(equs);
			}
			catch (IllegalArgumentException eIA) {
				return false;
			}
			setProgressBar("Finding Region");
			try {
				polyBuild.build();
			}
			catch (PolygonBuildException ePB) {
				JOptionPane.showMessageDialog(CalcSolidsWindow.this, ePB.getMessage(), "Error Building Polygon", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			poly = polyBuild.getPoly();
			
			if (solidOfRev) {
				setProgressBar("Checking Region vs. Axis");
				boolean axisCross = false;
				if (axisY) {
					if ((axisVal.compareTo(poly.getMaxY()) < 0) && (axisVal.compareTo(poly.getMinY()) > 0)) axisCross = true; 
				}
				else {
					if ((axisVal.compareTo(poly.getMaxX()) < 0) && (axisVal.compareTo(poly.getMinX()) > 0)) axisCross = true;
				}
				if (axisCross) {
					JOptionPane.showMessageDialog(CalcSolidsWindow.this, "Axis intersects polygon!", "Invalid axis value", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			
			initPoly = poly;
			initEqus = equs;
			initSolidRev = solidOfRev;
			if (solidOfRev) {
				SpinnerNumberModel nModel = (SpinnerNumberModel) nSpinRev.getModel();
				
				initAxis = axisVal;
				initAxisY = axisY;
				initNDiscShell = nModel.getNumber().intValue();
				initUseDisc = discRadio.isSelected();
			}
			else {
				SpinnerNumberModel nModel = (SpinnerNumberModel) nSpinSect.getModel();
				
				initSectPerpX = sectPerpX.isSelected();
				initNRepShape = nModel.getNumber().intValue();
				initHeight = heightVal;
				initSectType = sect;
			}
			return true;
		}
	}

	protected static class LoaderTask extends SwingWorker<Void, Void> {
		private final JDialog PROG_DIAG;
		private final NativeLibLoader LOADER;
		
		public LoaderTask(JDialog progress, NativeLibLoader load) {
			super();
			PROG_DIAG = progress;
			LOADER = load;
			LOADER.setLoaderTask(this);
		}
		
		protected Void doInBackground() {
			try {
				LOADER.loadLibs();
				PROG_DIAG.setVisible(false);
				PROG_DIAG.dispose();
			}
			catch (UnsupportedOSException eOUS) {
				PROG_DIAG.setVisible(false);
				PROG_DIAG.dispose();
				JOptionPane.showMessageDialog(null, eOUS.getMessage(), "Unsupported OS", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
			return null;
		}
	}
	
	private class ProgRun implements Runnable {
		private final String NEW_STR;
		private final boolean INDET;
		
		public ProgRun(String newStr) {
			NEW_STR = newStr;
			INDET = !newStr.equals(PROGRESS_READY);
		}
		
		public void run() {
			progress.setString(NEW_STR);
			progress.setIndeterminate(INDET);
			if (INDET) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				
				graph2D.setEnabled(false);
				graph3D.setEnabled(false);
				graphEqu.setEnabled(false);
				
				graph2DMenu.setEnabled(false);
				graph3DMenu.setEnabled(false);
				graphEquMenu.setEnabled(false);
			}
			else {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				
				graph2D.setEnabled(true);
				graph3D.setEnabled(true);
				graphEqu.setEnabled(true);
				
				graph2DMenu.setEnabled(true);
				graph3DMenu.setEnabled(true);
				graphEquMenu.setEnabled(true);
			}
		}
	}
}
