package com.daftdroid.android.udprelay;

public interface RelayConfiguration {
    public RelaySpec getRelaySpec();
    public int getId();
    public Storage getStorage();
}
