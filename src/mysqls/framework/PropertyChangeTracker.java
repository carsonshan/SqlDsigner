package mysqls.framework;

import mysqls.commands.Command;
import mysqls.commands.CompoundCommand;
import mysqls.graph.Graph;
import mysqls.graph.GraphElement;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Tacks the modification of GraphElement properties.
 *
 * @author Martin P. Robillard
 */
public class PropertyChangeTracker {
    private Object[] aPropertyValues;
    private GraphElement aEdited;

    /**
     * Records the value of the properties of pEdited.
     *
     * @param pEdited The object being edited.
     */
    public void startTrackingPropertyChange(GraphElement pEdited) {
        try {
            aEdited = pEdited;
            PropertyDescriptor[] oldDescriptors = Introspector.getBeanInfo(pEdited.getClass()).getPropertyDescriptors();
            aPropertyValues = new Object[oldDescriptors.length];
            for (int i = 0; i < aPropertyValues.length; i++) {
                aPropertyValues[i] = getPropertyValue(oldDescriptors[i]);
            }
        } catch (IntrospectionException | IllegalArgumentException e) {
            assert false;
            return;
        }
    }

    /*
     * Returns the value of the property represented by pDescriptor on pEdited,
     * or null if the property cannot be read for any reason.
     */
    private Object getPropertyValue(PropertyDescriptor pDescriptor) {
        final Method getter = pDescriptor.getReadMethod();
        if (getter == null) {
            return null;
        } else {
            try {
                return PropertyChangeTracker.copyIfNecessary(getter.invoke(aEdited));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return null;
            }
        }
    }

    /*
     * Encodes domain knowledge about which types of objects need to be copied
     * and which can safely be shared. If an object is mutable, it is copied.
     */
    private static Object copyIfNecessary(Object pObject) {
        if (pObject instanceof MultiLineString) {
            return ((MultiLineString) pObject).clone();
        } else {
            return pObject;
        }
    }

    /**
     * Creates a PropertyChangeCommand from the component data. Returns null if
     * there is any problem.
     *
     * @param pGraph    the graph containing the edited node.
     * @param pElement  The element whose property changed.
     * @param pProperty The name of the changed property.
     * @param pOldValue The value of the property before the change.
     * @param pNewValue The value of the property after the change.
     * @return The new command object.
     */
    public static PropertyChangeCommand createPropertyChangeCommand(Graph pGraph, GraphElement pElement,
                                                                    String pProperty, Object pOldValue, Object pNewValue) {
        try {
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(pElement.getClass()).getPropertyDescriptors();
            for (int i = 0; i < descriptors.length; i++) {
                if (descriptors[i].getName().equals(pProperty)) {
                    return new PropertyChangeCommand(pGraph, pElement, PropertyChangeTracker.copyIfNecessary(pOldValue),
                            PropertyChangeTracker.copyIfNecessary(pNewValue), i);
                }
            }
            return null;
        } catch (IntrospectionException e) {
            return null;
        }
    }

    /**
     * Creates and returns a CompoundCommand that represents any change in
     * properties detected between the time startTrackingPropertyChange and
     * stopTrackingPropertyChange were called.
     *
     * @param pGraph The Graph containing the selected elements.
     * @return A CompoundCommand describing the property changes.
     */
    public CompoundCommand stopTrackingPropertyChange(Graph pGraph) {
        try {
            CompoundCommand command = new CompoundCommand();
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(aEdited.getClass()).getPropertyDescriptors();
            for (int i = 0; i < descriptors.length; i++) {
                Object propVal = getPropertyValue(descriptors[i]);
                if (!PropertyChangeTracker.equals(propVal, aPropertyValues[i])) {
                    command.add(new PropertyChangeCommand(pGraph, aEdited, aPropertyValues[i],
                            PropertyChangeTracker.copyIfNecessary(propVal), i));
                }

            }
            return command;
        } catch (IntrospectionException | IllegalArgumentException e) {
            assert false;
            return null;
        }
    }

    /*
     * Equality taking null equality into account.
     */
    private static boolean equals(Object pObject1, Object pObject2) {
        if (pObject1 == null) {
            return pObject2 == null;
        } else {
            return pObject1.equals(pObject2);
        }
    }

    /**
     * Stores the change in value of the property of a graph element.
     *
     * @author EJBQ
     * @author Martin P. Robillard
     */
    static class PropertyChangeCommand implements Command {
        private Graph aGraph;
        private GraphElement aObject;
        private Object aPrevPropValue;
        private Object aNewPropValue;
        private int aIndex; // The index of the changed property in
        // BeanInfo.getPropertyDescriptors

        /**
         * Creates the command and sets the values of the element. The values
         * should be clones so they do not get edited.
         *
         * @param pGraph         The graph of the object being changed.
         * @param pObject        The graph element being transformed
         * @param pPrevPropValue The initial value
         * @param pNewPropValue  The new value
         * @param pIndex         Which property of the object this is, so that we can
         *                       select it
         */
        PropertyChangeCommand(Graph pGraph, GraphElement pObject, Object pPrevPropValue, Object pNewPropValue,
                              int pIndex) {
            aGraph = pGraph;
            aObject = pObject;
            aPrevPropValue = pPrevPropValue;
            aNewPropValue = pNewPropValue;
            aIndex = pIndex;
        }

        /**
         * Changes the property of the Object to the old value.
         */
        @Override
        public void undo() {
            try {
                PropertyDescriptor[] descriptors = Introspector.getBeanInfo(aObject.getClass()).getPropertyDescriptors()
                        .clone();
                final Method setter = descriptors[aIndex].getWriteMethod();
                if (setter != null) {
                    setter.invoke(aObject, aPrevPropValue);
                }
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException exception) {
                assert false;
                return;
            }
            aGraph.layout();
        }

        /**
         * Changes the property of the Object.
         */
        @Override
        public void execute() {
            try {
                PropertyDescriptor[] descriptors = Introspector.getBeanInfo(aObject.getClass()).getPropertyDescriptors()
                        .clone();
                final Method setter = descriptors[aIndex].getWriteMethod();
                if (setter != null) {
                    setter.invoke(aObject, aNewPropValue);
                }
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException exception) {
                assert false;
                return;
            }
            aGraph.layout();
        }
    }
}
