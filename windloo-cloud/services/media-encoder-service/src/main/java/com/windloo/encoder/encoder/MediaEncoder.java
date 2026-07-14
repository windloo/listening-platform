package com.windloo.encoder.encoder;
public interface MediaEncoder {
    boolean accept(String outputFormat);
    String encode(String sourceUrl, String outputFormat);
}