package org.aksw.vaadin.datashape.form;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.shared.Registration;

/**
 * Class copied and adapted from {@link com.vaadin.flow.component.ShortcutRegistration} where it is private.
 * 
 * Bundles multiple {@link Registration Registrations} together.
 * This is used to group registrations that need to be created and removed
 * together.
 */
public class CompoundRegistrationImpl
	implements CompoundRegistration
{
	private static final long serialVersionUID = 1L;

	protected Set<Registration> registrations;

    public CompoundRegistrationImpl(Registration... registrations) {
        this.registrations = new LinkedHashSet<>(Arrays.asList(registrations));
    }

    @Override
    public void add(Registration registration) {
    	Objects.requireNonNull(registration, "Cannot add because registration has already been removed");
    	
        // if (registration != null)
        registrations.add(registration);
    }

    @Override
    public void remove() {
        if (registrations != null) {
            registrations.forEach(Registration::remove);
            registrations = null;
        }
    }
}
