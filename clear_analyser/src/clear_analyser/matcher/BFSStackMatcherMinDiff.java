package clear_analyser.matcher;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.utils.STDOut;

public class BFSStackMatcherMinDiff {

    private final Deque<MatchingDecision> decisions;  // matching decision for each nodesToProcess
    // element
    private final List<GraphNode> nodesToProcess;  // list of states of the Bad LTS, ordered in BFS
    private int[] nbDifferences;  // number of differences for the current decision
    private List<Set<GraphNode>> conflicts;  // list of conflicts
    private STDOut outputWriter;

    public BFSStackMatcherMinDiff(STDOut outputWriter) {
        super();
        this.decisions = new LinkedList<>();
        this.nodesToProcess = new ArrayList<>();
        this.conflicts = new ArrayList<>();
        this.outputWriter = outputWriter;
    }

    /**
     * Compute the difference between a node of the Counterexample LTS and the
     * the node in the Full LTS which simulates it (through getEquivalentInSpec).
     *
     * @param specGraph the Full LTS
     * @param badGraph  the Counterexample LTS
     * @param bn        a node of the Counterexample LTS.
     *                  // MODIFIED @return true if the number of exiting transitions is different
     *                  // MODIFIED OR if the number of successors nodes is less than the one in the good
     * @return true if the number of exiting transitions is higher
     * OR if the number of successors nodes is higher than the one in the good
     * OR if the exiting transitions labels are not contianed in the exiting transitions labels
     * set of the equivalent node in the spec
     */
    public static boolean isDiff(LTS specGraph, LTS badGraph, GraphNode bn) {
        Set<String> badTransitions = new HashSet<>();  // duplicates not allowed
        for (GraphEdge edge : badGraph.getOutEdges(bn)) {
            badTransitions.add(edge.getAction());
        }
        Set<String> specTransitions = new HashSet<>();  // duplicates not allowed
        for (GraphEdge edge : specGraph.getOutEdges(bn.getEquivalentInSpec())) {
            specTransitions.add(edge.getAction());
        }
        return badGraph.getOutEdges(bn).size() >
                specGraph.getOutEdges(bn.getEquivalentInSpec()).size()
                || badGraph.getSuccessorCount(bn) >
                specGraph.getSuccessorCount(bn.getEquivalentInSpec())
                ||
                // check that the badTrans is a subset of specTrans
                !specTransitions.containsAll(badTransitions);

        // Old diff function:
        /*return badGraph.getOutEdges(bn).size() !=  // FIXME: why? badNode can also have less edges
                specGraph.getOutEdges(bn.getEquivalentInSpec()).size()
                || badGraph.getSuccessorCount(bn) <  // FIXME: is this correct???
                specGraph.getSuccessorCount(bn.getEquivalentInSpec());*/
    }

