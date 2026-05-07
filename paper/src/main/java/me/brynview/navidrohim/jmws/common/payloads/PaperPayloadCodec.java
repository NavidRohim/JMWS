package me.brynview.navidrohim.jmws.common.payloads;

import java.nio.charset.StandardCharsets;

final class PaperPayloadCodec {
    private PaperPayloadCodec() {
    }

    static byte[] writeUtf(String value, int maxLength) {
        byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
        if (utf8.length > maxLength) {
            throw new IllegalArgumentException("Encoded string exceeds max length of " + maxLength + " bytes");
        }

        byte[] length = writeVarInt(utf8.length);
        byte[] payload = new byte[length.length + utf8.length];
        System.arraycopy(length, 0, payload, 0, length.length);
        System.arraycopy(utf8, 0, payload, length.length, utf8.length);
        return payload;
    }

    static String readUtf(byte[] payload, int maxLength) {
        VarIntResult length = readVarInt(payload);
        if (length.value > maxLength) {
            throw new IllegalArgumentException("Encoded string exceeds max length of " + maxLength + " bytes");
        }
        if (payload.length - length.bytesRead < length.value) {
            throw new IllegalArgumentException("Encoded string length is larger than the received payload");
        }
        return new String(payload, length.bytesRead, length.value, StandardCharsets.UTF_8);
    }

    private static byte[] writeVarInt(int value) {
        byte[] bytes = new byte[5];
        int index = 0;
        do {
            byte current = (byte) (value & 0b0111_1111);
            value >>>= 7;
            if (value != 0) {
                current |= (byte) 0b1000_0000;
            }
            bytes[index++] = current;
        } while (value != 0);

        byte[] result = new byte[index];
        System.arraycopy(bytes, 0, result, 0, index);
        return result;
    }

    private static VarIntResult readVarInt(byte[] bytes) {
        int value = 0;
        int position = 0;

        for (int i = 0; i < bytes.length; i++) {
            int current = bytes[i] & 0xFF;
            value |= (current & 0b0111_1111) << position;

            if ((current & 0b1000_0000) == 0) {
                return new VarIntResult(value, i + 1);
            }

            position += 7;
            if (position >= 35) {
                throw new IllegalArgumentException("VarInt is too large");
            }
        }

        throw new IllegalArgumentException("Payload ended before VarInt completed");
    }

    private record VarIntResult(int value, int bytesRead) {
    }
}
