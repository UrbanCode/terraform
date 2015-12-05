package org.urbancode.terraform.tasks.helpers;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.urbancode.terraform.tasks.aws.helpers.SshHelper;

public class SshHelperTest {

    @Test(expected=IllegalArgumentException.class)
    public void isPortActiveTest() {
        SshHelper.isPortActive("127.0.0.1", 65536);
    }

}
