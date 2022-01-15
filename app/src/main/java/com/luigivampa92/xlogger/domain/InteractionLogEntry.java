package com.luigivampa92.xlogger.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.luigivampa92.xlogger.DataUtils;

import java.util.Arrays;
import java.util.Objects;

public final class InteractionLogEntry implements Parcelable {

    private final long timestamp;
    private final byte[] data;
    private final String sender;
    private final String receiver;

    public InteractionLogEntry(long timestamp, byte[] data, String sender, String receiver) {
        this.timestamp = timestamp;
        this.data = data;
        this.sender = sender;
        this.receiver = receiver;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getData() {
        return data;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    protected InteractionLogEntry(Parcel in) {
        timestamp = in.readLong();
        sender = in.readString();
        receiver = in.readString();
        data = in.createByteArray();
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
        parcel.writeString(sender);
        parcel.writeString(receiver);
        parcel.writeByteArray(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractionLogEntry that = (InteractionLogEntry) o;
        return timestamp == that.timestamp
                && Arrays.equals(data, that.data)
                && Objects.equals(sender, that.sender)
                && Objects.equals(receiver, that.receiver);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(timestamp, sender, receiver);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "InteractionLogEntry{" +
                "timestamp=" + timestamp +
                ", data=" + DataUtils.toHexString(data) +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                '}';
    }
}
