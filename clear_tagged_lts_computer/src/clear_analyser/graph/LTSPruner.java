package clear_analyser.graph;

import clear_analyser.utils.STDOut;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by gbarbon.
 */
public class LTSPruner {

	public static void execute(LTS lts, STDOut writer) {
        long startTime = System.currentTimeMillis();
		Set<GraphEdge> iTrans = new HashSet<>();
		getsAlliTrans(lts, iTrans);
		//blackColorerStarter(lts, iTrans);
        blackColorerNoRec(lts, iTrans);
        actualPruner(lts, iTrans);
		decolorize(lts);
		// colorITrans(lts, iTrans); //// to add the final i transitions, not used / not working
        long endTime = System.currentTimeMillis();
        writer.printComplete("LTSPruner exec time: " +
                (endTime - startTime) + " ms\n", true, true);
	}

	/**
	 * Retrieves all the i transitions
	 */
	private static void getsAlliTrans(LTS lts, Set<GraphEdge> iTrans) {
		int count = 0;
		for (GraphEdge edge : lts.getEdges()) {
			if (edge.getAction().equals("i")) {
				iTrans.add(edge);
                count++;
            }
		}
		System.out.println("getAlliTrans: number of i transitions: " + count);
	}

	/**
	 * Start the black colorer for every i transition. At the end all the traces
	 * protected by i transition will be colored in black
	 */
	private static void blackColorerStarter(LTS lts, Set<GraphEdge> iTrans) {
		for (GraphEdge edge : iTrans) {
			blackColorer(lts, edge);
		}
	}

	private static Set<GraphEdge> blackColorerNoRec(LTS lts, Set<GraphEdge> iTrans) {
		Set<GraphEdge> black = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();

        for (GraphEdge e : iTrans) {
			 todo.push(e);
		}

        while(!todo.isEmpty()){
            GraphEdge e = todo.pop();
            for (GraphEdge predEdge : lts.getInEdges(e.source)) {
                if (black.add(predEdge)) {
                    todo.push(predEdge);
                }

            }
        }
        black.parallelStream().forEach(e -> e.setAsNeutral());
		return black;
	}

	/**
	 * Colours all the predecessors of the given transition in black
	 *
	 * @param edge
	 */
	private static void blackColorer(LTS lts, GraphEdge edge) {
		if (edge != null) { // added check to avoid extreme case where there are
							// no correct
			// transition, thus no sink exist
			GraphNode node = edge.source;
			// System.out.println("Node: "+node.toStringShort());
			for (GraphEdge pred : lts.getInEdges(node)) {
				// System.out.println("Predecessor:" + pred.toStringSmall());
				if (!pred.isNeutral()) {
					pred.setAsNeutral(); // set back colour
					blackColorer(lts, pred); // recursion on the source of the
												// transition
				} // else
					// System.out.println("Not entering");
			}
		}
	}

	// part of the changes to add the final i transition, not used / not working
	/*private static void colorITrans(LTS lts, Set<GraphEdge> iTrans) {
		int count = 0;
		for (GraphEdge edge : iTrans) {
			edge.setAsIncorrect();
			System.out.print(edge.toString()+" type: "+  edge.getType());
			count++;
		}
		System.out.println("iTrans setted to incorrect: "+ count);
	}*/

	private static void actualPruner(LTS lts, Set<GraphEdge> iTrans) {

		// removing all the i transitions and their destination
		for (GraphEdge edge : iTrans) {
			// lts.removeVertex(edge.dest);
			//System.out.println("Pruning i transition: " + lts.getSource(edge).toStringShort() +
            //        edge.toStringSmall()
			//		+ lts.getDest(edge).toStringShort());
			lts.removeEdge(edge);
		}
		// NOTE: to add the changes for i transition, one should comment the loop above here
		// colorITrans(lts, iTrans); // to add the final i transition, not used / not working

		// removing all the unset transitions
		Iterable<GraphEdge> edgesToRemove = new ArrayList<>(lts.getEdges());
		for (GraphEdge edge : edgesToRemove) {
			if (edge.isUnset()) {
				//System.out.println("Pruning edge: " + lts.getSource(edge).toStringShort() + edge
                //        .toStringSmall()
				//		+ lts.getDest(edge).toStringShort());
				lts.removeEdge(edge);
			}
		}

		// removing all the nodes without exiting or entering transitions
		Iterable<GraphNode> statesToRemove = new ArrayList<>(lts.getVertices());
		for (GraphNode node : statesToRemove) {
			if (lts.getIncidentEdges(node).isEmpty()) {
				//System.out.println("Pruning node: " + node.toStringShort());
				lts.removeVertex(node);
			}
		}
	}

	/**
	 * Removes coloring from the graph
	 */
	private static void decolorize(LTS lts) {
		for (GraphEdge edge : lts.getEdges()) {
			edge.setAsUnset();
		}
	}

}
