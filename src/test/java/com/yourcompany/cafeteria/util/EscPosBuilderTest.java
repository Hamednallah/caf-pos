package com.yourcompany.cafeteria.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class EscPosBuilderTest {

    private static final byte[] INIT_BYTES = {0x1B, 0x40};

    // Helper to create expected byte arrays with the default initialization prefix
    private byte[] withInit(byte... commandBytes) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(INIT_BYTES);
        stream.write(commandBytes);
        return stream.toByteArray();
    }

    @Test
    public void testDefaultConstructorInitializes() {
        // The constructor should automatically call initialize()
        assertArrayEquals(INIT_BYTES, new EscPosBuilder().getBytes());
    }

    @Test
    public void testInitialize() throws IOException {
        // Calling initialize() again should just add the init sequence again
        byte[] expected = withInit(INIT_BYTES);
        byte[] actual = new EscPosBuilder().initialize().getBytes();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testBold() throws IOException {
        assertArrayEquals(withInit((byte)0x1B, (byte)0x45, (byte)1), new EscPosBuilder().bold(true).getBytes());
        assertArrayEquals(withInit((byte)0x1B, (byte)0x45, (byte)0), new EscPosBuilder().bold(false).getBytes());
    }

    @Test
    public void testUnderline() throws IOException {
        assertArrayEquals(withInit((byte)0x1B, (byte)0x2D, (byte)1), new EscPosBuilder().underline(true).getBytes());
        assertArrayEquals(withInit((byte)0x1B, (byte)0x2D, (byte)0), new EscPosBuilder().underline(false).getBytes());
    }

    @Test
    public void testAlignCenter() throws IOException {
        assertArrayEquals(withInit((byte)0x1B, (byte)0x61, (byte)1), new EscPosBuilder().alignCenter().getBytes());
    }

    @Test
    public void testAlignLeft() throws IOException {
        assertArrayEquals(withInit((byte)0x1B, (byte)0x61, (byte)0), new EscPosBuilder().alignLeft().getBytes());
    }

    @Test
    public void testAlignRight() throws IOException {
        assertArrayEquals(withInit((byte)0x1B, (byte)0x61, (byte)2), new EscPosBuilder().alignRight().getBytes());
    }

    @Test
    public void testCut() throws IOException {
        assertArrayEquals(withInit((byte)0x1D, (byte)0x56, (byte)0), new EscPosBuilder().cut().getBytes());
    }

    @Test
    public void testAppend() throws IOException {
        String text = "Hello";
        assertArrayEquals(withInit(text.getBytes(StandardCharsets.UTF_8)), new EscPosBuilder().append(text).getBytes());
    }

    @Test
    public void testComplexSequence() throws IOException {
        String text = "Hello World";

        ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
        expectedStream.write(INIT_BYTES); // Default init
        expectedStream.write(new byte[]{(byte)0x1B, (byte)0x61, (byte)1}); // Align Center
        expectedStream.write(new byte[]{(byte)0x1B, (byte)0x45, (byte)1}); // Bold on
        expectedStream.write(text.getBytes(StandardCharsets.UTF_8));
        expectedStream.write((byte)0x0A); // Line Feed
        expectedStream.write(new byte[]{(byte)0x1D, (byte)0x56, (byte)0}); // Cut

        byte[] actual = new EscPosBuilder()
            .alignCenter()
            .bold(true)
            .append(text)
            .feedLine()
            .cut()
            .getBytes();

        assertArrayEquals(expectedStream.toByteArray(), actual);
    }
}
