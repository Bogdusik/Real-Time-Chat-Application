// IMPORTANT: jest.mock() must be called BEFORE any imports
// Use __mocks__ directory for automatic mocking
jest.mock('sockjs-client');
jest.mock('@stomp/stompjs');

// Mock fetch BEFORE any imports
// This must be done at the module level, not inside a function
Object.defineProperty(global, 'fetch', {
  writable: true,
  value: jest.fn(() => 
    Promise.resolve({
      json: () => Promise.resolve([]),
      ok: true,
      status: 200,
      statusText: 'OK'
    })
  )
});

// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

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
