package org.azdaks.test.e2e.util;

public class Print {

    public static void section(String title) {
        System.out.println("-------------------------------------------------");
        System.out.println("🧪 " + title + " 🧪");
        System.out.println("-------------------------------------------------");
    }

    public static void message(String message) {
        System.out.println(message + "\n");
    }

    public static void response(String response) {
        System.out.println("\t🔬 Response\n\n\t✨️ " + response + "\n");
    }

    public static void request(String request) {
        System.out.println("\t🚚 Request\n\n\t✨️ " + request + "\n");
    }
}
