/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.xml.jaxb;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class JAXBSerializer
{

    private final JAXBContext jaxbContext;

    public JAXBSerializer(final Class<?>... classes) throws JAXBException
    {
        final List<Class<?>> classesToBeBoundAsList = new ArrayList<>();
        Collections.addAll(classesToBeBoundAsList, classes);
        // Add internal classes to be bound here.
        classesToBeBoundAsList.add(ListWrapper.class);
        Class<?>[] classesToBeBound = new Class<?>[classesToBeBoundAsList.size()];
        classesToBeBound = classesToBeBoundAsList.toArray(classesToBeBound);
        this.jaxbContext = JAXBContext.newInstance(classesToBeBound);
    }
    
    public String serialize(final Object object) throws JAXBException
    {
        return marshal(object);
    }
    
    public <T> T deserialize(final String xml, Class<T> type) throws JAXBException
    {
        return unmarshal(xml, type);
    }

    public <T> String serializeList(final Collection<T> collection) throws JAXBException
    {
        List<T> list = new ArrayList<>(collection);
        return JAXBSerializer.this.serializeList(list);
    }

    public <T> List<T> deserializeList(final String xml) throws JAXBException
    {
        @SuppressWarnings("unchecked")
        final ListWrapper<T> wrapper = unmarshal(xml, ListWrapper.class);
        return wrapper.getList();
    }

    public <T> String serializeList(final List<T> list) throws JAXBException
    {
        final ListWrapper<T> wrapper = new ListWrapper<>(list);
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
