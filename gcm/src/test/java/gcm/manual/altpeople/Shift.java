package gcm.manual.altpeople;

public class Shift {
	public static void main(String[] args) {
		
		System.out.println(getMidWay(3));
		
	}
	
	public static int getMidWay(int size) {		
		int result = 1;
		while(true) {
			int nextResult= 2*result;
			if(nextResult>=size) {
				return result;
			}
			result = nextResult;
		}		
	}
	
	public static int getNextPowerOfTwo(int value) {
		int v = value;
		int result = 1;
		while(v != 0 && result != value) {
			v/=2;
			result*=2;
		}
		return result;
	}
	
	public static int getPower2(int n) {

		if (n < 1) {
			throw new RuntimeException("Non-positive value");
		}
		
		int result = 0;
		int value = 1;
		while (value<n) {
			value*=2;
			result++;
		}
		return result;
	}

}
