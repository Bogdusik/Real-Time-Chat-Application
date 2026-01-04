// Mock for sockjs-client
module.exports = jest.fn(() => ({
  onopen: null,
  onmessage: null,
  onclose: null,
  send: jest.fn(),
  close: jest.fn(),
  readyState: 1
}));

