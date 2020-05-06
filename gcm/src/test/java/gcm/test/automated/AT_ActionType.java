package gcm.test.automated;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.scenario.ActionType;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link ActionType} 
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ActionType.class)
public class AT_ActionType {

	@Test
	public void testToString() {
		for(ActionType actionType : ActionType.values()) {
			String value = actionType.toString();
			assertNotNull(value);
			assertTrue(value.length()>0);
		}
	}
	
	@Test
	public void testValueOf() {
		//nothing to test

	}
	
	@Test
	public void testValues() {
		//nothing to test
	}
	

}
