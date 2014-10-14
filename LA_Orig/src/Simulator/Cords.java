/**
 * this class represents a simple world <--> screen converter
 * allowing to convert lat/lon cordinations to x,y (pixels / meters).
 * Moreover it allows performing computations in lat / lon in terms of direction (angle),
 * distance (meter)..
 */
package Simulator;

import geom3D.Point3D;

public class Cords {
	public static final double EARTH_RADIUS = 1000*6371;
	
	private Point3D _w_SW;
	private Point3D _w_NE;
	
	//meters 264 * 187  <--> pixels 942*593
	private Point3D _s_LL; 
	private Point3D _s_UR;
	double _w_dx, _w_dy, _s_dx, _s_dy;
	
	public Cords(Point3D w_sw, Point3D w_ne, Point3D s_ll, Point3D s_ur) {
		_w_SW = new Point3D(w_sw);
		_w_NE = new Point3D(w_ne);
		_w_dx = _w_NE.x() - _w_SW.x();
		_w_dy = _w_NE.y() - _w_SW.y();
		_s_LL = new Point3D(s_ll);
		_s_UR = new Point3D(s_ur);
		_s_dx = _s_UR.x() - _s_LL.x();
		_s_dy = _s_UR.y() - _s_LL.y();
	}
	public Point3D w2s(Point3D w){
		double dx = w.x()-_w_SW.x();
		double dy = w.y()-_w_SW.y();
		double nx = dx/_w_dx;
		double ny = dy/_w_dy;
		double dx_s = nx*_s_dx;
		double dy_s = ny*_s_dy;
		Point3D ans = new Point3D(_s_LL.x() +dx_s, _s_LL.y()+dy_s,w.z());
		return ans;
	}
	
	public Point3D s2w(Point3D s) {
		double dx = s.x()-_s_LL.x();
		double dy = s.y()-_s_UR.y();
		double nx = dx/_s_dx;
		double ny = dy/_s_dy;
		double dx_s = nx*_w_dx;
		double dy_s = ny*_w_dy;
		Point3D ans = new Point3D(_w_SW.x() +dx_s, _w_SW.y()+dy_s,s.z());
		return ans;
	}
	/*
	public static Cords W2P() {
		Point3D o = new Point3D(0,593,0);
		Point3D p = new Point3D(942,0,0);
		Point3D w_sw = new Point3D(34.88624399105073,32.11220904377757,0);
		Point3D w_ne = new Point3D(34.88905005277617,32.11390035530312,0);
		return new Cords(w_sw,w_ne,o,p);
		} */
	public static Cords W2P() {
		Point3D o = new Point3D(0,500,0);
		Point3D p = new Point3D(500,0,0);
		Point3D w_sw = new Point3D(-1,-1,0);
		Point3D w_ne = new Point3D(11,11,3);
		return new Cords(w_sw,w_ne,o,p);
		}
	/*
	<north>32.11351368877129</north>
		<south>32.11270509847664</south>
		<east>34.888318231947</east>
		<west>34.88713622388411</west>
	 
	public static Cords W2P_M2() {
		Point3D o = new Point3D(0,677,0);
		Point3D p = new Point3D(952,0,0);
		Point3D w_sw = new Point3D(34.88713622388411,32.11270509847664,0);
		Point3D w_ne = new Point3D(34.888318231947,32.11351368877129,0);
		return new Cords(w_sw,w_ne,o,p);
		}
	public static Cords W2M() {
		Point3D o = new Point3D(0,0,0);
		Point3D p = new Point3D(10,10,3);
		Point3D w_sw = new Point3D(34.88624399105073,32.11220904377757,0);
		Point3D w_ne = new Point3D(34.88905005277617,32.11390035530312,0);
		return new Cords(o,p,w_sw,w_ne);
	}
	public static Cords W2M_M2() {
		Point3D o = new Point3D(0,0,0);
		Point3D p = new Point3D(10,10,0);
		Point3D w_sw = new Point3D(34.88713622388411,32.11270509847664,0);
		Point3D w_ne = new Point3D(34.888318231947,32.11351368877129,0);
		return new Cords(o,p,w_sw,w_ne);
	}
*/
	
