package lights;

import geom3D.Ang_Vector;
import geom3D.Point3D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import kcg.core.Arrays;
import kcg.core.ImageRotate;
import kcg.core.filters.BinaryFilter;
import kcg.core.light.GeometryLight;
import kcg.core.light.ImageConfig;
import kcg.core.light.ImageConfig.DistanceType;
import kcg.core.light.Point3d;
import kcg.core.light.VisualLight;
import kcg.core.light.filter.LightFilter;
import kcg.core.light.filter.VisualOdometryFilter;
import kcg.datarecorder.main.Config;
import kcg.datarecorder.recorder.Data;
import kcg.datarecorder.recorder.FrameData;

public class FrameReader {

	//camera frames saved by the recorder
	private ArrayList<FrameData> foundedFrames;
	//the binary filter threshold 
	private int threshold;
	//minumum pixels that light should have in order to be qualified as a legit light.
	private int minPixelsForLight;	
	//the config of the camera recording
	private Config recordConfig;

	//width and height of the camera frames
	private int frameWidth;
	private int frameHeight;

	//colors to represent different lights
	private Color[] colors = { 
			Color.BLUE, Color.YELLOW, Color.CYAN,
			Color.ORANGE, Color.MAGENTA, Color.PINK };

	//TODO IMPORTANT yaw need to be fixed for the lab lights map
	private float yawFix = 26.50363f;

	//current frame yaw
	private float currYaw;
	//current frame pitch
	private float currPitch;
	//current frame roll
	private float currRoll;
	//current frame image
	private int[] currImage;
	//current frame binary image
	private int[] currBinaryImage;

	private Point3d currVisualOdometryLocation;

	ArrayList<VisualLight> visualLights;
	ArrayList<GeometryLight> geometryLights;

	//fixing the width and height to work with the rotation

	private ImageConfig.Camera camera = null;
	private int rotation;
	private boolean flip;

	private ImageConfig.Device device;
	private ImageConfig config;

	private LightFilter lightFilter;
	private VisualOdometryFilter odometryFilter;

	private kcg.core.light.Point3d lastLocation = null;

