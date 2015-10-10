package net.sharkfw.descriptor.knowledgeBase;

import net.sharkfw.knowledgeBase.sync.SyncKB;

/**
 * A {@link DescriptorSchema} usable for synchronisation. It is always based
 * on a {@link SyncKB} which alway has an owner.
 *
 * @author Nitros Razril (pseudonym)
 */
public class SyncDescriptorSchema extends DescriptorSchema
{

    /**
     * Constructor only accepts {@link SyncKB} and throws an exception if it
     * has no owner.
     * 
     * @param syncKB 
     * @throws IllegalArgumentException If syncKB has no owner.
     */
    public SyncDescriptorSchema(final SyncKB syncKB)
    {
        super(syncKB);
        if (syncKB.getOwner() == null)
        {
            throw new IllegalArgumentException("Given SyncKB must have an owner!");
        }
    }

    /**
     * Get the underlying Knowledge Base as {@link SyncKB}.
     * 
     * @return The underlying Knowledge Base as {@link SyncKB}.
     */
    public SyncKB getSyncKB()
    {
        return (SyncKB) this.getSharkKB();
    }

}
