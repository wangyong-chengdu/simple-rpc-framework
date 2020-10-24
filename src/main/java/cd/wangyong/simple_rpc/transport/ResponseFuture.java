package cd.wangyong.simple_rpc.transport;

import java.util.concurrent.CompletableFuture;

import cd.wangyong.simple_rpc.transport.command.Command;

/**
 * @author andy
 * @since 2020/10/12
 */
public class ResponseFuture {
    private final int requestId;
    private final CompletableFuture<Command> future;
    private final long timestamp;

    public ResponseFuture(int requestId, CompletableFuture<Command> future) {
        this.requestId = requestId;
        this.future = future;
        this.timestamp = System.nanoTime();
    }

    public int getRequestId() {
        return requestId;
    }

    public CompletableFuture<Command> getFuture() {
        return future;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
