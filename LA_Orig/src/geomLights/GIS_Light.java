package geomLights;

import geom3D.Point3D;
import geom3D.Segment3D;

/**
 * this class represent a light source as presented in the DB - as a GIS elemnt
 * @author boaz
 *
 */
public class GIS_Light {
	private Point3D _center;
	private Segment3D _seg;
	private int _kind;
	private boolean _vis; // for debug only!
	private double _diameter;
	private double _fatness; // 1 round , 0.707 square, 1/6 standard 0.2*1.2 neon light
	private int _id;
	private static int _count=0;
	public static final int FAT=0, NARROW=1, OTHER=2;
	
	public GIS_Light(Point3D center, double d, double fat, Segment3D s) {
		_center = new Point3D(center);
		_diameter = d;
		_fatness = fat;
		_kind = FAT;
		_id =_count++;
		if(s!=null) {
			_kind = NARROW;
			_seg = s;
		}
	}
	public String toFile() {
		String ans = "";
		Point3D c = this.getCenter();
		ans+=this.get_id()+" "+c.x()+" "+c.y()+" "+c.z()+" "+this.get_diameter()+
				" "+this.get_fatness();
		if(this.getKind()==NARROW) {
			Point3D c1 = this.get_seg().p1_ref();
			Point3D c2 = this.get_seg().p2_ref();
			ans+=" "+c1.x()+" "+c1.y()+" "+c1.z()+" "+c2.x()+" "+c2.y()+" "+c2.z();
		}
		return ans;
	}
	public static GIS_Light create(String s) {
		String[] data = s.split(" ");
		int id = Integer.parseInt(data[0]);
		double x = Double.parseDouble(data[1]);
		double y = Double.parseDouble(data[2]);
		double z = Double.parseDouble(data[3]);
		Point3D c = new Point3D(x,y,z);
		double d = Double.parseDouble(data[4]);
		double f = Double.parseDouble(data[5]);
		Segment3D seg = null;
		if(data.length>6) {
			x = Double.parseDouble(data[6]);
			y = Double.parseDouble(data[7]);
			z = Double.parseDouble(data[8]);
			Point3D c1 = new Point3D(x,y,z);
			x = Double.parseDouble(data[9]);
			y = Double.parseDouble(data[10]);
			z = Double.parseDouble(data[11]);
			Point3D c2 = new Point3D(x,y,z);
			seg = new Segment3D(c1,c2);
		}
		GIS_Light ans = new GIS_Light(c,d,f,seg);
		ans._id=id;
		return ans;
	}
	
	public static GIS_Light createFromCSV(String s) {
		String[] data = s.split(",");
		int id = Integer.parseInt(data[0]);
		double x = Double.parseDouble(data[1]);
		double y = Double.parseDouble(data[2]);
		double z = Double.parseDouble(data[3]);
		Point3D c = new Point3D(x,y,z);
		double d = Double.parseDouble(data[4]);
		double f = Double.parseDouble(data[5]);
		Segment3D seg = null;
		if(data.length>6) {
			x = Double.parseDouble(data[6]);
			y = Double.parseDouble(data[7]);
			z = Double.parseDouble(data[8]);
			Point3D c1 = new Point3D(x,y,z);
			x = Double.parseDouble(data[9]);
			y = Double.parseDouble(data[10]);
			z = Double.parseDouble(data[11]);
			Point3D c2 = new Point3D(x,y,z);
			seg = new Segment3D(c1,c2);
		}
		GIS_Light ans = new GIS_Light(c,d,f,seg);
		ans._id=id;
		return ans;
	}
	
	public int getKind() {return _kind;}
	public Segment3D get_seg() {
		return _seg;
	}
	public Point3D getCenter() {return _center;}
	public double get_diameter() {
		return _diameter;
	}
	public void set_diameter(double _diameter) {
		this._diameter = _diameter;
	}
	public double get_fatness() {
		return _fatness;
	}
	public void set_fatness(double _fatness) {
		this._fatness = _fatness;
	}

	public int get_id() {
		return _id;
	}
	public boolean is_vis() {
		return _vis;
	}
	public void set_vis(boolean _vis) {
		this._vis = _vis;
	}

}
