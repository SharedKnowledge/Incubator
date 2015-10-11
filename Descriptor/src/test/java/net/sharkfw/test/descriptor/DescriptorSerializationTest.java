package net.sharkfw.test.descriptor;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.kep.format.SerializerFactroy;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.system.L;
import net.sharkfw.test.util.DummyDataFactory;
import net.sharkfw.xml.jaxb.JAXBSerializer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the serialization of {@link ContextSpaceDescriptor}.
 * 
 * @author Nitros Razril (pseudonym)
 */
public class DescriptorSerializationTest extends AbstractDescriptorTest
{

    /**
     * Returned if an index was not found.
     */
    private static final int NOT_FOUND = -1;

    /**
     * Serialize a {@link ContextSpaceDescriptor} and deserialize it
     * afterwards. Content should still be same.
     */
    @Test
    public void serializationAndDeserializationTest() throws SharkKBException, JAXBException
    {
        final List<ContextSpaceDescriptor> descriptors = new ArrayList<>();
        final ContextSpaceDescriptor javaDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        final ContextSpaceDescriptor teapotDescriptors = DummyDataFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
        descriptors.add(teapotDescriptors);

        final JAXBSerializer serializer = SerializerFactroy.INSTANCE.getDescriptorSerializer();
        final String descriptorsXml = serializer.serializeList(descriptors);

        L.d(descriptorsXml, this);

        final List<ContextSpaceDescriptor> deserializedDescriptors = serializer.deserializeList(descriptorsXml);

        for (ContextSpaceDescriptor deserializedDescriptor : deserializedDescriptors)
        {
            int index = descriptors.indexOf(deserializedDescriptor);
            if (index != NOT_FOUND)
            {
                ContextSpaceDescriptor descriptor = descriptors.get(index);
                Assert.assertEquals(descriptor, deserializedDescriptor);
                Assert.assertEquals(descriptor.getId(), deserializedDescriptor.getId());
                final SharkCS descriptorContext = descriptor.getContext();
                final SharkCS deserializedDescriptorContext = deserializedDescriptor.getContext();
                final boolean identical = SharkCSAlgebra.identical(descriptorContext, deserializedDescriptorContext);
                Assert.assertTrue(identical);
            } else
            {
                Assert.fail("Descriptor not found in original List.");
            }
        }

        final String javaDescriptorAsXML = serializer.serialize(javaDescriptor);
        Assert.assertFalse(javaDescriptorAsXML.isEmpty());
        final ContextSpaceDescriptor deserializedJavaDescriptor = serializer.deserialize(javaDescriptorAsXML, ContextSpaceDescriptor.class);
        Assert.assertTrue(javaDescriptor.identical(deserializedJavaDescriptor));
    }
}
