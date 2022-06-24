package com.nazeem.multidb.mongodb.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.PrimitiveIterator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FTPSettings {
    private String server;
    private int port=21;
    private String username;
    private String password;
    private String directory;
}
