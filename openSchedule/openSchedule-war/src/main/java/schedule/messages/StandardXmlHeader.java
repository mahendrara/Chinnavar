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
@XmlRootElement(name = "hdr")
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardXmlHeader {
    @XmlElement(name = "schema")
    private String schema;
    @XmlElement(name = "sender")
    private String sender;
    @XmlElement(name = "utc")
    private String utc;
    @XmlElement(name = "replyTo")
    private String replyTo;

    public StandardXmlHeader() {
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String replyTo) {
        this.sender = replyTo;
    }

    public String getUtc() {
        return utc;
    }

    public void setUtc(String utc) {
        this.utc = utc;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}
