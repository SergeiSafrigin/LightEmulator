package Simulator;


import geomLights.GIS_Light;
import geomLights.GIS_Lights;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MapParser {

	public static GIS_Lights parse(String fileName){
		GIS_Lights map = new GIS_Lights();
		ArrayList<GIS_Light> lights = map.get_lights();
		BufferedReader fileReader = null;

		try {
			fileReader = new BufferedReader(new FileReader(fileName));
			String line = fileReader.readLine();

			while (line != null) {
				line = fileReader.readLine();

				if (line != null)
					lights.add(GIS_Light.createFromCSV(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return map;
	}
}
