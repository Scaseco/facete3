package org.aksw.facete3.app.vaadin;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.core.io.ClassPathResource;

/**
 * A global prefix object is created as a bean in the config which can be used
 * with NodeFmtLib.str(node, prefixMap) making this class obsolete.
 *
 * @author raven
 *
 */
@Deprecated
public class TransformService {

    private Model prefixes;

    public TransformService(String prefixFile) {
        //this.prefixes = configPrefixes;
        String path = new ClassPathResource(prefixFile).getPath();
        this.prefixes = RDFDataMgr.loadModel(path);
    }

    public Model getPrefixFile () {
        return this.prefixes;
    }

    public String handleResource(String uri) {
        return prefixes.shortForm(uri);
    }

    public String handleObject(RDFNode node) {
        String printName = node.isResource() ? handleResource(node.toString())
                : node.toString();
        return printName;

    }

}
