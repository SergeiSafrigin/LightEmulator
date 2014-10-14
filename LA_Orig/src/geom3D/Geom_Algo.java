package geom3D;

import java.util.ArrayList;

/**
 * this class contains the basic GEOM algorithms
 * @author boaz
 *
 */
public class Geom_Algo {
	public static final int MAX_LIGHTS_IN_FRAME = 10;
	private static final double EPS = Math.pow(10, -7);
	
	public static Point3D CenterOfMidSegmentBetweenTwoLines(Line3D l1, Line3D l2) {
		return getIntersectionSegment(l1,l2).midPoint();
	}
	public static Point3D CenterOfMidSegmentBetweenTwoLines(Point3D p1, Point3D p2, Point3D p3, Point3D p4){
		return getIntersectionSegment(p1,p2,p3,p4).midPoint();
	}
	public static Segment3D getIntersectionSegment(Line3D l1, Line3D l2) {
		return getIntersectionSegment(l1.a, l1.getP2(), l2.a, l2.getP2());	
	}
	/** computes all intersection */
	public static ArrayList<Segment3D> getIntersectionSegments(ArrayList<Line3D> lines) {
		ArrayList<Segment3D> ans = new ArrayList<Segment3D>();
		if(lines==null ||lines.size()<2) throw new RuntimeException("Wrong parameter to the getIntersectionSegments method - should be an ArrayList<lines> with 2 or more lines!");
		int size = Math.min(lines.size(),MAX_LIGHTS_IN_FRAME);
		for(int a=0;a<size-1;a++) {
			for(int b=a+1;b<size;b++) {
				Segment3D seg = getIntersectionSegment(lines.get(a), lines.get(b));
				ans.add(seg);
			}
		}
		return ans;
	}	
	public static double RMS_ERR(ArrayList<Segment3D> segs) {return  RMS_ERR(segs,2.0);}
	public static double RMS_ERR(ArrayList<Segment3D> segs, double exp) {
		double ans =0, size = segs.size();
		for(int i=0;i<size;i++) {
			ans+=Math.pow(segs.get(i).length(),exp);
		}
		ans=ans/size;
		ans = Math.pow(ans,1/exp);
		return ans;
	}
	public static Point3D mid(ArrayList<Segment3D> segs) {
		Point3D ans = new Point3D(0,0,0);
		double size = segs.size();
		for(int i=0;i<size;i++) {
			ans.add(segs.get(i).midPoint());
		}
		ans.multiplyByScalar(1/size);
		return ans;
	}
	/**
	   Calculate the line segment PaPb that is the shortest route between
	   two lines P1P2 and P3P4. Calculate also the values of mua and mub where
	      Pa = P1 + mua (P2 - P1)
	      Pb = P3 + mub (P4 - P3)
	   Return null if no solution exists.
	   @see http://astronomy.swin.edu.au/~pbourke/geometry/lineline3d/
	   @see http://www.youtube.com/watch?v=HC5YikQxwZA
	 */
	public static Segment3D getIntersectionSegment(Point3D p1, Point3D p2, Point3D p3, Point3D p4){
		
		double d1343,d4321,d1321,d4343,d2121;
		double numer,denom;
		//double EPS = 2.204*Math.pow(10, -7);
		double p13x, p13y, p13z, p21x, p21y, p21z, p43x, p43y, p43z;
		
		p13x = p1.x() - p3.x();
		p13y = p1.y() - p3.y();
		p13z = p1.z() - p3.z();
		p43x = p4.x() - p3.x();
		p43y = p4.y() - p3.y();
		p43z = p4.z() - p3.z();
		if (Math.abs(p43x) < EPS && Math.abs(p43y) < EPS && Math.abs(p43z) < EPS)
			return null;
		p21x = p2.x() - p1.x();
		p21y = p2.y() - p1.y();
		p21z = p2.z() - p1.z();
		if (Math.abs(p21x) < EPS && Math.abs(p21y) < EPS && Math.abs(p21z) < EPS)
			return null;
		
		d1343 = p13x * p43x + p13y * p43y + p13z * p43z;
		d4321 = p43x * p21x + p43y * p21y + p43z * p21z;
		d1321 = p13x * p21x + p13y * p21y + p13z * p21z;
		d4343 = p43x * p43x + p43y * p43y + p43z * p43z;
		d2121 = p21x * p21x + p21y * p21y + p21z * p21z;
		
		denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < EPS)
			return null;
		numer = d1343 * d4321 - d1321 * d4343;
		
