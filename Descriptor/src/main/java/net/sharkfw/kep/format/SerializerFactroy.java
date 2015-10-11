package net.sharkfw.kep.format;

import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 * A factory class as an singleton holden different {@link JAXBSerializer} to
 * reuse to prevent overload in initializing them.
 *
 * @author Nitros Razril (pseudonym)
 */
public enum SerializerFactroy
{

    /**
     * The singleton instance.
     */
    INSTANCE;
    /**
     * {@link JAXBSerializer} initialized for {@link ContextSpaceDescriptor}.
     */
    private JAXBSerializer descriptorSerializer;

    /**
     * Initializes all {@link JAXBSerializer} to use later. Preventing overload
     * in the process.
     */
    private SerializerFactroy()
    {
        try
        {
            descriptorSerializer = new JAXBSerializer(ContextSpaceDescriptor.class);
        } catch (JAXBException ex)
        {
            throw new IllegalStateException("Internal error. Could not create JAXBSerializer.", ex);
        }
    }

    /**
     * Gets a serializer to serialize {@link ContextSpaceDescriptor}.
     * 
     * @return A serializer to serialize {@link ContextSpaceDescriptor}.
     */
    public JAXBSerializer getDescriptorSerializer()
    {
        return descriptorSerializer;
    }
}
