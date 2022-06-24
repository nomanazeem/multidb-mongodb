package com.nazeem.multidb.mongodb.util;

import java.io.File;
import java.io.IOException;

public interface IFeedsActions {
    Object fetch(Object o);
    Object convert(Object o);
    File save(Object o);
    boolean upload(File file);
    boolean move(File file) throws IOException;
}
