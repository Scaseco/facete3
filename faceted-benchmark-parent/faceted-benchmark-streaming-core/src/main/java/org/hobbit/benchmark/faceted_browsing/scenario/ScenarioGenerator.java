package org.hobbit.benchmark.faceted_browsing.scenario;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.sparql.path.Path;

import com.vividsolutions.jts.geom.Envelope;

import io.reactivex.Flowable;

interface MetaController<T> {
	boolean isActive();
	void setActive(boolean status);
}

interface MapController {
	List<MapVocab> listVocabs();
}

interface MapVocab {
	Concept createConstraint(Envelope env);
	
	Flowable<Path> findPaths();
}

interface Session {
	<T> T aspect(Class<T> clazz);
	
	void undo();
}

//interface State {
//	// Reset the session to the prior state
//	void undo();
//}

public class ScenarioGenerator {
	public void generate() {
		
		
	}
	
	
	
	
	/**
	 * Find a map cell containing few enough spatial data.
	 * 
	 * Start with the maximum tile, then zoom in into random tiles containing data
	 * 
	 * TODO Where to get (1) the intitial extent? (2) the set of spatial vocabs to constrain?
	 * 
	 */
	public void findConcreteSpatialData(Session session) {
		// Get the map feature of the session
		MapController controller = session.aspect(MapController.class);
		
		// Get its supported vocabularies (usually wgs84 and geosparql)
		List<MapVocab> vocabs = controller.listVocabs();

		
		//controller.setEnvelope();
		//controller.isActive();

		// Check the vocabs whether there are paths connecting
		// the resources (i.e. the concept) of the current state
		// with the spatial entities 
		for(MapVocab vocab : vocabs) {
			Envelope env = null;
			//Concept c = vocab.createConstraint(env);
			
			// Find paths connecting the current session state with the spatial vocab
			Flowable<Path> paths = vocab.findPaths();
			
			// TODO The result should be some kind of SPath / FacetPath instance
			// i.e. something that allows managing the constraints
			
			
		}		
		
		//session.aspect()
	}
}
