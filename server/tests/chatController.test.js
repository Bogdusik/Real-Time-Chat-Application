// Mock database before requiring app
jest.mock('../src/database/db', () => ({
  pool: {
    query: jest.fn()
  },
  initializeDatabase: jest.fn().mockResolvedValue()
}));

const request = require('supertest');
const app = require('../src/index');
const { pool } = require('../src/database/db');

describe('ChatController', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  afterAll((done) => {
    // Close server if needed
    if (app && app.close) {
      app.close(done);
    } else {
      done();
    }
  });

  describe('GET /api/messages', () => {
    it('should return list of messages', async () => {
      const mockMessages = [
        {
          id: 1,
          content: 'Test message',
          timestamp: new Date(),
          user: { id: 1, username: 'testuser' }
        }
      ];

      pool.query.mockResolvedValue({
        rows: mockMessages
      });

      const response = await request(app)
        .get('/api/messages')
        .expect(200);

      expect(response.body).toBeInstanceOf(Array);
      expect(pool.query).toHaveBeenCalled();
    });

    it('should return empty array when no messages', async () => {
      pool.query.mockResolvedValue({
        rows: []
      });

      const response = await request(app)
        .get('/api/messages')
        .expect(200);

      expect(response.body).toEqual([]);
    });

    it('should handle database errors', async () => {
      pool.query.mockRejectedValue(new Error('Database error'));

      const response = await request(app)
        .get('/api/messages')
        .expect(500);

      expect(response.body).toHaveProperty('error');
    });
  });

  describe('GET /health', () => {
    it('should return health status', async () => {
      const response = await request(app)
        .get('/health')
        .expect(200);

      expect(response.body).toEqual({
        status: 'ok',
        message: 'Chat API is running'
      });
    });
  });
});

