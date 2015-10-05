/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.knowledgeBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.sharkfw.kep.format.SerializerFactroy;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class DescriptorSchema
{

    private static final String DESCRIPTOR_LIST = "net.sharkfw.descriptor.knowledgeBase.DescriptorEngine#DESCRIPTOR_LIST";
    private static final JAXBSerializer SERIALIZER = SerializerFactroy.INSTANCE.getDescriptorSerializer();

    private final SharkKB sharkKB;

    public DescriptorSchema(final SharkKB sharkKB)
    {
        this.sharkKB = sharkKB;
    }

    public ContextSpaceDescriptor getDescriptor(final String id) throws DescriptorSchemaException
    {
        ContextSpaceDescriptor foundDescriptor = null;
        final Iterator<ContextSpaceDescriptor> descriptorIterator = getDescriptors().iterator();
        while (descriptorIterator.hasNext() && foundDescriptor == null)
        {
            final ContextSpaceDescriptor descriptor = descriptorIterator.next();
            final String descriptorId = descriptor.getId();
            if (descriptorId.equals(id))
            {
                foundDescriptor = descriptor;
            }
        }
        return foundDescriptor;
    }

    public Set<ContextSpaceDescriptor> getDescriptors() throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> descriptors = new HashSet<>();
        final List<ContextSpaceDescriptor> deserializedDescriptors = readDescriptors();
        descriptors.addAll(deserializedDescriptors);
        return descriptors;
    }

    public static Set<ContextSpaceDescriptor> getChildren(final ContextSpaceDescriptor descriptor, final Collection<ContextSpaceDescriptor> descriptors)
    {
        final Set<ContextSpaceDescriptor> children = new HashSet<>();
        final String id = descriptor.getId();
        for (ContextSpaceDescriptor savedDescriptor : descriptors)
        {
            final String parentId = savedDescriptor.getParent();
            if (parentId != null && id.equals(parentId))
            {
                children.add(savedDescriptor);
            }
        }
        return children;
    }

    public Set<ContextSpaceDescriptor> getChildren(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> savedDescriptors = getDescriptors();
        return getChildren(descriptor, savedDescriptors);
    }

    public static ContextSpaceDescriptor getParent(final ContextSpaceDescriptor descriptor, final Collection<ContextSpaceDescriptor> descriptors)
    {
        ContextSpaceDescriptor parent = null;
        if (descriptor.hasParent())
        {
            final String parentId = descriptor.getParent();
            for (Iterator<ContextSpaceDescriptor> iterator = descriptors.iterator(); parent == null && iterator.hasNext();)
            {
                final ContextSpaceDescriptor element = iterator.next();
                final String id = element.getId();
                if (id.equals(parentId))
                {
                    parent = element;
                }
            }
        }
        return parent;
    }

    public ContextSpaceDescriptor getParent(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> savedDescriptors = getDescriptors();
        return getParent(descriptor, savedDescriptors);
    }

    public void setParent(final ContextSpaceDescriptor descriptor, final ContextSpaceDescriptor parent) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> existingDescriptors = getDescriptors();
        final String sourceId = descriptor.getId();
        if (!existingDescriptors.contains(descriptor) || !existingDescriptors.contains(parent))
        {
            throw new DescriptorSchemaException("Given parameters do not exist in schema.");
        }
        final Set<String> testedParentIds = new HashSet<>();
        ContextSpaceDescriptor descriptorToTest = new ContextSpaceDescriptor(
                descriptor.getContext(),
                sourceId,
                parent.getId()
        );

        boolean error = false;
        while (descriptorToTest.hasParent() && !error)
        {
            final String parentId = descriptorToTest.getParent();
            final boolean success = testedParentIds.add(parentId);
            if (success && !sourceId.equals(parentId))
            {
                descriptorToTest = getParent(descriptorToTest);
            } else
            {
                error = true;
            }
        }
        if (error)
        {
            throw new DescriptorSchemaException("Adding this parent would create an loop without a root element.");
        }
        removeDescriptor(descriptor);
        descriptor.setParent(parent);
        saveDescriptor(descriptor);
    }

    public boolean saveDescriptor(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        Set<ContextSpaceDescriptor> singelton = Collections.singleton(descriptor);
        return saveDescriptors(singelton);
    }

    public boolean saveDescriptors(final Collection<ContextSpaceDescriptor> descriptors) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> existingDescriptors = getDescriptors();
        final boolean changed = existingDescriptors.addAll(descriptors);
        if (changed)
        {
            writeDescriptors(existingDescriptors);
        }
        return changed;
    }

    public boolean removeDescriptor(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        Set<ContextSpaceDescriptor> singelton = Collections.singleton(descriptor);
        return removeDescriptors(singelton);
    }

    public boolean removeDescriptors(final Collection<ContextSpaceDescriptor> descriptors) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> existingDescriptors = getDescriptors();
        final boolean changed = existingDescriptors.removeAll(descriptors);
        if (changed)
        {
            writeDescriptors(existingDescriptors);
        }
        return changed;
    }

    public void clearDescriptors() throws DescriptorSchemaException
    {
        final Collection<ContextSpaceDescriptor> empty = Collections.emptySet();
        writeDescriptors(empty);
    }

    public SharkKB getSharkKB()
    {
        return sharkKB;
    }

    public boolean contains(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Collection<ContextSpaceDescriptor> descriptors = getDescriptors();
        return descriptors.contains(descriptor);
    }

    public static boolean containsIdentical(final ContextSpaceDescriptor descriptor, final Collection<ContextSpaceDescriptor> descriptors) throws DescriptorSchemaException
    {
        boolean found = false;
        try
        {
            for (Iterator<ContextSpaceDescriptor> iterator = descriptors.iterator(); !found && iterator.hasNext();)
            {

                final ContextSpaceDescriptor element = iterator.next();
                if (element.identical(descriptor))
                {
                    found = true;
                }
            }
        } catch (SharkKBException ex)
        {
            throw new DescriptorSchemaException("Error while testing descriptor.", ex);
        }
        return found;
    }

    public boolean containsIdentical(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Collection<ContextSpaceDescriptor> descriptors = getDescriptors();
        return containsIdentical(descriptor, descriptors);
    }

    public void clearParent(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> existingDescriptors = getDescriptors();
        if (!existingDescriptors.contains(descriptor))
        {
            throw new DescriptorSchemaException("Given descriptor does not exist in schema.");
        }
        removeDescriptor(descriptor);
        descriptor.clearParent();
        saveDescriptor(descriptor);
    }

    public void addChild(final ContextSpaceDescriptor descriptor, final ContextSpaceDescriptor child) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> singelton = Collections.singleton(child);
        addChildren(descriptor, singelton);
    }

    public void addChildren(final ContextSpaceDescriptor descriptor, final Collection<ContextSpaceDescriptor> children) throws DescriptorSchemaException
    {
        for (ContextSpaceDescriptor child : children)
        {
            setParent(child, descriptor);
        }
    }

    private void writeDescriptors(final Collection<ContextSpaceDescriptor> descriptors) throws DescriptorSchemaException
    {
        try
        {
            final String xml = SERIALIZER.serializeList(descriptors);
            sharkKB.setProperty(DESCRIPTOR_LIST, xml);
        } catch (SharkKBException | JAXBException ex)
        {
            throw new DescriptorSchemaException("Error while writing Descriptors to KB.", ex);
        }
    }

    private List<ContextSpaceDescriptor> readDescriptors() throws DescriptorSchemaException
    {
        List<ContextSpaceDescriptor> deserializedDescriptors = new ArrayList<>();
        try
        {
            final String xml = sharkKB.getProperty(DESCRIPTOR_LIST);
            if (xml != null)
            {
                deserializedDescriptors = SERIALIZER.deserializeList(xml);
            }
        } catch (SharkKBException | JAXBException ex)
        {
            throw new DescriptorSchemaException("Error while reading Descriptors from KB.", ex);
        }
        return deserializedDescriptors;
    }
}
