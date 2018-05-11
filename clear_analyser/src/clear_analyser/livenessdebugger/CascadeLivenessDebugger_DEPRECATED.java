package clear_analyser.livenessdebugger;

import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import clear_analyser.sccfinder.SCC;
import clear_analyser.sccfinder.SCCSet;
import clear_analyser.sccfinder.TarjanIterative;
import clear_analyser.utils.STDOut;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by gbarbon.
 */
public class CascadeLivenessDebugger_DEPRECATED {
    private String propFile;
    private Collection<String> actions;
    private LTS fullLTS;
    private String baseDir;
    private STDOut writer;

    public CascadeLivenessDebugger_DEPRECATED(String propFile, LTS fullLts, String baseDir, STDOut
            writer) throws Exception {
        this.propFile = propFile;
        this.fullLTS = fullLts;
        this.actions = null;
        this.baseDir = baseDir;
        this.writer = writer;
        simplePropertyParser();  // extraction action in the property
    }

    public CascadeLivenessDebugger_DEPRECATED(Collection<String> actions, LTS fullLts, STDOut writer)
            throws
            Exception {
        this.actions = actions;
        this.fullLTS = fullLts;
        this.writer = writer;
    }

    /**
     * TODO: implement parsing of the property (only with inevitability of a single action)
     */
    private void simplePropertyParser() throws Exception {
        // this.action = "";
        BufferedReader br = new BufferedReader(new FileReader(baseDir + "/" + propFile + ".mcl"));
        // TODO: check that the file is actually an mcl file
        // TODO: add exception in case of missing file
        String line;
        while ((line = br.readLine()) != null) {

            // exploiting macros of standard.mcl :
            if (line.toLowerCase().contains("INEVITABLE".toLowerCase())) {
                int firstBraket = line.indexOf("(");
                int lastBraket = line.lastIndexOf(")");
                //this.action = line.substring(firstBraket + 1, lastBraket).replace("\"", "")
                // .trim();
                // TODO: collect all the sequence of action
            }
            // TODO: add a String array to handle multiple inevitability

        }
        br.close();

        //writer.printComplete("simplePropertyParser: action is " + this.action + " ", true, true);
    }

    /**
     * Colours the full LTS in case of liveness
     */
    public void executor() {
        Set<GraphEdge> actionEdges = new HashSet<>();

        // init for first iteration (or single action liveness checking)
        Set<GraphNode> initialNodes = new HashSet<>();
        initialNodes.add(fullLTS.getInitialNode());

        // Step 1 [SCC detection]: find SCC
        SCCSet sccSet = new TarjanIterative(fullLTS).tarjanCaller();
        sccSet.collectEdgesInScc();

        // iterate over the set of actions
        String previousAtction = null;
        int i = 0 ;
        for (String action : actions) {
            actionEdges = livenessLTSGenerator(i, actionEdges, action, previousAtction, sccSet);
            previousAtction = action;
            i++;
        }
    }

    private Set<GraphEdge> livenessLTSGenerator(int colIter, Set<GraphEdge> previousActionEdges,
                                                String currAction, String previousAction,
                                                SCCSet sccSet) {
        Set<GraphNode> finalStates;
        Set<GraphEdge> actionEdges; // edges containing the inevitable action

        // preparing nodes for this iteration
        // Step 1 [input refining]
        Set<GraphNode> initialNodes = new HashSet<>();
        if (colIter==0) {
            initialNodes.add(fullLTS.getInitialNode());
        } else {
            for (GraphEdge edge : previousActionEdges) {
                initialNodes.add(fullLTS.getDest(edge));
            }
            inputRefiner(sccSet, currAction, previousAction, previousActionEdges, colIter);
        }

        // Step 2 [green coloring]: extraction transitions with action 'currAction' and coloring
        // transitions that bring to 'currAction'
        actionEdges = actionSearcher(currAction, initialNodes);
        greenColorer(actionEdges, colIter);

        // Step 3 [red coloring]: coloring transitions that bring to final states
        // and retrieves final states
        finalStates = redColorer(colIter, initialNodes, currAction);

        // Step 4 [black coloring]: color transitions to final states and SCC in black
        blackColorer(finalStates, sccSet, currAction, colIter);

        // Step 5 [backtracking]:
        if (colIter!=0) {
            backtracker(initialNodes, colIter);
        }

        return actionEdges;
    }

