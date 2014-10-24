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
		
		for(int i=0;i<size()/2;i++) {
			int ind = (int)ParticleFilterUtils.nextRandom(0,size());
			Particle curr = _parts.get(ind);
			if(curr.get_w()<w*0.5) {
				curr.set_pos(ParticleFilterUtils.randPoint(min,max));
				curr.set_w(w/100);
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
	
	public void update(ArrayList<Ang_Vector> an_vecs, GIS_Lights lightsMap) {
		if(an_vecs.size()>= 2) {
			for(Particle particle: _parts) {
				computePosition(particle, an_vecs, lightsMap);
			}
			resample(_min, _max);
			_stage++;
		} else if (an_vecs.size() == 1) {
			Ang_Vector an_vec = an_vecs.get(0);
			for(Particle particle: _parts) {
				computePosition1(particle, an_vec, lightsMap);
			}
		}
	}
	
	public void computePosition(Particle part, ArrayList<Ang_Vector> an_vecs, GIS_Lights lights) {
		ArrayList<Line3D> lines = new ArrayList<Line3D>();
		Point3D pos = part.get_pos();
		ArrayList<Integer> inds = new ArrayList<Integer>();
		for(Ang_Vector an_vec: an_vecs) {
			int ind = Lights_Algorithms.findClosestLight(pos, an_vec, lights);
			if(!inds.contains(ind)){
				Point3D li = lights.at(ind).getCenter();
				Vector3D vi = an_vec.toVector();
				Line3D l = new Line3D(li,vi);
				lines.add(l);
			}
		}
		ArrayList<Segment3D> segs = Geom_Algo.getIntersectionSegments(lines);
		// some filter should be implemented here!! say if the distance between a line
		if (segs.size() > 0)
			mid(segs, part);
//		computeSmallMove(part);
	}
	
	public void computePosition1(Particle part, Ang_Vector vec_an, GIS_Lights lights) {
		Point3D pos = part.get_pos();
		int ind = Lights_Algorithms.findClosestLight(pos, vec_an, lights);
		Point3D li = lights.at(ind).getCenter();
		Vector3D vi = vec_an.toVector();
		Line3D l = new Line3D(li,vi);
		// some filter should be implemented here!! say if the distance between a line
		mid1(l, part);
	}
	
	public void mid1(Line3D l, Particle part) {
		Point3D p1 = l.a;
		Vector3D v = l.v;
		Point3D ans = new Point3D(p1);
		if(v.z!=0) {
			Point3D pos = part.get_pos();
			// poz.z = a.z+M*v.z; ==> M=(pos.z-a.z) / v.z
			double m = (pos.z-p1.z) / v.z;
			v.multiplyByScalar(m);		
			ans.add(v.toPoint3D());
		//eval_W(part, ans, segs);
		Point3D move = computeSmallMove(part,ans, 0.2, 2.0);
		part.set_pos(move);
		}
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
	private void mid(ArrayList<Segment3D> segs, Particle part) {
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
	
	private Point3D computeSmallMove(Particle part, Point3D new_pos, double max_dist, double max_h) {
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
	
	private void eval_W(Particle p, Point3D new_pos, ArrayList<Segment3D> segs) {
		double dist=0;
		double size = segs.size();
		Point3D p0 = p.get_pos();
		for(Segment3D seg: segs) {
			Point3D m = seg.midPoint();
			double d = m.distance3D(new_pos);
			dist +=d;
		}
		dist = dist/size;

		double d2 = p0.distance3D(new_pos);
		double z = new_pos.z();
		double w = p.get_w();
		double dd = d2+dist;
		if(z>0&& z<2 && d2<0.3 & dist<0.5) {
			if(dd<=0.1) {dd= 0.1;}
			dd = Math.pow(dd, 0.3);
			if(w<ParticleFilterUtils.MAX_W) {p.set_w(w/dd);}
		} else{
			if(w>ParticleFilterUtils.MIN_W*10) {
				p.set_w(w/1.1);
			}
		}
		
		if(d2>1 & dist>1) {
			if(dd>10) {dd=10;}
			if(w>ParticleFilterUtils.MIN_W) {p.set_w(w/dd);}
		}
		if(z<0 | z>2) {
			if(w>ParticleFilterUtils.MIN_W) {p.set_w(w/2);}
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
	
	public static Particle best(ArrayList<Particle> particles){
		Particle ans = particles.get(0);
		for(int i = 1; i < particles.size(); i++) {
			Particle c = particles.get(i);
			if (ans.get_w() < c.get_w())
				ans = c;
		}
		return ans;		
	}
	
	public int size (){
		return _parts.size();
	}
}
