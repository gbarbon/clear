package clear_analyser.livenessdebugger;

import clear_analyser.affix.*;
import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.sccfinder.SCC;
import clear_analyser.sccfinder.SCCSet;
import clear_analyser.sccfinder.TarjanIterative;
import clear_analyser.utils.STDOut;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by gbarbon.
 * Created in 28/11/2017 (copy of the MinMaxLivenessDebugger class at that time), contains old
 * portion of code that are not used anymore.
 */
public class MinMaxLivenessDebugger_DEPRECATED1 extends LivenessDebugger {
	private SCCSet sccSet; // the scc set for the current lts

	/*
	 * Build a new algorithm which pass through the whole graph and analyses for
	 * all the properties once. Two passes: one from the initial node direction
	 * forward and another one from the final nodes detected in the previous
	 * pass.
	 */

	public MinMaxLivenessDebugger_DEPRECATED1(String propFile, LTS fullLts, String baseDir, STDOut writer) throws Exception {
		super(propFile, fullLts, baseDir, writer);
	}

	public MinMaxLivenessDebugger_DEPRECATED1(List<String> actions, LTS fullLts, STDOut writer) throws Exception {
		super(actions, fullLts, writer);
	}

	public void executor() {
		// prefixSuffixCollector_v1();

		// Step 1 [SCC detection]: find SCC
		sccSet = new TarjanIterative(fullLTS).tarjanCaller();
		sccSet.collectEdgesInScc();
		sccSet.setInitialSCC();

		// Step 2 [prefixes and suffixes calculation]
		prefixSuffixCollector_v4(sccSet);
		colorer();
		backtracker();
	}

	/**
	 * @deprecated not correct, contains conceptual error Initial version with
	 *             problems
	 */
	@Deprecated
	private void prefixSuffixCollector_v1() {

		Stack<GraphNode> todo = new Stack<>();
		Set<GraphNode> alreadyDiscovered = new HashSet<>();
		Set<GraphNode> finalNodes = new HashSet<>();

		// first step: from initial node to final ones
		todo.push(fullLTS.getInitialNode()); // init stack
		while (!todo.isEmpty()) {
			GraphNode node = todo.pop();
			if (alreadyDiscovered.add(node)) { // if true = it has not been
												// discovered yet
				node.setCommonPrefix(new CommonPrefix(fullLTS, node, actions));
				node.setMaxPrefix(new MaxPrefix(fullLTS, node, actions));
				Collection<GraphNode> succs = fullLTS.getSuccessors(node);
				if (succs.isEmpty()) // node is a final node
					finalNodes.add(node);
				else
					succs.parallelStream().forEach(todo::push);
			}
		}

		// second step: from final nodes to initial one
		alreadyDiscovered = new HashSet<>(); // initialising this set
		finalNodes.parallelStream().forEach(todo::push); // init initial nodes
		while (!todo.isEmpty()) {
			GraphNode node = todo.pop();
			if (alreadyDiscovered.add(node)) {
				node.setCommonSuffix(new CommonSuffix(fullLTS, node, actions));
				node.setMaxSuffix(new MaxSuffix(fullLTS, node, actions));
				Collection<GraphNode> preds = fullLTS.getPredecessors(node);
				if (!preds.isEmpty()) // if preds.isEmpty==true, node is the
										// initial node
					preds.parallelStream().forEach(todo::push);
			}
		}
	}

