package gcm.manual.altpeople;

import org.apache.commons.math3.util.FastMath;

public class TradeSpace {
	private final long blockSize;
	private final long populationSize;
	private final boolean useExtendedValues;
	private long baseLayerBlockCount;
	private long treePower;

	// .replaceAll(",", "\n");

	long bitsPerByte = 8;
	long bitsPerShort = 16;
	long bitsPerInt = 32;

	long maxByteValue;
	long maxShortValue;

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

//	@Override
//	public String toString() {
//		StringBuilder builder = new StringBuilder();
//		builder.append("TradeSpace [intNodeCount=");
//		builder.append(intNodeCount);
//		builder.append(", shortNodeCount=");
//		builder.append(shortNodeCount);
//		builder.append(", byteNodeCount=");
//		builder.append(byteNodeCount);
//		builder.append("]");
//		return builder.toString();
//	}

	private TradeSpace(int populationSize, int blockSize, boolean useExtendedValues) {
		this.blockSize = blockSize;
		this.populationSize = populationSize;
		this.useExtendedValues = useExtendedValues;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TradeSpace [blockSize=");
		builder.append(blockSize);
		builder.append(", populationSize=");
		builder.append(populationSize);
		builder.append(", useExtendedValues=");
		builder.append(useExtendedValues);
		builder.append(", baseLayerBlockCount=");
		builder.append(baseLayerBlockCount);
		builder.append(", treePower=");
		builder.append(treePower);
		builder.append(", bitsPerByte=");
		builder.append(bitsPerByte);
		builder.append(", bitsPerShort=");
		builder.append(bitsPerShort);
		builder.append(", bitsPerInt=");
		builder.append(bitsPerInt);
		builder.append(", maxByteValue=");
		builder.append(maxByteValue);
		builder.append(", maxShortValue=");
		builder.append(maxShortValue);		
		builder.append(", intNodeCount=");
		builder.append(intNodeCount);
		builder.append(", shortNodeCount=");
		builder.append(shortNodeCount);
		builder.append(", byteNodeCount=");
		builder.append(byteNodeCount);
		builder.append(", bitsInIntNodes=");
		builder.append(bitsInIntNodes);
		builder.append(", bitsInShortNodes=");
		builder.append(bitsInShortNodes);
		builder.append(", bitsInByteNodes=");
		builder.append(bitsInByteNodes);
		builder.append(", originalTotalBits=");
		builder.append(originalTotalBits);
		builder.append(", totalBits=");
		builder.append(totalBits);
		builder.append(", originalBitsPersPerson=");
		builder.append(originalBitsPersPerson);
		builder.append(", bitsPerPerson=");
		builder.append(bitsPerPerson);
		builder.append(", memRatio=");
		builder.append(memRatio);
		builder.append(", treeWalk=");
		builder.append(treeWalk);
		builder.append(", bitWalk=");
		builder.append(bitWalk);
		builder.append("]");
		return builder.toString();
	}

	private void execute() {
		if (useExtendedValues) {
			maxByteValue = 127 + 128;
			maxShortValue = 32767 + 32768;
		} else {
			maxByteValue = 127;
			maxShortValue = 32767;
		}

		
		baseLayerBlockCount = populationSize/blockSize;
		if(populationSize%blockSize!=0) {
			baseLayerBlockCount++;
		}
		baseLayerBlockCount = FastMath.max(baseLayerBlockCount, 1);
		
		
		treePower = 0;
		long nodeCount = 1;
		while(nodeCount<baseLayerBlockCount) {
			treePower++;
			nodeCount*=2;
		}
		treePower++;
		
		

		byteNodeCount = 0;
		shortNodeCount = 0;
		intNodeCount = 0;

		long maxNodeValue = blockSize;
		for (long power = treePower - 1; power >= 0; power--) {
			long nodesOnLayer;
			if (power == treePower - 1) {
				nodesOnLayer = baseLayerBlockCount;
			} else {
				nodesOnLayer = 1 << power;
			}
			if (maxNodeValue <= maxByteValue) {
				byteNodeCount += nodesOnLayer;
			} else if (maxNodeValue <= maxShortValue) {
				shortNodeCount += nodesOnLayer;
			} else {
				intNodeCount += nodesOnLayer;
			}
			maxNodeValue *= 2;
		}
		if (intNodeCount > 0) {
			intNodeCount++;
		} else if (shortNodeCount > 0) {
			shortNodeCount++;
		} else {
			byteNodeCount++;
		}

		////////////////////////////////////////////////////////

		bitsInIntNodes = 32 * intNodeCount;
		bitsInShortNodes = 16 * shortNodeCount;
		bitsInByteNodes = 8 * byteNodeCount;

		totalBits = bitsInByteNodes + bitsInShortNodes + bitsInIntNodes;

		bitsPerPerson = totalBits;
		bitsPerPerson /= populationSize;
		bitsPerPerson += 1;

		//////////////

		double x = populationSize;
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
		int blockSize = 63;
		TradeSpace tradeSpace = new TradeSpace(350_000_000, blockSize, false);
		tradeSpace.execute();

		System.out.println(tradeSpace.toString().trim().replace(",","\n"));

	}
}
