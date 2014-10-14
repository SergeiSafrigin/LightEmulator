package geom3D;
public class Vector3D {
	private final double EPS = 1e-7;
	private final int FLOAT_NUMBER_PRECISSION = 6;
	public double x, y, z;
	public Vector3D(){
		x = y = z = 0;
	}
	public Vector3D(Point3D p1, Point3D p2) {
		this(p2.x-p1.x, p2.y-p1.y, p2.z-p1.z);
	}
	public Vector3D(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Vector3D(Vector3D p){
		if( p == null )
			p = new Vector3D();
		x = p.x;
		y = p.y;
		z = p.z;
	}
	
	public void add(double dx, double dy, double dz) {
		x += dx;
		y += dy;
		z += dz;
	}
	public void add(Vector3D p) {
		x += p.x;
		y += p.y;
		z += p.z;
	}
	
	public void substract(Vector3D p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
	}
	public static double dotProduct(Vector3D p, Vector3D q) {
		return p.x * q.x + p.y * q.y + p.z * q.z;
	}
	public static Vector3D crossProduct(Vector3D p, Vector3D q) {
		return new Vector3D(p.y * q.z - p.z * q.y, p.z * q.x - p.x * q.z, p.x * q.y - p.y * q.x);
	}
	public void multiplyByScalar(double scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
	}
	public void divideByScalar(double scalar) {
		if( Math.abs(scalar) > EPS ){
			x /= scalar;
			y /= scalar;
			z /= scalar;
		}
	}
	public double length() {
		return Math.sqrt(dotProduct(this,this));
	}
	public double lengthOrth() {
		double ox = Math.abs(x);
		double oy = Math.abs(y);
		double oz = Math.abs(z);
		return Math.max(ox, Math.max(oy, oz));
	}
	public void normalyze() {
		double length = length();
		if (length > EPS ) {
			x /= length;
			y /= length;
			z /= length;
		}
	}
	public boolean equals(Vector3D p) {
		double dx = Math.abs(this.x-p.x);
		double dy = Math.abs(this.y-p.y);
		double dz = Math.abs(this.z-p.z);
		if( dx < EPS && dy < EPS && dz < EPS )
			return true;
		return false;
	}
	public boolean orthogonal(Vector3D v) {
		return Math.abs(dotProduct(this,v)) < EPS;
	}
	public boolean colinear(Vector3D v) {
		double dp = dotProduct(this,v);
		double nn = this.length() * v.length();
		if (Math.abs(nn) < EPS) 
			return false;
		double frac = dp / nn;
		return Math.abs(Math.abs(frac) - 1) < EPS;
	}
	public boolean coplanar(Vector3D u, Vector3D v) {
		Vector3D d1 = crossProduct(u,v), d2 = new Vector3D();
		if (!u.equals(this))
			d2 = crossProduct(u,this);
		else if (!v.equals(this))
			d2 = crossProduct(v,this);
		else
			return false;
		return d1.colinear(d2);
	}
	public double angleTo(Vector3D v){
		double len1Xlen2 = length()*v.length();
		if( Math.abs(len1Xlen2) < EPS )
			return 0;		
		return Math.acos(dotProduct(this, v) / len1Xlen2);
	}
	public String toString() {
		String pcssn = String.format("%%8.%df",FLOAT_NUMBER_PRECISSION);
		String format = String.format("[%s,%s,%s]",pcssn,pcssn,pcssn);
		return String.format(format, x,y,z);
	}
	public Point3D toPoint3D(){
		return new Point3D(x,y,z);
	}
	public Vector3D clone(){
		return new Vector3D(this);
	}
}