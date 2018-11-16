package clear_analyser.livenessdebugger;

import clear_analyser.affix.*;
import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.sccfinder.SCC;
import clear_analyser.sccfinder.SCCSet;
import clear_analyser.sccfinder.TarjanIterative;
import clear_analyser.utils.STDOut;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by gbarbon.
 */
public class NestedInevitabilitiesDebugger extends LivenessDebugger {
	private SCCSet sccSet; // the scc set for the current lts
	private Affix<String> k;

	// FIXME: property parser not yet implemented
	@Deprecated
	public NestedInevitabilitiesDebugger(String propFile, LTS fullLts, String baseDir, STDOut writer) throws Exception {
		super(propFile, fullLts, baseDir, writer);
		throw new NotImplementedException();
	}

	public NestedInevitabilitiesDebugger(List<String> actions, LTS fullLts, STDOut writer) throws Exception {
		super(actions, fullLts, writer);
		k = new Affix<>(actions);
	}

	/**
	 * Constructor for deadlock and livelock detection (ONLY LIVELOCK?)
	 * @param fullLts
	 * @param writer
	 * @throws Exception
	 */
	public NestedInevitabilitiesDebugger(LTS fullLts, STDOut writer) throws Exception {
		super(fullLts, writer);
		this.actions = new ArrayList<>();
		actions.add("FINALEDGE");

		// TODO: add FINALEDGES to LTS
		fullLts.addFakeFinalEdges();
	}

	public void executor() {
		// Step 1 [SCC detection]: find SCC
		sccSet = new TarjanIterative(fullLTS).tarjanCaller();
		sccSet.setInitialSCC();
		sccSet.collectEdgesInScc();

		// Step 2 [prefixes and suffixes calculation]
		prefixSuffixCollector(sccSet);

		// Step 3 [coloring]
		colorer();
		sccSet.fakeSccRemover();
	}

	/**
	 * V5, start date: 26-01-2018 - paper version
	 *
	 * The algorithm passes through the whole graph and analyses for all the action in
	 * the property once.
	 * Two passes: one from the initial node direction forward and another one from the final
	 * nodes detected in the previous pass.
	 */
	private void prefixSuffixCollector(SCCSet sccSet) {
		ArrayList<SCC> sccsPrefixToDo = new ArrayList<>();
		ArrayList<SCC> sccsSuffixToDo;
		Queue<SCC> sccsToSetDepth = new LinkedList<>();
		Set<SCC> finalSccs = new HashSet<>();

		// init the scc
		sccSet.getInitialSCC().setDepth(0);
		sccsToSetDepth.add(sccSet.getInitialSCC());
		while (!sccsToSetDepth.isEmpty()) {
			SCC current = sccsToSetDepth.remove();
			// INIT PATCH // Todo: do better?
			if (sccsPrefixToDo.contains(current))
				sccsPrefixToDo.remove(current);
			// END PATCH
			for (SCC succ : sccSet.getSuccessors(current)) {
				succ.increaseDepthByPredecessor(current);
				sccsToSetDepth.add(succ);
			}
			sccsPrefixToDo.add(current);

			// also add to finalSCC if current is final
			if (sccSet.isFinalScc(current)) {
				finalSccs.add(current);
			}
		}
		Collections.sort(sccsPrefixToDo, SCC.COMPARE_BY_DEPTH);
		sccsSuffixToDo = (ArrayList<SCC>) sccsPrefixToDo.clone();
		Collections.sort(sccsSuffixToDo, Collections.reverseOrder(SCC.COMPARE_BY_DEPTH));

		// first step: all prefixes
		while (!sccsPrefixToDo.isEmpty()) {
			SCC current = sccsPrefixToDo.remove(0);
			//System.out.println("Computing prefix for SCC " + current.getRootNode().getId());
			sccCommonPrefixCalculation(current);
			current.setMaxPrefix(new MaxPrefix(sccSet, current, actions));
		}

		// do suffixes in the final sccs
		for (SCC current : finalSccs) {
			//System.out.println("Computing suffix for final SCC " + current.getRootNode().getId());
			sccCommonSuffixCalculation(current);
			current.setMaxSuffix(new MaxSuffix(sccSet, current, actions));
		}
		sccsSuffixToDo.removeAll(finalSccs);

		// second step: all suffixes
		while (!sccsSuffixToDo.isEmpty()) {
			SCC current = sccsSuffixToDo.remove(0);
			//System.out.println("Computing suffix for SCC " + current.getRootNode().getId());
			sccCommonSuffixCalculation(current);
			current.setMaxSuffix(new MaxSuffix(sccSet, current, actions));
		}
	}

