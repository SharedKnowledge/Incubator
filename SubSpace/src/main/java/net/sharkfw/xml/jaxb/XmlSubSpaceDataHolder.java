/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.xml.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;

/**
 *
 * @author jgrundma
 */
@XmlRootElement(name = "subspace")
@XmlAccessorType(XmlAccessType.FIELD)
class XmlSubSpaceDataHolder
{

    @XmlJavaTypeAdapter(SemanticTagAdapter.class)
    private SemanticTag description;
    @XmlJavaTypeAdapter(SharkCSAdapter.class)
    private SharkCS context;

    public SemanticTag getDescription()
    {
        return description;
    }

    public void setDescription(final SemanticTag description)
    {
        this.description = description;
    }

    public SharkCS getContext()
    {
        return context;
    }

    public void setContext(final SharkCS context)
    {
        this.context = context;
    }
}
