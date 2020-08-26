package a;

import java.nio.charset.StandardCharsets;

public class Ascii85 {
    public static final int SIZE_OF_ENCODING_BLOCKS = 32;

    public static final int SIZE_OF_DECODING_BLOCKS = 40;

    public static final int BITS_AMOUNT_IN_BYTE = 8;

    public static final String NULL_BYTE = "00000000";


    public static String toAscii85 (byte[] data) {
        StringAndPadAmount binaryRepresentation = convertBinary(data.toString(), SIZE_OF_ENCODING_BLOCKS / BITS_AMOUNT_IN_BYTE);

        System.out.println("to: " + binaryRepresentation.getStr());

        return calculateEncodeBlocksValues(binaryRepresentation.getStr(), binaryRepresentation.getPadAmount());
    }

    public static byte[] fromAscii85 (String data) {
        data = data.replaceAll("\\s", "");
        data = removeTagsForDecoding(data);
        StringAndPadAmount binaryRepresentation = convertBinary(data, SIZE_OF_DECODING_BLOCKS / BITS_AMOUNT_IN_BYTE);

        System.out.println("fr: " + binaryRepresentation.getStr());

        return calculateDecodeBlocksValues(binaryRepresentation.getStr(), binaryRepresentation.padAmount);

    }

    private static String removeTagsForDecoding(String str) {
        StringBuilder sb = new StringBuilder(str);
        sb.delete(0, 2);
        sb.setLength(sb.length() - 2);

        return sb.toString();
    }

    private static StringAndPadAmount convertBinary(String str, int blockSectorAmount) {
        StringBuilder binary = new StringBuilder();

        byte[] data = str.getBytes();

        for (byte b : data)
        {
            int val = b;
            for (int i = 0; i < 8; i++)
            {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        int padAmount = data.length % blockSectorAmount == 0 ? 0 : blockSectorAmount - (data.length % blockSectorAmount);

        binary.append(NULL_BYTE.repeat(padAmount));
        return new StringAndPadAmount(binary.toString(), padAmount);
    }

    private static String calculateEncodeBlocksValues(String str, int padAmount) {
        StringBuilder sb = new StringBuilder();

        sb.append("<~"); // starting symbols

        int numberOfBlocks = str.length() / SIZE_OF_ENCODING_BLOCKS;

        for (int i = 0; i < numberOfBlocks; i++) {
            int startIndex = i * SIZE_OF_ENCODING_BLOCKS;
            int endIndex = startIndex + 32;
            int number = Integer.parseInt(str.substring(startIndex, endIndex), 2);
            for (int powIndex = 4; powIndex >= 0; powIndex--) {
                int multiplyFactor = (int) Math.pow(85.0, (double)powIndex);
                int ascii85Char = (int) number / multiplyFactor;
                sb.append((char) (ascii85Char + 33));
                number -= ascii85Char * multiplyFactor;
            }
        }

        sb.setLength(sb.length() - padAmount);

        sb.append("~>"); // ending symbols

        return sb.toString();

    }

    private static byte[] calculateDecodeBlocksValues(String str, int padAmount) {
        StringBuilder resultSb = new StringBuilder();

        int numberOfBlocks = str.length() / SIZE_OF_DECODING_BLOCKS;

        for (int i = 0; i < numberOfBlocks; i++) {
            int startIndex = i * SIZE_OF_DECODING_BLOCKS;
            int endIndex = startIndex + 8;
            int blockTotal = 0;
            for (int powIndex = 4; powIndex >= 0; powIndex--) {
                int number = Integer.parseInt(str.substring(startIndex, endIndex), 2);
                int multiplyFactor = (int) Math.pow(85.0, (double)powIndex);
                int ascii85Char = number * multiplyFactor;
                blockTotal += ascii85Char * multiplyFactor;
                startIndex += 8;
                endIndex += 8;
            }
            resultSb.append(blockTotal);
        }

        resultSb.setLength(resultSb.length() - padAmount);

        return resultSb.toString().getBytes();

    }

    public static void main(String[] args) {
        String str = "somewhat difficult";

        String encodedStr = toAscii85(str.getBytes());

        System.out.println("encoded : " + encodedStr);

        byte[] decodedBytes = fromAscii85(encodedStr);

        String decodedString = new String(decodedBytes, StandardCharsets.ISO_8859_1);

        System.out.println("decoded: " + decodedString);
    }

    static class StringAndPadAmount {
        String str;
        int padAmount;

        public StringAndPadAmount(String str, int padAmount) {
            this.str = str;
            this.padAmount = padAmount;
        }

        public String getStr() {
            return str;
        }

        public int getPadAmount() {
            return padAmount;
        }
    }
}