    /**
     * Main function to match nodes between the Full LTS and the Counterexample LTS
     *
     * @param specGraph     full LTS
     * @param specStartNode intial node of the full LTS
     * @param badGraph      counterexample LTS
     * @param badStartNode  initial node of the Counterexample LTS
     * @return
     */
    public boolean matchNodes(LTS specGraph, GraphNode specStartNode, LTS badGraph,
                              GraphNode badStartNode) {
        long startTime = System.currentTimeMillis();
        int minDiff = Integer.MAX_VALUE;
        List<GraphNode> bestMatching = null;


        outputWriter.printComplete("BFSmatching: starting matching.", true, true);

        // init support data
        this.genBFS(badGraph, badStartNode); // init nodesToProcess
        this.genConflicts(badGraph); // init conflicts
        this.nbDifferences = new int[this.nodesToProcess.size()];

        // checking initial node
        MatchingDecision firstDecision = new SimpleMatching(specStartNode, badStartNode, null);
        if (!firstDecision.nextAlternative(specGraph, badGraph)) {  // matching initial nodes
            outputWriter.printComplete("Initial nodes not matched!", true, true);
            return false;  // Can this happen ?
        } else {
            this.decisions.push(firstDecision);  // inserting the firstDecision
            if (isDiff(specGraph, badGraph, badStartNode)) {
                this.nbDifferences[0] = 1;
                // FIXME: shouldn't be always true if the nextAlernative returns true?
                // Adding an error message to check this case
                //System.err.println("Checking initial node, nbDifferences = 1, should not be " +
                //        "possible");
            } else {
                this.nbDifferences[0] = 0;
            }
        }


        // main loop
        int debuggingElseIterations = 0;  // debugging-purpose variable
        while (!this.decisions.isEmpty()) {
            if (debuggingElseIterations%100000==0)
                outputWriter.printComplete("BFSmatcher: iteration n "+ debuggingElseIterations +
                        " Decisions queue size: "+ this.decisions.size(),
                        false, true);
            if (this.decisions.size() == this.nodesToProcess.size()) {
                // I found a coherent matching of the whole graph
                int nbDiff = this.nbDifferences[this.nbDifferences.length - 1];
                if (nbDiff < minDiff) {
                    //System.err.println("BFSStackMatcherMinDiff.matchNode: " +
                    //        "improved configuration with nbDiff " + nbDiff);
                    minDiff = nbDiff;
                    if (bestMatching == null) {
                        bestMatching = new ArrayList<>(this.nodesToProcess.size());
                    } else {
                        bestMatching.clear();
                    }
                    for (GraphNode n : this.nodesToProcess) {  // final matching
                        bestMatching.add(n.getEquivalentInSpec());
                    }
                }
                //System.err.println("BFSStackMatcherMinDiff.matchNode: coherent matching found, " +
                //        "calling changeADecision");
                this.changeADecision(specGraph, badGraph, minDiff);  // trigger new decisions
                // TODO: if the matching here is coherent, why start changing decisions?
            } else {
                //System.err.println("BFSStackMatcherMinDiff.matchNode: generating decision.");
                // generating decision (candidates evaluation)
                MatchingDecision decision = this.genDecision(specGraph, badGraph);
                debuggingElseIterations++;
                if (decision != null) {
                    this.decisions.push(decision);
                    if (!this.makeADecision(specGraph, badGraph, minDiff)) {
                        this.decisions.pop();
                        //System.err.println("simply wanna now if I arrive here");
                        this.changeADecision(specGraph, badGraph, minDiff);
                    }
                } else {
                    // in this case no candidate where found, so we change the decision
                    /*System.err.println("BFSStackMatcherMinDiff.matchNode: no candidates " +
                            "found, triggering changeADecision, minDiff=" + minDiff + ", " +
                            "iterations on else branch: " + debuggingElseIterations);
                    debuggingElseIterations = 0;*/
                    this.changeADecision(specGraph, badGraph, minDiff);
                    // FIXME: if changeADecision is not able to find a new decision, it will empty
                    // FIXME (continues): the 'decisions' field. Thus, we will not have any correct
                    // FIXME (continues): decision left to produce a matching. Is this correct?
                }
            }

        }

        outputWriter.printComplete("BFSmatching: matching ended after "+debuggingElseIterations+"" +
                " iterations.",
                true, true);

        // final part, setting the result
        if (bestMatching == null) {
            long endTime = System.currentTimeMillis();
            outputWriter.printComplete("BFSStackMatcherMinDiff.matchNodes: not able to find the " +
                    "best matching!!", true, true);
            outputWriter.printComplete("BFSStackMatcherMinDiff.matchNodes exec time: " +
                    (endTime - startTime) + " ms\n", true, true);
            return false;
        } else {
            for (int i = 0; i < this.nodesToProcess.size(); i++) {
                this.nodesToProcess.get(i).setEquivalentInSpec(bestMatching.get(i));
            }
            long endTime = System.currentTimeMillis();
            outputWriter.printComplete("BFSStackMatcherMinDiff.matchNodes exec time: " +
                    (endTime - startTime) + " ms\n", true, true);
            return true;
        }
    }

    /**
     * @param specGraph         the Full LTS graph
     * @param badGraph          the Counterexample LTS graph
     * @param currentBestNbDiff number of differences in the matching decision
     * @return
     */
    private boolean makeADecision(LTS specGraph, LTS badGraph, int currentBestNbDiff) {
        boolean found = false;
        MatchingDecision dec = this.decisions.peek();  // Retrieves, but does not remove, the head
        // of the queue represented by this deque
        while (!found && dec.nextAlternative(specGraph, badGraph)) {
            if (isDiff(specGraph, badGraph, this.nodesToProcess.get(this.decisions.size() - 1))) {
                this.nbDifferences[this.decisions.size() - 1] =
                        this.nbDifferences[this.decisions.size() - 2] + 1;
            } else {
                this.nbDifferences[this.decisions.size() - 1] =
                        this.nbDifferences[this.decisions.size() - 2];
            }
            if (this.nbDifferences[this.decisions.size() - 1] < currentBestNbDiff) {
                found = true;
            }
        }
        //System.err.println("BFSStackMatcherMinDiff.makeADecision: found value is "+ found);
        return found;
    }

