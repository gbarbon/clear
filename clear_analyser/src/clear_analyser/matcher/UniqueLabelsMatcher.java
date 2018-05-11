package clear_analyser.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.utils.STDOut;

public class UniqueLabelsMatcher {
	private STDOut outputWriter;

	public UniqueLabelsMatcher(STDOut outputWriter) {
		super();
		this.outputWriter = outputWriter;
	}

	/**
	 * Main function to match nodes between the Full LTS and the Counterexample
	 * LTS
	 *
	 * @param specGraph
	 *            full LTS
	 * @param specStartNode
	 *            intial node of the full LTS
	 * @param badGraph
	 *            counterexample LTS
	 * @param badStartNode
	 *            initial node of the Counterexample LTS
	 * @return
	 */
	public boolean matchNodes(LTS specGraph, GraphNode specStartNode, LTS badGraph, GraphNode badStartNode) {
		long startTime = System.currentTimeMillis();

		outputWriter.printComplete("Unique labels matching: starting matching.", true, true);

		Map<String, GraphNode> inEdgeToNode = new HashMap<>();
		for (GraphNode sn : specGraph.getVertices()) {
			for (GraphEdge e : specGraph.getIncidentEdges(sn)) {
				inEdgeToNode.put(e.getAction(), sn);
			}
		}
		badStartNode.setEquivalentInSpec(specStartNode);

		for (GraphNode bn : badGraph.getVertices()) {
			Collection<GraphEdge> inEdges = badGraph.getIncidentEdges(bn);
			if (!inEdges.isEmpty()) {
				GraphEdge e = inEdges.iterator().next();
				bn.setEquivalentInSpec(inEdgeToNode.get(e.getAction()));
			}
		}

		long endTime = System.currentTimeMillis();
		outputWriter.printComplete("Unique labels matching.matchNodes exec time: " + (endTime - startTime) + " ms\n",
				true, true);
		return true;
	}

}
