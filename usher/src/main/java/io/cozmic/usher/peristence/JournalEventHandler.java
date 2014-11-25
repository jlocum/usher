package io.cozmic.usher.peristence;
import com.lmax.disruptor.EventHandler;
import io.cozmic.usher.PersistenceVerticle;
import io.cozmic.usherprotocols.core.Request;
import org.vertx.java.core.Vertx;

/**
 * Created by chuck on 9/15/14.
 */
public class JournalEventHandler implements EventHandler<RequestEvent>
{

    private final Vertx vertx;

    public JournalEventHandler(Vertx vertx) {

        this.vertx = vertx;
    }
    public void onEvent(RequestEvent event, long sequence, boolean endOfBatch)
    {
        final Request request = event.getRequest();
        vertx.eventBus().send(PersistenceVerticle.JOURNAL_ADDRESS, request.buildEnvelope());
    }
}