package dev.instellar.stellar.configuration.serializer;

import com.mohistmc.org.spongepowered.configurate.serialize.ScalarSerializer;
import com.mohistmc.org.spongepowered.configurate.serialize.SerializationException;
import dev.instellar.stellar.configuration.type.StorageEngine;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public final class StorageEngineSerializer extends ScalarSerializer<StorageEngine> {

    public StorageEngineSerializer() {
        super(StorageEngine.class);
    }

    @Override
    public Object serialize(final StorageEngine value, final Predicate<Class<?>> typeSupported) {
        return value.getId();
    }

    @Override
    public StorageEngine deserialize(final Type type, final Object obj) throws SerializationException {
        if (obj instanceof Integer id) {
            try {
                return StorageEngine.fromId(id);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid storage engine ID: " + id, e);
            }
        }

        throw new SerializationException("%s is not of a valid type %s for this node".formatted(obj, type));
    }

}
