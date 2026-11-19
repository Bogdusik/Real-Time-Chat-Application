import React from 'react';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import Chat from './Chat';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#2dd4bf', light: '#5eead4' },
    background: { default: '#0b1020', paper: '#111827' },
    divider: 'rgba(255,255,255,0.08)',
  },
  shape: { borderRadius: 12 },
  typography: { fontFamily: "'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif", button: { textTransform: 'none', fontWeight: 600 } },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Chat />
    </ThemeProvider>
  );
}

export default App;
