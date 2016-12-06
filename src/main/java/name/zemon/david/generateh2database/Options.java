package name.zemon.david.generateh2database;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by david on 12/5/16.
 */
@SuppressWarnings("FieldMayBeFinal")
@Parameters(separators = "=")
public class Options {

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    @Parameter(names = "--host", required = true, description = "TCP host where the DB is hosted")
    private String host;

    @Parameter(names = {"--port"}, description = "TCP port of the database connection (default = 9092)")
    private int port = Main.DEFAULT_PORT;

    @Parameter(names = {"-d", "--database"}, required = true, description = "Database name (absolute path on the remote server)")
    private String database;

    @Parameter(names = {"-u", "--user"}, description = "Username for the user of the DB (default = 'admin'")
    private String user = Main.DEFAULT_USER;

    @Parameter(names = {"-p", "--password"}, description = "Password for the DB user (default = '')")
    private String password = Main.DEFAULT_PASSWORD;

    @Parameter(names = {"-t", "--template"}, required = true, description = "Template file to execute")
    private String templateFile;

    @Parameter(names = {"-v", "--values"}, description = "JSON file with list of value objects used to fill " +
            "placeholders in the template file")
    private String valuesFile;

    @Nonnull
    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    @Nonnull
    public String getDatabase() {
        return this.database;
    }

    @Nonnull
    public String getUser() {
        return this.user;
    }

    @Nonnull
    public String getPassword() {
        return this.password;
    }

    @Nonnull
    public String getTemplateFile() {
        return this.templateFile;
    }

    @Nullable
    public String getValuesFile() {
        return this.valuesFile;
    }
}
