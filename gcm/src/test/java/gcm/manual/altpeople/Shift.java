package gcm.manual.altpeople;

public class Shift {
	public static void main(String[] args) {
		
		System.out.println(getNextPowerOfTwo(37));

	}
	
	private static int getNextPowerOfTwo(int value) {
		int v = value;
		int result = 1;
		while(v != 0 && result != value) {
			v/=2;
			result*=2;
		}
		return result;
	}

}
