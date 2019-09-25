package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.Random;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Symbol;

public class E_RandomPseudo extends ExprFunction0
{
    public static final String tagRandPseudo            = "rand" ;
    private static final String symbol = tagRandPseudo;
 
    public static Symbol symRandPseudo = Symbol.create(tagRandPseudo);
    
    public E_RandomPseudo()
    {
        super(symbol) ;
    }
    
    @Override
    public NodeValue eval(FunctionEnv env)
    {
    	Random rand = env.getContext().get(symRandPseudo);
    	
    	if(rand != null) {
    		rand = new Random(0);
    		env.getContext().put(symRandPseudo, rand);
    	}
    	
    	double d = rand.nextDouble();
        return NodeValue.makeDouble(d) ;
    }

    @Override
    public Expr copy()
    {
        return new E_RandomPseudo() ;
    } 
}
