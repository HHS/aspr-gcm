package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;
import gcm.util.containers.EnumContainer;
/**
 * Test class for {@link EnumContainer}
 * @author Shawn Hatch
 *
 */

@UnitTest(target = EnumContainer.class)
public class AT_EnumContainer {
	public enum Animal {
		CAT, PIG, SHEEP, DOG, HORSE;
	}

	@Test
	public void testConstructor() {
		assertNotNull(new EnumContainer(Animal.class, Animal.DOG));

		// Test preconditions

		// if the class is null
		assertException(() -> new EnumContainer(null, Animal.DOG), IllegalArgumentException.class);
		assertException(() -> new EnumContainer(null, Animal.DOG,100), IllegalArgumentException.class);

		// if the class is not an enumeration
		assertException(() -> new EnumContainer(Integer.class, Animal.DOG), IllegalArgumentException.class);
		assertException(() -> new EnumContainer(Integer.class, Animal.DOG,100), IllegalArgumentException.class);

		// if the default value is null
		assertException(() -> new EnumContainer(Animal.class, null), IllegalArgumentException.class);
		assertException(() -> new EnumContainer(Animal.class, null,100), IllegalArgumentException.class);

		// if the default is not a member of the enum
		assertException(() -> new EnumContainer(Animal.class, 234), IllegalArgumentException.class);
		assertException(() -> new EnumContainer(Animal.class, 234,100), IllegalArgumentException.class);

		assertException(() ->new EnumContainer(Animal.class, Animal.DOG,-1), IllegalArgumentException.class);
		
	}

	/**
	 * Tests {@link EnumContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(name = "getValue", args = {int.class})
	public void testGetValue() {
		EnumContainer enumContainer = new EnumContainer(Animal.class, Animal.DOG);
		enumContainer.setValue(3, Animal.CAT);
		enumContainer.setValue(5, Animal.CAT);
		enumContainer.setValue(2, Animal.SHEEP);
		enumContainer.setValue(0, Animal.HORSE);
		enumContainer.setValue(2, Animal.PIG);
		
		assertEquals(Animal.HORSE,enumContainer.getValue(0));
		assertEquals(Animal.DOG,enumContainer.getValue(1));
		assertEquals(Animal.PIG,enumContainer.getValue(2));
		assertEquals(Animal.CAT,enumContainer.getValue(3));
		assertEquals(Animal.DOG,enumContainer.getValue(4));
		assertEquals(Animal.CAT,enumContainer.getValue(5));
		assertEquals(Animal.DOG,enumContainer.getValue(6));

		
		enumContainer = new EnumContainer(Animal.class, Animal.DOG,100);
		enumContainer.setValue(3, Animal.CAT);
		enumContainer.setValue(5, Animal.CAT);
		enumContainer.setValue(2, Animal.SHEEP);
		enumContainer.setValue(0, Animal.HORSE);
		enumContainer.setValue(2, Animal.PIG);
		
		assertEquals(Animal.HORSE,enumContainer.getValue(0));
		assertEquals(Animal.DOG,enumContainer.getValue(1));
		assertEquals(Animal.PIG,enumContainer.getValue(2));
		assertEquals(Animal.CAT,enumContainer.getValue(3));
		assertEquals(Animal.DOG,enumContainer.getValue(4));
		assertEquals(Animal.CAT,enumContainer.getValue(5));
		assertEquals(Animal.DOG,enumContainer.getValue(6));
		
		// Test pre-conditions

		EnumContainer preConditionEnumContainer = enumContainer;
		// if the index is negative
		assertException(() -> preConditionEnumContainer.getValue(-1), IllegalArgumentException.class);

	}

	/**
	 * Test {@link EnumContainer#setValue(int, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setValue", args = {int.class,Object.class})
	public void testSetValue() {
		
		Map<Integer,Animal> animalMap = new LinkedHashMap<>();
		
		EnumContainer enumContainer = new EnumContainer(Animal.class, Animal.DOG);
		Random random = new Random(4545456567994423L);
		for(int i = 0;i<1000;i++) {
			int index = random.nextInt(6);
			int ord = random.nextInt(Animal.values().length);
			Animal animal = Animal.values()[ord];
			animalMap.put(index, animal);
			enumContainer.setValue(index, animal);			
			assertEquals(animal,enumContainer.getValue(index));
		}

		enumContainer = new EnumContainer(Animal.class, Animal.DOG,100);		
		for(int i = 0;i<1000;i++) {
			int index = random.nextInt(6);
			int ord = random.nextInt(Animal.values().length);
			Animal animal = Animal.values()[ord];
			animalMap.put(index, animal);
			enumContainer.setValue(index, animal);			
			assertEquals(animal,enumContainer.getValue(index));
		}

		
		// Test pre-conditions
		EnumContainer preConditionEnumContainer = enumContainer;
		// if the index is negative
		assertException(() -> preConditionEnumContainer.setValue(-1,Animal.HORSE), IllegalArgumentException.class);

		// if the value is null
		assertException(() -> preConditionEnumContainer.setValue(1,null), IllegalArgumentException.class);

		// if the value is not a member of the enumeration
		assertException(() -> preConditionEnumContainer.setValue(1,45), IllegalArgumentException.class);
	}

	
}