	/**
	 * Computes the commonPrefix for all the internal nodes.
	 * Date: 28-01-2018 - Version from the paper
	 * @param currentScc the current SCC
	 */
	// TODO: check if it works on intial scc (and also with the dummy one)
	private void sccCommonPrefixCalculation(SCC currentScc) {
		//System.out.println("sccCommonPrefixCalculation for SCC " + currentScc.getRootNode().getId());
		ArrayList<GraphNode> Q = new ArrayList<>();

		// initialisation
		for (GraphNode state : currentScc.getNodes()) {
			state.setCommonPrefix(new CommonPrefix(k));
			if (fullLTS.getInitialNode().equals(state)) { // state is the initial state of the LTS
				state.setCommonPrefix(new CommonPrefix());
			} else if (sccSet.getEnteringNodes(currentScc).contains(state)) {
				for (GraphEdge edge : fullLTS.getInEdges(state)) {
					GraphNode source = fullLTS.getSource(edge);
					if (!currentScc.getNodes().contains(source)) {
						CommonPrefix tmp = new CommonPrefix(source.getCommonPrefix());
						tmp.add(edge.getAction());
						state.setCommonPrefix(CommonPrefix.greatestCommonPrefix(
								state.getCommonPrefix(), tmp));
					}
				}
			}
		}

		Q.addAll(currentScc.getNodes());
		Collections.sort(Q, GraphNode.COMPARE_BY_COMMON_PREFIX);
		while (!Q.isEmpty()) {
			GraphNode source = Q.remove(0);
			for (GraphEdge edge : fullLTS.getOutEdges(source)) {
				GraphNode dest = fullLTS.getDest(edge);
				if (currentScc.containsNode(dest)) {
					CommonPrefix tmp1 = new CommonPrefix(source.getCommonPrefix());
					tmp1.add(edge.getAction());
					CommonPrefix tmp2 = CommonPrefix.greatestCommonPrefix(tmp1, dest
							.getCommonPrefix());
					if (tmp2.size() < dest.getCommonPrefix().size()) {
						dest.setCommonPrefix(tmp2);
						// TODO: the following is not the best implementation, it should be
						// sufficient to update the position of dest
						Collections.sort(Q, GraphNode.COMPARE_BY_COMMON_PREFIX);
					}
				}
			}
		}
		// post-condition: all the nodes of the scc will have the correct
		// commonPrefix
	}

