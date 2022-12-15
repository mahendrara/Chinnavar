/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jia Li
 */
@XmlRootElement(name = "rcsMsg")
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardXmlMsg{

    @XmlElement(name = "hdr")
    private StandardXmlHeader xmlHeader;

    @XmlElement(name = "data")
    private StandardXmlMsgData xmlData;

    public StandardXmlHeader getXmlHeader() {
        return xmlHeader;
    }

    public void setXmlHeader(StandardXmlHeader xmlHeader) {
        this.xmlHeader = xmlHeader;
    }

    public StandardXmlMsgData getXmlData() {
        return xmlData;
    }

    public void setXmlData(StandardXmlMsgData xmlData) {
        this.xmlData = xmlData;
    }

}