    /**
     * DFS of actions. Retrieves all the transitions that match the given action. It stops at the
     * first instances of the action founded in a given path, meaning that it avoid successive
     * instances.
     * TODO: what if it founds an action in an SCC?
     *
     * @param action      is the searched action
     * @param initialNodes  the set of initial nodes is provided to make it usable also with
     *                      sub-graphs
     * @return the set of transitions that match the action
     */
    private Set<GraphEdge> actionSearcher(String action, Set<GraphNode> initialNodes) {
        Set<GraphEdge> aTrans = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();
        Set<GraphEdge> discovered = new HashSet<>();
        GraphEdge edge;

        for (GraphNode n : initialNodes) {
            fullLTS.getOutEdges(n).parallelStream().forEach(todo::push); // init
        }
        while (!todo.isEmpty()) {
            edge = todo.pop();
            if (edge.isUnset() && discovered.add(edge)) {
                // the edge.isUnset check allows to work only in areas that have not been coloured
                if (edge.getAction().equals(action))
                    aTrans.add(edge);
                else
                    fullLTS.getOutEdges(fullLTS.getDest(edge)).parallelStream().forEach(todo::push);
            }
        }

        return aTrans;
    }

    /**
     * Color in green all the transitions from the initial state
     *
     * @param aTrans  the set of transitions containing action A
     * @param colIter coloring iteration number on edge
     */
    private void greenColorer(Set<GraphEdge> aTrans, int colIter) {
        Set<GraphEdge> green = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();
        GraphEdge edge;

        // coloring in green the inevitable actions and loading edges in the stack
        aTrans.parallelStream().forEach(e -> {
            green.add(e);  // will allow to color in green later
            todo.push(e);
        });

        while (!todo.isEmpty()) {
            edge = todo.pop();
            fullLTS.getInEdges(fullLTS.getSource(edge)).parallelStream().forEach(predEdge -> {
                if (predEdge.isUnset() && green.add(predEdge)) {
                    todo.push(predEdge);
                }
            });
        }

        green.parallelStream().forEach(e -> e.setAsCorrect(colIter));
    }

    /**
     * Color in RED the states that are not set and that are not preceded by the action contained
     * in the Inevitability statement (in this way it colours also SCC, that are before the
     * inevitability, in RED)
     *
     * @param colIter coloring iteration number on edge
     * @param initialNodes set of nodes to start the red coloring
     * @return the set of the final nodes
     */
    private Set<GraphNode> redColorer(int colIter, Set<GraphNode> initialNodes, String action) {
        Stack<GraphNode> todo = new Stack<>();
        Set<GraphEdge> toColor = new HashSet<>();
        Set<GraphEdge> notToColor = new HashSet<>();
        Set<GraphNode> finalStates = new HashSet<>();
        Collection<GraphEdge> outEdges;
        GraphNode state;

        // init
        initialNodes.parallelStream().forEach(todo::push);

        while (!todo.isEmpty()) {
            state = todo.pop();
            outEdges = fullLTS.getOutEdges(state);
            if (outEdges.isEmpty()) // no successors, state is a final state
                finalStates.add(state);
            else
                for (GraphEdge edge : outEdges) {
                    if (edge.isUnset() && toColor.add(edge))
                        todo.push(fullLTS.getDest(edge));
                    else if (edge.canColor(colIter) && !edge.getAction().equals(action) &&
                            notToColor.add(edge))
                        // if clause needed to traverse edges that are coloured in green from step 1
                        // of the same iteration (path coloured in green can share prefixes with
                        // paths that lead to final states). The canColor method allows to check
                        // if the coloring belongs to the current iteration.
                        todo.push(fullLTS.getDest(edge));
                }
        }

        // coloring in red
        toColor.parallelStream().forEach(e -> e.setAsIncorrect(colIter));

        return finalStates;
    }

