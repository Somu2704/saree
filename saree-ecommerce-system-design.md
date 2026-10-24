# Saree E-Commerce Platform — System Design

> Dedicated **website + mobile app** for a saree manufacturing business.
> Catalog is **photo & video heavy** (model shoots), with online payments,
> inventory, orders, and an admin panel for the internal team.
>
> **Design inspiration:** Apple.com-grade visual polish (generous whitespace,
> large full-bleed imagery, restrained typography, subtle motion) applied to a
> product-forward saree catalog experience similar in spirit to
> shriimant.com — hero photo/video banners, zoomable product photography,
> smooth category browsing, and a frictionless checkout.
>
> **Companion instruction file:** `CLAUDE.md` — read it first. It tells the
> coding agent *how* to build this (phase order, definition of done). This
> file tells the agent *what* to build and why.

---

## Table of Contents

0. [How the Agent Should Use This Document](#0-how-the-agent-should-use-this-document)
1. [Business Overview](#1-business-overview)
2. [Recommended Tech Stack](#2-recommended-tech-stack)
3. [Product Channels](#3-product-channels)
4. [High-Level Architecture](#4-high-level-architecture)
5. [Scale-Out Architecture](#5-scale-out-architecture)
6. [Core Business Modules](#6-core-business-modules)
7. [Frontend & UI/UX Design Language](#7-frontend--uiux-design-language)
8. [Backend Design (Spring Boot)](#8-backend-design-spring-boot)
9. [Database Design](#9-database-design)
10. [Media Architecture (Images & Video Streaming)](#10-media-architecture-images--video-streaming)
11. [Payment Integration](#11-payment-integration)
12. [API Design](#12-api-design)
13. [Authentication & API Security](#13-authentication--api-security)
14. [Deployment Architecture (Docker + Kubernetes on AWS)](#14-deployment-architecture-docker--kubernetes-on-aws)
15. [CI/CD](#15-cicd)
16. [Performance & Scalability](#16-performance--scalability)
17. [Search & Filtering](#17-search--filtering)
18. [Notifications](#18-notifications)
19. [Analytics & Dashboard](#19-analytics--dashboard)
20. [Maintenance Plan](#20-maintenance-plan)
21. [Delivery Roadmap (MVP → Phase 2)](#21-delivery-roadmap-mvp--phase-2)
22. [Final Recommended Stack](#22-final-recommended-stack)

---

## 0. How the Agent Should Use This Document

- This is the **source of truth for architecture and schema**. `CLAUDE.md` is
  the source of truth for **process** (phase order, definition of done).
- Never contradict this doc without asking the user first — especially the
  tech stack (§2), database shape (§9), payment flow (§11), and deployment
  target (§14: **Docker + Kubernetes (EKS) on AWS**).
- Build **phase by phase** per `CLAUDE.md` §3. Do not jump ahead to video
  streaming infra or Kubernetes manifests before Auth/Catalog/Payments work.
- When implementing the UI (§7), hold it to the bar described there: Apple-style
  restraint + shriimant.com-style product-forward merchandising. If unsure
  whether a UI choice meets that bar, ask rather than guessing.

---

## 1. Business Overview

| Aspect | Detail |
|---|---|
| **Business** | Saree manufacturing & direct selling |
| **Key differentiator** | High-quality model photos & videos per saree |
| **Primary goals** | Showcase catalog beautifully, sell online, manage stock & orders |
| **Channels** | SEO website, mobile app, internal admin panel |
| **Scale expectation** | Start small, grow to medium/high traffic over time |

**Design principles**
- Media-first: image/video delivery must be fast and high quality.
- Start simple (modular monolith), scale when needed.
- Never trust the client for payments — always verify server-side.
- SEO matters for a fashion brand — the website must rank on Google.

---

## 2. Recommended Tech Stack

| Layer | Recommendation | Why |
|---|---|---|
| **Backend** | Spring Boot | Strong for APIs, payments, order workflows |
| **Web frontend** | Next.js (React) | Best for SEO, fast product pages |
| **Mobile app** | React Native | One codebase for Android + iOS |
| **Admin panel** | Next.js / React Admin | Fast internal tooling |
| **Database** | PostgreSQL | Reliable relational data + transactions |
| **Cache** | Redis | Faster catalog & session performance |
| **Media storage** | AWS S3 or Cloudinary | Built for images/videos |
| **CDN** | CloudFront | Fast global media delivery |
| **Payments** | Razorpay (India first) | Best fit for India; Stripe later |
| **Search** | PostgreSQL → OpenSearch later | Avoid over-engineering early |
| **Containerization** | Docker | Consistent local/staging/prod images |
| **Orchestration** | Kubernetes (AWS EKS) | Auto-scaling, self-healing, portable across clouds |
| **Video streaming** | HLS (adaptive bitrate) via MediaConvert/FFmpeg workers + CloudFront | Smooth playback on slow mobile networks |
| **Deployment** | AWS (EKS + ECR) | Full control over scaling, matches team's k8s requirement |

---

## 3. Product Channels

You need **2 customer channels** and **1 internal system**:

### Website (public)
- Product browsing & SEO
- Category & collection pages
- Product detail pages (photos + videos)
- Cart & checkout

### Mobile App
- Repeat customers & notifications
- Wishlist, fast browsing
- Order tracking

### Admin Panel (internal)
- Upload saree photos/videos
- Manage stock, variants, pricing
- Orders, banners, coupons
- Customer support actions

---

## 4. High-Level Architecture

```text
            Customers (Web / Mobile App)
                        |
                        v
              CDN  +  Load Balancer
                        |
                        v
   Frontend:  Next.js Website  |  React Native App
                        |
                        v
            Spring Boot Backend APIs
                        |
        +---------------+----------------+
        |               |                |
        v               v                v
   PostgreSQL         Redis       Object Storage
   (orders, users,  (cache,       (S3 / Cloudinary)
    catalog)         sessions)     images & videos
                        |
                        v
             Payment Gateway (Razorpay)
```

---

## 5. Scale-Out Architecture

When traffic grows, evolve to:

```text
                         Users
                           |
                          CDN
                           |
              API Gateway / Load Balancer
                           |
              Spring Boot App Instances (N)
                           |
   +----------+-----------+-----------+-------------+
   |          |           |           |             |
   v          v           v           v             v
PostgreSQL  Redis     Message Queue  Media        Search
(primary +  (cache)   (SQS/RabbitMQ) Storage      (OpenSearch)
 replicas)                 |         (S3 + CDN)
                           v
              Background Workers
         (media processing, notifications)
```

---

## 6. Core Business Modules

| Module | Responsibility |
|---|---|
| **Catalog** | Saree details, fabric, color, occasion, price, stock, media |
| **Product Media** | Upload, compress, thumbnail, video preview, CDN delivery |
| **User** | Registration/login (OTP/password), addresses, profile, wishlist |
| **Cart & Checkout** | Add/remove items, coupons, shipping, tax, payment |
| **Order** | Pending → paid → packed → shipped → delivered / returned |
| **Inventory** | Stock per saree, low-stock alerts, reservation at checkout |
| **Admin** | Dashboard, product/media upload, orders, offers, analytics |
| **Notification** | Order, payment, shipping updates, abandoned cart, launches |

**Catalog attributes (example):** name, SKU, description, fabric, color,
occasion (wedding/party/casual/festival), price, discount price, stock,
images, videos, model details, size/length/blouse info, tags, collection.

---

## 7. Frontend & UI/UX Design Language

### Visual direction (reference: Apple.com + shriimant.com)
The site must feel **premium, calm, and product-first** — not a typical
cluttered Indian e-commerce page. Concretely:

| Principle | Apple-style implementation |
|---|---|
| **Generous whitespace** | Large margins, no dense grids; let the saree photography breathe |
| **Full-bleed hero media** | Homepage & collection pages open with full-width photo or looping muted video |
| **Restrained typography** | 1–2 font families, large bold headlines, light body text, strong hierarchy |
| **Subtle motion** | Fade/slide-in on scroll, smooth parallax on hero, gentle hover zoom on product cards — never gimmicky |
| **Color** | Neutral background (white/off-white/charcoal), let saree colors be the accent |
| **Product-forward (shriimant-style)** | Large tappable product tiles, quick "auto-apply offer" banners, sticky add-to-cart bar on mobile, zoomable product images, model-video previews on hover/tap |
| **Frictionless checkout** | Minimal steps, address autofill, persistent cart, guest checkout allowed |
| **Consistency** | Shared design-token system (spacing, color, type scale) used by web, admin, and mobile |

### Website → **Next.js**
- Best for SEO (critical for fashion e-commerce)
- SSR/SSG for fast, crawlable product detail pages
- `next/image` for responsive, lazy-loaded, auto-format (AVIF/WebP) images
- Native `<video>` + `hls.js` (or a player like `video.js`/`Vidstack`) for
  adaptive-bitrate product & hero videos (see §10)
- Design system: Tailwind CSS + a small component library (shadcn/ui or
  Radix primitives) styled to the Apple-like token system above
- Framer Motion (or CSS scroll-driven animations) for the subtle motion layer
- Core Web Vitals budget: LCP < 2.5s, CLS < 0.1 even with heavy media —
  achieved via poster images, lazy video load, and CDN-first delivery

### Mobile App → **React Native**
- One app for Android + iOS
- Reuse the same design tokens and API contracts as the web app
- Native video player (e.g., `react-native-video`) with HLS support

### Admin Panel → **Next.js Admin / React Admin**
- Fast internal tooling for product & order management
- Drag-and-drop media upload with reordering, progress bars, and
  thumbnail/video-preview generation status

> **Fastest MVP route:** build the **website first**, then the app in Phase 2.
> Agents implementing UI should prefer composing existing accessible
> primitives (Radix/shadcn) over hand-rolling interactive components.

---

## 8. Backend Design (Spring Boot)

Logical modules inside **one modular monolith** (Phase 1):

```text
Spring Boot Application
├── Auth Service
├── User Service
├── Catalog Service
├── Cart Service
├── Order Service
├── Payment Service
├── Inventory Service
├── Notification Service
├── Media Service
└── Admin Service
```

**Why a modular monolith first?**
- Easier to build, deploy & maintain
- Faster delivery for a small team
- Still scalable for early/medium traffic

**Later, split out** when needed: Payment, Notification, Media processing, Search.

---

## 9. Database Design

**Primary DB: PostgreSQL** — relational integrity + transactions for
order/payment consistency, plus easy reporting.

### Entity Relationship (simplified)

```text
users ──< addresses
users ──< orders ──< order_items >── products
users ──< cart ──< cart_items >── products
users ──< wishlist >── products
users ──< reviews >── products
products ──< product_media
products >──< categories      (product_categories)
products >──< collections      (product_collections)
orders ──< payments
products ──< inventory_ledger
coupons (standalone, applied to orders)
```

### Key Tables

**users** — `id, name, phone, email, password_hash, auth_type, status, created_at`

**addresses** — `id, user_id, full_address, city, state, pincode, country, is_default`

**products** — `id, sku, name, slug, description, fabric, color, occasion,
base_price, sale_price, currency, stock_quantity, status, created_at`

**product_media** — `id, product_id, media_type(image/video), url,
thumbnail_url, sort_order, is_primary`

**categories** — `id, name, slug, parent_id`
**product_categories** — `product_id, category_id`

**collections** — `id, name, season, banner_image, active`
**product_collections** — `product_id, collection_id`

**cart** — `id, user_id, status`
**cart_items** — `id, cart_id, product_id, quantity, price_snapshot`

**orders** — `id, order_number, user_id, total_amount, discount_amount,
shipping_amount, tax_amount, final_amount, order_status, payment_status,
address_snapshot, created_at`

**order_items** — `id, order_id, product_id, product_name_snapshot,
price_snapshot, quantity`

**payments** — `id, order_id, gateway, gateway_order_id, gateway_payment_id,
amount, status, method, created_at`

**coupons** — `id, code, type, value, min_order_amount, valid_from, valid_to, active`

**inventory_ledger** — `id, product_id, change_type, quantity_delta, reference_id, created_at`

**wishlist** — `id, user_id, product_id`

**reviews** — `id, user_id, product_id, rating, comment, approved`

> **Snapshots** (price/address/name) are stored on orders so historical
> records stay correct even if products or prices change later.

---

## 10. Media Architecture (Images & Video Streaming)

This is the **most important** part for a saree business — the whole brand
feel (Apple/shriimant-style polish) rides on how fast and sharp media loads.

### Upload & delivery flow — images

```text
1. Admin uploads photo
2. Backend returns a short-lived SIGNED upload URL (S3 presigned POST)
3. File uploads DIRECTLY to S3 (never through the API/server)
4. S3 event → RabbitMQ/SQS → worker generates:
     - multiple responsive sizes (e.g. 400/800/1200/2000px)
     - WebP/AVIF variants + original fallback
     - thumbnail + blurhash/placeholder for instant perceived load
5. CloudFront (CDN) serves media globally with long cache TTLs + versioned URLs
```

### Upload & delivery flow — video (streaming, not plain MP4)

```text
1. Admin uploads raw model-shoot video (signed S3 upload, direct to a
   "raw-video" bucket/prefix)
2. S3 event → queue → transcoding worker (AWS MediaConvert or
   FFmpeg in a worker pod) produces:
     - HLS (.m3u8 + .ts segments) at multiple renditions (e.g. 360p/540p/720p/1080p)
     - a poster frame (JPEG) extracted at ~1s for the <video poster>
     - an optional short muted auto-loop preview clip for hover/tap states
3. Rendition manifests + segments are written to a "processed-video" bucket
4. CloudFront serves the HLS manifest + segments with adaptive bitrate —
   the player automatically switches quality based on the viewer's network
5. Product/media record stores: hls_manifest_url, poster_url, duration,
   available_renditions, processing_status
```

**Why HLS instead of a single MP4:** adaptive bitrate avoids buffering on
slow mobile connections (critical for India), enables instant seek, and lets
the same source serve both a quick hover-preview and full-quality playback.

### Background processing contract
- Media rows start `status = PENDING` → worker updates to `PROCESSING` →
  `READY` (with derived URLs) or `FAILED` (with error reason, retried via
  dead-letter queue).
- The product page/API must never block on transcoding — it serves the
  original/poster immediately and upgrades to streaming once `READY`.

### Rules
- **Never store media blobs in the database** (slow, expensive, heavy backups) —
  only URLs + metadata (dimensions, duration, renditions, blurhash).
- Use **signed URLs** for secure uploads (short expiry, scoped to one object key).
- Offload all transformations to background workers so APIs stay fast.
- Serve responsive `srcset`/`sizes` images and HLS video — never a single
  giant unoptimized file.
- Optional watermarking to protect catalog photos/videos from scraping.

### Tool choice
| Option | Best when |
|---|---|
| **AWS MediaConvert + S3 + CloudFront** | Full control, scales with the rest of the AWS/K8s stack, pay-per-use transcoding |
| **Cloudinary / Mux** | Fastest to integrate, less ops, higher per-GB cost at scale |

> MVP can start with Cloudinary (image+video transformations as a managed
> service) and migrate to self-managed MediaConvert + CloudFront once volume
> justifies the switch — keep the `product_media` schema (§9) agnostic to
> the provider (store generic URLs, not provider-specific IDs).

---

## 11. Payment Integration

### Recommended: **Razorpay** (best first choice for India)

### Payment flow

```text
1. User clicks checkout
2. Backend creates order in DB
3. Backend creates Razorpay order
4. Frontend opens Razorpay checkout
5. User pays
6. Razorpay sends WEBHOOK to backend
7. Backend verifies signature
8. Backend marks payment success
9. Backend confirms order
10. Reduce stock (inventory ledger)
11. Send SMS / email / WhatsApp notification
```

### Critical rules
- **Never trust frontend payment success alone.**
- Always verify via **webhook + signature**.
- Store the **raw gateway response** for audit.
- Make payment updates **idempotent** (handle duplicate webhooks safely).

### Future options
- **Stripe** for international
- **COD** (cash on delivery)
- **Partial advance** for premium/custom sarees

---

## 12. API Design

### Public
```http
GET  /api/products
GET  /api/products/{slug}
GET  /api/categories
GET  /api/collections
GET  /api/search?q=...
```

### Auth / User
```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/otp/send
POST /api/auth/otp/verify
GET  /api/users/me
```

### Cart
```http
GET    /api/cart
POST   /api/cart/items
PUT    /api/cart/items/{id}
DELETE /api/cart/items/{id}
```

### Checkout / Orders
```http
POST /api/checkout
POST /api/payments/create-order
POST /api/payments/webhook
GET  /api/orders
GET  /api/orders/{id}
```

### Admin
```http
POST /api/admin/products
PUT  /api/admin/products/{id}
POST /api/admin/media/upload-url
GET  /api/admin/orders
PUT  /api/admin/orders/{id}/status
```

---

## 13. Authentication & API Security

### Auth
- **JWT access token** (short-lived, ~15 min) + **refresh token**
  (long-lived, rotated, stored hashed)
- Role-based access: `customer`, `admin`, `super_admin`
- Refresh tokens stored server-side (DB/Redis) so they can be revoked

### API security requirements
- HTTPS everywhere (TLS terminated at the Kubernetes Ingress/ALB)
- BCrypt password hashing (cost ≥ 12)
- Strict DTO validation on every endpoint (`@Valid` + Bean Validation) — never
  bind request bodies directly to JPA entities
- Centralized exception handler returning the standard error envelope (§9 API
  conventions) — never leak stack traces to clients
- Rate limiting on auth, OTP, and payment endpoints (e.g. Redis + Bucket4j, or
  an API-gateway/Ingress-level rate limiter)
- CORS locked to known frontend origins only
- Security headers via Spring Security (HSTS, X-Content-Type-Options,
  X-Frame-Options/CSP) on every response
- Webhook signature verification (Razorpay) on every payment callback
- Signed, short-expiry URLs for all media uploads
- Audit logs for all admin actions (who/what/when, append-only table)
- Secrets via **Kubernetes Secrets** (sealed/external-secrets from AWS
  Secrets Manager) — never baked into images or committed to git
- Automated DB backup & tested restore plan
- PCI handled entirely by the gateway — **never store raw card data**
- Dependency scanning (`mvn dependency-check` / `npm audit`/Dependabot) in CI
- Container image scanning (Trivy/ECR scan) before deploy

### Admin security
- 2FA for admin/super_admin users
- Optional IP allowlist for the admin panel
- Mandatory, queryable activity logs

---

## 14. Deployment Architecture (Docker + Kubernetes on AWS)

### Recommended cloud: **AWS**, orchestrated with **Kubernetes (EKS)**

| Component | Service |
|---|---|
| Container registry | AWS ECR |
| Orchestration | **AWS EKS (Kubernetes)** |
| Backend | Dockerized Spring Boot, deployed as a Kubernetes **Deployment** behind a **Service** + **Ingress** (ALB Ingress Controller) |
| Web frontend | Next.js — deployed on EKS as its own Deployment, or Vercel/Amplify if you want managed edge SSR (either is compatible; keep APIs identical) |
| Media workers | Separate Deployment consuming RabbitMQ, scaled via HPA on queue depth |
| Database | AWS RDS (PostgreSQL) — **not** run inside the cluster |
| Cache | AWS ElastiCache (Redis) |
| Message queue | Amazon MQ (RabbitMQ) or self-hosted RabbitMQ via a K8s StatefulSet for local/dev |
| Media storage | S3 |
| CDN | CloudFront |
| DNS | Route 53 |
| TLS | cert-manager (Let's Encrypt) or ACM via ALB |
| Secrets | AWS Secrets Manager + External Secrets Operator → K8s Secrets |
| Autoscaling | Horizontal Pod Autoscaler (CPU/memory + custom queue-depth metrics) + Cluster Autoscaler/Karpenter for nodes |
| Monitoring | Prometheus + Grafana + Loki (in-cluster) + CloudWatch + Sentry |

### Local development
`docker-compose.yml` runs Postgres, Redis, and RabbitMQ locally so the team
never needs a live cluster to develop against. Kubernetes manifests/Helm
charts mirror this setup for staging/production.

### Kubernetes structure
```text
/infra
  /docker            Dockerfiles for backend, web, workers
  /k8s (or /helm)
    base/            Deployments, Services, ConfigMaps, HPA, Ingress
    overlays/
      dev/
      staging/
      production/    (Kustomize overlays or separate Helm values files)
```
- Use **Kustomize** or **Helm** (pick one, don't mix) so dev/staging/prod
  share a base and only override replicas, resource limits, and env-specific
  config.
- Each service (backend, media workers, frontend) gets its own Deployment +
  readiness/liveness probes tied to the health check endpoint (Phase 0).
- Run at least 2 replicas of the backend in staging/production for zero-downtime
  rolling deploys.

**Environments:** `dev` → `staging` → `production`, each its own namespace
(or cluster, if budget allows). Never deploy directly from a local machine to
production — always via CI/CD (§15).

---

## 15. CI/CD

**Tools:** GitHub + GitHub Actions + Docker + Kubernetes (kubectl/Helm or ArgoCD)

```text
code push
  → run unit/integration tests
  → build Docker image → push to ECR
  → scan image (Trivy)
  → helm/kustomize apply to staging namespace
  → smoke test staging
  → manual approval
  → helm/kustomize apply to production namespace (rolling update)
```

Later, replace the manual `kubectl`/`helm` deploy step with **ArgoCD**
(GitOps): CI only builds/pushes the image and bumps the image tag in the
manifests repo; ArgoCD reconciles the cluster automatically.

---

## 16. Performance & Scalability

**Early stage:** a single Spring Boot app is fine.

**Scale strategy as traffic grows**
- Multiple backend instances behind a load balancer
- Redis caching for homepage & product pages
- CDN for all images/videos
- DB read replicas
- Async notifications via queue
- Queue-based media processing
- Dedicated search engine for large catalog

**Good things to cache:** homepage banners, product detail pages,
category listings, collections, stable pricing rules.

---

## 17. Search & Filtering

Customers filter by: color, fabric, price, collection, occasion,
latest arrivals, bestseller.

| Stage | Approach |
|---|---|
| **Start** | PostgreSQL indexes + filter APIs + full-text search on names/tags |
| **Later** | OpenSearch / Elasticsearch for typo-tolerance, ranking, facets |

---

## 18. Notifications

**Channels:** email, SMS, WhatsApp, push notifications.

**Events:** order confirmed, payment success, shipped, out for delivery,
delivered, abandoned cart, new arrivals.

Use **queue-based async processing** so payment/order APIs stay fast.

---

## 19. Analytics & Dashboard

**Admin dashboard shows:** daily sales, top-selling sarees, low-stock products,
abandoned carts, conversion rate, payment success rate, repeat customers,
traffic source.

**Tools:** Google Analytics (web traffic), Meta Pixel (ads),
custom reports from PostgreSQL.

---

## 20. Maintenance Plan

| Cadence | Tasks |
|---|---|
| **Daily** | Error monitoring, payment-failure checks, order sync, backup status |
| **Weekly** | DB health, slow-API review, security patching, media cleanup |
| **Monthly** | Backup restore drill, dependency upgrades, cost & performance review |

**Must-have operational items:** automated DB backups, log retention,
failure alerting, rollback strategy, a real staging environment.

---

## 21. Delivery Roadmap (MVP → Phase 2)

### MVP (first release)
- Website + admin panel
- Catalog with image/video upload
- Product detail pages
- Cart & checkout
- Razorpay payments
- Order management
- Customer login + wishlist
- Basic analytics

### Phase 2
- Mobile app + push notifications
- Advanced search & reviews
- Referral / influencer features
- Multilingual support
- International shipping & payments
- Recommendation engine

### Recommended build order
```text
1. Admin panel + backend
2. SEO-friendly website
3. Payment + order system
4. Media optimization pipeline
5. Mobile app
```

---

## 22. Final Recommended Stack

| Area | Final choice |
|---|---|
| Backend | **Spring Boot (Java 21)** |
| Web frontend | **Next.js** (Apple-style, shriimant-inspired UI) |
| Mobile app | **React Native** |
| Admin panel | **Next.js / React Admin** |
| Database | **PostgreSQL** |
| Cache | **Redis** |
| Media storage | **S3 + CloudFront**, video transcoded to **HLS** (MediaConvert/FFmpeg) or **Cloudinary/Mux** for MVP |
| Payments | **Razorpay** |
| Auth | **JWT + Refresh Token** |
| Containerization | **Docker** |
| Orchestration | **Kubernetes (AWS EKS)** |
| Infra | **EKS + ECR + RDS + ElastiCache + S3 + CloudFront** |
| CI/CD | **GitHub Actions** → Docker/ECR → Helm/Kustomize (ArgoCD later) |
| Monitoring | **Prometheus + Grafana + Loki + CloudWatch + Sentry** |

> This gives you a professional, scalable, production-ready platform without
> over-complicating it in the early stage. Build the admin + website first,
> get payments and orders solid, perfect the media/video pipeline, then ship
> the app — all on Docker + Kubernetes from day one so local, staging, and
> production stay consistent.
