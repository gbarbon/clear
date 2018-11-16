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
public class SimpleLivenessDebugger {
    private String propFile;
    private String action;
    private LTS fullLTS;
    private String baseDir;
    private STDOut writer;

    public SimpleLivenessDebugger(String propFile, LTS fullLts, String baseDir, STDOut
            writer) throws Exception {
        this.propFile = propFile;
        this.fullLTS = fullLts;
        this.action = "";
        this.baseDir = baseDir;
        this.writer = writer;
        simplePropertyParser();  // extraction action in the property
    }

    public SimpleLivenessDebugger(String action, LTS fullLts, STDOut writer) throws
            Exception {
        this.action = action;
        this.fullLTS = fullLts;
        this.writer = writer;
    }


    /**
     * TODO: implement parsing of the property (only with inevitability of a single action)
     */
    private void simplePropertyParser() throws Exception {
        this.action = "";
        BufferedReader br = new BufferedReader(new FileReader(baseDir + "/" + propFile + ".mcl"));
        // TODO: check that the file is actually an mcl file
        // TODO: add exception in case of missing file
        String line;
        while ((line = br.readLine()) != null) {

            // exploiting macros of standard.mcl :
            if (line.toLowerCase().contains("INEVITABLE".toLowerCase())) {
                int firstBraket = line.indexOf("(");
                int lastBraket = line.lastIndexOf(")");
                this.action = line.substring(firstBraket + 1, lastBraket).replace("\"", "").trim();
            }

            // TODO: add a String array to handle multiple inevitability

        }
        br.close();

        writer.printComplete("simplePropertyParser: action is " + this.action + " ", true, true);
    }

    /**
     * This colours the full LTS in case of liveness
     * Old version, not correct
     */
    /*public void livenessLTSGeneratorOLD() {
        Set<GraphEdge> aTrans;
        //Set<GraphEdge> greenTrans;

        // extraction transitions with action 'action'
        aTrans = getsAllATrans(action);
        // coloring transitions that bring to 'action'
        greenColorerNoRec(aTrans);
        // coloring transitions after 'action'
        successorColorer(aTrans);

        // find SCC where A is not existing (thus where lassos exists)
        SCCSet incorrectSccSet = new TarjanIterativeAction(fullLTS, action).tarjanCaller();
        // colour all the SCC in red
        for (SCC scc : incorrectSccSet.getSCCs()) {
            // FIXME: I don't like to use TranistionType here, better to use a lambda with
            // FIXME (continues): setAsincorrect function
            scc.sccColorer(fullLTS, GraphEdge.TransitionType.RED);
        }
        // now color all the paths that bring to the scc
        for (SCC scc : incorrectSccSet.getSCCs()) {
            redOrBlackColorer(scc.getNodes());
        }

        // setting all the rest to red
        unsetToRed();
    }*/

    /**
     * Colours the full LTS in case of liveness
     */
    public void executor() {

        // Step 1 [SCC detection]: find SCC
        SCCSet sccSet = new TarjanIterative(fullLTS).tarjanCaller();
        sccSet.collectEdgesInScc();

        livenessLTSGenerator(this.action, sccSet);
    }

    private void livenessLTSGenerator(String currAction, SCCSet sccSet) {
        Set<GraphEdge> aTrans;

        // Step 2 [green coloring]: extraction transitions with action 'currAction' and coloring
        // transitions that bring to 'currAction'
        aTrans = actionSearcher(currAction, fullLTS.getInitialNode());
        greenColorer(aTrans);

        // Step 3 [red coloring]: coloring transitions that bring to final states
        // and retrieves final states
        Set<GraphNode> finalStates = redColorer(currAction);

        // Step 4 [black coloring]: color transitions to final states and SCC in black
        blackColorer(finalStates, sccSet, currAction);
    }

