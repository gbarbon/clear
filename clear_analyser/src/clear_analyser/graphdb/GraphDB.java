package clear_analyser.graphdb;

import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.Map;

/**
 * Created by gbarbon.
 */
public class GraphDB {
    private Node initialNode;
    private GraphDatabaseService graphDB;

    public GraphDB(){}

    public GraphDatabaseService getGraphDB() { return graphDB; }
    public Node getInitialNode() { return initialNode; }

    public interface NodeProperties {
        String ID = "id";  // (int) id of the node
        String EQUIV = "equivalentInSpec"; // (int) id of the equivalent in the Full TLS // TODO: the sink does not use it!
        String ISFRONTIER = "isFrontier"; // (boolean) if it is a neighbourhood
    }

    public interface NeighbourhoodProp {
        String NB_ID = "nb_id"; // neighbourhood ID
        String NB_TYPE = "nb_type";
    }

    public enum NodeLabels implements Label {
        STATE,  // every node is a state, so every node has label STATE
        INITIAL_NODE, // only one node is an initial node
        NEIGHBOURHOOD,  // a subset of nodes have a neighbourhood
        // (the ones in the frontier)
        IS_SINK, // used only to identify the sink
        IS_FINAL // used for the final node
        }

    // Old implementation of relationship
    /*
    public enum RelTypes implements RelationshipType {
        HAS_SUCCESSOR
        // Notice that the relation is between nodes (states).
        // Actions are a property of the relation.
    }*/

    public interface TransProperties {
        String ID = "id";  // (int) id of the node
        String ACTION = "action";  // the action name is redundant
        String TYPE = "type";

        /*
        interface Type {
            String GREEN = GraphEdge.TransitionType.GREEN.toString();
            String RED = GraphEdge.TransitionType.RED.toString();
            String BLACK = GraphEdge.TransitionType.BLACK.toString();
            String UNSET = GraphEdge.TransitionType.UNSET.toString();
        }*/
    }

    public void executor(LTS lts, String baseDir) {
        long startTime = System.currentTimeMillis();
        GraphDatabaseService graphDB = graphDBCreator(baseDir);
        this.graphDB = graphDB;
        graphDBCleaner(graphDB);  // testing purpose, otherwise we have duplicate node at each
        // execution of the tool
        //nodesLoader(lts, graphDB);
        newNodesLoader(lts, graphDB);
        //transitionLoader(lts, graphDB);
        newTransitionLoader(lts, graphDB);
        if (lts.getSink()!=null)  // fix in case there is no sink
            setSink(lts, graphDB);
        setFinalNode(graphDB);
        //nodesPrinter(graphDB);
        long endTime =  System.currentTimeMillis();
        System.out.println("Graph creation total exec time: "+ (endTime-startTime) + " ms\n");

    }

