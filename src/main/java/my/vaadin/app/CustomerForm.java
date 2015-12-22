package my.vaadin.app;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.event.ShortcutAction.KeyCode;

public class CustomerForm extends CustomerFormDesign {
	
	CustomerService service = CustomerService.getInstance();
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

}
