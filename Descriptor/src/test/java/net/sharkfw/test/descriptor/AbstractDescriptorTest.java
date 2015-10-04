/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.descriptor;

import net.sharkfw.system.L;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public abstract class AbstractDescriptorTest
{
    protected static final String ALICE_NAME = "Alice";
    protected static final String ALICE_SI = "http://www.sharksystem.net/alice.html";
    protected static final String BOB_NAME = "Bob";
    protected static final String BOB_SI = "http://www.sharksystem.net/bob.html";

    protected static final String TEAPOT_NAME = "teapot";
    protected static final String TEAPOT_SI = "https://en.wikipedia.org/?title=Teapot";
    protected static final String JAVA_NAME = "java";
    protected static final String JAVA_SI = "https://www.java.com";
    protected static final String C_NAME = "C";
    protected static final String C_SI = "https://de.wikipedia.org/wiki/C_%28Programmiersprache%29";
    protected static final String DEUTSCHLAND_NAME = "Deutschland";
    protected static final String DEUTSCHLAND_SI = "https://de.wikipedia.org/wiki/Deutschland";

    public AbstractDescriptorTest()
    {
        L.setLogLevel(L.LOGLEVEL_ALL);
    }
}
