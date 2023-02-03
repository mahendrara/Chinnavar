package com.alstom.openschedule.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.alstom.openschedule.model.Customer;
import com.alstom.openschedule.repository.CustomerRepository;




@SuppressWarnings("deprecation")
@ManagedBean(name = "customerService", eager = true)
@javax.faces.bean.SessionScoped
public class Customercontroller<T> {
	
	
	private CustomerRepository cusRep = new CustomerRepository();
	private static DataModel items = null;
//	private static Collection<Integer> ids = null;
	
	public Customercontroller(){
	
	}
	
	/* @PostConstruct
	   public void init() {
		    ids = cusRep.getCusomerIds();
		    for(Integer id :ids) {
		    	System.out.println(id);
		    }
	    }

	public Collection<Integer> getIds() {
		return ids;
	}
   
	public boolean isSelect() {
		 if(ids==null) {
			 return false;
		 }
		 else {
			 return true;
		 }
	}
	
	public String bring() {
		return null;
	} */


	public CustomerRepository getCusRep() {
		return cusRep;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DataModel findAll() {
		List <T> cus = cusRep.getCustomers();
		return  new ListDataModel(cus);
	}

	public DataModel getItems() {
		 if(items==null) {
			 System.out.println("recollectong");
			 items = findAll();
		 }
		 return items;
	}
  

    private void recreateModel() {
        items = null;
    }
    
	public String create() {
		Customer cusObj = new Customer();
		cusObj.setCreating(true);
		cusObj.setEditing(true);
	    List<Customer> oldArray = (List<Customer>) getItems().getWrappedData();
        oldArray.add(0, cusObj);
		getItems().setWrappedData(oldArray);
		return null;

	}


	public String delete(Customer customer) {
          
		if(!customer.isCreating()) {
			 cusRep.deleteCustomer(customer);
			  System.out.println("DELETE BEING CALLED");
		}
		 
		  recreateModel();
		  return "List";

	}
	
	public String cancel(Customer customer) {
		 if(customer.isCreating() || customer.isEditing()) {
			 if(customer.isEditing()) {
				 customer.setEditing(false);
			 }
			 if(customer.isCreating()) {
				 customer.setCreating(false);
			 }
			 recreateModel();
		 }
		 return "List";
	}
	
	  public String activateEdit(Customer customer) {
	        customer.setEditing(true);
	        return null;
	    }
	  
	  public boolean isAddAllowed() {
	        List<Customer> array = (List<Customer>) getItems().getWrappedData();
	        if (array == null || array.isEmpty()) {
	            return true;
	        } else {

	            Customer e = (Customer) array.get(0);
	            if (e != null && e.isCreating()) {
	                return false;
	            } else {
	                Iterator<Customer> iterator = array.iterator();
	                while (iterator.hasNext()) {
	                    Customer item = (Customer) iterator.next();
	                    if (item.isEditing()) {
	                        return false;
	                    }
	                }
	            }
	            return true;
	        }
	    }

	  
	public String save(Customer customer) {
		
		System.out.println(customer.getName());
		if(customer.isCreating() && customer.isEditing()) {
			cusRep.addCustomerInDb(customer);
			customer.setCreating(false);
			customer.setEditing(false);
		}
		if(!customer.isCreating() && customer.isEditing()){
			cusRep.updateCustomerRecord(customer);
			customer.setEditing(false);
		}
		
		return null;
	}
	
	

}
