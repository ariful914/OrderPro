package com.arif.smartordermanagement.network;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class SafeDoubleTypeAdapter extends TypeAdapter<Double> {
    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        out.value(value);
    }

    @Override
    public Double read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return 0.0;
        } else if (token == JsonToken.NUMBER) {
            return in.nextDouble();
        } else if (token == JsonToken.STRING) {
            String str = in.nextString().trim();
            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0.0;
            }
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1.0 : 0.0;
        } else {
            in.skipValue();
            return 0.0;
        }
    }
}
