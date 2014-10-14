package geomLights;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * this class is a simple collection of GIS_Light
 * @author boaz
 *
 */
public class GIS_Lights {
	private ArrayList<GIS_Light> _lights;
	
	public GIS_Lights() {
		_lights = new ArrayList<GIS_Light>();
	}
	
	public GIS_Lights(String name) throws Exception {
		this();
		FileReader fr = new FileReader(name);
		BufferedReader is = new BufferedReader(fr);
		String s = is.readLine();
		while(s!=null) {
			GIS_Light g = GIS_Light.create(s);
			_lights.add(g);
			s = is.readLine();
		}
		is.close();
	}
	
	public void add(GIS_Light l) {
		_lights.add(l);
	}
	public void write2File(String name) throws IOException {
		FileWriter fw = new FileWriter(name);
		PrintWriter os = new PrintWriter(fw);
		for(int i=0;i<size();i++) {
			os.println(at(i).toFile());
		}
		os.close();fw.close();
	}
	public int size() {return _lights.size();}
	public GIS_Light at(int i) {return _lights.get(i);} 
	public ArrayList<GIS_Light> get_lights() {
		return _lights;
	}
	
}
