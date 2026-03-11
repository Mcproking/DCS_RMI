# HRM RMI Project

This is a Java RMI-based Human Resource Management project with:

- `HRMServer` for the RMI server
- `PRSServer` for the PRS server
- `HRMGUIClient` for the Swing GUI client
- `HRMClient` for the console client
- SQLite as the local database

## Java Requirement

This project requires:

- Java JDK 23

Why:

- The project is configured with Java release `23` in `pom.xml`
- The build scripts use `javac`, so a full JDK is required, not just a JRE

Check your Java version:

```cmd
java -version
javac -version
```

## Project Structure

Important files:

- `build.cmd` compiles the project without Maven
- `run.cmd` runs the server, GUI client, or console client
- `database.db` is the SQLite database file
- `lib/` contains required JDBC libraries

## Run Without Maven

This project includes Windows batch scripts so you can build and run it without installing Maven.

### 1. Build the project

```cmd
./build.cmd
```

This compiles all Java source files into:

```text
target/classes
```

### 2. Run the server

```cmd
./run.cmd server
```

Expected output:

```text
Connected to SQLite
HRM Server is Running
```

### 3. Run the GUI client

Open a new terminal and run:

```cmd
./run.cmd gui
```

### 4. Run the console client

Open a new terminal and run:

```cmd
./run.cmd client
```

## Recommended Startup Order

**Important:** HRM Server must be started before any other server or client.

1. Start the HRM server first:

```cmd
./.cmd server
```

Expected output:

```text
Connected to SQLite
HRM Server is Running
```

2. Start the PRS Server (if applicable) in another terminal:

```cmd
./run.cmd prs
```

3. Start the GUI or console client in another terminal:

```cmd
./.cmd gui
```

or

```cmd
./run.cmd client
```

**Note:** Always ensure HRM Server (step 1) is fully initialized and running before starting the PRS Server or any client applications.

## Run With Maven

Maven support is also configured.

If you want to use Maven:

```cmd
mvnw.cmd compile
mvnw.cmd -Pserver exec:java
mvnw.cmd -Pgui exec:java
mvnw.cmd -Pclient exec:java
```

Note:

- `mvnw.cmd` in this project depends on the local Maven setup currently present in the repo environment
- The batch scripts are the simpler way to run this project on Windows

## Troubleshooting

### `java` or `javac` not found

Install JDK 23 and make sure Java is available in your system environment.

### Server starts but client cannot connect

Make sure:

- the server is already running
- port `1099` and `1100` is available
- client and server are running on the same machine unless you update the RMI host configuration

### SQLite errors

Make sure these files exist:

- `database.db`
- `lib/sqlite-jdbc-3.49.1.0.jar`

## Notes

- The project currently targets Windows because the included scripts are `.cmd` files
- If you want Linux or macOS support, add equivalent shell scripts
