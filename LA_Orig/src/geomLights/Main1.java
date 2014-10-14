package geomLights;
import java.io.IOException;
import geom3D.Point3D;
import geom3D.Segment3D;

public class Main1 {
	public static void main(String[] a) {
		GIS_Lights lights = new GIS_Lights();
		for(int i=0;i<5;i++) {
			for(int j=0;j<3;j++) {
				Point3D p = new Point3D(i,j,3);
				GIS_Light l = new GIS_Light(p,0.3,1, null);
				lights.add(l);
			}
		}
		Point3D p1 = new Point3D(10,10,3);
		Point3D p2 = new Point3D(10,11,3);
		Segment3D s1 = new Segment3D(p1,p2);
		GIS_Light l = new GIS_Light(s1.midPoint(),0.2,1/6.0, s1);
		lights.add(l);
		try {
			String name = "Light1.txt";
			String name2 = "Light2.txt";
			lights.write2File(name);
			lights = new GIS_Lights(name);
			lights.write2File(name2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
