package at.ac.wu.web.crawlers.thesis.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Helper class to enforce strict ordering of responses with same destination host.
 * <p>
 * Created by Patrick on 18.08.2017.
 */
@Service
public class OrderHelper {
    private static HashMap<String, TreeSet<OrderKey>> map = new HashMap<>();
    private static boolean isTimeout;
    @Autowired
    Environment environment;
    private long timeout;

    public OrderHelper(long timeout) {
        this.timeout = timeout;
        isTimeout = this.timeout > 0L;
    }

    /**
     * Adds key to map for later check.
     *
     * @param key Key
     */
    public static synchronized void add(OrderKey key) {
        if (!isTimeout) {
            return;
        }
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
        if (key == null || !isTimeout) {
            return false;
        }
        TreeSet<OrderKey> orderSet = map.get(key.remoteHost);
        if (orderSet != null && orderSet.contains(key)) {
            int tried = 0;
            while (true) {
                OrderKey oldest = orderSet.first();
                if (!oldest.equals(key) && tried * 200 < timeout) { //Try until timeout reached
                    try {
                        tried++;
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    orderSet.remove(key);
                    return true;
                }
            }
        }
        return true;
    }
}