    /**
     * It allows to change a decision.
     * Invoke the makeADecision method until the decision queue is empty.
     *
     * @param specGraph         full LTS
     * @param badGraph          Counterexample LTS
     * @param currentBestNbDiff number of differences in the matching decision
     */
    private void changeADecision(LTS specGraph, LTS badGraph, int currentBestNbDiff) {
        //System.err.println("BFSStackMathcerMinDiff.changeADecision: starting changing " +
        //        "decision");
        while (!this.decisions.isEmpty()) {  // work until no other decisions are remaining
            //System.out.println("DEBUG BFSStackMathcerMinDiff.changeADecision: looping");
            if (this.makeADecision(specGraph, badGraph, currentBestNbDiff)) {
                //System.err.println("BFSStackMatcherMinDiff.changeADecision: triggering " +
                //        "makeADecision");
                return;
            } else {
                this.decisions.pop();  // remove last decision
                // coming back to change decision, so I am deleting the last one
            }
        }
        // FIXME: what happen if the decision queue is empty, and no new decisions are made??
        // FIXME (continues): This just happened in one exaple, not able to find a matching.
        //System.err.println("BFSStackMatcherMinDiff.changeADecision: unable to change a decision
        // .");
    }

    /**
     * Fills nodesToProcess with nodes from 'graph' in a BFS order.
     * Notice that this is only the order in which every node will be checked.
     *
     * @param graph     an LTS graph
     * @param startNode initial node of graph
     */
    private void genBFS(LTS graph, GraphNode startNode) {
        long startTime = System.currentTimeMillis();
        outputWriter.printComplete("BFSmatcher: genBFS starting", true, true);
        Set<GraphNode> seenNode = new HashSet<>();
        this.nodesToProcess.add(startNode);
        seenNode.add(startNode);
        int debuggingIterations = 0;
        for (int pos = 0; pos < this.nodesToProcess.size(); pos++) {
            if (debuggingIterations%100000==0)
                outputWriter.printComplete("BFSmatcher: genBFS nodes processed: " +
                    debuggingIterations, false, true);
            for (GraphNode n : graph.getSuccessors(this.nodesToProcess.get(pos))) {
                if (seenNode.add(n)) {  // if the node is not in seenNode (condition = true)
                    this.nodesToProcess.add(n); // add it to nodesToProcess
                }
            }
            debuggingIterations++;
        }
        outputWriter.printComplete("BFSmatcher: genBFS ends after "+ debuggingIterations +" nodes",
                true, true);
        long endTime = System.currentTimeMillis();
        outputWriter.printComplete("BFSMatcher genBFS exec time: " +
                (endTime - startTime) + " ms\n", true, true);


    }

    private void genDFS(LTS graph, GraphNode startNode) {
/*        Set<GraphNode> seenNode = new HashSet<>();
        this.nodesToProcess.add(startNode);
        seenNode.add(startNode);
        for (int pos = 0; pos < this.nodesToProcess.size(); pos++) {
            for (GraphNode n : graph.getSuccessors(this.nodesToProcess.get(pos))) {
                if (seenNode.add(n)) {  // if the node is not in seenNode (condition = true)
                    this.nodesToProcess.add(n); // add it to nodesToProcess
                }
            }
        }

        node.setDfsDiscovered();
        Set<GraphEdge> path; // used for the path from the initial node to the current one
        if (node.isFrontier()) {
            edgeBlackColourer(previousPath);
            previousPath.clear();  // in this way we don't color in Black more than once the same transition
        }
        for (GraphEdge e : lts.getOutEdges(node)) {
            GraphNode nextNode = lts.getDest(e);
            if (!nextNode.isDfsDiscovered()) {
                path = new HashSet<>(); // need to create another instance, since we must have different sets of paths
                path.addAll(previousPath);
                path.add(e);
                recursiveDFS(lts, nextNode, path);
            }
        }*/
    }