    /**
     * Colours in black:
     * 1) transitions in path to final states that are green
     * 2) transitions in SCC and in paths that bring to SCC
     *
     * @param colIter coloring iteration number on edge
     * @param finalStates set of final states
     * @param sccSet      set of scc
     */
    private void blackColorer(Set<GraphNode> finalStates, SCCSet sccSet, String currentAction,
                              int colIter) {
        Set<GraphEdge> toColor = new HashSet<>();
        Set<GraphEdge> notToColor = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();

        // init coloring from finalStates
        for (GraphNode n : finalStates) {
            fullLTS.getInEdges(n).stream().forEach(e -> {
                todo.push(e);
                if (e.canColor(colIter) && e.isCorrect())
                    // this should not be possible, since they should all be RED
                    toColor.add(e);
                if (e.canColor(colIter) && e.isIncorrect())
                    // this should not be necessary, since the first edges are
                    // traversed only one time
                    notToColor.add(e);
            });
        }

        // colouring paths from finalStates
        while (!todo.isEmpty()) {
            GraphEdge e = todo.pop();
            fullLTS.getInEdges(fullLTS.getSource(e)).parallelStream().forEach(predEdge -> {
                if (!predEdge.isNeutral()) {
                    // if neutral no need to color, and we don't need to add it to the stack
                    if (predEdge.canColor(colIter) && predEdge.isCorrect() && toColor.add(predEdge))
                        todo.push(predEdge);
                    else if (predEdge.canColor(colIter) && predEdge.isIncorrect() && notToColor.add
                            (predEdge))
                        todo.push(predEdge);
                }
            });
        }
        toColor.parallelStream().forEach(e -> e.setAsNeutral(colIter));

        // colouring the SCCs and paths to SCCs
        Set<GraphEdge> sccToColor = new HashSet<>();
        Set<GraphEdge> sccNotToColor = new HashSet<>();
        Stack<GraphEdge> sccTodo = new Stack<>();

        // iterating over all the SCCs and predecessor
        for (SCC scc : sccSet.getSCCs()) {

            if (!scc.isUnset() && scc.canColor(colIter) && !scc.containsAction(currentAction)) {
                    // if clause avoids colouring SCC containing UNSET paths
                    sccTodo.addAll(scc.getEdges());
                    while (!sccTodo.isEmpty()) {
                        GraphEdge e = sccTodo.pop();
                        // getInEdges method allows to exit from the SCC and colour the paths that
                        // bring to the scc
                        fullLTS.getInEdges(fullLTS.getSource(e)).parallelStream().forEach(predEdge -> {
                            if (predEdge.canColor(colIter) && predEdge.isCorrect() && sccToColor.add
                                    (predEdge))
                                sccTodo.push(predEdge);
                            else if (predEdge.canColor(colIter) && !predEdge.isUnset() && sccNotToColor
                                    .add(predEdge))
                                // if clause allows to avoid the SCCs contained after the Inevitability
                                // TODO: see comment above, maybe it is not true, so following part
                                // can be removed
                                sccTodo.push(predEdge);
                        });
                    }
                }
        }
        sccToColor.parallelStream().forEach(e -> e.setAsNeutral(colIter));
    }

    /*
    private void sccsRefining(SCCSet set, String currentAction, int colIter) {
        SCCSet newSet;
        Set<GraphEdge> sccToColor = new HashSet<>();
        Set<GraphEdge> sccNotToColor = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();

        newSet = set.getCorrectSCCSubSet();
        newSet = newSet.getSCCSubSetNotContAction(currentAction);

        // iterating over all the SCCs and predecessor
        for (SCC scc : newSet.getSCCs()) {

                todo.addAll(scc.getEdges());
                while (!todo.isEmpty()) {
                    GraphEdge e = todo.pop();
                    // getInEdges method allows to exit from the SCC and colour the paths that
                    // bring to the scc
                    fullLTS.getInEdges(fullLTS.getSource(e)).parallelStream().forEach(predEdge -> {
                        if (predEdge.canColor(colIter) && predEdge.isCorrect() && sccToColor.add
                                (predEdge))
                            todo.push(predEdge);
                        else if (predEdge.canColor(colIter) && !predEdge.isUnset() && sccNotToColor
                                .add(predEdge))
                            // if clause allows to avoid the SCCs contained after the Inevitability
                            // TODO: see comment above, maybe it is not true, so following part
                            // can be removed
                            todo.push(predEdge);
                    });
                }
        }

        sccToColor.parallelStream().forEach(e -> e.setAsIncorrect(colIter));

    }


    private void backtracking(SCCSet sccSet, Set<GraphNode> initialNodes, String currentAction, int
            colIter) {

        // Backtracking on initialNodes:
        // for each initial node:
        // 1. if initial node only have red successors
        // 2. collect edge to color in red)
        // 3. evaluate source node: if source has no green nor black outgoing edge (with
        // exception of the one from which we come from), go on coloring in red. If source has no
        // green but black outgoing edge (with exception of t he one we come from), go on coloring
        // in black. If source has green outside edge, stops.

        // checkintegrity on scc

        // Backtracking on SCC:
        // 1. find green correct scc,
        // 2. verify them (search new action)
        // 3. color scc and predecessor



    }

     */

