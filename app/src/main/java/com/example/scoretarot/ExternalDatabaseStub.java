package com.example.scoretarot;

import android.content.Context;

public final class ExternalDatabaseStub {

    private ExternalDatabaseStub() {
    }

    public static String sync(Context context) {
        return context.getString(R.string.external_db_stub_message);
    }
}