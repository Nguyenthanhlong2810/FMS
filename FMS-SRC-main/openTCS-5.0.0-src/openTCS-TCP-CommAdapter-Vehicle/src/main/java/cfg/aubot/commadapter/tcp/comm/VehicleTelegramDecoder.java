/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp.comm;

import cfg.aubot.commadapter.tcp.netty.ConnectionEventListener;
import cfg.aubot.commadapter.tcp.telegrams.*;
import com.google.common.primitives.Ints;
import org.opentcs.example.telegrams.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Decodes incoming bytes into {@link StateResponse} instances.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleTelegramDecoder
    extends ByteToMessageDecoder {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleTelegramDecoder.class);
  /**
   * The handler decoded responses are sent to.
   */
  private final ConnectionEventListener<Response> responseHandler;
  /**
   * The minimum bytes required to even try decoding (size of the smallest telegram).
   */
  private final long minimumBytesRequired;

  /**
   * Creates a new instance.
   *
   * @param responseHandler The handler decoded responses are sent to.
   */
  public VehicleTelegramDecoder(ConnectionEventListener<Response> responseHandler) {
    this.responseHandler = requireNonNull(responseHandler, "responseHandler");
    this.minimumBytesRequired = Ints.min(MovingResponse.TELEGRAM_LENGTH,
                                         OrderResponse.TELEGRAM_LENGTH,
                                         SetRouteResponse.TELEGRAM_LENGTH,
                                         RouteResponse.TELEGRAM_LENGTH,
                                         StateResponse.TELEGRAM_LENGTH,
                                         ErrorResponse.TELEGRAM_LENGTH);
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    // Don't do anything if we don't have enough bytes.
    if (in.readableBytes() < minimumBytesRequired) {
      return;
    }

    byte[] telegramData;
    in.markReaderIndex();

    if (in.readableBytes() >= MovingResponse.TELEGRAM_LENGTH) {
      LOG.debug("Checking if it's an order response...");
      telegramData = new byte[MovingResponse.TELEGRAM_LENGTH];
      in.readBytes(telegramData);
      LOG.debug("Telegram data: {}", Hex.encodeHexString(telegramData));
      if (MovingResponse.isMovingResponse(telegramData)) {
        responseHandler.onIncomingTelegram(new MovingResponse(telegramData));
        return;
      }
      else {
        in.resetReaderIndex();
      }
    }

    if (in.readableBytes() >= OrderResponse.TELEGRAM_LENGTH) {
      LOG.debug("Checking if it's an order response...");
      telegramData = new byte[OrderResponse.TELEGRAM_LENGTH];
      in.readBytes(telegramData);
      LOG.debug("Telegram data: {}", Hex.encodeHexString(telegramData));
      if (OrderResponse.isOrderResponse(telegramData)) {
        responseHandler.onIncomingTelegram(new OrderResponse(telegramData));
        return;
      }
      else {
        in.resetReaderIndex();
      }
    }

    if (in.readableBytes() >= SetRouteResponse.TELEGRAM_LENGTH) {
      LOG.debug("Checking if it's an order response...");
      telegramData = new byte[SetRouteResponse.TELEGRAM_LENGTH];
      in.readBytes(telegramData);
      LOG.debug("Telegram data: {}", Hex.encodeHexString(telegramData));
      if (SetRouteResponse.isSetRouteResponse(telegramData)) {
        responseHandler.onIncomingTelegram(new SetRouteResponse(telegramData));
        return;
      }
      else {
        in.resetReaderIndex();
      }
    }

    if (in.readableBytes() >= RouteResponse.TELEGRAM_LENGTH) {
      LOG.debug("Checking if it's an route response...");
      telegramData = new byte[RouteResponse.TELEGRAM_LENGTH];
      in.readBytes(telegramData);
      LOG.debug("Telegram data: {}", Hex.encodeHexString(telegramData));
      if (RouteResponse.isRouteResponse(telegramData)) {
        responseHandler.onIncomingTelegram(new RouteResponse(telegramData));
        return;
      }
      else {
        in.resetReaderIndex();
      }
    }

    if (in.readableBytes() >= StateResponse.TELEGRAM_LENGTH) {
      LOG.debug("Checking if it's a state response...");
      telegramData = new byte[StateResponse.TELEGRAM_LENGTH];
      in.readBytes(telegramData);
      LOG.debug("Telegram data: {}", Hex.encodeHexString(telegramData));
      if (StateResponse.isStateResponse(telegramData)) {
        responseHandler.onIncomingTelegram(new StateResponse(telegramData));
        return;
      }
      else {
        in.resetReaderIndex();
      }
    }

    if (in.readableBytes() >= ErrorResponse.TELEGRAM_LENGTH) {
      telegramData = new byte[ErrorResponse.TELEGRAM_LENGTH];
      in.readBytes(telegramData);

      if (ErrorResponse.isErrorResponse(telegramData)) {
        responseHandler.onIncomingTelegram(new ErrorResponse(telegramData));
        return;
      } else {
        in.resetReaderIndex();
      }
    }

    telegramData = new byte[in.readableBytes()];
    in.readBytes(telegramData);

    if (CurrentRouteResponse.isCurrentRouteResponse(telegramData)) {
      responseHandler.onIncomingTelegram(new CurrentRouteResponse(telegramData));
    } else {
      // Don't reset reader index and discard bytes
      LOG.warn("Not a valid telegram: {}", Hex.encodeHexString(telegramData));
    }
  }
}
