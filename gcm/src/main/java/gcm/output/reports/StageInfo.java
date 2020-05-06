package gcm.output.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gcm.scenario.MaterialsProducerId;
import gcm.scenario.StageId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * Represents the primary information that was associated with a stage just
 * prior to its removal from the simulation. It is designed to be used by
 * {@link Report} classes that need insight into a stage after the stage has
 * been removed. Construction is conducted via the contained Builder class.
 * 
 * @author Shawn Hatch
 *
 */

@Source(status = TestStatus.UNEXPECTED)
@Immutable
public final class StageInfo {

	private StageInfo(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	private static class Scaffold {
		private StageId stageId;

		private MaterialsProducerId materialsProducerId;

		private boolean stageOffered;

		private List<BatchInfo> batchInfos = new ArrayList<>();
	}

	private final Scaffold scaffold;

	@NotThreadSafe
	public static class StageInfoBuilder {
		private Scaffold scaffold = new Scaffold();

		private void validate() {
			if (scaffold.stageId == null) {
				throw new RuntimeException("null stage id");
			}
			if (scaffold.materialsProducerId == null) {
				throw new RuntimeException("null materials producer id");
			}
		}

		/**
		 * Builds the {@link StageInfo} from the collected data
		 * 
		 * @throws RuntimeException
		 *             <li>if no stage id was collected
		 *             <li>if no materials producer id was collected
		 * 
		 */
		public StageInfo build() {
			try {
				validate();
				return new StageInfo(scaffold);
			} finally {
				scaffold = new Scaffold();
			}

		}

		/**
		 * Sets the stage id
		 * 
		 * @throws RuntimeException
		 *             if the stage id is null
		 */
		public void setStageId(StageId stageId) {
			if (stageId == null) {
				throw new RuntimeException("null stage id");
			}
			scaffold.stageId = stageId;
		}

		/**
		 * Sets the {@link MaterialsProducerId}
		 * 
		 * @throws RuntimeException
		 *             if the materials producer id is null
		 */
		public void setMaterialsProducerId(MaterialsProducerId materialsProducerId) {
			if (materialsProducerId == null) {
				throw new RuntimeException("null materials producer id");
			}
			scaffold.materialsProducerId = materialsProducerId;
		}

		/**
		 * Sets the stage offered state
		 */
		public void setStageOffered(boolean stageOffered) {
			scaffold.stageOffered = stageOffered;
		}

		/**
		 * Add a {@link BatchInfo}
		 * 
		 * throws {@link RuntimeException} if the batch info is null
		 */
		public void addBatchInfo(BatchInfo batchInfo) {
			if (batchInfo == null) {
				throw new RuntimeException("null batch info");
			}
			scaffold.batchInfos.add(batchInfo);
		}
	}

	/**
	 * Returns the stage id
	 */
	public StageId getStageId() {
		return scaffold.stageId;
	}

	/**
	 * Returns the materials producer id
	 */
	public MaterialsProducerId getMaterialsProducerId() {
		return scaffold.materialsProducerId;
	}

	/**
	 * Returns the stage offer state
	 */
	public boolean isStageOffered() {
		return scaffold.stageOffered;
	}

	/**
	 * Returns the batch infos for the stage
	 */
	public List<BatchInfo> getBatchInfos() {
		return Collections.unmodifiableList(scaffold.batchInfos);
	}

}
