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
 * This class persists {@link ContextSpaceDescriptor} as well as holds their
 * relationships. A DescriptorSchema is a List of trees.
 *
 * @author Nitros Razril (pseudonym)
 */
public class DescriptorSchema
{

    /**
     * Key for property the list {@link ContextSpaceDescriptor} is saved on at
     * the Knowledge Base.
     */
    private static final String DESCRIPTOR_LIST = "net.sharkfw.descriptor.knowledgeBase.DescriptorEngine#DESCRIPTOR_LIST";
    /**
     * Serializer to turn {@link ContextSpaceDescriptor} into XML.
     */
    private static final JAXBSerializer SERIALIZER = SerializerFactroy.INSTANCE.getDescriptorSerializer();
    /**
     * Knowledge Base that holds the list of {@link ContextSpaceDescriptor}.
     */
    private final SharkKB sharkKB;

    /**
     * Any schema is based on a Knowledge Base. SO it needs to be passed in the
     * constructor.
     *
     * @param sharkKB This schemas Knowledge Base.
     */
    public DescriptorSchema(final SharkKB sharkKB)
    {
        this.sharkKB = sharkKB;
    }

    /**
     * Gets a ContextSpaceDescriptor by id form the schema.
     *
     * @param id The id of the ContextSpaceDescriptor to get.
     * @return The ContextSpaceDescriptor for the id or null, if none exits.
     * @throws DescriptorSchemaException If en error occurs while reading form
     * the Knowledge Base.
     */
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

