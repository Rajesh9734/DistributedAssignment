# HRM System (Java RMI + Swing + SQLite)

This repository contains a simple Human Resource Management (HRM) system implemented in Java using RMI (client-server), Swing for GUI, and SQLite (via JDBC) for persistence.

---

## High-level overview

- Server: Console application that exposes an RMI service (`HRMService`) and manages the SQLite database (`hrm_system.db`). Business logic and persistence live here.
- Client: Java Swing GUI application. All remote calls executed on background threads using `SwingWorker` to keep the EDT responsive.
- Secure PRS Sync: Mocked payroll sync method encrypts payload using AES before "sending" (logged by the server).

---

## What I changed / important notes

- Database file: `hrm_system.db` is created automatically in the project root when the server first connects (requires the SQLite JDBC driver on the classpath).
- The server contains an example AES utility (`src/server/AESUtil.java`) that uses a hardcoded key only for demonstration. Do not use this for production secrets.
- The client contains no SQL. All DB access is done on the server.
- The UI uses Nimbus look-and-feel and Swing layout improvements.
- Default admin account is created automatically if missing: `admin@hrm.com` / `admin123`.

---

## Prerequisites

- Java JDK 11+ (or newer) installed and `java`/`javac` available on PATH.
- Windows PowerShell used in examples below.
- The SQLite JDBC driver JAR is included at `lib/sqlite-jdbc.jar` in this repository. If missing, download from https://github.com/xerial/sqlite-jdbc/releases and place in `lib/`.

---

## Quick run (recommended)

Two convenience batch files are included in the project root for Windows:

- `run_server.bat` — compile and start the server (also starts/uses local RMI registry on port 1099)
- `run_client.bat` — compile and start the Swing client

Double-click `run_server.bat` to start the server, wait for messages indicating the database and RMI registry are running, then double-click `run_client.bat` to start the GUI.

Alternatively run from PowerShell manually (from project root):

```powershell
# Compile (one-time)
javac -cp "src;lib/sqlite-jdbc.jar" src/common/*.java src/server/*.java src/client/*.java

# Start server (in a separate terminal)
java -cp "src;lib/sqlite-jdbc.jar" server.HRMServer

# Start client (in another terminal)
java -cp "src;lib/sqlite-jdbc.jar" client.ClientApp
```

If you run from an IDE (IntelliJ), add `lib/sqlite-jdbc.jar` as a project library and run the `server.HRMServer` and `client.ClientApp` run configurations.

---

## Default credentials

- HR Admin: `admin@hrm.com` / `admin123`

Create employees via HR Dashboard and set their passwords during registration.

---

## Primary Features

- Login using Email + Password (client-side login GUI).
- HR Dashboard (after login with HR role):
  - Employee Management: Register, edit employee details, set leave balance.
  - Leave Management: View pending leave applications and approve/reject them.
  - Report: Generate yearly employee profile + leave history report.
  - PRS Sync: Trigger secure (AES encrypted) sync to external payroll (simulated).
- Employee Dashboard:
  - Profile: View/Edit personal details, address, designation, and family members (add/remove).
  - Leave Management: View leave balance, apply for leave, and see leave history.

All GUI remote operations run in the background via `SwingWorker` and update the UI after completion.

---

## Database schema (auto-created)

- `users` (id TEXT PRIMARY KEY, email TEXT UNIQUE, password TEXT, role TEXT)
- `employees` (id TEXT PRIMARY KEY, first_name TEXT, last_name TEXT, ic_passport TEXT, designation TEXT, address TEXT, leave_balance INTEGER)
- `family_members` (id INTEGER PRIMARY KEY AUTOINCREMENT, employee_id TEXT, first_name TEXT, last_name TEXT, relation TEXT, email TEXT, phone TEXT)
- `leave_applications` (leave_id TEXT PRIMARY KEY, employee_id TEXT, start_date TEXT, end_date TEXT, status TEXT, year INTEGER)

---

## Important files

- `src/common/` — shared domain models and `HRMInterface` (RMI interface)
- `src/server/` — server implementation, DB manager, AES util, and `HRMServer` main
- `src/client/` — Swing client (Login, HR and Employee dashboards)
- `lib/sqlite-jdbc.jar` — SQLite JDBC driver used at runtime
- `hrm_system.db` — generated SQLite database file (created at server runtime)

---

## Troubleshooting

- "No suitable driver found for jdbc:sqlite:hrm_system.db" or `ClassNotFoundException: org.sqlite.JDBC`:
  - Ensure `lib/sqlite-jdbc.jar` exists and is present on the classpath when running the server.
  - Example classpath usage: `java -cp "src;lib/sqlite-jdbc.jar" server.HRMServer`

- Database not created:
  - Check server logs. The DB file `hrm_system.db` is created when the server attempts the first connection. If the JDBC driver is missing the server will not create the file.

- UI looks cramped or controls are tiny:
  - Use a larger screen resolution or increase the window size. The client uses Nimbus LAF and preconfigured sizes; they are adjustable in `src/client/LoginFrame.java`.

---

## Security & production notes

- AES key in `src/server/AESUtil.java` is hardcoded for demonstration. In production, use a secure secrets manager and rotate keys.
- Passwords are stored in plaintext in this demo code for simplicity — this is NOT secure. For any real deployment, implement proper password hashing (bcrypt/Argon2) and transport-level encryption (TLS).

---

## Development notes

- All remote calls from the UI must be non-blocking; this project uses `SwingWorker` for that purpose—do not call remote methods directly on the EDT.
- Client code contains no SQL and relies on the server RMI implementation for persistence.

---

If you'd like, I can:
- Add unit tests for server methods.
- Improve the UI further (icons, color themes, consistent spacing, and font scaling).
- Implement password hashing and secure key storage.


---

Project created: March 2026