    /**
     * Generates list of possible conflicts for each node.
     * Conflicting nodes are all the successors of predecessors, minus the current node (bn)
     *
     * @param badGraph the counterexample LTS
     */
    private void genConflicts(LTS badGraph) {
        long startTime = System.currentTimeMillis();
        outputWriter.printComplete("BFSmatcher: genConflicts starting", true, true);
        int debuggingIterations = 0;
        for (GraphNode bn : this.nodesToProcess) {  // for each node in nodesToProcess
            if (debuggingIterations%1000==0)
                outputWriter.printComplete("BFSmatcher: genConflicts node processed "+
                        debuggingIterations, false, true);
            Set<GraphNode> s = new HashSet<>();  // each node will have its own set
            for (GraphNode pred : badGraph.getPredecessors(bn)) {
                s.addAll(badGraph.getSuccessors(pred));
                // all successors of predecessors are considered as possible conflicts
            }
            s.remove(bn);
            this.conflicts.add(s);
            debuggingIterations++;
        }
        outputWriter.printComplete("BFSmatcher: genConflicts ends after "+ debuggingIterations
                +" nodes", true, true);
        long endTime = System.currentTimeMillis();
        outputWriter.printComplete("BFSMatcher genConflicts exec time: " +
                (endTime - startTime) + " ms\n", true, true);

    }

