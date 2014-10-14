/**
 * this class represents a set of simple static functions for general use of the project.
 */
package Simulator;

import java.util.Random;

public class Utils {
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
	public static double _epsilon = 0.0001;
	public static double _BIG_epsilon = 1.0;
}
