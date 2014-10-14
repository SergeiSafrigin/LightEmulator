package geom3D;
/**
 * this class represents a 3D ball, with radius, pointInSide, 
 * @author boaz
 *
 */
public class Circle3D {
	private Point3D _center;
	private double _radius;
	
	public Circle3D(Point3D c, double r) {
		if(c==null | r<0) throw new RuntimeException("Error - wrong, parameters for Circle constractor");		
		this._center = new Point3D(c);
		this._radius = r;
	}
	public Circle3D(Circle3D c) {
		this(c._center,c._radius);
	}
	public boolean contains(Point3D p) {
		return _radius>= p.distance3D(_center);
	}
	public boolean intersect(Circle3D c) {
		return _radius+c._radius>= c._center.distance3D(_center);
	}
	public double getRadius() {return _radius;}
	public Point3D getCenter_ref(){return _center;}
}
