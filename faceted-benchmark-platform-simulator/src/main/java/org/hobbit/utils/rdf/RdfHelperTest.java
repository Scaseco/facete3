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
package org.hobbit.utils.rdf;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

public class RdfHelperTest {

    @Test
    public void testGetLabel() {
        Model model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://example.org/example1"), RDFS.label, "Label 1");
        model.add(model.getResource("http://example.org/example1"), RDFS.comment, "Comment 1");
        model.add(model.getResource("http://example.org/example2"), RDFS.label, "Second label");
        model.add(model.getResource("http://example.org/example3"), RDFS.comment, "Comment for resource 3");

        Assert.assertEquals("Label 1", RdfHelper.getLabel(model, model.getResource("http://example.org/example1")));
        Assert.assertEquals("Second label",
                RdfHelper.getLabel(model, model.getResource("http://example.org/example2")));

        Assert.assertTrue((new HashSet<String>(Arrays.asList("Label 1", "Second label"))
                .contains(RdfHelper.getLabel(model, null))));

        Assert.assertNull(RdfHelper.getLabel(model, model.getResource("http://example.org/example3")));
        Assert.assertNull(RdfHelper.getLabel(model, model.getResource("http://example.org/example4")));
        Assert.assertNull(RdfHelper.getLabel(null, model.getResource("http://example.org/example1")));
    }

    @Test
    public void testGetDescription() {
        Model model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://example.org/example1"), RDFS.label, "Label 1");
        model.add(model.getResource("http://example.org/example1"), RDFS.comment, "Comment 1");
        model.add(model.getResource("http://example.org/example2"), RDFS.label, "Second label");
        model.add(model.getResource("http://example.org/example3"), RDFS.comment, "Comment for resource 3");

        Assert.assertEquals("Comment 1",
                RdfHelper.getDescription(model, model.getResource("http://example.org/example1")));
        Assert.assertEquals("Comment for resource 3",
                RdfHelper.getDescription(model, model.getResource("http://example.org/example3")));

        Assert.assertTrue((new HashSet<String>(Arrays.asList("Comment 1", "Comment for resource 3"))
                .contains(RdfHelper.getDescription(model, null))));

        Assert.assertNull(RdfHelper.getDescription(model, model.getResource("http://example.org/example2")));
        Assert.assertNull(RdfHelper.getDescription(model, model.getResource("http://example.org/example4")));
        Assert.assertNull(RdfHelper.getDescription(null, model.getResource("http://example.org/example1")));
    }

    @Test
    public void testGetStringValue() {
        Model model = ModelFactory.createDefaultModel();
        model.add(model.getResource("http://example.org/example1"), model.getProperty("http://example.org/property1"),
                "value 1");
        model.add(model.getResource("http://example.org/example1"), model.getProperty("http://example.org/property2"),
                model.getResource("http://example.org/example2"));
        model.add(model.getResource("http://example.org/example1"), model.getProperty("http://example.org/property3"),
                "A");
        model.add(model.getResource("http://example.org/example1"), model.getProperty("http://example.org/property3"),
                "B");

        // literal object matches
        Assert.assertEquals("value 1", RdfHelper.getStringValue(model, model.getResource("http://example.org/example1"),
                model.getProperty("http://example.org/property1")));
        // resource object matches
        Assert.assertEquals("http://example.org/example2", RdfHelper.getStringValue(model,
                model.getResource("http://example.org/example1"), model.getProperty("http://example.org/property2")));
        // more than one triple matches
        Assert.assertTrue((new HashSet<String>(Arrays.asList("A", "B"))).contains(RdfHelper.getStringValue(model,
                model.getResource("http://example.org/example1"), model.getProperty("http://example.org/property3"))));

        // resource wildcard
        Assert.assertTrue((new HashSet<String>(Arrays.asList("A", "B")))
                .contains(RdfHelper.getStringValue(model, null, model.getProperty("http://example.org/property3"))));
        // property wildcard
        Assert.assertTrue((new HashSet<String>(Arrays.asList("value 1", "http://example.org/example2", "A", "B")))
                .contains(RdfHelper.getStringValue(model, model.getResource("http://example.org/example1"), null)));

        // resource and property exist but there is no matching triple
        Assert.assertNull(RdfHelper.getStringValue(model, model.getResource("http://example.org/example2"),
                model.getProperty("http://example.org/property1")));
        // resource does not exist
        Assert.assertNull(RdfHelper.getStringValue(model, model.getResource("http://example.org/example3"),
                model.getProperty("http://example.org/property1")));
        // property does not exist
        Assert.assertNull(RdfHelper.getStringValue(model, model.getResource("http://example.org/example1"),
                model.getProperty("http://example.org/property4")));
        // model is null
        Assert.assertNull(RdfHelper.getStringValue(null, model.getResource("http://example.org/example1"),
                model.getProperty("http://example.org/property1")));
    }

