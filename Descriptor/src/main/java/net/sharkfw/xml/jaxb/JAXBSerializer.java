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
 * This Serializer serializes classes using the JAXB technology.
 *
 * @author Nitros Razril (pseudonym)
 */
public class JAXBSerializer
{

    /**
     * Internal context used for the Serializer.
     */
    private final JAXBContext jaxbContext;

    /**
     * This will creates a {@link JAXBContext} to use for serialization. All
     * classes passed are used in the context plus the {@link ListWrapper} to
     * serialize and deserialize {@link List} of the given classes.
     *
     * @param classes Classes to serialize. See also
     * {@link JAXBContext#newInstance(Class...)}
     *
     * @throws JAXBException See {@link JAXBContext#newInstance(Class...)}
     */
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

    /**
     * Serializes an object to XML.
     *
     * @param object Object to serialize.
     * @return The object as XML.
     * @throws JAXBException Errors in the serialization process.
     */
    public String serialize(final Object object) throws JAXBException
    {
        return marshal(object);
    }

    /**
     * Deserializes an object from XML.
     *
     * @param <T> Typ of the object that the XML contains.
     * @param xml XML to deserialize.
     * @param type Typ of the object that the XML contains.
     * @return The form XML deserialized object form type type.
     * @throws JAXBException Errors in the deserialization process.
     */
    public <T> T deserialize(final String xml, Class<T> type) throws JAXBException
    {
        return unmarshal(xml, type);
    }

    /**
     * Serializes a collection of objects to XML. For this process the
     * collection is copied as a {@link List} and passed to
     * {@link #serializeList(List)}.
     *
     * @param <T> Type of the objects in the list.
     * @param collection Collection of objects to serialize.
     * @return The serialized collection as XML.
     * @throws JAXBException Errors in the serialization process.
     */
    public <T> String serializeList(final Collection<T> collection) throws JAXBException
    {
        List<T> list = new ArrayList<>(collection);
        return JAXBSerializer.this.serializeList(list);
    }

    /**
     * Deserializes as list of objects from XML.
     *
     * @param <T> Type of the objects to deserialize-
     * @param xml XML containing the list of objects in XML.
     * @return The list of objects in the XML. their type is T.
     * @throws JAXBException Errors in the deserialization process.
     */
    public <T> List<T> deserializeList(final String xml) throws JAXBException
    {
        @SuppressWarnings("unchecked")
        final ListWrapper<T> wrapper = unmarshal(xml, ListWrapper.class);
        return wrapper.getList();
    }

    /**
     * Serializes a collection of objects to XML. The list is wrapped into a
     * {@link ListWrapper} beforehand.
     *
     * @param <T> Type of the objects in the list.
     * @param list Collection of objects to serialize.
     * @return The serialized collection as XML.
     * @throws JAXBException Errors in the serialization process.
     */
    public <T> String serializeList(final List<T> list) throws JAXBException
    {
        final ListWrapper<T> wrapper = new ListWrapper<>(list);
        return marshal(wrapper);
    }

    /**
     * This method will turn an object in a String representing a XML object.
     * The {@link #jaxbContext} is used to create the {@link Marshaller}.
     *
     * @param object Object to turn into XML.
     * @return A String representing a XML document.
     * @throws JAXBException Errors in the marshal process.
     */
    private String marshal(final Object object) throws JAXBException
    {
        final Marshaller marshaller = jaxbContext.createMarshaller();
        final StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();
    }

    /**
     * This method will turn a XML document in a object of type T. The
     * {@link #jaxbContext} is used to create the {@link Unmarshaller}.
     *
     * @param <T> Type of returned object.
     * @param xml The XML document containing the object
     * @param type The type the object will be casted to.
     * @return An object of type type that is in the XML document.
     * @throws JAXBException Errors in the unmarshal process.
     */
    private <T> T unmarshal(final String xml, final Class<T> type) throws JAXBException
    {
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final StringReader reader = new StringReader(xml);
        final Object object = unmarshaller.unmarshal(reader);
        return type.cast(object);
    }
}
