import { Client, LocalAuth } from 'whatsapp-web.js';
import qrcode from 'qrcode-terminal';
import axios from 'axios';
import * as dotenv from 'dotenv';

dotenv.config();

const API_URL = process.env.API_URL || 'http://localhost:3000/api/v1';

// Initialize WhatsApp client
const client = new Client({
  authStrategy: new LocalAuth(),
  puppeteer: {
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  }
});

// QR Code for authentication
client.on('qr', (qr) => {
  console.log('📱 Scan QR code with WhatsApp:');
  qrcode.generate(qr, { small: true });
});

// Ready event
client.on('ready', () => {
  console.log('✅ WhatsApp bot is ready!');
});

// Message handler
client.on('message', async (message) => {
  const chat = await message.getChat();
  const contact = await message.getContact();
  const text = message.body.toLowerCase().trim();

  console.log(`Message from ${contact.pushname}: ${message.body}`);

  // Help command
  if (text === 'help' || text === 'помощь' || text === 'yordam') {
    await message.reply(`
🤖 *Smartup LMS WhatsApp Bot*

Available commands:
• *courses* - Browse available courses
• *mycourses* - View your enrolled courses
• *register* - Register for an account
• *login* - Login to your account
• *help* - Show this help message

Choose your language:
🇬🇧 English | 🇷🇺 Русский | 🇺🇿 O'zbekcha
    `);
    return;
  }

  // Courses command
  if (text === 'courses' || text === 'курсы' || text === 'kurslar') {
    try {
      const response = await axios.get(`${API_URL}/courses`, {
        params: { page: 1, limit: 5 }
      });

      if (response.data.data && response.data.data.length > 0) {
        let replyText = '📚 *Available Courses:*\n\n';
        response.data.data.forEach((course: any, index: number) => {
          replyText += `${index + 1}. *${course.title}*\n`;
          replyText += `   Level: ${course.level}\n`;
          replyText += `   ${course.isFree ? '🆓 Free' : `💰 $${course.price}`}\n\n`;
        });
        replyText += '\nReply with course number to learn more!';
        await message.reply(replyText);
      } else {
        await message.reply('📭 No courses available at the moment.');
      }
    } catch (error) {
      console.error('Error fetching courses:', error);
      await message.reply('❌ Error fetching courses. Please try again later.');
    }
    return;
  }

  // My courses command
  if (text === 'mycourses' || text === 'мои курсы' || text === 'mening kurslarim') {
    await message.reply(`
📚 *Your Enrolled Courses:*

1. Introduction to Programming
2. Web Development Basics

(Demo data - connect to API for real user data)

Reply *courses* to browse more courses!
    `);
    return;
  }

  // Register command
  if (text === 'register' || text === 'регистрация' || text === 'ro\'yxatdan o\'tish') {
    await message.reply(`
📝 *Registration*

To register for Smartup LMS:

1. Visit: https://your-lms-domain.com
2. Click "Sign Up"
3. Fill in your details
4. Verify your email

After registration, you can login here with:
*login <email> <password>*
    `);
    return;
  }

  // Login command
  if (text.startsWith('login') || text.startsWith('войти') || text.startsWith('kirish')) {
    await message.reply(`
🔐 *Login*

Format: *login <email> <password>*

Example: login user@example.com mypassword

For security, we recommend using the web interface at:
https://your-lms-domain.com
    `);
    return;
  }

  // Default response
  await message.reply(`
👋 Hello! I'm the Smartup LMS bot.

Type *help* to see available commands.

🌐 Languages: EN | RU | UZ | AR | KK | TR
  `);
});

// Authentication failure
client.on('auth_failure', (msg) => {
  console.error('❌ Authentication failure:', msg);
});

// Disconnected
client.on('disconnected', (reason) => {
  console.log('⚠️  Client was logged out:', reason);
});

// Error handling
client.on('error', (error) => {
  console.error('❌ WhatsApp client error:', error);
});

// Initialize client
client.initialize()
  .then(() => {
    console.log('🚀 Initializing WhatsApp bot...');
  })
  .catch((error) => {
    console.error('Failed to initialize WhatsApp bot:', error);
    process.exit(1);
  });

// Graceful shutdown
process.on('SIGINT', async () => {
  console.log('⏹️  Shutting down WhatsApp bot...');
  await client.destroy();
  process.exit(0);
});
