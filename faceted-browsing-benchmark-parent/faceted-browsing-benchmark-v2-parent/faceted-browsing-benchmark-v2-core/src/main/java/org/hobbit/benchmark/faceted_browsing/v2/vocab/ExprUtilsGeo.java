package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import java.util.Arrays;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;

public class ExprUtilsGeo {
    /**
     * @param varX The SPARQL variable that corresponds to the longitude
     * @param varY The SPARQL variable that corresponds to the longitude
     * @param bounds The bounding box to use for filtering
     * @param castNode An optional SPAQRL node used for casting, e.g. xsd.xdouble
     */
    public static Expr createExprWgs84Intersects(Var varX, Var varY, Envelope env, Node castNode) {
        Expr lon = new ExprVar(varX);
        Expr lat = new ExprVar(varY);

        // Cast the variables if requested
        // Using E_Function(castNode.getUri(), lon) - i.e. the cast type equals the cast function name
        if(castNode != null) {
            String fnName = castNode.getURI();
            lon = new E_Function(fnName, new ExprList(lon));
            lat = new E_Function(fnName, new ExprList(lat));
        }

        Expr minX = NodeValue.makeDecimal(env.getMinX());
        Expr maxX = NodeValue.makeDecimal(env.getMaxX());
        Expr minY = NodeValue.makeDecimal(env.getMinY());
        Expr maxY = NodeValue.makeDecimal(env.getMaxY());

        Expr result = new E_LogicalAnd(
            new E_LogicalAnd(new E_GreaterThan(lon, minX), new E_LessThan(lon, maxX)),
            new E_LogicalAnd(new E_GreaterThan(lat, minY), new E_LessThan(lat, maxY))
        );

        return result;
    }


    public static Expr createExprOgcIntersects(Var v, Envelope env, String intersectsFnName, String datatypeIri) { //String geomFromTextFnName) {
    	String ogc = "http://www.opengis.net/ont/geosparql#";
        datatypeIri = datatypeIri != null ? datatypeIri : ogc + "wktLiteral";
    	
    	WKTWriter writer = new WKTWriter();
        TypeMapper typeMapper = TypeMapper.getInstance();
        RDFDatatype dtype = typeMapper.getSafeTypeByName(datatypeIri);
        

        intersectsFnName = intersectsFnName != null ? intersectsFnName : ogc + "intersects";;
        //geomFromTextFnName = geomFromTextFnName != null ? geomFromTextFnName ? geomFromTextFnName : ogc + "geomFromText";


        GeometryFactory geometryFactory = new GeometryFactory();
        Geometry geom = geometryFactory.toGeometry(env);
        Expr exprVar = new ExprVar(v);
        String wktStr = writer.write(geom); //this.boundsToWkt(bounds);

        // FIXME: Better use typeLit with xsd:string
        //Expr wktNodeValue = NodeValueUtils.makeString(wktStr); //new NodeValue(rdf.NodeFactory.createPlainLiteral(wktStr));
        Node wktNode = NodeFactory.createLiteral(wktStr, dtype);
        Expr wktExpr = NodeValue.makeNode(wktNode);
        
        
        Expr result = new E_Function(
                intersectsFnName,
                new ExprList(Arrays.asList(exprVar, wktExpr))
            //[exprVar, new E_Function(geomFromTextFnName, [wktNodeValue])]
        );

        return result;
    }

    /**
     * Not needed; using JTS WKTWriter instead
     * 
     * Convert a bounds object to a WKT polygon string
     *
     * TODO This method could be moved to a better place
     *
     */
//    boundsToWkt: function(bounds) {
//        var ax = bounds.left;
//        var ay = bounds.bottom;
//        var bx = bounds.right;
//        var by = bounds.top;
//
//        var result = 'POLYGON((' + ax + ' ' + ay + ',' + bx + ' ' + ay
//                + ',' + bx + ' ' + by + ',' + ax + ' ' + by + ',' + ax
//                + ' ' + ay + '))';
//
//        return result;
//    }
}
