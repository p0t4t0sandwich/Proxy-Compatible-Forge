package org.adde0109.pcf.forwarding;

/** Forwarding types supported by PCF */
public enum Mode {
    LEGACY,
    BUNGEEGUARD,
    MODERN;

    public boolean isLegacy() {
        return this == LEGACY || this == BUNGEEGUARD;
    }

    public boolean isModern() {
        return this == MODERN;
    }
}
