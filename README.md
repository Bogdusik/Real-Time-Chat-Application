# 📡 Real-time Chat Application (Node.js + React + WebSocket)

[![CI](https://github.com/Bogdusik/Real-Time-Chat-Application/workflows/CI/badge.svg)](https://github.com/Bogdusik/Real-Time-Chat-Application/actions)
[![Node.js](https://img.shields.io/badge/Node.js-18.x-green.svg)](https://nodejs.org/)
[![Express](https://img.shields.io/badge/Express-4.18.2-lightgrey.svg)](https://expressjs.com/)
[![React](https://img.shields.io/badge/React-19.0.0-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📝 Description
This is a simple yet powerful real-time chat application that allows users to send and receive messages instantly. The project demonstrates how to build a full-stack JavaScript application with:

- **Node.js + Express** for the back-end
- **React** for the front-end
- **WebSocket (STOMP protocol via SockJS)** for real-time communication
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
- **CI/CD pipeline** with automated testing
- **Docker support** for easy deployment
- **Comprehensive test coverage** for backend and frontend

---

## 🚀 Technologies Used
### Backend:
- Node.js 18.x
- Express.js 4.18.2
- WebSocket (ws) with SockJS
- STOMP protocol support
- PostgreSQL (node-postgres)
- Jest & Supertest (for testing)

### Frontend:
- React 19.0.0 (with functional components and hooks)
- Material-UI (MUI) 6.4.8
- Stomp.js & SockJS (for WebSocket connection)
- React Testing Library (for testing)

### DevOps:
- GitHub Actions (CI/CD)
- Docker & Docker Compose
- Nginx (for frontend production)

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

### Prerequisites:
- Node.js 18.x or higher
- PostgreSQL 15 (or use Docker Compose)
- npm 9.0+ or yarn

### Option 1: Using Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL, Backend, Frontend)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

The application will be available at:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

### Option 2: Manual Setup

#### Backend:
1. Navigate to `server` directory
2. Install dependencies:
```bash
npm install
```
3. Make sure PostgreSQL is running and configured (see `.env.example`)
4. Run the server:
```bash
npm start
```

Or in development mode with auto-reload:
```bash
npm run dev
```

#### Frontend:
1. Navigate to `chat-frontend`
2. Install dependencies:
```bash
npm install
```
3. Start the frontend app:
```bash
npm start
```

### Option 3: Docker (Individual Services)

#### Backend:
```bash
docker build -t chat-backend .
docker run -p 8080:8080 \
  -e DB_HOST=localhost \
  -e DB_PORT=5432 \
  -e DB_NAME=chatdb \
  -e DB_USER=chatuser \
  -e DB_PASSWORD=chatpass \
  chat-backend
```

#### Frontend:
```bash
cd chat-frontend
docker build -t chat-frontend .
docker run -p 3000:80 chat-frontend
```

---

## 🧪 Testing

### Backend Tests:
```bash
cd server
npm test
```

### Frontend Tests:
```bash
cd chat-frontend
npm test
```

### Run All Tests:
The CI/CD pipeline automatically runs all tests on push and pull requests.

---

## 🐳 Docker

### Build Images:
```bash
# Backend
docker build -t chat-backend .

# Frontend
cd chat-frontend
docker build -t chat-frontend .
```

### Docker Compose:
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Rebuild and restart
docker-compose up -d --build
```

---

## 📸 Result:
- Users can type and send messages.
- Messages appear instantly with timestamp and username.
- Messages are stored in PostgreSQL.
- Real-time updates via WebSocket.
- Responsive UI with Material-UI components.

---

## 🔧 Configuration

### Environment Variables:
- `PORT`: Backend server port (default: 8080)
- `DB_HOST`: PostgreSQL host (default: localhost)
- `DB_PORT`: PostgreSQL port (default: 5432)
- `DB_NAME`: Database name (default: chatdb)
- `DB_USER`: Database username (default: chatuser)
- `DB_PASSWORD`: Database password (default: chatpass)
- `CORS_ORIGIN`: Allowed CORS origins (default: http://localhost:3000)

See `.env.example` for more details.

---

## 📊 CI/CD

The project includes GitHub Actions workflows that:
- Run backend tests on Java 17 and 21
- Run frontend tests and build
- Build both backend and frontend
- Verify successful builds

---

## ✏️ Author
- **Bogdusik**

---

## 📄 License
This project is licensed under the MIT License.

---

## 🌟 Feel free to clone, explore, and modify!
