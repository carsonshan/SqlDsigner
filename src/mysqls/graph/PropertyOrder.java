package mysqls.graph;

import java.util.HashMap;

import mysqls.sql.entity.Table;

/**
 * Singleton object that can return the presentation sequence index number at
 * which an object property should be displayed. The order does not have to be
 * total.
 *
 * @author Martin P. Robillard
 *
 */
public final class PropertyOrder {
	private static final PropertyOrder INSTANCE = new PropertyOrder();

	static {
		PropertyOrder.INSTANCE.addIndex(AssociationEdge.class, "directionality", 1);
		PropertyOrder.INSTANCE.addIndex(AssociationEdge.class, "sTableColumn", 2);
		PropertyOrder.INSTANCE.addIndex(AssociationEdge.class, "eTableColumn", 3);
		PropertyOrder.INSTANCE.addIndex(Table.class, "name", 1);
		PropertyOrder.INSTANCE.addIndex(Table.class, "columnlist", 2);
		// INSTANCE.addIndex(CallEdge.class, "middleLabel", 1);
		// PropertyOrder.INSTANCE.addIndex(ClassNode.class, "name", 1);
		// PropertyOrder.INSTANCE.addIndex(ClassNode.class, "attributes", 2);
		// PropertyOrder.INSTANCE.addIndex(ClassNode.class, "methods", 3);
		// INSTANCE.addIndex(InterfaceNode.class, "name", 1);
		// INSTANCE.addIndex(InterfaceNode.class, "methods", 2);
		// INSTANCE.addIndex(PackageNode.class, "name", 1);
		// INSTANCE.addIndex(PackageNode.class, "contents", 2);
	}

	private HashMap<Class<?>, HashMap<String, Integer>> aProperties = new HashMap<>();

	private PropertyOrder() {
	}

	/**
	 * @return The singleton instance of PropertyOrder
	 */
	public static PropertyOrder getInstance() {
		return PropertyOrder.INSTANCE;
	}

	/**
	 * Return the sequence index of pProperty of class pClass. Always returns 0
	 * if the sequence index is not specified for a property. If pProperty is
	 * not found in pClass, the superclasses are searched.
	 *
	 * @param pClass
	 *            The class supporting a property.
	 * @param pProperty
	 *            The property supported.
	 * @return The sequence order for the property.
	 */
	public int getIndex(Class<?> pClass, String pProperty) {
		if (pClass == null) {
			return 0;
		}
		HashMap<String, Integer> properties = aProperties.get(pClass);
		if (properties != null) {
			if (properties.containsKey(pProperty)) {
				return properties.get(pProperty);
			} else {
				return getIndex(pClass.getSuperclass(), pProperty);
			}
		} else {
			return getIndex(pClass.getSuperclass(), pProperty);
		}
	}

	private void addIndex(Class<?> pClass, String pProperty, int pIndex) {
		HashMap<String, Integer> properties = aProperties.get(pClass);
		if (properties == null) {
			properties = new HashMap<String, Integer>();
			aProperties.put(pClass, properties);
		}
		properties.put(pProperty, pIndex);
	}
}
