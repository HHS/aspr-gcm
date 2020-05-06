package gcm.test.manual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import gcm.test.manual.BinContainer.Bin;
import gcm.util.TimeElapser;
import gcm.util.delaunay.GeoCoordinate;
import gcm.util.delaunay.GeoDelaunaySolver;
import gcm.util.earth.ECC;
import gcm.util.earth.Earth;
import gcm.util.earth.LatLon;
import gcm.util.graph.ArrayPathManager;
import gcm.util.graph.GenericMutableGraph;
import gcm.util.graph.GraphPathSolver;
import gcm.util.graph.MutableGraph;

public class Virginia_Instate_Travel {
	private static class Person {
		Node homeNode;
		Node workNode;
		double distanceToWork;
		double minDistanceToWork;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Person [homeTract=");
			builder.append(homeNode);
			builder.append(", workTract=");
			builder.append(workNode);
			builder.append(", distanceToWork=");
			builder.append(distanceToWork);
			builder.append(", minDistanceToWork=");
			builder.append(minDistanceToWork);
			builder.append("]");
			return builder.toString();
		}

	}

	private final Path tractFile;
	private final Path populationFile;

	private Virginia_Instate_Travel(Path tractFile, Path populationFile) {
		this.tractFile = tractFile;
		this.populationFile = populationFile;
	}

	public static void main(String[] args) throws IOException {
		Path tractFile = Paths.get(args[0]);
		Path populationFile = Paths.get(args[1]);
		new Virginia_Instate_Travel(tractFile, populationFile).execute();
	}

