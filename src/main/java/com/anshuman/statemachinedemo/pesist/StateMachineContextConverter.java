package com.anshuman.statemachinedemo.pesist;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;

@SuppressWarnings("rawtypes") // StateMachineContext is used without generics.
@RequiredArgsConstructor
@Converter(autoApply = true)
@Slf4j
public class StateMachineContextConverter implements AttributeConverter<StateMachineContext, byte[]> {
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
        kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
        kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
        return kryo;
    });

    @Override
    public byte[] convertToDatabaseColumn(StateMachineContext attribute) {
        byte[] smByteArr = serialize(attribute);
        if (smByteArr == null)
            log.warn("StateMachineContext: {} serialized to null byte array", attribute.getId());
        return smByteArr;
    }

    @Override
    public StateMachineContext convertToEntityAttribute(byte[] dbData) {
        StateMachineContext stateMachineContext = deserialize(dbData);
        if (stateMachineContext == null)
            log.warn("Byte array of size: {} deserialized to a null StateMachineContext object", dbData.length);
        return stateMachineContext;
    }

    private byte[] serialize(StateMachineContext context) {
        if (context == null) {
            log.warn("No serialization occurred as the state machine context object is null");
            return null;
        }

        Kryo kryo = kryoThreadLocal.get();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            try(Output output = new Output(out)) {
                kryo.writeObject(output, context);
            }
            byte[] outByteArr = out.toByteArray();
            log.debug("StateMachineContext: {}, serialized and written to thread local as byte array of: {} bytes",
                context.getId(), outByteArr.length);
            return outByteArr;
        } catch (IOException e) {
            throw new RuntimeException("Exception encountered when serializing state machine context to byte array", e);
        }
    }

    private StateMachineContext deserialize(byte[] data) {
        if (data == null || data.length == 0) {
            log.warn("No deserialization occurred as the input byte array is null or empty");
            return null;
        }

        Kryo kryo = kryoThreadLocal.get();

        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            try(Input input = new Input(in)) {
                StateMachineContext stateMachineContext = kryo.readObject(input, StateMachineContext.class);
                log.debug("StateMachineContext: {}, deserialized from thread local byte array of: {} bytes",
                    stateMachineContext.getId(), data.length);
                return stateMachineContext;
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception encountered when deserializing byte array to state machine context", e);
        }
    }
}
