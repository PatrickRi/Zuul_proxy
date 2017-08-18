package at.ac.wu.web.crawlers.thesis.order;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Helper class to enforce strict ordering of responses with same destination host.
 * <p>
 * Created by Patrick on 18.08.2017.
 */
public class OrderHelper {
    private static HashMap<String, TreeSet<OrderKey>> map = new HashMap<>();

    /**
     * Adds key to map for later check.
     *
     * @param key Key
     */
    public static synchronized void add(OrderKey key) {
        if (map.containsKey(key.remoteHost)) {
            map.get(key.remoteHost).add(key);
        } else {
            TreeSet<OrderKey> set = new TreeSet<>();
            set.add(key);
            map.put(key.remoteHost, set);
        }
    }

    /**
     * Checks if the given key is the oldest one with the same remoteHost, if not the current Thread polls the
     * map until all older elements for the same host are removed from the list.
     *
     * @param key Key
     * @return true
     */
    public boolean remove(OrderKey key) {
        TreeSet<OrderKey> orderKeys = map.get(key.remoteHost);
        if (orderKeys != null && orderKeys.contains(key)) {
            int tried = 0;
            while (true) {
                OrderKey oldest = orderKeys.first();
                if (!oldest.equals(key) && tried < 60 * 3) { //Try a few times
                    try {
                        tried++;
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    orderKeys.remove(key);
                    return true;
                }
            }
        }
        return true;
    }
}
