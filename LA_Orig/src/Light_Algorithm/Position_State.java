package Light_Algorithm;

import geom3D.Ang_Vector;
import geom3D.Point3D;
import geom3D.Vector3D;

/**
 * this class present a time based position: including location, orientation, velocity, 
 *  
 * @author boaz
 *
 */
public class Position_State {
	private Point3D _pos;   
	private Ang_Vector _ori;
	private long _time;    // ms 
	private Vector3D _vel; // in m/s
	public static long s_time = 0, dt = 40;//ms
	
	public Position_State(Point3D p, Vector3D or, long t, Vector3D v) {
		set_pos(new Point3D(p));
		set_ori(new Ang_Vector(or));
		set_time(t);
		s_time = t;
		if(v==null) {
			v = new Vector3D(0,0,0);
		}
		set_vel(new Vector3D(v));
	}

	public Position_State(Point3D p, Ang_Vector or, Vector3D v, long t) {
		this._pos = p;this._ori=or; this._time=t; this._vel=v;
	}
	public Position_State(Point3D p, Ang_Vector or) {
		this(p,or, new Vector3D(0,0,0),0);
	}
	public Position_State(Point3D p) {
		this(p,new Ang_Vector(0,45,10));
	}
	public Position_State(Position_State t) {
		this(t._pos, t._ori, t._vel, t._time);
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
	public Ang_Vector get_ori() {
		return _ori;
	}
	public void set_ori(Ang_Vector _ori) {
		this._ori = _ori;
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

