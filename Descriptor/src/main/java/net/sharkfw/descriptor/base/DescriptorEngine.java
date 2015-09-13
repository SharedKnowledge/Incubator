/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.base;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private List<SpaceDescriptor> spaceDescriptors;

    public DescriptorEngine(final SharkKB sharkKB)
    {
        try
        {
            this.sharkKB = sharkKB;
            loadSpaceDescriptor();
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Could not initialize class due to exception occurring.", ex);
        }
    }

    public List<SpaceDescriptor> getSpaceDescriptors()
    {
        return Collections.unmodifiableList(spaceDescriptors);
    }

    private void loadSpaceDescriptor() throws SharkKBException
    {
        final String xml = sharkKB.getProperty(DESCRIPTOR_LIST);
    }

}
