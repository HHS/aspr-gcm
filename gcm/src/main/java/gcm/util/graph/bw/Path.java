package gcm.util.graph.bw;

import java.util.List;




public interface Path<E>  {
	
	public List<E> getEdges();
	
	public int length();
	
	public boolean isEmpty();
	
}
