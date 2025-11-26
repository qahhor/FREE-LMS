import { Telegraf, Markup } from 'telegraf';
import axios from 'axios';
import * as dotenv from 'dotenv';

dotenv.config();

const bot = new Telegraf(process.env.TELEGRAM_BOT_TOKEN || '');
const API_URL = process.env.API_URL || 'http://localhost:3000/api/v1';

// Language selection
const languages = {
  en: { name: 'English', flag: '🇬🇧' },
  ru: { name: 'Русский', flag: '🇷🇺' },
  uz: { name: 'O\'zbekcha', flag: '🇺🇿' },
  ar: { name: 'العربية', flag: '🇸🇦' },
  kk: { name: 'Қазақша', flag: '🇰🇿' },
  tr: { name: 'Türkçe', flag: '🇹🇷' }
};

// Translations
const translations = {
  en: {
    welcome: 'Welcome to Smartup LMS! 🎓\n\nChoose a language to continue:',
    main_menu: 'Main Menu',
    my_courses: 'My Courses',
    browse_courses: 'Browse Courses',
    profile: 'Profile',
    help: 'Help',
    language: 'Change Language'
  },
  ru: {
    welcome: 'Добро пожаловать в Smartup LMS! 🎓\n\nВыберите язык:',
    main_menu: 'Главное меню',
    my_courses: 'Мои курсы',
    browse_courses: 'Обзор курсов',
    profile: 'Профиль',
    help: 'Помощь',
    language: 'Изменить язык'
  },
  uz: {
    welcome: 'Smartup LMS ga xush kelibsiz! 🎓\n\nTilni tanlang:',
    main_menu: 'Asosiy menyu',
    my_courses: 'Mening kurslarim',
    browse_courses: 'Kurslarni ko\'rish',
    profile: 'Profil',
    help: 'Yordam',
    language: 'Tilni o\'zgartirish'
  }
};

// User state storage (in production, use Redis or database)
const userStates = new Map<number, { lang: string }>();

// Start command
bot.command('start', (ctx) => {
  const userId = ctx.from?.id;
  if (!userId) return;

  const keyboard = Markup.inlineKeyboard([
    [
      Markup.button.callback(`${languages.en.flag} ${languages.en.name}`, 'lang_en'),
      Markup.button.callback(`${languages.ru.flag} ${languages.ru.name}`, 'lang_ru')
    ],
    [
      Markup.button.callback(`${languages.uz.flag} ${languages.uz.name}`, 'lang_uz'),
      Markup.button.callback(`${languages.ar.flag} ${languages.ar.name}`, 'lang_ar')
    ],
    [
      Markup.button.callback(`${languages.kk.flag} ${languages.kk.name}`, 'lang_kk'),
      Markup.button.callback(`${languages.tr.flag} ${languages.tr.name}`, 'lang_tr')
    ]
  ]);

  ctx.reply(translations.en.welcome, keyboard);
});

// Language selection handler
bot.action(/lang_(.+)/, async (ctx) => {
  const lang = ctx.match[1];
  const userId = ctx.from?.id;

  if (!userId) return;

  userStates.set(userId, { lang });

  await ctx.answerCbQuery();
  await ctx.editMessageText(
    `✅ Language set to: ${languages[lang as keyof typeof languages].name}`
  );

  showMainMenu(ctx, lang);
});

// Main menu
function showMainMenu(ctx: any, lang: string) {
  const t = translations[lang as keyof typeof translations] || translations.en;

  const keyboard = Markup.keyboard([
    [t.my_courses, t.browse_courses],
    [t.profile, t.help],
    [t.language]
  ]).resize();

  ctx.reply(t.main_menu, keyboard);
}

// My courses command
bot.hears(/my courses|мои курсы|mening kurslarim/i, async (ctx) => {
  const userId = ctx.from?.id;
  if (!userId) return;

  try {
    // In production, authenticate user and fetch their courses
    ctx.reply('📚 Your enrolled courses:\n\n1. Introduction to Programming\n2. Web Development Basics\n\n(Demo data - connect to API for real data)');
  } catch (error) {
    ctx.reply('❌ Error fetching courses. Please try again later.');
  }
});

// Browse courses command
bot.hears(/browse courses|обзор курсов|kurslarni ko'rish/i, async (ctx) => {
  try {
    // Fetch courses from API
    const response = await axios.get(`${API_URL}/courses`, {
      params: { page: 1, limit: 5 }
    });

    if (response.data.data && response.data.data.length > 0) {
      let message = '📖 Available Courses:\n\n';
      response.data.data.forEach((course: any, index: number) => {
        message += `${index + 1}. ${course.title}\n`;
        message += `   Level: ${course.level}\n`;
        message += `   ${course.isFree ? '🆓 Free' : `💰 $${course.price}`}\n\n`;
      });
      ctx.reply(message);
    } else {
      ctx.reply('📭 No courses available at the moment.');
    }
  } catch (error) {
    ctx.reply('❌ Error fetching courses. Please try again later.');
  }
});

// Help command
bot.command('help', (ctx) => {
  const helpText = `
🤖 Smartup LMS Bot Help

Available commands:
/start - Start the bot and select language
/help - Show this help message
/courses - Browse available courses
/mycourses - View your enrolled courses
/profile - View your profile

You can also use the menu buttons below!
  `;
  ctx.reply(helpText);
});

// Error handling
bot.catch((err, ctx) => {
  console.error(`Error for ${ctx.updateType}`, err);
  ctx.reply('❌ An error occurred. Please try again later.');
});

// Start bot
bot.launch()
  .then(() => {
    console.log('🤖 Telegram bot started successfully!');
    console.log('Bot username:', bot.botInfo?.username);
  })
  .catch((error) => {
    console.error('Failed to start bot:', error);
    process.exit(1);
  });

// Enable graceful stop
process.once('SIGINT', () => bot.stop('SIGINT'));
process.once('SIGTERM', () => bot.stop('SIGTERM'));
