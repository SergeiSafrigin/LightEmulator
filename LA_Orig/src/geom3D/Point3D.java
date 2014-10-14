package geom3D;
public class Point3D {
	private final double EPS = 1e-7;
	private final int FLOAT_NUMBER_PRECISSION = 6;
	public double x, y, z;
	public Point3D(){
		x = y = z = 0;
	}
	public Point3D(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Point3D(Point3D p){
		if( p == null )
			p = new Point3D();			
		x = p.x;
		y = p.y;
		z = p.z;
	}
	public double x(){return x;}
	public double y(){return y;}
	public double z(){return z;}
	/** returns the distance^2!
	 * @param t
	 * @return
	 */
	public double distance_square(Point3D t){
		double dx = this.x-t.x;
		double dy = this.y-t.y;
		double dz = this.z-t.z;
		double ans = dx*dx+dy*dy+dz*dz;
		return ans;
	}
	public double distance3D(Point3D t) {
		double ans = distance_square(t);
		ans = Math.sqrt(ans);
		return ans;
	}
	
	public void add(Point3D p) {
		x += p.x;
		y += p.y;
		z += p.z;
	}
	public void add(double dx, double dy, double dz) {
		x += dx;
		y += dy;
		z += dz;
	}
	public void substract(Point3D p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
	}
	public static double dotProduct(Point3D p, Point3D q) {
		return p.x * q.x + p.y * q.y + p.z * q.z;
	}
	public static Point3D crossProduct(Point3D p, Point3D q) {
		return new Point3D(p.y * q.z - p.z * q.y, p.z * q.x - p.x * q.z, p.x * q.y - p.y * q.x);
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
	public double angleTo(Point3D p){
		double len1Xlen2 = length()*p.length();
		if( Math.abs(len1Xlen2) < EPS )
			return 0;
		return Math.acos(dotProduct(this, p) / len1Xlen2);
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
	public boolean equals(Point3D p) {
		double dx = Math.abs(this.x-p.x);
		double dy = Math.abs(this.y-p.y);
		double dz = Math.abs(this.z-p.z);
		if( dx < EPS && dy < EPS && dz < EPS )
			return true;
		return false;
	}
	public boolean orthogonal(Point3D v) {
		return Math.abs(dotProduct(this,v)) < EPS;
	}
	public boolean colinear(Point3D v) {
		double dp = dotProduct(this,v);
		double nn = this.length() * v.length();
		if (Math.abs(nn) < EPS) 
			return false;
		double frac = dp / nn;
		return Math.abs(Math.abs(frac) - 1) < EPS;
	}
	public boolean coplanar(Point3D u, Point3D v) {
		Point3D d1 = crossProduct(u,v), d2 = new Point3D();
		if (!u.equals(this))
			d2 = crossProduct(u,this);
		else if (!v.equals(this))
			d2 = crossProduct(v,this);
		else
			return false;
		return d1.colinear(d2);
	}
	public String toString() {
		String pcssn = String.format("%%8.%df",FLOAT_NUMBER_PRECISSION);
		String format = String.format("[%s,%s,%s]",pcssn,pcssn,pcssn);
		return String.format(format, x,y,z);
	}
	public Vector3D toVector3D(){
		return new Vector3D(x,y,z);
	}
	public Point3D clone(){
		return new Point3D(this);
	}
}