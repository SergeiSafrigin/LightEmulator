package geom3D;

public class Segment3D {
	private Point3D _p1,_p2;
	public Segment3D(Point3D p1, Point3D p2){
		_p1 = new Point3D(p1);
		_p2 = new Point3D(p2);
	}
	public Point3D p1_ref(){return _p1;}
	public Point3D p2_ref(){return _p2;}
	public double length() {return _p1.distance3D(_p2);}
	public Point3D midPoint() {
		return new Point3D((_p1.x+_p2.x)/2,(_p1.y+_p2.y)/2, (_p1.z+_p2.z)/2);} 
	public String toString() {return "Segment p1: "+_p1+"  p2: "+_p2;}
}
