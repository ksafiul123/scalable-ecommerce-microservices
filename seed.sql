-- ============================================================
-- ECOMMERCE SYSTEM — SEED DATA (corrected)
-- Run against ecommerce_db in DBeaver / pgAdmin / psql
-- All services must have started at least once (tables created)
-- ============================================================

-- ============================================================
-- 1. AUTH SERVICE
-- Table: roles  (id, name)
-- Table: users  (id, username, email, password, role_id, created_at)
-- Note: NO user_roles join table — role_id is a direct FK on users
-- Passwords = BCrypt of "password123"
-- ============================================================

INSERT INTO roles (name) VALUES ('ROLE_USER')  ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, email, password, role_id) VALUES
  ('admin',
   'admin@ecommerce.com',
   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
   (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')),

  ('alice',
   'alice@example.com',
   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
   (SELECT id FROM roles WHERE name = 'ROLE_USER')),

  ('bob',
   'bob@example.com',
   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
   (SELECT id FROM roles WHERE name = 'ROLE_USER')),

  ('charlie',
   'charlie@example.com',
   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
   (SELECT id FROM roles WHERE name = 'ROLE_USER'))

ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- 2. USER SERVICE
-- Table: user_profiles (id, user_id, email, username, full_name, phone, address, ...)
-- ============================================================

INSERT INTO user_profiles (user_id, email, username, full_name, phone, address) VALUES
  ((SELECT id FROM users WHERE email = 'admin@ecommerce.com'),
   'admin@ecommerce.com', 'admin', 'Admin User', '+1-555-0001', '1 Admin Lane, New York, NY 10001'),

  ((SELECT id FROM users WHERE email = 'alice@example.com'),
   'alice@example.com', 'alice', 'Alice Johnson', '+1-555-0101', '42 Maple Street, Boston, MA 02101'),

  ((SELECT id FROM users WHERE email = 'bob@example.com'),
   'bob@example.com', 'bob', 'Bob Smith', '+1-555-0202', '17 Oak Avenue, Chicago, IL 60601'),

  ((SELECT id FROM users WHERE email = 'charlie@example.com'),
   'charlie@example.com', 'charlie', 'Charlie Brown', '+1-555-0303', '99 Pine Road, Austin, TX 73301')

ON CONFLICT (user_id) DO NOTHING;

-- ============================================================
-- 3. PRODUCT SERVICE — categories (already seeded, but safe to re-run)
-- ============================================================

INSERT INTO categories (name, description) VALUES
  ('Electronics',    'Phones, laptops, accessories and gadgets'),
  ('Clothing',       'Men and women apparel, shoes and accessories'),
  ('Books',          'Fiction, non-fiction, academic and self-help'),
  ('Home & Kitchen', 'Appliances, cookware, furniture and decor'),
  ('Sports',         'Fitness equipment, outdoor gear and sportswear')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- 4. PRODUCT SERVICE — products
-- ============================================================

INSERT INTO products (name, description, price, stock, category_id, image_url) VALUES

  -- Electronics
  ('iPhone 15 Pro',
   'Apple iPhone 15 Pro with 48MP camera, A17 Pro chip, titanium design. 256GB.',
   999.99, 50,
   (SELECT id FROM categories WHERE name = 'Electronics'),
   'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=400'),

  ('Samsung Galaxy S24',
   'Snapdragon 8 Gen 3, 50MP triple camera, 6.2" Dynamic AMOLED display.',
   849.99, 40,
   (SELECT id FROM categories WHERE name = 'Electronics'),
   'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400'),

  ('MacBook Air M3',
   '13-inch MacBook Air with M3 chip, 8GB RAM, 256GB SSD. Ultra-thin fanless design.',
   1099.99, 25,
   (SELECT id FROM categories WHERE name = 'Electronics'),
   'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400'),

  ('Sony WH-1000XM5',
   'Industry-leading noise cancelling wireless headphones, 30-hour battery life.',
   349.99, 60,
   (SELECT id FROM categories WHERE name = 'Electronics'),
   'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400'),

  ('iPad Air 5th Gen',
   'M1 chip, 10.9-inch Liquid Retina display, USB-C, 5G capable.',
   599.99, 35,
   (SELECT id FROM categories WHERE name = 'Electronics'),
   'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400'),

  -- Clothing
  ('Levi''s 501 Jeans',
   'Classic straight fit 100% cotton denim jeans. Available in various washes.',
   59.99, 120,
   (SELECT id FROM categories WHERE name = 'Clothing'),
   'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400'),

  ('Nike Air Force 1',
   'Iconic basketball shoe with premium leather upper and Air-Sole cushioning.',
   90.00, 80,
   (SELECT id FROM categories WHERE name = 'Clothing'),
   'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400'),

  ('Adidas Ultraboost 23',
   'Running shoes with Boost midsole, Primeknit+ upper, Continental rubber outsole.',
   180.00, 55,
   (SELECT id FROM categories WHERE name = 'Clothing'),
   'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400'),

  ('Classic Cotton T-Shirt',
   '100% organic cotton crew-neck t-shirt. Available in 12 colors.',
   24.99, 200,
   (SELECT id FROM categories WHERE name = 'Clothing'),
   'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400'),

  -- Books
  ('Clean Code',
   'A Handbook of Agile Software Craftsmanship by Robert C. Martin.',
   34.99, 75,
   (SELECT id FROM categories WHERE name = 'Books'),
   'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400'),

  ('The Pragmatic Programmer',
   'Your Journey to Mastery by David Thomas and Andrew Hunt. 20th Anniversary Edition.',
   39.99, 60,
   (SELECT id FROM categories WHERE name = 'Books'),
   'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400'),

  ('Designing Data-Intensive Applications',
   'Reliable, Scalable, and Maintainable Systems by Martin Kleppmann.',
   44.99, 45,
   (SELECT id FROM categories WHERE name = 'Books'),
   'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400'),

  ('Atomic Habits',
   'An Easy & Proven Way to Build Good Habits & Break Bad Ones by James Clear.',
   16.99, 100,
   (SELECT id FROM categories WHERE name = 'Books'),
   'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400'),

  -- Home & Kitchen
  ('Instant Pot Duo 7-in-1',
   'Pressure cooker, slow cooker, rice cooker, steamer, sauté, yogurt maker. 6 quart.',
   89.99, 40,
   (SELECT id FROM categories WHERE name = 'Home & Kitchen'),
   'https://images.unsplash.com/photo-1585515320310-259814833e62?w=400'),

  ('Dyson V15 Detect',
   'Cordless vacuum with laser dust detection, HEPA filtration, 60 min run time.',
   649.99, 20,
   (SELECT id FROM categories WHERE name = 'Home & Kitchen'),
   'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400'),

  ('Nespresso Vertuo Next',
   'Coffee and espresso machine with centrifusion technology. All Vertuo pods compatible.',
   159.99, 30,
   (SELECT id FROM categories WHERE name = 'Home & Kitchen'),
   'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400'),

  -- Sports
  ('Yoga Mat Premium',
   'Non-slip 6mm TPE yoga mat with alignment lines, carrying strap. 183cm x 61cm.',
   45.99, 90,
   (SELECT id FROM categories WHERE name = 'Sports'),
   'https://images.unsplash.com/photo-1601925228516-37739ee39e1b?w=400'),

  ('Adjustable Dumbbell Set',
   'Quick-change 5-52.5 lb adjustable dumbbells. Replaces 15 sets of weights.',
   299.99, 15,
   (SELECT id FROM categories WHERE name = 'Sports'),
   'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400'),

  ('Resistance Bands Set',
   'Set of 5 latex bands (10-50 lbs). Includes door anchor, ankle straps and bag.',
   29.99, 150,
   (SELECT id FROM categories WHERE name = 'Sports'),
   'https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=400'),

  ('Garmin Forerunner 265',
   'GPS running smartwatch, AMOLED display, training readiness, 13-day battery.',
   449.99, 22,
   (SELECT id FROM categories WHERE name = 'Sports'),
   'https://images.unsplash.com/photo-1544117519-31a4b719223d?w=400')

ON CONFLICT DO NOTHING;

-- ============================================================
-- 5. ORDER SERVICE — orders + order_items
-- ============================================================

-- Order 1: Alice — MacBook + Headphones (DELIVERED)
INSERT INTO orders (user_id, status, total_amount, shipping_address, created_at, updated_at)
SELECT
  u.id, 'DELIVERED', 1449.98,
  '42 Maple Street, Boston, MA 02101',
  NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days'
FROM users u WHERE u.email = 'alice@example.com';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1, 1099.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'MacBook Air M3'
WHERE u.email = 'alice@example.com' AND o.status = 'DELIVERED';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1, 349.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Sony WH-1000XM5'
WHERE u.email = 'alice@example.com' AND o.status = 'DELIVERED';

-- Order 2: Bob — 3 Books (CONFIRMED)
INSERT INTO orders (user_id, status, total_amount, shipping_address, created_at, updated_at)
SELECT
  u.id, 'CONFIRMED', 119.97,
  '17 Oak Avenue, Chicago, IL 60601',
  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
FROM users u WHERE u.email = 'bob@example.com';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1, 34.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Clean Code'
WHERE u.email = 'bob@example.com' AND o.status = 'CONFIRMED';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1, 39.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'The Pragmatic Programmer'
WHERE u.email = 'bob@example.com' AND o.status = 'CONFIRMED';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 2, 16.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Atomic Habits'
WHERE u.email = 'bob@example.com' AND o.status = 'CONFIRMED';

-- Order 3: Charlie — Fitness gear (PENDING)
INSERT INTO orders (user_id, status, total_amount, shipping_address, created_at, updated_at)
SELECT
  u.id, 'PENDING', 375.98,
  '99 Pine Road, Austin, TX 73301',
  NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'
FROM users u WHERE u.email = 'charlie@example.com';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1, 45.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Yoga Mat Premium'
WHERE u.email = 'charlie@example.com' AND o.status = 'PENDING';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 2, 29.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Resistance Bands Set'
WHERE u.email = 'charlie@example.com' AND o.status = 'PENDING';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1, 449.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Garmin Forerunner 265'
WHERE u.email = 'charlie@example.com' AND o.status = 'PENDING';

-- Order 4: Alice — Shoes + T-shirts (SHIPPED)
INSERT INTO orders (user_id, status, total_amount, shipping_address, created_at, updated_at)
SELECT
  u.id, 'SHIPPED', 329.98,
  '42 Maple Street, Boston, MA 02101',
  NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day'
FROM users u WHERE u.email = 'alice@example.com';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1, 90.00
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Nike Air Force 1'
WHERE u.email = 'alice@example.com' AND o.status = 'SHIPPED';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1, 180.00
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Adidas Ultraboost 23'
WHERE u.email = 'alice@example.com' AND o.status = 'SHIPPED';

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 2, 24.99
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON p.name = 'Classic Cotton T-Shirt'
WHERE u.email = 'alice@example.com' AND o.status = 'SHIPPED';

-- ============================================================
-- 6. PAYMENT SERVICE — payments
-- ============================================================

-- Alice order 1 (DELIVERED) → COMPLETED payment
INSERT INTO payments (order_id, user_id, amount, status, stripe_session_id, stripe_payment_id, created_at, updated_at)
SELECT
  o.id,
  u.id,
  1449.98,
  'COMPLETED',
  'cs_test_seed_alice_delivered',
  'pi_test_seed_alice_delivered',
  NOW() - INTERVAL '10 days',
  NOW() - INTERVAL '10 days'
FROM orders o
JOIN users u ON o.user_id = u.id
WHERE u.email = 'alice@example.com' AND o.status = 'DELIVERED'
ON CONFLICT (order_id) DO NOTHING;

-- Alice order 2 (SHIPPED) → PENDING payment
INSERT INTO payments (order_id, user_id, amount, status, stripe_session_id, stripe_payment_id, created_at, updated_at)
SELECT
  o.id,
  u.id,
  329.98,
  'PENDING',
  'cs_test_seed_alice_shipped',
  NULL,
  NOW() - INTERVAL '5 days',
  NOW() - INTERVAL '5 days'
FROM orders o
JOIN users u ON o.user_id = u.id
WHERE u.email = 'alice@example.com' AND o.status = 'SHIPPED'
ON CONFLICT (order_id) DO NOTHING;

-- Bob order (CONFIRMED) → PENDING payment
INSERT INTO payments (order_id, user_id, amount, status, stripe_session_id, stripe_payment_id, created_at, updated_at)
SELECT
  o.id,
  u.id,
  119.97,
  'PENDING',
  'cs_test_seed_bob_confirmed',
  NULL,
  NOW() - INTERVAL '2 days',
  NOW() - INTERVAL '2 days'
FROM orders o
JOIN users u ON o.user_id = u.id
WHERE u.email = 'bob@example.com' AND o.status = 'CONFIRMED'
ON CONFLICT (order_id) DO NOTHING;

-- ============================================================
-- VERIFY (uncomment and run after seeding)
-- ============================================================
-- SELECT 'roles'         AS tbl, COUNT(*) FROM roles
-- UNION ALL SELECT 'users',         COUNT(*) FROM users
-- UNION ALL SELECT 'user_profiles', COUNT(*) FROM user_profiles
-- UNION ALL SELECT 'categories',    COUNT(*) FROM categories
-- UNION ALL SELECT 'products',      COUNT(*) FROM products
-- UNION ALL SELECT 'orders',        COUNT(*) FROM orders
-- UNION ALL SELECT 'order_items',   COUNT(*) FROM order_items
-- UNION ALL SELECT 'payments',      COUNT(*) FROM payments;
