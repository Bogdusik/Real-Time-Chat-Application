// Mock fetch before any imports
global.fetch = jest.fn(() => 
  Promise.resolve({
    json: () => Promise.resolve([]),
    ok: true,
    status: 200
  })
);

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

const mockActivate = jest.fn();
const mockDeactivate = jest.fn();

jest.mock('@stomp/stompjs', () => ({
  Client: jest.fn().mockImplementation(() => ({
    webSocketFactory: null,
    onConnect: null,
    onDisconnect: null,
    onStompError: null,
    debug: jest.fn(),
    activate: mockActivate,
    deactivate: mockDeactivate,
    connected: true,
    publish: jest.fn(),
    subscribe: jest.fn()
  }))
}));

import { render, screen } from '@testing-library/react';
import App from './App';

test('renders chat application', () => {
  render(<App />);
  // App renders Chat component, so we just check that it renders without errors
  const container = screen.getByRole('main') || document.body;
  expect(container).toBeInTheDocument();
});
