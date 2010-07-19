import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Vetruvet
 *
 */
public final class NativeLibLoader {
	private static final String[] WIN32_LIBS = new String[] { "lib/j3dcore-d3d.dll.x32",
		/*"lib/j3dcore-ogl-cg.dll.x32",*/ 	//don't ask why, but this one doesn't load, 
											//and its absense doesn't cause any problems
		"lib/j3dcore-ogl-chk.dll.x32",
		"lib/j3dcore-ogl.dll.x32" };
	private static final String[] WIN64_LIBS = new String[] { "lib/j3dcore-ogl.dll.x64" };
	private static final String[] LINUX_LIBS_32 = new String[] { "lib/libj3dcore-ogl.so.x32",
		"lib/libj3dcore-ogl-cg.so.x32" };
	private static final String[] LINUX_LIBS_64 = new String[] { "lib/libj3dcore-ogl.so.x64",
		"lib/libj3dcore-ogl-cg.so.x64" };
	
	private static final int[] WIN32_LIB_SIZE = new int[] { 823296, /*40960,*/ 49152, 163840 };
	private static final int[] WIN64_LIB_SIZE = new int[] { 229376 };
	private static final int[] LINUX_LIB_32_SIZE = new int[] { 159428, 5274 };
	private static final int[] LINUX_LIB_64_SIZE = new int[] { 158113, 11359 };
	
	private ArrayList<String> loadedLibs = new ArrayList<String>();
	private int bytesCopied = 0;
	private CalcSolidsWindow.LoaderTask listener;
	private final OSType OS;
	
	private static NativeLibLoader instance = null;
	
	/**
	 * @return
	 */
	public static synchronized NativeLibLoader getInstance() {
		if (instance == null) instance = new NativeLibLoader();
		return instance;
	}
	
	private NativeLibLoader() {
		OS = detectOS();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Cloning NativeLibLoader not allowed!");
	}
	
	/**
	 * @throws UnsupportedOSException
	 */
	public synchronized void loadLibs() throws UnsupportedOSException {
		String[] libs;
		switch (OS) {
		case WINDOWS32:
			libs = WIN32_LIBS;
			break;
		case WINDOWS64:
			libs = WIN64_LIBS;
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
	
	/**
	 * @return
	 */
	public synchronized boolean unloadLibs() {
		String[] libs;
		switch (OS) {
		case WINDOWS32:
			libs = WIN32_LIBS;
			break;
		case WINDOWS64:
			libs = WIN64_LIBS;
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
	
	/**
	 * @param paths
	 * @return
	 */
	public synchronized boolean unloadLibs(String[] paths) {
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		for (String lib : paths) {
			if (!unloadLib(lib)) 
				return false;
		}
		return true;
	}
	
	/**
	 * @param path
	 * @return
	 */
	private synchronized boolean loadLib(String path) {
		String libName = path.substring(path.lastIndexOf('/') + 1).replaceFirst("\\.x((32)|(64))$$", "");
		InputStream is = getClass().getClassLoader().getResourceAsStream(path);
		if (is == null) return false;
		try {
			File libFile = new File(libName);
			if (libFile.exists()) libFile.delete();
			
			BufferedInputStream in = new BufferedInputStream(is);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(libName));
			
			byte[] buf = new byte[4096];
			int nRead = -1;
			while ((nRead = in.read(buf)) != -1) {
				bytesCopied += nRead;
				if (listener != null)
					listener.firePropertyChange("progress", new Integer(bytesCopied - 512), new Integer(bytesCopied));
				out.write(buf, 0, nRead);
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
	
	/**
	 * @return
	 */
	public String[] getLoadedLibs() {
		return loadedLibs.toArray(new String[] { });
	}
	
	/**
	 * @param path
	 * @return
	 */
	private synchronized boolean unloadLib(String path) {
		return CalcUtils.deleteWithRetry(path, 250, 8);
	}
	
	/**
	 * @return
	 */
	public int getNBytes() {
		int[] sizes;
		switch (OS) {
		case WINDOWS32:
			sizes = WIN32_LIB_SIZE;
			break;
		case WINDOWS64:
			sizes = WIN64_LIB_SIZE;
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
	
	/**
	 * @return
	 */
	private static OSType detectOS() {
		boolean is64bit = System.getProperty("os.arch").indexOf("64") != -1;
		
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") != -1) {
			if (is64bit) 	return OSType.WINDOWS64;
			else 			return OSType.WINDOWS32;
		}
		if (os.indexOf("mac") != -1) {
			if (is64bit) 	return OSType.MAC64;
			else 			return OSType.MAC32;
		}
		if (os.indexOf("nix") != -1) {
			if (is64bit) 	return OSType.UNIX64;
			else 			return OSType.UNIX32;
		}
		if (os.indexOf("nux") != -1) {
			if (is64bit) 	return OSType.LINUX64;
			else 			return OSType.LINUX32;
		}
		return OSType.OTHER;
	}
	
	/**
	 * @param task
	 */
	public void setLoaderTask(CalcSolidsWindow.LoaderTask task) {
		listener = task;
	}
	
	/**
	 * 
	 */
	public void removeLoaderTask() {
		listener = null;
	}
}

enum OSType {
	WINDOWS32, WINDOWS64, LINUX32, LINUX64, MAC32, MAC64, UNIX32, UNIX64, OTHER
}