    /**
     * Loads nodes from the LTS to the graph database
     * @param lts the counterexample LTS instance
     * @param graphDB the graph database instance
     */
    private void nodesLoader(LTS lts, GraphDatabaseService graphDB) {
        Node node;
        int index = 0;
        long initTime = System.currentTimeMillis();
        long internalStartTime = initTime;
        try (Transaction tx=graphDB.beginTx()) {
            long startTime = System.currentTimeMillis();
            for (GraphNode ltsNode:lts.getVertices()) {
                //System.out.println("loading"+ltsNode.toStringShort());
                node = graphDB.createNode(NodeLabels.STATE);
                if (lts.getInitialNode().equals(ltsNode)) { // setting initial node
                    initialNode = node;
                    node.addLabel(NodeLabels.INITIAL_NODE);
                }
                node.setProperty(NodeProperties.ID, ltsNode.getId());
                //node.setProperty(NodeProperties.EQUIV, ltsNode.getEquivalentInSpec().getId());
                node.setProperty(NodeProperties.ISFRONTIER, ltsNode.isFrontier());
                if (ltsNode.isFrontier()) {
                    node.addLabel(NodeLabels.NEIGHBOURHOOD);
                    node.setProperty(NeighbourhoodProp.NB_ID, ltsNode.getNeighbourhood().getId());
                    node.setProperty(NeighbourhoodProp.NB_TYPE, ltsNode.getNeighbourhood().getType().toString());
                }
                //long internalEndTime = System.currentTimeMillis();
                //System.out.println("GraphDB.nodesLoader internal time: "+
                //        (internalEndTime-internalStartTime) + " ms");
                index++;
                if ((index % 1000) == 0 ) {
                    long internalEndTime = System.currentTimeMillis();
                    System.out.println("GraphDB.nodesLoader internal time for 1000 iterations " +
                            "after "+ index + " total iterations is: "+
                            (internalEndTime-internalStartTime) + " ms. Time from init of " +
                            "nodesLoader is " + (internalEndTime-initTime) + " ms");
                    internalStartTime = System.currentTimeMillis();
                }
/*                if (index == 300.000)  {
                    System.out.println("Sleeping");
                    Thread.sleep(5000);
                }*/

            }

            tx.success();
            long endTime =  System.currentTimeMillis();
            System.out.println("GraphDB.nodesLoader exec time: "+ (endTime-startTime) + " ms");
        }  // TODO: add catch
/*        catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * New version with batch loading
     * Loads nodes from the LTS to the graph database
     * @param lts the counterexample LTS instance
     * @param graphDB the graph database instance
     */
    private void newNodesLoader(LTS lts, GraphDatabaseService graphDB) {
        long startTime = System.currentTimeMillis();
        int batchSize = 10000;
        BatchTransactionExecutor batchExecutor =
                new IterableInputBatchTransactionExecutor(graphDB,
                batchSize, lts.getVertices(), new CreateGraphNode(lts));
        batchExecutor.execute();
        long endTime =  System.currentTimeMillis();
        System.out.println("GraphDB.newNodesLoader exec time: "+ (endTime-startTime) + " ms");
    }

/*    private static void neighbourhoodLoader(Neighbourhood nb, GraphDatabaseService graphDB) {
        Node node;
        try (Transaction tx=graphDB.beginTx()) {

            tx.success();
        }  // TODO: add catch
    }

    *//**
     *
     * @param neighbourhoods
     * @param graphDB
     *//*
    private static void neighbourhoodLoader(Set<Neighbourhood> neighbourhoods, GraphDatabaseService graphDB) {
        try (Transaction tx=graphDB.beginTx()) {
            tx.success();
        }
    }*/

    /**
     *  set the sink
     * @param lts
     * @param graphDB
     */
    private static void setSink(LTS lts, GraphDatabaseService graphDB) {
        long startTime = System.currentTimeMillis();
        int sinkId = lts.getSink().getId();
        String sinkSetQuery = "MATCH (sink) \n"+
                "WHERE sink.id="+sinkId + "\n"+
                "SET sink :"+ NodeLabels.IS_SINK.toString() + "\n"+
                "RETURN sink.id";
        String rows="";
        try (Transaction tx=graphDB.beginTx();
             Result result = graphDB.execute(sinkSetQuery)) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    rows += column.getKey() + ": " + column.getValue() + "; ";
                }
                rows += "\n";
            }
            tx.success();
        }
        //System.out.println("The sink is: "+rows);
        long endTime =  System.currentTimeMillis();
        System.out.println("GraphDB.setSink exec time: "+ (endTime-startTime) + " ms");
    }

    /**
     *
     * @param graphDB
     */
    private static void setFinalNode(GraphDatabaseService graphDB) {
        long startTime = System.currentTimeMillis();
        String query =
                "MATCH (node)\n" +
                        "WHERE NOT (node)-[]->()" +
                        " AND NOT node:"+ GraphDB.NodeLabels.IS_SINK+"\n"+
                        "SET node :"+ NodeLabels.IS_FINAL.toString() + "\n"+
                        "RETURN node."+GraphDB.NodeProperties.ID;;
        String rows="";
        try (Transaction tx=graphDB.beginTx();
             Result result = graphDB.execute(query)) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    rows += column.getKey() + ": " + column.getValue() + "; ";
                }
                rows += "\n";
            }
            tx.success();
        }
        //System.out.println("Printing final node: "+rows);
        long endTime =  System.currentTimeMillis();
        System.out.println("GraphDB.setFinalNode exec time: "+ (endTime-startTime) + " ms");
    }

    /**
     *
     * @param lts
     * @param graphDB
     */
    private static void transitionLoader(LTS lts, GraphDatabaseService graphDB) {
        int index = 0;
        long initTime = System.currentTimeMillis();
        long internalStartTime = initTime;
        long startTime = System.currentTimeMillis();
        Relationship rel;
        GraphNode source, dest;
        Node sourceDBNode, destDBNode;
        try (Transaction tx=graphDB.beginTx()) {
            for (GraphEdge edge : lts.getEdges()) {
                //System.out.println("Loading "+lts.getSource(edge).toStringShort()+" : "+edge.getAction()+ " : "+lts.getDest(edge).toStringShort());
                source = lts.getSource(edge);
                sourceDBNode = graphDB.findNode(NodeLabels.STATE, NodeProperties.ID, source.getId());
                dest = lts.getDest(edge);
                destDBNode = graphDB.findNode(NodeLabels.STATE, NodeProperties.ID, dest.getId());
                // old relationship implementation below:
                // rel = sourceDBNode.createRelationshipTo(destDBNode, RelTypes.HAS_SUCCESSOR);
                rel = sourceDBNode.createRelationshipTo(destDBNode, RelationshipType.withName
                        (edge.getAction()));
                rel.setProperty(TransProperties.ID, edge.getId());
                rel.setProperty(TransProperties.ACTION, edge.getAction());  // redundant
                rel.setProperty(TransProperties.TYPE, edge.getType().toString());
                index++;
                if ((index % 10) == 0 ) {
                    long internalEndTime = System.currentTimeMillis();
                    System.out.println("GraphDB.transitionLoader internal time for 1000 iterations " +
                            "after "+ index + " total iterations is: "+
                            (internalEndTime-internalStartTime) + " ms. Time from init of " +
                            "transitionLoader is " + (internalEndTime-initTime) + " ms");
                    internalStartTime = System.currentTimeMillis();
                }
            }
            tx.success();
        }
        long endTime =  System.currentTimeMillis();
        System.out.println("GraphDB.transitionLoader exec time: "+ (endTime-startTime) + " ms");
    }

    /**
     * New version with batch loading
     * @param lts
     * @param graphDB
     */
    private static void newTransitionLoader(LTS lts, GraphDatabaseService graphDB) {
        long startTime = System.currentTimeMillis();
        int batchSize = 10000;
        BatchTransactionExecutor batchExecutor =
                new IterableInputBatchTransactionExecutor(graphDB,
                        batchSize, lts.getEdges(), new CreateGraphEdge(lts));
        batchExecutor.execute();
        long endTime =  System.currentTimeMillis();
        System.out.println("GraphDB.newTransitionLoader exec time: "+ (endTime-startTime) + " ms");
    }

    /**
     * removes all the tranistions and nodes from the database
     * @param graphDB
     */
    private static void graphDBCleaner(GraphDatabaseService graphDB) {
        try (Transaction tx=graphDB.beginTx()) {
            long startTime =  System.currentTimeMillis();
            for(Relationship rel: graphDB.getAllRelationships()) {
                rel.delete();
            }
            for(Node node : graphDB.getAllNodes()) {
                node.delete();
            }
            tx.success();
            long endTime =  System.currentTimeMillis();
            System.out.println("GraphDB.graphDBCleaner exec time: "+ (endTime-startTime) + " ms");
        }
    }

    /**
     * Prints the nodes in the graph database
     * @param graphDB the graph database instance
     */
    private static void nodesPrinter(GraphDatabaseService graphDB) {
        try (Transaction tx=graphDB.beginTx()) {
            System.out.println("Starting printing nodes");
            for (Node node : graphDB.getAllNodes()) {
                String is="";
                if ( (boolean) node.getProperty(NodeProperties.ISFRONTIER))
                    is = "IS";
                else
                    is= "IS NOT";
                System.out.println("Node "+ node.getProperty(NodeProperties.ID)+ " " + is + " frontier" );
            }
            tx.success();
        }  // TODO: add catch
    }

    /**
     * Create (or open) a graphdatabase instance
     * @return a graph database instance
     */
    private static GraphDatabaseService graphDBCreator(String baseDir){
        long startTime = System.currentTimeMillis();
        File file = new File(baseDir + "/databases/graph.db/");
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase( file ); // creation of the database instance
        registerShutdownHook( graphDB );
        long endTime =  System.currentTimeMillis();
        System.out.println("GraphDB.graphDBCreator exec time: "+ (endTime-startTime) + " ms");
        return graphDB;
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb ) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
}
