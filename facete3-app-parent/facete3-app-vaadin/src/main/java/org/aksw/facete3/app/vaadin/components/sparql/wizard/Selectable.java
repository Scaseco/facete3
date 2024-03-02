package org.aksw.facete3.app.vaadin.components.sparql.wizard;

import java.io.Serializable;
import java.util.Objects;

/** Helper class for adding a boolean state to an item */
public class Selectable<T>
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected T value;
    protected boolean isSelected;

    public Selectable(T value, boolean isSelected) {
        super();
        this.value = value;
        this.isSelected = isSelected;
    }

    public static <T> Selectable<T> of(T item) {
        return of(item, true);
    }

    public static <T> Selectable<T> of(T item, boolean isSelected) {
        return new Selectable<>(item, isSelected);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSelected, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Selectable<?> other = (Selectable<?>) obj;
        return isSelected == other.isSelected && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return "Selectable [isSelected=" + isSelected + ", value=" + value + "]";
    }
}
