/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace.knowledgeBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public final class SubSpaceAlgebra
{

    public static final String SUBSPACES_PROPERTY = "net.sharkfw.subspace.SubSpaceAlgebra#SUBSPACES_PROPERTY";

    private final SubSpaceFactory factory;
    private final SharkKB knowledgeBase;

    public SubSpaceAlgebra(final SubSpaceFactory factory, final SharkKB knowledgeBase)
    {
        this.factory = factory;
        this.knowledgeBase = knowledgeBase;
    }

    public List<SubSpace> extractSubSpaces() throws SharkKBException, JAXBException
    {
        List<SubSpace> subSpaces = new ArrayList<>();
        final String subspaceListXml = knowledgeBase.getProperty(SUBSPACES_PROPERTY);
        if (subspaceListXml != null)
        {
            final JAXBSerializer serializer = JAXBSerializer.INSTANCE;
            subSpaces = serializer.deserializeSubSpaceList(subspaceListXml, factory);
        }
        return subSpaces;
    }

    public List<SubSpace> assimilateSubSpace(final Collection<SubSpace> subSpaces) throws SharkKBException, JAXBException
    {
        final List<SubSpace> savedSpaces = extractSubSpaces();
        for (SubSpace subSpace : subSpaces)
        {
            if (!savedSpaces.contains(subSpace))
            {
                savedSpaces.add(subSpace);
            }
        }
        setSubSpaces(savedSpaces);
        return savedSpaces;
    }

    public void assimilateSubSpace(final SubSpace subSpaces) throws SharkKBException, JAXBException
    {
        final Set<SubSpace> singelton = Collections.singleton(subSpaces);
        assimilateSubSpace(singelton);
    }

    public boolean removeSubSpace(final SubSpace subSpace) throws SharkKBException, JAXBException
    {
        final List<SubSpace> savedSpaces = extractSubSpaces();
        final boolean removed = savedSpaces.remove(subSpace);
        setSubSpaces(savedSpaces);
        return removed;
    }

    private void setSubSpaces(final List<SubSpace> list) throws SharkKBException, JAXBException
    {
        final JAXBSerializer serializer = JAXBSerializer.INSTANCE;
        final String xml = serializer.serializeSubSpaceList(list);
        knowledgeBase.setProperty(SUBSPACES_PROPERTY, xml);
    }
}
