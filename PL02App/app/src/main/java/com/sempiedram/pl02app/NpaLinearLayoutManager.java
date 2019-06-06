package com.sempiedram.pl02app;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

public class NpaLinearLayoutManager extends LinearLayoutManager {

    public NpaLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}
