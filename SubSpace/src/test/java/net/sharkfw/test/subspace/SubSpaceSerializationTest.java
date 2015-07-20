/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.subspace;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpaceFactory;
import net.sharkfw.test.util.SubSpaceDummyFactory;
import net.sharkfw.xml.jaxb.JAXBSerializer;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jgrundma
 */
public class SubSpaceSerializationTest extends AbstractSubSpaceTest
{

    @Test
    public void serializationAndDeserializationTest() throws SharkKBException, JAXBException
    {
        final List<SubSpace> subspaces = new ArrayList<>();
        final SubSpaceFactory factory = SubSpaceDummyFactory.getStandardSubSpaceFactory();
        final SubSpace javaSS = SubSpaceDummyFactory.createSimpleSubSpace(JAVA_NAME, JAVA_SI);
        subspaces.add(javaSS);
        final SubSpace teapotSS = SubSpaceDummyFactory.createSimpleSubSpace(TEAPOT_NAME, TEAPOT_SI);
        subspaces.add(teapotSS);

        final JAXBSerializer serializer = JAXBSerializer.INSTANCE;
        final String javaXml = serializer.serializeSubSpace(javaSS);
        final String teapotXml = serializer.serializeSubSpace(teapotSS);
        final String listXml = serializer.serializeSubSpaceList(subspaces);

        final SubSpace deserializedJavaSS = serializer.deserializeSubSpace(javaXml, factory);
        final SubSpace deserializedTeapotSS = serializer.deserializeSubSpace(teapotXml, factory);
        final List<SubSpace> deserializedSubspaces = serializer.deserializeSubSpaceList(listXml, factory);

        Assert.assertThat(deserializedJavaSS, CoreMatchers.is(javaSS));
        Assert.assertThat(deserializedTeapotSS, CoreMatchers.is(teapotSS));
        Assert.assertThat(deserializedJavaSS, CoreMatchers.not(teapotSS));
        Assert.assertThat(deserializedTeapotSS, CoreMatchers.not(javaSS));
        Assert.assertTrue(subspaces.contains(deserializedJavaSS));
        Assert.assertTrue(subspaces.contains(deserializedTeapotSS));
        Assert.assertEquals(deserializedSubspaces.size(), subspaces.size());
        Assert.assertTrue(deserializedSubspaces.contains(javaSS));
        Assert.assertTrue(deserializedSubspaces.contains(teapotSS));
        Assert.assertTrue(deserializedSubspaces.contains(deserializedJavaSS));
        Assert.assertTrue(deserializedSubspaces.contains(deserializedTeapotSS));
    }
}
