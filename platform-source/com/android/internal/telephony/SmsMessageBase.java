package com.android.internal.telephony;

public abstract class SmsMessageBase {
    public static abstract class SubmitPduBase  {
        public byte[] encodedScAddress; // Null if not applicable.
        public byte[] encodedMessage;
    }
}
