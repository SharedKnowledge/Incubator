/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.subspace.chat.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

/**
 *
 * @author Nitros
 */
public class Chat
{

    private static final String CHAT_PREFIX = "example.subspace.chat.base.Chat#CHAT_PREFIX";
    private static final String DEMLIMITER = "$";

    private final ContextSpaceDescriptor chatDescriptor;
    private final DescriptorSchema schema;

    protected Chat(final DescriptorSchema schema, final ContextSpaceDescriptor parent, final PeerSemanticTag... peers) throws SharkKBException, DescriptorSchemaException
    {
        Objects.requireNonNull(schema, "DescriptorSchema must not be null.");
        this.schema = schema;
        if (peers.length < 1)
        {
            throw new IllegalArgumentException("Must pass at least one peer.");
        }
        final PeerSemanticTag owner = this.schema.getSharkKB().getOwner();
        if (owner == null)
        {
            throw new IllegalArgumentException("SharkKB in DescriptorSchema must have an Owner.");
        }
        System.out.println(owner.getName());
        final ArrayList<PeerSemanticTag> peerList = new ArrayList<>(Arrays.asList(peers));
        peerList.add(owner);
        final String chatId = buildChatId(peerList);
        System.out.println("Chat ID: " + chatId);
        ContextSpaceDescriptor descriptor = schema.getDescriptor(chatId);
        System.out.println("Hello World 001");
        if (descriptor == null)
        {
            System.out.println("Hello World 002");
            final SharkCS context = buildChatContext(chatId, peerList);
            descriptor = new ContextSpaceDescriptor(context, chatId);
        }
        schema.setParent(descriptor, parent);
        schema.saveDescriptor(descriptor);
        Objects.requireNonNull(descriptor, "Error in initialisation. Descriptor should not be null here.");
        this.chatDescriptor = descriptor;
    }

    public ChatEntry createChatEntry(final String text) throws SharkKBException
    {
        final SharkKB sharkKB = schema.getSharkKB();
        return ChatEntry.createChatEntry(chatDescriptor, sharkKB, text);
    }

    public List<ChatEntry> getChatEntries() throws SharkKBException
    {
        final List<ChatEntry> entries = new ArrayList<>();
        final SharkKB sharkKB = schema.getSharkKB();
        System.out.println(sharkKB.getOwner().getName());
        System.out.println(chatDescriptor.getId());
        Objects.requireNonNull(sharkKB, "KB null.");
        Objects.requireNonNull(chatDescriptor, "chatDescriptor null.");
        final Knowledge knowledge = SharkCSAlgebra.extract(sharkKB, chatDescriptor);
        final Enumeration<ContextPoint> contextPoints = knowledge.contextPoints();
        while (contextPoints.hasMoreElements())
        {
            final ContextPoint contextPoint = contextPoints.nextElement();
            final ChatEntry entry = new ChatEntry(contextPoint);
            entries.add(entry);
        }
        return entries;
    }

    private String buildChatId(final List<PeerSemanticTag> peers)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(CHAT_PREFIX);
        for (final PeerSemanticTag peer : peers)
        {
            final String[] addresses = peer.getAddresses();
            final String addressesAsString = Arrays.toString(addresses);
            builder.append(DEMLIMITER);
            builder.append(addressesAsString);

        }
        return builder.toString();
    }

    private SharkCS buildChatContext(final String chatId, final List<PeerSemanticTag> peers) throws SharkKBException
    {
        final PeerSemanticTag owner = schema.getSharkKB().getOwner();
        // topics
        final STSet topicsDimension = new InMemoSTSet();
        final String chatname = buildChatname(peers);
        topicsDimension.createSemanticTag(chatname, chatId);
        // peers
        final PeerSTSet peersDimension = new InMemoPeerSTSet();
        for (PeerSemanticTag peer : peers)
        {
            peersDimension.merge(peer);
        }
        // remote peers
        final PeerSTSet remotePeersDimension = new InMemoPeerSTSet();
        for (PeerSemanticTag peer : peers)
        {
            if (!peer.identical(owner))
            {
                remotePeersDimension.merge(peer);
            }
        }
        final SharkCS context = new InMemoSharkKB().createInterest(
                topicsDimension,
                owner,
                peersDimension,
                remotePeersDimension,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        return context;
    }

    private String buildChatname(List<PeerSemanticTag> peers)
    {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < peers.size(); i++)
        {
            if (i != 0)
            {
                builder.append(", ");
            }
            final String name = peers.get(i).getName();
            builder.append(name);
        }
        return builder.toString();
    }

}
