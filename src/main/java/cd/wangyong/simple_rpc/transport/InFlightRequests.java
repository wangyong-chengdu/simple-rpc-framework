package cd.wangyong.simple_rpc.transport;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 在途请求，也就是已经发出去，但是还没有收到响应的responseFuture, 缓存ResponseFuture
 * @author andy
 * @since 2020/10/12
 */
public class InFlightRequests implements Closeable {
    private final static long TIMEOUT_SEC = 10L;

    /**
     * 用于限制同时发起请求的数量
     */
    private final Semaphore semaphore = new Semaphore(10);
    private final Map<Integer, ResponseFuture> futureMap = new ConcurrentHashMap<>();

    // 定时任务，兜底，防止有请求一直没有响应，积压在内存中
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    // 定时任务结果
    private final ScheduledFuture scheduledFuture;

    public InFlightRequests() {
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this::removeTimeoutFutures, TIMEOUT_SEC, TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    public void put(ResponseFuture responseFuture) throws TimeoutException, InterruptedException {
        if (semaphore.tryAcquire(TIMEOUT_SEC, TimeUnit.SECONDS)) {
            futureMap.put(responseFuture.getRequestId(), responseFuture);
        }
        else {
            throw new TimeoutException();
        }
    }

    private void removeTimeoutFutures() {
        futureMap.entrySet().removeIf(entry -> {
           if (System.nanoTime() - entry.getValue().getTimestamp() > TIMEOUT_SEC * 1000000000L) {
               semaphore.release();
               return true;
           }
           else {
               return false;
           }
        });
    }

    public ResponseFuture remove(int requestId) {
        ResponseFuture future = futureMap.remove(requestId);
        if (future != null) {
            semaphore.release();
        }
        return future;
    }


    @Override
    public void close() {
        scheduledFuture.cancel(true);
        scheduledExecutorService.shutdown();
    }
}
