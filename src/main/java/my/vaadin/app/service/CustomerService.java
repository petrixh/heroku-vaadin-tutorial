package my.vaadin.app.service;

import java.util.List;

import my.vaadin.app.Customer;

public interface CustomerService {

	/**
	 * @return all available Customer objects.
	 */
	List<Customer> findAll();

	/**
	 * Finds all Customer's that match given filter.
	 *
	 * @param stringFilter
	 *            filter that returned objects should match or null/empty string
	 *            if all objects should be returned.
	 * @return list a Customer objects
	 */
	List<Customer> findAll(String stringFilter);

	/**
	 * Finds all Customer's that match given filter and limits the resultset.
	 *
	 * @param stringFilter
	 *            filter that returned objects should match or null/empty string
	 *            if all objects should be returned.
	 * @param start
	 *            the index of first result
	 * @param maxresults
	 *            maximum result count
	 * @return list a Customer objects
	 */
	List<Customer> findAll(String stringFilter, int start, int maxresults);

	/**
	 * @return the amount of all customers in the system
	 */
	long count();

	/**
	 * Deletes a customer from a system
	 *
	 * @param value
	 *            the Customer to be deleted
	 */
	void delete(Customer value);

	/**
	 * Persists or updates customer in the system. Also assigns an identifier
	 * for new Customer instances.
	 *
	 * @param entry
	 */
	void save(Customer entry);

}