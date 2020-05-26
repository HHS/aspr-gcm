package gcm.test.manual;

import static gcm.test.support.EnvironmentSupport.getRandomGenerator;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import gcm.test.manual.Circle2D.SolutionAlgorithm;
import gcm.test.manual.delaunay.planarvisualizer.PlanarVisualzerFrame;
import gcm.test.support.SeedProvider;
import gcm.util.vector.Vector2D;

public class CircleTest {
	private static SeedProvider SEED_PROVIDER = new SeedProvider(134563453453453L);

	private CircleTest() {

	}

	@SuppressWarnings("unused")
	private void execute() {
		
		Random random = new Random();

		int seedIndex = random.nextInt(1000);
		//seedIndex = 40;
		
		System.out.println("seedIndex = "+seedIndex);

		final long seed = SEED_PROVIDER.getSeedValue(seedIndex);

		RandomGenerator randomGenerator = getRandomGenerator(seed);
		int n = 30;
		List<Vector2D> points = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			Vector2D point = new Vector2D(randomGenerator.nextDouble(),randomGenerator.nextDouble());
			points.add(point);
		}
		
		Circle2D circle2d1 = new Circle2D(points,SolutionAlgorithm.CENTROID);
		System.out.println("solution 1 "+circle2d1);
		for(Vector2D point : points) {
			assertTrue(circle2d1.contains(point));
		}
		
		Circle2D circle2d2 = new Circle2D(points,SolutionAlgorithm.N4);
		System.out.println("solution 2 "+circle2d2);
		for(Vector2D point : points) {
			assertTrue(circle2d2.contains(point));
			
		}
		
		Circle2D circle2d3 = new Circle2D(points,SolutionAlgorithm.COLLAPSING_BUBBLE);
		System.out.println("solution 3 "+circle2d3);
		for(Vector2D point : points) {
			assertTrue(circle2d3.contains(point));			
		}
		
		Map<Integer, Vector2D> dataMap = new LinkedHashMap<>();
		int base = 0;
		for (int i = 0; i < points.size(); i++) {			
			dataMap.put(i+base, points.get(i));
		}
		base += points.size();

		
		List<Pair<Integer, Integer>> pairs = new ArrayList<>();
		
		int cirleSteps = 100;
		for (int i = 0; i < cirleSteps; i++) {
			Vector2D center = circle2d1.getCenter();
			double angle = FastMath.PI*2*i/cirleSteps;
			Vector2D v = new Vector2D(circle2d1.getRadius(),0).rotate(angle).add(center);
			dataMap.put(base+i,v);
		}
		
		for (int i = 0; i < cirleSteps-1; i++) {
			pairs.add(new Pair<Integer,Integer>(i+base,i+base+1));
		}
		
		base+=cirleSteps;

		for (int i = 0; i < cirleSteps; i++) {
			Vector2D center = circle2d2.getCenter();
			double angle = FastMath.PI*2*i/cirleSteps;
			Vector2D v = new Vector2D(circle2d2.getRadius(),0).rotate(angle).add(center);
			dataMap.put(base+i,v);
		}
		
		for (int i = 0; i < cirleSteps-1; i++) {
			pairs.add(new Pair<Integer,Integer>(i+base,i+base+1));
		}
		
		base+=cirleSteps;

		for (int i = 0; i < cirleSteps; i++) {
			Vector2D center = circle2d3.getCenter();
			double angle = FastMath.PI*2*i/cirleSteps;
			Vector2D v = new Vector2D(circle2d3.getRadius(),0).rotate(angle).add(center);
			dataMap.put(base+i,v);
		}
		
		for (int i = 0; i < cirleSteps-1; i++) {
			pairs.add(new Pair<Integer,Integer>(i+base,i+base+1));
		}

		new PlanarVisualzerFrame(dataMap, pairs);		

	}

	public static void main(String[] args) {
		new CircleTest().execute();
	}
}
