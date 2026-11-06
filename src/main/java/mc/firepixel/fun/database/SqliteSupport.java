package mc.firepixel.fun.database;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

final class SqliteSupport implements AutoCloseable {

    private static final String SQLITE_PACKAGE = "org.sqlite.";
    private static final String SQLITE_RESOURCE_PREFIX = "org/sqlite/";

    private final ChildFirstClassLoader loader;
    private final DataSource dataSource;

    private SqliteSupport(
            ChildFirstClassLoader loader,
            DataSource dataSource
    ) {
        this.loader = loader;
        this.dataSource = dataSource;
    }

    static SqliteSupport create(String jdbcUrl) throws Exception {

        java.security.CodeSource codeSource = SqliteSupport.class
                .getProtectionDomain()
                .getCodeSource();

        URL location = codeSource == null
                ? null
                : codeSource.getLocation();

        URL[] urls = location == null
                ? new URL[0]
                : new URL[]{location};

        ChildFirstClassLoader loader = new ChildFirstClassLoader(
                urls,
                SqliteSupport.class.getClassLoader()
        );

        try {
            Class<?> dataSourceClass = Class.forName(
                    "org.sqlite.SQLiteDataSource",
                    true,
                    loader
            );

            Object sqliteDataSource = dataSourceClass
                    .getConstructor()
                    .newInstance();

            dataSourceClass
                    .getMethod("setUrl", String.class)
                    .invoke(sqliteDataSource, jdbcUrl);

            return new SqliteSupport(
                    loader,
                    (DataSource) sqliteDataSource
            );
        } catch (Exception exception) {
            try {
                loader.close();
            } catch (IOException ignored) {
                // Keep the original failure.
            }
            throw exception;
        }
    }

    DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        try {
            loader.close();
        } catch (IOException ignored) {
        }
    }

    private static final class ChildFirstClassLoader
            extends URLClassLoader {

        ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {

            synchronized (getClassLoadingLock(name)) {

                Class<?> type = findLoadedClass(name);

                if (type == null && name.startsWith(SQLITE_PACKAGE)) {
                    try {
                        type = findClass(name);
                    } catch (ClassNotFoundException ignored) {

                    }
                }

                if (type == null) {
                    type = super.loadClass(name, false);
                }

                if (resolve) {
                    resolveClass(type);
                }

                return type;
            }
        }

        @Override
        public URL getResource(String name) {
            if (name.startsWith(SQLITE_RESOURCE_PREFIX)) {
                URL resource = findResource(name);

                if (resource != null) {
                    return resource;
                }
            }

            return super.getResource(name);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if (name.startsWith(SQLITE_RESOURCE_PREFIX)) {
                URL resource = findResource(name);

                if (resource != null) {
                    try {
                        return resource.openStream();
                    } catch (IOException ignored) {
                    }
                }
            }

            return super.getResourceAsStream(name);
        }
    }
}
