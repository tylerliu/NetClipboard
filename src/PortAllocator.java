import java.util.ArrayList;
import java.util.List;

/**
 * Allocator for the connection port
 */
public class PortAllocator {
    private static final int INITIAL_PORT = 60001;
    private static int nextFree = 60001;
    private static List<Integer> allocated = new ArrayList<>();
    private static List<Integer> freed = new ArrayList<>();

    public synchronized static boolean request(int port){
        if (port < INITIAL_PORT || port > 65535) return false;
        if (port >= nextFree){
            while (nextFree < port) {
                freed.add(nextFree);
                nextFree ++;
            }
            allocated.add(nextFree);
            return true;
        }
        if (freed.contains(port)){
            freed.remove(Integer.valueOf(port));
            allocated.add(port);
            return true;
        }
        return false;
    }

    public synchronized static boolean free(int port){
        if (port < INITIAL_PORT || port > 65535) return false;
        if (!allocated.contains(port)) return false;
        allocated.remove(Integer.valueOf(port));
        freed.add(port);
        System.out.println("Freed: " + port);
        return true;
    }

    public synchronized static int alloc(){
        if (!freed.isEmpty()){
            int port = freed.get(0);
            allocated.add(port);
            return port;
        }
        assert nextFree <= 65535;
        int port = nextFree;
        allocated.add(port);
        nextFree ++;
        System.out.println("Allocated: " + port);
        return port;
    }
}
