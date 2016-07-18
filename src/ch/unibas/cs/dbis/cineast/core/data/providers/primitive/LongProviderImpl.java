package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public class LongProviderImpl implements LongProvider {

	private final long value;
	
	public LongProviderImpl(long value){
		this.value = value;
	}

	@Override
	public long getLong() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LongProviderImpl other = (LongProviderImpl) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("LongProviderImpl [value=%s]", value);
	}
}