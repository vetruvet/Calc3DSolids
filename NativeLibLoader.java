import java.io.*;
import java.util.ArrayList;

public final class NativeLibLoader {
	private static final String[] WIN_LIBS = new String[] { "lib/j3dcore-d3d.dll",
		/*"lib/j3dcore-ogl-cg.dll",*/
		"lib/j3dcore-ogl-chk.dll",
		"lib/j3dcore-ogl.dll" };
	private static final String[] LINUX_LIBS_32 = new String[] { "lib/libj3dcore-ogl.so.x32",
		"lib/libj3dcore-ogl-cg.so.x32" };
	private static final String[] LINUX_LIBS_64 = new String[] { "lib/libj3dcore-ogl.so.x64",
		"lib/libj3dcore-ogl-cg.so.x64" };
	
	private static final int[] WIN_LIB_SIZE = new int[] { 823296, /*40960,*/ 49152, 163840 };
	private static final int[] LINUX_LIB_32_SIZE = new int[] { 159428, 5274 };
	private static final int[] LINUX_LIB_64_SIZE = new int[] { 158113, 11359 };
	
	private ArrayList<String> loadedLibs = new ArrayList<String>();
	private int bytesCopied = 0;
	private CalcSolidsWindow.LoaderTask listener;
	private final OSType OS;
	
	private static NativeLibLoader instance = null;
	
	public static synchronized NativeLibLoader getInstance() {
		if (instance == null) instance = new NativeLibLoader();
		return instance;
	}
	
	private NativeLibLoader() {
		OS = detectOS();
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Cloning NativeLibLoader not allowed!");
	}
	
	public void loadLibs() throws UnsupportedOSException {
		String[] libs;
		switch (OS) {
		case WINDOWS:
			libs = WIN_LIBS;
			break;
		case LINUX32:
		case UNIX32:
		case MAC32:
			libs = LINUX_LIBS_32;
			break;
		case LINUX64:
		case UNIX64:
		case MAC64:
			libs = LINUX_LIBS_64;
			break;
		default:
			throw new UnsupportedOSException("The Operating System " + System.getProperty("os.name") + " is not supported!");
		}
		for (String lib : libs) {
			if (!loadLib(lib))
				throw new UnsupportedOSException("Necessary " + System.getProperty("os.name") + " libraries not Found!");
		}
	}
	
	public boolean unloadLibs() {
		String[] libs;
		switch (OS) {
		case WINDOWS:
			libs = WIN_LIBS;
			break;
		case LINUX32:
		case UNIX32:
		case MAC32:
			libs = LINUX_LIBS_32;
			break;
		case LINUX64:
		case UNIX64:
		case MAC64:
			libs = LINUX_LIBS_64;
			break;
		default:
			return true;
		}
		for (String lib : libs) {
			String libPath = lib.substring(lib.lastIndexOf('/') + 1).replaceFirst("\\.x((32)|(64))$", "");
			if (!unloadLib(libPath)) return false;
		}
		return true;
		
	}
	
	public boolean unloadLibs(String[] paths) {
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		for (String lib : paths) {
			if (!unloadLib(lib)) 
				return false;
		}
		return true;
	}
	
	private boolean loadLib(String path) {
		String libName = path.substring(path.lastIndexOf('/') + 1).replaceFirst("\\.x((32)|(64))$$", "");
		InputStream is = getClass().getClassLoader().getResourceAsStream(path);
		if (is == null) return false;
		try {
			File libFile = new File(libName);
			if (libFile.exists()) libFile.delete();
			
			BufferedInputStream in = new BufferedInputStream(is);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(libName));
			
			byte[] buf = new byte[512];
			int nRead = -1;
			while ((nRead = in.read(buf)) != -1) {
				bytesCopied += nRead;
				if (listener != null)
					listener.firePropertyChange("progress", new Integer(bytesCopied - 512), new Integer(bytesCopied));
				out.write(buf);
			}
			
			in.close();
			is.close();
			out.flush();
			out.close();
			
			System.load(libFile.getAbsolutePath());
			loadedLibs.add(libFile.getAbsolutePath());
			
			libFile.deleteOnExit();
		}
		catch (IOException eIO) {
			return false;
		}
		return true;
	}
	
	public String[] getLoadedLibs() {
		return loadedLibs.toArray(new String[] { });
	}
	
	private boolean unloadLib(String path) {
		return unloadLib(path, 250, 8);
	}
	
	private boolean unloadLib(String path, int timeout, int tries) {
		File libFile = new File(path);
		if (!libFile.exists()) return true;
		boolean deled = libFile.delete();
		if (deled) return true;
		else {
			if (tries > 0) return unloadLib(path, timeout, tries - 1);
			else return !libFile.exists();
		}
	}
	
	public int getNBytes() {
		int[] sizes;
		switch (OS) {
		case WINDOWS:
			sizes = WIN_LIB_SIZE;
			break;
		case LINUX32:
		case UNIX32:
		case MAC32:
			sizes = LINUX_LIB_32_SIZE;
			break;
		case LINUX64:
		case UNIX64:
		case MAC64:
			sizes = LINUX_LIB_64_SIZE;
			break;
		default:
			return 0;
		}
		int tot = 0;
		for (int size : sizes) {
			tot += size;
		}
		return tot;
	}
	
	private static OSType detectOS() {
		boolean is64bit = System.getProperty("os.arch").indexOf("64") != -1;
		
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") != -1) return OSType.WINDOWS;
		if (os.indexOf("mac") != -1) {
			if (is64bit) return OSType.MAC64;
			else return OSType.MAC32;
		}
		if (os.indexOf("nix") != -1) {
			if (is64bit) return OSType.UNIX64;
			else return OSType.UNIX32;
		}
		if (os.indexOf("nux") != -1) {
			if (is64bit) return OSType.LINUX64;
			else return OSType.LINUX32;
		}
		return OSType.OTHER;
	}
	
	public void setLoaderTask(CalcSolidsWindow.LoaderTask task) {
		listener = task;
	}
	
	public void removeLoaderTask() {
		listener = null;
	}
}

enum OSType {
	WINDOWS, LINUX32, LINUX64, MAC32, MAC64, UNIX32, UNIX64, OTHER
}