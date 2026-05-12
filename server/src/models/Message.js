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

    let finalUserId = null;
    if (userId) {
      const userResult = await pool.query('SELECT id FROM "user" WHERE id = $1', [userId]);
      if (userResult.rows.length > 0) {
        finalUserId = userResult.rows[0].id;
      }
    }

    const query = `
      WITH inserted AS (
        INSERT INTO message (content, timestamp, user_id)
        VALUES ($1, CURRENT_TIMESTAMP, $2)
        RETURNING id, content, timestamp, user_id
      )
      SELECT i.id, i.content, i.timestamp,
             u.id AS uid, u.username
      FROM inserted i
      LEFT JOIN "user" u ON i.user_id = u.id
    `;

    const result = await pool.query(query, [content, finalUserId]);
    const row = result.rows[0];

    return {
      id: row.id,
      content: row.content,
      timestamp: row.timestamp,
      user: row.uid ? { id: row.uid, username: row.username } : null
    };
  }
}

module.exports = Message;

