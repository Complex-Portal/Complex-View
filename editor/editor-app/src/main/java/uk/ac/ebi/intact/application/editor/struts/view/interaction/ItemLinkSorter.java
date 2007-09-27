/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.interaction;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * This class is responsible for sorting out items to link or unlink.
 * The metod doIt() does the bulk of the work. Two get methods are provided
 * to get the results.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ItemLinkSorter {

    /**
     * Contains a list of items to link.
     */
    private Set myItemsToLink = new HashSet();

    /**
     * Contains a list of items to unlink.
     */
    private Set myItemsToUnlink = new HashSet();

    /**
     * This method does the sorting out features. If an Item occurs once
     * in both link and unlink, that Item wouldn't appear either in
     * the items to link or unlink list. On the otherhand, if a item occurs twice
     * in the link and once in the unlink lists, this item will only appear
     * in the items to link set.
     *
     * <b>This method is generic, ie., lists may contain any objects</b>
     *
     * @param linkedItems a list of items to link.
     * @param unlinkedItems a list of items to unlink.
     */
    public void doIt(List linkedItems, List unlinkedItems) {
        // Maps: bean -> how many times this bean ocurred in the list.
        Map linkedMap = getCardinalMap(linkedItems);

        // Maps: bean -> how many times this bean ocurred in the list.
        Map unlinkedMap = getCardinalMap(unlinkedItems);

        // Set of Feature beans to link.
        setItemsToUpdate(myItemsToLink, linkedMap, unlinkedMap);

        // Set of Feature beans to unlink.
        setItemsToUpdate(myItemsToUnlink, unlinkedMap, linkedMap);
    }

    /**
     * Returns a set of items to link.
     * @return a set containing items to link.
     */
    public Set getItemsToLink() {
        return myItemsToLink;
    }

    /**
     * Returns a set of items to unlink.
     * @return a set containing items to unlink.
     */
    public Set getItemsToUnLink() {
        return myItemsToUnlink;
    }

    // Helper Methods

    /**
     *
     * @param list a list of items to compute the cardinality of each item
     * @return map: key (an item in the list) -> how many times the key occurs
     * in the list (should give at least 1).
     */
    private Map getCardinalMap(List list) {
        // This will contains unique items from the list.
        Set set = new HashSet(list);

        // Maps: bean -> how many times this bean ocurred in the list.
        Map map = new HashMap();

        // Calculate the cardinality and store it in the map.
        for (Iterator i = set.iterator(); i.hasNext();) {
            Object key = (Object) i.next();
            int count = CollectionUtils.cardinality(key, list);
            map.put(key, new Integer(count));
        }
        return map;
    }

    /**
     *
     * @param features the Features. This will be cleared first and then fill
     * with Features need to be updated.
     * @param map1 the first map. if a count for a key in this map is
     * greater than the corresponding key in map2, then that key will be added
     * to <code>features</code> set. If a corresponding key is not found
     * in map2, the key is added <code>features</code> set as well.  
     * @param map2 the second map to make the comparision.
     */
    private void setItemsToUpdate(Set features, Map map1, Map map2) {
        // Clear out any prvious features to update.
        features.clear();

        for (Iterator i = map1.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Object key = entry.getKey();

            // The number of items to (un)link.
            int count1 = ((Integer) entry.getValue()).intValue();

            // The numbe of items to (un)link.
            int count2 = 0;
            if (map2.containsKey(key)) {
                count2 = ((Integer) map2.get(key)).intValue();
            }
            // Add to the list only when linked items exceed unlikned items or
            // vice vrsa.
            if (count1 > count2) {
                features.add(key);
            }
        }
    }
}
