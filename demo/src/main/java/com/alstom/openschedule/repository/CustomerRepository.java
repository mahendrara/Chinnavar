package com.alstom.openschedule.repository;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.alstom.openschedule.HibernateUtil;
import com.alstom.openschedule.model.Customer;

import javax.persistence.criteria.CriteriaQuery;
 

public class CustomerRepository<T>{

	 private static Transaction transObj;
	    private static Session sessionObj = HibernateUtil.getSessionFactory().openSession();
	 
	    // Method To Add New customer Details In Database
	    public void addCustomerInDb(Customer cusObj) {        
	        try {
	        	System.out.println("add method");
	            transObj = sessionObj.beginTransaction();
	            sessionObj.save(cusObj);
	        } catch (Exception exceptionObj) {
	            exceptionObj.printStackTrace();
	        } finally {
	            transObj.commit();
	        }
	    }
	    
	    public List<T> getCusomerIds() {
	    	 List<T> customersIds = null; 
	    	  try {
		        	System.out.println("add method");
		            transObj = sessionObj.beginTransaction();
		            Query query =  sessionObj.createQuery("SELECT id FROM tb_customer");
		            customersIds = query.getResultList();
		        } catch (Exception exceptionObj) {
		            exceptionObj.printStackTrace();
		        } finally {
		            transObj.commit();
		        }
	    	  return customersIds;
	    }
	    // Method To Delete A customer Student Record From The Database
	    public void deleteCustomer(T entity) {
	        try {
	            transObj = sessionObj.beginTransaction();
	            sessionObj.remove(sessionObj.merge(entity)); 
	        } catch (Exception exceptionObj) {
	            exceptionObj.printStackTrace();
	        } finally {
	            transObj.commit();
	        }
	    }
	    
	    
	    // Method To Fetch every customer Details From The Database
	    @SuppressWarnings({ "unchecked", "unused" })
	    public List<T> getCustomers() { 
	    	
	        List<T> customers = null;           
	        try {
	            transObj = sessionObj.beginTransaction();
	            CriteriaQuery cq = sessionObj.getCriteriaBuilder().createQuery();
	            cq.select(cq.from(Customer.class));
	            customers = sessionObj.createQuery(cq).getResultList();
	           
	        } catch(Exception exceptionObj) {
	            exceptionObj.printStackTrace();
	        } finally {
	            transObj.commit();
	        }
	        return customers;
	    }
	 
	    // Method To Update Particular customer Details In The Database  
	    public void updateCustomerRecord(Customer entity) {
	        try {
	            transObj = sessionObj.beginTransaction();
	            sessionObj.merge(entity);        
	        } catch(Exception exceptionObj){
	            exceptionObj.printStackTrace();
	        } finally {
	            transObj.commit();
	        }
	    }
}
