/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.kep.format;

import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 *
 * @author Nitros
 */
public enum SerializerFactroy
{

    INSTANCE;

    private JAXBSerializer descriptorSerializer;

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

    public JAXBSerializer getDescriptorSerializer()
    {
        return descriptorSerializer;
    }
}
