package name.zemon.david.generateh2database;

import com.beust.jcommander.JCommander;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.h2.Driver;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by david on 12/5/16.
 */
public class Main {
    public static final int DEFAULT_PORT = 9092;
    public static final String DEFAULT_USER = "sa";
    public static final String DEFAULT_PASSWORD = "";

    private static final Pattern USER_HOME_PATTERN = Pattern.compile("^~");
    private static final File USER_HOME = new File(System.getProperty("user.home"));
    public static final Gson GSON = new Gson();

    public static void main(@Nonnull final String[] args) throws IOException, SQLException {
        final Options options = getOptions(args);
        final String host = options.getHost();
        final int port = options.getPort();
        final String database = options.getDatabase();
        final String user = options.getUser();
        final String password = options.getPassword();
        final String url = String.format("jdbc:h2:tcp://%s:%d/%s", host, port, database);

        final Path templateFile = getpath(options.getTemplateFile());
        final String queryTemplate = new String(Files.readAllBytes(templateFile));


        final Driver driver = new Driver();
        DriverManager.registerDriver(driver);

        System.out.format("Connecting with %s:%s@%s%n", user, password, url);
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            final boolean success = run(queryTemplate, options, connection);
            if (!success) {
                System.exit(1);
            }
        }
    }

    @Nonnull
    private static Path getpath(@Nonnull final String input) {
        final String replaced = USER_HOME_PATTERN.matcher(input).replaceFirst(USER_HOME.getAbsolutePath());
        return Paths.get(replaced);
    }

    @Nonnull
    private static Collection<Map<String, String>> getValues(@Nonnull final String valuesPath) throws IOException {
        final Path actualValuesPath = getpath(valuesPath);
        final String json = new String(Files.readAllBytes(actualValuesPath));
        return GSON.fromJson(json, new TypeToken<Collection<Map<String, String>>>() {
        }.getType());
    }

    @Nonnull
    private static Options getOptions(@Nonnull final String[] args) {
        final Options options = new Options();
        final JCommander jCommander = new JCommander(options, args);
        jCommander.setProgramName("Generate H2 Database");
        if (options.help()) {
            jCommander.usage();
            System.exit(0);
            return null;
        } else {
            return options;
        }
    }

    private static boolean run(@Nonnull final String queryTemplate, @Nonnull final Options options,
                               @Nonnull final Connection connection) throws IOException, SQLException {
        if (null == options.getValuesFile()) {
            runStatement(connection, queryTemplate);
            return true;
        } else {
            final Collection<Map<String, String>> values = getValues(options.getValuesFile());

            final boolean[] success = {true};
            values.parallelStream().forEach(run -> {
                final StrSubstitutor strSubstitutor = new StrSubstitutor(run);
                final String expandedStatements = strSubstitutor.replace(queryTemplate);

                if (!runStatements(connection, expandedStatements)) {
                    success[0] = false;
                }
            });
            return success[0];
        }
    }

    private static boolean runStatements(@Nonnull final Connection connection, final String expandedQuery) {
        final boolean[] success = {true};
        Arrays.stream(expandedQuery.split(";"))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .forEach(query -> {
                    try {
                        System.out.println(query);
                        runStatement(connection, query);
                    } catch (final SQLException e) {
                        success[0] = false;
                        e.printStackTrace();
                    }
                });
        return success[0];
    }

    private static void runStatement(@Nonnull final Connection connection, @Nonnull final String expandedQuery) throws SQLException {
        try (CallableStatement callableStatement = connection.prepareCall(expandedQuery)) {
            callableStatement.execute();
        }
    }
}
