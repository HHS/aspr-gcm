package gcm.manual.delaunay.planarvisualizer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.apache.commons.math3.util.Pair;

import gcm.util.TimeElapser;
import gcm.util.delaunay.PlanarDelaunaySolver;
import gcm.util.vector.Vector2D;

public class PlanarVisualizerDriver {

	public static void main(String[] args) {
		long seed = 147623563453456L;
		RandomGenerator randomGenerator = new Well44497b(seed);
		int nodeCount = 10_000;

		Map<Object, Vector2D> dataMap = new LinkedHashMap<>();
		for (int i = 0; i < nodeCount; i++) {
			Vector2D v = new Vector2D(randomGenerator.nextDouble() * 1000 - 500, randomGenerator.nextDouble() * 1000 - 500);
			dataMap.put(new Object(), v);
		}

		TimeElapser timeElapser = new TimeElapser();
		List<Pair<Object, Object>> pairs = PlanarDelaunaySolver.solve(dataMap);
		System.out.println("PlanarDelaunaySolver time = " + timeElapser.getElapsedMilliSeconds());

		System.out.println("pairs = " + pairs.size());

		new PlanarVisualzerFrame(dataMap, pairs);
	}

}
