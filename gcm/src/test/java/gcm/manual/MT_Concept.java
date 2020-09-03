package gcm.manual;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class MT_Concept{

	@Test
	public void test() {
		Concept concept = Concept.create()//
				.withCount(56)//
				.withName("Harry")//
				.withValue(45.7);//

		System.out.println(concept.getCount());
		System.out.println(concept.getName());
		System.out.println(concept.getValue());

		System.out.println(concept);

		Concept concept2 = Concept.create()//

				.withName("Harry")//
				.withCount(56)//
				.withValue(45.7);//

		assertEquals(concept, concept2);

		Concept concept3 = Concept.create()//
				//.withName("Harry")//
				.withCount(56)//
				.withValue(45.7);//
		
		assertNotEquals(concept, concept3);
	}
}
