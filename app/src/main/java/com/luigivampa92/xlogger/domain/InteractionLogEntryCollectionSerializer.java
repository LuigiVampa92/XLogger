package com.luigivampa92.xlogger.domain;

import java.util.Collection;

public interface InteractionLogEntryCollectionSerializer {
    String serialize(Collection<InteractionLogEntry> logEntries);
    Collection<InteractionLogEntry> deserialize(String serializedLogEntries);
}
