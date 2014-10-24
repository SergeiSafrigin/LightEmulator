/**
 * this class represents a set of simple static functions for general use of the project.
 */
package Light_Algorithm;

import geom3D.Point3D;

import java.util.Random;

public class ParticleFilterUtils {
	public static final double ROI_XY = 5.0;
	public static final double ROI_Z = 1.0;
	public static int _rand_seed = 31;
	public static Random _rand = new Random(_rand_seed);
	public static void reset_seed(int seed) {
		_rand_seed = seed;
		_rand = new Random(_rand_seed);
	}
	public static double nextRandom() {return _rand.nextDouble();}
	public static double nextRandom(double min, double max) {
		double dx = max-min;
		double ans = dx*nextRandom();
		ans = min+ans;
		return ans;
	}
	public static Point3D randPoint(Point3D min, Point3D max) {
		double x = nextRandom(min.x(), max.x());
		double y = nextRandom(min.y(), max.y());
		double z = nextRandom(min.z(), max.z());
		return new Point3D(x,y,z);
	}
	
	public static double _epsilon = 0.0001;
	public static double _BIG_epsilon = 1.0;
	public static double MAX_W = 1000;
	public static double MIN_W = 1.0/1000;
	public static double ELEV_ERR=1;
	public static double ORI_ERR=2;
	public static double X_FOV = 30; //[-30,+30];
	public static double Y_FOV = 22; //[-22,+22];
	
}