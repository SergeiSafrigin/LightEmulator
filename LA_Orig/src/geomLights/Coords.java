package geomLights;

public class Coords {
	
	public static final double EARTH_RADIUS = 1000*6371;
	
	/**
	 * this method computes the flat world distance vector between two global points (lat-north, lon-east, alt-above-sea) 
	 * assuming the two points are relatively close .
	 * @param ll1
	 * @param ll2
	 * @return
	 */
	public static double[] flatWorldDist(double[] ll1, double[] ll2) {
		double[] ans = new double[3];
		double dx = ll2[1]-ll1[1]; // delta lon east
		double dy = ll2[0]-ll1[0]; // delta lat north
		double dz = ll2[2]-ll1[2]; // delta alt
		if(Math.abs(dx)>0.1 | Math.abs(dy)>0.1) {
			throw new RuntimeException ("ERR: the two latlon points are too far - can not be assumed as flat world");
		}
		double x = EARTH_RADIUS * Math.toRadians(dx) * Math.cos(Math.toRadians(ll1[0]));
		double y = EARTH_RADIUS * Math.toRadians(dy); 
		ans[0] = x; ans[1]=y; ans[2] = dz;
		return ans;
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
		ans[0] = ll1[0]+Math.toDegrees(lat); ans[1]=ll1[1]+Math.toDegrees(lon); ans[2] = ll1[2]+dz;
		return ans;
	}
	
	public static void main(String[] a) {
		double[] ll1 = {32.166916,34.803395,100};
		double[] ll2 = {32.166917,34.81401,120};
		double[] vec = Coords.flatWorldDist(ll1, ll2);
		
		double[] ll2a = Coords.offsetLatLonAlt(ll1,vec);
		System.out.println("Vec:"+vec[0]+", "+vec[1]+", "+vec[2]);
		System.out.println("ll2a:"+ll2a[0]+", "+ll2a[1]+", "+ll2a[2]);
		if(Math.abs(vec[1])>1 | Math.abs(vec[0]-1000)>1 || Math.abs(vec[2]-20)>0.001 ) {
			System.err.println("Wrong Coords to dVec convertor");
		}
		if(Math.abs(ll2[0]-ll2a[0])>0.0001 | Math.abs(ll2[1]-ll2a[1])>0.0001 || Math.abs(ll2[2]-ll2a[2])>0.0001 ) {
			System.err.println("Wrong Coords to dVec2 convertor");
		}
		
	}
}

