# TechDeals — Community-Ranked Tech Deals Platform

A full-stack tech deals platform inspired by SlickDeals, with a focus on technology products. Features include community voting, benchmark data integration, price history charts, faceted search/filtering, and personalized user feeds.

## Architecture

| Layer | Technology | Purpose |
|---|---|---|
| **Frontend** | SvelteKit + Tailwind CSS | SSR/CSR UI, deal browsing, voting, search |
| **Backend API** | Spring Boot 3.2 (Java 17) | REST API, business logic, scheduled jobs |
| **Database** | PostgreSQL 16 | Source of truth — users, deals, votes, price history |
| **Search Engine** | Elasticsearch 8.12 | Faceted filtering, full-text search, personalized feeds |
| **Cache / Queue** | Redis 7 | Benchmark cache, session state, async job queuing |

```
┌─────────────────────────────────────────────────────────┐
│                    User Browser                          │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP
┌──────────────────────▼──────────────────────────────────┐
│              SvelteKit Frontend  (:3000)                 │
│   Deal listing · Search/Filter · Voting · Price Charts   │
└──────────────────────┬──────────────────────────────────┘
                       │ /api proxy → :8080
┌──────────────────────▼──────────────────────────────────┐
│             Spring Boot Backend  (:8080)                 │
│  Auth · Deals CRUD · Vote logic · Benchmark enrichment   │
│  Price history · ES sync · Scheduled price updates       │
└─────┬──────────────┬────────────────┬───────────────────┘
      │              │                │
┌─────▼──────┐ ┌────▼──────┐ ┌──────▼──────┐
│ PostgreSQL │ │   Elastic  │ │    Redis    │
│  :5432     │ │  search    │ │   :6379     │
│            │ │  :9200     │ │             │
└────────────┘ └───────────┘ └─────────────┘
```

## Features

### 🔥 Community Voting & Hot Score Ranking
- Thumbs up / thumbs down voting (one vote per user per deal)
- Optimistic UI updates for instant feedback
- Time-decay hot score algorithm: **Score = (U − D) / (T + 2)^1.8** where T = hours since posting
- Hot Deals feed sorted by hot score

### 🔍 Search & Faceted Filtering (Elasticsearch)
- Full-text search across title and description
- Sidebar filters: category, price range, retailer, CPU brand, GPU brand, minimum benchmark score
- Sort by: Hot Score, Newest, Price (Low→High), Price (High→Low)
- URL-synced filter state for shareable links

### 📊 Benchmark Integration
- CPU and GPU benchmark scores from the `components` database
- Automatic benchmark enrichment when deals are submitted (matches CPU/GPU model names)
- Visual benchmark bar charts on deal detail pages (PassMark scale)
- Color-coded benchmark badges: 🟢 High-end (≥15k) · 🟡 Mid-range (8k–15k) · 🔴 Budget (<8k)

### 📈 Price History
- Time-series price tracking stored in PostgreSQL
- Interactive line chart (Chart.js) on every deal detail page
- Hourly background scheduler updates prices and recalculates hot scores

### 👤 Personalized User Feeds
- Users set interest tags (e.g., "SFF PC", "OLED Monitor", "RTX 4090")
- Elasticsearch `function_score` query weights deals matching user tags and preferred categories
- Falls back to hot deals feed for unauthenticated users

### 🔒 Authentication
- JWT-based authentication (stateless REST API)
- BCrypt password hashing
- Protected routes for submitting deals and accessing the personalized feed

## Quick Start

### Prerequisites
- [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/)

### Run with Docker Compose

```bash
git clone https://github.com/dlin2028/tech-deals-forum.git
cd tech-deals-forum
docker compose up --build
```

This starts:
- **Frontend** at http://localhost:3000
- **Backend API** at http://localhost:8080
- **Elasticsearch** at http://localhost:9200
- **PostgreSQL** at localhost:5432
- **Redis** at localhost:6379

### Local Development

