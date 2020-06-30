package gcm.methodexamination;

public class P implements I{

	@Override
	@ExampleAnnotation(x = "xP", y = "yP", z="zP")
	public int getIntVal(int x) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@ExampleAnnotation(x = "xP1", y = "yP1", z="zP1")
	public int getDoubleVal(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@ExampleAnnotation(x = "xP2", y = "yP2", z="zP2")
	public void doSomething() {
		
	}

}
