// Mock STOMP and SockJS before imports
jest.mock('sockjs-client', () => {
  return jest.fn(() => ({
    onopen: null,
    onmessage: null,
    onclose: null,
    send: jest.fn(),
    close: jest.fn()
  }));
});

jest.mock('@stomp/stompjs', () => ({
  Client: jest.fn().mockImplementation(() => ({
    webSocketFactory: null,
    onConnect: null,
    onDisconnect: null,
    onStompError: null,
    debug: jest.fn(),
    activate: jest.fn(),
    deactivate: jest.fn(),
    connected: true,
    publish: jest.fn(),
    subscribe: jest.fn()
  }))
}));

// scrollIntoView is mocked in setupTests.js

// Mock fetch
global.fetch = jest.fn().mockResolvedValue({
  json: async () => []
});

import { render, screen } from '@testing-library/react';
import App from './App';

test('renders chat application', () => {
  render(<App />);
  // App renders Chat component, so we just check that it renders without errors
  const container = screen.getByRole('main') || document.body;
  expect(container).toBeInTheDocument();
});
