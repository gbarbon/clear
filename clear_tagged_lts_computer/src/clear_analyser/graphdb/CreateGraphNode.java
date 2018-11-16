package clear_analyser.graphdb;

import com.graphaware.tx.executor.batch.UnitOfWork;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Unit of work that creates an empty node with random name. Singleton.
 */
public class CreateGraphNode implements UnitOfWork<GraphNode> {
    //private static final CreateGraphNode INSTANCE = new CreateGraphNode(this.lts);
    private LTS lts;
    private Node initialNode;

    //public static CreateGraphNode getInstance() {
    //    return INSTANCE;
    //}

    public CreateGraphNode(LTS lts) {
        this.lts = lts;
    }

    @Override
    public void execute(GraphDatabaseService graphDB, GraphNode ltsNode , int batchNumber,
                        int stepNumber) {
        // Node node = database.createNode();
        // node.setProperty("name", UUID.randomUUID());
        Node node = graphDB.createNode(GraphDB.NodeLabels.STATE);
        if (lts.getInitialNode().equals(ltsNode)) { // setting initial node
            initialNode = node;
            node.addLabel(GraphDB.NodeLabels.INITIAL_NODE);
        }
        node.setProperty(GraphDB.NodeProperties.ID, ltsNode.getId());
        //node.setProperty(NodeProperties.EQUIV, ltsNode.getEquivalentInSpec().getId());
        node.setProperty(GraphDB.NodeProperties.ISFRONTIER, ltsNode.isFrontier());
        if (ltsNode.isFrontier()) {
            node.addLabel(GraphDB.NodeLabels.NEIGHBOURHOOD);
            node.setProperty(GraphDB.NeighbourhoodProp.NB_ID, ltsNode.getNeighbourhood().getId());
            node.setProperty(GraphDB.NeighbourhoodProp.NB_TYPE, ltsNode.getNeighbourhood().getType().toString());
        }
    }
}