package clear_analyser.graphdb;

import com.graphaware.tx.executor.batch.UnitOfWork;
import clear_analyser.graph.GraphEdge;
import clear_analyser.graph.GraphNode;
import clear_analyser.graph.LTS;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * Unit of work that creates an empty node with random name. Singleton.
 */
public class CreateGraphEdge implements UnitOfWork<GraphEdge> {
    //private static final CreateGraphNode INSTANCE = new CreateGraphNode(this.lts);
    private LTS lts;
    private Node initialNode;

    //public static CreateGraphNode getInstance() {
    //    return INSTANCE;
    //}

    public CreateGraphEdge(LTS lts) {
        this.lts = lts;
    }

    @Override
    public void execute(GraphDatabaseService graphDB, GraphEdge edge , int batchNumber,
                        int stepNumber) {
        // Node node = database.createNode();
        // node.setProperty("name", UUID.randomUUID());
        Relationship rel;
        GraphNode source, dest;
        Node sourceDBNode, destDBNode;
        source = lts.getSource(edge);
        sourceDBNode = graphDB.findNode(GraphDB.NodeLabels.STATE, GraphDB.NodeProperties.ID, source.getId());
        dest = lts.getDest(edge);
        destDBNode = graphDB.findNode(GraphDB.NodeLabels.STATE, GraphDB.NodeProperties.ID, dest.getId());
        // old relationship implementation below:
        // rel = sourceDBNode.createRelationshipTo(destDBNode, RelTypes.HAS_SUCCESSOR);
        rel = sourceDBNode.createRelationshipTo(destDBNode, RelationshipType.withName
                (edge.getAction()));
        rel.setProperty(GraphDB.TransProperties.ID, edge.getId());
        rel.setProperty(GraphDB.TransProperties.ACTION, edge.getAction());  // redundant
        rel.setProperty(GraphDB.TransProperties.TYPE, edge.getType().toString());
    }
}