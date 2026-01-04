const Message = require('../models/Message');

class ChatController {
  async getMessages(req, res) {
    try {
      const messages = await Message.findAll();
      res.json(messages);
    } catch (error) {
      console.error('Error fetching messages:', error);
      res.status(500).json({ 
        error: 'Failed to fetch messages',
        message: error.message 
      });
    }
  }

  async sendMessage(messageData) {
    try {
      const message = await Message.create(messageData);
      return message;
    } catch (error) {
      console.error('Error saving message:', error);
      throw error;
    }
  }
}

module.exports = new ChatController();