		double mua = numer / denom;
		double mub = (d1343 + d4321 * mua) / d4343;
		
		double x1 = p1.x() + mua * p21x;
		double y1 = p1.y() + mua * p21y;
		double z1 = p1.z() + mua * p21z;
		double x2 = p3.x() + mub * p43x;
		double y2 = p3.y() + mub * p43y;
		double z2 = p3.z() + mub * p43z;
		Point3D pa = new Point3D(x1,y1,z1);
		Point3D pb = new Point3D(x2,y2,z2);
		return new Segment3D(pa,pb);
	}
	/** NOT IN USE!!!! 
	public static Point3D CenterOfMidSegmentBetweenTwoLines_old(Line3D line1, Line3D line2){
		double d1343,d4321,d1321,d4343,d2121;
		double numer,denom;

		Point3D p1 = line1.a.clone();
		Point3D p2 = line1.a.clone();
		p2.add(line1.v.toPoint3D());

		Point3D p3 = line2.a.clone();
		Point3D p4 = line2.a.clone();
		p4.add(line2.v.toPoint3D());

		Vector3D p43 = p3.toVector3D();
		p43.substract(p4.toVector3D());

		Vector3D p13 = p3.toVector3D();
		p13.substract(p1.toVector3D());

		Vector3D p21 = p1.toVector3D();
		p21.substract(p2.toVector3D());

		d1343 = p13.x * p43.x + p13.y * p43.y + p13.z * p43.z;
		d4321 = p43.x * p21.x + p43.y * p21.y + p43.z * p21.z;
		d1321 = p13.x * p21.x + p13.y * p21.y + p13.z * p21.z;
		d4343 = p43.x * p43.x + p43.y * p43.y + p43.z * p43.z;
		d2121 = p21.x * p21.x + p21.y * p21.y + p21.z * p21.z;

		denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < EPS)
			return null; // Infinity point 
		
		numer = d1343 * d4321 - d1321 * d4343;

		double mua = numer / denom;
		double mub = (d1343 + d4321 * (mua)) / d4343;

		Point3D pa = new Point3D(p1.x + mua * p21.x, p1.y + mua * p21.y, p1.z + mua * p21.z);
		Point3D pb = new Point3D(p3.x + mub * p43.x, p3.y + mub * p43.y, p3.z + mub * p43.z);
// bug fixed by Boaz 14/5/2014
		Point3D mid = new Point3D((pa.x+pb.x)/2, (pa.y+pb.y)/2, (pa.z+pb.z)/2);
		return mid;		
	}
	*/
	/**
	 * this simple function return the location of the observer from a known point, angle & distance.
	 * Please use only this function - in order to simplify the testing!
	 * @param light - location of the point
	 * @param yaw - horizontal angle - north (up) = 0 (deg')
	 * @param elev - elevation (deg') 
	 * @param dist - in meters
	 * @return
	 */
	public static Point3D azm_Dist(Point3D l, double yaw, double elev, double dist) {
		Point3D ans = null;
		Vector3D v = azm2vector(yaw, elev, dist);
		ans = new Point3D(l);
		ans.add(-v.x, -v.y, -v.z);
		return ans;
	}
	
	public static Point3D azm_dz(Point3D l, double yaw, double elev, double dz) {
		Point3D ans = null;
		double dist = 50;
		if(elev>2) { // above horizontal
			double elevR = Math.toRadians(elev);
			dist = dz/Math.sin(elevR);
		}
		Vector3D v = azm2vector(yaw, elev, dist);
		ans = new Point3D(l);
		ans.add(-v.x, -v.y, -v.z);
		return ans;
	}
	
	/**
	 * this function convert a GPS like angles to a normalized 3D vector. It assumes that the vector starts at 0,0,0 and has an elevation 
	 * angle of north=0, east=90 and up angle (deg', 0- assumes horisontal, 90 up).
	 * @param angUp
	 * @param angNorth 
	 */
	public static Vector3D azm2vector(double azm, double elev, double dist) {
		Vector3D ans = null;
		double elevR = Math.toRadians(elev);
		double azmR = Math.toRadians(azm);
		double z = dist * Math.sin(elevR);
		double xy = dist * Math.cos(elevR);
		double y = xy * Math.cos(azmR);
		double x = xy * Math.sin(azmR);
		ans = new Vector3D(x,y,z);
		return ans;
	}
}
