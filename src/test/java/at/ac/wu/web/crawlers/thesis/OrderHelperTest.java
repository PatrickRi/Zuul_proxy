package at.ac.wu.web.crawlers.thesis;

import at.ac.wu.web.crawlers.thesis.order.OrderHelper;
import at.ac.wu.web.crawlers.thesis.order.OrderKey;
import org.junit.Test;

import java.util.Date;

/**
 * Created by Patrick on 18.08.2017.
 */
public class OrderHelperTest {

    @Test
    public void test() throws Exception {
        OrderStubClient fast = new OrderStubClient("fast", 1000);
        OrderStubClient slow = new OrderStubClient("slow", 5000);
        new Thread(slow).start();
        Thread.sleep(200);
        new Thread(fast).start();
        Thread.sleep(10000);
    }

    class OrderStubClient implements Runnable {

        private int delay;
        private String name;

        public OrderStubClient(String name, int delay) {
            this.name = name;
            this.delay = delay;
        }

        @Override
        public void run() {
            OrderKey key = new OrderKey("remote", Thread.currentThread().getName());
            System.out.println(this.name + " - ADD");
            OrderHelper.add(key);
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(this.name + " - REMOVE " + new Date());
            new OrderHelper().remove(key);
            System.out.println(this.name + " - DONE " + new Date());
        }
    }
}
