# Test Management System

Test Management System is a web application designed to manage tests. This project includes a backend built with Spring Boot and a frontend built with React.

## Requirements

- **Java 17**
- **Gradle 7.3**
- **Node.js 14.x or later**
- **npm 6.x or later** (or **yarn** as an alternative)
- **PostgreSQL** (ensure that PostgreSQL server is running)

## Project Structure

The project is divided into two main modules:

- **backend**: Contains the Spring Boot application.
- **frontend**: Contains the React application.

## Getting Started

### Backend

1. **Set up PostgreSQL and mail server:**

   Make sure PostgreSQL is installed and running. Create a database and an `application.yml` file with your database credentials in the resources folder. You also need to set up a mail server, enter your account details.

   Example `application.yml`:
    ```yaml
   spring:
      datasource:
      url: jdbc:postgresql://localhost:5432/db_name
      username: test
      password: test
      driver-class-name: org.postgresql.Driver
   jpa:
     hibernate:
       ddl-auto: update
     show-sql: true
     properties:
       hibernate:
         dialect: org.hibernate.dialect.PostgreSQLDialect
     mail:
      host: smtp.gmail.com
      default-encoding: UTF-8
      port: 587
      username: <yourusername>
      password: <yourpassword>
      protocol: smtp
      properties.mail.smtp:
        auth: true
        starttls.enable: true
   server:
     port: 8081
   jwt:
     secret: secret    
   ```

2. **Build and run the backend:**
    ```bash
    gradle wrapper --gradle-version 7.3
    ./gradlew build
    ./gradlew bootRun
    ```

   This will start the Spring Boot application on `http://localhost:8081`.

### Frontend

1. **Navigate to the frontend directory:**
    ```bash
    cd frontend
    ```

2. **Install dependencies:**
   Using npm:
    ```bash
    npm install
    ```

   Or using yarn:
    ```bash
    yarn install
    ```

3. **Run the frontend:**
    ```bash
    npm start
    ```

   This will start the React application on `http://localhost:3000`.

## Running the Application

After starting both the backend and frontend servers, you can access the Test Management System in your web browser at `http://localhost:3000`.