#### Backend
```bash
cd backend
# Requires running PostgreSQL, Elasticsearch, and Redis (use docker compose for infra)
docker compose up postgres elasticsearch redis -d
mvn spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

## Project Structure

```
tech-deals-forum/
├── docker-compose.yml          # Full stack orchestration
├── backend/                    # Spring Boot application
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/techdeals/
│       ├── TechDealsApplication.java
│       ├── config/             # Security, ES, Redis, Cache config
│       ├── controller/         # REST controllers
│       │   ├── AuthController.java
│       │   ├── DealController.java
│       │   ├── VoteController.java
│       │   ├── PriceHistoryController.java
│       │   └── ComponentController.java
│       ├── service/            # Business logic
│       │   ├── DealService.java        # ES search + JPA fallback
│       │   ├── VoteService.java        # Transactional voting
│       │   ├── BenchmarkService.java   # Cached benchmark lookups
│       │   ├── ElasticsearchSyncService.java
│       │   ├── PriceHistoryService.java
│       │   ├── PriceUpdateScheduler.java  # @Scheduled hourly job
│       │   └── UserService.java
│       ├── model/              # JPA entities
│       ├── elasticsearch/      # ES documents
│       ├── repository/         # JPA + ES repositories
│       ├── dto/                # Request/Response DTOs
│       ├── security/           # JWT provider + filter
│       └── exception/          # Global exception handler
└── frontend/                   # SvelteKit application
    ├── Dockerfile
    ├── package.json
    └── src/
        ├── lib/
        │   ├── api.js              # Axios API client
        │   ├── stores/             # auth.js, deals.js
        │   └── components/         # Reusable Svelte components
        │       ├── DealCard.svelte
        │       ├── VoteButtons.svelte  # Optimistic UI
        │       ├── BenchmarkBadge.svelte
        │       ├── FilterSidebar.svelte
        │       ├── PriceChart.svelte   # Chart.js integration
        │       ├── DealSkeleton.svelte
        │       ├── SearchBar.svelte
        │       └── TagInput.svelte
        └── routes/
            ├── +layout.svelte      # Navbar, footer
            ├── +page.svelte        # Home: Hot/New/Feed tabs
            ├── search/+page.svelte # Search with filters
            ├── deals/[id]/+page.svelte  # Deal detail + price chart
            ├── login/+page.svelte
            ├── register/+page.svelte
            ├── submit/+page.svelte # Submit deal form
            └── profile/+page.svelte    # User preferences & feed tags
```

## API Reference

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/auth/me` | Get current user |

### Deals
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/deals` | List deals (paginated) |
| POST | `/api/deals` | Submit a new deal 🔒 |
| GET | `/api/deals/{id}` | Get deal detail |
| GET | `/api/deals/search` | Search with filters |
| GET | `/api/deals/hot` | Hot deals by time-decay score |
| GET | `/api/deals/feed` | Personalized feed 🔒 |

#### Search Query Parameters
`q`, `category`, `min_price`, `max_price`, `cpu_brand`, `gpu_brand`, `min_benchmark`, `sort` (`hot`/`new`/`price_asc`/`price_desc`)

### Voting
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/deals/{id}/vote` | Cast vote `{"vote_type": "UP"/"DOWN"}` 🔒 |
| DELETE | `/api/deals/{id}/vote` | Remove vote 🔒 |

### Price History
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/deals/{id}/price-history` | Get price history array |

### Components (Benchmarks)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/components/search?q=i9-14900K` | Search benchmark database |
| POST | `/api/components` | Add component with benchmark 🔒 |

## Environment Variables

### Backend
| Variable | Default | Description |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/techdeals` | PostgreSQL URL |
| `DATABASE_USER` | `techdeals` | Database user |
| `DATABASE_PASSWORD` | `techdeals` | Database password |
| `ELASTICSEARCH_URL` | `http://localhost:9200` | Elasticsearch URL |
| `REDIS_HOST` | `localhost` | Redis host |
| `JWT_SECRET` | *(dev default)* | **Change in production!** |
| `CORS_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins |

## Hot Score Algorithm

The front page ranking uses a time-decay gravity algorithm:

```
Score = (U - D) / (T + 2)^1.8
```

Where:
- **U** = upvotes
- **D** = downvotes  
- **T** = hours since posting
- **1.8** = gravity multiplier (controls decay speed)

Scores are recalculated on every vote and hourly by the background scheduler.
