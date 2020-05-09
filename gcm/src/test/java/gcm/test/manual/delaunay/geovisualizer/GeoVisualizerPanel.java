package gcm.test.manual.delaunay.geovisualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import gcm.util.delaunay.GeoCoordinate;
import gcm.util.dimensiontree.DimensionTree;
import gcm.util.earth.ECC;
import gcm.util.earth.Earth;
import gcm.util.earth.LatLon;
import gcm.util.earth.LatLonAlt;
import gcm.util.graph.GenericMutableGraph;
import gcm.util.graph.GraphPathSolver;
import gcm.util.graph.Path;
import gcm.util.vector.MutableVector3D;

public class GeoVisualizerPanel extends JPanel {

	private static class Link {
		int first;
		int second;

		public Link(int first, int second) {
			super();
			this.first = first;
			this.second = second;
		}

	}

	private static class Model {
		List<MutableVector3D> positions = new ArrayList<>();
		List<Link> links = new ArrayList<>();
	}

	private static final long serialVersionUID = -8746718644903555611L;
	private Timer timer;
	private boolean paintOnTimer = true;
	private final int timerDelayMilliseconds = 30;
	private Model dataModel;
	private final Camera camera;

	private Model worldGridModel;

	private void buildWorldGridModel(Earth earth) {
		worldGridModel = new Model();
		// build the longitude lines
		int id = 0;
		for (int i = -180; i < 180; i += 15) {
			int baseId = id;
			for (int j = -90; j < 90; j++) {
				LatLon latLon = new LatLon(j, i);
				worldGridModel.positions.add(earth.getECCFromLatLon(latLon).toVector3D());
				Link link;
				if (j == 89) {
					link = new Link(id, baseId);
				} else {
					link = new Link(id, id + 1);
				}
				worldGridModel.links.add(link);
				id++;
			}
		}

		for (int i = -90; i < 90; i += 15) {
			int baseId = id;
			for (int j = -180; j < 180; j++) {
				LatLon latLon = new LatLon(i, j);
				worldGridModel.positions.add(earth.getECCFromLatLon(latLon).toVector3D());
				Link link;
				if (j == 179) {
					link = new Link(id, baseId);
				} else {
					link = new Link(id, id + 1);
				}
				worldGridModel.links.add(link);
				id++;
			}
		}

	}

	private <T extends GeoCoordinate> void buildDataModel(Earth earth, List<T> geoCoordinates, List<Pair<T, T>> links) {
		dataModel = new Model();

		Map<T, Integer> map = new LinkedHashMap<>();

		geoCoordinates.forEach(geoCoordinate -> map.put(geoCoordinate, map.size()));

		geoCoordinates.forEach(geoCoordinate -> {
			LatLon latLon = new LatLon(geoCoordinate.getLatitude(), geoCoordinate.getLongitude());
			MutableVector3D v = earth.getECCFromLatLon(latLon).toVector3D();
			dataModel.positions.add(v);
		});
		
		
		
		links.forEach(pair -> {
			Integer index1 = map.get(pair.getFirst());
			Integer index2 = map.get(pair.getSecond());			
			Link link = new Link(index1, index2);			
			dataModel.links.add(link);
		});

	}

	private Earth earth;

	private static class Edge {
		Link link;

		public Edge(Link link) {
			super();
			this.link = link;
		}

	}

	private PathManager pathManager;

	public <T extends GeoCoordinate> GeoVisualizerPanel(List<T> geoCoordinates, List<Pair<T, T>> links) {
		earth = Earth.fromMeanRadius();

		buildWorldGridModel(earth);

		buildDataModel(earth, geoCoordinates, links);

		MutableVector3D initialCameraPosition = new MutableVector3D();

		pathManager = new PathManager(earth, dataModel);

		dataModel.positions.forEach(v -> initialCameraPosition.add(v));
		initialCameraPosition.normalize();
		initialCameraPosition.scale(earth.getRadius() + 500_000);
		camera = new Camera(earth, initialCameraPosition);

		addMouseListener(new MouseListenerImpl());
		addMouseWheelListener(new MouseWheelListenerImpl());
		addKeyListener(new KeyListenerImpl());
		addMouseMotionListener(new MouseMotionListenerImpl());

		setBackground(Color.black);

		timer = new Timer(timerDelayMilliseconds, new ActionListenerImpl());
		timer.start();

	}

