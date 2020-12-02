# Facete3 Vaadin Web Application

This project features a powerful front end for faceted search over RDF data.

## Roadmap

* A way to provide additional UI views (Vaadin components) on RDF data using separate jars
* Add facets as columns to the table

## Creating custom views


The most relevant classes are the following:

* ViewManager: This class manages a collection of views and is responsible for selecting the best one by whatever criteria
* ViewFactory: This class is capable of generating a VaadinComponent from a given Jena Resource. A ViewFactory exposes a ViewTemplate which declares for which resources it is applicable and which attributes it needs.
* ViewTemplate: This class encapsulates declarative information about a view:
	* A Resource that describes the view. That resource must be an IRI
	* An condition that intensionally declares the set of RDF terms for which the resource applies, e.g. `[SELECT] ?s { ?s a :Publication }`
	* An SPARQL query fragment that describes which attributes to fetch 


TODO EntityQuery needs more explanation

### A simple View

```java
public class ViewFactoryLabel
    implements ViewFactory
{
    @Override
    public ViewTemplate getViewTemplate() {

        EntityQueryImpl attrQuery = new EntityQueryImpl();

        Node p = RDFS.Nodes.label;
        List<Var> vars = Collections.singletonList(Vars.s);
        EntityGraphFragment fragment = new EntityGraphFragment(
                vars,
                new EntityTemplateImpl(Collections.<Node>singletonList(Vars.s), new Template(
                        BasicPattern.wrap(Collections.singletonList(Triple.create(Vars.s, p, Vars.o))))),
                ElementUtils.createElementTriple(Vars.s, p, Vars.o)
                );

        attrQuery.getOptionalJoins().add(new GraphPartitionJoin(fragment));

        return new ViewTemplateImpl(
                // The id + metadata of the view
                ModelFactory.createDefaultModel()
                    .createResource("http://cord19.aksw.org/view/label")
                    .addLiteral(RDFS.label, "Label"),

                // The condition for which sete of resources the view is applicable
                Concept.parse("?s { ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o } "),

                // The entity-centric construct query for what information to fetch when applying the view
                attrQuery
                );

    }

    @Override
    public Component createComponent(Resource initialData) {
        ViewComponentLabel result = new ViewComponentLabel(initialData);
        return result;
    }
}


class ViewComponentLabel
    extends Span
{
    protected Resource state;

    public ViewComponentLabel(Resource initialState) {
        this.state = initialState;
        setWidthFull();
        display();
    }

    public void setState(Resource state) {
        this.state = state;
        display();
    }

    public void display() {
        BestLiteralConfig bestLiteralCfg = BestLiteralConfig.fromProperty(RDFS.label);
        String xlabel = state == null ? null : LabelUtils.getOrDeriveLabel(state, bestLiteralCfg);

        setText(xlabel);
    }
}
```


### Registering Views in Facete:

Facete at present uses two view managers - one for a snippet in the matching items list and one for when viewing individual resources.


```java
public class ConfigViewManager
{

    @Bean
    @FullView
    public ViewManager viewManagerFull(SparqlQueryConnection conn) {
        ViewManagerImpl result = new ViewManagerImpl(conn);

        result.register(new ViewFactoryPaper());
        result.register(new ViewFactoryDoiPdfViewer());
        result.register(new ViewFactoryLabel());

        return result;
    }

    @Bean
    @SnippetView
    public ViewManager viewManagerDetail(SparqlQueryConnection conn) {
        ViewManagerImpl result = new ViewManagerImpl(conn);

        result.register(new ViewFactoryPaper());
        result.register(new ViewFactoryLabel());


        return result;
    }
}
```





# Generic Vaadin and and Spring Boot Information

This project can be used as a starting point to create your own Vaadin application with Spring Boot.
It contains all the necessary configuration and some placeholder files to get you started.

The best way to create your own project based on this starter is [start.vaadin.com](https://start.vaadin.com/) - you can get only the necessary parts and choose the package naming you want to use.

### Running the Application

Import the project to the IDE of your choosing as a Maven project.

Run the application using `mvn spring-boot:run` or by running the `Application` class directly from your IDE.

Open http://localhost:8080/ in your browser.

If you want to run the application locally in the production mode, run `mvn spring-boot:run -Pproduction`.

To run Integration Tests, execute `mvn verify -Pintegration-tests`.

### More Information

- [Vaadin Flow](https://vaadin.com/flow) documentation
- [Using Vaadin and Spring](https://vaadin.com/docs/v14/flow/spring/tutorial-spring-basic.html) article

