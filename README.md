# BitcoinWallet

Тестовый Bitcoin-кошелёк для сети Signet, реализованный на Android с использованием современного стека технологий и Clean-архитектуры.

## Основные возможности

- Генерация и безопасное хранение ключей (WIF) в AndroidKeystore / DataStore
- Поддержка P2WPKH (bech32) адресов
- Получение UTXO и вычисление баланса через Blockstream Esplora API
- Формирование и подписание транзакций вручную с помощью **bitcoinj**
- Отправка транзакций через REST API Esplora
- Отображение истории транзакций с постраничной подгрузкой
- Вычисление комиссии

## Архитектура

Проект разделён на три уровня по принципам Clean Architecture:

```
Presentation  ↔  Domain  ↔  Data
```

- **Presentation**: Composable UI (Jetpack Compose), ViewModel, StateFlows.
- **Domain**: Interactors.
- **Data**: Реализация репозиториев, сетевой слой (Retrofit2), Storage (DataStore), KeyStoreManager.

## Основные библиотеки

- **Jetpack Compose** — UI-фреймворк
- **Kotlin Coroutines & Flow** — асинхронность и реактивные потоки
- **Dagger 2** — DI
- **Retrofit 2** — HTTP-клиент для Esplora
- **OkHttp + Logging Interceptor** — HTTP логирование
- **bitcoinj** — работа с ключами и транзакциями Bitcoin
- **AndroidX DataStore** — хранение настроек и шифрованной приватной части кошелька

## Внешние API

- **Blockstream Esplora Signet**
    - `GET /address/{address}/utxo` — список UTXO
    - `GET /address/{address}/txs` — первая страница истории
    - `GET /address/{address}/txs/chain/{last_txid}` — постраничная подгрузка
    - `GET /fee-estimates` — список рекомендованных fee-rate
    - `POST /tx` — бродкаст hex-транзакции

## Модульная структура

- `:data`
    - Реализация сетевого слоя (`ApiService`, `NetworkProvider`)
    - Реализация репозиториев (`MainRepositoryImpl`, `WalletRepositoryImpl`)
    - KeyStoreManager, DataStoreManager
- `:domain`
    - Реализация интерактора
    - Логика формирования и подписания транзакций
- `:presentation`
    - `MainViewModel`, состояния экрана (`MainScreenState`)
    - Composables: `MainScreen`, `HistoryList`, `HistoryRow` и т.д.
    - Сущности
    - Интерфейсы interactor и репозиториев
- `:network`
    - `NetworkProvider`
    - `DTO's`