package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SshInteractiveSudoConnectionOptionsTest {

    private ConnectionOptions connectionOptions;

    @BeforeClass
    public void init() {
        connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SFTP);
        connectionOptions.set(OPERATING_SYSTEM, UNIX);
        connectionOptions.set(ADDRESS, "nowhere.example.com");
        connectionOptions.set(USERNAME, "some-user");
        connectionOptions.set(PASSWORD, "foo");
        connectionOptions.set(SUDO_USERNAME, "some-other-user");
        connectionOptions.set(ALLOCATE_DEFAULT_PTY, true);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("resource")
    public void shouldNotAcceptPasswordPromptRegexWithWildcardStar() {
        ConnectionOptions options = new ConnectionOptions(connectionOptions);
        options.set(SUDO_PASSWORD_PROMPT_REGEX, "assword*");
        new SshInteractiveSudoConnection(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("resource")
    public void shouldNotAcceptPasswordPromptRegexWithWildcardQuestion() {
        ConnectionOptions options = new ConnectionOptions(connectionOptions);
        options.set(SUDO_PASSWORD_PROMPT_REGEX, "assword?");
        new SshInteractiveSudoConnection(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
    }

    @Test
    @SuppressWarnings("resource")
    public void shouldAcceptPasswordPromptRegex() {
        ConnectionOptions options = new ConnectionOptions(connectionOptions);
        options.set(SUDO_PASSWORD_PROMPT_REGEX, "[Pp]assword.*:");
        new SshInteractiveSudoConnection(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
    }

    @Test
    public void shouldSetAllocatePtyIfNoDefaultPtyAndNoPtySet() throws NoSuchFieldException, IllegalAccessException {
        ConnectionOptions options = new ConnectionOptions(connectionOptions);
        options.set(ALLOCATE_DEFAULT_PTY, false);
        options.set(ALLOCATE_PTY, null);
        SshInteractiveSudoConnection sshInteractiveSudoConnection = new SshInteractiveSudoConnection(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
        Field allocatePty = SshConnection.class.getDeclaredField("allocatePty");
        allocatePty.setAccessible(true);
        assertThat((String) allocatePty.get(sshInteractiveSudoConnection), equalTo("vt220:80:24:0:0"));
    }
}