    /**
     * OLD VERSION, REMOVE IT
     * Retrieves all the transitions that match the given action.
     *
     * @param action the action contained in the property or passed via command line
     * @return aTrans set of transitions that match the action
     */
    /*
    private Set<GraphEdge> getsAllATrans(String action) {
        Set<GraphEdge> aTrans = new HashSet<>();
        int count = 0;
        for (GraphEdge edge : fullLTS.getEdges()) {
            if (edge.getAction().equals(action)) {
                aTrans.add(edge);
                count++;
            }
        }
        System.out.println("getAllATrans: number of transitions that match action " + action + " :"
                + count);

        // coloring in green the inevitable actions
        aTrans.parallelStream().forEach(GraphEdge::setAsCorrect);

        return aTrans;
    }*/

    /**
     * DFS of actions. Retrieves all the transitions that match the given action. It stops at the
     * first instances of the action founded in a given path, meaning that it avoid successive
     * instances.
     * TODO: what if it founds an action in an SCC?
     *
     * @param action      is the searched action
     * @param initialNode initial node is provided to make it usable also with sub-graphs
     * @return the set of transitions that match the action
     */
    private Set<GraphEdge> actionSearcher(String action, GraphNode initialNode) {
        Set<GraphEdge> aTrans = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();
        Set<GraphEdge> discovered = new HashSet<>();
        GraphEdge edge;

        fullLTS.getOutEdges(initialNode).parallelStream().forEach(todo::push); // init
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
     */
    private void greenColorer(Set<GraphEdge> aTrans) {
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
                if (green.add(predEdge)) {
                    todo.push(predEdge);
                }
            });
        }

        green.parallelStream().forEach(GraphEdge::setAsCorrect);
    }

    /**
     * Color in green all the successors transitions of the trans with the inevitable action
     * (OLD: method is not used anymore)
     *
     * @param aTrans the set containing the inevitable actions
     */
    /*
    private void successorColorer(Set<GraphEdge> aTrans) {
        Set<GraphEdge> toColor = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();
        GraphEdge edge;

        for (GraphEdge e : aTrans) {
            todo.push(e);
        }

        while (!todo.isEmpty()) {
            edge = todo.pop();
            for (GraphEdge succ : fullLTS.getOutEdges(fullLTS.getDest(edge))) {
                if (toColor.add(succ)) {
                    todo.push(succ);
                }
            }
        }

        // coloring transitions
        toColor.parallelStream().forEach(GraphEdge::setAsCorrect);
    }*/

    /**
     * OLD: method not used anymore
     * Colours tha paths that lead to the scc. Coloring table:
     * unset -> red
     * black -> black
     * red -> red
     * green -> black
     * // TODO: this is not an optimal solution, since some transitions are traversed more times
     * // TODO (continues): we should color the root of the SCC
     * // TODO: maybe the previous todo is not true... with the Set transitions are collected one
     * time only
     *
     * @param nodesInScc nodes in the SCC
     */
    /*
    private void redOrBlackColorer(Set<GraphNode> nodesInScc) {
        Set<GraphEdge> toColor = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();

        for (GraphNode n : nodesInScc) {
            for (GraphEdge e : fullLTS.getInEdges(n))
                todo.push(e);
        }

        while (!todo.isEmpty()) {
            GraphEdge e = todo.pop();
            for (GraphEdge predEdge : fullLTS.getInEdges(e.source)) {
                if (toColor.add(predEdge)) {
                    todo.push(predEdge);
                }

            }
        }

        for (GraphEdge e : toColor) {
            if (e.isUnset())
                e.setAsIncorrect();
            else if (e.isCorrect())
                e.setAsNeutral();
            else
                ; //TODO: handle this, should not be possible (see table above in comments)
        }
    }*/

    /**
     * OLD: method not used anymore
     * Colour the UNSET transition in RED
     */
    /*
    private void unsetToRed() {
        fullLTS.getEdges().parallelStream().forEach(edge -> {
            if (edge.isUnset()) {
                edge.setAsIncorrect();
            }
        });
    }*/

    /**
     * Color in RED the states that are not set and that are not preceded by the action contained
     * in the Inevitability statement (in this way it colours also SCC, that are before the
     * inevitability, in RED)
     *
     * @return the set of the final nodes
     */
    private Set<GraphNode> redColorer(String action) {
        Stack<GraphNode> todo = new Stack<>();
        Set<GraphEdge> toColor = new HashSet<>();
        Set<GraphEdge> notToColor = new HashSet<>();
        Set<GraphNode> finalStates = new HashSet<>();
        Collection<GraphEdge> outEdges;
        GraphNode state;

        // init
        todo.push(fullLTS.getInitialNode());

        while (!todo.isEmpty()) {
            state = todo.pop();
            outEdges = fullLTS.getOutEdges(state);
            if (outEdges.isEmpty()) // no successors, state is a final state
                finalStates.add(state);
            else
                for (GraphEdge edge : outEdges) {
                    if (edge.isUnset() && toColor.add(edge))
                        todo.push(fullLTS.getDest(edge));
                    else if (!edge.getAction().equals(action) && notToColor.add(edge))
                        // if clause needed to traverse edges that are coloured in green from step 1
                        // (path coloured in green can share prefixes with paths that lead to
                        // final states)
                        todo.push(fullLTS.getDest(edge));
                }
        }

        // coloring in red
        toColor.parallelStream().forEach(GraphEdge::setAsIncorrect);

        return finalStates;
    }

    /**
     * Colours in black:
     * 1) transitions in path to final states that are green
     * 2) transitions in SCC and in paths that bring to SCC
     *
     * @param finalStates set of final states
     * @param sccSet      set of scc
     */
    private void blackColorer(Set<GraphNode> finalStates, SCCSet sccSet, String currentAction) {
        Set<GraphEdge> toColor = new HashSet<>();
        Set<GraphEdge> notToColor = new HashSet<>();
        Stack<GraphEdge> todo = new Stack<>();

        // init coloring from finalStates
        for (GraphNode n : finalStates) {
            fullLTS.getInEdges(n).stream().forEach(e -> {
                todo.push(e);
                if (e.isCorrect())
                    // this should not be possible, since they should all be RED
                    // TODO: add an assertion exception
                    toColor.add(e);
                if (e.isIncorrect())
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
                    if (predEdge.isCorrect() && toColor.add(predEdge))
                        todo.push(predEdge);
                    else if (predEdge.isIncorrect() && notToColor.add(predEdge))
                        todo.push(predEdge);
                }
            });
        }
        toColor.parallelStream().forEach(GraphEdge::setAsNeutral);

        // colouring the SCCs and paths to SCCs
        Set<GraphEdge> sccToColor = new HashSet<>();
        Set<GraphEdge> sccNotToColor = new HashSet<>();
        Stack<GraphEdge> sccTodo = new Stack<>();

        // iterating over all the SCCs
        for (SCC scc : sccSet.getSCCs()) {

            if (!scc.isUnset() && !scc.containsAction(currentAction)) {
                // if clause avoids colouring SCC containing UNSET paths
                sccTodo.addAll(scc.getEdges());
                while (!sccTodo.isEmpty()) {
                    GraphEdge e = sccTodo.pop();
                    // getInEdges method allows to exit from the SCC and colour the paths that
                    // bring to the scc
                    fullLTS.getInEdges(fullLTS.getSource(e)).parallelStream().forEach(predEdge -> {
                        if (predEdge.isCorrect() && sccToColor.add(predEdge))
                            sccTodo.push(predEdge);
                        else if (!predEdge.isUnset() && sccNotToColor.add(predEdge))
                            // if clause allows to avoid the SCCs contained after the Inevitability
                            // TODO: see comment above, maybe it is not true, so following part
                            // can be removed
                            sccTodo.push(predEdge);
                    });
                }
            }
        }
        sccToColor.parallelStream().forEach(GraphEdge::setAsNeutral);
    }
}
