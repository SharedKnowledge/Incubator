/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.subspace;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.base.SpaceDescriptor;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.system.L;
import net.sharkfw.test.util.DescriptorDummyFactory;
import net.sharkfw.xml.jaxb.JAXBSerializer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class DescriptorSerializationTest extends AbstractDescriptorTest
{

    private static final int NOT_FOUND = -1;

    @Test
    public void serializationAndDeserializationTest() throws SharkKBException, JAXBException
    {
        final List<SpaceDescriptor> descriptors = new ArrayList<>();
        final SpaceDescriptor javaDescriptor = DescriptorDummyFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        final SpaceDescriptor teapotDescriptors = DescriptorDummyFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
        teapotDescriptors.setParent(javaDescriptor);
        descriptors.add(teapotDescriptors);

        final JAXBSerializer serializer = new JAXBSerializer(SpaceDescriptor.class);
        final String descriptorsXml = serializer.serializeList(descriptors);

        L.d(descriptorsXml, this);

        final List<SpaceDescriptor> deserializedDescriptors = serializer.deserializeList(descriptorsXml);

        for (SpaceDescriptor deserializedDescriptor : deserializedDescriptors)
        {
            int index = descriptors.indexOf(deserializedDescriptor);
            if (index != NOT_FOUND)
            {
                SpaceDescriptor descriptor = descriptors.get(index);
                Assert.assertEquals(descriptor, deserializedDescriptor);
                Assert.assertEquals(descriptor.getId(), deserializedDescriptor.getId());
                Assert.assertEquals(descriptor.getParent(), deserializedDescriptor.getParent());
                final SharkCS descriptorContext = descriptor.getContext();
                final SharkCS deserializedDescriptorContext = deserializedDescriptor.getContext();
                final boolean identical = SharkCSAlgebra.identical(descriptorContext, deserializedDescriptorContext);
                Assert.assertTrue(identical);
            } else
            {
                Assert.fail("Descriptor not found in original List.");
            }

        }
    }
}
