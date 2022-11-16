package com.luigivampa92.xlogger.domain;

public interface InteractionLogEntrySerializer {
    String serialize(InteractionLogEntry logEntryObject);
    InteractionLogEntry deserialize(String serializedLogEntryObject);
}