    /**
     * @param specGraph the Full LTS
     * @param badGraph  the Counterexample LTS
     * @return a MatchingDecision
     */
    private MatchingDecision genDecision(LTS specGraph, LTS badGraph) {
        GraphNode badNode = this.nodesToProcess.get(this.decisions.size());
        Set<GraphNode> candidates = null;  // candidates are potential candidate nodes in the
        // Full LTS that should simulates the badNode

        // DEBUGGING: printing in and out transitions of the badNode
/*        System.err.println("\nBadNode: " + badNode.toString());
        System.err.println("In trans: " + badGraph.getInEdges(badNode));
        System.err.println("Out trans: " + badGraph.getOutEdges(badNode));*/

        // looping on predecessors of the badNode to find candidates
        for (GraphNode parentNode : badGraph.getPredecessors(badNode)) {

/*            System.err.println("*** Predecessors step, child node:" + parentNode.toString() + "  " +
                    "***");*/

            // working only if the equivalent state is set in the parent node
            if (parentNode.getEquivalentInSpec() != null) {

                // loading transitions between predecessors and badNode in the Counterexample LTS
                Set<String> badTransitions = new HashSet<>();  // duplicates not allowed
                for (GraphEdge edge : badGraph.findEdgeSet(parentNode, badNode)) {
                    badTransitions.add(edge.getAction());
                }
                Set<GraphNode> localCandidates = new HashSet<>();

                // looping on successors of the equivalent node in the Full LTS to collect local
                // candidates, searching for the potential node that simulates the badNode
                for (GraphNode potential :
                        specGraph.getSuccessors(parentNode.getEquivalentInSpec())) {

                    // DEBUGGING: printing in and out transition of the potential candidate
                    //System.err.println("Pred potential: " + potential.toString());
                    //System.err.println("Pred In trans: " + specGraph.getInEdges
                    // (potential));
                    //System.err.println("Pred Out trans: " + specGraph.getOutEdges
                    //        (potential));

                    // working only if 'candidates' is empty or if it contains the current successor
                    if (candidates == null || candidates.contains(potential)) {
                        // TODO: why we are not considering other candidates than the potential?
                        Set<String> specTrans = new HashSet<>();

                        // extracting all the transitions in the spec between the equivalent node
                        // of 'parentNode' and the 'potential' successor
                        for (GraphEdge e : specGraph.findEdgeSet(parentNode.getEquivalentInSpec(),
                                potential)) {
                            specTrans.add(e.getAction());
                        }
                        if (specTrans.containsAll(badTransitions)) {
                            // if the set of transitions is the same, this is a potential candidate
                            localCandidates.add(potential);
/*                            System.err.println("Pred potential: " + potential.toString());
                            System.err.println("Pred In trans: " + specGraph.getInEdges(potential));
                            System.err.println("Pred Out trans: " + specGraph.getOutEdges
                                    (potential));*/
                        }
                    }
                }
                if (candidates == null) {
                    candidates = localCandidates;
                } else {
                    // keeping only candidates that were also found with the last parentNode
                    candidates.retainAll(localCandidates);
                    if (candidates.isEmpty()) {
                        //System.err.println("BFSStackMathcerMinDiff.genDecision: after " +
                        //        "evaluation of predecessors, no candidates for the current node
                        // .");
                        return null;
                    }
                }
            }
        }

        // DEBUGGING
/*        System.err.println("*** Candidates before successors ***");
        for (GraphNode currCand : candidates) {

            // DEBUGGING: printing in and out transition of the  candidate
            System.err.println("Candidate: " + currCand.toString());
            System.err.println("Candidate In trans: " + specGraph.getInEdges(currCand));
            System.err.println("Candidate Out trans: " + specGraph.getOutEdges(currCand));
        }*/

        // looping on successors of the badNode to find candidates
        for (GraphNode childNode : badGraph.getSuccessors(badNode)) {

            // System.err.println("*** Successors step, child node:" + childNode.toString() + "
            // ***");

            // works only if childNode has the equivalent on the Full LTS
            if (childNode.getEquivalentInSpec() != null) {

                // loading transition between badNode and childNode in the Counterexample LTS
                Set<String> badTransitions = new HashSet<>();
                for (GraphEdge edge : badGraph.findEdgeSet(badNode, childNode)) {
                    badTransitions.add(edge.getAction());
                }
                Set<GraphNode> localCandidates = new HashSet<>();

                // looping on predecessors of the equivalent node in the Full LTS to collect local
                // candidates, searching for the potential node that simulates the badNode
                for (GraphNode potential :
                        specGraph.getPredecessors(childNode.getEquivalentInSpec())) {
                    // in this case the candidates set should be not empty, since at least a
                    // candidate should already have been discovered in the previous step
                    // (predecessors of badNode). So we only check if the candidate exists.

                    // DEBUGGING: printing in and out transition of the potential candidate
                    //System.err.println("Succ potential: " + potential.toString());
                    //System.err.println("Succ In trans: " + specGraph.getInEdges(potential));
                    //System.err.println("Succ Out trans: " + specGraph.getOutEdges
                    // (potential));

                    if (candidates.contains(potential)) {
                        Set<String> specTrans = new HashSet<>();

                        // extracting all the transitions in the spec between the 'potential' node
                        // and the equivalent node of 'childNode' in the spec
                        for (GraphEdge e : specGraph.findEdgeSet(potential,
                                childNode.getEquivalentInSpec())) {
                            specTrans.add(e.getAction());
                        }
                        if (specTrans.containsAll(badTransitions)) {
                            // if the badTransitions exist also in the spec, we add the candidate
                            // in the 'potential' set

                            //System.err.println("Succ potential: " + potential.toString());
                            //System.err.println("Succ In trans: " + specGraph.getInEdges
                            //        (potential));
                            //System.err.println("Succ Out trans: " + specGraph.getOutEdges
                            //        (potential));

                            localCandidates.add(potential);
                        }
                    }
                }
                // keeping only candidates that where found with the last childNode
/*                System.err.println("CANDIDATES: "+ candidates.toString());
                System.err.println("LOCAL: "+ localCandidates.toString());*/
                candidates.retainAll(localCandidates);
                if (candidates.isEmpty()) {

                    // DEBUGGING: calculating the size of the shortest path between the initial node
                    // and the badNode
                    int depth = 0;
                    Set<GraphNode> source = new HashSet<>();
                    source.add(badNode);
                    while (!source.contains(badGraph.getInitialNode())) {
                        depth++;
                        Set<GraphNode> nextGen = new HashSet<>();
                        for (GraphNode n : source) {
                            nextGen.addAll(badGraph.getPredecessors(n));
                        }
                        source = nextGen;
                    }
                    //System.err.println("Depth " + depth);

                    //System.err.println("BFSStackMathcerMinDiff.genDecision: after evaluation of
                    // " +
                     //       "successors, no candidates for the current node.");
                    return null;
                }
            }
        }

        // Returning the final result
        if (candidates.size() == 1) {  // in this case we've found only one candidate
            //System.out.println("DEBUG BFSStackMathcerMinDiff.genDecision: only one candidate, " +
            //        "SimpleMatching");
            GraphNode finalChoice = candidates.iterator().next();
            //System.err.println("Final choice: " + finalChoice.toString());
            //System.err.println("In trans: " + specGraph.getInEdges(finalChoice));
            //System.err.println("Out trans: " + specGraph.getOutEdges(finalChoice));
            /*return new SimpleMatching(candidates.iterator().next(), badNode,
                    this.conflicts.get(this.decisions.size()));*/
            return new SimpleMatching(finalChoice, badNode,
                    this.conflicts.get(this.decisions.size()));
        } else { // in this case we've found more than one candidates
            //System.err.println("BFSStackMathcerMinDiff.genDecision: multiple candidates, " +
            //        "MultipleMatching");
            GraphNode[] array = new GraphNode[candidates.size()];
            // List<GraphNode> l = new ArrayList<>(candidates);
            // Collections.reverse(l);
            GraphNode[] potentialMatches = candidates.toArray(array);
            return new MultipleMatching(potentialMatches, badNode,
                    this.conflicts.get(this.decisions.size()));
        }
    }

}