    /**
     * Refine the input by checking and possibly coloring the previous SCC and by choosing the
     * new input nodes.
     * @param set
     * @param currIter
     * @param currentAction
     * @param previousAction
     * @param previousActionEdges
     * @return
     */
    private Set<GraphNode> inputRefiner(SCCSet set, String
            currentAction, String previousAction, Set<GraphEdge> previousActionEdges, int
            currIter) {
        Set<GraphNode> refinedInitialNodes = new HashSet<>();
        Set<GraphEdge> allSccUnsetExitingEdges = new HashSet<>();

        // removing edge containing previousAction from the set of previousActionEdges for this
        // iteration
        set.getSCCs().parallelStream().forEach(scc -> {

            // removing edges that are not used as source for initialNodes
            previousActionEdges.removeAll(scc.getEdgesMatchingAction(previousAction));

            // now cleaning green SCC that do not contains currentAction
            if (!scc.containsAction(currentAction)) {
                // 1. color the scc in black
                scc.setAsNeutral(scc.getColorIter()); // set as neutral using the same color iter

                // 2. collect of edges at the extremity of the scc
                Set<GraphEdge> unsetEdges = set.getUnsetOutEdges(scc);
                allSccUnsetExitingEdges.addAll(unsetEdges);
                Set<GraphEdge> setEdges = set.getNonUnsetOutEdges(scc);  // colored edges

                // 3. init backtracking and coloring first batch of edges
                Stack<GraphNode> todo = new Stack<>();
                setEdges.parallelStream().forEach(edge -> {
                    if (edge.getColorIter()<currIter) {
                        GraphNode source = fullLTS.getSource(edge);
                        if (edge.isCorrect()) {
                            // outgoing are black, or other combinations
                            edge.setAsNeutral();
                            todo.push(source);
                        }
                        // TODO: add asserts for all the cases that should not be taken into account
                    } else
                        // this should not be possible, we do not have colored
                        // anything with the currIter until now
                        throw new AssertionError();

                });

                // 3. launch the backtracking on the 'false' green SCC predecessors
                while (!todo.isEmpty()) {
                    GraphNode currNode = todo.pop();
                    fullLTS.getInEdges(currNode).parallelStream().forEach(edge -> {
                                if (edge.getColorIter()<currIter) {  // backtrack only on previous colored part
                                    GraphNode source = fullLTS.getSource(edge);
                                    if (edge.isCorrect() && fullLTS.areOutTransMixed(currNode)) {
                                        // outgoing are black, or other combinations
                                        edge.setAsNeutral();
                                        todo.push(source);
                                    }
                                    // TODO: add asserts for all the cases that should not be taken into account
                                }
                                else
                                    // this should not be possible, we do not have colored
                                    // anything with the currIter until now
                                    throw new AssertionError();
                                } );
                }
            }
        }
        );

        // producing refined set of initial nodes
        for (GraphEdge edge : previousActionEdges) {
            refinedInitialNodes.add(fullLTS.getDest(edge));
        }
        // Now add edges at the limit of the scc that contains UNSET edges
        // Note that the approach is different between and scc exiting edge and an edge from the
        // previousActionEdges set (source for the first, dest for the latter),  so we cannot make
        // the merge of the two
        for (GraphEdge edge : allSccUnsetExitingEdges) {
            refinedInitialNodes.add(fullLTS.getSource(edge));
        }

        return refinedInitialNodes;
    }

    /**
     * Backtracker re-colour transitions colored in the previous iteration, following the coloring
     * just done for the current iteration.
     *
     * @param startingNodes all the states
     * @param currIter the current iteration number. Note that currIter is not used to set
     *                 coloring in the backtracker method.
     */
    private void backtracker(Set<GraphNode> startingNodes, int currIter) {
        Stack<GraphNode> todo = new Stack<>();

        //init
        startingNodes.parallelStream().forEach(e -> {
            // avoid nodes without successors
            if (!fullLTS.getOutNeutralTransitions(e).isEmpty())
                todo.push(e);
        });

        while (!todo.isEmpty()) {
            GraphNode currNode = todo.pop();
            fullLTS.getInEdges(currNode).parallelStream().forEach(edge -> {
                if (edge.getColorIter()<currIter) {  // backtrack only on previous colored part
                    GraphNode source = fullLTS.getSource(edge);
                    if (edge.isCorrect()) {
                        if (fullLTS.areOutTransIncorrect(currNode))  // outgoing are only red
                            edge.setAsIncorrect();
                        else if (fullLTS.areOutTransMixed(currNode)) // outgoing are black, or
                            // other combinations
                            edge.setAsNeutral();
                        todo.push(source);
                    }
                    else if (edge.isNeutral() && fullLTS.areOutTransIncorrect(currNode)) {
                        // outgoing are red
                        edge.setAsNeutral();
                        todo.push(source);
                    }
                    // TODO: add asserts for all the cases that should not be taken into account
                }
            });
        }
    }
}
