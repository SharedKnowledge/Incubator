/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.xml.jaxb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jgrundma
 */
@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
class ListWrapper<T>
{

    @XmlElement(name = "element")
    private List<T> list;

    public ListWrapper()
    {
        list = new ArrayList<>();
    }

    public ListWrapper(final List<T> list)
    {
        this.list = list;
    }

    public List<T> getList()
    {
        return list;
    }

    public void setList(final List<T> list)
    {
        this.list = list;
    }
}
