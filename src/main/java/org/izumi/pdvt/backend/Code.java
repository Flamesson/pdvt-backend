package org.izumi.pdvt.backend;

import java.util.Base64;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Code {
    private static final String PROTOCOL = "http://";

    private final String raw;
    private boolean initialized = false;
    private String decoded;
    private String codeword;
    private String address;
    private String password;

    public String getRaw() {
        return raw;
    }

    public String getCodeword() {
        if (!initialized) {
            decode();
        }

        return codeword;
    }

    public String getAddress() {
        if (!initialized) {
            decode();
        }

        return address;
    }

    public String getServer() {
        if (!initialized) {
            decode();
        }

        if (!address.startsWith(PROTOCOL)) {
            return PROTOCOL + address;
        } else {
            return address;
        }
    }

    public boolean hasPassword() {
        if (!initialized) {
            decode();
        }

        return Objects.nonNull(password) && !password.isBlank();
    }

    public String getPassword() {
        if (!initialized) {
            decode();
        }

        return password;
    }

    private void decode() {
        this.decoded = new String(Base64.getDecoder().decode(raw));

        final int firstLengthFrom = 0;
        final String lengthFirstString = decoded.substring(firstLengthFrom, decoded.indexOf('.', firstLengthFrom));
        final int secondLengthFrom = lengthFirstString.length() + 1;
        final String lengthSecondString = decoded.substring(secondLengthFrom, decoded.indexOf('.', secondLengthFrom));
        final int thirdLengthFrom = lengthFirstString.length() + 1 + lengthSecondString.length() + 1;
        final String lengthThirdString = decoded.substring(thirdLengthFrom, decoded.indexOf('.', thirdLengthFrom));

        final int encodedFrom = lengthFirstString.length() + lengthSecondString.length() + lengthThirdString.length() + 3;

        final int lengthFirst = Integer.parseInt(lengthFirstString);
        final int lengthSecond = Integer.parseInt(lengthSecondString);
        final int lengthThird = Integer.parseInt(lengthThirdString);

        final int firstFrom = encodedFrom;
        final int secondFrom = encodedFrom + lengthFirst;
        final int thirdFrom = encodedFrom + lengthFirst + lengthSecond;

        this.codeword = decoded.substring(firstFrom, firstFrom + lengthFirst);
        this.address = decoded.substring(secondFrom, secondFrom + lengthSecond);
        this.password = decoded.substring(thirdFrom, thirdFrom +  lengthThird);

        this.initialized = true;
    }
}