	/**
	 * @deprecated not correct, contains conceptual error Evolved version, re-do
	 *             again the nodes that have null predecessors And uses scc's
	 *             root of 'final' sccs as final nodes STILL DRAFT
	 */
	@Deprecated
	private void prefixSuffixCollector_v2(SCCSet sccSet) {

		// Stack<GraphNode> todo = new Stack<>();
		Set<GraphNode> alreadyDiscovered = new HashSet<>();
		Set<GraphNode> finalNodes = new HashSet<>();
		Queue<GraphNode> todo = new LinkedList<>();

		// first step: from initial node to final ones
		// todo.push(fullLTS.getInitialNode()); // init stack
		todo.add(fullLTS.getInitialNode());
		while (!todo.isEmpty()) {
			// GraphNode node = todo.pop();
			GraphNode node = todo.remove();
			if (alreadyDiscovered.add(node)) { // if true = it has not been
												// discovered yet
				if (!checkPredecessor(node, todo) && !node.belongsToSCC()) {
					todo.add(node);
					alreadyDiscovered.remove(node);
				} else {
					node.setCommonPrefix(new CommonPrefix(fullLTS, node, actions));
					node.setMaxPrefix(new MaxPrefix(fullLTS, node, actions));
					Collection<GraphNode> succs = fullLTS.getSuccessors(node);

					// if (!checkPredecessor(node, todo)) {
					// todo.add(node);
					// }/
					if (succs.isEmpty()) // node is a final node
						finalNodes.add(node);
					else
						// succs.parallelStream().forEach(todo::push);
						succs.parallelStream().forEach(todo::add);
				}
			}

			// if node belongs to scc

		}

		// filling final nodes with scc roots from scc that are "final"
		sccSet.getSCCs().parallelStream().forEach(scc -> {
			if (sccSet.isFinalScc(scc))
				finalNodes.add(scc.getRootNode());
		});

		// second step: from final nodes to initial one
		alreadyDiscovered = new HashSet<>(); // initialising this set
		// finalNodes.parallelStream().forEach(todo::push); // init initial
		// nodes
		finalNodes.parallelStream().forEach(todo::add); // init initial nodes
		while (!todo.isEmpty()) {
			// GraphNode node = todo.pop();
			GraphNode node = todo.remove();
			if (alreadyDiscovered.add(node)) {
				if (!checkSuccessor(node, todo) && !node.belongsToSCC()) {
					todo.add(node);
					alreadyDiscovered.remove(node);
				} else {
					node.setCommonSuffix(new CommonSuffix(fullLTS, node, actions));
					node.setMaxSuffix(new MaxSuffix(fullLTS, node, actions));
					Collection<GraphNode> preds = fullLTS.getPredecessors(node);
					if (!preds.isEmpty()) // if preds.isEmpty==true, node is the
											// initial node
						// preds.parallelStream().forEach(todo::push);
						preds.parallelStream().forEach(todo::add);
				}
			}
		}
	}

	/**
	 * @deprecated not correct, contains conceptual error Considers also nodes
	 *             as single elements scc
	 */
	@Deprecated
	private void prefixSuffixCollector_v3(SCCSet sccSet) {
		Set<GraphNode> alreadyDiscovered = new HashSet<>();
		Set<GraphNode> finalNodes = new HashSet<>();
		Queue<GraphNode> todo = new LinkedList<>();

		// first step: from initial node to final ones
		todo.add(fullLTS.getInitialNode());
		while (!todo.isEmpty()) {

			// TODO: consider a single node as an SCC if it does not belongs to
			// an actual SCC
			// TODO (continues): in that way we should avoid problems
			// for instance, wait to have done all the entering state in the scc
			// works also
			// for a single node

			GraphNode node = todo.remove();
			if (alreadyDiscovered.add(node)) { // if true = it has not been
												// discovered yet
				if (!checkPredecessor(node, todo) && !node.belongsToSCC()) {
					// the node does not belongs to an scc
					todo.add(node);
					alreadyDiscovered.remove(node);
				} else {
					node.setCommonPrefix(new CommonPrefix(fullLTS, node, actions));
					node.setMaxPrefix(new MaxPrefix(fullLTS, node, actions));
					Collection<GraphNode> succs = fullLTS.getSuccessors(node);
					if (succs.isEmpty()) // node is a final node
						finalNodes.add(node);
					else
						succs.parallelStream().forEach(todo::add);
				}

				// TODO: wait for having all entering edges in an scc before
				// computing it
				/*
				 * if (every entering edge of the scc has been computed)
				 * calculateSCCInternalCommonPrefix(node.getSCC(), actions);
				 */

			}
			if (node.belongsToSCC()) {
				/**
				 * if (scc is ok){ // if all the entering edges of the scc have
				 * been computed calculateSCCInternalCommonPrefix(node.getSCC(),
				 * actions); // TODO: add all exiting edges from scc to todo }
				 * else what??
				 */
			}
		}

		// filling final nodes with scc roots from scc that are "final"
		for (SCC scc : sccSet.getSCCs()) {
			if (sccSet.isFinalScc(scc))
				finalNodes.add(scc.getRootNode());
		}

		// second step: from final nodes to initial one
		alreadyDiscovered = new HashSet<>(); // initialising this set
		finalNodes.parallelStream().forEach(todo::add); // init initial nodes
		while (!todo.isEmpty()) {
			GraphNode node = todo.remove();
			if (alreadyDiscovered.add(node)) {
				if (!checkSuccessor(node, todo) && !node.belongsToSCC()) {
					todo.add(node);
					alreadyDiscovered.remove(node);
				} else {
					node.setCommonSuffix(new CommonSuffix(fullLTS, node, actions));
					node.setMaxSuffix(new MaxSuffix(fullLTS, node, actions));
					Collection<GraphNode> preds = fullLTS.getPredecessors(node);
					if (!preds.isEmpty()) // if preds.isEmpty==true, node is the
											// initial node
						preds.parallelStream().forEach(todo::add);
				}
			}
		}
	}

