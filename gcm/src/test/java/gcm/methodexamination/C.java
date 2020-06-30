package gcm.methodexamination;

public class C extends A{

	@Override
	@ExampleAnnotation(x = "xC", y = "yC", z="zC")
	public int getDoubleVal(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	@ExampleAnnotation(x = "xC1", y = "yC1", z="zC1")
	public int getIntVal(int x) {		
		return -1;
	}

}