	private class MouseMotionListenerImpl implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			camera.setMouseDragEnd(e.getPoint());
			paintOnTimer = true;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			camera.setMousePoint(e.getPoint());
			paintOnTimer = true;
		}

	}

	private class KeyListenerImpl implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent e) {

			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				camera.left();
				break;
			case KeyEvent.VK_RIGHT:
				camera.right();
				break;
			case KeyEvent.VK_UP:
				camera.up();
				break;
			case KeyEvent.VK_DOWN:
				camera.down();
				break;
			case KeyEvent.VK_EQUALS:
				camera.fovyUp();
				break;
			case KeyEvent.VK_MINUS:
				camera.fovyDown();
				break;
			case KeyEvent.VK_S:
				setPathSource();
				break;
			case KeyEvent.VK_D:
				setPathDestination();
				break;

			default:
				return;
			}
			// repaint();
			paintOnTimer = true;
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}

	}

	private class MouseWheelListenerImpl implements MouseWheelListener {

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int wheelRotation = e.getWheelRotation();
			if (wheelRotation < 0) {
				camera.zoom(true);
			} else {
				camera.zoom(false);
			}
			paintOnTimer = true;
		}

	}

	private class ActionListenerImpl implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (paintOnTimer) {
				paintOnTimer = false;
				repaint();
			}
		}
	}

	private static class PathManager {
		private boolean recalcPath = true;
		private final Model dataModel;
		private final Earth earth;

		private double getEdgeCost(Edge edge) {
			MutableVector3D a = dataModel.positions.get(edge.link.first);
			MutableVector3D b = dataModel.positions.get(edge.link.second);
			return earth.getGroundDistanceFromECC(new ECC(a), new ECC(b));
		}

		private double getTravelCost(Integer id1, Integer id2) {
			MutableVector3D a = dataModel.positions.get(id1);
			MutableVector3D b = dataModel.positions.get(id2);
			return earth.getGroundDistanceFromECC(new ECC(a), new ECC(b));
		}

		public PathManager(Earth earth, Model dataModel) {
			this.earth = earth;
			this.dataModel = dataModel;
			positionTree = DimensionTree.builder()//
										.setLowerBounds(new double[] { -earth.getRadius(), -earth.getRadius(), -earth.getRadius() })//
										.setUpperBounds(new double[] { earth.getRadius(), earth.getRadius(), earth.getRadius() })//
										.build();//

			graph = new GenericMutableGraph<>();
			for (int i = 0; i < dataModel.positions.size(); i++) {
				graph.addNode(i);
			}
			for (Link link : dataModel.links) {
				graph.addEdge(new Edge(link), link.first, link.second);
				graph.addEdge(new Edge(link), link.second, link.first);
			}

			for (int i = 0; i < dataModel.positions.size(); i++) {
				MutableVector3D position = dataModel.positions.get(i);
				positionTree.add(position.toArray(), i);
			}
		}

		public MutableVector3D getPathSourcePosition() {
			return dataModel.positions.get(pathSourceIndex);
		}

		public MutableVector3D getPathDestinationPosition() {
			return dataModel.positions.get(pathDestinationIndex);
		}

		private int pathSourceIndex;

		private int pathDestinationIndex;

		private final DimensionTree<Integer> positionTree;

		private final GenericMutableGraph<Integer, Edge> graph;

		private Set<Link> pathLinks = new LinkedHashSet<>();

		private Set<Link> getPathLinks() {
			if (recalcPath) {
				pathLinks.clear();
				if (pathSourceIndex != pathDestinationIndex) {
					Path<Edge> path = GraphPathSolver.getPath(graph, pathSourceIndex, pathDestinationIndex, this::getEdgeCost, this::getTravelCost);
					path.getEdges().forEach(edge -> pathLinks.add(edge.link));
				}
				recalcPath = false;
			}
			return new LinkedHashSet<>(pathLinks);
		}

		private void setPathSource(MutableVector3D position) {

			Integer nearestMember = positionTree.getNearestMember(position.toArray());
			if (nearestMember != null) {
				if (pathSourceIndex != nearestMember) {
					pathSourceIndex = nearestMember;
					recalcPath = true;
				}
			}

		}

		private void setPathDestination(MutableVector3D position) {
			Integer nearestMember = positionTree.getNearestMember(position.toArray());
			if (nearestMember != null) {
				if (pathDestinationIndex != nearestMember) {
					pathDestinationIndex = nearestMember;
					recalcPath = true;
				}
			}
		}

	}

	private void setPathSource() {
		if (camera.mousePoint == null) {
			return;
		}
		MutableVector3D mousePosition = camera.get3DPositionFromPoint(camera.mousePoint);

		if (mousePosition == null) {
			return;
		}
		pathManager.setPathSource(mousePosition);
		paintOnTimer = true;
	}

	private void setPathDestination() {
		if (camera.mousePoint == null) {
			return;
		}
		MutableVector3D mousePosition = camera.get3DPositionFromPoint(camera.mousePoint);

		if (mousePosition == null) {
			return;
		}
		pathManager.setPathDestination(mousePosition);
		paintOnTimer = true;
	}

	private static class Camera {
		private final Earth earth;
		private final double panFactor = FastMath.PI / 500_000_000;

		private double screenUnitConversionRatio = 1;// depends on fovy and
														// screenHeight
		private double fovy = 15;
		private double screenWidth;
		private double screenHeight;

		private MutableVector3D cameraPosition = new MutableVector3D();

		private MutableVector3D head = new MutableVector3D();// depends on camera position
		private MutableVector3D nose = new MutableVector3D();// depends on camera position
		private MutableVector3D left = new MutableVector3D();// depends on camera position

		public double getAltitudeMeters() {
			return cameraPosition.length() - earth.getRadius();
		}

		private void recalcScreenUnitConversionRatio() {
			screenUnitConversionRatio = this.screenHeight / (2 * FastMath.tan(FastMath.toRadians(fovy)));
		}

		public void setMouseDragStart(Point point) {
			mouseDragStartPosition = get3DPositionFromPoint(point);
		}

		private Point mouseDragEndPoint;

		public void setMouseDragEnd(Point point) {
			mouseDragEndPoint = point;
		}

		public void stopMouseDrag() {
			mouseDragStartPosition = null;
			mouseDragEndPoint = null;
		}

		private MutableVector3D mouseDragStartPosition;

		public void moveCameraToDrag() {

			if (mouseDragStartPosition != null && mouseDragEndPoint != null) {

				MutableVector3D endPosition = get3DPositionFromPoint(mouseDragEndPoint);
				if (endPosition == null) {
					// the mouse was dragged off the world
					return;
				}
				double angle = mouseDragStartPosition.angle(endPosition);
				MutableVector3D rotator = new MutableVector3D(endPosition);
				rotator.cross(mouseDragStartPosition);
				rotator.normalize();

				MutableVector3D v = new MutableVector3D(cameraPosition);
				v.rotateAbout(rotator, angle);
				if (v.isFinite()) {
					cameraPosition.assign(v);
					calculateVectorsFromCameraPosition();
				}
			}
		}

		// this does not require a repaint
		private Point mousePoint;

		private void setMousePoint(Point mousePoint) {
			this.mousePoint = mousePoint;
		}

		private Optional<LatLon> getLatLonOfMousePosition() {
			if (mousePoint == null) {
				return Optional.empty();
			}
			MutableVector3D mousePosition = get3DPositionFromPoint(mousePoint);

			if (mousePosition == null) {
				return Optional.empty();
			}

			LatLonAlt latLonAlt = earth.getLatLonAlt(new ECC(mousePosition));
			LatLon latLon = new LatLon(latLonAlt);
			return Optional.of(latLon);
		}

		private MutableVector3D get3DPositionFromPoint(Point point) {

			/*
			 * We will convert from screen coordinates to a vector that points
			 * from the camera to the viewing plane. Recall that the viewing
			 * plane is one unit away from the camera in the direction of he
			 * nose.
			 */

			double x = point.getX();
			double y = point.getY();

			x -= screenWidth / 2;
			x /= screenUnitConversionRatio;

			y -= screenHeight / 2;
			y /= screenUnitConversionRatio;

			MutableVector3D u = new MutableVector3D();
			MutableVector3D v = new MutableVector3D();

			v.assign(left);
			v.scale(-x);
			u.add(v);

			v.assign(head);
			v.scale(-y);
			u.add(v);

			u.add(nose);

			/*
			 * We now normalize u and determine how far we scale u such that it
			 * will reach the surface of the earth.
			 * 
			 * Let d = distance from the camera to the surface of the earth
			 * along the pointing vector u
			 * 
			 * Let c = length of the camera vector
			 * 
			 * Let theta = angle from the camera to u
			 * 
			 * Let r = radius of the earth
			 * 
			 * By the law of cosines : d^2 +c^2 -2cd cos(theta) = r^2
			 * 
			 * We solve for d, using the smallest root(the first intersection
			 * with the earth).
			 * 
			 */
			u.normalize();

			double c = cameraPosition.length();

			v.assign(cameraPosition);
			v.reverse();
			// double cosTheta = FastMath.cos(u.angleInRadians(v));
			double cosTheta = u.dot(v) / v.length();

			double value = earth.getRadius() * earth.getRadius() + c * c * (cosTheta * cosTheta - 1);
			if (value < 0) {
				// there are no real roots
				return null;
			}
			value = FastMath.sqrt(value);

			double d = c * cosTheta - value;

			/*
			 * We now know the scaling factor to apply to u. After we add back
			 * he camera position, we will have the position on the earth's
			 * surface
			 */
			u.scale(d);
			u.add(cameraPosition);
			return u;
		}

		public List<String> getCameraStatus() {

			LatLonAlt latLonAlt = earth.getLatLonAlt(new ECC(cameraPosition));

			List<String> result = new ArrayList<>();
			result.add("Fovy = " + fovy);
			result.add("lat = " + latLonAlt.getLatitude());
			result.add("lon = " + latLonAlt.getLongitude());
			double altKm = latLonAlt.getAltitude() / 1000;
			result.add("alt(km) = " + altKm);

			Optional<LatLon> latLonOfMousePosition = getLatLonOfMousePosition();
			if (latLonOfMousePosition.isPresent()) {
				LatLon latLon = latLonOfMousePosition.get();
				DecimalFormat decimalFormat = new DecimalFormat("#.##");
				result.add(decimalFormat.format(latLon.getLatitude()) + " " + decimalFormat.format(latLon.getLongitude()));

			}

			return result;
		}

		private void calculateVectorsFromCameraPosition() {
			nose.assign(cameraPosition);
			nose.reverse();
			nose.normalize();

			MutableVector3D north = new MutableVector3D(0, 0, 1);

			head.assign(cameraPosition);
			head.rotateToward(north, FastMath.PI / 2);
			head.normalize();

			left.assign(head);
			left.cross(nose);
			left.normalize();
		}

		public void fovyUp() {
			fovy += 5;
			fovy = FastMath.min(180, fovy);
			recalcScreenUnitConversionRatio();
		}

		public void fovyDown() {
			fovy -= 5;
			fovy = FastMath.max(5, fovy);
			recalcScreenUnitConversionRatio();
		}

		public void zoom(boolean zoomIn) {
			// determine where the mouse appears to be on the earth's surface
			MutableVector3D currentPositionOfMouse = get3DPositionFromPoint(mousePoint);
			if (currentPositionOfMouse == null) {
				return;
			}

			double alt = cameraPosition.length() - earth.getRadius();
			if (zoomIn) {
				alt *= 0.9;
				alt = FastMath.max(100, alt);
			} else {
				alt *= 1.1;
				alt = FastMath.min(50_000_000, alt);
			}

			// zoom as expected, just scaling the camera position
			cameraPosition.normalize();
			cameraPosition.scale(earth.getRadius() + alt);
			calculateVectorsFromCameraPosition();

			if (!zoomIn && alt > 8_000_000) {
				// zooming out and keeping location under the mouse constant
				// looks odd
				// if we get to high
				return;
			}

			// determine where the mouse appears to be now that we have zoomed
			MutableVector3D newPositionOfMouse = get3DPositionFromPoint(mousePoint);
			if (newPositionOfMouse == null) {
				// we are off the world
				return;
			}

			/*
			 * Rotate the camera so that the new position of the mouse is now
			 * the old position of the mouse. This ensures that we keep the
			 * point where the mouse is constant during zoom.
			 */
			MutableVector3D v = new MutableVector3D(newPositionOfMouse);
			v.cross(currentPositionOfMouse);
			double angle = currentPositionOfMouse.angle(newPositionOfMouse);
			MutableVector3D newCamera = new MutableVector3D(cameraPosition);
			newCamera.rotateAbout(v, angle);

			if (newCamera.isFinite()) {
				cameraPosition.assign(newCamera);
				calculateVectorsFromCameraPosition();
			}

		}

		public int getWorldScreenRadius() {
			double alt = cameraPosition.length() - earth.getRadius();
			return (int) (screenUnitConversionRatio * earth.getRadius() / FastMath.sqrt(2 * alt * earth.getRadius() + alt * alt));
		}

		public void up() {
			double alt = cameraPosition.length() - earth.getRadius();
			MutableVector3D north = new MutableVector3D(0, 0, 1);
			cameraPosition.rotateToward(north, panFactor * alt);
			calculateVectorsFromCameraPosition();
		}

		public void down() {
			double alt = cameraPosition.length() - earth.getRadius();
			MutableVector3D north = new MutableVector3D(0, 0, 1);
			cameraPosition.rotateToward(north, -panFactor * alt);
			calculateVectorsFromCameraPosition();
		}

		public void left() {
			double alt = cameraPosition.length() - earth.getRadius();
			MutableVector3D north = new MutableVector3D(0, 0, 1);
			cameraPosition.rotateAbout(north, -panFactor * alt);
			calculateVectorsFromCameraPosition();

		}

		public void right() {
			double alt = cameraPosition.length() - earth.getRadius();
			MutableVector3D north = new MutableVector3D(0, 0, 1);
			cameraPosition.rotateAbout(north, panFactor * alt);
			calculateVectorsFromCameraPosition();

		}

		public void setScreen(int screenWidth, int screenHeight) {
			if (this.screenWidth != screenWidth) {
				this.screenWidth = screenWidth;
			}
			if (this.screenHeight != screenHeight) {
				this.screenHeight = screenHeight;
				recalcScreenUnitConversionRatio();
			}

		}

		public Camera(Earth earth, MutableVector3D initialCameraPosition) {
			this.earth = earth;
			cameraPosition = new MutableVector3D(initialCameraPosition);
			calculateVectorsFromCameraPosition();
		}

		public Point getPointFrom3DPosition(MutableVector3D position) {
			MutableVector3D v = new MutableVector3D(position);
			v.sub(cameraPosition);

			/*
			 * If the position is occluded by the earth then don't show the data
			 */
			double alt = cameraPosition.length() - earth.getRadius();
			double squareExclusionDistance = (2 * earth.getRadius() + alt) * alt;
			if (v.squareLength() > squareExclusionDistance) {
				return null;
			}

			double x = -v.dot(left);
			double y = -v.dot(head);
			double z = v.dot(nose);

			x /= z;
			x *= screenUnitConversionRatio;
			x += screenWidth / 2;
			int xCoord = (int) FastMath.round(x);

			if (xCoord < -0.1 * screenWidth) {
				return null;
			}
			if (xCoord > 1.1 * screenWidth) {
				return null;
			}

			y /= z;
			y *= screenUnitConversionRatio;
			y += screenHeight / 2;
			int yCoord = (int) FastMath.round(y);

			if (yCoord < -0.1 * screenHeight) {
				return null;
			}
			if (yCoord > 1.1 * screenHeight) {
				return null;
			}
			return new Point(xCoord, yCoord);
		}

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		camera.setScreen(getWidth(), getHeight());
		camera.moveCameraToDrag();

		double baseWidth = Math.min(getWidth(), getHeight());
		baseWidth = baseWidth / FastMath.sqrt(dataModel.positions.size());
		int pew = (int) (baseWidth / 10);
		pew = FastMath.max(pew, 1);
		pew = FastMath.min(pew, 10);
		int pathEdgeWidth = pew;

		int nr = (int) (baseWidth / 10);
		nr = FastMath.max(nr, 2);
		nr = FastMath.min(nr, 10);
		int nodeRadius = nr;

		// paint the green globe
		g2.setColor(new Color(161, 194, 78));
		int worldScreenRaius = camera.getWorldScreenRadius();
		g2.fillOval(getWidth() / 2 - worldScreenRaius, getHeight() / 2 - worldScreenRaius, 2 * worldScreenRaius, 2 * worldScreenRaius);

		// paint the path source and destination

		g2.setColor(Color.BLUE);
		MutableVector3D pathSourcePosition = pathManager.getPathSourcePosition();
		Point pathSourcePoint = camera.getPointFrom3DPosition(pathSourcePosition);
		if (pathSourcePoint != null) {
			g2.fillOval(pathSourcePoint.x - 15, pathSourcePoint.y - 15, 30, 30);
		}

		g2.setColor(Color.ORANGE);
		MutableVector3D pathDestinationPosition = pathManager.getPathDestinationPosition();
		Point pathDestinationPoint = camera.getPointFrom3DPosition(pathDestinationPosition);
		if (pathDestinationPoint != null) {
			g2.fillOval(pathDestinationPoint.x - 15, pathDestinationPoint.y - 15, 30, 30);
		}

		Set<Link> pathLinks = pathManager.getPathLinks();

		// paint the links between the points in the data model

		dataModel.links.forEach(link -> {
			if (pathLinks.contains(link)) {
				g2.setStroke(new BasicStroke(pathEdgeWidth * 3));
				g2.setColor(Color.RED);
			} else {
				g2.setStroke(new BasicStroke(pathEdgeWidth));
				g2.setColor(Color.yellow);
			}
			MutableVector3D originPosition = dataModel.positions.get(link.first);
			MutableVector3D destinationPosition = dataModel.positions.get(link.second);
			List<MutableVector3D> smoothPath = getSmoothPath(originPosition, destinationPosition);
			for (int i = 0; i < smoothPath.size() - 1; i++) {
				MutableVector3D a = smoothPath.get(i);
				MutableVector3D b = smoothPath.get(i + 1);
				Point originPoint = camera.getPointFrom3DPosition(a);
				Point destinationPoint = camera.getPointFrom3DPosition(b);
				if (originPoint != null && destinationPoint != null) {
					g2.drawLine(originPoint.x, originPoint.y, destinationPoint.x, destinationPoint.y);
				}

			}

		});

		/*
		 * paint the nodes in the data model -- fade in nodes starting from 1000
		 * km altitude to help with frame rate
		 */
		double altitude = camera.getAltitudeMeters();
		if (altitude <= 1_000_000) {
			int alpha = (int) (255 * (1_000_000 - altitude) / 1_000_000);
			alpha = Math.min(alpha, 255);
			g2.setColor(new Color(255, 0, 0, alpha));
			dataModel.positions.forEach(position -> {
				Point point = camera.getPointFrom3DPosition(position);
				if (point != null) {
					g2.fillOval(point.x - nodeRadius, point.y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);
				}
			});
		}

		// paint the world grid lines
		g2.setColor(new Color(33, 180, 239));
		worldGridModel.links.forEach(link -> {
			MutableVector3D originPosition = worldGridModel.positions.get(link.first);
			MutableVector3D destinationPosition = worldGridModel.positions.get(link.second);
			Point originPoint = camera.getPointFrom3DPosition(originPosition);
			Point destinationPoint = camera.getPointFrom3DPosition(destinationPosition);
			if (originPoint != null && destinationPoint != null) {
				g2.drawLine(originPoint.x, originPoint.y, destinationPoint.x, destinationPoint.y);
			}
		});

		// paint the camera's stats
		g2.setColor(Color.white);
		g2.setFont(new Font("Serif", Font.BOLD, 30));
		int yOffset = 50;
		for (String stat : camera.getCameraStatus()) {
			g2.drawString(stat, 10, yOffset);
			yOffset += 30;
		}

	}

	private List<MutableVector3D> getSmoothPath(MutableVector3D a, MutableVector3D b) {
		List<MutableVector3D> result = new ArrayList<>();
		double angle = a.angle(b);
		int steps = (int) FastMath.ceil(180 * angle / FastMath.PI);
		for (int i = 0; i < steps; i++) {
			double stepAngle = (i * angle) / steps;
			MutableVector3D v = new MutableVector3D(a);
			v.rotateToward(b, stepAngle);
			result.add(v);
		}
		result.add(b);

		return result;
	}

	private class MouseListenerImpl implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {
			camera.setMouseDragStart(e.getPoint());
			paintOnTimer = true;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			camera.stopMouseDrag();
			paintOnTimer = true;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// do nothing

		}

	}

}