    /**
     * Reads all ContextSpaceDescriptors form the underlying Knowledge Base.
     *
     * @return All ContextSpaceDescriptors form the underlying Knowledge Base.
     * @throws DescriptorSchemaException If the reading fails to to an
     * exception.
     */
    public Set<ContextSpaceDescriptor> getDescriptors() throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> descriptors = new HashSet<>();
        final List<ContextSpaceDescriptor> deserializedDescriptors = readDescriptors();
        descriptors.addAll(deserializedDescriptors);
        return descriptors;
    }

    /**
     * Gets all children of a ContextSpaceDescriptors in a collection of
     * ContextSpaceDescriptors.
     *
     * @param descriptor ContextSpaceDescriptors to get the children for.
     * @param descriptors Collection to search in.
     * @return Children of given DescriptorSchema.
     */
    public static Set<ContextSpaceDescriptor> getChildren(final ContextSpaceDescriptor descriptor, final Collection<ContextSpaceDescriptor> descriptors)
    {
        final Set<ContextSpaceDescriptor> children = new HashSet<>();
        final String id = descriptor.getId();
        for (ContextSpaceDescriptor savedDescriptor : descriptors)
        {
            final String parentId = savedDescriptor.getParent();
            if (id.equals(parentId))
            {
                children.add(savedDescriptor);
            }
        }
        return children;
    }

    /**
     * Gets all children of a ContextSpaceDescriptors in this Schema.
     *
     * @param descriptor ContextSpaceDescriptors to get the children for.
     * @return Children of given DescriptorSchema.
     * @throws DescriptorSchemaException Any error while reading from the
     * schema.
     */
    public Set<ContextSpaceDescriptor> getChildren(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> savedDescriptors = getDescriptors();
        return getChildren(descriptor, savedDescriptors);
    }

    /**
     * Gets the direct parent of a ContextSpaceDescriptor from a collection of
     * ContextSpaceDescriptors.
     *
     * @param descriptor ContextSpaceDescriptor to get the parent for.
     * @param descriptors collection to get the parent from.
     * @return The parent of ContextSpaceDescriptor or null, if it has none in
     * this collection.
     */
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

    /**
     * Gets the direct parent of a ContextSpaceDescriptor from this schema.
     *
     * @param descriptor ContextSpaceDescriptor to get the parent for.
     * @return The parent of ContextSpaceDescriptor or null, if it has none.
     * @throws DescriptorSchemaException Any error while reading from the
     * schema.
     */
    public ContextSpaceDescriptor getParent(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> savedDescriptors = getDescriptors();
        return getParent(descriptor, savedDescriptors);
    }

    /**
     * Sets a new parent for an ContextSpaceDescriptor. If the schema does not
     * contain an identical ContextSpaceDescriptor as parent and
     * descriptor.<br/><br/>
     *
     * NOTE: The to descriptor identical ContextSpaceDescriptor in the schema is
     * is overridden with the new parameters. It also tests if the tree
     * structure criteria is still met after adding setting the new parent. If
     * not, throws a exception.
     *
     * @param descriptor The ContextSpaceDescriptor to give a new parent.
     * @param parent The parent to set for descriptor.
     * @throws DescriptorSchemaException Any error while writing to the schema
     * and if the schema does not contain an identical ContextSpaceDescriptor as
     * parent and descriptor. Also throws this, if the new schema would create a
     * loop.
     */
    public void setParent(final ContextSpaceDescriptor descriptor, final ContextSpaceDescriptor parent) throws DescriptorSchemaException
    {
        if (!containsIdentical(descriptor) || !containsIdentical(parent))
        {
            throw new DescriptorSchemaException("Given parameters do not exist identical in schema.");
        }
        final String sourceId = descriptor.getId();
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

    /**
     * Gets the whole tree for a given ContextSpaceDescriptor. First searches
     * the root and then calls {@link #getSubtree(ContextSpaceDescriptor)} with
     * root as parameter.
     *
     * @param descriptor The ContextSpaceDescriptor to get the tree for.
     * @return The tree of an ContextSpaceDescriptor.
     * @throws DescriptorSchemaException Any error while reading form the
     * schema.
     */
    public Set<ContextSpaceDescriptor> getTree(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        ContextSpaceDescriptor root = descriptor;
        while (root.hasParent())
        {
            root = getParent(root);
        }
        return getSubtree(root);
    }

    /**
     * Gets the subtree of an ContextSpaceDescriptor including itself. This is
     * done via recursively calling this method.
     *
     * @param descriptor The descriptor to get the subtree for.
     * @return Subtree of a ContextSpaceDescriptor including itself.
     * @throws DescriptorSchemaException Any error while reading form the
     * schema.
     */
    public Set<ContextSpaceDescriptor> getSubtree(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> tree = new HashSet<>();
        tree.add(descriptor);
        final Set<ContextSpaceDescriptor> children = getChildren(descriptor);
        for (ContextSpaceDescriptor child : children)
        {
            final Set<ContextSpaceDescriptor> childTree = getSubtree(child);
            tree.addAll(childTree);
        }
        return tree;
    }

    /**
     * Saves the ContextSpaceDescriptor to the Knowledge Base. Will not override
     * a any ContextSpaceDescriptor with the same ID. So efficiently only saves
     * new ContextSpaceDescriptor.
     *
     * @param descriptor ContextSpaceDescriptor to save.
     * @return If successfully saved,
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     *
     * @see #overrideDescriptor(ContextSpaceDescriptor)
     */
    public boolean saveDescriptor(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        Set<ContextSpaceDescriptor> singelton = Collections.singleton(descriptor);
        return saveDescriptors(singelton);
    }

    /**
     * Saves a collection ContextSpaceDescriptor to the Knowledge Base. Will not
     * override a any ContextSpaceDescriptor with the same ID. So efficiently
     * only saves new ContextSpaceDescriptor.
     *
     * @param descriptors The collection to save.
     * @return If the schema has changed in the process.
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     *
     * @see #overrideDescriptors(Collection)
     */
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

    /**
     * Removes a ContextSpaceDescriptor form the Schema.
     *
     * @param descriptor The ContextSpaceDescriptor to remove.
     * @return If the process was successful.
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     */
    public boolean removeDescriptor(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        Set<ContextSpaceDescriptor> singelton = Collections.singleton(descriptor);
        return removeDescriptors(singelton);
    }

    /**
     * Removes a collection of ContextSpaceDescriptor form the Schema.
     *
     * @param descriptors The collection of ContextSpaceDescriptor to remove.
     * @return If the schema has changed in the process.
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     */
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

    /**
     * Forcefully Overrides a ContextSpaceDescriptor in the Schema. The
     * ContextSpaceDescriptor in the schema with the same ID will be replaced.
     * If non exits this simply adds him.
     *
     * @param descriptor The ContextSpaceDescriptor to override.
     * @return If the schema has changed.
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     */
    public boolean overrideDescriptor(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        Set<ContextSpaceDescriptor> singelton = Collections.singleton(descriptor);
        return overrideDescriptors(singelton);
    }

    /**
     * Forcefully Overrides a collection of ContextSpaceDescriptor in the
     * Schema. The ContextSpaceDescriptor in the schema with the same ID will be
     * replaced. If non exits this simply adds him.
     *
     * @param descriptors The collection of ContextSpaceDescriptor to override.
     * @return If the schema has changed.
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     */
    public boolean overrideDescriptors(final Collection<ContextSpaceDescriptor> descriptors) throws DescriptorSchemaException
    {
        removeDescriptors(descriptors);
        return saveDescriptors(descriptors);
    }

    /**
     * Deletes all ContextSpaceDescriptor in the schema. This means, the
     * underlying Knowledge Base no longe holds any ContextSpaceDescriptor.
     *
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     */
    public void clearDescriptors() throws DescriptorSchemaException
    {
        final Collection<ContextSpaceDescriptor> empty = Collections.emptySet();
        writeDescriptors(empty);
    }

    /**
     * Gets the underlying Knowledge Base.
     *
     * @return The underlying Knowledge Base.
     */
    public SharkKB getSharkKB()
    {
        return sharkKB;
    }

    /**
     * Test if a ContextSpaceDescriptor with the same ID exists in the schema.
     *
     * @param descriptor ContextSpaceDescriptor to test.
     * @return True of a ContextSpaceDescriptor with the same ID exists, false
     * otherwise.
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     */
    public boolean contains(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Collection<ContextSpaceDescriptor> descriptors = getDescriptors();
        return descriptors.contains(descriptor);
    }

    /**
     * Test if a ContextSpaceDescriptor with the same ID, parent an context
     * exists in the given collection of ContextSpaceDescriptor.
     *
     * @param descriptor ContextSpaceDescriptor to test.
     * @param descriptors ContextSpaceDescriptor to test.
     * @return True, if a ContextSpaceDescriptor with the same ID, parent an
     * context exists in the given collection of ContextSpaceDescriptor, false
     * otherwise.
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     */
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

    /**
     * Test if a ContextSpaceDescriptor with the same ID, parent an context
     * exists in the schema.
     *
     * @param descriptor ContextSpaceDescriptor to test.
     * @return True, if a ContextSpaceDescriptor with the same ID, parent an
     * context exists in the schema, false otherwise.
     * @throws DescriptorSchemaException Any error while writing to the
     * Knowledge Base.
     */
    public boolean containsIdentical(final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException
    {
        final Collection<ContextSpaceDescriptor> descriptors = getDescriptors();
        return containsIdentical(descriptor, descriptors);
    }

    /**
     * Removes a parent from a ContextSpaceDescriptor, setting its
     * {@link ContextSpaceDescriptor#parent} to null.
     *
     * @param descriptor The ContextSpaceDescriptor to remove the parent from.
     * @throws DescriptorSchemaException If the ContextSpaceDescriptor does not
     * exist in the schema
     */
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

    /**
     * Adds a child to a ContextSpaceDescriptor by simply calling
     * <code>setParent(child, descriptor)</code> of this class.
     *
     * @param descriptor The ContextSpaceDescriptor to add a child.
     * @param child the child to add
     * @throws DescriptorSchemaException
     *
     * @see #setParent(ContextSpaceDescriptor, ContextSpaceDescriptor)
     */
    public void addChild(final ContextSpaceDescriptor descriptor, final ContextSpaceDescriptor child) throws DescriptorSchemaException
    {
        final Set<ContextSpaceDescriptor> singelton = Collections.singleton(child);
        addChildren(descriptor, singelton);
    }

    /**
     * Adds a collection of ContextSpaceDescriptor as direct children to a
     * ContextSpaceDescriptor by calling
     * <code>setParent(element, descriptor) for</code> for each element in
     * the collection.
     *
     * @param descriptor he ContextSpaceDescriptor to add the children.
     * @param children The collection to add.
     * @throws DescriptorSchemaException
     */
    public void addChildren(final ContextSpaceDescriptor descriptor, final Collection<ContextSpaceDescriptor> children) throws DescriptorSchemaException
    {
        for (ContextSpaceDescriptor child : children)
        {
            setParent(child, descriptor);
        }
    }

    /**
     * Serialize a collection of ContextSpaceDescriptor as XML and sets it as
     * Property on the Knowledge base of this schema.
     * 
     * @param descriptors The collection to write.
     * @throws DescriptorSchemaException Any error in the serialization process.
     */
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

    /**
     * Reads the as XML serialized ContextSpaceDescriptor, deserialize them 
     * and returns them as list.
     * 
     * @return The deserialized ContextSpaceDescriptors as List.
     * @throws DescriptorSchemaException Any error in the deserialization process.
     */
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
