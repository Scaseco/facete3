package org.aksw.facete3.app.vaadin;

import java.io.Serializable;

import org.springframework.stereotype.Service;

@SuppressWarnings("serial")
@Service
public class GreetService implements Serializable {

    public String greet(String name) {
        if (name == null || name.isEmpty()) {
            return "Hello anonymous user";
        } else {
            return "Hello " + name;
        }
    }

}
