package dev.farhan.codereview.seeder;

import dev.farhan.codereview.model.ReviewPattern;
import dev.farhan.codereview.model.Severity;
import dev.farhan.codereview.repository.ReviewPatternRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ReviewPatternRepository patternRepository;
    private final EmbeddingModel embeddingModel;

    public DataSeeder(ReviewPatternRepository patternRepository, EmbeddingModel embeddingModel) {
        this.patternRepository = patternRepository;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(String... args) {
        if (patternRepository.count() > 0) {
            return;
        }

        List<ReviewPattern> patterns = createPatterns();

        for (ReviewPattern pattern : patterns) {
            pattern.setEmbedding(embeddingModel.embed(pattern.buildEmbeddingText()));
        }

        patternRepository.saveAll(patterns);
    }

    private List<ReviewPattern> createPatterns() {
        List<ReviewPattern> patterns = new ArrayList<>();

        // --- Error handling patterns ---

        patterns.add(new ReviewPattern(
                "Catching generic Exception",
                "Catching java.lang.Exception instead of specific exception types",
                "java",
                Severity.WARNING,
                "error-handling",
                """
                try {
                    File file = new File("data.txt");
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = fis.readAllBytes();
                } catch (Exception e) {
                    System.out.println("Something went wrong");
                }""",
                """
                try {
                    File file = new File("data.txt");
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = fis.readAllBytes();
                } catch (FileNotFoundException e) {
                    System.out.println("File not found: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("Error reading file: " + e.getMessage());
                }""",
                "Catching generic Exception hides the actual error type and makes debugging harder. "
                + "It also catches unchecked exceptions like NullPointerException and IllegalArgumentException, "
                + "which usually indicate programming errors that should not be silently handled."
        ));

        patterns.add(new ReviewPattern(
                "Empty catch block",
                "Catching an exception and doing nothing with it, silently swallowing errors",
                "java",
                Severity.CRITICAL,
                "error-handling",
                """
                try {
                    connection.close();
                } catch (SQLException e) {
                    // ignore
                }""",
                """
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("Failed to close connection: {}", e.getMessage());
                }""",
                "Empty catch blocks silently swallow errors. When something fails, there is no log entry, "
                + "no alert, and no way to diagnose the problem later. At minimum, log the exception message. "
                + "If the exception truly does not matter, add a comment explaining why."
        ));

        patterns.add(new ReviewPattern(
                "Catching and rethrowing without context",
                "Catching an exception and rethrowing it without adding context or wrapping it",
                "java",
                Severity.INFO,
                "error-handling",
                """
                try {
                    userRepository.save(user);
                } catch (DataAccessException e) {
                    throw e;
                }""",
                """
                try {
                    userRepository.save(user);
                } catch (DataAccessException e) {
                    throw new ServiceException("Failed to save user: " + user.getId(), e);
                }""",
                "Catching and immediately rethrowing the same exception adds no value. Either wrap the exception "
                + "with additional context that helps the caller understand what operation failed, or remove the "
                + "try-catch block entirely and let the exception propagate naturally."
        ));

        patterns.add(new ReviewPattern(
                "Using printStackTrace instead of a logger",
                "Calling e.printStackTrace() instead of using a proper logging framework",
                "java",
                Severity.WARNING,
                "error-handling",
                """
                try {
                    processOrder(order);
                } catch (OrderException e) {
                    e.printStackTrace();
                }""",
                """
                try {
                    processOrder(order);
                } catch (OrderException e) {
                    logger.error("Failed to process order {}: {}", order.getId(), e.getMessage(), e);
                }""",
                "printStackTrace writes to System.err, which is often not captured by log aggregation systems. "
                + "In production, this output can be lost entirely. A logging framework like SLF4J sends output "
                + "to configured appenders where it can be searched, filtered, and monitored."
        ));

        patterns.add(new ReviewPattern(
                "Swallowing InterruptedException",
                "Catching InterruptedException without restoring the interrupt flag on the thread",
                "java",
                Severity.CRITICAL,
                "error-handling",
                """
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn("Sleep interrupted");
                }""",
                """
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn("Sleep interrupted");
                    Thread.currentThread().interrupt();
                }""",
                "When a thread is interrupted, the InterruptedException clears the interrupt flag. If you catch "
                + "the exception without calling Thread.currentThread().interrupt(), calling code that checks the "
                + "interrupt status will not know the thread was interrupted. This can prevent proper shutdown of "
                + "thread pools and executor services."
        ));

        // --- Security patterns ---

        patterns.add(new ReviewPattern(
                "Hardcoded credentials",
                "Storing passwords, API keys, or secrets as string literals in source code",
                "java",
                Severity.CRITICAL,
                "security",
                """
                public class DatabaseConfig {
                    private static final String DB_PASSWORD = "s3cretP@ss!";
                    private static final String API_KEY = "sk-abc123def456";
                }""",
                """
                public class DatabaseConfig {
                    @Value("${db.password}")
                    private String dbPassword;

                    @Value("${api.key}")
                    private String apiKey;
                }""",
                "Hardcoded credentials end up in version control, build artifacts, and log files. Anyone with "
                + "access to the repository can read them. Use environment variables, a secrets manager, or "
                + "Spring's externalized configuration to inject sensitive values at runtime."
        ));

        patterns.add(new ReviewPattern(
                "SQL injection via string concatenation",
                "Building SQL or database queries by concatenating user input directly into the query string",
                "java",
                Severity.CRITICAL,
                "security",
                """
                String query = "SELECT * FROM users WHERE username = '" + username + "'";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);""",
                """
                String query = "SELECT * FROM users WHERE username = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();""",
                "String concatenation in queries allows an attacker to inject arbitrary SQL. If username contains "
                + "something like \"' OR '1'='1\", the query returns all rows. Parameterized queries or prepared "
                + "statements escape input automatically and prevent injection."
        ));

        patterns.add(new ReviewPattern(
                "Using Math.random for security tokens",
                "Using java.lang.Math.random() to generate tokens, session IDs, or other security-sensitive values",
                "java",
                Severity.CRITICAL,
                "security",
                """
                String token = "";
                for (int i = 0; i < 32; i++) {
                    token += (char) ('a' + (int) (Math.random() * 26));
                }""",
                """
                SecureRandom random = new SecureRandom();
                byte[] bytes = new byte[32];
                random.nextBytes(bytes);
                String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);""",
                "Math.random() uses a linear congruential generator that is predictable. An attacker who observes "
                + "a few outputs can predict future values. For security-sensitive random values, use "
                + "java.security.SecureRandom, which draws from the operating system's entropy source."
        ));

        patterns.add(new ReviewPattern(
                "Logging sensitive data",
                "Writing passwords, tokens, credit card numbers, or other sensitive data to log output",
                "java",
                Severity.CRITICAL,
                "security",
                """
                logger.info("User login attempt: username={}, password={}", username, password);
                logger.debug("Processing payment with card: {}", creditCardNumber);""",
                """
                logger.info("User login attempt: username={}", username);
                logger.debug("Processing payment with card ending in: {}", creditCardNumber.substring(creditCardNumber.length() - 4));""",
                "Log files are often stored in plain text, shipped to aggregation services, and accessible to "
                + "operations teams. Sensitive data in logs can leak credentials, violate compliance requirements "
                + "like PCI-DSS, and create legal liability. Mask or omit sensitive fields before logging."
        ));

        // --- Performance patterns ---

        patterns.add(new ReviewPattern(
                "String concatenation in loops",
                "Using the + operator or += to build strings inside a loop instead of StringBuilder",
                "java",
                Severity.WARNING,
                "performance",
                """
                String result = "";
                for (String item : items) {
                    result += item + ", ";
                }""",
                """
                StringBuilder result = new StringBuilder();
                for (String item : items) {
                    result.append(item).append(", ");
                }""",
                "Each += on a String creates a new String object because strings are immutable in Java. In a loop "
                + "with n iterations, this produces n intermediate objects and copies characters repeatedly, giving "
                + "O(n^2) time complexity. StringBuilder appends in place and avoids the repeated copying."
        ));

        patterns.add(new ReviewPattern(
                "Creating objects inside tight loops",
                "Instantiating heavy objects like DateTimeFormatter or Pattern inside a loop body",
                "java",
                Severity.WARNING,
                "performance",
                """
                for (String dateStr : dates) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(dateStr);
                    process(date);
                }""",
                """
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for (String dateStr : dates) {
                    Date date = sdf.parse(dateStr);
                    process(date);
                }""",
                "Creating a new formatter or pattern compiler per iteration wastes CPU and generates garbage for "
                + "the garbage collector. If the object does not change between iterations, create it once before "
                + "the loop. For thread-safe alternatives, use DateTimeFormatter which is immutable."
        ));

        patterns.add(new ReviewPattern(
                "N+1 query pattern",
                "Executing a database query inside a loop, resulting in one query per element plus the initial list query",
                "java",
                Severity.WARNING,
                "performance",
                """
                List<Order> orders = orderRepository.findAll();
                for (Order order : orders) {
                    Customer customer = customerRepository.findById(order.getCustomerId());
                    order.setCustomerName(customer.getName());
                }""",
                """
                List<Order> orders = orderRepository.findAll();
                List<String> customerIds = orders.stream()
                        .map(Order::getCustomerId)
                        .distinct()
                        .toList();
                Map<String, Customer> customers = customerRepository.findAllById(customerIds)
                        .stream()
                        .collect(Collectors.toMap(Customer::getId, c -> c));
                for (Order order : orders) {
                    order.setCustomerName(customers.get(order.getCustomerId()).getName());
                }""",
                "If the list has 100 elements, you execute 101 queries instead of 2. Each query has network "
                + "round-trip overhead. Fetch all related data in a single batch query using findAllById or "
                + "an IN clause, then join in memory."
        ));

        patterns.add(new ReviewPattern(
                "Synchronized on mutable field",
                "Using synchronized on a field whose reference can change, making the lock unreliable",
                "java",
                Severity.CRITICAL,
                "performance",
                """
                private Object lock = new Object();

                public void reset() {
                    lock = new Object();
                }

                public void doWork() {
                    synchronized (lock) {
                        // critical section
                    }
                }""",
                """
                private final Object lock = new Object();

                public void doWork() {
                    synchronized (lock) {
                        // critical section
                    }
                }""",
                "If the lock field is reassigned, two threads can synchronize on different objects simultaneously, "
                + "defeating the purpose of the lock. Always declare lock objects as final so the reference cannot "
                + "change after construction."
        ));

        patterns.add(new ReviewPattern(
                "Unnecessary autoboxing in tight loops",
                "Using boxed types like Integer or Long in performance-critical loops where primitives would suffice",
                "java",
                Severity.INFO,
                "performance",
                """
                Long sum = 0L;
                for (int i = 0; i < 1_000_000; i++) {
                    sum += i;
                }""",
                """
                long sum = 0L;
                for (int i = 0; i < 1_000_000; i++) {
                    sum += i;
                }""",
                "Each += operation on a Long object triggers unboxing, addition, and reboxing, creating a new Long "
                + "object per iteration. With a million iterations, that is a million unnecessary object allocations. "
                + "Use the primitive long type for accumulators in tight loops."
        ));

        // --- Maintainability patterns ---

        patterns.add(new ReviewPattern(
                "Unclosed resources",
                "Opening a resource like InputStream, Connection, or Reader without using try-with-resources",
                "java",
                Severity.CRITICAL,
                "maintainability",
                """
                FileInputStream fis = new FileInputStream("config.properties");
                Properties props = new Properties();
                props.load(fis);
                return props;""",
                """
                try (FileInputStream fis = new FileInputStream("config.properties")) {
                    Properties props = new Properties();
                    props.load(fis);
                    return props;
                }""",
                "If an exception occurs between opening and manually closing a resource, the close call never runs. "
                + "This leaks file handles, database connections, or network sockets. Try-with-resources guarantees "
                + "the resource is closed even if an exception is thrown."
        ));

        patterns.add(new ReviewPattern(
                "Missing null check before dereferencing",
                "Calling a method on an object that could be null without checking first",
                "java",
                Severity.WARNING,
                "maintainability",
                """
                User user = userRepository.findByEmail(email);
                String name = user.getName();
                logger.info("Found user: {}", name);""",
                """
                User user = userRepository.findByEmail(email);
                if (user == null) {
                    throw new UserNotFoundException("No user with email: " + email);
                }
                String name = user.getName();
                logger.info("Found user: {}", name);""",
                "If findByEmail returns null and you call getName() on it, you get a NullPointerException with no "
                + "useful context. Checking for null and throwing a descriptive exception makes the failure explicit "
                + "and tells the caller exactly what went wrong."
        ));

        patterns.add(new ReviewPattern(
                "Using == for string comparison",
                "Comparing strings with == instead of .equals(), which checks reference identity rather than content",
                "java",
                Severity.WARNING,
                "maintainability",
                """
                String status = order.getStatus();
                if (status == "COMPLETED") {
                    processCompletedOrder(order);
                }""",
                """
                String status = order.getStatus();
                if ("COMPLETED".equals(status)) {
                    processCompletedOrder(order);
                }""",
                "The == operator checks whether two references point to the same object in memory, not whether they "
                + "have the same characters. Strings from user input, database queries, or deserialization are usually "
                + "different objects even if they contain the same text. Using .equals() compares the actual content."
        ));

        patterns.add(new ReviewPattern(
                "Raw types in generics",
                "Using raw generic types like List or Map instead of parameterized types like List<String>",
                "java",
                Severity.WARNING,
                "maintainability",
                """
                List users = new ArrayList();
                users.add("Alice");
                users.add(42);
                String first = (String) users.get(0);""",
                """
                List<String> users = new ArrayList<>();
                users.add("Alice");
                // users.add(42); // compile error
                String first = users.get(0);""",
                "Raw types bypass generics checking at compile time. You can put any object into a raw List, and "
                + "the ClassCastException only shows up at runtime when you retrieve and cast the element. "
                + "Parameterized types catch these mistakes during compilation."
        ));

        patterns.add(new ReviewPattern(
                "God method",
                "A single method that does too many things, making it hard to test, understand, and maintain",
                "java",
                Severity.INFO,
                "maintainability",
                """
                public void processOrder(Order order) {
                    // validate order (20 lines)
                    // calculate totals (15 lines)
                    // apply discounts (25 lines)
                    // check inventory (20 lines)
                    // charge payment (15 lines)
                    // send confirmation email (10 lines)
                    // update analytics (10 lines)
                }""",
                """
                public void processOrder(Order order) {
                    validateOrder(order);
                    BigDecimal total = calculateTotal(order);
                    total = applyDiscounts(order, total);
                    reserveInventory(order);
                    chargePayment(order, total);
                    sendConfirmation(order);
                    updateAnalytics(order);
                }""",
                "A method with hundreds of lines and multiple responsibilities is hard to test in isolation. "
                + "Each responsibility should be a separate method that can be tested, reused, and understood "
                + "independently. If a method needs a comment to separate its sections, those sections should "
                + "probably be their own methods."
        ));

        patterns.add(new ReviewPattern(
                "Mutable objects as map keys",
                "Using mutable objects as keys in HashMap or HashSet, where the hash code can change after insertion",
                "java",
                Severity.WARNING,
                "maintainability",
                """
                Map<List<String>, String> cache = new HashMap<>();
                List<String> key = new ArrayList<>(List.of("a", "b"));
                cache.put(key, "value");
                key.add("c");
                System.out.println(cache.get(key)); // prints null""",
                """
                Map<List<String>, String> cache = new HashMap<>();
                List<String> key = List.of("a", "b");
                cache.put(key, "value");
                System.out.println(cache.get(key)); // prints "value"
                // List.of returns an unmodifiable list, so the key cannot change""",
                "HashMap and HashSet use the hash code to determine which bucket to store an entry in. If you "
                + "mutate a key after inserting it, its hash code changes but the entry stays in the old bucket. "
                + "Looking up the modified key checks the new bucket and finds nothing. Use immutable objects as "
                + "map keys, or make defensive copies."
        ));

        return patterns;
    }
}