	/**
	 * Computes the commonSuffix for all the internal nodes.
	 * Date: 29-01-2018 - Version from the paper
	 * @param currentScc the current SCC
	 */
	private void sccCommonSuffixCalculation(SCC currentScc) {
		// System.out.println("sccCommonSuffixCalculation for SCC " + currentScc.getRootNode().getId());
		ArrayList<GraphNode> Q = new ArrayList<>();
		// initialisation
		for (GraphNode state : currentScc.getNodes()) {
			// first, we consider final states
			if (fullLTS.getOutEdges(state).isEmpty()) {  // this works only for final scc of one
				// state
				state.setCommonSuffix(new CommonSuffix());
			} else {
				CommonSuffix loopSuff = minSuffixLoopFinder(state, currentScc, fullLTS, k);
				if (loopSuff==null)  // in case the SCC is single-node without loop
					// TODO: remove this? Never reached
					loopSuff = new CommonSuffix(k);
				state.setCommonSuffix(loopSuff);
				if (sccSet.getExitingNodes(currentScc).contains(state)) {
					for (GraphEdge edge : fullLTS.getOutEdges(state)) {
						GraphNode dest = fullLTS.getDest(edge);
						if (!currentScc.getNodes().contains(dest)) {
							CommonSuffix tmp = new CommonSuffix(dest.getCommonSuffix());
							tmp.addOnTop(edge.getAction());
							state.setCommonSuffix(CommonSuffix.greatestCommonSuffix(
									state.getCommonSuffix(), tmp));
						}
					}
				}
			}
		}

		Q.addAll(currentScc.getNodes());
		Collections.sort(Q, GraphNode.COMPARE_BY_COMMON_SUFFIX);
		while (!Q.isEmpty()) {
			GraphNode dest = Q.remove(0);
			for (GraphEdge edge : fullLTS.getInEdges(dest)) {
				GraphNode source = fullLTS.getSource(edge);
				if (currentScc.containsNode(source)) {
					CommonSuffix tmp1 = new CommonSuffix(dest.getCommonSuffix());
					tmp1.addOnTop(edge.getAction());
					CommonSuffix tmp2 = CommonSuffix.greatestCommonSuffix(tmp1, source
							.getCommonSuffix());
					if (tmp2.size() < source.getCommonSuffix().size()) {
						source.setCommonSuffix(tmp2);
						// TODO: the following is not the best implementation, it should be
						// sufficient to update the position of dest
						Collections.sort(Q, GraphNode.COMPARE_BY_COMMON_SUFFIX);
					}
				}
			}
		}
		// post-condition: all the nodes of the scc will have the correct
		// commonSuffix
	}

	/*
	// version of 28/01/2018, suffer from the state explosion problem
	private CommonSuffix minSuffixLoopFinderOLD(GraphNode startingState, GraphNode currentState,
										Set<String> foundActions, Set<String> actionsInK,
											 Set<GraphNode> traversedStates, SCC scc,
								   CommonSuffix bestSoFar) {
		//System.out.println("minSuffixLoopFinder");
		for (GraphEdge edge : fullLTS.getOutEdges(currentState)) {
			GraphNode dest = fullLTS.getDest(edge);
			Set<String> localFoundActions = new HashSet<>(foundActions);
			Set<GraphNode> localTraversedStates = new HashSet<>(traversedStates);
			if (scc.containsNode(dest) && localTraversedStates.add(dest)) {
				if (actionsInK.contains(edge.getAction())) {
					localFoundActions.add(edge.getAction());
				}
				CommonSuffix tmp = new CommonSuffix(k);
				tmp.retainAll(localFoundActions); // join operation
				CommonSuffix localSuffix = CommonSuffix.greatestCommonSuffix(new CommonSuffix(k), tmp);
				if (bestSoFar== null || bestSoFar.size()>localSuffix.size()) { //early stop
					if (dest.equals(startingState) || localFoundActions.size() == actionsInK.size()) {  //;
						// loop closed OR all actions discovered
						bestSoFar = localSuffix;
					} else if (localFoundActions.size() < actionsInK.size()) { // recursion
						CommonSuffix recurRes = minSuffixLoopFinderOLD(startingState, dest,
								localFoundActions, actionsInK, localTraversedStates, scc, bestSoFar);
						if (bestSoFar == null || bestSoFar.size() > recurRes.size())
							bestSoFar = recurRes;
					}
				}
			}
		}
		return bestSoFar;
	}*/

