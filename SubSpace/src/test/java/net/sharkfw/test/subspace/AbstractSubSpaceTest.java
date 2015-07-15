/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.subspace;

import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.system.L;

/**
 *
 * @author jgrundma
 */
public abstract class AbstractSubSpaceTest
{
    protected static final String ALICE_NAME = "Alice";
    protected static final String ALICE_SI = "http://www.sharksystem.net/alice.html";
    protected static final String BOB_NAME = "Bob";
    protected static final String BOB_SI = "http://www.sharksystem.net/bob.html";

    protected static final String TEAPOT_NAME = "teapot";
    protected static final String TEAPOT_SI = "https://en.wikipedia.org/?title=Teapot";
    protected static final String JAVA_NAME = "java";
    protected static final String JAVA_SI = "https://www.java.com";

    protected static final STSet SEMANTIC_TAG_FACTORY = new InMemoPeerSTSet();
    protected static final SharkKB CONTEX_FACTORY = new InMemoSharkKB();

    public AbstractSubSpaceTest()
    {
        L.setLogLevel(L.LOGLEVEL_ALL);
    }
}
