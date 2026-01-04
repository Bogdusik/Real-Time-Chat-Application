const Message = require('../src/models/Message');
const { pool } = require('../src/database/db');

jest.mock('../src/database/db', () => ({
  pool: {
    query: jest.fn()
  }
}));

describe('Message Model', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('findAll', () => {
    it('should return all messages ordered by timestamp', async () => {
      const mockMessages = [
        {
          id: 1,
          content: 'Message 1',
          timestamp: new Date('2024-01-01'),
          user: { id: 1, username: 'user1' }
        },
        {
          id: 2,
          content: 'Message 2',
          timestamp: new Date('2024-01-02'),
          user: { id: 2, username: 'user2' }
        }
      ];

      pool.query.mockResolvedValue({
        rows: mockMessages
      });

      const messages = await Message.findAll();

      expect(messages).toHaveLength(2);
      expect(messages[0].content).toBe('Message 1');
      expect(pool.query).toHaveBeenCalled();
    });

    it('should return empty array when no messages', async () => {
      pool.query.mockResolvedValue({
        rows: []
      });

      const messages = await Message.findAll();

      expect(messages).toEqual([]);
    });
  });

  describe('create', () => {
    it('should create a new message with user', async () => {
      const messageData = {
        content: 'New message',
        user: { id: 1 }
      };

      const mockUser = { id: 1, username: 'testuser' };
      const mockMessage = {
        id: 1,
        content: 'New message',
        timestamp: new Date(),
        user: mockUser
      };

      pool.query
        .mockResolvedValueOnce({ rows: [{ id: 1 }] }) // User exists check
        .mockResolvedValueOnce({ rows: [mockMessage] }); // Message insert

      const message = await Message.create(messageData);

      expect(message).toHaveProperty('id');
      expect(message.content).toBe('New message');
      expect(message.user).toEqual(mockUser);
      expect(pool.query).toHaveBeenCalledTimes(2);
    });

    it('should create a message without user', async () => {
      const messageData = {
        content: 'Message without user',
        user: null
      };

      const mockMessage = {
        id: 1,
        content: 'Message without user',
        timestamp: new Date(),
        user: null
      };

      pool.query.mockResolvedValue({
        rows: [mockMessage]
      });

      const message = await Message.create(messageData);

      expect(message).toHaveProperty('id');
      expect(message.content).toBe('Message without user');
      expect(message.user).toBeNull();
    });
  });
});

