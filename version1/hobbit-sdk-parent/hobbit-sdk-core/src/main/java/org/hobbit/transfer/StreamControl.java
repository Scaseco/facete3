package org.hobbit.transfer;

public enum StreamControl {
    // The reader has lost interest in the stream;
    // the sender should not bother sending more data
    READING_ABORTED((byte)1),

    // The sender has lost interest in completing the stream;
    // The reader cannot expect any data it started reading to become completed
    SENDING_ABORTED((byte)2);

    private byte code;

    StreamControl(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
}
