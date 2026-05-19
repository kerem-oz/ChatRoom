# Java Concurrent Chat Room

A multithreaded client-server chat application built with Java and Swing GUI. This project was developed as a midterm project for the **Concurrent and Real-Time Programming in Java** course.

## Features

- **Multi-client Support:** Multiple clients can connect to the server simultaneously and chat in a shared room.
- **Multithreaded Server:** Handles incoming client connections and message broadcasting concurrently.
- **Graphical User Interface:** Clean and intuitive UI built with Java Swing for both the Server dashboard and Client chat windows.
- **Producer-Consumer Architecture:** Utilizes a bounded buffer and worker threads for efficient message dispatching.

## Project Structure

The codebase is organized cleanly to separate client, server, and common logic:

- `src/client/` - Contains the client-side application logic and GUI components (Login, Chat Room).
- `src/server/` - Contains the server-side application, connection handlers, bounded buffer, and dispatcher threads.
- `src/common/` - Contains shared resources like Request objects and Enums used for communication between client and server.

## How to Run

### Prerequisites
- Java Development Kit (JDK) 8 or higher installed.

### Compiling
You can easily compile the project using the provided batch script:
```bat
compile.bat
```
This will compile all `.java` files from the `src` directory and output the `.class` files into the `out` directory.

### Running the Server
Start the server first to allow clients to connect:
```bat
run_server.bat
```
A Server GUI will open where you can start the server on a specified port (default is usually 12345).

### Running a Client
Once the server is running, you can launch one or multiple clients:
```bat
run_client.bat
```
A Login GUI will appear. Enter your desired username, the server's IP address (e.g., `localhost`), and the port, then click Connect to join the chat room!

## Architecture Highlights
- **Thread Safety:** Shared resources (like the bounded buffer and connected client lists) are properly synchronized to prevent race conditions.
- **Concurrency:** Worker threads continuously poll the buffer to broadcast messages to all connected clients without blocking the main server thread.
