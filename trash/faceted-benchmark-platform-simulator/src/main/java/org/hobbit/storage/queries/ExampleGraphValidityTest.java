/**
 * This file is part of core.
 *
 * core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with core.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hobbit.storage.queries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.hobbit.vocab.HOBBIT;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This simple test iterates over test resources that are used to test the
 * SPARQL queries and makes sure that resources of the HOBBIT vocabulary
 * namespace are part of the {@link HOBBIT} vocabulary class.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
@RunWith(Parameterized.class)
public class ExampleGraphValidityTest {

    protected static Set<String> HOBBIT_VOCABULARY_RESOURCES = loadHobbitVocabResources();

    protected static Set<String> loadHobbitVocabResources() {
        Set<String> resources = new HashSet<String>();
        Field fields[] = HOBBIT.class.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            try {
                if (fields[i].getType().equals(Resource.class)) {
                    Resource r = (Resource) fields[i].get(null);
                    resources.add(r.getURI());
                } else if (fields[i].getType().equals(Property.class)) {
                    Property p = (Property) fields[i].get(null);
                    resources.add(p.getURI());
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return resources;
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        addModelFiles(new File("src/test/resources"), testConfigs);
        return testConfigs;
    }

    protected static void addModelFiles(File file, List<Object[]> modelFiles) {
        if (file.isFile()) {
            if (file.getName().endsWith(".ttl")) {
                modelFiles.add(new Object[] { file.getAbsolutePath().toString() });
            }
        } else if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addModelFiles(f, modelFiles);
            }
        }
    }

    private String modelFile;

    public ExampleGraphValidityTest(String modelFile) {
        this.modelFile = modelFile;
    }

    @Test
    public void test() throws IOException {
        Assert.assertNotNull(modelFile);
        Model model = loadModel(modelFile);

        StmtIterator iterator = model.listStatements();
        Statement s;
        Set<String> wrongUris = new HashSet<>();
        while (iterator.hasNext()) {
            s = iterator.next();
            check(s.getSubject(), wrongUris);
            check(s.getPredicate(), wrongUris);
            check(s.getObject(), wrongUris);
        }
        Assert.assertTrue("The model \"" + modelFile + "\" contains the unknown resources " + wrongUris.toString(),
                wrongUris.size() == 0);
    }

    private void check(RDFNode node, Set<String> wrongUris) {
        if (node.isURIResource()) {
            Resource resource = node.asResource();
            if (resource.getNameSpace().equals(HOBBIT.getURI())
                    && !HOBBIT_VOCABULARY_RESOURCES.contains(resource.getURI())) {
                wrongUris.add(resource.getURI());
            }
        }
    }

    protected static Model loadModel(String modelFile) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = null;
        try {
            is = new FileInputStream(modelFile);
            RDFDataMgr.read(model, is, Lang.TTL);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return model;
    }
}
