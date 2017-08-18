package at.ac.wu.web.crawlers.thesis.order;

/**
 * Created by Patrick on 18.08.2017.
 */
public class OrderKey implements Comparable<OrderKey> {

    public final String remoteHost;
    public final String threadName;
    public final long time;

    public OrderKey(String remoteHost, String threadName) {
        this.remoteHost = remoteHost;
        this.threadName = threadName;
        this.time = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrderKey orderKey = (OrderKey) o;

        if (remoteHost != null ? !remoteHost.equals(orderKey.remoteHost) : orderKey.remoteHost != null) {
            return false;
        }
        return threadName != null ? threadName.equals(orderKey.threadName) : orderKey.threadName == null;

    }

    @Override
    public int hashCode() {
        int result = remoteHost != null ? remoteHost.hashCode() : 0;
        result = 31 * result + (threadName != null ? threadName.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(OrderKey o) {
        return Long.compare(this.time, o.time);
    }

    @Override
    public String toString() {
        return "OrderKey{" +
                "remoteHost='" + remoteHost + '\'' +
                ", threadName='" + threadName + '\'' +
                ", time=" + time +
                '}';
    }
}
