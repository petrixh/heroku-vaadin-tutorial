package my.vaadin.app.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import my.vaadin.app.Customer;
import my.vaadin.app.CustomerStatus;

/**
 * Minimalistic back-end implementation that talks to a PostgreSQL database
 * using plain JDBC. No locking/versioning logic etc. just for demonstration
 * purposes on the Heroku platform.
 * 
 */
public class CustomerServicePostgres implements CustomerService {

	private static Logger log = Logger.getLogger(CustomerServicePostgres.class.getName());

	private static CustomerService instance;

	private String databaseURL = "jdbc:postgresql://127.0.0.1:5432/herokutestdb";
	private String dbUserName = "vaadin";
	private String dbPassword = "";

	private final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS customer (" + "id SERIAL PRIMARY KEY, "
			+ "firstname VARCHAR(100) DEFAULT NULL," + "lastname VARCHAR(100) DEFAULT NULL,"
			+ "status VARCHAR(100) DEFAULT NULL," + "email VARCHAR(100) DEFAULT NULL," + "birthdate date DEFAULT NULL"
			+ ");";

	private static final String SELECT_ALL_CUSTOMERS = "SELECT * FROM customer";
	private static final String SELECT_ALL_CUSTOMERS_WHERE = "SELECT * FROM customer WHERE firstname LIKE ? OR lastname LIKE ?";

	// Prepared statements
	private static final String DELETE_FROM_CUSTOMER_WHERE_ID = "DELETE FROM customer WHERE id=?";
	private static final String INSERT_TO_CUSTOMER_SQL = "INSERT INTO customer (firstname, lastname,status,email, birthDate) values(?,?,?,?,?);";
	private static final String UPDATE_CUSTOMER_SQL = "UPDATE customer SET firstname=?,lastname=?,status=?,email=? ,birthDate=? WHERE id=? ;";
	private static final String SELECT_COUNT_FROM_CUSTOMER = "SELECT count(*) FROM customer";

	private CustomerServicePostgres() {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");

			// Parse database connection url from system env if available
			// (Heroku sets this automatically for the Dyno)
			try {
				parseDbURLFromSystemEnv();
			} catch (URISyntaxException e) {
				throw new RuntimeException("Error parsing URL! Cannot create database connection", e);
			}

			// Crate customer table if it doesn't exist
			connection = getConnection();
			Statement statement = connection.createStatement();

			statement.execute(CREATE_TABLE_SQL);

			if (count() < 1) {
				log.info("Creating test data");
				ensureTestData();
			} else {
				log.info("Dataabse contains data, not generating new test data..");
			}

			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException("Error connecting to the database.", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("PostgreSQL driver not found, did you remember to add the dependency?", e);
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void parseDbURLFromSystemEnv() throws URISyntaxException {
		String herokuDbUrl = System.getenv("DATABASE_URL");
		if (herokuDbUrl != null && !"".equals(herokuDbUrl.trim())) {
			URI dbURI = new URI(herokuDbUrl);
			dbUserName = dbURI.getUserInfo().split(":")[0];
			dbPassword = dbURI.getUserInfo().split(":")[1];
			databaseURL = "jdbc:postgresql://" + dbURI.getHost() + ":" + dbURI.getPort() + dbURI.getPath();
		}
	}

	private Connection getConnection() throws SQLException {
		Connection connection;
		connection = DriverManager.getConnection(databaseURL, dbUserName, dbPassword);
		return connection;
	}

	public synchronized static CustomerService getInstance() {
		if (instance == null) {
			instance = new CustomerServicePostgres();
		}
		return instance;
	}

	@Override
	public synchronized List<Customer> findAll() {
		return findAll(null);
	}

	@Override
	public synchronized List<Customer> findAll(String stringFilter) {

		Connection connection = null;
		try {
			connection = getConnection();
			ResultSet executeQuery;
			if (stringFilter != null && !"".equals(stringFilter.trim())) {
				PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_CUSTOMERS_WHERE);
				preparedStatement.setString(1, "%" + stringFilter + "%");
				preparedStatement.setString(2, "%" + stringFilter + "%");
				executeQuery = preparedStatement.executeQuery();

			} else {
				Statement statement = connection.createStatement();
				executeQuery = statement.executeQuery(SELECT_ALL_CUSTOMERS);
			}

			ArrayList<Customer> arrayList = new ArrayList<Customer>();
			while (executeQuery.next()) {
				Customer customer = new Customer();
				customer.setId(new Long(executeQuery.getInt(1)));
				customer.setFirstName(executeQuery.getString(2));
				customer.setLastName(executeQuery.getString(3));

				String statusString = executeQuery.getString(4);
				if (statusString != null) {
					customer.setStatus(CustomerStatus.valueOf(statusString));
				}
				customer.setEmail(executeQuery.getString(5));
				customer.setBirthDate(executeQuery.getDate(6));
				arrayList.add(customer);
			}

			log.info("Found " + arrayList.size() + " customers in db...");
			return arrayList;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new ArrayList<>();

	}

	@Override
	public synchronized List<Customer> findAll(String stringFilter, int start, int maxresults) {
		return findAll(stringFilter);
	}

	@Override
	public synchronized long count() {

		Connection connection = null;
		try {
			connection = getConnection();
			Statement createStatement = connection.createStatement();
			ResultSet executeQuery = createStatement.executeQuery(SELECT_COUNT_FROM_CUSTOMER);
			if (executeQuery.next()) {
				long count = executeQuery.getLong(1);
				log.info("Count is: " + count);
				return count;
			}
			return -1;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return -1;
	}

	@Override
	public synchronized void delete(Customer value) {

		Connection connection = null;
		try {
			connection = getConnection();
			PreparedStatement statement = connection.prepareStatement(DELETE_FROM_CUSTOMER_WHERE_ID);
			statement.setInt(1, value.getId().intValue());
			statement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public synchronized void save(Customer entry) {
		Connection connection = null;
		try {
			connection = getConnection();

			if (entry.getId() == null) {

				PreparedStatement prepareStatement = connection.prepareStatement(INSERT_TO_CUSTOMER_SQL);
				prepareStatement.setString(1, entry.getFirstName());
				prepareStatement.setString(2, entry.getLastName());
				if (entry.getStatus() != null) {
					prepareStatement.setString(3, entry.getStatus().toString());
				} else {
					prepareStatement.setString(3, null);
				}
				prepareStatement.setString(4, entry.getEmail());
				if (entry.getBirthDate() != null) {
					prepareStatement.setDate(5, new java.sql.Date(entry.getBirthDate().getTime()));
				} else {
					prepareStatement.setDate(5, null);
				}

				prepareStatement.execute();

				//
			} else {

				PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_CUSTOMER_SQL);
				prepareStatement.setString(1, entry.getFirstName());
				prepareStatement.setString(2, entry.getLastName());
				if (entry.getStatus() != null) {
					prepareStatement.setString(3, entry.getStatus().toString());
				} else {
					prepareStatement.setString(3, null);
				}
				prepareStatement.setString(4, entry.getEmail());
				if (entry.getBirthDate() != null) {
					prepareStatement.setDate(5, new java.sql.Date(entry.getBirthDate().getTime()));
				} else {
					prepareStatement.setDate(5, null);
				}
				prepareStatement.setInt(6, entry.getId().intValue());
				prepareStatement.execute();

			}
			try {
				entry = (Customer) entry.clone();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void ensureTestData() {
		if (findAll().isEmpty()) {
			final String[] names = new String[] { "Gabrielle Patel", "Brian Robinson", "Eduardo Haugen",
					"Koen Johansen", "Alejandro Macdonald", "Angel Karlsson", "Yahir Gustavsson", "Haiden Svensson",
					"Emily Stewart", "Corinne Davis", "Ryann Davis", "Yurem Jackson", "Kelly Gustavsson",
					"Eileen Walker", "Katelyn Martin", "Israel Carlsson", "Quinn Hansson", "Makena Smith",
					"Danielle Watson", "Leland Harris", "Gunner Karlsen", "Jamar Olsson", "Lara Martin",
					"Ann Andersson", "Remington Andersson", "Rene Carlsson", "Elvis Olsen", "Solomon Olsen",
					"Jaydan Jackson", "Bernard Nilsen" };
			Random r = new Random(0);
			for (String name : names) {
				String[] split = name.split(" ");
				Customer c = new Customer();
				c.setFirstName(split[0]);
				c.setLastName(split[1]);
				c.setEmail(split[0].toLowerCase() + "@" + split[1].toLowerCase() + ".com");
				c.setStatus(CustomerStatus.values()[r.nextInt(CustomerStatus.values().length)]);
				Calendar cal = Calendar.getInstance();
				int daysOld = 0 - r.nextInt(365 * 15 + 365 * 60);
				cal.add(Calendar.DAY_OF_MONTH, daysOld);
				c.setBirthDate(cal.getTime());
				save(c);
			}
		}
	}

}
