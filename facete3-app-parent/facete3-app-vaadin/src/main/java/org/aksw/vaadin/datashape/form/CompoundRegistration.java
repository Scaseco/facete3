package org.aksw.vaadin.datashape.form;

import com.vaadin.flow.shared.Registration;

public interface CompoundRegistration
	extends Registration
{
    public void add(Registration registration);


    default void add(Runnable registration) {
    	Registration tmp = registration::run; 
    	add(tmp);
    }
}
