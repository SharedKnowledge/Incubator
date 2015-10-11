package net.sharkfw.xml.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.sharkfw.kep.format.XMLSerializer;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * A adapter so serialize {@link SharkCS} into a string. Useful for the JAXB
 * technology.
 *
 * @author Nitros Razril (pseudonym)
 */
public class SharkCSAdapter extends XmlAdapter<String, SharkCS>
{

    /**
     * Turns a String into a SharkCS. Uses
     * {@link XMLSerializer#deserializeSharkCS(String)} to d so.
     *
     * @param xml String to be turned into a SharkCS.
     * @return The unmarshaled SharkCS.
     * @throws SharkKBException Any errors in the process.
     */
    @Override
    public SharkCS unmarshal(final String xml) throws SharkKBException
    {
        final XMLSerializer serializer = new XMLSerializer();
        return serializer.deserializeSharkCS(xml);
    }

    /**
     * Turns a SharkCS into a String using
     * {@link XMLSerializer#serializeSharkCS(SharkCS)}.
     *
     * @param context SharkCS to turn into s String.
     * @return SharkCS as a String.
     * @throws SharkKBException Any errors in the process.
     */
    @Override
    public String marshal(final SharkCS context) throws SharkKBException
    {
        final XMLSerializer serializer = new XMLSerializer();
        return serializer.serializeSharkCS(context);
    }

}
