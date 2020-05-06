package gcm.test.manual.delaunay.planarvisualizer;



import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.apache.commons.math3.util.Pair;

import gcm.util.TimeElapser;
import gcm.util.delaunay.PlanarDelaunaySolver;
import gcm.util.delaunay.SimplePlanarCoordinate;

public class PlanarVisualizerDriver {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		long seed = 147623563453456L;
		RandomGenerator randomGenerator = new Well44497b(seed);
		int nodeCount = 10_000;

		List<SimplePlanarCoordinate> planarCoordinates = new ArrayList<>();
		for(int i = 0;i<nodeCount;i++) {
			SimplePlanarCoordinate simplePlanarCoordinate = new SimplePlanarCoordinate(randomGenerator.nextDouble()*1000-500, randomGenerator.nextDouble()*1000-500);
			planarCoordinates.add(simplePlanarCoordinate);
		}

		
		TimeElapser timeElapser = new TimeElapser();
		List<Pair<SimplePlanarCoordinate, SimplePlanarCoordinate>> pairs = PlanarDelaunaySolver.solve(planarCoordinates);
		System.out.println("PlanarDelaunaySolver time = " + timeElapser.getElapsedMilliSeconds());

		System.out.println("pairs = "+pairs.size());
		
		new PlanarVisualzerFrame(planarCoordinates,pairs);
	}


}
