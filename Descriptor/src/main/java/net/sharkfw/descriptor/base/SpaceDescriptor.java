/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.base;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.xml.jaxb.SharkCSAdapter;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
@XmlRootElement(name = "space")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpaceDescriptor
{

    @XmlElement(name = "context")
    @XmlJavaTypeAdapter(SharkCSAdapter.class)
    private SharkCS context;
    @XmlElement(name = "id")
    private String id;
    @XmlElementWrapper(name = "children")
    @XmlElement(name = "child")
    private Set<String> children;
    @XmlElement(name = "parent")
    private String parent;

    public SpaceDescriptor()
    {
        children = new HashSet<>();
    }

    public SpaceDescriptor(final SharkCS context, final String id)
    {
        this();
        this.context = context;
        this.id = id;

    }

    public SharkCS getContext()
    {
        return context;
    }

    public String getId()
    {
        return id;
    }

    public Set<String> getChildren()
    {
        return Collections.unmodifiableSet(children);
    }
    
    public String getParent()
    {
        return parent;
    }

    public boolean addChild(SpaceDescriptor descriptor)
    {
        final String descriptorId = descriptor.getId();
        boolean success = children.add(descriptorId);
        if (success)
        {
            descriptor.setParent(id);
        }
        return success;
    }

    public boolean removeChild(SpaceDescriptor descriptor)
    {
        final String descriptorId = descriptor.getId();
        boolean success = children.remove(descriptorId);
        if (success)
        {
            descriptor.setParent(null);
        }
        return success;
    }

    public int getChildrenCount()
    {
        return children.size();
    }

    public boolean hasChildren()
    {
        return !children.isEmpty();
    }

    public boolean containsChild(SpaceDescriptor descriptor)
    {
        final String otherId = descriptor.getId();
        return children.contains(otherId);
    }

    protected void setParent(String parentId)
    {
        this.parent = parentId;
    }

    /**
     * Test if an object equals this object.<br/>
     * This is the case when:
     * <ol>
     * <li>The given object is an instance of {@link SpaceDescriptor}.</li>
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
        if (object instanceof SpaceDescriptor)
        {
            final SpaceDescriptor other = (SpaceDescriptor) object;
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
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(id);
        return hash;
    }
}
