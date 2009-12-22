import java.io.*;
import java.util.HashMap;

public class StateStore {
	public static void writeState(Calc3DState state, String file) throws IOException {
		writeState(state, new File(file));
	}
	
	public static void writeState(Calc3DState state, File file) throws IOException {
		//TODO write state to XML file
	}
	
	public static Calc3DState readState(String file) throws IOException {
		return readState(new File(file));
	}
	
	public static Calc3DState readState(File file) throws IOException {
		//TODO read state from XML file
		return null;
	}
	
	class Calc3DState {
		private HashMap<String, String> settings = new HashMap<String, String>();
		
		public Calc3DState() { }
		
		public void addValue(String key, String value) {
			settings.put(key, value);
		}
		
		public String getValue(String key) {
			return settings.get(key);
		}
	}
}
