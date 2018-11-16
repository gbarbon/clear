package clear_analyser.graphdb;

import org.neo4j.graphdb.*;

import java.util.Map;


/**
 * Created by gbarbon.
 */
public class Heuristics {

    /*
    public static void tester(GraphDatabaseService graphDB, Node node) {
        try (Transaction tx = graphDB.beginTx()) {


            TraversalDescription testTraversal = graphDB.traversalDescription()
                    .depthFirst()
                    .relationships(GraphDB.RelTypes.HAS_SUCCESSOR)
                    .evaluator(Evaluators.atDepth(1));

            Traverser traverser = testTraversal.traverse(node);

            for (Node node1 : traverser.nodes()) {
                System.out.println("" + node1.getProperty(GraphDB.NodeProperties.ID));
            }

            tx.success();
        }
    }*/

    /**
     * Query executor
     * @param query
     * @param graphDB
     * @return
     */
    private static String queryExecutor(String query, GraphDatabaseService graphDB) {
        String res = "";
        try (Transaction tx = graphDB.beginTx();
             Result result = graphDB.execute(query)) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    res += column.getKey() + ": " + column.getValue() + "; ";
                }
                res += "\n";
            }
            tx.success();
        }
        return res;
    }

    /**
     * Simple Query: Prints all neighbourhoods
     *
     * @param graphDB
     */
    public static void allNeighbourhood(GraphDatabaseService graphDB) {
        String query =
                "MATCH (nb: " + GraphDB.NodeLabels.NEIGHBOURHOOD.toString() + ")" +
                        "RETURN nb." + GraphDB.NodeProperties.ID;
        System.out.println("allNeighbourhood res: " + queryExecutor(query, graphDB));
    }

    /**
     * Simple Query: Prints all neighbourhood, alternative version
     *
     * @param graphDB
     */
    public static void allNeighbourhoodWhereClause(GraphDatabaseService graphDB) {
        String query =
                "MATCH (node)" +
                        "WHERE node:" + GraphDB.NodeLabels.NEIGHBOURHOOD.toString() + "\n" +
                        "RETURN node." + GraphDB.NodeProperties.ID;
        System.out.println("allNeighbourhoodWhereClause res: " + queryExecutor(query, graphDB));
    }

    /**
     * Simple Query: Path with neighbourhoods
     *
     * @param graphDB
     */
    public static void pathWNeighbourhoods(GraphDatabaseService graphDB) {
        String query =
                "MATCH path=(init:" + GraphDB.NodeLabels.INITIAL_NODE.toString()
                        + ")-[*]->(end:" + GraphDB.NodeLabels.IS_FINAL.toString()
                        + ")\n" +
                        "RETURN path";
        System.out.println("pathWNeighbourhoods res: " + queryExecutor(query, graphDB));
    }

    /**
     * Simple Query: Returns the initial node
     *
     * @param graphDB
     */
    public static void initialNode(GraphDatabaseService graphDB) {
        String query =
                "MATCH (init:" + GraphDB.NodeLabels.INITIAL_NODE.toString() + ")\n" +
                        "RETURN init." + GraphDB.NodeProperties.ID;
        System.out.println("Printing initial node: " + queryExecutor(query, graphDB));
    }

    /**
     * Simple Query: Returns the final node
     *
     * @param graphDB
     */
    public static void finalNode(GraphDatabaseService graphDB) {
        String query =
                "MATCH (end:" + GraphDB.NodeLabels.IS_FINAL.toString() + ")\n" +
                        "RETURN end." + GraphDB.NodeProperties.ID;
        System.out.println("Printing final node: " + queryExecutor(query, graphDB));
    }

    /*
     * *****************************
     * *** Here heuristics start ***
     * *****************************
    */

    /**
     * Heuristic (independent of the example):
     * the shortest path (sequence of actions) from the initial node to a neighbourhood
     * TODO: not optimal, improve
     * @param graphDB
     */
    public static void shortestPathInitNb(GraphDatabaseService graphDB) {
        // from the initial node (label INITIAL_NODE) find the closest neighbourhood (label
        // NEIGHBOURHOOD) then get the path between the two
        String query2 = "MATCH (init:INITIAL_NODE), (nb:NEIGHBOURHOOD), "+
                "path = shortestpath((init)-[*]->(nb))"+
                "RETURN path ORDER BY length(path) LIMIT 1";
        long startTime = System.currentTimeMillis();
        String queryRes = queryExecutor(query2, graphDB);
        System.out.println("Heuristics.shortestPathInitNb exec time: "+ (System.currentTimeMillis()-
                startTime) + " ms");
        System.out.println("Heuristics.shortestPathInitNb res: " + queryRes);
    }

    /**
     * Heuristic (independent of the example):
     * Shortest counterexample (path from initial to final node)
     * TODO: not optimal, improve
     * TODO: it is also returning loops (check and improve if true)
     * @param graphDB
     */
    public static void shortestPathInitFinal(GraphDatabaseService graphDB) {
        String query2 = "MATCH (init:INITIAL_NODE), (final:IS_FINAL), \n" +
                "path = shortestpath((init)-[*]->(final)) \n" +
                "RETURN path ORDER BY length(path) LIMIT 1";
        long startTime = System.currentTimeMillis();
        String queryRes = queryExecutor(query2, graphDB);
        System.out.println("Heuristics.shortestPathInitFinal exec time: "+ (System.currentTimeMillis()-
                startTime) + " ms");
        System.out.println("Heuristics.shortestPathInitFinal res: " + queryRes);
    }

    /**
     * Heuristic (independent of the example):
     * the abstract counterexample with the highest number of neighbourhoods  (but without loops)
     *
     * TODO: test if it is working also with loops
     *
     * @param graphDB
     */
    public static void highestNOfNeighbourhoods(GraphDatabaseService graphDB) {
        String query =
                "MATCH path=(init:" + GraphDB.NodeLabels.INITIAL_NODE.toString() + ")-[*]->(end:"
                        + GraphDB.NodeLabels.IS_FINAL.toString() + ")\n" +
                        "WITH FILTER(node IN nodes(path) WHERE node:"
                        + GraphDB.NodeLabels.NEIGHBOURHOOD.toString()
                        + ") AS pathS\n" +
                        "WITH pathS, length(pathS) AS size\n" +
                        "RETURN pathS, size ORDER BY size DESC LIMIT 1";
        System.out.println("highestNOfNeighbourhoods res: " + queryExecutor(query, graphDB));
    }

    /**
     * Heuristic (independent of the example):
     * The abstract counterexample with the smaller number of neighbourhoods
     *
     * Retrieve the abstracted counterexample with the lowest number of neighbourhood
     * FIXME: notice that it retrieves only the first result,
     * thus we can have other abstracted counterexample with the same size
     * which have different neighbourhoods.
     *
     * @param graphDB
     */
    public static void lowestNOfNeighbourhoods(GraphDatabaseService graphDB) {
        String query_v1 = // not correct
                "MATCH path=(init:" + GraphDB.NodeLabels.INITIAL_NODE.toString() + ")-[*]->(end:" +
                        GraphDB.NodeLabels.IS_FINAL.toString() + ")\n" +
                        "WITH nodes(path) AS nodes\n" +
                        "UNWIND nodes AS node\n" +
                        "WITH node\n" +
                        "WHERE node:" + GraphDB.NodeLabels.NEIGHBOURHOOD.toString() + ")\n" +
                        "WHERE min(size(path))\n" +
                        "RETURN node." + GraphDB.NodeProperties.ID;
        String query_v2 = // not correct
                "MATCH path=(init:" + GraphDB.NodeLabels.INITIAL_NODE.toString() +
                        ")-[*]->(end:" + GraphDB.NodeLabels.IS_FINAL.toString() + ")\n" +
                        "WHERE ANY (node IN nodes(path) WHERE node:"
                        + GraphDB.NodeLabels.NEIGHBOURHOOD.toString() + ")\n" +
                        //"RETURN path";
                        //"MATCH (n:"+GraphDB.NodeLabels.NEIGHBOURHOOD.toString()+")\n"+
                        //"RETURN n."+GraphDB.NodeProperties.ID;
                        "RETURN ";
        String query_v3 = //with this query results are not correct, list of shortest paths
                "MATCH path=(init:" + GraphDB.NodeLabels.INITIAL_NODE.toString() + ")-[*]->(end:"
                        + GraphDB.NodeLabels.IS_FINAL.toString() + ")\n" +
                        "WITH FILTER(node IN nodes(path) WHERE node:"
                        + GraphDB.NodeLabels.NEIGHBOURHOOD.toString() + ") AS pathS\n" +
                        "WITH pathS, min(size(pathS)) AS minSize\n" +
                        "WHERE size(pathS)=minSize\n" +
                        "RETURN pathS" + ", minSize";
        String query_v4 =  // Vincent version
                "MATCH path=(init:" + GraphDB.NodeLabels.INITIAL_NODE.toString() + ")-[*]->(end:"
                        + GraphDB.NodeLabels.IS_FINAL.toString() + ")\n" +
                        "WITH FILTER(node IN nodes(path) WHERE node:"
                        + GraphDB.NodeLabels.NEIGHBOURHOOD.toString()
                        + ") AS pathS\n" +
                        "WITH pathS, length(pathS) AS size\n" +
                        "RETURN pathS, size ORDER BY size LIMIT 1"; // the first one with
        // shortest length
        System.out.println("lowestNOfNeighbourhoodsVincent res: " + queryExecutor(query_v4,
                graphDB));
    }

    /**
     * Heuristic (dependent of the example):
     * the shortest abstracted counterexample that pass through a given sequence of
     * actions (given by the user). Three possible options:
     *  - a single action
     *  - contiguous actions
     *  - non-contiguous actions
     *
     *  Note: the given action (or sequence of action) must belong to the non-abstracted
     *  counterexample, but could not appear in the abstracted counterexample. This means that we
     *  must search the actions in non abstracted paths, and only after return the corresponding
     *  abstracted counterexample.
     *  Note: this is not the shortest counterexample that we return with the old procedure,
     *  since this is the shortest one that pass through some given actions
     */
    public void abstractedWithSequence(String[] actions, GraphDatabaseService graphDB) {
        //TODO: implement me!
        String query = "";
        System.out.println("lowestNOfNeighbourhoodsVincent res: " + queryExecutor(query, graphDB));
    }



    
}
