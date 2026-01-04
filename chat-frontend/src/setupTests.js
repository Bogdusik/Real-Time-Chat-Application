// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

// Mock fetch globally
global.fetch = jest.fn(() => 
  Promise.resolve({
    json: () => Promise.resolve([]),
    ok: true,
    status: 200,
    statusText: 'OK'
  })
);

// Mock SockJS globally
jest.mock('sockjs-client', () => {
  return jest.fn(() => ({
    onopen: null,
    onmessage: null,
    onclose: null,
    send: jest.fn(),
    close: jest.fn(),
    readyState: 1
  }));
});

// Mock STOMP Client globally
jest.mock('@stomp/stompjs', () => {
  const mockActivate = jest.fn();
  const mockDeactivate = jest.fn();
  const mockPublish = jest.fn();
  const mockSubscribe = jest.fn();
  
  return {
    Client: jest.fn().mockImplementation(() => ({
      webSocketFactory: null,
      onConnect: null,
      onDisconnect: null,
      onStompError: null,
      debug: jest.fn(),
      activate: mockActivate,
      deactivate: mockDeactivate,
      connected: true,
      publish: mockPublish,
      subscribe: mockSubscribe
    }))
  };
});

// Mock scrollIntoView for all tests
Element.prototype.scrollIntoView = jest.fn();

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // deprecated
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});
