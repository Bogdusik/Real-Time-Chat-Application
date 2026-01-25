# Real-Time Chat Application

A full-stack real-time chat application built with Node.js, Express, and React. Features instant messaging via WebSocket (STOMP protocol via SockJS), message persistence in PostgreSQL, and a responsive Material-UI interface. Perfect for learning WebSocket-based communication in a full JavaScript stack.

## Demo

![Chat Interface](screenshots/chat-interface.png)
![Real-Time Messaging](screenshots/realtime-messaging.png)
![Responsive Design](screenshots/responsive.png)

## Why It's Cool

- **Real-Time Communication**: Instant messaging without page reloads using WebSocket (STOMP via SockJS) for bidirectional communication
- **Full JavaScript Stack**: Complete full-stack application using Node.js/Express backend and React frontend
- **Message Persistence**: All messages stored in PostgreSQL database with REST API for fetching message history
- **Auto-Scroll & UX**: Smooth user experience with auto-scroll to newest messages and Material-UI components
- **Comprehensive Testing**: Full test coverage for both backend (Jest/Supertest) and frontend (React Testing Library)
- **Docker Ready**: Complete Docker Compose setup for easy deployment with PostgreSQL, backend, and frontend services

## Tech Stack

- **Backend**: Node.js 18, Express.js, WebSocket (ws), SockJS, STOMP protocol, PostgreSQL (node-postgres)
- **Frontend**: React 19, Material-UI (MUI), Stomp.js, SockJS-client
- **Testing**: Jest, Supertest, React Testing Library
- **DevOps**: Docker, Docker Compose, Nginx, GitHub Actions (CI/CD)

## How to Run Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Bogdusik/Real-Time-Chat-Application.git
   cd Real-Time-Chat-Application
   ```

2. **Start with Docker Compose (Recommended):**
   ```bash
   docker-compose up -d
   ```
   Application will be available at:
   - Frontend: `http://localhost:3000`
   - Backend API: `http://localhost:8080`

   **Alternative (Manual Setup):**
   ```bash
   # Backend
   cd server
   npm install
   npm start
   
   # Frontend (in new terminal)
   cd chat-frontend
   npm install
   npm start
   ```

3. **Configure environment variables:**
   Create `.env` file in `server/` directory:
   ```
   PORT=8080
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=chatdb
   DB_USER=chatuser
   DB_PASSWORD=chatpass
   CORS_ORIGIN=http://localhost:3000
   ```

> **Important**: Never hardcode secrets. Always use `.env` file for sensitive data.

## Project Structure

```
Real-Time-Chat-Application/
├── server/                       # Node.js/Express Backend
│   ├── src/
│   │   ├── index.js             # Express server entry point
│   │   ├── controllers/         # Request handlers
│   │   │   └── chatController.js
│   │   ├── database/            # Database connection
│   │   │   └── db.js
│   │   ├── models/               # Data models
│   │   │   └── Message.js
│   │   └── websocket/           # WebSocket handling
│   │       └── websocketHandler.js
│   ├── tests/                   # Backend tests
│   │   ├── chatController.test.js
│   │   └── message.test.js
│   └── package.json
│
├── chat-frontend/                # React Frontend
│   ├── src/
│   │   ├── App.js               # Main app component
│   │   ├── Chat.js              # Chat component
│   │   ├── __tests__/           # Frontend tests
│   │   │   └── Chat.test.js
│   │   └── __mocks__/           # Test mocks
│   │       ├── @stomp/stompjs.js
│   │       └── sockjs-client.js
│   ├── public/                  # Static assets
│   ├── Dockerfile               # Frontend Docker config
│   └── package.json
│
├── src/                          # Spring Boot (legacy/alternative)
│   └── main/java/                # Java backend code
│
├── .github/workflows/            # CI/CD pipelines
│   └── ci.yml
│
├── docker-compose.yml            # Docker Compose configuration
├── Dockerfile                    # Backend Docker config
└── package.json                  # Root package.json
```

## What I Learned

- **WebSocket Communication**: Implemented real-time bidirectional communication using WebSocket with STOMP protocol and SockJS for browser compatibility
- **Full-Stack JavaScript**: Built complete application with Node.js/Express backend and React frontend, understanding the full request-response cycle
- **Real-Time Architecture**: Designed system architecture for instant message delivery, broadcasting, and persistence
- **Database Integration**: Integrated PostgreSQL with Node.js using node-postgres, implementing message storage and retrieval
- **Testing Strategies**: Wrote comprehensive tests for both backend (API endpoints, WebSocket handlers) and frontend (React components, user interactions)
- **Docker Deployment**: Containerized full-stack application with Docker Compose, including database, backend, and frontend services

Fork it, use it, improve it - open to PRs!
