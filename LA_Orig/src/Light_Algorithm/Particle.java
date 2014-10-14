package Light_Algorithm;

import java.util.Date;

import geom3D.Ang_Vector;
import geom3D.Point3D;
import geom3D.Vector3D;

/**
 * this class present a time based possible solution (aka particle): 
 * including location, velocity, weight (confidence)
 *  
 * @author boaz
 *
 */
public class Particle {
	private Point3D _pos;   
	private double  _weight; //   
	private long _time;    // ms 
	private Vector3D _vel; // in m/s
	public static double N=0.3;
	public Particle(Point3D p, long t, Vector3D v) {
		set_pos(new Point3D(p));
		set_w(0.1);
		set_time(t);
		if(v==null) {
			v = new Vector3D(0,0,0);
		}
		set_vel(new Vector3D(v));
	}
	public Particle(Point3D p) {
		this(p,new Date().getTime(), null);

	}
	
	public Vector3D update(Point3D p, double w, long dt) {
		Vector3D v = new Vector3D(this._pos,p);
		v.multiplyByScalar(1000.0/dt);
		this.set_w(w*N+this._weight*(1-N));
		return v;
	}
	
	/**
	 * this method moves the current position in the velocity direction 
	 * @param msec
	 */
	public void next_step(double dt_msec) {
		double norm = dt_msec/1000;
		double dx = this.get_vel().x *norm;
		double dy = this.get_vel().y *norm;
		double dz = this.get_vel().z *norm;
		this.get_pos().add(dx,dy,dz);
	}
	public Vector3D get_vel() {
		return _vel;
	}
	public void set_vel(Vector3D _vel) {
		this._vel = _vel;
	}
	public double get_w() {
		return _weight;
	}
	public void set_w(double w) {
		this._weight = w;
	}
	public long get_time() {
		return _time;
	}
	public void set_time(long _time) {
		this._time = _time;
	}
	public Point3D get_pos() {
		return _pos;
	}
	public void set_pos(Point3D _pos) {
		this._pos = _pos;
	}
	
}

