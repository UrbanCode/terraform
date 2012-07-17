package org.urbancode.terraform.tasks.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class IOUtil {
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    static private final Logger log = Logger.getLogger(IOUtil.class);
    static private final int BUFFER_SIZE = 8192;    // hangs if 0

    private static IOUtil instance = null;

    //----------------------------------------------------------------------------------------------
    public static IOUtil getInstance() {
        if (instance == null) {
            instance = new IOUtil();
        }
        return instance;
    }
    
    //----------------------------------------------------------------------------------------------
    final static public OutputStream NULL = new OutputStream() {
        @Override public void close() {}
        @Override public void flush() {}
        @Override public void write(byte[] buffer) {
            if (buffer == null) {
                throw new NullPointerException("buffer is null");
            }
        }
        @Override public void write(byte[] buffer, int offset, int length) {
            if (buffer == null) {
                throw new NullPointerException("buffer is null");
            }
            if (buffer.length < 0) {
                throw new IllegalArgumentException("buffer length < 0");
            }
            if (offset < 0) {
                throw new IndexOutOfBoundsException(String.format("offset %d < 0", offset));
            }
            if (length < 0) {
                throw new IndexOutOfBoundsException(String.format("length %d < 0", length));
            }
            if (offset > buffer.length || length > buffer.length - offset) {
                throw new IndexOutOfBoundsException(String.format("offset %d + length %d > buffer" +
                                                        " length %d", offset, length, buffer.length));
            }
        }
        @Override public void write(int byteValue) {}
    };
    
    //----------------------------------------------------------------------------------------------
    static public void copyStream(InputStream in, OutputStream out) 
    throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int count;
        while((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
    }

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    ExecutorService service = Executors.newCachedThreadPool();
    
    //----------------------------------------------------------------------------------------------
    private Runnable createNewStreamDiscarder(InputStream in) {
        Runnable result = new Runnable() {
            InputStream in;
            public Runnable withStream(InputStream in) {
                this.in = in;
                return this;
            }
            @Override
            public void run() {
                if (in == null) {
                    throw new NullPointerException("No stream specified to discard");
                }
                try {
                    IOUtil.copyStream(in, NULL);
                } catch (IOException e) {
                    log.error("Unable to copy from stream (" + in + ") to stream NULL (" + NULL + ")", e);
                }
            }
        }.withStream(in);
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------    
    public void discardStream(InputStream in) {
        service.submit(createNewStreamDiscarder(in));
    }

}
