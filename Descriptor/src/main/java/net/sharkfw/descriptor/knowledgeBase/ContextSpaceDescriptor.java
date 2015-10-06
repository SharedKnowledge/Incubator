package net.sharkfw.descriptor.knowledgeBase;

import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.system.L;
import net.sharkfw.xml.jaxb.SharkCSAdapter;

/**
 * This class des describes a set of Daten. As that is holds a {@link SharkCS}.
 * A ContextSpaceDescriptor can be set into an parent child realtionship with
 * the help of the {@link DescriptorSchema} class. Data can be extracted with
 * the help of the {@link DescriptorAlgebra} class. <br/><br/>
 *
 * A ContextSpaceDescriptor can be empty when the context paramter is null. In
 * this case it only describes a realtionship. <br/><br/>
 *
 * This class can be used serialized via JAXB an passed to
 * {@link JAXBContext#newInstance(java.lang.Class...)}.
 *
 * @author Nitros Razril (pseudonym)
 */
@XmlRootElement(name = "space")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContextSpaceDescriptor
{

    /**
     * The context of this descriptor.
     */
    @XmlElement(name = "context")
    @XmlJavaTypeAdapter(SharkCSAdapter.class)
    private SharkCS context;
    /**
     * The id of this descriptor. May be null if it describes no actual data.
     */
    @XmlElement(name = "id")
    private String id;
    /**
     * The id of this descriptor parent. May be null if it has o parent.
     */
    @XmlElement(name = "parent")
    private String parent;

    /**
     * Private Constructor for JAXB.
     */
    private ContextSpaceDescriptor()
    {
        initialize(null, null, null);
    }

    /**
     * Constructor for an ContextSpaceDescriptor. It will have no parent
     * initially. Save it in a {@link DescriptorSchema} and use
     * {@link DescriptorSchema#setParent(ContextSpaceDescriptor, ContextSpaceDescriptor)} for that.
     *
     * @param context The context for this descriptor. Data will be extracted
     * based on this.
     * @param id The ID of this descriptor.
     */
    public ContextSpaceDescriptor(final SharkCS context, final String id)
    {
        this(context, id, null);
    }

    /**
     * Constructor for an empty ContextSpaceDescriptor. It will describe no 
     * actual data. It will have no parentinitially. Save it in a
     * {@link DescriptorSchema} and use {@link DescriptorSchema#setParent(
     * net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor,
     * net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor)} for that.
     *
     * @param id The ID of this descriptor.
     */
    public ContextSpaceDescriptor(final String id)
    {
        this(null, id, null);
    }

    /**
     * Constructor only usable for classes of this package. Alle 3 
     * paramters can be set. Choose parent with care!
     * 
     * @param context The context for this descriptor. Data will be extracted
     * based on this.
     * @param id The ID of this descriptor.
     * @param parent The ID of praent of this descriptor.
     */
    protected ContextSpaceDescriptor(final SharkCS context, final String id, final String parent)
    {
        Objects.requireNonNull(id, "Parameter 'id' must not be null.");
        initialize(context, id, parent);
    }

    /**
     * Gets {@link #context}.
     * 
     * @return {@link #context}
     */
    public SharkCS getContext()
    {
        return context;
    }

    /**
     * Gets {@link #id}.
     * 
     * @return {@link #id}
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets {@link #parent}.
     * 
     * @return {@link #parent}
     */
    public String getParent()
    {
        return parent;
    }

    /**
     * Retruns if the descriptor is empty an will not extract actual data. <br/>
     * In more details retruns <code>context == null</code>.
     * 
     * @return True if this descriptor is empty, false otherwise.
     */
    public boolean isEmpty()
    {
        return (context == null);
    }

     /**
     * Retruns if the descriptor is has a parent.<br/>
     * In more details retruns <code>parent != null</code>.
     * 
     * @return True if this descriptor has a parent, false otherwise.
     */
    public boolean hasParent()
    {
        return (parent != null);
    }

    /**
     * Sets {@link #parent}. Only useable for classes of this package.
     * Use with care!
     * 
     * @param parentId The new value for {@link #parent}.
     */
    protected void setParent(final String parentId)
    {
        this.parent = parentId;
    }

    /**
     * Sets {@link #parent} with ID of teh given descriptor.
     * Only useable for classes of this package.
     * Use with care!
     * 
     * @param descriptor The ID of the geiven descriptor will
     * be sett as new value for {@link #parent}.
     */
    protected void setParent(final ContextSpaceDescriptor descriptor)
    {
        this.parent = descriptor.getId();
    }

    /**
     * Sets {@link #parent} to null. Clears teh parent reationship this way.
     */
    protected void clearParent()
    {
        this.parent = null;
    }

    /**
     * Test if an object equals this object.<br/>
     * This is the case when:
     * <ol>
     * <li>The given object is an instance of
     * {@link ContextSpaceDescriptor}.</li>
     * <li>
     * Both ids ({@link ContextSpaceDescriptor#getId() }) are the same as in
     * {@link String#equals(java.lang.Object)}.
     * </li>
     * </ol>
     *
     * @param object The object to test.
     * @return True if the given objects equals this object, false otherwise.
     * See above.
     */
    @Override
    public boolean equals(final Object object)
    {
        boolean equals = false;
        if (object instanceof ContextSpaceDescriptor)
        {
            final ContextSpaceDescriptor other = (ContextSpaceDescriptor) object;
            final String thisId = this.getId();
            final String otherid = other.getId();
            equals = thisId.equals(otherid);
        }
        return equals;
    }

    /**
     * Generates the hash code for this class. The hash code is based on
     * {@link #id}.
     *
     * @return Hash code for this class.
     */
    @Override
    public int hashCode()
    {
        return 259 + Objects.hashCode(id);
    }

    public boolean identical(final ContextSpaceDescriptor descriptor) throws SharkKBException
    {
        final String otherParent = descriptor.getParent();
        boolean identical = equals(descriptor);
        if (parent == null)
        {
            identical &= (otherParent == null);
        } else
        {
            identical &= parent.equals(otherParent);
        }
        final SharkCS otherContext = descriptor.getContext();
        if (context == null)
        {
            identical &= (otherContext == null);
        } else
        {
            identical &= (otherContext != null) && SharkCSAlgebra.identical(context, otherContext);
        }
        return identical;
    }

    /**
     * Turns the 3 parameter of this class into a String. The String will
     * have newlines in it. Usefull für debugging.
     * 
     * @return Zhe 3 parameter of this class as String
     */
    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        final String contextAsString = L.contextSpace2String(context);
        builder.append("ID: ").append(id).append("\n");
        builder.append("Parent: ").append(parent).append("\n");
        builder.append("Context: ").append(contextAsString);
        return builder.toString();
    }

    /**
     * Sets the 3 parameter of this class.
     * 
     * @param context Kontext to set.
     * @param id ID to set.
     * @param parent Parent to set.
     */
    private void initialize(final SharkCS context, final String id, final String parent)
    {
        this.context = context;
        this.id = id;
        this.parent = parent;
    }
}
