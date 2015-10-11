package net.sharkfw.test.descriptor;

import net.sharkfw.system.L;

/**
 * An abstract class containing useful constants.
 *
 * @author Nitros Razril (pseudonym)
 */
public abstract class AbstractDescriptorTest
{

    /**
     * Name for Alice peer.
     */
    protected static final String ALICE_NAME = "Alice";
    /**
     * SI for Alice peer.
     */
    protected static final String ALICE_SI = "http://www.sharksystem.net/alice.html";
    /**
     * Name for Bob peer.
     */
    protected static final String BOB_NAME = "Bob";
    /**
     * SI for Bob peer.
     */
    protected static final String BOB_SI = "http://www.sharksystem.net/bob.html";
    /**
     * Name for Eve peer.
     */
    protected static final String EVE_NAME = "EVE";
    /**
     * SI for eve peer.
     */
    protected static final String EVE_SI = "http://www.sharksystem.net/eve.html";
    /**
     * Name of a teapot topic.
     */
    protected static final String TEAPOT_NAME = "teapot";
    /**
     * SI of a teapot topic.
     */
    protected static final String TEAPOT_SI = "https://en.wikipedia.org/?title=Teapot";
    /**
     * Name of a Java topic.
     */
    protected static final String JAVA_NAME = "java";
    /**
     * Si of a Java topic.
     */
    protected static final String JAVA_SI = "https://www.java.com";
    /**
     * Name of a C topic.
     */
    protected static final String C_NAME = "C";
    /**
     * Si of a C topic.
     */
    protected static final String C_SI = "https://de.wikipedia.org/wiki/C_%28Programmiersprache%29";
    /**
     * Name of a Deutschland topic.
     */
    protected static final String DEUTSCHLAND_NAME = "Deutschland";
    /**
     * Si of a Deutschland topic.
     */
    protected static final String DEUTSCHLAND_SI = "https://de.wikipedia.org/wiki/Deutschland";

    /**
     * Sets the loglevel to all.
     */
    public AbstractDescriptorTest()
    {
        L.setLogLevel(L.LOGLEVEL_ALL);
    }
}
