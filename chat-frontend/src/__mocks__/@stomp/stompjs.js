// Mock for @stomp/stompjs
module.exports = {
  Client: jest.fn().mockImplementation(() => {
    const mockActivate = jest.fn();
    const mockDeactivate = jest.fn();
    const mockPublish = jest.fn();
    const mockSubscribe = jest.fn();
    
    return {
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
    };
  })
};

