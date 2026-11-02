package com.arif.smartordermanagement.network;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class SafeIntegerTypeAdapter extends TypeAdapter<Integer> {
    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
        out.value(value);
    }

    @Override
    public Integer read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return 0;
        } else if (token == JsonToken.NUMBER) {
            return in.nextInt();
        } else if (token == JsonToken.STRING) {
            String str = in.nextString().trim();
            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0;
            }
            try {
                return (int) Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1 : 0;
        } else {
            in.skipValue();
            return 0;
        }
    }
}