	/*
	// version of 29/01/2018, suffer from the state explosion problem
	private CommonSuffix minSuffixLoopFinder(GraphNode startingState, GraphNode currentState,
											 Set<String> foundActions, Set<String> actionsInK,
											 Set<GraphNode> traversedStates, SCC scc,
											 CommonSuffix bestSoFar) {
		CommonSuffix tmp = new CommonSuffix(k);
		tmp.retainAll(foundActions); // join operation
		CommonSuffix localSuffix = CommonSuffix.greatestCommonSuffix(new CommonSuffix(k), tmp);
		if (currentState==null)
			currentState = startingState;
		else
			if (startingState.equals(currentState) || foundActions.size() == actionsInK.size()) {
				if (foundActions.size()==0){
					System.out.println("Found");
				}
				return localSuffix;
		}

		for (GraphEdge edge : fullLTS.getOutEdges(currentState)) {
			GraphNode dest = fullLTS.getDest(edge);
			Set<GraphNode> localTraversedStates = new HashSet<>(traversedStates);
			if (scc.containsNode(dest) && localTraversedStates.add(dest)) {
				Set<String> localFoundActions = new HashSet<>(foundActions);
				if (actionsInK.contains(edge.getAction())) {
					localFoundActions.add(edge.getAction());
				}
				if (bestSoFar == null || bestSoFar.size()>localSuffix.size()) { //early stop
					CommonSuffix recurRes = minSuffixLoopFinder(startingState, dest,
								localFoundActions, actionsInK, localTraversedStates, scc, bestSoFar);
					if (bestSoFar == null || bestSoFar.size() > recurRes.size())
						bestSoFar = recurRes;
					}
				}
			}
		return bestSoFar;
	}*/

	/**
	 * Produce the minimal common suffix generated by a loop in the SCC
	 * version 05/02/2018 - improved version, not suffering from states explosion
	 * @param startingState the GraphNode to check
	 * @param scc the current scc
	 * @param lts the current lts to which the scc belgons
	 * @param seq the current sequence (k at the first execution)
	 * @return a minimal CommonSuffix generated by a loop from the given node
	 */
	private CommonSuffix minSuffixLoopFinder(GraphNode startingState, SCC scc, LTS lts,
											 Affix<String> seq) {
		if (seq.isEmpty()) {  // early stop
			return new CommonSuffix();
		}
		String action = seq.getFirstElement();
		// we need a copy of the subset of the lts in order to remove edges from the scc
		LTS tmpLTS = LTS.copySubset(lts, scc.getNodes());
		SCC tmpScc = SCC.copy(scc, lts, tmpLTS);

		// we remove edges from the scc containing the action,
		// In that way the scc becomes split in more scc and lots of nodes are no more
		// reachable from startingState, simplifying computation.
		// Then we check if there still exist loops from startingState
		tmpLTS.removeAllEdges(action, tmpScc.getNodes());
		Affix<String> newSeq = seq.removeFirst(seq);
		CommonSuffix cs = minSuffixLoopFinder(startingState, tmpScc, tmpLTS, newSeq);
		if (!loopExist(startingState, tmpLTS)) {
			// if a loop does not exist, the current action can be considered as commonSuffix
			// if it is compliant to seq (checked in the return statement)
			cs.addOnTop(action);
		}
		// returning a commonSuffix compliant to seq  (note that seq is a suffix of k)
		return CommonSuffix.greatestCommonSuffix(cs, new CommonSuffix(seq));
	}

	/*
	// this loopExist version is not the best one (it is not dijkstra), and does not terminate
	private boolean loopExist(GraphNode startingState, GraphNode currentState, SCC scc, LTS lts,
							  Set<GraphNode> traversedStates) {
		if (currentState==null) {
			currentState = startingState;
		} else if (currentState.equals(startingState)) {
			System.out.println("There is a loop!");
			return true;
		}
		System.out.println("Before loop");
		for (GraphEdge edge : lts.getOutEdges(currentState)) {
			System.out.println("inside the loop!");
			GraphNode dest = lts.getDest(edge);
			Set<GraphNode> localTraversedStates = new HashSet<>(traversedStates);
			if (scc.containsNode(dest) && localTraversedStates.add(dest)) {
				System.out.println("traversed states " + localTraversedStates);
				if (loopExist(startingState, dest, scc, lts, localTraversedStates)) {
					return true;
				}
			}
		}
		return false;
	}*/

