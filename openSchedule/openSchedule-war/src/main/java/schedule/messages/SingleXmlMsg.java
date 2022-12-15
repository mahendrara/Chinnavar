/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule.messages;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jia Li
 */
@XmlRootElement(name = "rcsMsg")
@XmlAccessorType(XmlAccessType.FIELD)
public class SingleXmlMsg {
    @XmlElement(name = "hdr")
    private StandardXmlHeader xmlHeader;

    @XmlElementWrapper(name = "data")
    @XmlElements({
        @XmlElement(name = "ServiceDutyChange", type = ServiceDutyChangeXmlMsgData.class)
    })
    private List<StandardItem> xmlData;

    public StandardXmlHeader getXmlHeader() {
        return xmlHeader;
    }

    public void setXmlHeader(StandardXmlHeader xmlHeader) {
        this.xmlHeader = xmlHeader;
    }

    public List<StandardItem> getXmlData() {
        return xmlData;
    }

    public void setXmlData(List<StandardItem> xmlData) {
        this.xmlData = xmlData;
    }
}
