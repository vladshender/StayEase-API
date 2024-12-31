# Ebooking API 📚
Welcome to the Ebooking API, a RESTful API designed for managing a booking service. It allows users to view accommodations, make reservations, pay for them, and supports user registration and authentication via JWT. The API also includes role-based access control, which provides different levels of access for users and administrators.
## Technologies
- **Java 17**: Core programming language for backend development.
- **Spring Boot**: Framework for building microservices with minimal configuration.
- **Spring Web**: Module for building REST APIs and handling HTTP requests.
- **Spring Security**: Authentication and authorization framework.
- **PostgreSQL**: The basis of the database for the project.
- **MapStruct**: Simplifies object mapping between DTOs and entities.
- **Liquibase**: Tool for managing database schema migrations.
- **JUnit & MockMvc**: Frameworks for unit and integration testing.
- **Docker**: Containerization platform for consistent development and deployment environments.
- **Telegram Bot API**: API for sending notification to Telegram bot.
- **Stripe API**: API for making payments
## Features
### Users 👥
- User registration and login.
- JWT-based authentication.
- User roles: USER, GOLD_USER, PRIVILEGED_USER and ADMIN (administrator).
### Accommodation 📚 
- CRUD operations for accommodation: create, read, update, and delete.
- Filter accommodations by categories.
- Search accommodations by title or author.
- Users can only perform GET operations
- Administrator can perform POST, PUT, DELETE operations
### Booking 📂
- All actions are performed only by authenticated users.
- Users can create reservations, view all their reservations, or reservations by id.
- Users can modify, cancel and delete their bookings..
- Admins can search by booking filters or view all and change status
- There is a scheduled hourly check for sold-out reservation
### Payment 🛒
- Users can create a payment based on a booking.
- The payment can be successful or not.
- If a payment is initiated but not completed, it may be exhausted, then the user may renewpayment
### Notification 📋
- Notifications are sent to the Telegram bot for the admin.
- Notifications about about actions of accommodations, bookings and payments.
## Getting Started 🚀
### Setup
1. **Clone the repository:**
```
git clone https://github.com/vladshender/Online-Book-Store.git
```
2. **Build the project:**
```
mvn clean package
```
3. **Build the project:**
```
docker build -t name_image_your_app .
```
4. **Start the application using Docker Compose:**
```
docker-compose up
```
### Connecting to a Custom Database 🗄
To connect to a custom PostgreSQL database, update the application.properties file with your database details:
```
spring.config.import=optional:file:.env[.properties]
spring.datasource.url=jdbc:postgresql://<YOUR_DB_HOST>:<YOUR_DB_PORT>/<YOUR_DB_NAME>?currentSchema=<YOUR_DB_SCHEMA>
spring.datasource.username=<YOUR_DB_USERNAME>
spring.datasource.password=<YOUR_DB_PASSWORD>
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
server.servlet.context-path=/api

jwt.expiration=<TIME_SESSION_EXPERATION_IN_MS>
jwt.secret=<SECRET_WORD>

bot.name=${BOT_NAME}
bot.key=${BOT_KEY}

stripe.secretKey=${STRIPE_SECRET_KEY}
```
### You can explore the api through the Swagger UI:
```
http://localhost:8088/api/swagger-ui/index.html
```
