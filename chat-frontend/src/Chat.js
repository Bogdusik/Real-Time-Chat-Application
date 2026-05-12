import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import {
  TextField,
  Button,
  List,
  ListItem,
  Container,
  Box,
  Alert,
  Card,
  CardContent,
  Typography,
  Paper,
  Snackbar
} from '@mui/material';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export default function Chat() {
  const [username, setUsername] = useState('');
  const [usernameInput, setUsernameInput] = useState('');
  const [currentUser, setCurrentUser] = useState(null);
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState([]);
  const [connected, setConnected] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });
  const messagesEndRef = useRef(null);
  const stompClientRef = useRef(null);

  useEffect(() => {
    fetch(`${API_URL}/api/messages`)
      .then(res => res.json())
      .then(data => setMessages(data))
      .catch(() => setSnackbar({ open: true, message: 'Failed to load message history', severity: 'warning' }));
  }, []);

  useEffect(() => {
    if (!currentUser) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_URL}/ws`),
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/messages', (msg) => {
          const messageOutput = JSON.parse(msg.body);
          setMessages(prev => [...prev, messageOutput]);
        });
      },
      onDisconnect: () => {
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('Broker error:', frame.headers['message']);
        setSnackbar({ open: true, message: 'WebSocket error: ' + frame.headers['message'], severity: 'error' });
      },
      debug: () => {}
    });

    if (typeof client.activate === 'function') {
      client.activate();
      stompClientRef.current = client;
    }

    return () => {
      if (typeof client.deactivate === 'function') {
        client.deactivate();
      }
    };
  }, [currentUser]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleJoin = async () => {
    const trimmed = usernameInput.trim();
    if (!trimmed) return;
    try {
      const res = await fetch(`${API_URL}/api/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: trimmed })
      });
      if (!res.ok) {
        const err = await res.json();
        setSnackbar({ open: true, message: err.error || 'Failed to register user', severity: 'error' });
        return;
      }
      const user = await res.json();
      setCurrentUser(user);
      setUsername(trimmed);
    } catch {
      setSnackbar({ open: true, message: 'Connection error. Is the server running?', severity: 'error' });
    }
  };

  const sendMessage = () => {
    const client = stompClientRef.current;
    if (!client || !client.connected) {
      setSnackbar({ open: true, message: 'WebSocket not connected yet. Please wait.', severity: 'warning' });
      return;
    }
    if (!message.trim()) {
      setSnackbar({ open: true, message: 'Message cannot be empty.', severity: 'warning' });
      return;
    }
    client.publish({
      destination: '/app/sendMessage',
      body: JSON.stringify({ content: message, user: { id: currentUser.id } })
    });
    setMessage('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      currentUser ? sendMessage() : handleJoin();
    }
  };

  if (!currentUser) {
    return (
      <Box sx={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <Container maxWidth="xs">
          <Card sx={{ p: 3, boxShadow: 4 }}>
            <CardContent>
              <Typography variant="h5" gutterBottom align="center">
                Enter your name to join
              </Typography>
              <Box display="flex" gap={2} mt={2}>
                <TextField
                  fullWidth
                  label="Your name"
                  value={usernameInput}
                  onChange={(e) => setUsernameInput(e.target.value)}
                  onKeyDown={handleKeyDown}
                  inputProps={{ maxLength: 50 }}
                  autoFocus
                />
                <Button variant="contained" onClick={handleJoin} disabled={!usernameInput.trim()}>
                  Join
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Container>
        <Snackbar
          open={snackbar.open}
          autoHideDuration={4000}
          onClose={() => setSnackbar(s => ({ ...s, open: false }))}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        >
          <Alert severity={snackbar.severity} onClose={() => setSnackbar(s => ({ ...s, open: false }))}>
            {snackbar.message}
          </Alert>
        </Snackbar>
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <Container maxWidth="sm">
        {!connected && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            Connecting to WebSocket...
          </Alert>
        )}

        <Card sx={{ p: 3, boxShadow: 4 }}>
          <CardContent>
            <Typography variant="h5" gutterBottom align="center">
              Chat — {username}
            </Typography>

            <Box display="flex" gap={2} mb={2}>
              <TextField
                fullWidth
                label="Enter message"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                disabled={!connected}
                inputProps={{ maxLength: 5000 }}
              />
              <Button variant="contained" onClick={sendMessage} disabled={!connected}>
                Send
              </Button>
            </Box>

            <Paper sx={{ maxHeight: 300, overflowY: 'auto', p: 1 }}>
              <List>
                {messages.map((msg) => (
                  <ListItem key={msg.id} sx={{ mb: 1, p: 1, borderRadius: 2, backgroundColor: '#f5f5f5' }}>
                    <Box>
                      <Typography variant="subtitle2" color="primary">
                        {msg.user?.username || 'Anonymous'}:
                      </Typography>
                      <Typography variant="body1">{msg.content}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {msg.timestamp ? `${msg.timestamp}` : ''}
                      </Typography>
                    </Box>
                  </ListItem>
                ))}
                <div ref={messagesEndRef} />
              </List>
            </Paper>
          </CardContent>
        </Card>
      </Container>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar(s => ({ ...s, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar(s => ({ ...s, open: false }))}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
