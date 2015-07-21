/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.xml.jaxb;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpaceFactory;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public enum JAXBSerializer
{

    INSTANCE;

    private final Class<?> CLASSES[] =
    {
        XmlSubSpaceDataHolder.class,
        ListWrapper.class
    };

    private final JAXBContext jaxbContext;

    private JAXBSerializer()
    {
        try
        {
            this.jaxbContext = JAXBContext.newInstance(CLASSES);
        } catch (JAXBException ex)
        {
            throw new IllegalStateException("Could not create class " + getClass().getName() + " due to exception occurring.", ex);
        }
    }

    public String serializeSubSpace(final SubSpace subSpace) throws JAXBException
    {
        final XmlSubSpaceDataHolder dataHolder = Converter.convertSubSpace(subSpace);
        return marshal(dataHolder);
    }

    public SubSpace deserializeSubSpace(final String xml, final SubSpaceFactory factory) throws JAXBException
    {
        final XmlSubSpaceDataHolder dataHolder = unmarshal(xml, XmlSubSpaceDataHolder.class);
        return Converter.convertSubSpaceDataHolder(dataHolder, factory);
    }

    public String serializeSubSpaceList(final List<SubSpace> subspaces) throws JAXBException
    {
        final List<XmlSubSpaceDataHolder> list = Converter.convertSubSpaceList(subspaces);
        return serializeList(list);
    }

    public List<SubSpace> deserializeSubSpaceList(final String xml, final SubSpaceFactory factory) throws JAXBException
    {
        final ListWrapper<XmlSubSpaceDataHolder> wrapper = unmarshal(xml, ListWrapper.class);
        final List<XmlSubSpaceDataHolder> list = wrapper.getList();
        return Converter.convertSubSpaceDataHolderList(list, factory);
    }

    private String serializeList(final List list) throws JAXBException
    {
        final ListWrapper wrapper = new ListWrapper(list);
        return marshal(wrapper);
    }

    private String marshal(final Object object) throws JAXBException
    {
        final Marshaller marshaller = jaxbContext.createMarshaller();
        final StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();
    }

    private <T> T unmarshal(final String xml, final Class<T> type) throws JAXBException
    {
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final StringReader reader = new StringReader(xml);
        final Object object = unmarshaller.unmarshal(reader);
        return type.cast(object);
    }
}
