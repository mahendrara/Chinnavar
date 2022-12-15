/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author spirttin
 */
@XmlRootElement(name = "Employee")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployeeItem extends StandardItem{
    /*public enum EmployeeOperation {
        UNKNOWN,
        MODIFY,
        CREATE
    }*/
    
    @XmlElement(name = "Id")
    private Integer id;

    @XmlElement(name = "CustomerId")
    private String customerId;

    @XmlElement(name = "UserName")
    private String userName;

    @XmlElement(name = "FirstName")
    private String firstName;
    
    @XmlElement(name = "LastName")
    private String lastName;
    
    @XmlElement(name = "Availability")
    private Integer availability;

    @XmlAttribute(name = "EmployeeOperation")
    private Operation employeeOperation;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }    
    
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }    
    
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }    
    
    public Integer getAvailability() {
        return availability;
    }
    public void setAvailability(Integer availability) {
        this.availability = availability;
    }
    
    public Operation getOperation() {
        return employeeOperation;
    }
    public void setOperation(Operation employeeOperation) {
        this.employeeOperation = employeeOperation;
    }
}
