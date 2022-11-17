package com.luigivampa92.xlogger.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Objects;

public final class InteractionLogEntry implements Parcelable {

    private final long timestamp;
    private final InteractionLogEntryAction action;
    private final byte[] data;
    private final String message;
    private final String sender;
    private final String receiver;
    private final String serviceName;
    private final String characteristicName;

    public InteractionLogEntry(
            long timestamp,
            InteractionLogEntryAction action,
            byte[] data,
            String message,
            String sender,
            String receiver,
            String serviceName,
            String characteristicName
    ) {
        this.timestamp = timestamp;
        this.action = action;
        this.data = data;
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.serviceName = serviceName;
        this.characteristicName = characteristicName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public InteractionLogEntryAction getAction() {
        return action;
    }

    public byte[] getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getCharacteristicName() {
        return characteristicName;
    }

    protected InteractionLogEntry(Parcel in) {
        timestamp = in.readLong();
        action = InteractionLogEntryAction.fromValue(in.readInt());
        data = in.createByteArray();
        message = in.readString();
        sender = in.readString();
        receiver = in.readString();
        serviceName = in.readString();
        characteristicName = in.readString();
    }

    public static final Creator<InteractionLogEntry> CREATOR = new Creator<InteractionLogEntry>() {
        @Override
        public InteractionLogEntry createFromParcel(Parcel in) {
            return new InteractionLogEntry(in);
        }

        @Override
        public InteractionLogEntry[] newArray(int size) {
            return new InteractionLogEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(timestamp);
        parcel.writeInt(action.getValue());
        parcel.writeByteArray(data);
        parcel.writeString(message);
        parcel.writeString(sender);
        parcel.writeString(receiver);
        parcel.writeString(serviceName);
        parcel.writeString(characteristicName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                timestamp,
                action.getValue(),
                message,
                sender,
                receiver,
                serviceName,
                characteristicName
        );
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractionLogEntry that = (InteractionLogEntry) o;
        return timestamp == that.timestamp
                && action.getValue() == that.action.getValue()
                && Arrays.equals(data, that.data)
                && Objects.equals(message, that.message)
                && Objects.equals(sender, that.sender)
                && Objects.equals(receiver, that.receiver)
                && Objects.equals(serviceName, that.serviceName)
                && Objects.equals(characteristicName, that.characteristicName);
    }
}
