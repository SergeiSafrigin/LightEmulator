package Simulator;

import geom3D.Ang_Vector;
import geom3D.Geom_Algo;
import geom3D.Point3D;
import geom3D.Vector3D;
import geomLights.GIS_Light;
import geomLights.GIS_Lights;

import java.util.*;

import Light_Algorithm.Lights_Algorithms;
import Light_Algorithm.Particle;
import Light_Algorithm.Particle_Algo;
import Light_Algorithm.Position_State;


/**
this class is the window manager of the simple IndoogGo Light algorithm simulator 
including all GUI parts: paint method, data Editable (Drawable container),GUI, mouse manager,
*/

public class Sim_no_GUI	{ 
  // *** private data ***
	 //  private BufferedImage image;
	
	public static void main(String[] a) {
		System.out.println("InDoorGo Simulation: K&CG");
		init();
		int i=0;
		while(i<1000) {
			step(1);
			i++;
		}
	}	
	private static Cords W2P = Cords.W2P();
	private static int _stage;
	private static String Lights_File="data/Light3.txt";
	private static Point3D _p1,_p2; // tmp Points for selection
	private static GIS_Lights _map = null;
	private static int NUMBER_OF_PARTICLES = 100;
	private static Particle_Algo _algo = null; 
	public static double _time=0;
	public static Position_State _solution = null;
	public static Point3D _solve;
	
	public static double max_speed = 2.0; // m/s
	public static Point3D min = new Point3D(0,0,0);
	public static Point3D max = new Point3D(10,10,3);
	public static double min_elev = -20, max_elev = 90;
	public static double min_z = 0, max_z = 2.0;
	public static int fps=25;
	public static double sim_time = 60*1000; // mili-seconds
	public static String light_map = "data/Light3.txt";
	public static double max_ang_speed = 90; // deg/s
	public static boolean DEBUG=true;
	public static double NORM_ERR = 1.0;  // [-2,2] - yaw, [-1,1] pitch
	// ****************************************
	
	// *** text area ***
  
  private static void init() {
	   	_stage = Const.Gen; 
	  	_p1=null;
	  	_time = 0;
	  	
	    try {    
	    	_map = new GIS_Lights(Lights_File);
	    	_algo = new Particle_Algo(NUMBER_OF_PARTICLES);
	    	Point3D start = new Point3D(5,5,1.5);
	    	_solution = new Position_State(start);
	    	_algo.init(min,max,NUMBER_OF_PARTICLES);
	    	_solve = null;
		//	_algo.get(0).update(start, 0.99, 40);
	     } catch (Exception ex) {ex.printStackTrace();}
  }
  private static void step(double dt) {
	 ArrayList<Ang_Vector> lts = Lights_Algorithms.VisLights(_map, _solution);
	 if(lts.size()>=2) {
		_solve = simpleTest(lts);
		_algo.update(lts, _map);
		Point3D best = _algo.best().get_pos();
		int dist1 = (int)(100*_solve.distance3D(_solution.get_pos()));
		int dist2 = (int)(100*best.distance3D(_solution.get_pos()));
		System.out.println("Step: best: "+dist2+"  opt: "+dist1+"  pp: "+_solve+"   real: "+_solution.get_pos()+"  best: "+best);
		//Particle best = _algo.best();
		//	double dist = best.get_pos().distance3D(_solution.get_pos());
		//	if(DEBUG) System.out.println(", "+best.get_pos()+", ERR: "+dist+"  w: "+best.get_w());
		}
	  move();
	  _time = _time + 1;
  }
  private static Point3D simpleTest( ArrayList<Ang_Vector> lts) {
	  Point3D l0=null, l1=null, v0=null, v1 = null;
	  for(int i=0;l1==null && i<_map.size();i++) {
		  GIS_Light c = _map.get_lights().get(i);
		  if(c.is_vis()) {
			  Point3D t = c.getCenter();
			  if(l0==null) l0=t;
			  else l1=t;
		  }
	  }
	  v0 = getPoint(l0,lts.get(0)); v1 = getPoint(l1,lts.get(1));
	  Point3D pp0 = Geom_Algo.CenterOfMidSegmentBetweenTwoLines(l0, v0, l1, v1);
	  v0 = getPoint(l0,lts.get(1)); v1 = getPoint(l1,lts.get(0));
	  Point3D pp1 = Geom_Algo.CenterOfMidSegmentBetweenTwoLines(l0, v0, l1, v1);
	  
	 Point3D ans = pp0;
	 if(_solution.get_pos().distance3D(pp0)>_solution.get_pos().distance3D(pp1)) {
		 ans = pp1;
	 }
	 return ans;
  }
  private static Point3D getPoint(Point3D p, Ang_Vector v) {
	  Point3D ans = new Point3D(p);
	  Vector3D vec = v.toVector();
	  ans.add(vec.toPoint3D());
	  return ans;
  }
  private static void move() {
	  Point3D p = _solution.get_pos();
	  double r = 0.2;
	  double x = rand(-r,r);
	  double y = rand(-r,r);
	  double z = rand(-r,r);
	  if(p.z()<=1) z=Math.abs(z); if(p.z()>=2) z=-Math.abs(z);
	  if(p.x()<=r) x=Math.abs(x); if(p.x()>=10-r) x=-Math.abs(x);
	  if(p.y()<=r) y=Math.abs(y); if(p.y()>=10-r) y=-Math.abs(y);
	  p.add(x, y, z);
	  _solution.set_pos(p);
	  Ang_Vector av = _solution.get_ori();
	  double da = rand(-3,5);
	  double de = rand(-2,2);
	  av._azm+=da;
	  av._elev+=de;
	  if( av._azm<0) { av._azm+=360;}
	  if( av._azm>=360) { av._azm-=360;}
	  if(av._elev>=90) {av._elev=45;}
	  if(av._elev<=-10) {av._elev=0;}
	  _solution.set_ori(av);
		
  }
  private static double rand(double min, double max){
		double d = Math.random();
		double dx = max-min;
		return min+dx*d;
	}
	private static double azmDiff(double a1, double a2) {
		double max = Math.max(a1, a2);
		double min = Math.min(a1, a2);
		double da = max-min;
		if(da>=180) {
			da = Math.abs((360-max)-min);
		}
		return da;
	}
 
	// ********** Private methodes (open,save...) ********
	
}

		