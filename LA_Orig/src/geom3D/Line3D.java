package geom3D;
public class Line3D {
	public Point3D  a;
	public Vector3D v;
	public Line3D(Point3D a, Vector3D v){
		this.a = a;
		this.v = v;
	}
	public Line3D(Point3D p1, Point3D p2) {
		this(p1, new Vector3D(p1,p2));
	}
	public Point3D getP2() {
		Point3D ans = new Point3D(a);
		ans.add(v.toPoint3D());
		return ans;
	}
	public String toString(){
		return "a=" + a + ", v=" + v;
	}
}
