package gcm.test.manual;

import java.util.Random;

import org.apache.commons.math3.util.FastMath;

import gcm.util.TimeElapser;
import gcm.util.vector.MutableVector3D;
import gcm.util.vector.Vector3D;

public class MT_VectorComparison {
	private static void test0() {
		Random random = new Random();
		TimeElapser timeElapser = new TimeElapser();

		for (int i = 0; i < 1000000; i++) {
			Vector3D a = new Vector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
			Vector3D b = new Vector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
			double theta = random.nextDouble() * 2 * FastMath.PI;		
		}

		System.out.println("MT_VectorComparison.test0() " + timeElapser.getElapsedMilliSeconds());

	}
	private static void test1() {
		Random random = new Random();
		TimeElapser timeElapser = new TimeElapser();

		for (int i = 0; i < 1000000; i++) {
			Vector3D a = new Vector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
			Vector3D b = new Vector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
			double theta = random.nextDouble() * 2 * FastMath.PI;
			a.rotateToward(b, theta);
		}

		System.out.println("MT_VectorComparison.test1() " + timeElapser.getElapsedMilliSeconds());

	}

	private static void test2() {
		Random random = new Random();
		TimeElapser timeElapser = new TimeElapser();

		for (int i = 0; i < 1000000; i++) {
			MutableVector3D a = new MutableVector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
			MutableVector3D b = new MutableVector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
			double theta = random.nextDouble() * 2 * FastMath.PI;
			a.rotateToward(b, theta);
		}

		System.out.println("MT_VectorComparison.test2() " + timeElapser.getElapsedMilliSeconds());
	}

	
	private static void test3() {
		Random random = new Random();
		TimeElapser timeElapser = new TimeElapser();

		Vector3D sum = new Vector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
		for (int i = 0; i < 1000000; i++) {
			Vector3D b = new Vector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());	
			
		}
		

		System.out.println("MT_VectorComparison.test3() " + timeElapser.getElapsedMilliSeconds());

	}
	
	private static void test4() {
		Random random = new Random();
		TimeElapser timeElapser = new TimeElapser();

		Vector3D sum = new Vector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
		for (int i = 0; i < 1000000; i++) {
			Vector3D b = new Vector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());	
			sum = sum.add(b);
		}
		System.out.println("MT_VectorComparison.test4() " + timeElapser.getElapsedMilliSeconds());

	}

	private static void test5() {
		Random random = new Random();
		TimeElapser timeElapser = new TimeElapser();

		MutableVector3D sum = new MutableVector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
		for (int i = 0; i < 1000000; i++) {
			MutableVector3D b = new MutableVector3D(random.nextDouble(), random.nextDouble(), random.nextDouble());	
			sum.add(b);
		}		
		System.out.println("MT_VectorComparison.test5() " + timeElapser.getElapsedMilliSeconds());
	}
	
	
	public static void main(String[] args) {
		test0();
		test1();
		test2();
		
		test3();
		test4();
		test5();
	}
}
