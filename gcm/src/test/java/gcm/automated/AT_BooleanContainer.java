package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;
import gcm.util.containers.BooleanContainer;

/**
 * Test class for {@link BooleanContainer}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = BooleanContainer.class)
public class AT_BooleanContainer {

	
	/**
	 * Test {@link BooleanContainer} constructor test
	 */
	@Test
	public void testDefaultValue() {
		BooleanContainer booleanContainer = new BooleanContainer(true);

		for (int i = 0; i < 10; i++) {
			assertTrue(booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(true, 100);

		for (int i = 0; i < 10; i++) {
			assertTrue(booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(false);

		for (int i = 0; i < 10; i++) {
			assertFalse(booleanContainer.get(i));		
		}
		
		booleanContainer = new BooleanContainer(false,100);

		for (int i = 0; i < 10; i++) {
			assertFalse(booleanContainer.get(i));
		}

	}

	/**
	 * Test {@link BooleanContainer#get(int)}
	 */
	@Test
	@UnitTestMethod(name="get",args= {int.class})
	public void testGet() {
		Random random = new Random(53463457457456456L);
		int n = 1000;
		boolean[] array = new boolean[n];
		for (int i = 0; i < n; i++) {
			array[i] = random.nextBoolean();
		}

		BooleanContainer booleanContainer = new BooleanContainer(true);

		for (int i = 0; i < n; i++) {
			booleanContainer.set(i, array[i]);
		}

		for (int i = 0; i < n; i++) {
			assertEquals(array[i], booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(true, n);

		for (int i = 0; i < n; i++) {
			booleanContainer.set(i, array[i]);
		}

		for (int i = 0; i < n; i++) {
			assertEquals(array[i], booleanContainer.get(i));
		}

	}

	/**
	 * Test {@link BooleanContainer#set(int, boolean)}
	 */
	@Test
	@UnitTestMethod(name="set",args= {int.class,boolean.class})
	public void testSet() {
		// proxy via testGet()
	}
}
