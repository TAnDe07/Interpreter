package edu.ufl.cise.plcsp23;

public class NumLitToken {

    default:
            if (isDigit(c)) {
        number();
    } else {
        error(line, "Unexpected character.");
    }
        break;
}


