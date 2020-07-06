package gcm.automated;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import org.junit.Test;

import gcm.simulation.BaseElement;
import gcm.simulation.Context;
import gcm.util.MemoryPartition;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;

/**
 * Tests for {@link BaseElement}
 * @author Shawn Hatch
 *
 */

@UnitTest(target = BaseElement.class)

public class AT_BaseElement {
	/**
	 * Tests {@link BaseElement#collectMemoryLinks(MemoryPartition)}
	 */
	@Test
	@UnitTestMethod(name = "collectMemoryLinks", args = {MemoryPartition.class})
	public void testCollectMemoryLinks() {
		//nothing to test
	}

	/**
	 * Tests {@link BaseElement#init(Context)}
	 */
	@Test
	@UnitTestMethod(name = "init", args = {Context.class})
	public void testInit() {
		BaseElement baseElement = new BaseElement();
		assertFalse(baseElement.isInitialized());
		baseElement.init(null);
		assertTrue(baseElement.isInitialized());
	}
	
	/**
	 * Tests {@link BaseElement#BaseElement()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		BaseElement baseElement = new BaseElement();
		assumeNotNull(baseElement);
	}
	/**
	 * Tests {@link BaseElement#isInitialized()}
	 */
	@Test	
	@UnitTestMethod(name = "isInitialized", args = {})
	public void testIsInitialized() {

		// deferred to testInit()
	}

}
