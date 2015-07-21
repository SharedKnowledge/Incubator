/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace.knowledgeBase;

import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PropertyHolder;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * This is a simple standard implementation of the {@link SubSpace}. It holds
 * the base and context of the SubSpace. The description is saved as part of the
 * context and flaged with {@link #DESCRIPTION_PROPERTY} as a property.
 *
 * @author Nitros Razril (pseudonym)
 */
public class StandardSubSpace implements SubSpace
{

    /**
     * Key and Value for the property, that is set on the description
     * {@link SemanticTag}. This flags the SemanticTag inside the
     * {@link #context} topic dimension as the description for this SubSpace.
     */
    private static final String DESCRIPTION_PROPERTY = "net.sharkfw.subspace.StandardSubSpace#DESCRIPTION_PROPERTY";
    /**
     * Context that descripts this SubSpace
     */
    private final SharkCS context;

    public StandardSubSpace(final SharkCS context, final SemanticTag description)
    {
        this.context = context;
        try
        {
            setDescriptionProperty(description);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Could not initialize " + getClass().getName() + " due to Exception occurring.", ex);
        }
    }

    /**
     * Returns {@link #context}
     *
     * @return The context of this SubSpace.
     */
    @Override
    public SharkCS getContext()
    {
        return context;
    }

    /**
     * Extracts a {@link List} of {@link ContextPoint} from {@link #base}
     * described by this SubSpace.<br/>
     *
     * @return The list of ContextPoint described by this SubSpace.
     * @throws IllegalStateException If an error occurred while performing the
     * extraction.
     */
    //TODO. Implement better...
    @Override
    public Knowledge getKnowledge(final SharkKB base)
    {
        Knowledge knowledge = null;
        try
        {
            final ContextCoordinates coordinates = base.createContextCoordinates(
                    getDescription(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    SharkCS.DIRECTION_INOUT
            );
            final SharkCS extractor = base.createInterest(coordinates);
            knowledge = SharkCSAlgebra.extract(base, extractor);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Coud not get Knowledge due to Exception occurring", ex);
        }
        return knowledge;
    }

    /**
     * Gets the description.<br/>
     * The description is part of the {@link #context}. This method searches for
     * the first occurrence of a topic where the proptery of
     * {@link #DESCRIPTION_PROPERTY} makes the follwing statement true: <br/>
     * {@code DESCRIPTION_PROPERTY_KEY.equals(proptery)}<br/>
     *
     * @return The description for this SubSpace.
     * @throws IllegalStateException If no description could be found or an
     * error occourred while searching through the topics.
     */
    @Override
    public SemanticTag getDescription()
    {
        boolean found = false;
        final STSet topics = context.getTopics();
        SemanticTag description = null;
        try
        {
            final Enumeration<SemanticTag> tags = topics.tags();
            while (!found && tags.hasMoreElements())
            {
                final SemanticTag tag = tags.nextElement();
                final String descriptionProperty = tag.getProperty(DESCRIPTION_PROPERTY);
                if (DESCRIPTION_PROPERTY.equals(descriptionProperty))
                {
                    description = tag;
                    found = true;
                }
            }
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Coud not get description due to Exception occurring", ex);
        }
        // Could not find description? Something went wrong here. Class was not initialize correctly.
        if (!found)
        {
            throw new IllegalStateException("Class was not initialize correctly. Could not find description.");
        }
        return description;
    }

    /**
     * Sets {@link #DESCRIPTION_PROPERTY} as a property via
     * {@link PropertyHolder#setProperty(java.lang.String, java.lang.String)} on
     * the description inside the topics of {@link #context}.<br/>
     * If the description does not exists already, it is added to the topics of
     * the {@link #context}.
     * <br/><br/>
     * Note:<br/>
     * This method will always override the {@link #DESCRIPTION_PROPERTY}
     * property of the description.
     *
     * @param description The description to set the property for.
     * @throws SharkKBException Any error while searching the topics or adding
     * the property.
     */
    private void setDescriptionProperty(final SemanticTag description) throws SharkKBException
    {
        // Test if the topics already contain the description.
        final String[] descriptionSIs = description.getSI();
        final STSet topics = context.getTopics();
        SemanticTag propertyHolder = topics.getSemanticTag(descriptionSIs);
        // If they do not contain the description, add it.
        if (propertyHolder == null)
        {
            final String nameSIs = description.getName();
            propertyHolder = topics.createSemanticTag(nameSIs, descriptionSIs);
        }
        // Set indicator, that this is the SubSpace description. Override property in any case.
        propertyHolder.setProperty(DESCRIPTION_PROPERTY, DESCRIPTION_PROPERTY);
    }

    /**
     * Test if an object equals this object.<br/>
     * This is the case when:
     * <ol>
     * <li>The given object is an instance of {@link SubSpace}.</li>
     * <li>
     * Its description ({@link SubSpace#getDescription()}) is identical to this
     * objects description as in
     * {@link SemanticTag#identical(net.sharkfw.knowledgeBase.SemanticTag)}.#
     * </li>
     * </ol>
     *
     * @param object The object to test.
     * @return True if the given objects equals this object, as descripted
     * above.
     */
    @Override
    public boolean equals(final Object object)
    {
        boolean equals = false;
        if (object instanceof SubSpace)
        {
            final SubSpace other = (SubSpace) object;
            final SemanticTag firstSubSpaceDescription = this.getDescription();
            final SemanticTag secondSubSpaceDescription = other.getDescription();
            equals = firstSubSpaceDescription.identical(secondSubSpaceDescription);
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
        hash = 37 * hash + Objects.hashCode(this.context);
        return hash;
    }
}
