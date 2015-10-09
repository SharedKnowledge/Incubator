/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.knowledgeBase;

import net.sharkfw.knowledgeBase.sync.SyncKB;

/**
 *
 * @author Nitros
 */
public class SyncDescriptorSchema extends DescriptorSchema
{

    public SyncDescriptorSchema(final SyncKB syncKB)
    {
        super(syncKB);
        if (syncKB.getOwner() == null)
        {
            throw new IllegalArgumentException("Given SyncKB must have an owner!");
        }
    }

    public SyncKB getSyncKB()
    {
        return (SyncKB) this.getSharkKB();
    }

}