	/**
	 * V4, date: 13-11-2017 Consider also single nodes that do not belongs to an
	 * actual SCC as an SCC
	 */
	private void prefixSuffixCollector_v4(SCCSet sccSet) {
		Set<SCC> alreadyDiscovered = new HashSet<>();
		Set<SCC> finalSCC = new HashSet<>();
		Queue<SCC> SCCtodo = new LinkedList<>();

		// first step: from initial scc to final sccs
		SCCtodo.add(sccSet.getInitialSCC());
		while (!SCCtodo.isEmpty()) {

			// for instance, wait to have done all the entering state in the scc
			// works also
			// for a single node

			SCC currentScc = SCCtodo.remove();
			if (alreadyDiscovered.add(currentScc)) {
				if (!checkPredecessor(currentScc, SCCtodo)) {
					// wait for having all entering edges in an scc before
					// computing its prefixes
					SCCtodo.add(currentScc);
					alreadyDiscovered.remove(currentScc);
				} else {
					sccCommonPrefixCalculation(currentScc);
					currentScc.setMaxPrefix(new MaxPrefix(sccSet, currentScc, actions));
					if (sccSet.isFinalScc(currentScc))
						finalSCC.add(currentScc);
					else
						sccSet.getSuccessors(currentScc).parallelStream().forEach(SCCtodo::add);
				}
			}
		}

		// second step: from final nodes to initial one
		alreadyDiscovered = new HashSet<>(); // initialising this set
		finalSCC.parallelStream().forEach(SCCtodo::add); // init initial nodes
		while (!SCCtodo.isEmpty()) {
			SCC currentScc = SCCtodo.remove();
			if (alreadyDiscovered.add(currentScc)) {
				if (!checkSuccessor(currentScc, SCCtodo)) {
					// wait for having all exiting edges from an scc before
					// computing it
					SCCtodo.add(currentScc);
					alreadyDiscovered.remove(currentScc);
				} else {
					sccCommonSuffixCalculation(currentScc);
					currentScc.setMaxSuffix(new MaxSuffix(sccSet, currentScc, actions));
					sccSet.getPredecessors(currentScc).parallelStream().forEach(SCCtodo::add);

				}
			}
		}
	}

