package com.jervisffb.fumbbl.net;

import com.eclipsesource.json.JsonValue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Class responsible for saving the websocket messages in the FUMBBL client to disk for further analysis.
 *
 * The interface for this class is intended to be as simple as possible to make it easier to inject into
 * the FUMBBL jar files.
 *
 * This class is being injected "as-is", i.e. it doesn't bring any dependencies that are not present already
 * in the FUMBBL client. So keep the code simple.
 */
public class FumbblDebugger {

    private static Writer writer;

    static {
        try {
            writer = new FileWriter("websocket-traffic.fumbbl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handle messages being sent from the Client to Server.
     */
    public static void handleClientMessage(JsonValue value) {
        if (value == null) {
            return;
        }
        System.out.println(value);
        try {
            value.writeTo(writer);
            writer.write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handle messages being sent from the Server to the Client.
     */
    public static void handleServerMessage(JsonValue value) {
        if (value == null) {
            return;
        }
        System.out.println(value);
        try {
            value.writeTo(writer);
            writer.write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

