package com.eccenca.access_control.triple_based.core;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathRewriter;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.ValueSetOld;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformApplyElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;


public class ElementTransformTripleRewrite
	extends ElementTransformTripleBasedRewrite
{

	@Override
	public Element applyTriplePathTransform(TriplePath tp) {
		Path basePath = tp.getPath();
		Path effectivePath = pathRewriter.apply(basePath);

		Path foldPath = PathUtils.foldNulls(effectivePath);
		
		//System.out.println(effectivePath + " -> " + finalPath);
		
		Path finalPath = PathUtils.isNull(foldPath)
				? PathFactory.pathLink(NodeUtils.nullUriNode)
				: foldPath;
		
		Element result = ElementUtils.createElementPath(tp.getSubject(), finalPath, tp.getObject());

		// Inject a FILTER(FALSE)
		if(PathUtils.isNull(foldPath)) {
			result = ElementUtils.groupIfNeeded(result, new ElementFilter(NodeValue.FALSE));
		}

		return result;
	}

    public Element applyTripleTransform(Triple t) {
        Element result = ElementTransformTripleRewrite.applyTransform(t, genericLayer.getRelation().toTernaryRelation(), null, varGen);
        return result;
    }

    protected GenericLayer genericLayer;
    protected Generator<Var> varGen;


    protected transient	PathRewriter pathRewriter;

    public ElementTransformTripleRewrite(GenericLayer genericLayer) {
        this(genericLayer, VarGeneratorImpl2.create("v"));
    }

    public ElementTransformTripleRewrite(GenericLayer genericLayer, Generator<Var> varGen) {
        super();
        this.genericLayer = genericLayer;    	
    	this.varGen = varGen;
    }

    /**
     *
     * Returns null if no transformation needed to be applied
     *
     * @param triple
     * @param filter
     * @param varGen
     * @return
     */
    public static Element applyTransform(Triple triple, TernaryRelation filter, ValueSetOld<Binding> valueSet, Generator<Var> varGen) {
    	
    	Map<Var, Node> map = new HashMap<>();
    	map.put(filter.getS(), triple.getSubject());
    	map.put(filter.getP(), triple.getPredicate());
    	map.put(filter.getO(), triple.getObject());
    	//Relation transformed = filter.applyNodeTransform(new NodeTransformSubst(map));

    	NodeTransform nodeTransform = new NodeTransformSubst(map);
    	Element e = filter.getElement();
    	Element result = ElementUtils.applyNodeTransform(e, nodeTransform);

    	return result;
    }
    
    
    public static Query transform(Query query, GenericLayer conceptLayer, boolean cloneOnChange) {
        Element oldQueryPattern = query.getQueryPattern();
        Element newQueryPattern = transform(oldQueryPattern, conceptLayer);

        Query result;
        if(oldQueryPattern == newQueryPattern) {
            result = query;
        } else {
            result = cloneOnChange ? query.cloneQuery() : query;
            result.setQueryPattern(newQueryPattern);
        }

        return result;
    }

    public static Element transform(Element element, GenericLayer conceptLayer) { //ValueSet<Node> valueSet) {    	
    	ElementTransformTripleRewrite elementTransform = new ElementTransformTripleRewrite(conceptLayer);
        Element result = ElementTransformer.transform(element, elementTransform, new ExprTransformApplyElementTransform(elementTransform));
        
        ElementTransform t2 = new ElementTransformCleanGroupsOfOne();
        result = ElementTransformer.transform(result, t2, new ExprTransformApplyElementTransform(t2));
        return result;
    }
}