	public FrameReader(String fileName){
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));	
			Data data = (Data)in.readObject();
			foundedFrames = data.getData();

			threshold = 230;
			minPixelsForLight = 50;
			recordConfig = data.getConfig();
			frameWidth = recordConfig.getPreviewWidth();
			frameHeight = recordConfig.getPreviewHeight();

			if (recordConfig.getCamera() == Config.Camera.BACK){
				camera = ImageConfig.Camera.BACK;
				rotation = 90;
				flip = false;
			} else {
				camera = ImageConfig.Camera.FRONT;
				rotation = -90;
				flip = true;
			}

			rotation %= 360;
			if (rotation == 90 || rotation == -90 || rotation == 270 || rotation == -270) {
				int temp = frameWidth;
				frameWidth = frameHeight;
				frameHeight = temp;
			}

			device = recordConfig.getDevice() == Config.Device.GLASSES ? ImageConfig.Device.SMART_GLASSES : ImageConfig.Device.PHONE;
			config = new ImageConfig(DistanceType.EDGE_PIXELS, device, camera, recordConfig.gethAngle(), recordConfig.getvAngle(), frameHeight, frameWidth);

			lightFilter = new LightFilter(config);
			odometryFilter = new VisualOdometryFilter(config);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void drawImage(int[] frame, int width, int height, int offsetX, int offsetY, Graphics g) {
		if (frame != null){
			BufferedImage currentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int[] imgData = ((DataBufferInt) currentImage.getRaster().getDataBuffer()).getData();
			System.arraycopy(frame, 0, imgData, 0, imgData.length);

			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(currentImage, null, offsetX, offsetY);
		}
	}

	public void drawLightEdges(int width, int height, int offsetX, int offsetY, Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		int numOfEdgePixels = lightFilter.getEdgePixelsFound();

		if (numOfEdgePixels > 0) {
			g2d.fillRect(offsetX, offsetY, width, height);

			int[] edgePixelsX = lightFilter.getEdgePixelsX();
			int[] edgePixelsY = lightFilter.getEdgePixelsY();

			g2d.setColor(Color.RED);

			for (int i = 0; i < numOfEdgePixels; i++) {
				g2d.fillRect((int) (edgePixelsX[i] + 0.5) + offsetX,
						(int) (edgePixelsY[i] + 0.5) + offsetY, 1, 1);
			}
		}
	}

	public void drawGeometryLights(int offsetX, int offsetY, Graphics g) {
		if (geometryLights != null){
			Graphics2D g2d = (Graphics2D) g;

			for (int i = 0; i < geometryLights.size(); i++) {
				GeometryLight geometryLight = geometryLights.get(i);
				if (geometryLight.pitch > 0 && geometryLight.unregisteredFrames == 0) {
					Color color;
					if (geometryLight.mainLight) {
						color = Color.WHITE;

					} else
						color = colors[geometryLight.registrationId
						               % colors.length];

					g2d.setColor(color);
					g2d.fillRect((int) (geometryLight.x + 0.5) + offsetX, (int) (geometryLight.y + 0.5) + offsetY, 5, 5);

					if (geometryLight.mainLight) {
						g2d.drawString("(m)", (int) (geometryLight.x + 0.5) + offsetX-20, (int) (geometryLight.y + 0.5) + offsetY);
					}
					g2d.drawString(geometryLight.registrationId + "", (int) (geometryLight.x + 0.5) + offsetX, (int) (geometryLight.y + 0.5) + offsetY);
				}
			}
		}
	}

	/**
	 * converts JPEG byte array to buffered image
	 * @param src JPEG byte array
	 * @return new buffered image
	 */

	public static BufferedImage JPEGToBufferedImage(byte[] src){
		ByteArrayInputStream bis = new ByteArrayInputStream(src);

		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(bis);
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bufferedImage;
	}

	/**
	 * converts any type of buffered image to ARGB type
	 * @param src the buffered image we want to convert
	 * @return new buffered image
	 */

	public static BufferedImage AnyBufferedToARGBBuffered(BufferedImage src){
		BufferedImage newImage = new BufferedImage(src.getWidth(), src.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D newImageGraphics = newImage.createGraphics();
		newImageGraphics.drawImage(src, 0, 0, null);
		return newImage;
	}

	/**
	 * rotates buffered image
	 * @param src the buffered image we want to rotate
	 * @param rotation the rotation 0,90,180,270,-180,-270..
	 * @return new rotated buffered image
	 */
	public static BufferedImage rotateBufferedImage(BufferedImage src, int rotation, boolean flip){
		if (src.getType() != BufferedImage.TYPE_INT_ARGB)
			throw new RuntimeException("Only BufferedImage.TYPE_INT_ARGB type is supported");
		if (rotation % 90 != 0)
			throw new RuntimeException("Rotation has to be multiple of 90");

		rotation %= 360;

		int width = src.getWidth();
		int height = src.getHeight();

		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int[] imgData = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
		int[] newImageData = ((DataBufferInt) newImage.getRaster().getDataBuffer()).getData();

		int[] storage = new int[width * height];

		ImageRotate.rotate(imgData, storage, width, height, rotation);

		if (flip)
			ImageRotate.flip(storage.clone(), storage, height, width);

		System.arraycopy(storage, 0, newImageData, 0, imgData.length);

		return newImage;
	}

	public ArrayList<VisualLight> proccessLightFilter(LightFilter lightFilter,
			int[] frame, int width, int height, int minPixelsForLight) {				
		int[][] matrixStorage = new int[height][width];

		Arrays.arrayToMatrix(frame, matrixStorage, width, height);

		ArrayList<VisualLight> visualLights = lightFilter.process(matrixStorage, minPixelsForLight);

		return visualLights;
	}

	public int[] getBinaryImage(int[] frame, int width, int height, int threshold) {
		int[] intStorage = new int[width * height];
		return BinaryFilter.RGB_TO_BINARY(frame, intStorage, threshold, width, height);
	}

	public ArrayList<Ang_Vector> step(int i){
		i %= foundedFrames.size();

		FrameData frame = foundedFrames.get(i%foundedFrames.size());

		currYaw = frame.getYaw();
		currPitch = frame.getPitch();
		currRoll = frame.getRoll();

		//fixing yaw for the lab lights map		
		currYaw += yawFix;
		currYaw %= 360;

		byte[] frameImage = frame.getFrame();

		BufferedImage image = JPEGToBufferedImage(frameImage);
		BufferedImage rgbImage = AnyBufferedToARGBBuffered(image);
		BufferedImage rotatedImage = rotateBufferedImage(rgbImage, rotation, flip);

		currImage = ((DataBufferInt) rotatedImage.getRaster().getDataBuffer()).getData();
		currBinaryImage = getBinaryImage(currImage, frameWidth, frameHeight, threshold);

		visualLights = proccessLightFilter(
				lightFilter, currBinaryImage, frameWidth, frameHeight, minPixelsForLight);

		geometryLights = odometryFilter.process(visualLights, currYaw, currPitch, currRoll);

		currVisualOdometryLocation = odometryFilter.getLocationInCm();

		return geometryLightsToAngVectors(geometryLights);
	}

	//convert list of geometry lights to angular vectors
	//it takes only lights that are higher that pitch 10 (parameter) - so low lights,
	//like windows won't be a problem 
	public ArrayList<Ang_Vector> geometryLightsToAngVectors(ArrayList<GeometryLight> geometryLights) {
		ArrayList<Ang_Vector> lts = new ArrayList<Ang_Vector>();

		for(GeometryLight light: geometryLights){			
			//TODO maybe change this number
			if (light.pitch > 0)
				lts.add(new Ang_Vector(light.yaw, light.pitch));
		}

		return lts;
	}

	//returns yaw from the last frame fired by step function
	public float getCurrYaw() {
		return currYaw;
	}

	//returns pitch from the last frame fired by step function
	public float getCurrPitch() {
		return currPitch;
	}

	//returns roll from the last frame fired by step function
	public float getCurrRoll() {
		return currRoll;
	}

	public ImageConfig getConfig() {
		return config;
	}

	public int[] getCurrImage() {
		return currImage;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public Point3D getCurrVisualOdometryLocation() {
		return new Point3D(currVisualOdometryLocation.x, currVisualOdometryLocation.y, currVisualOdometryLocation.z);
	}
}
