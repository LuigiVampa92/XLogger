package com.luigivampa92.xlogger.domain;

public interface InteractionLogSerializer {
    String serialize(InteractionLog logObject);
    InteractionLog deserialize(String serializedLogObject);
}
