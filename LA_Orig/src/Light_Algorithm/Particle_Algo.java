/**
 * this class include a simple implementation of modified particle algorithm.
 */
package Light_Algorithm;

import geom3D.Ang_Vector;
import geom3D.Geom_Algo;
import geom3D.Line3D;
import geom3D.Point3D;
import geom3D.Segment3D;
import geom3D.Vector3D;
import geomLights.GIS_Lights;

import java.util.ArrayList;

public class Particle_Algo {
	private ArrayList<Particle> _parts;
	
	private Point3D _min, _max;
	
	private int _size;
	private int _stage = 0;
	
	
	public Particle_Algo(int size) {
		if(size<=0) {size=100;}
		_size = size;
		_parts = new ArrayList<Particle>(_size);
	}
	
	public void init(ArrayList<Point3D> ps) {
		_size = ps.size();
		_parts = new ArrayList<Particle>(_size);
		for(int i=0;i<_size;i++) {
			Particle pr = new Particle(ps.get(i));
			_parts.add(pr);
		}
		_stage=0;
	}
	
	public void init(Point3D min, Point3D max, int size) {
		_min = min;
		_max = max;
		_size = size;
		_parts = new ArrayList<Particle>(_size);
		
		for(int i=0;i<size;i++) {
			Point3D p = randPoint(min,max);
			Particle pr = new Particle(p);
			_parts.add(pr);
		}
		
		this._stage=0;
	}
	
	private void resample(Point3D min, Point3D max) {
		Particle best = best();
		double w = best.get_w();
		for(Particle curr: _parts) {
			if(curr.get_w()<w*0.5) {
				curr.set_pos(randPoint(min,max));
				curr.set_w(w/10);
			}
		}
	}
	public static Point3D randPoint(Point3D min, Point3D max){
		double x = rand(min.x, max.x);
		double y = rand(min.y, max.y);
		double z = rand(min.z, max.z);
		return new Point3D(x,y,z);
	}
	
	private static double rand(double min, double max){
		double d = Math.random();
		double dx = max-min;
		return min+dx*d;
	}
	
	public void update(ArrayList<Ang_Vector> an_vecs, GIS_Lights lights) {
		if(an_vecs.size()>= 2) {
			for(int i=0; i < this.size(); i++) {
				Particle p = _parts.get(i);
				computePosition(p,an_vecs,lights);
			//	Point3D c = Lights_Algorithms.findClosestLight(p.get_pos(), an_vecs, lights);
			//	p.update(c, w, 40);
			}
			resample(_min, _max);
			_stage++;
		}
	}
	
	public static void computePosition(Particle part, ArrayList<Ang_Vector> vec_an, GIS_Lights lights) {
		ArrayList<Line3D> lines = new ArrayList<Line3D>();
		Point3D pos = part.get_pos();
		ArrayList<Integer> inds = new ArrayList<Integer>();
		for(int i=0;i<vec_an.size();i++) {
			int ind = Lights_Algorithms.findClosestLight(pos, vec_an.get(i), lights);
			if(!inds.contains(ind)){
				Point3D li = lights.at(ind).getCenter();
				Vector3D vi = vec_an.get(i).toVector();
				Line3D l = new Line3D(li,vi);
				lines.add(l);
			}
		}
		ArrayList<Segment3D> segs = Geom_Algo.getIntersectionSegments(lines);
		// some filter should be implemented here!! say if the distance between a line
		mid(segs, part);
	}

	public Particle get(int i){
		return _parts.get(i);
	}
	
	/**
	 * the distance / weights should be tested and optimized - w.r.t. dt!!!
	 * @param segs
	 * @param lastPos
	 * @return
	 */
	public static void mid2(ArrayList<Segment3D> segs, Particle part) {
		Point3D ans = new Point3D(0,0,0);
		double size = segs.size(), total_w=0;
		double[] we_arr = {0.001, 0.005, 0.02,0.1}; 
		Point3D lastPos = part.get_pos();
		double max = size * we_arr[we_arr.length-1];
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
		double we = total_w/max;
		part.update(ans, we, 40);
	}
	/**
	 * the distance / weights should be tested and optimized - w.r.t. dt!!!
	 * @param segs
	 * @param lastPos
	 * @return
	 */
	public static void mid(ArrayList<Segment3D> segs, Particle part) {
		Point3D ans = new Point3D(0,0,0);
		double size = segs.size();
		for(int i=0;i<size;i++) {
			Point3D m = segs.get(i).midPoint();
			ans.add(m);
		}
		ans.multiplyByScalar(1.0/size);
		eval_W(part, ans, segs);
		Point3D move = computeSmallMove(part,ans, 0.2, 2);
//		part.set_pos(ans);
		part.set_pos(move);
	}
	
	private static Point3D computeSmallMove(Particle part, Point3D new_pos, double max_dist, double max_h) {
		Point3D ans = part.get_pos();
		if(new_pos.z()<max_h && new_pos.z()>0) {
			Vector3D v = new Vector3D(ans, new_pos);
			double dist = v.length();
			if(dist<max_dist) return new_pos;
			v.divideByScalar(dist/max_dist);
			ans.add(v.toPoint3D());
		}
		return ans;
	}
	
	private static void eval_W(Particle p, Point3D new_pos, ArrayList<Segment3D> segs) {
		double dist=0;
		double size = segs.size();
		Point3D p0 = p.get_pos();
		for(int i=0;i<size;i++) {
			Point3D m = segs.get(i).midPoint();
			double d = m.distance3D(new_pos);
			dist +=d;
		}
		dist = dist/size;
		
		double d2 = p0.distance3D(new_pos);
		double z = new_pos.z();
		double w = p.get_w();
		if(z>0&& z<2 && d2<0.5 & dist<0.5) {
			if(w<100) {p.set_w(w*2);}
		}
		else if(d2>1 & dist>1) {
			if(w>0.0001) {p.set_w(w/2);}
		}
		if(z<0 | z>2) {
			if(w>0.0001) {p.set_w(w/2);}
		}
	}
	
	public int getStage() {
		return _stage;
	}
	
	public Particle best() {
		Particle ans = _parts.get(0);
		for(int i=1;i<size(); i++) {
			Particle c = _parts.get(i);
			if(ans.get_w()<c.get_w()) {ans = c;}
		}
		return ans;
	}
	
	public int size (){
		return _parts.size();
	}
}
