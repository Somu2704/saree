# Project Instructions — Saree E-Commerce Platform

> Instruction file for the Claude IDE coding agent.
> Read this fully before writing any code. Follow it strictly.
> Companion design doc: `saree-ecommerce-system-design.md` (read it first).

---

## 1. What We Are Building

A startup-grade e-commerce platform for a **saree manufacturing business**.
The catalog is **photo & video heavy** (model shoots). It needs a public
website, an admin panel, online payments, inventory, orders, and (later) a
mobile app.

**Business priorities (in order):**
1. Beautiful, fast media (image/video) display.
2. Reliable payments and order management.
3. Easy product/media uploading for the internal team.
4. SEO so Google can rank product pages.
5. Scalability as traffic grows.

**Design inspiration:** Apple.com-grade visual polish (whitespace, large
full-bleed imagery, restrained typography, subtle motion) merchandised in the
product-forward style of shriimant.com (large tappable product tiles,
zoomable photos, hover/tap video previews, sticky mobile add-to-cart). See
`saree-ecommerce-system-design.md` §7 for the full UI/UX spec before building
any frontend screen.

---

## 2. Tech Stack (DO NOT change without asking)

| Layer | Technology |
|---|---|
| Backend | **Spring Boot (Java 21)**, Maven, Spring Web, Spring Data JPA, Spring Security |
| Web frontend | **Next.js (React, TypeScript)** |
| Mobile app | **React Native** (Phase 2) |
| Admin panel | **Next.js** (can share the web repo or be separate) |
| Database | **PostgreSQL** |
| Cache | **Redis** |
| Media storage | **AWS S3** (or Cloudinary) + **CloudFront** CDN |
| Payments | **Razorpay** |
| Async jobs | **RabbitMQ** (media processing, notifications) |
| Search | PostgreSQL full-text first, **OpenSearch** later |
| Auth | **JWT access + refresh tokens** |
| Infra | **Kubernetes** on AWS (EKS) |
| CI/CD | **GitHub Actions** (+ ArgoCD later) |
| Monitoring | Prometheus + Grafana + Loki + Sentry |

---

## 3. Build Order (Phases)

**Do NOT build everything at once. Build and verify phase by phase.**

### Phase 0 — Project setup
- Create repo structure (see section 4).
- Spring Boot skeleton + PostgreSQL + Flyway migrations + Docker Compose for local (Postgres, Redis, RabbitMQ).
- Health check endpoint. CI pipeline that builds and runs tests.

### Phase 1 — Backend core (START HERE)
1. **Auth & Users** — register, login, OTP, JWT + refresh, roles (customer/admin/super_admin).
2. **Catalog** — products, categories, collections, variants, CRUD + public read APIs.
3. **Media** — signed upload URLs to S3, store media metadata, async image-variant + **HLS video transcoding** (poster frame, adaptive renditions) via RabbitMQ worker. See design doc §10.
4. **Cart** — add/update/remove items, price snapshots.
5. **Orders** — checkout, order lifecycle (pending→paid→packed→shipped→delivered→returned/cancelled).
6. **Payments** — Razorpay create-order + **webhook verification (signature)** + idempotent status update + stock decrement.
7. **Inventory** — stock per product, inventory ledger, reservation at checkout.
8. **Admin APIs** — product/media management, order status, coupons, banners.
9. **Notifications** — async email/SMS/WhatsApp events via queue.

### Phase 2 — Web frontend
- Public website (home, listing, product detail with photos/videos, cart, checkout) built to the Apple/shriimant-style UI spec in design doc §7 (design tokens, responsive images, HLS video player, scroll motion).
- Admin panel (upload products/media, manage orders, stock, coupons).

### Phase 3 — Mobile app (React Native) + advanced search + reviews.

---

## 4. Repository Structure

