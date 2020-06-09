package gcm.automated.support;
/**
 * Implementor for proxy class testing
 * @author Shawn Hatch
 *
 */
public final class CatImpl implements Cat{
	private final int lives;
	
	public CatImpl(int lives) {
		this.lives = lives;
	}
	
	@Override
	public int getLives() {
		return lives;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lives;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Cat)) {
			return false;
		}
		Cat other = (Cat) obj;
		if (lives != other.getLives()) {
			return false;
		}
		return true;
	}



	

}