	private void execute() throws IOException {
		System.out.println("Virginia_Instate_Travel.execute() started");

		// Create a common earth
		Earth earth = Earth.fromMeanRadius();

		// Collect VA tracts
		List<Node> nodes = Files.readAllLines(tractFile).stream().skip(1).filter(line -> line.startsWith("51")).map(line -> {
			String[] strings = line.split(",");
			String name = strings[0];
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			return new Node(name, earth, lat, lon);
		}).collect(Collectors.toCollection(ArrayList::new));
		System.out.println("nodes collected: " + nodes.size());

		// Use a Delaunay solver to create node pairs that will become edges
		List<Pair<Node, Node>> pairs = GeoDelaunaySolver.solve(nodes);
		System.out.println("Delaunay Solver complete: " + pairs.size());

		// Convert the node pairs into edges for a graph -- two edges per pair
		List<Edge> edges = new ArrayList<>();
		for (Pair<Node, Node> pair : pairs) {
			edges.add(new Edge(earth, pair.getFirst(), pair.getSecond()));
			edges.add(new Edge(earth, pair.getSecond(), pair.getFirst()));
		}
		System.out.println("Edges created " + edges.size());

		// Construct the mutable graph
		MutableGraph<Node, Edge> graph = new GenericMutableGraph<>();

		// Add the nodes to the graph -- this ensures that orphan nodes will be
		// included, but we don't expect any.
		nodes.forEach(node -> graph.addNode(node));

		// Add the edges to the graph
		edges.forEach(edge -> {
			graph.addEdge(edge, edge.first, edge.second);
		});

		System.out.println("Graph formed");

		// Create a map from node ids to nodes so that people can find nodes for
		// home and work
		Map<String, Node> nodeMap = new LinkedHashMap<>();
		for (Node node : nodes) {
			nodeMap.put(node.name, node);
		}

		// Load the people who both live and work in VA
		List<Person> people = Files.readAllLines(populationFile).stream().skip(1).filter(line -> {
			String[] strings = line.split(",", -1);
			return strings[3].startsWith("51");
		}).map(line -> {
			String[] strings = line.split(",", -1);
			Person person = new Person();
			person.homeNode = nodeMap.get(strings[1].trim().substring(0, 11));
			person.workNode = nodeMap.get(strings[3].trim().substring(0, 11));
			person.distanceToWork = earth.getGroundDistanceFromECC(person.homeNode.ecc, person.workNode.ecc);
			return person;
		}).collect(Collectors.toList());
		System.out.println("People loaded " + people.size());

		BinContainer binContainer = new BinContainer(5000);
		for (Person person : people) {
			binContainer.addValue(person.distanceToWork, 1);
		}
		System.out.println();
		for (int i = 0; i < binContainer.binCount(); i++) {
			Bin bin = binContainer.getBin(i);
			System.out.println(bin.getLowerBound() + "\t" + bin.getUpperBound() + "\t" + bin.getCount());
		}

		boolean calculateShorterPaths = false;

		if (calculateShorterPaths) {
			boolean usePathManager = true;
			TimeElapser timeElapser = new TimeElapser();

			if (usePathManager) {
				ArrayPathManager<Node, Edge> pathManager = new ArrayPathManager<>(graph, this::getEdgeCost, this::getTravelCost);
				// For each person, find the minimum work distance
				for (int i = 0; i < people.size(); i++) {
					Person person = people.get(i);
					// find the shortest path from the person's home to work
					// node
					if (person.homeNode.equals(person.workNode)) {
						person.minDistanceToWork = person.distanceToWork;
					} else {
						Node node = person.homeNode;
						Node nextToFirstNode = null;
						Node nextToLastNode = null;
						while (node != person.workNode) {
							Node nextNode = pathManager.getNextNode(node, person.workNode);
							if (node.equals(person.homeNode)) {
								nextToFirstNode = nextNode;
							}
							if (nextNode.equals(person.workNode)) {
								nextToLastNode = node;
							}
							node = nextNode;
						}
						person.minDistanceToWork = earth.getGroundDistanceFromECC(nextToFirstNode.ecc, nextToLastNode.ecc);
					}
					if (i % 100_000 == 0) {
						System.out.println(i + "\t" + person.distanceToWork + "\t" + person.minDistanceToWork);
						double timePerPerson = timeElapser.getElapsedSeconds() / (i + 1);
						double remainingTime = (people.size() - i - 1) * timePerPerson;
						remainingTime /= 60;
						System.out.println("time remaining " + remainingTime + " minutes");
					}
				}

			} else {

				// For each person, find the minimum work distance
				for (int i = 0; i < people.size(); i++) {
					Person person = people.get(i);

					if (person.homeNode.equals(person.workNode)) {
						person.minDistanceToWork = person.distanceToWork;
					} else {
						// find the shortest path from the person's home to work
						// node
						gcm.util.graph.Path<Edge> path = GraphPathSolver.getPath(graph, //
								person.homeNode, //
								person.workNode, //
								this::getEdgeCost, this::getTravelCost);

						// convert the iterable edges in the path into a list
						List<Edge> pathEdges = new ArrayList<>();
						path.getEdges().forEach(edge -> pathEdges.add(edge));

						// if there is only one edge, then just use the
						// previously
						// calculated distance
						if (pathEdges.size() < 2) {
							person.minDistanceToWork = person.distanceToWork;
						} else {
							// calculate the distance one step from both ends
							Node node1 = pathEdges.get(1).first;
							Node node2 = pathEdges.get(pathEdges.size() - 1).first;
							person.minDistanceToWork = earth.getGroundDistanceFromECC(node1.ecc, node2.ecc);
						}
					}
					System.out.println(i + "\t" + person.distanceToWork + "\t" + person.minDistanceToWork);
					double timePerPerson = timeElapser.getElapsedSeconds() / (i + 1);
					double remainingTime = (people.size() - i - 1) * timePerPerson;
					remainingTime /= 60;
					System.out.println("time remaining " + remainingTime + " minutes");
				}
			}

			binContainer = new BinContainer(5000);
			for (Person person : people) {
				binContainer.addValue(person.minDistanceToWork, 1);
			}

			System.out.println();
			for (int i = 0; i < binContainer.binCount(); i++) {
				Bin bin = binContainer.getBin(i);
				System.out.println(bin.getLowerBound() + "\t" + bin.getUpperBound() + "\t" + bin.getCount());
			}
		}
	}

	private static class Node implements GeoCoordinate {
		private final String name;
		private final ECC ecc;
		private final Earth earth;
		private final double latitude;
		private final double longitude;

		public Node(String name, Earth earth, double latitude, double longitude) {
			this.name = name;
			this.earth = earth;
			this.latitude = latitude;
			this.longitude = longitude;
			LatLon latLon = new LatLon(latitude, longitude);
			ecc = earth.getECCFromLatLon(latLon);
		}

		@Override
		public double getLatitude() {
			return latitude;
		}

		@Override
		public double getLongitude() {
			return longitude;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static class Edge {
		private final Node first;
		private final Node second;
		private final double edgeCost;

		public Edge(Earth earth, Node first, Node second) {
			this.first = first;
			this.second = second;
			LatLon latLon1 = new LatLon(first.getLatitude(), first.getLongitude());
			LatLon latLon2 = new LatLon(second.getLatitude(), second.getLongitude());
			edgeCost = earth.getGroundDistanceFromLatLon(latLon1, latLon2);
		}

		public double getEdgeCost() {
			return edgeCost;
		}

		@Override
		public String toString() {
			return first.name + "->" + second.name;
		}
	}

	private double getEdgeCost(Edge edge) {
		return edge.getEdgeCost();
	}

	private double getTravelCost(Node node1, Node node2) {
		return node1.earth.getGroundDistanceFromECC(node1.ecc, node2.ecc);
	}

}
