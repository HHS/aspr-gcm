package gcm.test.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gcm.scenario.IntId;
import gcm.scenario.PersonId;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link IntId}
 * 	
 * @author Shawn Hatch
 *
 */
@UnitTest(target = IntId.class)
public class AT_IntId {
	/**
	 * Tests {@link IntId#getValue()}
	 */
	@Test
	public void testGetValue() {
		for(int i = 0;i<1000;i++) {
			PersonId personId = new PersonId(i);
			assertEquals(i, personId.getValue());
		}
	}
	
	/**
	 * Tests {@link IntId#compareTo(PersonId)}
	 */
	@Test
	public void testCompareTo() {
		int testSize=30;
		List<PersonId> personIds = new ArrayList<>();
		for(int i = 0;i<testSize;i++) {
			personIds.add(new PersonId(i));
		}
		
		for(int i = 0;i<testSize;i++) {			
			PersonId personIdI = personIds.get(i);
			for(int j = 0;j<testSize;j++) {
				PersonId personIdJ = personIds.get(j);
				int comparisonResult = personIdI.compareTo(personIdJ);
				if(i<j) {
					assertTrue(comparisonResult<0);
				}else {
					if(i==j) {
						assertTrue(comparisonResult==0);
					}else {
						assertTrue(comparisonResult>0);
					}
				}
			}
		}
	}

	/**
	 * Tests {@link IntId#hashCode()}
	 */
	@Test
	public void testHashCode() {
		for(int i = 0;i<1000;i++) {
			PersonId personId = new PersonId(i);
			assertEquals(personId.getValue(), personId.hashCode());
		}
	}

	/**
	 * Tests {@link IntId#equals(Object)}
	 */
	@Test
	public void testEquals() {
		int testSize=30;
		List<PersonId> personIds = new ArrayList<>();
		for(int i = 0;i<testSize;i++) {
			personIds.add(new PersonId(i));
		}
		
		for(int i = 0;i<testSize;i++) {			
			PersonId personIdI = personIds.get(i);
			for(int j = 0;j<testSize;j++) {
				PersonId personIdJ = personIds.get(j);
				if(i==j) {
					assertTrue(personIdI.equals(personIdJ));
					assertEquals(personIdI.hashCode(), personIdJ.hashCode());
				}else {
					assertFalse(personIdI.equals(personIdJ));
				}
				
			}
		}
		
	}
	
	@Test
	public void testToString() {
		for(int i = 0;i<100;i++) {
			assertEquals(Integer.toString(i), new IntId(i).toString());			
		}
	}

}
