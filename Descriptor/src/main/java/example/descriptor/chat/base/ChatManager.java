/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.descriptor.chat.base;

import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.knowledgeBase.Interest;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class ChatManager
{

    private static final String CHAT_MANAGER_ID = "example.subspace.chat.base.ChatManager#CHAT_MANAGER_ID";

    private final DescriptorSchema shema;
    private final ContextSpaceDescriptor chatManagerDescriptor;

    public ChatManager(final DescriptorSchema shema)
    {
        this.shema = shema;
        if (this.shema.getSharkKB().getOwner() == null)
        {
            throw new IllegalArgumentException("SharkKB in DescriptorShema must have an Owner.");
        }
        try
        {
            ContextSpaceDescriptor descriptor = shema.getDescriptor(CHAT_MANAGER_ID);
            if (descriptor == null)
            {
                final Interest emptyInterest = new InMemoSharkKB().createInterest();
                descriptor = new ContextSpaceDescriptor(emptyInterest, CHAT_MANAGER_ID);
                shema.saveDescriptor(descriptor);
            }
            this.chatManagerDescriptor = descriptor;
        } catch (DescriptorSchemaException | SharkKBException ex)
        {
            throw new IllegalArgumentException("Could not create Class due to Exception in DescriptorShema.", ex);
        }
    }

    public Chat getChat(final PeerSemanticTag... peers) throws DescriptorSchemaException, SharkKBException
    {
        return new Chat(shema, chatManagerDescriptor, peers);
    }
}
