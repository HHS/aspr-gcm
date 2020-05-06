package gcm.test.manual.delaunay.geovisualizer;

import java.awt.Frame;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.math3.util.Pair;

import gcm.util.delaunay.GeoCoordinate;



public class GeoVisualzerFrame extends JFrame {
	private static final long serialVersionUID = -5106781364986923139L;

	public<T extends GeoCoordinate> GeoVisualzerFrame(List<T> geoCoordinates, List<Pair<T, T>> links) {
		super();
		setSize(500, 500);
		setLocation(0, 0);
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GeoVisualizerPanel geoVisualizerPanel = new GeoVisualizerPanel(geoCoordinates,links);
		setContentPane(geoVisualizerPanel);
		// give the panel focus so that key listeners will work
		geoVisualizerPanel.setFocusable(true);
		setVisible(true);
	}

}
