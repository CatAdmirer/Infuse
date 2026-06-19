package com.catadmirer.infuseSMP.worldguard.handlers;

import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;

public class OceanEnabledHandler extends Handler {

    private static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<OceanEnabledHandler> {
        @Override
        public OceanEnabledHandler create(Session session) {
            return new OceanEnabledHandler(session);
        }
    }

    public OceanEnabledHandler(Session session) {
        super(session);
    }

    public static Factory getFactory() {
        return FACTORY;
    }
}
