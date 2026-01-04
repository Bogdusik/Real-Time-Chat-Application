const { pool } = require('../database/db');

class Message {
  constructor(data) {
    this.id = data.id;
    this.content = data.content;
    this.timestamp = data.timestamp;
    this.user = data.user;
  }

  static async findAll() {
    const query = `
      SELECT 
        m.id,
        m.content,
        m.timestamp,
        json_build_object(
          'id', u.id,
          'username', u.username
        ) as user
      FROM message m
      LEFT JOIN "user" u ON m.user_id = u.id
      ORDER BY m.timestamp ASC
    `;
    
    const result = await pool.query(query);
    return result.rows.map(row => ({
      id: row.id,
      content: row.content,
      timestamp: row.timestamp,
      user: row.user
    }));
  }

  static async create(messageData) {
    const { content, user } = messageData;
    const userId = user?.id || null;
    
    // If user ID is provided, verify user exists
    let finalUserId = userId;
    if (userId) {
      const userResult = await pool.query('SELECT id FROM "user" WHERE id = $1', [userId]);
      if (userResult.rows.length > 0) {
        finalUserId = userResult.rows[0].id;
      }
    }

    const query = `
      INSERT INTO message (content, timestamp, user_id)
      VALUES ($1, CURRENT_TIMESTAMP, $2)
      RETURNING 
        id,
        content,
        timestamp,
        (SELECT json_build_object('id', id, 'username', username) FROM "user" WHERE id = $2) as user
    `;

    const result = await pool.query(query, [content, finalUserId]);
    const row = result.rows[0];
    
    return {
      id: row.id,
      content: row.content,
      timestamp: row.timestamp,
      user: row.user || null
    };
  }
}

module.exports = Message;