    @Test
    public void testGetDateValue() {
        String modelString = "<http://example.org/MyChallenge> a <http://w3id.org/hobbit/vocab#Challenge>;"
                + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#label> \"My example Challenge\"@en;"
                + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#comment>    \"This is an example for a challenge.\"@en;"
                + "<http://w3id.org/hobbit/vocab#executionDate> \"2016-12-24\"^^<http://www.w3.org/2001/XMLSchema#Date>;"
                + "<http://w3id.org/hobbit/vocab#publicationDate> \"2016-12-26\"^^<http://www.w3.org/2001/XMLSchema#Date> ."
                + "<http://example.org/MySecondChallenge> <http://w3id.org/hobbit/vocab#publicationDate> \"2016-12-27\"^^<http://www.w3.org/2001/XMLSchema#Date> .";
        Model model = ModelFactory.createDefaultModel();
        model.read(new StringReader(modelString), "http://example.org/", "TTL");

        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        Calendar executionDate = Calendar.getInstance(timeZone);
        executionDate.set(2016, Calendar.DECEMBER, 24, 0, 0, 0);
        executionDate.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(executionDate,
                RdfHelper.getDateValue(model, model.getResource("http://example.org/MyChallenge"),
                        model.getProperty("http://w3id.org/hobbit/vocab#executionDate")));
        Calendar publicationDate = Calendar.getInstance(timeZone);
        publicationDate.set(2016, Calendar.DECEMBER, 26, 0, 0, 0);
        publicationDate.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(publicationDate,
                RdfHelper.getDateValue(model, model.getResource("http://example.org/MyChallenge"),
                        model.getProperty("http://w3id.org/hobbit/vocab#publicationDate")));
        Calendar secPublicationDate = Calendar.getInstance(timeZone);
        secPublicationDate.set(2016, Calendar.DECEMBER, 27, 0, 0, 0);
        secPublicationDate.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(secPublicationDate,
                RdfHelper.getDateValue(model, model.getResource("http://example.org/MySecondChallenge"),
                        model.getProperty("http://w3id.org/hobbit/vocab#publicationDate")));

        Assert.assertTrue((new HashSet<Calendar>(Arrays.asList(publicationDate, secPublicationDate))).contains(RdfHelper
                .getDateValue(model, null, model.getProperty("http://w3id.org/hobbit/vocab#publicationDate"))));

        // resource and property exist but there is no matching triple
        Assert.assertNull(RdfHelper.getDateValue(model, model.getResource("http://example.org/MySecondChallenge"),
                model.getProperty("http://w3id.org/hobbit/vocab#executionDate")));
        // resource does not exist
        Assert.assertNull(RdfHelper.getDateValue(model, model.getResource("http://example.org/example3"),
                model.getProperty("http://w3id.org/hobbit/vocab#executionDate")));
        // property does not exist
        Assert.assertNull(RdfHelper.getDateValue(model, model.getResource("http://example.org/MySecondChallenge"),
                model.getProperty("http://example.org/property4")));
        // model is null
        Assert.assertNull(RdfHelper.getDateValue(null, model.getResource("http://example.org/MyChallenge"),
                model.getProperty("http://w3id.org/hobbit/vocab#executionDate")));
    }
}
