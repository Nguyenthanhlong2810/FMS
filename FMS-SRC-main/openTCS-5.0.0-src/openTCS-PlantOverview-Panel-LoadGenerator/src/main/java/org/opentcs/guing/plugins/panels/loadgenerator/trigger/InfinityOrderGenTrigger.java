package org.opentcs.guing.plugins.panels.loadgenerator.trigger;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.plugins.panels.loadgenerator.batchcreator.ExplicitOrderBatchGenerator;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.plugins.panels.loadgenerator.batchcreator.ExplicitOrderBatchGenerator.TRANSPORT_ORDER_TRIGGER_PROPERTY;

public class InfinityOrderGenTrigger
        implements EventHandler,
        OrderGenerationTrigger {

    /**
     * This class's Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdOrderGenTrigger.class);
    /**
     * Where we get events from.
     */
    private final EventSource eventSource;
    /**
     * The instance actually creating the new orders.
     */
    private final ExplicitOrderBatchGenerator orderBatchCreator;

    public InfinityOrderGenTrigger(final @ApplicationEventBus EventSource eventSource,
                                   final ExplicitOrderBatchGenerator orderBatchCreator) {
        this.eventSource = requireNonNull(eventSource, "eventSource");
        this.orderBatchCreator = requireNonNull(orderBatchCreator, "orderBatchCreator");
    }

    @Override
    public void setTriggeringEnabled(boolean enabled) {
        if (enabled) {
            eventSource.subscribe(this);
            triggerOrderGeneration();
        } else {
            eventSource.unsubscribe(this);
        }
    }

    @Override
    public void triggerOrderGeneration() throws KernelRuntimeException {
        orderBatchCreator.createOrderBatch();
    }

    @Override
    public void onEvent(Object event) {
        if (!(event instanceof TCSObjectEvent)) {
            return;
        }

        TCSObjectEvent objEvent = (TCSObjectEvent) event;
        if (!(objEvent.getCurrentOrPreviousObjectState() instanceof TransportOrder)) {
            return;
        }

        TransportOrder eventOrder = (TransportOrder) objEvent.getCurrentOrPreviousObjectState();
        if (eventOrder.getState().isFinalState()
        && eventOrder.getProperties().containsKey(TRANSPORT_ORDER_TRIGGER_PROPERTY)) {
            orderBatchCreator.reCreateOrder(eventOrder);
        }
    }
}