	/**
	 * @deprecated not used anymore on last version Checks predecessors for a
	 *             single node
	 * @param node
	 * @param coll
	 * @return false if the predecessor needs to be done again
	 */
	@Deprecated
	private boolean checkPredecessor(GraphNode node, Collection<GraphNode> coll) {
		for (GraphNode pred : fullLTS.getPredecessors(node)) {
			if (pred.getCommonPrefix() == null || pred.getMaxPrefix() == null) {
				// System.out.println("It happened for node: " +
				// node.toStringShort());
				return false;
			}
			if (coll.contains(pred)) {
				// System.out.println("It happened THE SECOND for node: " +
				// node.toStringShort());
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks predecessors for an SCC
	 * 
	 * @param scc
	 *            the scc whose predecessors we are checking
	 * @param sccTodo
	 * @return false if the predecessor needs to be done again
	 */
	private boolean checkPredecessor(SCC scc, Collection<SCC> sccTodo) {
		for (GraphEdge edge : sccSet.getInEdges(scc)) {
			GraphNode predNode = sccSet.getLts().getSource(edge);

			// check if the prefixes in the predecessor of this scc have been
			// set
			if (predNode.getCommonPrefix() == null)
				return false;

			if (predNode.getMaxPrefix() == null)
				return false;

			// check if the predecessor is contained into the sccTodo list
			if (sccTodo.contains(predNode.getSCC()))
				return false;
		}
		return true;
	}

	/**
	 * @deprecated not used anymore in last version Check successors of a single
	 *             node
	 * @param node
	 * @param coll
	 * @return false if the successor needs to be done again
	 */
	@Deprecated
	private boolean checkSuccessor(GraphNode node, Collection<GraphNode> coll) {
		for (GraphNode succ : fullLTS.getSuccessors(node)) {
			if (succ.getCommonSuffix() == null || succ.getMaxSuffix() == null) {
				return false;
			}
			if (coll.contains(succ)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks successors of an scc
	 * 
	 * @param scc
	 * @param sccTodo
	 * @return false if the successor needs to be done again
	 */
	private boolean checkSuccessor(SCC scc, Collection<SCC> sccTodo) {
		for (GraphEdge edge : sccSet.getOutEdges(scc)) {
			GraphNode succNode = sccSet.getLts().getDest(edge);

			// check if the prefixes in the predecessor of this scc have been
			// set
			if (succNode.getCommonSuffix() == null)
				return false;

			if (succNode.getMaxSuffix() == null)
				return false;

			// check if the predecessor is contained into the sccTodo list
			if (sccTodo.contains(succNode.getSCC()))
				return false;
		}
		return true;
	}

	/**
	 * @deprecated it does not calculate correctly the common prefix of nodes
	 *             inside the scc
	 *
	 *             If commonPrefix has been computed for all sources of the
	 *             entering edges of the current scc, compute incoming common
	 *             prefix (used version at 13-11-2017)
	 * @param currentScc
	 */
	@Deprecated
	private void sccCommonPrefixCalculation_old(SCC currentScc) {
		currentScc.setCommonInPrefix(CommonPrefix.inEdgesCommonPrefix(sccSet, currentScc, actions));
		if (currentScc.getNodes().size() == 1) {
			// single node scc: set commonInPrefix as commonPrefix
			currentScc.getRootNode().setCommonPrefix(currentScc.getCommonInPrefix());
		} else {
			// calculate commonPrefix for every exiting node
			sccSet.getExitingNodes(currentScc).parallelStream()
					.forEach(e -> e.setCommonPrefix(new CommonPrefix(sccSet, currentScc, e, actions)));
		}
	}

	/**
	 * New version that allows to compute the commonPrefix for all the internal
	 * nodes. Notice that this do not use a single commonPrefix for the scc, but
	 * evaluates each entering common prefix. Initial Date: 14/11/2017, Last
	 * mod: 15/11/2017
	 * 
	 * @param currentScc
	 *            the current SCC
	 */
	private void sccCommonPrefixCalculation(SCC currentScc) {
		if (currentScc.getNodes().size() == 1) {
			// single node scc
			GraphNode rootNode = currentScc.getRootNode();
			rootNode.setCommonPrefix(new CommonPrefix(sccSet.getLts(), rootNode, actions));
		} else {
			Stack<GraphNode> nodesToDo = new Stack<>();
			Set<GraphNode> initNodes = sccSet.getEnteringNodes(currentScc);

			// init step
			if (initNodes.isEmpty()) {
				// this condition is used to compute commonPrefix in case of an
				// scc at the
				// beginning of the lts (and containing the initial node)
				GraphNode initialNode = currentScc.getRootNode();
				// now looping on exiting nodes
				sccSet.getLts().getOutEdges(initialNode).parallelStream().forEach(edge -> {
					if (currentScc.containsEdge(edge)) {
						GraphNode succ = sccSet.getLts().getDest(edge);

						// TODO: move this part in the CommonPrefix class?
						// compute temporary common prefix of succ
						CommonPrefix tempCommonPrefix = new CommonPrefix();
						tempCommonPrefix.evaluateEdge(edge, initialNode.getCommonPrefix(), actions, 0);

						// if temp common prefix is null or if it is shorter,
						// update old common prefix
						// of succ and add succ to nodesToDo
						if (succ.getCommonPrefix() == null
								|| (tempCommonPrefix.size() < succ.getCommonPrefix().size())) {
							succ.setCommonPrefix(tempCommonPrefix);
							nodesToDo.add(succ);
						}
					}
				});
			} else {
				// normal case: calculate temporary common prefix for entering
				// nodes
				for (GraphNode enteringNode : initNodes) {
					CommonPrefix tempCommonPrefix = new CommonPrefix();
					int i = 0;
					// TODO: move this part in the CommonPrefix class?
					for (GraphEdge enteringEdge : sccSet.getLts().getInEdges(enteringNode)) {
						if (!currentScc.containsEdge(enteringEdge)) {
							// the edge does not belong to the internal edges,
							// so it is an entering one
							tempCommonPrefix.evaluateEdge(enteringEdge,
									sccSet.getLts().getSource(enteringEdge).getCommonPrefix(), actions, i);
						}
					}
					enteringNode.setCommonPrefix(tempCommonPrefix);
					nodesToDo.add(enteringNode);
				}
				// post-condition: now each entering node should contain its
				// commonPrefix
			}

			// calculate commonPrefix for every node of the scc
			while (!nodesToDo.isEmpty()) {
				Collections.sort(nodesToDo, GraphNode.COMPARE_BY_COMMON_PREFIX);
				GraphNode currentNode = nodesToDo.pop();
				// now looping on exiting nodes
				sccSet.getLts().getOutEdges(currentNode).parallelStream().forEach(edge -> {
					if (currentScc.containsEdge(edge)) {
						GraphNode succ = sccSet.getLts().getDest(edge);
						// TODO: move this part in the CommonPrefix class?
						// compute temporary common prefix of succ
						// Notice that, even if the initialNode commonPrefix is
						// not set, the copy
						// method used inside evaluateEdge allows to create a
						// new Prefix when the
						// passed one is null. Moreover, notice that the
						// initialNode commonPrefix
						// can be updated if an exiting edge has itself as dest.
						CommonPrefix tempCommonPrefix = new CommonPrefix();
						tempCommonPrefix.evaluateEdge(edge, currentNode.getCommonPrefix(), actions, 0);

						// if temp common prefix is null or if it is shorter,
						// update old common prefix
						// of succ and add succ to nodesToDo
						if (succ.getCommonPrefix() == null
								|| (tempCommonPrefix.size() < succ.getCommonPrefix().size())) {
							succ.setCommonPrefix(tempCommonPrefix);
							nodesToDo.add(succ);
						}
					}
				});
			}
			// post-condition: all the nodes of the scc will have the correct
			// commonPrefix
		}
	}

	/**
	 * @deprecated it does not calculate correctly the common suffix of nodes
	 *             inside the scc
	 *
	 *             if commonSuffix has been computed for all destinations of the
	 *             exiting edges of the current scc, compute outgoing
	 *             commonSuffix
	 * @param currentScc
	 */
	@Deprecated
	private void sccCommonSuffixCalculation_old(SCC currentScc) {
		currentScc.setCommonOutSuffix(CommonSuffix.outEdgesCommonSuffix(sccSet, currentScc, actions));
		if (currentScc.getNodes().size() == 1) {
			// single node scc: set commonOutSuffix as commonSuffix
			currentScc.getRootNode().setCommonSuffix(currentScc.getCommonOutSuffix());
		} else {
			// calculate commonSuffix for every entering node
			sccSet.getEnteringNodes(currentScc).parallelStream()
					.forEach(e -> e.setCommonSuffix(new CommonSuffix(sccSet, currentScc, e, actions)));
		}
	}

	/**
	 * New version that allows to compute the commonSuffix for all the internal
	 * nodes. Notice that this do not use a single commonSuffix for the scc, but
	 * evaluates each exiting common suffix. Initial Date: 14/11/2017, Last mod:
	 * 15/11/2017
	 * 
	 * @param currentScc
	 *            the current SCC
	 */
	private void sccCommonSuffixCalculation(SCC currentScc) {
		if (currentScc.getNodes().size() == 1) {
			// single node scc
			GraphNode rootNode = currentScc.getRootNode();
			rootNode.setCommonSuffix(new CommonSuffix(sccSet.getLts(), rootNode, actions));
		} else {
			Stack<GraphNode> nodesToDo = new Stack<>();
			Set<GraphNode> initNodes = sccSet.getExitingNodes(currentScc);

			// init step
			if (initNodes.isEmpty()) {
				// this condition is used to compute commonSuffix in final sccs
				GraphNode initialNode = currentScc.getRootNode();
				// now looping on entering nodes
				sccSet.getLts().getInEdges(initialNode).parallelStream().forEach(edge -> {
					if (currentScc.containsEdge(edge)) {
						GraphNode pred = sccSet.getLts().getSource(edge);

						// TODO: move this part in the CommonSuffix class?
						// compute temporary common suffix of pred
						// Notice that, even if the initialNode commonSuffix is
						// not set, the copy
						// method used inside evaluateEdge allows to create a
						// new Suffix when the
						// passed one is null. Moreover, notice that the
						// initialNode commonSuffix
						// can be updated if an entering edge has itself as
						// source.
						CommonSuffix tempCommonSuffix = new CommonSuffix();
						tempCommonSuffix.evaluateEdge(edge, initialNode.getCommonSuffix(), actions, 0);

						// if temp common suffix is null or if it is shorter,
						// update old common suffix
						// of pred and add pred to nodesToDo
						if (pred.getCommonSuffix() == null
								|| (tempCommonSuffix.size() < pred.getCommonSuffix().size())) {
							pred.setCommonSuffix(tempCommonSuffix);
							nodesToDo.add(pred);
						}
					}
				});
			} else {
				// normal case: calculate temporary common suffix for exiting
				// nodes
				for (GraphNode exitingNode : initNodes) {
					// TODO: move this part in the CommonSuffix class?
					CommonSuffix tempCommonSuffix = new CommonSuffix();
					int i = 0;
					sccSet.getLts().getOutEdges(exitingNode).parallelStream().forEach(exitingEdge -> {
						if (!currentScc.containsEdge(exitingEdge)) {
							// the edge does not belong to the internal edges,
							// so it is an entering one
							tempCommonSuffix.evaluateEdge(exitingEdge,
									sccSet.getLts().getDest(exitingEdge).getCommonSuffix(), actions, i);
						}
					});
					exitingNode.setCommonSuffix(tempCommonSuffix);
					nodesToDo.add(exitingNode);
				}
				// post-condition: now each entering node should contain its
				// commonSuffix
			}

			// calculate commonSuffix for every node of the scc
			while (!nodesToDo.isEmpty()) {
				Collections.sort(nodesToDo, GraphNode.COMPARE_BY_COMMON_SUFFIX);
				GraphNode currentNode = nodesToDo.pop();
				// now looping on entering nodes
				sccSet.getLts().getInEdges(currentNode).parallelStream().forEach(edge -> {
					if (currentScc.containsEdge(edge)) {
						GraphNode pred = sccSet.getLts().getSource(edge);

						// TODO: move this part in the CommonSuffix class?
						// compute temporary common suffix of pred
						CommonSuffix tempCommonSuffix = new CommonSuffix();
						tempCommonSuffix.evaluateEdge(edge, currentNode.getCommonSuffix(), actions, 0);

						// if temp common suffix is null or if it is shorter,
						// update old common suffix
						// of pred and add pred to nodesToDo
						if (pred.getCommonSuffix() == null
								|| (tempCommonSuffix.size() < pred.getCommonSuffix().size())) {
							pred.setCommonSuffix(tempCommonSuffix);
							nodesToDo.add(pred);
						}
					}
				});
			}
			// post-condition: all the nodes of the scc will have the correct
			// commonSuffix
		}
	}

	/**
	 * Colors the incoming edges for all each node.
	 */
	private void colorer() {
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

	/**
	 * Re-colors the set of edges of the given scc.
	 */
	private void backtrackingColorer(SCC scc) {
		scc.setType(null);
		for (GraphNode node : scc.getNodes()) {
			fullLTS.getInEdges(node).parallelStream().forEach(edge -> {
				if (scc.containsEdge(edge)) {
					GraphNode pred = fullLTS.getSource(edge);

					// checking action in the middle
					String action = edge.getAction();
					int relActionWRTCommonPref = 0, relActionWRTMaxPref = 0;
					if (Prefix.checkActionCoherency(action, actions, pred.getCommonPrefix()))
						relActionWRTCommonPref = 1;
					if (Prefix.checkActionCoherency(action, actions, pred.getMaxPrefix()))
						relActionWRTMaxPref = 1;

					// coloring the edge
					if (!(pred.getCommonPrefix() == null || node.getBTCommonSuffix() == null ||
							pred.getMaxPrefix() == null || node.getBTMaxSuffix() == null)) {
						if ((pred.getCommonPrefix().size() + relActionWRTCommonPref
								+ node.getBTCommonSuffix().size()) >= actions.size())
							edge.setAsCorrect();
						else if ((pred.getMaxPrefix().size() + relActionWRTMaxPref +
								node.getBTMaxSuffix().size()) < actions.size()) {
							edge.setAsIncorrect();
						} else
							edge.setAsNeutral();
					} else
						throw new AssertionError("a prefix or a subfix is null");

					// color the scc
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

	/**
	 * If the scc contains a loop that does not contain at least one of the
	 * remaining actions, the scc eeds to be backtraked
	 * 
	 * @param scc
	 * @return
	 */
	private boolean isToBacktrack(SCC scc) {

		if (scc.getType() != GraphEdge.TransitionType.GREEN) {
			return false;
		}

		// Pre-filtering green scc. 2 steps
		// 1) if an scc has all the exiting nodes with commonPrefix that are
		// equals to the
		// sequence, there is no need to check backtracking on it (we should
		// avoid backtracking
		// on it).
		CommonPrefix sccCommonExitingPrefix = new CommonPrefix();
		int i = 0; // TODO: I do not like use of i...
		for (GraphNode rootNode : sccSet.getExitingNodes(scc)) {
			if (i == 0)
				sccCommonExitingPrefix.copy(rootNode.getCommonPrefix());
			else
				sccCommonExitingPrefix.retainShortest(rootNode.getCommonPrefix());
			i++;
		}
		if (sccCommonExitingPrefix.size() == actions.size())
			return false;
		// 2) if the exiting minimal common prefix is less than the sequence,
		// but the scc
		// does not contain the actions that it should need to complete it, it
		// is sure that we
		// have to do backtracking on it.
		Set<String> remainingActions = new HashSet<>(actions.subList(sccCommonExitingPrefix.size(), actions.size()));
		// Collection<String> remainingActions = new ArrayList<>(actions);
		// remainingActions.removeAll(sccCommonExitingPrefix.toList());
		for (String action : remainingActions) {
			if (!scc.getActions().contains(action))
				return true;
		}

		// iterating over exiting nodes
		for (GraphNode rootNode : sccSet.getExitingNodes(scc)) {
			CommonPrefix tempPrefix = new CommonPrefix();
			tempPrefix.copy(rootNode.getCommonPrefix());
			Set<String> actionsToCheck = new HashSet<>(actions.subList(tempPrefix.size(), actions.size()));
			// List<String> actionsToCheck = new ArrayList<>(actions);
			// actionsToCheck.removeAll(tempPrefix.toList()); // these are the
			// // actions to check

			if (existsLoopWithoutActions(rootNode, rootNode, new HashSet<>(), scc, actionsToCheck)) {
				return true;
			}
			// // iterating over all the actions
			// for (String action : actionsToCheck) {
			// Stack<GraphNode> nodesTodo = new Stack<>();
			// nodesTodo.add(rootNode);
			// boolean found = false;
			// Set<GraphEdge> visitedEdges = new HashSet<>();
			//
			// // doing the depth first
			// while (!nodesTodo.isEmpty()) {
			// GraphNode node = nodesTodo.pop();
			// for (GraphEdge succEdge : fullLTS.getOutEdges(node)) {
			// GraphNode edgeDest = fullLTS.getDest(succEdge);
			// if (scc.containsNode(edgeDest) && visitedEdges.add(succEdge)) {
			// if (Prefix.checkActionCoherency(succEdge.getAction(), actions,
			// tempPrefix))
			// found = true;
			// if (edgeDest.equals(rootNode)) {
			// // the loop is completed, now evaluate the loop,
			// // if the action
			// // if path not compliant, return false
			//
			// if (!found)
			// return true;
			// // initing for next path
			// // FIXME: the 'new' here let delete some edges
			// // that we should
			// // FIXME (continues): avoid, increasing the
			// // complexity
			// visitedEdges = new HashSet<>(); // empty list of
			// // visited edge
			// found = false; // set found to false again for
			// // nex path
			// } else
			// nodesTodo.push(edgeDest);
			// }
			// }
			// }
			// // the action is present in all the loops, so it is added to the
			// // commonPrefix
			// tempPrefix.add(action);
			// }
		}
		// all the action produces correct sequences in the
		return false;
	}

    /**
     * Finds a loop in the scc that does not allow to complete the sequence of inevitable actions
     * @param startingPoint the exiting node of the scc
     * @param currentNode the current node of the scc
     * @param traversedNodes allows to keep track of traversed nodes
     * @param scc the current scc
     * @param avoidedActions
     * @return
     */
	private boolean existsLoopWithoutActions(GraphNode startingPoint, GraphNode currentNode,
			Set<GraphNode> traversedNodes, SCC scc, Set<String> avoidedActions) {
		for (GraphEdge succEdge : fullLTS.getOutEdges(currentNode)) {
			GraphNode edgeDest = fullLTS.getDest(succEdge);
			if (scc.containsNode(edgeDest) && traversedNodes.add(edgeDest)) {
				boolean removedAction = avoidedActions.remove(succEdge.getAction());
				if (!avoidedActions.isEmpty()) {
					if (edgeDest.equals(startingPoint)) {
                        // a loop is completed
						return true;
					}
					if (existsLoopWithoutActions(startingPoint, edgeDest, traversedNodes, scc, avoidedActions)) {
						return true;
					}
				}
				if (removedAction) {
					avoidedActions.add(succEdge.getAction());
				}
				traversedNodes.remove(edgeDest);
			}
		}
		return false;
	}

	/**
	 *
	 */
	private void backtracker() {
		// Set<SCC> setOfSCCtoBacktrack = new HashSet<>();

		// detect all the scc that should be backtracked
		// we can exclude:
		// - scc containing all red transitions
		// - containing all black transition
		// - containing black and green transitions
		// we focus on sccs containing only green transitions, that do not
		// contain any action
		// coherent to the prefix. If there remains the need for a suffix to
		// obtain the given
		// sequence (thus if prefix + contained actions are not sufficient),
		// this scc can avoid
		// inevitability

		// note: all-green scc has already been detected in the coloring step
		List<SCC> setOfSCCtoBacktrack = sccSet.getSCCs().parallelStream().filter(scc -> isToBacktrack(scc))
				.collect(Collectors.toList());
		/*
		 * if (scc.getType()==GraphEdge.TransitionType.GREEN) { if
		 * (sccSet.getExitingNodes(scc).iterator().hasNext()) {
		 * System.err.println("SCC with root: " + scc.getRootNode().getId());
		 * CommonPrefix tmpPref = new CommonPrefix(); int i = 0; // collecting
		 * the smallest common prefix of the exiting edges for (GraphNode node :
		 * sccSet.getExitingNodes(scc)) { if (i==0)
		 * tmpPref.copy(node.getCommonPrefix()); else tmpPref =
		 * node.getCommonPrefix().size() < tmpPref.size() ?
		 * node.getCommonPrefix() : tmpPref; i++; } if (tmpPref.size() <
		 * actions.size()) setOfSCCtoBacktrack.add(scc); }
		 * 
		 * // all the exiting nodes should have the same prefix (in this
		 * particular case) // so it is sufficient to take a random one /*if
		 * (sccSet.getExitingNodes(scc).iterator().hasNext()) { try { GraphNode
		 * element = sccSet.getExitingNodes(scc).iterator().next(); CommonPrefix
		 * tmpPref = element.getCommonPrefix(); if (tmpPref.size() <
		 * actions.size()) setOfSCCtoBacktrack.add(scc); } catch
		 * (NoSuchElementException e) { e.printStackTrace(); } } }
		 */
		// });

		// colouring the SCCs and paths to SCCs
		Set<GraphEdge> edgesToColor = new HashSet<>();
		Stack<GraphNode> nodesTodo = new Stack<>();
		// Stack<GraphEdge> sccTodo = new Stack<>();

		// iterating over all the SCCs edges and predecessors edges
		for (SCC scc : setOfSCCtoBacktrack) {

			// backtrack the scc
			sccBTCommonSuffixCalculation(scc);
			scc.setBTMaxSuffix(MaxSuffix.btMaxSuffix(sccSet, scc, actions));
			backtrackingColorer(scc);

			// backtracking of incoming paths
			nodesTodo.addAll(sccSet.getEnteringNodes(scc));
			while (!nodesTodo.isEmpty()) {
				GraphNode node = nodesTodo.pop();
				// getInEdges method allows to exit from the SCC and colour the
				// paths that bring to the scc
				fullLTS.getInEdges(node).parallelStream().forEach(predEdge -> {
					GraphNode source = fullLTS.getSource(predEdge);
					if (predEdge.isCorrect() && edgesToColor.add(predEdge))
						if (source.getSCC().getEdges()==null)
							// add the source only if it is not a true scc
							nodesTodo.push(source);
				});
			}
		}
		edgesToColor.parallelStream().forEach(e -> e.setAsNeutral());

		// color the paths that enters the scc, verifying each transition
		// (prefix of predecessor,
		// current transition and suffix of successor)
	}

	/**
	 * Compute the backtracking commonSuffix for all the internal nodes.
	 * Notice that this do not use a single commonSuffix for the scc, but
	 * evaluates each exiting common suffix.
	 * Initial Date: 14/11/2017, Last mod: 28/11/2017
	 *
	 * @param currentScc the current SCC
	 */
	private void sccBTCommonSuffixCalculation(SCC currentScc) {
		Set<GraphNode> initNodes = sccSet.getExitingNodes(currentScc);
		//if (initNodes.isEmpty()) {
		//	throw new AssertionError("should not be possible, since we should not have" +
		//			"scc here without exiting nodes");
		//}

		Stack<GraphNode> nodesToDo = new Stack<>();

		// init step
		// we consider the exiting nodes as not having exiting transitions
		GraphNode initialNode = currentScc.getRootNode();
		// now looping on entering nodes
		sccSet.getLts().getInEdges(initialNode).parallelStream().forEach(edge -> {
			if (currentScc.containsEdge(edge)) {
				GraphNode pred = sccSet.getLts().getSource(edge);

				// compute temporary common suffix of pred
				// Notice that, even if the initialNode commonSuffix is
				// not set, the copy method used inside evaluateEdge
				// allows to create a new Suffix when the
				// passed one is null. Moreover, notice that the
				// initialNode commonSuffix can be updated if an
				// entering edge has itself assource.
				CommonSuffix tempBTCommonSuffix = new CommonSuffix();
				tempBTCommonSuffix.evaluateEdge(edge, initialNode.getBTCommonSuffix(),
						actions, 0);

				// if temp common suffix is null or if it is shorter,
				// update old common suffix of pred and add pred
				// to nodesToDo
				if (pred.getBTCommonSuffix() == null
						|| (tempBTCommonSuffix.size() < pred.getBTCommonSuffix().size())) {
					pred.setBTCommonSuffix(tempBTCommonSuffix);
					nodesToDo.add(pred);
				}
			}
		});

				/*// normal case: set temporary common suffix for exiting
				// nodes
				for (GraphNode exitingNode : initNodes) {
					CommonSuffix btCommonSuffix = new CommonSuffix();
					exitingNode.setBTCommonSuffix(btCommonSuffix);
					nodesToDo.add(exitingNode);
				}
				// post-condition: now each entering node should contain
				// an empty commonSuffix*/


		// calculate commonSuffix for every node of the scc
		while (!nodesToDo.isEmpty()) {
			Collections.sort(nodesToDo, GraphNode.COMPARE_BY_COMMON_SUFFIX);
			GraphNode currentNode = nodesToDo.pop();
			// now looping on entering nodes
			sccSet.getLts().getInEdges(currentNode).parallelStream().forEach(edge -> {
				if (currentScc.containsEdge(edge)) {
					GraphNode pred = sccSet.getLts().getSource(edge);

					// TODO: move this part in the CommonSuffix class?
					// compute temporary common suffix of pred
					CommonSuffix tempBTCommonSuffix = new CommonSuffix();
					tempBTCommonSuffix.evaluateEdge(edge, currentNode.getBTCommonSuffix(),
							actions, 0);

					// if temp common suffix is null or if it is shorter,
					// update old common suffix of pred and add pred to nodesToDo
					if (pred.getBTCommonSuffix() == null
							|| (tempBTCommonSuffix.size() < pred.getBTCommonSuffix().size())) {
						pred.setBTCommonSuffix(tempBTCommonSuffix);
						nodesToDo.add(pred);
					}
				}
			});
		}
		// post-condition: all the nodes of the scc will have the correct
		// BTCommonSuffix

	}
}


