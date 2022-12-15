/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficnotification.servermsgs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jiali
 */
@XmlRootElement(name = "Hdr")
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardXMLHdr {

    @XmlElement(name = "SchemaV")
    private String schemaVersion;
    @XmlElement(name = "replyTo")
    private String replyTo;
    @XmlElement(name = "Application")
    private String application;

    public StandardXMLHdr() {
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    ///@XmlElement(name = "replyTo")
    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
