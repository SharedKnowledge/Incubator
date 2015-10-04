/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.knowledgeBase;

import java.util.Objects;
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
 *
 * @author Nitros Razril (pseudonym)
 */
@XmlRootElement(name = "space")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContextSpaceDescriptor
{

    @XmlElement(name = "context")
    @XmlJavaTypeAdapter(SharkCSAdapter.class)
    private SharkCS context;
    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "parent")
    private String parent;

    private ContextSpaceDescriptor()
    {
        initialize(null, null, null);
    }

    public ContextSpaceDescriptor(final SharkCS context, final String id)
    {
        this(context, id, null);
    }

    public ContextSpaceDescriptor(final String id)
    {
        this(null, id, null);
    }

    protected ContextSpaceDescriptor(final SharkCS context, final String id, final String parent)
    {
        Objects.requireNonNull(id, "Parameter 'id' must not be null.");
        initialize(context, id, parent);
    }

    public SharkCS getContext()
    {
        return context;
    }

    public String getId()
    {
        return id;
    }

    public String getParent()
    {
        return parent;
    }

    public boolean isEmpty()
    {
        return (context == null);
    }

    public boolean hasParent()
    {
        return (parent != null);
    }

    protected void setParent(final String parentId)
    {
        this.parent = parentId;
    }

    protected void setParent(final ContextSpaceDescriptor descriptor)
    {
        this.parent = descriptor.getId();
    }

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
     * Both ids ({@link SpaceDescriptor#getId()}) are the same as in
     * {@link String#equals(java.lang.Object)}.
     * </li>
     * </ol>
     *
     * @param object The object to test.
     * @return True if the given objects equals this object, false otherwise.
     * above.
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
     * Generates the hash code for this class.
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

    private void initialize(final SharkCS context, final String id, final String parent)
    {
        this.context = context;
        this.id = id;
        this.parent = parent;
    }
}
