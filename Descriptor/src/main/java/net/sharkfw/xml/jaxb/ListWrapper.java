package net.sharkfw.xml.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A wrapper for {@link List} to be used in the {@link JAXBSerializer}.
 *
 * @author Nitros Razril (pseudonym)
 */
@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListWrapper<T>
{

    /**
     * The wrapped list.
     */
    @XmlElement(name = "element")
    private List<T> list;

    /**
     * Creates the wrapper with an empty list.
     */
    public ListWrapper()
    {
        list = new ArrayList<>();
    }

    /**
     * Creates the wrapper and wraps list.
     *
     * @param list List to wrap.
     */
    public ListWrapper(final List<T> list)
    {
        this.list = list;
    }

    /**
     * Gest the wrapped list.
     *
     * @return The wrapped list.
     */
    public List<T> getList()
    {
        return list;
    }

    /**
     * Sets the list to wrap.
     *
     * @param list List to wrap.
     */
    public void setList(final List<T> list)
    {
        this.list = list;
    }
}
