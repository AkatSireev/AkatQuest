# AkatQuest 📚

## В ДАННЫЙ МОМЕНТ ПЛАГИН ОЧЕНЬ СЫРОЙ!!!!
AkatQuest — это плагин для Minecraft, позволяющий создавать уникальные квесты с привязкой к NPC. Квесты отображаются в виде книг, наподобие старого режима Azerus, что делает их удобными и привлекательными для игроков. Плагин зависит от нескольких популярных плагинов, что расширяет его функциональность.

<picture>
  <img alt="" src="https://i.imgur.com/XuAPIMJ.png">
</picture>

## ✨ Особенности

- Создание квестов с привязкой к NPC.
- Квесты отображаются в виде книг, как в старом режиме Azerus.
- Легкая настройка.

## 🔧 Зависимости

AkatQuest зависит от следующих плагинов:

- **MythicMobs**: Для создания уникальных мобов (необязательно).
- **Vault**: Для управления экономикой.
- **NBTAPI**: Для работы с NBT-данными предметов.
- **Citizens**: Для работы с NPC.

## 📜 Установка

1. Скачайте последний релиз плагина AkatQuest с [Releases](https://github.com/AkatSireev/AkatQuest/releases).
2. Поместите файл `AkatQuest.jar` в папку `plugins` вашего сервера.
3. Убедитесь, что у вас установлены все зависимости (MythicMobs, Vault, NBTAPI, Citizens).
4. Перезапустите сервер.

## 💬 Обратная связь

Если у вас есть вопросы или предложения по улучшению плагина, не стесняйтесь связаться со мной!

- **Telegram**: [Akat Telegram](https://t.me/AkatSireev)
- **GitHub Issues**: Вы также можете создать **issue** на [GitHub](https://github.com/AkatSireev/AkatQuest/issues), если столкнулись с проблемой.


## 📝 Пример использования

Для создания простого квеста следуйте этим шагам:

1. Создайте NPC с помощью плагина Citizens.
2. Настройте квест в конфигурационном файле, указав NPC и описание квеста.
   
Пример квеста:

```yaml
quests:
  quest1:
    name: "Начало приключений #1"
    description: |
      О, ты пришёл! Это не может быть простым совпадением...
      Я ждал здесь уже несколько дней, надеясь встретить
      кого-то вроде тебя. Легенды говорят, что на этой
      горе иногда появляются герои, что сразили самого
      Дракона Края. Ты... один из них, верно? В твоих глазах
      я вижу пламя отваги!
#   Имя NPC без цвета (в игре NPC может иметь цвет, но здесь указывать его не нужно)
    npcName: "Айрон"
#   В данный момент linear — бесполезная строка, но она нужна
    linear: true
#   Награды для квеста
    rewards:
      reward1:
#       В данный момент может быть три вида наград: item, money, command
        type: money
        amount: 70
      reward2:
        type: item
        name: "&eКоординаты города Гримгал"
        description:
        - ""
        - "&fКоординаты: &ax:60 y:68 z:1100"
        - ""
        material: "paper"
        amount: 1
#       Поле tags отвечает за NBT-теги и не является обязательным
        tags:
          tag1:
            text: "Grimgal"
            text2: "Grimgal"
          tag2:
            text: "Grimgal"
      reward3:
        type: command
#       Все команды выполняются от имени консоли
        command: "minecraft:give %player% minecraft:fishing_rod[minecraft:enchantments={levels:{'lure':4}}] 1"
#   Условия для выполнения квеста
    conditions:
      condition1:
#       В данный момент есть три вида условий: bring_item_with_nbt, kill_mythic_mob, kill_regular_mob
        type: bring_item_with_nbt
        material: "player_head"
        amount: 1
#       Должен ли NPC забрать у игрока предмет
        take: true
#       Теги работают так же, как с наградами, и не являются обязательными
        tags:
#         Пример для плагина EvenMoreFish
          evenmorefish:
            emf-fish-rarity: "Epic"
            emf-fish-name: "Арктический голец"
      condition2:
        type: kill_mythic_mob
        mobName: "SkeletonKing"
        amount: 1
      condition3:
        type: kill_regular_mob
        mobType: ZOMBIE
        amount: 1
    
  quest2:
    name: "Начало приключений #2"
    description: |
      Но есть ещё кое-что, о чём я должен тебе рассказать, герой. 
      В моём городе, Гримгале, недавно произошло нечто странное. 
      Мы обнаружили древний артефакт, подобного которому 
      никто из нас раньше не видел. 
      Сначала мы думали, что это просто древняя реликвия, 
      но позже выяснили, что он... портал. 
      Портал в мир, о котором мы ничего не знаем.
    npcName: "Айрон"
    linear: true
    rewards:
    conditions:
