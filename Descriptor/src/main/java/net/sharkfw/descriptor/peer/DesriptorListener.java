package net.sharkfw.descriptor.peer;

import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;

/**
 * An listener to notify classes when a {@link ContextSpaceDescriptor} changes.
 *
 * @author Nitros Razril (pseudonym)
 */
public interface DesriptorListener
{

    /**
     * Method called when a @link ContextSpaceDescriptor} changes.
     */
    public void onDesriptorChange();
}
