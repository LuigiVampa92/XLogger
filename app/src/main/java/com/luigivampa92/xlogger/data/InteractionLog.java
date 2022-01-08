package com.luigivampa92.xlogger.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public final class InteractionLog implements Parcelable {

    private final InteractionType type;
    private final String packageName;
    private final String serviceName;
    private final List<InteractionLogEntry> entries;
    private final long timestamp;
    private final long duration;

    public InteractionLog(InteractionType type, String packageName, String serviceName, List<InteractionLogEntry> entries) {
        this.type = type;
        this.entries = entries;
        this.packageName = packageName;
        this.serviceName = serviceName;
        if (entries != null && !entries.isEmpty()) {
            InteractionLogEntry firstEntry = entries.get(0);
            InteractionLogEntry lastEntry = entries.get(entries.size() - 1);
            timestamp = firstEntry.getTimestamp();
            duration = lastEntry.getTimestamp() - firstEntry.getTimestamp();
        } else {
            timestamp = 0;
            duration = 0;
        }
    }

    public InteractionType getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<InteractionLogEntry> getEntries() {
        return entries;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }

    protected InteractionLog(Parcel in) {
        type = InteractionType.fromValue(in.readInt());
        packageName = in.readString();
        serviceName = in.readString();
        entries = in.createTypedArrayList(InteractionLogEntry.CREATOR);
        timestamp = in.readLong();
        duration = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.getValue());
        dest.writeString(packageName);
        dest.writeString(serviceName);
        dest.writeTypedList(entries);
        dest.writeLong(timestamp);
        dest.writeLong(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InteractionLog> CREATOR = new Creator<InteractionLog>() {
        @Override
        public InteractionLog createFromParcel(Parcel in) {
            return new InteractionLog(in);
        }

        @Override
        public InteractionLog[] newArray(int size) {
            return new InteractionLog[size];
        }
    };
}
