package gcm.methodexamination;

public abstract class A implements I{

	@Override
	@ExampleAnnotation(x = "xA", y = "yA", z="zA")
	public int getIntVal(int x) {		
		return -1;
	}
	

}
