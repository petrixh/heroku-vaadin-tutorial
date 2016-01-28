package my.vaadin.app;

import java.io.IOException;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.event.ShortcutAction.KeyCode;

public class CustomerForm extends CustomerFormDesign {
	
	private static final long serialVersionUID = 4667761926311526405L;
	private transient CustomerService service = CustomerService.getInstance();
	private Customer customer;
	private MyUI parent;
	
	public CustomerForm(MyUI myUI) {
		this.parent = myUI;
		save.addClickListener(e->this.save());
		delete.addClickListener(e->this.delete());
		status.removeAllItems(); // Remove demo data assigned by Designer
		status.addItems(CustomerStatus.values());
		save.setClickShortcut(KeyCode.ENTER);
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
		// Show delete button for only customers already in the database
		delete.setVisible(customer.isPersisted());
		BeanFieldGroup.bindFieldsUnbuffered(customer, this);
		setVisible(true);
		firstName.selectAll();
	}
	
	private void delete() {
		service.delete(customer);
		parent.updateList();
	}

	protected void save() {
		service.save(customer);
		parent.updateList();
	}
	
	private void writeObject(java.io.ObjectOutputStream out)
		     throws IOException{
		System.out.println("Serializing instance of class: " + this.getClass().getSimpleName());
		out.defaultWriteObject();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		System.out.println("Deserializing " + this.getClass().getSimpleName());
		service = CustomerService.getInstance(); 
	}

}
