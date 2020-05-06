package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.output.simstate.MemoryReportItem;
import gcm.output.simstate.MemoryReportItem.MemoryReportItemBuilder;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.MemSizer;
import gcm.util.MemoryLink;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor of {@link MemoryReportManager}
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class MemoryReportManagerImpl extends BaseElement implements MemoryReportManager {

	private Context context;

	/*
	 * Remove the proxy wrappers from memory links that may be present if the
	 * profile report is on.
	 */
	private List<MemoryLink> getNonProxiedMemoryLinks(List<MemoryLink> proxiedMemoryLinks) {
		List<MemoryLink> result = new ArrayList<>();
		for (MemoryLink memoryLink : proxiedMemoryLinks) {
			Object parent = profileManager.getOrginalInstance(memoryLink.getParent());
			Object child = profileManager.getOrginalInstance(memoryLink.getChild());
			MemoryLink unproxiedMemoryLink = new MemoryLink(parent, child, memoryLink.getDescriptor());
			result.add(unproxiedMemoryLink);
		}
		return result;
	}

	@Override
	public void generateMemoryReport() {
		double time = context.getEventManager().getTime();
		OutputItemManager outputItemManager = context.getOutputItemManager();

		/*
		 * Collect the memory links that form the memory partition. The context
		 * will pass the memory partition to its children and they will do the
		 * same. The memory partition will be used to for a tree of nodes that
		 * will aide in our calculations.
		 */

		MemoryPartition memoryPartition = new MemoryPartition();
		memoryPartition.addMemoryLink(null, context, "Context");
		context.collectMemoryLinks(memoryPartition);

		/*
		 * Validate the links and form them into a tree. Strip off any proxy
		 * wrappers that appear when profile reporting is on.
		 */
		List<MemoryLink> memoryLinks = memoryPartition.getMemoryLinks();
		memoryLinks = getNonProxiedMemoryLinks(memoryLinks);

		// Rule 1 : every child has exactly one link
		// Rule 2 : every non-null parent is also a child
		// Rule 3 : the links do not form any loops

		/*
		 * Create the nodes that will form the tree. Enforce Rule 1.
		 */
		int nodeId = 0;
		List<Node> rootNodes = new ArrayList<>();
		Map<Object, Node> nodeMap = new LinkedHashMap<>();
		for (MemoryLink memoryLink : memoryLinks) {
			Node node = new Node();
			node.id = nodeId++;
			node.descriptor = memoryLink.getDescriptor();
			node.focus = memoryLink.getChild();

			Node previousNode = nodeMap.put(memoryLink.getChild(), node);
			if (previousNode != null) {
				// violation of Rule 1
				throw new RuntimeException("multiple memory links for " + memoryLink.getChild());
			}
		}

		/*
		 * Link the nodes in parent/child linkages, thus forming the tree.
		 * Enforce Rule 2.
		 */
		for (MemoryLink memoryLink : memoryLinks) {
			Node childNode = nodeMap.get(memoryLink.getChild());
			if (memoryLink.getParent() == null) {
				rootNodes.add(childNode);
			} else {
				Node parentNode = nodeMap.get(memoryLink.getParent());
				if (parentNode == null) {
					// violation of Rule 2
					throw new RuntimeException("cannot locate parent node for " + memoryLink.getDescriptor());
				} else {
					childNode.parentNode = parentNode;
					parentNode.childNodes.add(childNode);
				}
			}
		}

		// Enforce Rule 3.
		for (Node node : nodeMap.values()) {
			Node n = node;
			Set<Node> visitedNodes = new LinkedHashSet<>();
			while (n != null) {
				if (!visitedNodes.add(n)) {
					// violation of Rule 3
					throw new RuntimeException("memory links do not form a tree");
				}
				n = n.parentNode;
			}
		}

		/*
		 * Walk the tree and determine byte counts
		 */

		/*
		 * Tell the MemSizer to exclude all the nodes
		 */
		MemSizer memSizer = new MemSizer(true);
		memSizer.excludeClass(Class.class);
		memSizer.excludeClass(MemSizer.class);
		for (Node node : nodeMap.values()) {
			memSizer.excludeObject(node.focus);
		}

		/*
		 * Walk the tree in a top-down fashion, determining the self byte
		 * counts. We choose a top down approach so that objects not accounted
		 * for in the design of the tree will tend to be accounted at the root
		 * of the tree rather than the leaves. Note also that since the memsizer
		 * was told to exclude the focus object of each of the nodes, the child
		 * byte counts will not be included.
		 */
		for (Node node : rootNodes) {
			determineSelfByteCounts(node, memSizer);
		}

		/*
		 * Walk the tree in a bottom-up fashion to determine the child byte
		 * counts
		 */
		for (Node node : rootNodes) {
			determineChildByteCounts(node);
		}

		/*
		 * Build the MemoryReportItems from the nodes and release the memory
		 * report items to the output manager.
		 */
		ScenarioId scenarioId = context.getScenario().getScenarioId();
		ReplicationId replicationId = context.getReplication().getId();

		MemoryReportItemBuilder memoryReportItemBuilder = new MemoryReportItemBuilder();
		for (Node node : nodeMap.values()) {
			memoryReportItemBuilder.setSelfByteCount(node.selfByteCount);
			memoryReportItemBuilder.setChildByteCount(node.childByteCount);
			memoryReportItemBuilder.setDescriptor(node.descriptor);
			memoryReportItemBuilder.setItemClass(node.focus.getClass());
			memoryReportItemBuilder.setId(node.id);
			Node parentNode = node.parentNode;
			if (parentNode == null) {
				memoryReportItemBuilder.setParentId(-1);
			} else {
				memoryReportItemBuilder.setParentId(parentNode.id);
			}
			memoryReportItemBuilder.setReplicationId(replicationId);
			memoryReportItemBuilder.setScenarioId(scenarioId);
			memoryReportItemBuilder.setTime(time);
			MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
			outputItemManager.releaseOutputItem(memoryReportItem);
		}

	}

	/*
	 * Sets the child byte counts for the node and its children in a depth first
	 * search of the sub tree associated with the node. Note that nodes that do
	 * not have children will have child byte counts of zero.
	 */
	private long determineChildByteCounts(Node node) {
		for (Node childNode : node.childNodes) {
			node.childByteCount += determineChildByteCounts(childNode);
		}
		return node.childByteCount + node.selfByteCount;
	}

	/*
	 * A top down determination of byte counts that belong to the node's focus
	 * object
	 */
	private void determineSelfByteCounts(Node node, MemSizer memSizer) {
		/*
		 * Tell the memsizer that it is allowed to explore the node's focus
		 */
		memSizer.includeObject(node.focus);
		node.selfByteCount = memSizer.getByteCount(node.focus);
		/*
		 * Cascade down onto the children of the node
		 */
		for (Node childNode : node.childNodes) {
			determineSelfByteCounts(childNode, memSizer);
		}
	}

	private static class Node {
		private int id;
		private String descriptor;
		private Object focus;
		private Node parentNode;
		private List<Node> childNodes = new ArrayList<>();
		private long selfByteCount;
		private long childByteCount;
	}

	private ProfileManager profileManager;

	@Override
	public void init(Context context) {
		this.context = context;
		profileManager = context.getProfileManager();
	}

}
