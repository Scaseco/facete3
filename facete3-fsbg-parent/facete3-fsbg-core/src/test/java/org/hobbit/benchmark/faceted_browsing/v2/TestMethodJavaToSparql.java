package org.hobbit.benchmark.faceted_browsing.v2;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

public class TestMethodJavaToSparql {

	public static void jts() throws NoSuchMethodException, SecurityException {

		Method m = TestMethodJavaToSparql.class.getMethod("jts");
	
		
		
		m.getGenericReturnType();
		m.getGenericParameterTypes();
		
	}
	
	public static RDFDatatype typeToJena(Type type) {
		TypeMapper tm = TypeMapper.getInstance();
		RDFDatatype t = null;
		if(type instanceof Class) {
			Class<?> cls = (Class<?>)type;
			t = tm.getTypeByClass(cls);
		}
		
		return t;
	}
	
	
}

class FunctionWrapper
	extends FunctionBase
{

	@Override
	public NodeValue exec(List<NodeValue> args) {
		for(NodeValue arg : args) {
			Node n = arg.asNode();
			
		}
	
		return null;
	}

	@Override
	public void checkBuild(String uri, ExprList args) {
		// TODO Auto-generated method stub
		
	}
}


