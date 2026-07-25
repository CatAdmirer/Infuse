package com.catadmirer.infuseSMP.util;

public abstract class RegionBlocker {
    private static RegionBlocker instance;

    public static void setInstance(RegionBlocker instance) {
        RegionBlocker.instance = instance;
    }

    public static RegionBlocker getInstance() {
        return instance;
    }

    // TODO: let people check for worldguard plugin
    // TODO: Make worldguardimpl a child of this class
    // TODO: make a child of this that uses blacklisted-worlds configs
    // TODO: make a way to use both blacklisted-worlds and worldguard at once.
}