```
/backend          Spring Boot modular monolith
  /src/main/java/com/saree/
    /auth
    /user
    /catalog
    /media
    /cart
    /order
    /payment
    /inventory
    /notification
    /admin
    /common        (config, security, exceptions, utils)
  /src/main/resources
    /db/migration  (Flyway SQL)
/web              Next.js website
/admin            Next.js admin (or a route group inside /web)
/mobile           React Native (Phase 3)
/infra            Dockerfiles, k8s manifests, Helm charts
/.github/workflows CI/CD
docker-compose.yml Local dev (Postgres, Redis, RabbitMQ)
```

Keep the backend a **modular monolith** now. Keep module boundaries clean so
Payment, Notification, Media, and Search can be extracted into separate
services later with minimal rework.

---

## 5. Database Rules

- Use **Flyway** migrations for every schema change. Never auto-generate schema in prod.
- Use the schema in `saree-ecommerce-system-design.md` section 9 as the source of truth.
- Store **snapshots** (price, name, address) on orders so history stays correct.
- **Never store media blobs in the DB** — only store S3/CDN URLs + metadata.
- Add indexes for search/filter columns (color, fabric, occasion, price, slug).

---

## 6. Payment Rules (critical)

- **Never trust the frontend** for payment success.
- Confirm orders ONLY via the **Razorpay webhook** after **signature verification**.
- Make webhook handling **idempotent** (handle duplicate/retried webhooks safely).
- Store the **raw gateway response** for audit.
- Decrement stock only after confirmed payment.
- Never store raw card data (PCI is the gateway's responsibility).

---

## 7. Security Rules

- HTTPS only. BCrypt password hashing.
- JWT access + refresh tokens; role-based authorization on every endpoint.
- Rate-limit auth and payment endpoints.
- Use **signed URLs** for media uploads.
- Validate and sanitize all input (DTO validation).
- Audit-log all admin actions.
- Secrets via env vars / AWS Secrets Manager — **never commit secrets**.

---

## 8. Coding Standards

- **Backend:** layered — Controller → Service → Repository. DTOs for all API
  input/output (never expose entities directly). Centralized exception handling
  with consistent error responses. Use constructor injection.
- **Frontend:** TypeScript strict mode. Reusable components. Server-side
  rendering for SEO on product pages. Environment-based API config.
- Write **unit tests** for services and **integration tests** for key flows
  (auth, checkout, payment webhook).
- Meaningful commit messages. Small, reviewable changes.
- Comment only non-obvious logic. No dead code.

---

## 9. API Conventions

- REST, JSON, versioned base path `/api`.
- Plural resource names: `/api/products`, `/api/orders`.
- Standard HTTP status codes. Consistent error body:
  `{ "error": { "code": "...", "message": "...", "details": [...] } }`.
- Pagination for list endpoints: `?page=&size=&sort=`.
- See section 12 of the design doc for the full endpoint list.

---

## 10. Definition of Done (per feature)

A feature is done only when:
1. Code compiles and passes lint.
2. Unit/integration tests written and passing.
3. Flyway migration added (if schema changed).
4. API documented (OpenAPI/Swagger).
5. Manual happy-path verified locally via Docker Compose.
6. No secrets committed; env vars documented in `.env.example`.

---

## 11. How the Agent Should Work

- **Work phase by phase.** Finish and verify one module before starting the next.
- **Ask before** changing the tech stack, DB schema shape, or payment flow.
- Prefer existing libraries/ecosystem tools over custom code.
- After each module: run tests, show what was built, and list what's next.
- If a requirement is ambiguous, ask a focused question instead of guessing.
- Keep the modular boundaries clean for future microservice extraction.

---

## 12. First Task for the Agent

Start with **Phase 0 + Phase 1 step 1 (Auth & Users)**:
1. Scaffold the backend Spring Boot project and repo structure.
2. Add Docker Compose (Postgres, Redis, RabbitMQ) and Flyway.
3. Implement Auth & User module (register, login, OTP stub, JWT + refresh, roles).
4. Add tests and a health check endpoint.
5. Stop and report before moving to the Catalog module.
