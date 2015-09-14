/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 *
 * @author Nitros
 */
public class DescriptorEngine
{

    private static final String DESCRIPTOR_LIST = "net.sharkfw.descriptor.base.DescriptorEngine#DESCRIPTOR_LIST";
    private static final JAXBSerializer SERIALIZER;

    // Initialize the JAXBSerializer. This isn't great but faster than 
    // recreating the JAXBSerializer and underlying JAXBContext for every instance.
    static
    {
        try
        {
            SERIALIZER = new JAXBSerializer(SpaceDescriptor.class);
        } catch (JAXBException ex)
        {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private final SharkKB sharkKB;

    public DescriptorEngine(final SharkKB sharkKB)
    {
        this.sharkKB = sharkKB;
    }

    public Set<SpaceDescriptor> getDescriptors() throws SharkKBException, JAXBException
    {
        final Set<SpaceDescriptor> descriptors = new HashSet<>();
        final String xml = sharkKB.getProperty(DESCRIPTOR_LIST);
        if (xml != null)
        {
            final List<SpaceDescriptor> deserializedDescriptors = SERIALIZER.deserializeList(xml);
            descriptors.addAll(deserializedDescriptors);
        }
        return descriptors;
    }

    public final void saveDescriptor(final Set<SpaceDescriptor> descriptors) throws JAXBException, SharkKBException
    {
            final String xml = SERIALIZER.serializeList(descriptors);
            sharkKB.setProperty(DESCRIPTOR_LIST, xml);
  
    }
}
