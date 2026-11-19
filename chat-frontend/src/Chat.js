import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import {
  TextField,
  Button,
  Box,
  Alert,
  Typography,
  Snackbar,
  Avatar,
  IconButton,
  Paper
} from '@mui/material';

// A stable colour per username, so each person keeps the same avatar colour.
const AVATAR_COLORS = ['#f97316', '#22c55e', '#06b6d4', '#a855f7', '#ec4899', '#eab308', '#3b82f6', '#14b8a6'];
function colorFor(name = '') {
  let hash = 0;
  for (const ch of name) hash = (hash * 31 + ch.charCodeAt(0)) >>> 0;
  return AVATAR_COLORS[hash % AVATAR_COLORS.length];
}

function initials(name = '?') {
  return name.trim().slice(0, 2).toUpperCase() || '?';
}

function formatTime(timestamp) {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
}

function SendIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M3.4 20.4 21 12 3.4 3.6l-.01 6.53L15 12 3.39 13.87z" />
    </svg>
  );
}

function ConnectionStatus({ connected }) {
  return (
    <Box
      role="status"
      sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 1.5, py: 0.5, borderRadius: 999,
        bgcolor: connected ? 'rgba(34,197,94,0.12)' : 'rgba(234,179,8,0.12)',
        color: connected ? '#4ade80' : '#facc15', fontSize: 13, fontWeight: 600 }}
    >
      <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'currentColor',
        boxShadow: connected ? '0 0 0 4px rgba(74,222,128,0.18)' : 'none' }} />
      {connected ? 'Connected' : 'Connecting…'}
    </Box>
  );
}

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
        // the server answers a rejected message only to its sender, with the reason
        client.subscribe('/user/queue/errors', (msg) => {
          const { error } = JSON.parse(msg.body);
          setSnackbar({ open: true, message: error || 'Message was rejected', severity: 'error' });
        });
      },
      onDisconnect: () => {
        setConnected(false);
      },
      // the client reconnects on its own; show that instead of a stale "Connected"
      onWebSocketClose: () => {
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

  const snackbarEl = (
    <Snackbar
      open={snackbar.open}
      autoHideDuration={4000}
      onClose={() => setSnackbar(s => ({ ...s, open: false }))}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      <Alert severity={snackbar.severity} variant="filled" onClose={() => setSnackbar(s => ({ ...s, open: false }))}>
        {snackbar.message}
      </Alert>
    </Snackbar>
  );

  const shell = { width: '100%', maxWidth: 760, mx: 'auto', bgcolor: 'background.paper',
    border: '1px solid', borderColor: 'divider', borderRadius: 4, boxShadow: '0 30px 80px rgba(0,0,0,0.45)' };

  if (!currentUser) {
    return (
      <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 2 }}>
        <Paper sx={{ ...shell, maxWidth: 420, p: 4 }}>
          <Typography variant="overline" sx={{ color: 'primary.main', letterSpacing: 2 }}>Realtime Chat</Typography>
          <Typography variant="h5" sx={{ fontWeight: 700, mt: 0.5 }}>Enter your name to join</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1, mb: 3 }}>
            One room, live over STOMP and WebSocket. The same name brings back the same user.
          </Typography>
          <Box display="flex" gap={1.5}>
            <TextField
              fullWidth
              label="Your name"
              value={usernameInput}
              onChange={(e) => setUsernameInput(e.target.value)}
              onKeyDown={handleKeyDown}
              inputProps={{ maxLength: 50 }}
              autoFocus
            />
            <Button variant="contained" onClick={handleJoin} disabled={!usernameInput.trim()} sx={{ px: 3 }}>
              Join
            </Button>
          </Box>
        </Paper>
        {snackbarEl}
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: { xs: 0, sm: 3 } }}>
      <Paper sx={{ ...shell, height: { xs: '100vh', sm: 'min(760px, 90vh)' }, display: 'flex', flexDirection: 'column',
        borderRadius: { xs: 0, sm: 4 }, overflow: 'hidden' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, px: 2.5, py: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.2 }}># general</Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>Signed in as {username}</Typography>
          </Box>
          <ConnectionStatus connected={connected} />
          <Avatar sx={{ width: 34, height: 34, fontSize: 14, fontWeight: 700, bgcolor: colorFor(username) }}>{initials(username)}</Avatar>
        </Box>

        <Box sx={{ flex: 1, overflowY: 'auto', px: { xs: 1.5, sm: 2.5 }, py: 2, display: 'flex', flexDirection: 'column', gap: 1.25 }}>
          {messages.length === 0 && (
            <Typography variant="body2" sx={{ color: 'text.secondary', textAlign: 'center', mt: 6 }}>
              No messages yet. Say hello.
            </Typography>
          )}
          {messages.map((msg) => {
            const author = msg.user?.username || 'Anonymous';
            const own = currentUser && msg.user?.id === currentUser.id;
            return (
              <Box key={msg.id} data-own={own ? 'true' : 'false'}
                sx={{ display: 'flex', gap: 1, alignItems: 'flex-end', flexDirection: own ? 'row-reverse' : 'row' }}>
                {!own && (
                  <Avatar sx={{ width: 30, height: 30, fontSize: 12, fontWeight: 700, bgcolor: colorFor(author) }}>{initials(author)}</Avatar>
                )}
                <Box sx={{ maxWidth: '72%', px: 1.75, py: 1, borderRadius: 3,
                  borderBottomRightRadius: own ? 6 : 24, borderBottomLeftRadius: own ? 24 : 6,
                  bgcolor: own ? 'primary.main' : 'rgba(255,255,255,0.06)', color: own ? '#04130f' : 'text.primary' }}>
                  {!own && (
                    <Typography variant="caption" sx={{ display: 'block', fontWeight: 700, color: colorFor(author) }}>{author}</Typography>
                  )}
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>{msg.content}</Typography>
                  <Typography variant="caption" sx={{ display: 'block', textAlign: 'right', opacity: 0.6, fontSize: 11, mt: 0.25 }}>
                    {formatTime(msg.timestamp)}
                  </Typography>
                </Box>
              </Box>
            );
          })}
          <div ref={messagesEndRef} />
        </Box>

        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', p: 1.5, borderTop: '1px solid', borderColor: 'divider' }}>
          <TextField
            fullWidth
            size="small"
            placeholder={connected ? 'Message #general' : 'Waiting for the connection…'}
            inputProps={{ maxLength: 5000, 'aria-label': 'Enter message' }}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={!connected}
          />
          <IconButton aria-label="Send" onClick={sendMessage} disabled={!connected || !message.trim()}
            sx={{ bgcolor: 'primary.main', color: '#04130f', '&:hover': { bgcolor: 'primary.light' },
              '&.Mui-disabled': { bgcolor: 'rgba(255,255,255,0.08)' } }}>
            <SendIcon />
          </IconButton>
        </Box>
      </Paper>
      {snackbarEl}
    </Box>
  );
}
