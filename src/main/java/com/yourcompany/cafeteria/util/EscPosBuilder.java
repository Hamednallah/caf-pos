package com.yourcompany.cafeteria.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A utility class to build byte command sequences for ESC/POS printers.
 */
public class EscPosBuilder {

    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    // Command constants
    private static final byte ESC = 0x1B;
    private static final byte GS = 0x1D;

    public EscPosBuilder() {
        // Initialize printer by default
        initialize();
    }

    private void write(byte... data) {
        try {
            stream.write(data);
        } catch (IOException e) {
            // This should not happen with a ByteArrayOutputStream
            throw new RuntimeException(e);
        }
    }

    public EscPosBuilder initialize() {
        write(ESC, (byte) 0x40);
        return this;
    }

    public EscPosBuilder append(String text) {
        if (text != null && !text.isEmpty()) {
            write(text.getBytes(StandardCharsets.UTF_8));
        }
        return this;
    }

    public EscPosBuilder feedLine() {
        write((byte) 0x0A); // LF
        return this;
    }

    public EscPosBuilder feedLines(int lines) {
        write(ESC, (byte) 0x64, (byte) lines);
        return this;
    }

    public EscPosBuilder bold(boolean on) {
        write(ESC, (byte) 0x45, (byte) (on ? 1 : 0));
        return this;
    }

    public EscPosBuilder underline(boolean on) {
        write(ESC, (byte) 0x2D, (byte) (on ? 1 : 0));
        return this;
    }

    public EscPosBuilder alignLeft() {
        write(ESC, (byte) 0x61, (byte) 0);
        return this;
    }

    public EscPosBuilder alignCenter() {
        write(ESC, (byte) 0x61, (byte) 1);
        return this;
    }

    public EscPosBuilder alignRight() {
        write(ESC, (byte) 0x61, (byte) 2);
        return this;
    }

    public EscPosBuilder cut() {
        write(GS, (byte) 0x56, (byte) 0);
        return this;
    }

    public byte[] getBytes() {
        return stream.toByteArray();
    }
}
