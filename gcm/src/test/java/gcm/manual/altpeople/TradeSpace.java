package gcm.manual.altpeople;

import org.apache.commons.math3.util.FastMath;

public class TradeSpace {
	private final long blockSize;
	private final long populationSize;
	private final boolean useExtendedValues;
	private long baseLayerBlockCount;
	private long treePower;
	private long ullageNodeCount;
	// .replaceAll(",", "\n");

	long bitsPerByte = 8;
	long bitsPerShort = 16;
	long bitsPerInt = 32;

	long maxByteValue;
	long maxShortValue;

	long maxLayersOfByteNodes;
	long maxLayersOfShortNodes;

	long byteLayers;
	long shortLayers;
	long intLayers;

	long intNodeCount;
	long shortNodeCount;
	long byteNodeCount;

	long bitsInIntNodes;
	long bitsInShortNodes;
	long bitsInByteNodes;

	long originalTotalBits;
	long totalBits;

	double originalBitsPersPerson;
	double bitsPerPerson;

	double memRatio;

	double treeWalk;
	double bitWalk;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TradeSpace [intNodeCount=");
		builder.append(intNodeCount);
		builder.append(", shortNodeCount=");
		builder.append(shortNodeCount);
		builder.append(", byteNodeCount=");
		builder.append(byteNodeCount);
		builder.append("]");
		return builder.toString();
	}

	private TradeSpace(int populationSize, int blockSize, boolean useExtendedValues) {
		this.blockSize = blockSize;
		this.populationSize = populationSize;
		this.useExtendedValues = useExtendedValues;
	}

	private void execute() {
		if (useExtendedValues) {
			maxByteValue = 127 + 128;
			maxShortValue = 32767 + 32768;
		} else {
			maxByteValue = 127;
			maxShortValue = 32767;
		}

		double x = populationSize;
		x /= blockSize;
		baseLayerBlockCount = (long) FastMath.ceil(x);
		treePower = (long) FastMath.ceil(FastMath.log(2, baseLayerBlockCount)) + 1;
		int lowestTreeCells = 1 << (treePower - 1);
		ullageNodeCount = lowestTreeCells - baseLayerBlockCount;

		x = maxByteValue;
		x /= blockSize;
		maxLayersOfByteNodes = (long) FastMath.floor(FastMath.log(2, x))+1;

		x = maxShortValue;
		x /= blockSize;
		maxLayersOfShortNodes = (long) FastMath.floor(FastMath.log(2, x))+1;
		maxLayersOfShortNodes -= maxLayersOfByteNodes;

		byteLayers = FastMath.min(treePower, maxLayersOfByteNodes);
		shortLayers = FastMath.min(treePower - byteLayers, maxLayersOfShortNodes);
		intLayers = treePower - shortLayers - byteLayers;

		intNodeCount = 1;
		int layerSize = 1;
		for (int i = 1; i <= intLayers; i++) {
			intNodeCount += layerSize;
			layerSize *= 2;
		}

		shortNodeCount = 0;
		for (int i = 1; i <= shortLayers; i++) {
			shortNodeCount += layerSize;
			layerSize *= 2;
		}

		byteNodeCount = 0;
		for (int i = 1; i <= byteLayers; i++) {
			byteNodeCount += layerSize;
			layerSize *= 2;
		}

		byteNodeCount -= ullageNodeCount;

		bitsInIntNodes = 32 * intNodeCount;
		bitsInShortNodes = 16 * shortNodeCount;
		bitsInByteNodes = 8 * byteNodeCount;

		totalBits = bitsInByteNodes + bitsInShortNodes + bitsInIntNodes;

		bitsPerPerson = totalBits;
		bitsPerPerson /= populationSize;
		bitsPerPerson += 1;

		//////////////

		x = populationSize;
		x /= blockSize;
		x = FastMath.ceil(FastMath.log(2, x));
		x = FastMath.pow(2, x) * 2;
		x *= 32;

		originalTotalBits = (long) x;

		originalBitsPersPerson = originalTotalBits;
		originalBitsPersPerson /= populationSize;
		originalBitsPersPerson += 1;

		memRatio = bitsPerPerson;
		memRatio /= originalBitsPersPerson;

		treeWalk = treePower - 1;

		bitWalk = blockSize;
		bitWalk /= 2;

	}

	public static void main(String[] args) {
		// TradeSpace peeps = new TradeSpace(350_000_000,31, true);
		// peeps.execute();
		// System.out.println(peeps.toString().replaceAll(",", "\n"));

		// int blockSize = i + 1;
		int blockSize = 2;
		TradeSpace tradeSpace = new TradeSpace(10, blockSize, false);
		tradeSpace.execute();
		
		System.out.println(tradeSpace.toString().replace(",","\n"));
		
	}
}
