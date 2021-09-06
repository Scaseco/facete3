package org.aksw.jena_sparql_api.schema.traversal.relgen;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathOpsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprVar;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

/** */
public abstract class RelationGeneratorBase
//    implements Trav1Provider<Node, RelationBuilder>
{
    protected Relation relation;

    // A hash updated upon requesting a new relation; hash is based on the seen path segments
    // Note the hash is really per relation and not per column.
    // The column names are appended to the hash
    protected HashCode contextHash;

    /** Cached String version of the hash */
    protected String contextHashStr;

    /** Conditions imposed the current relation based on the seen path segments */
    protected List<Expr> conditions = new ArrayList<>();


    /** The absolute path at which the relation was requested
     * Once it covers all columns of the relation it is used to compute the
     * next context hash
     */
    protected Path<Node> relationStartAbsPath;


    /**
     * The relative path of segments seen for the current relation
     * Connects to relationStartPath
     */
    protected Path<Node> relPath;

    /** The path segments seen for the current relation. */
    // protected List<Node> segments = new ArrayList<>();

    int columnIdx = 0;




    /**
     * Yield the next relation to traverse
     *
     * @param path
     * @param index
     * @return
     */
    protected abstract Relation nextInstance();


    public RelationGeneratorBase() {
        super();
        reset();
    }


    protected void setHashCode(HashCode hashCode) {
         contextHash = hashCode;
         contextHashStr = hashCode == null ? null : encodeHashCode(hashCode);
    }

    protected String encodeHashCode(HashCode hashCode) {
        return hashCode.toString(); // BaseEncoding.base64Url().encode(contextHash.asBytes());
    }

    public Relation process(Path<Node> path) {
        if (path.isAbsolute()) {
            reset();
        }

        ensureInit();


        Relation result = relation;

        for (Node segment : path.getSegments()) {
            result = process(segment);
        }

        return result;
    }

    protected void reset() {
        setHashCode(null);
        relation = null;
        columnIdx = 0;
        relationStartAbsPath = PathOpsNode.newAbsolutePath();
        relPath = PathOpsNode.newRelativePath();
        updateHash();
    }

    public void ensureInit() {
        if (relation == null || columnIdx >= relation.getVars().size()) {

            String oldHash = contextHashStr;
            updateHash();


            relation = nextInstance();

            relationStartAbsPath = relationStartAbsPath.resolve(relPath);
            relPath = PathOpsNode.newRelativePath();

            List<Var> vars = relation.getVars();
            if (vars.size() <= 1) {
                throw new RuntimeException("Relations must have at least 2 variables");
            }

            conditions.clear();

            // Rename variables w.r.t the hashes:
            // The first var receives the prior hash (in order to join with the prior relation)
            // all other vars receive the new hash

            Var firstVar = vars.get(0);

            Map<Var, Node> remap = vars.stream()
                    .collect(Collectors.toMap(
                            v -> v,
                            node -> {
                                Node r = null;
                                if (node.isVariable()) {
                                    String prefix = node.equals(firstVar)
                                            ? oldHash
                                            : contextHashStr;
                                    r = Var.alloc(prefix + "_" + node.getName());
                                } else {
                                    r = null;
                                }
                                return r;
                            }));

            relation = relation.applyNodeTransform(remap::get);

            columnIdx = 0;
        }
    }


    protected void updateHash() {
        HashCode nextHashCode = computeNextHash(contextHash, relationStartAbsPath, relPath);
        setHashCode(nextHashCode);
    }


    protected HashCode computeNextHash(HashCode currentHash, Path<Node> relationStartAbsPath, Path<Node> relPath) {
        HashCode contrib = Hashing.murmur3_32().hashString(relPath.toString(), StandardCharsets.UTF_8);

        HashCode result = currentHash == null ? contrib : Hashing.combineOrdered(Arrays.asList(currentHash, contrib));
        return result;
    }

    public Relation process(Node segment) {

        ensureInit();

        relPath = relPath.resolve(segment);


        List<Var> vars = relation.getVars();
        Var v = vars.get(columnIdx);
        ++ columnIdx;

        Expr expr = new E_Equals(new ExprVar(v), ExprLib.nodeToExpr(segment));
        conditions.add(expr);

        Relation r = relation.filter(conditions);

        return r;
    }

}