	/**
	 * this method computes the flat world distance vector between two global points (lat-north, lon-east, alt-above-sea) 
	 * assuming the two points are relatively close .
	 * @param ll1
	 * @param ll2
	 * @return
	 */
	public static double[] flatWorldDist(double[] ll1, double[] ll2) {
		double[] ans = new double[3];
		double dx = ll2[0]-ll1[0]; // delta lon east
		double dy = ll2[1]-ll1[1]; // delta lat north
		double dz = ll2[2]-ll1[2]; // delta alt
		if(Math.abs(dx)>0.1 | Math.abs(dy)>0.1) {
			throw new RuntimeException ("ERR: the two latlon points are too far - can not be assumed as flat world");
		}
		double x = EARTH_RADIUS * Math.toRadians(dx) * Math.cos(Math.toRadians(ll1[0]));
		double y = EARTH_RADIUS * Math.toRadians(dy); 
		ans[0] = x; ans[1]=y; ans[2] = dz;
		return ans;
	}
	public static Point3D flatWorldDist(Point3D ll1, Point3D ll2) {
		return conv_back(flatWorldDist(conv(ll1), conv(ll2)));
	}
	/**
	 * this function computes the azimuth and distance (and dz) in degrees and meters between
	 * two lat/lon points. 
	 * NOTE: this function is used by many navigation higher level function do NOT change it
	 * unless you fully understand - use all Junits - to make sure the change is not adding bugs.
	 *  
	 * @param ll1 first GPS cords
	 * @param ll2 second GPS cords
	 * @return [azm,dist,dz];
	 */
	public static double[] azmDist(double[] ll1, double[] ll2){
		double[] ans = new double[3];
		double[] vec = flatWorldDist(ll1,ll2);
		double dist = Math.sqrt(vec[0]*vec[0]+vec[1]*vec[1]); // 2D for now
		double ang = angXY(vec[0], vec[1]);
				
		ans[0] = ang;
		ans [1]= dist;
		ans[2] = vec[2];
		return ans;
	}
	public static Point3D azmDist(Point3D p1, Point3D p2) {
		return conv_back(azmDist(conv(p1), conv(p2)));
	}
	private static Point3D conv_back(double[] p) {
		return new Point3D(p[0],p[1],p[2]);
	}
	private static double[] conv(Point3D p1) {
		double[] a = new double[3];
		a[0] = p1.x(); a[1] = p1.y(); a[2] = p1.z();
		return a;
	}
	/**
	 * This method gets a Lat-Lon-Alt global coordination and a vector (in meters) <x,y,z> (east, north, up)
	 * it returns a new Lat-Lon-Alt global coordination which is the offset of the inpot point with the vector.
	 * Assumes the vector is smaller than 100km).
	 * @param ll1
	 * @param vec
	 * @return
	 */
	public static double[] offsetLatLonAlt(double[] ll1, double[] vec) {
		double[] ans = new double[3];
		double dz = vec[2]; // delta alt
		if(Math.abs(vec[0])>100000 | Math.abs(vec[1])>100000) {
			throw new RuntimeException ("ERR: the offset vectr os too big (more than 100km) - can not be assumed as flat world");
		}
		double lon = vec[0]/(EARTH_RADIUS * Math.cos(Math.toRadians(ll1[0])));
		double lat = vec[1]/EARTH_RADIUS; 
		ans[0] = ll1[0]+Math.toDegrees(lon); ans[1]=ll1[1]+Math.toDegrees(lat); ans[2] = ll1[2]+dz;
		return ans;
	}
	public static double[] offsetLatLonAzmDist(double[] ll1, double azm, double dist) {
		double[] vec = azmDist2cords(azm,dist);
		return offsetLatLonAlt(ll1,vec);
	}
	public static Point3D offsetLatLonAzmDist(Point3D ll1, double azm, double dist) {
		double[] a = offsetLatLonAzmDist(conv(ll1), azm,dist);
		return conv_back(a);
	}
	public  static Point3D offsetLatLonAlt(Point3D ll1, Point3D vec) {
		return conv_back(offsetLatLonAlt(conv(ll1), conv(vec)));
	}
	public static void main(String[] a) {
		double[] ll1 = {34.803395,32.166916,100};
		double[] ll2 = {34.81401,32.166917,120};
		double[] vec = Cords.flatWorldDist(ll1, ll2);
		
		double[] ll2a = Cords.offsetLatLonAlt(ll1,vec);
		System.out.println("Vec:"+vec[0]+", "+vec[1]+", "+vec[2]);
		System.out.println("ll2a:"+ll2a[0]+", "+ll2a[1]+", "+ll2a[2]);
		if(Math.abs(vec[1])>1 | Math.abs(vec[0]-1000)>1 || Math.abs(vec[2]-20)>0.001 ) {
			System.err.println("Wrong Coords to dVec convertor");
		}
		if(Math.abs(ll2[0]-ll2a[0])>0.0001 | Math.abs(ll2[1]-ll2a[1])>0.0001 || Math.abs(ll2[2]-ll2a[2])>0.0001 ) {
			System.err.println("Wrong Coords to dVec2 convertor");
		}
		
	}
	/**
	 * this static function compute the angle from 0,0 to x,y assuming north is 0, east is 90, south =180, west = 270
	 * @return
	 */
	public static double angXY(double dx,double dy){
		double a0 = Math.atan2(dy, dx);
		//double a1 = Math.toDegrees(a0);
		//double  ans = 90-a1;
		//if(a1>90) ans =450 - a1;
		double ans = rad2Deg(a0);
		return ans;
	}
	public static double[] azmDist2cords(double azm,double dist){
		//double  a = 90-azm;
		//if(azm>270) a = 450 - azm;
		//a = Math.toRadians(a);
		double  a = deg2Rad(azm);
		double[] ans = new double[3];
		ans[0] = Math.cos(a)*dist;
		ans[1] = Math.sin(a)*dist;
		ans[2] = 0;
		return ans;
	}
	public static double deg2Rad(double deg) {
		double  a = 90-deg;
		if(deg>270) a = 450 - deg;
		a = Math.toRadians(a);
		return a;
	}
	public static double rad2Deg(double rad) {
		double a1 = Math.toDegrees(rad);
		double  ans = 90-a1;
		if(a1>90) ans =450 - a1;
		return ans;
	}
}
