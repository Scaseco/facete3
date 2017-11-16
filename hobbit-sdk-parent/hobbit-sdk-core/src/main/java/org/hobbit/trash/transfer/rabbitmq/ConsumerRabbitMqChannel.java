package org.hobbit.trash.transfer.rabbitmq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

public class ConsumerRabbitMqChannel
    implements Consumer<ByteBuffer>
{
    protected Channel channel;
    protected String exchangeName;
    protected String routingKey;
    protected AMQP.BasicProperties properties;

    // false: Sends data between position and limit
    // true: Sends buffer.array()
    protected boolean sendWholeBackingArray;

    @Override
    public void accept(ByteBuffer t) {

        byte[] data = t.array();
        if(!sendWholeBackingArray && t.remaining() != data.length) {
            data = new byte[t.remaining()];
            t.get(data);
        }

        try {
            channel.basicPublish(exchangeName, routingKey, properties, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Allow for easy hacking via casting a consumer to this type

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public AMQP.BasicProperties getProperties() {
        return properties;
    }

    public void setProperties(AMQP.BasicProperties properties) {
        this.properties = properties;
    }

    public boolean isSendWholeBackingArray() {
        return sendWholeBackingArray;
    }

    public void setSendWholeBackingArray(boolean sendWholeBackingArray) {
        this.sendWholeBackingArray = sendWholeBackingArray;
    }


}
