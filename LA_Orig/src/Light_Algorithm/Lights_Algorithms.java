/**
 * this class contains a set of registration and positioning algorithms
 * for lights based indoor positioning
 */
package Light_Algorithm;

import java.util.ArrayList;

import geom3D.Ang_Vector;
import geom3D.Geom_Algo;
import geom3D.Line3D;
import geom3D.Point3D;
import geom3D.Segment3D;
import geom3D.Vector3D;
import geomLights.GIS_Light;
import geomLights.GIS_Lights;

public class Lights_Algorithms {
	/**
	 * this static function is an important sub-function in any light algorithm:
	 * it evaluate the if a tested position is a valid solution - w.r.t. the set of 3D rays to the lights.
	 * The registration is based on angular diff - yet should also use the distance (radius) filter.
	 * @return
	 */
	public static double accuracyCheck(Point3D pos, ArrayList<Ang_Vector> angles, GIS_Lights lights) {
		double ans = -1;
		
		return ans;
		
	}
	/**
	 * this function gets a set of GIS Lights, an approximated Position, and an angular vector,
	 * it finds the light with the vector which is closest to the vector.
	 * @return
	 */
	public static int findClosestLight(Point3D pos, Ang_Vector vec_an, GIS_Lights lights) {
		int ans=-1;
		Vector3D vec = vec_an.toVector();
		if(lights!=null && lights.size()>0) {
			double min_diff = 361;
			for(int i=0;i<lights.size();i++) {
				Vector3D c = new Vector3D(pos, lights.at(i).getCenter()); 
				double da = Math.abs(vec.angleTo(c));
				if(da<min_diff) {ans=i; min_diff = da;}
			}
		}
		return ans;
	}
	
	public static Point3D computePosition(Point3D pos, ArrayList<Ang_Vector> vec_an, GIS_Lights lights) {
		ArrayList<Line3D> lines = new ArrayList<Line3D>();
		for(int i=0;i<vec_an.size();i++) {
			int ind = findClosestLight(pos, vec_an.get(i), lights);
			Point3D li = lights.at(ind).getCenter();
			Vector3D vi = vec_an.get(i).toVector();
			Line3D l = new Line3D(li,vi);
			lines.add(l);
		}
		ArrayList<Segment3D> segs = Geom_Algo.getIntersectionSegments(lines);
		// some filter should be implemented here!! say if the distance between a line
		return mid(segs, pos);
	}
	/**
	 * the distance / weights should be tested and optimized - w.r.t. dt!!!
	 * @param segs
	 * @param lastPos
	 * @return
	 */
	public static Point3D mid(ArrayList<Segment3D> segs, Point3D lastPos) {
		Point3D ans = new Point3D(0,0,0);
		double size = segs.size(), total_w=0;
		for(int i=0;i<size;i++) {
			Point3D m = segs.get(i).midPoint();
			double dist = m.distance3D(lastPos);
			double norm = 0.001;
			if(dist<1) norm=norm*4;
			if(dist<0.5) norm = norm*4;
			if(dist<0.25) norm = norm*4;
			m.multiplyByScalar(norm);
			ans.add(m);
			total_w+=norm;
		}
		ans.multiplyByScalar(1/total_w);
		return ans;
	}
	
	
	/**
	 * this static function search the GIS-Lights DB for the most suitable light w.r.t. the 
	 * given position, angle to light and the light diameter (distance)
	 * @param pos
	 * @param IL
	 * @param lights
	 * @return
	 */
//	public static int findClosestLight(Point3D pos, ImageLight IL, GIS_Lights lights) {}
	private static ArrayList<Segment3D> allCouples_naive_algo(GIS_Lights map, ArrayList<Ang_Vector> vis_lt){
		ArrayList<Segment3D> ans = new ArrayList<Segment3D>();
		if(vis_lt.size()>=2) {
			for(int a=0;a<map.size();a++) {
				for(int b=0;b<map.size();b++) {
					if(a!=b) {
						GIS_Light la = map.at(a);
						GIS_Light lb = map.at(b);
						Point3D pa = la.getCenter(), pb = lb.getCenter();
						Vector3D va = vis_lt.get(0).toVector(), vb = vis_lt.get(1).toVector();
						Point3D ma = new Point3D(pa); ma.add(va.x, va.y, va.z);
						Point3D mb = new Point3D(pb); mb.add(vb.x, vb.y, vb.z);
						Line3D l1 = new Line3D(ma,pa);
						Line3D l2 = new Line3D(mb,pb);
						Segment3D seg = Geom_Algo.getIntersectionSegment(l1, l2);
						if(seg!=null) ans.add(seg);
					}
				}
			}
				
		}
		return ans;
	}
	private static Point3D findBestSegment(ArrayList<Segment3D> segs) {
		if(segs ==null || segs.size()==0) return null;
		Point3D ans = segs.get(0).midPoint();
		double min_len = segs.get(0).length();
		for(int i=1;i<segs.size();i++) {
			if(segs.get(i).length()<min_len) {
				ans = segs.get(i).midPoint();
				min_len =  segs.get(i).length();
			}
		}
		return ans;
	}
	
	public static Point3D findBestSegment2(ArrayList<Segment3D> segs, Point3D pos) {
		if(segs ==null || segs.size()==0) return null;
		Point3D ans = segs.get(0).midPoint();
		for(int i=1;i<segs.size();i++) {
			Point3D curr = segs.get(i).midPoint();
			if(ans.distance3D(pos)>curr.distance3D(pos)) {
				ans = curr;
			}
		}
		return ans;
	}
	
	public static ArrayList<Ang_Vector> VisLights(GIS_Lights map, Position_State pos){
		ArrayList<Ang_Vector> ans = new ArrayList<Ang_Vector>();
		Point3D me = pos.get_pos();
		Ang_Vector ae = pos.get_ori();
		for(int i=0;i<map.size();i++) {
			GIS_Light li = map.at(i);
			Point3D cli = li.getCenter();
			Ang_Vector av = new Ang_Vector(me,cli);
			av._azm += rand(-2,2);
			av._elev += rand(-1,1);
			if(azmDiff(av._azm,ae._azm)<30 && Math.abs(av._elev-ae._elev)<22) {
				ans.add(av);
				li.set_vis(true);			
			//	System.out.println("Light in angle: "+i+"  "+cli+"  "+av);
			}
			else{li.set_vis(false);}
		}
		return ans;
	}
	public static double rand(double min, double max){
		double d = Math.random();
		double dx = max-min;
		return min+dx*d;
	}
	public static double azmDiff(double a1, double a2) {
		double max = Math.max(a1, a2);
		double min = Math.min(a1, a2);
		double da = max-min;
		double da2 = (max-360)-min;
		da = Math.min(da, Math.abs(da2));
		return da;
	}
	
	
}

