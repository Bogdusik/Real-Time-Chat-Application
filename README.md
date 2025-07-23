# 📡 Real-time Chat Application (Spring Boot + React + WebSocket)

## 📝 Description
This is a simple yet powerful real-time chat application that allows users to send and receive messages instantly. The project demonstrates how to build a full-stack app with:

- **Spring Boot (Java)** for the back-end
- **React** for the front-end
- **WebSocket (STOMP protocol)** for real-time communication
- **PostgreSQL** as the database

---

## ⚙️ Key Features
- Real-time messaging without page reload
- User-friendly React interface
- Messages stored in a PostgreSQL database
- WebSocket server configured with Spring Boot
- Auto-scroll to the newest message
- Load previous messages on startup via REST API
- Responsive design with Material-UI

---

## 🚀 Technologies Used
### Backend:
- Java 17
- Spring Boot
- Spring WebSocket
- Spring Data JPA
- PostgreSQL
- Lombok (for cleaner code)

### Frontend:
- React (with functional components and hooks)
- Material-UI (MUI)
- Stomp.js & SockJS (for WebSocket connection)

---

## 🏗️ How it works:
1. **Backend**
   - Accepts WebSocket connections on `/ws`
   - Listens for incoming messages on `/app/sendMessage`
   - Saves each message into the PostgreSQL database
   - Broadcasts new messages to `/topic/messages`
   - Exposes REST endpoint `/api/messages` to fetch stored messages

2. **Frontend**
   - Connects to the backend using WebSocket
   - Subscribes to `/topic/messages` to receive real-time updates
   - Sends messages using the STOMP protocol
   - Displays messages in a styled chat card
   - Fetches existing messages on page load

---

## 🗄️ Database Structure:
- **User Table**: `id`, `username`
- **Message Table**: `id`, `content`, `timestamp`, `user_id`

---

## ✅ How to run the project:

### Backend:
- Make sure PostgreSQL is running and configured in `application.properties`
- Run the Spring Boot application from your IDE or with:
```bash
mvn spring-boot:run
```

### Frontend:
- Navigate to `chat-frontend`
- Install dependencies:
```bash
npm install
```
- Start the frontend app:
```bash
npm start
```

---

## 📸 Result:
- Users can type and send messages.
- Messages appear instantly with timestamp and username.
- Messages are stored in PostgreSQL.

---

## ✏️ Author
- **Bogdusik**

---

## 🌟 Feel free to clone, explore, and modify!
