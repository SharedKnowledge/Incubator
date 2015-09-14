/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.base;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jgrundma
 */
public final class DescriptorAlgebra
{

    private DescriptorAlgebra()
    {
    }

    public static SpaceDescriptor extractDescriptor(final Iterable<SpaceDescriptor> descriptors, final String id)
    {
        SpaceDescriptor foundDescriptor = null;
        final Iterator<SpaceDescriptor> descriptorIterator = descriptors.iterator();
        while (descriptorIterator.hasNext() && foundDescriptor == null)
        {
            final SpaceDescriptor descriptor = descriptorIterator.next();
            final String descriptorId = descriptor.getId();
            if (descriptorId.equals(id))
            {
                foundDescriptor = descriptor;
            }
        }
        return foundDescriptor;
    }
}
