package org.urbancode.terraform.tasks.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.urbancode.terraform.tasks.common.util.IOUtil;

public class IOUtilTest {

    
    @Test
    public void utilTest() {
        
        IOUtil io = IOUtil.getInstance();
        OutputStream nout = IOUtil.NULL;
//        OutputStream std = System.out;
        
        List<String> command = new ArrayList<String>();
        command.add("echo");
        command.add("test");

        ProcessBuilder builder = new ProcessBuilder(command);
        
        try {
            builder.redirectErrorStream(true);
            Process proc = builder.start();
            
            InputStream pin = proc.getInputStream();
            
            System.out.println("InputStream: " + pin);
            System.out.println("OutputStream: " + nout);
            
            // print to stdout
//            IOUtil.copyStream(pin, System.out);
            
            // discard in new thread
            IOUtil.getInstance().discardStream(pin);
            int exitCode = proc.waitFor();
            System.out.println("EXIT CODE: " + exitCode);
        }
        catch (IOException e) {
            System.out.println("io fail");
        }
        catch (InterruptedException e) {
            System.out.println("interrupt");
        }
    }
}
