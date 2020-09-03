package gcm.manual;

import java.util.Optional;

import net.jcip.annotations.Immutable;


@Immutable
public class Concept {

	private final Scaffold scaffold;

	@Override
	public String toString() {
		return "Concept "+scaffold.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scaffold == null) ? 0 : scaffold.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		Concept other = (Concept) obj;
		if (scaffold == null) {
			if (other.scaffold != null) {
				return false;
			}
		} else if (!scaffold.equals(other.scaffold)) {
			return false;
		}
		return true;
	}

	private static class Scaffold {

		
		
		@Override
		public String toString() {
			return "[count=" + count + ", name=" + name + ", value=" + value + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((count == null) ? 0 : count.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			if (getClass() != obj.getClass()) {
				return false;
			}
			Scaffold other = (Scaffold) obj;
			if (count == null) {
				if (other.count != null) {
					return false;
				}
			} else if (!count.equals(other.count)) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		private Integer count;
		private String name;
		private Double value;

		private void copy(Scaffold scaffold) {
			this.count = scaffold.count;
			this.name = scaffold.name;
			this.value = scaffold.value;
		}
	}

	private Concept(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	public static Concept create() {
		return new Builder().build();
	}

	private static class Builder {

		private Scaffold scaffold = new Scaffold();

		private Concept build() {
			try {
				return new Concept(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		private Builder setCount(Integer count) {
			scaffold.count = count;
			return this;
		}

		private Builder setValue(Double value) {
			scaffold.value = value;
			return this;
		}

		private Builder setName(String name) {
			scaffold.name = name;
			return this;
		}

		private Builder setAll(Concept concept) {
			scaffold.copy(concept.scaffold);
			return this;
		}

	}

	public Optional<Integer> getCount() {
		return Optional.ofNullable(scaffold.count);
	}

	public Optional<String> getName() {
		return Optional.ofNullable(scaffold.name);
	}

	public Optional<Double> getValue() {
		return Optional.of(scaffold.value);
	}

	public Concept withCount(Integer count) {
		return new Builder().setAll(this).setCount(count).build();
	}

	public Concept withValue(Double value) {
		return new Builder().setAll(this).setValue(value).build();
	}
	
	public Concept withName(String name) {
		return new Builder().setAll(this).setName(name).build();
	}


}
