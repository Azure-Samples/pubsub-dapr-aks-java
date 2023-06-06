package org.azdaks.test.e2e.util;

public class Assert {
    public static void matchesStatusCode(int expectedStatusCode, int actualStatusCode, String successMessage, String errorMessage) throws Exception {
        if (actualStatusCode != expectedStatusCode) {
            Print.message(errorMessage + ", Expected: " + expectedStatusCode + ", Actual: " + actualStatusCode);
            throw new Exception(errorMessage);
        }

        Print.message(successMessage);
    }

    public static void contentContains(String expected, String actual, String successMessage, String errorMessage) throws Exception {
        if (!actual.contains(expected)) {
            Print.message(errorMessage + ", Expected: " + expected + ", Actual: " + actual);
            throw new Exception(errorMessage);
        }

        Print.message(successMessage);
    }

    public static void contentMatches(String expected, String actual, String successMessage, String errorMessage) throws Exception {
        if (!actual.matches(expected)) {
            Print.message(errorMessage + ", Expected: " + expected + ", Actual: " + actual);
            throw new Exception(errorMessage);
        }

        Print.message(successMessage);
    }

    public static void contentMatches(double expected, double actual, String successMessage, String errorMessage) throws Exception {
        if (actual != expected) {
            Print.message(errorMessage + ", Expected: " + expected + ", Actual: " + actual);
            throw new Exception(errorMessage);
        }

        Print.message(successMessage);
    }
}