	/**
	 * Find a loop from the current state
	 * version 06/02/2018, terminate
	 * @param startingState the GraphNode to check for loops
	 * @param lts the lts we are analysing
	 * @return true if a loop exists, false otherwise
	 */
	private boolean loopExist(GraphNode startingState, LTS lts) {
		Collection<GraphEdge> outEdges = lts.getOutEdges(startingState);
		if (outEdges.isEmpty()) {  // early stop : no exiting edges from startingState
			return false;
		}
		for (GraphEdge edge : outEdges) {
			GraphNode dest = lts.getDest(edge);
			if (dest.equals(startingState))
				return true;  // early stop : self loop on startingState
		}
		Collection<GraphEdge> inEdges = lts.getInEdges(startingState);
		if (inEdges.isEmpty()) { // early stop : no incoming edges to startingState
			return false;
		}

		// jung library Dijkstra return an epty path if the source and the deestination
		// are the same so we exploits predecessors
		DijkstraShortestPath<GraphNode, GraphEdge> dijkstraShortestPath = new DijkstraShortestPath<>(lts);
		for (GraphEdge edge : inEdges) {
			GraphNode pred = lts.getSource(edge);
			List<GraphEdge> path = dijkstraShortestPath.getPath(startingState, pred);
			if (path.size()>0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Colors the incoming edges for all each node, exploiting prefixes and suffixes
	 */
	// FIXME : use k instead of actions!!
	private void colorer() {
		STDOut.dbugLog("starting colorer");
		for (GraphNode node : fullLTS.getVertices()) {
			fullLTS.getInEdges(node).parallelStream().forEach(edge -> {
				GraphNode pred = fullLTS.getSource(edge);

				// checking action in the middle
				String action = edge.getAction();
				int relActionWRTCommonPref = 0, relActionWRTMaxPref = 0;
				if (Prefix.checkActionCoherency(action, actions, pred.getCommonPrefix()))
					relActionWRTCommonPref = 1;
				if (Prefix.checkActionCoherency(action, actions, pred.getMaxPrefix()))
					relActionWRTMaxPref = 1;
				// Development note: another way to get if the action is
				// relevant or not consists
				// in making the difference between the prefix of the
				// predecessor and the one of
				// this node (both for common and max).

				// coloring the edge
/*				System.out.println("Actions is " + actions);
				System.out.println("Source: "+  pred.getId() + " Edge is "+ edge.getAction() + " " +
						"Dest is " + node.getId());
				System.out.println("cp in source: "+  pred.getCommonPrefix() +
						"cs in dest" + node.getCommonSuffix());
				System.out.println(pred.getCommonPrefix().size() + " + " + relActionWRTCommonPref
						+ " + " + node
						.getCommonSuffix
						().size() + " = " + (pred.getCommonPrefix().size() + relActionWRTCommonPref
						+ node.getCommonSuffix().size()) + " = "+ actions.size());*/
				if (!(pred.getCommonPrefix() == null || node.getCommonSuffix() == null || pred.getMaxPrefix() == null
						|| node.getMaxSuffix() == null)) {
					if ((pred.getCommonPrefix().size() + relActionWRTCommonPref
							+ node.getCommonSuffix().size()) >= actions.size())
						edge.setAsCorrect();
					else if ((pred.getMaxPrefix().size() + relActionWRTMaxPref + node.getMaxSuffix().size()) < actions
							.size()) {
						edge.setAsIncorrect();
					} else
						edge.setAsNeutral();
				} else
					throw new AssertionError("a prefix or a subfix is null");

				// coloring the scc to which the edge belongs
				if (pred.getSCC().equals(node.getSCC())) {
					// if nodes belongs to the same scc, the edge belongs to the
					// same scc
					if (node.getSCC().getType() == null) {
						node.getSCC().setType(edge.getType());
					} else if (!edge.getType().equals(node.getSCC().getType())) {
						// if the scc already has a color and the color is not
						// the same as the edge
						node.getSCC().setType(GraphEdge.TransitionType.UNSET);
					}
				}
			});
		}
	}
}
