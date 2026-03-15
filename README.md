# HRM RMI Project

Java RMI-based Human Resource Management system with:

- `HRMServer` (main RMI server)
- `PRSServer` (payroll-related server)
- `HRMGUIClient` (Swing client)
- `HRMClient` (console client)
- SQLite local database

## Requirements

- Windows
- JDK 23 (`java` and `javac` available)

Check Java:

```cmd
java -version
javac -version
```

## Quick Start (Non-Maven, recommended)

From project root:

```text
c:\Users\Tsundane\Desktop\RMI
```

1) Build:

```cmd
build.cmd
```

2) Run in this order (separate terminals):

```cmd
run.cmd server
run.cmd prs
run.cmd gui
```

or use console client:

```cmd
run.cmd client
```

Expected server output:

```text
Connected to SQLite
HRM Server is Running
```

## Testing

### Can tests run without Maven?

Yes, but not out-of-the-box in this repository. Test dependencies (JUnit 5 + Mockito) are managed by Maven, and there is no `test.cmd` script yet for manual classpath setup.

### Recommended (Maven)

Resolve dependencies once:

```cmd
mvnw.cmd -q -DskipTests compile
```

Run all tests:

```cmd
mvnw.cmd test
```

Run one class:

```cmd
mvnw.cmd -Dtest=EmployeeServiceImplTest test
```

Run one method:

```cmd
mvnw.cmd -Dtest=EmployeeServiceImplTest#applyLeaveWithEnoughBalanceUpdatesDbAndBalance test
```

Clean + rerun:

```cmd
mvnw.cmd clean test
```

## Maven Run (optional)

Build:

```cmd
mvnw.cmd clean compile
```

Run profiles:

```cmd
mvnw.cmd -Pserver exec:java
mvnw.cmd -Pgui exec:java
mvnw.cmd -Pclient exec:java
```

## Project Structure

- `build.cmd`: non-Maven compile script
- `run.cmd`: run targets (`server`, `prs`, `gui`, `client`)
- `database.db`: SQLite file
- `lib/`: JDBC dependencies

## Troubleshooting

### `java` or `javac` not found

Install JDK 23 and ensure Java is in PATH.

### Client cannot connect

Ensure:

- `run.cmd server` is started first
- ports `1099` and `1100` are available
- client/server run on the same machine (unless RMI host config is changed)

### SQLite errors

Ensure these exist:

- `database.db`
- `lib/sqlite-jdbc-3.49.1.0.jar`

## Notes

- Repo currently targets Windows (`.cmd` scripts)
- For Linux/macOS, add equivalent shell scripts
