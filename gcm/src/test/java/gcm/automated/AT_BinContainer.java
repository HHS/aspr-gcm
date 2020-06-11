package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.stats.BinContainer;
import gcm.util.stats.BinContainer.Bin;

/**
 * Test class for {@link BinContainer}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = BinContainer.class)
public class AT_BinContainer {

	/**
	 * Tests {@link BinContainer#builder(double)} construction
	 */
	@Test
	public void testBuilder() {

		assertException(() -> BinContainer.builder(0), RuntimeException.class);

		assertException(() -> BinContainer.builder(-0.5), RuntimeException.class);
	}

	/**
	 * Tests {@link BinContainer#addValue(double, int)}
	 */
	@Test
	public void testAddValue() {
		BinContainer.Builder builder = BinContainer.builder(3);
		builder.addValue(2.3, 5);
		builder.addValue(3, 2);
		builder.addValue(10, 1);
		builder.addValue(11, 0);
		builder.addValue(-10, 3);
		BinContainer binContainer = builder.build();

		List<Bin> expected = new ArrayList<>();
		expected.add(new Bin(-12.0, -9.0, 3));
		expected.add(new Bin(-9.0, -6.0, 0));
		expected.add(new Bin(-6.0, -3.0, 0));
		expected.add(new Bin(-3.0, 0.0, 0));
		expected.add(new Bin(0.0, 3.0, 5));
		expected.add(new Bin(3.0, 6.0, 2));
		expected.add(new Bin(6.0, 9.0, 0));
		expected.add(new Bin(9.0, 12.0, 1));

		List<Bin> actual = new ArrayList<>();
		for (int i = 0; i < binContainer.binCount(); i++) {
			actual.add(binContainer.getBin(i));
		}

		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link BinContainer#binCount()}
	 */
	@Test
	public void testBinCount() {
		BinContainer.Builder builder = BinContainer.builder(3);
		builder.addValue(2.3, 5);
		builder.addValue(3, 2);
		builder.addValue(10, 1);
		builder.addValue(11, 0);
		builder.addValue(-10, 3);
		BinContainer binContainer = builder.build();

		assertEquals(8,binContainer.binCount());
		
	}

	/**
	 * Tests {@link BinContainer#getBin(int)}
	 */
	@Test
	public void testGetBin() {
		BinContainer.Builder builder = BinContainer.builder(3);
		builder.addValue(2.3, 5);
		builder.addValue(3, 2);
		builder.addValue(10, 1);
		builder.addValue(11, 0);
		builder.addValue(-10, 3);
		BinContainer binContainer = builder.build();

		assertEquals(new Bin(-12.0, -9.0, 3), binContainer.getBin(0));
		assertEquals(new Bin(-9.0, -6.0, 0), binContainer.getBin(1));
		assertEquals(new Bin(-6.0, -3.0, 0), binContainer.getBin(2));
		assertEquals(new Bin(-3.0, 0.0, 0), binContainer.getBin(3));
		assertEquals(new Bin(0.0, 3.0, 5), binContainer.getBin(4));
		assertEquals(new Bin(3.0, 6.0, 2), binContainer.getBin(5));
		assertEquals(new Bin(6.0, 9.0, 0), binContainer.getBin(6));
		assertEquals(new Bin(9.0, 12.0, 1), binContainer.getBin(7));

	}

}
