package com.paike.adapter.parser;

import java.util.List;
import java.util.Map;

public interface DataParser<T> {

    List<T> parse(String data);

    List<T> parse(byte[] data);

    String export(List<T> data);

    boolean supports(String format);
}
