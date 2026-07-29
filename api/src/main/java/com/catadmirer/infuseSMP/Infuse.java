package com.catadmirer.infuseSMP;

public class Infuse {
    private static final Infuse instance = new Infuse();

    public static Infuse getInstance() {
        return instance;
    }

    private Infuse() {}
}
