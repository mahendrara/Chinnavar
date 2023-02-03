package com.alstom.openschedule.model;

import javax.persistence.Transient;

public class Editable {
	   @Transient
	    private boolean editing = false;
	    @Transient
	    private boolean creating = false;
	    @Transient
	    private boolean removing = false;
		public boolean isEditing() {
			return editing;
		}
		public void setEditing(boolean editing) {
			this.editing = editing;
		}
		public boolean isCreating() {
			return creating;
		}
		public void setCreating(boolean creating) {
			this.creating = creating;
		}
		public boolean isRemoving() {
			return removing;
		}
		public void setRemoving(boolean removing) {
			this.removing = removing;
		}
	    
	    
}
